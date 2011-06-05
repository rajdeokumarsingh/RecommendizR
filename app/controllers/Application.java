package controllers;

import static utils.Redis.newConnection;
import static utils.feed.AtomEntryConstructor.newEntry;
import static utils.feed.AtomFeedConstructor.cat;
import static utils.feed.AtomFeedConstructor.newFeed;
import static utils.feed.AtomFeedConstructor.self;
import static utils.feed.AtomFeedConstructor.text;
import static org.apache.commons.collections.CollectionUtils.isEmpty;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;
import javax.persistence.Query;

import org.apache.lucene.queryParser.ParseException;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.common.FastByIDMap;
import org.apache.mahout.cf.taste.impl.model.BooleanUserPreferenceArray;
import org.apache.mahout.cf.taste.model.PreferenceArray;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;

import utils.feed.AtomFeedConstructor;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Sets;
import com.sun.syndication.feed.synd.SyndEntry;
import models.Liked;
import models.User;
import play.Logger;
import play.db.jpa.JPA;
import play.mvc.Router;
import redis.clients.jedis.JedisCommands;
import services.SearchService;

public class Application extends RecommendizRController {

   public static void index() {
      render();
   }

   public static void home() {
      render();
   }

   public static void add() {
      render();
   }

   public static void liked(Long id) {
      Liked liked = findLiked(id);
      if (liked == null) {
         notFound();
      }
      User user = Security.connectedUser();
      Liked.fill(liked, user, newConnection());
      render(liked);
   }

   public static void search(String text) {
      Set<Liked> likedSet = null;
      try {
         List<Liked> likedList = SearchService.search(text);
         if (isEmpty(likedList)) {
            likedSet = Sets.newHashSet();
         } else {
            likedSet = Sets.newHashSet(likedList);
         }
      } catch (IOException e) {
         Logger.error(e, e.getMessage());
         error(e.getMessage());
      } catch (ParseException e) {
         error(e.getMessage());
      }
      Liked.fill(likedSet, Security.connectedUser(), newConnection());
      renderJSON(likedSet);
   }

   public static void searchfeed(String text) {
      Set<Liked> likedSet = null;
      try {
         List<Liked> likedList = SearchService.search(text);
         if (isEmpty(likedList)) {
            likedSet = Sets.newHashSet();
         } else {
            likedSet = Sets.newHashSet(likedList);
         }
      } catch (IOException e) {
         Logger.error(e, e.getMessage());
         error(e.getMessage());
      } catch (ParseException e) {
         error(e.getMessage());
      }
      Liked.fill(likedSet, Security.connectedUser(), newConnection());
      Map<String, Object> parametersMap = new HashMap<String, Object>(1);
      parametersMap.put("text", text);
      AtomFeedConstructor feedConstructor = newFeed()
              .withEntries(getEntries(likedSet))
              .withLinks(self(Router.getFullUrl("Application.searchfeed", parametersMap)));
      renderAtomJSon(feedConstructor.value());
   }

   static List<SyndEntry> getEntries(Iterable<Liked> likedList) {
      List<SyndEntry> entries = new ArrayList<SyndEntry>();
      for (Liked liked : likedList) {

         entries.add(newEntry(liked.name)
                 .inCategories(cat("LIKED ITEM", null))
                 .hasDescription(text(liked.description))
                         //.withLinks(alternate(URL(liked.getId())))
                 .value());
      }
      return entries;
   }

   public static void lastAdded(int startIndex, int pageSize) {
      User user = Security.connectedUser();
      JedisCommands jedis = newConnection();
      Collection<Liked> list = likedList(user, jedis, "recents");
      Liked.fill(list, Security.connectedUser(), jedis);
      renderJSON(list);
   }

   public static void recommendFromLiked(int limit, Long likedId) throws TasteException {
      if (likedId == null) {
         renderJSON(Sets.<Object>newHashSet());
      }
      JedisCommands jedis = newConnection();
      int trainUsersLimit = 100;
      Long userId = 0l; // fake user.
      FastByIDMap<PreferenceArray> usersData = Reco.usersData(jedis, trainUsersLimit, new HashSet<String>());
      BooleanUserPreferenceArray preferenceArray = new BooleanUserPreferenceArray(1);
      preferenceArray.setUserID(0, userId);
      preferenceArray.setItemID(0, likedId);
      usersData.put(userId, preferenceArray);
      List<RecommendedItem> recommendedItems = Reco._internalRecommend(limit, userId, usersData);
      Set<Liked> likedSet = new HashSet<Liked>(recommendedItems.size());
      for (RecommendedItem item : recommendedItems) {
         Liked liked = findLiked(item.getItemID());
         if (liked != null) {
            likedSet.add(liked);
         }
      }
      Liked.fill(likedSet, Security.connectedUser(), jedis);
      renderJSON(likedSet);
   }

   static <T extends Collection<Liked>> T removeIgnored(T likedCol, User user, JedisCommands jedis) {
      if (user == null || likedCol == null) {
         return likedCol;
      } else {
         Map<String, String> ignoreList = jedis.hgetAll("ignore:u" + user.id);
         for (Iterator<Liked> iter = likedCol.iterator(); iter.hasNext();) {
            Liked liked = iter.next();
            if (ignoreList.containsKey("like:l" + liked.id)) {
               iter.remove();
            }
         }
         return likedCol;
      }
   }

   public static void mostLiked(int howMany) {
      User user = Security.connectedUser();
      JedisCommands jedis = newConnection();
      List<Liked> list = likedList(user, jedis, "popular");
      Liked.fill(list, Security.connectedUser(), jedis);
      renderJSON(list);
   }

   static List<Liked> PagedLikedList(List<Liked> likedList, int startIndex, int pageSize) {
      if (null == likedList || likedList.size() < startIndex * pageSize) {
         return Collections.emptyList();
      } else if (likedList.size() < (startIndex * pageSize) + pageSize) {
         return likedList.subList(startIndex * pageSize, likedList.size());
      } else {
         return likedList.subList(startIndex * pageSize, (startIndex * pageSize) + pageSize);
      }

   }

   static List<Liked> likedList(User user, JedisCommands jedis, String listName) {
      final Map<String, String> relevantLikedMap = jedis.hgetAll(listName);
      if (relevantLikedMap == null || relevantLikedMap.size() == 0) {
         return new ArrayList<Liked>();
      } else {
         List<Liked> likedList = likedFromRelevantIds(relevantLikedMap.keySet());
         sortRelevantList(relevantLikedMap, likedList);
         return likedList;
      }
   }

   private static void sortRelevantList(final Map<String, String> relevantLikedMap, List<Liked> likedList) {
      Collections.sort(likedList, new Comparator<Liked>() {
         public int compare(Liked l1, Liked l2) {
            Long scoreL1 = Long.valueOf(relevantLikedMap.get(String.valueOf(l1.id)));
            Long scoreL2 = Long.valueOf(relevantLikedMap.get(String.valueOf(l2.id)));
            return scoreL1.equals(scoreL2) ? 0 : scoreL1 > scoreL2 ? -1 : 1;
         }
      });
   }


   private static List<Liked> likedFromRelevantIds(Collection<String> relevantLikedList) {
      Query query = JPA.em().createQuery("from Liked where id in (:list)");
      Collection<Long> ids = Collections2.transform(relevantLikedList, new Function<String, Long>() {
         public Long apply(@Nullable String s) {
            return Long.valueOf(s);
         }
      });
      query.setParameter("list", ids);
      return query.getResultList();
   }

   static Liked findLiked(long itemID) {
      return Liked.findById(itemID);
   }
}