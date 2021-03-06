package controllers;

import static utils.Redis.newConnection;
import static controllers.Application.findLiked;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.eval.RecommenderBuilder;
import org.apache.mahout.cf.taste.impl.common.FastByIDMap;
import org.apache.mahout.cf.taste.impl.model.BooleanUserPreferenceArray;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.model.PreferenceArray;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.Recommender;

import models.Liked;
import models.User;
import play.Logger;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.With;
import redis.clients.jedis.JedisCommands;
import services.CrossingBooleanRecommenderBuilder;
import services.CrossingDataModelBuilder;
import services.SearchService;
import utils.paging.Paging;

@With(Secure.class)
public class Reco extends Controller {

   public static void like(Long likedId) {
      User user = Security.connectedUser();
      JedisCommands jedis = newConnection();
      if (!Liked.isLiked(likedId, user, jedis)) {
         doLike(likedId, String.valueOf(user.id), jedis);
      }
      if (Liked.isIgnored(likedId, user, jedis)) {
         doUnignore(likedId, user, jedis);
      }
      renderJSON(Liked.fill(Collections.singleton(findLiked(likedId)), user, jedis));
   }

   public static void ignore(Long likedId) {
      User user = Security.connectedUser();
      JedisCommands jedis = newConnection();
      if (!Liked.isIgnored(likedId, user, jedis)) {
         doIgnore(likedId, user, jedis);
      }
      if (Liked.isLiked(likedId, user, jedis)) {
         doUnlike(likedId, user, jedis);
      }

      renderJSON(Liked.fill(Collections.singleton(findLiked(likedId)), user, jedis));
   }

   static void doLike(Long likedId, String userId, JedisCommands jedis) {
      Long count = jedis.hincrBy("l" + likedId, "count", 1);
      manageRelevantList(likedId, count, jedis, "popular", 100);
      manageRelevantList(likedId, System.currentTimeMillis(), jedis, "recents", 100);
      manageRelevantList(likedId, System.currentTimeMillis(), jedis, "user:" + userId + ":recents", 100);
      jedis.hset("u" + userId, "like:l" + likedId, String.valueOf(likedId));

      // Useless ?
      jedis.hset("l" + likedId, "user:u" + userId, String.valueOf(userId));
   }

   static void doIgnore(Long likedId, User user, JedisCommands jedis) {
      jedis.hincrBy("l" + likedId, "countIgnore", 1);
      jedis.hset("ignore:u" + user.id, "like:l" + likedId, String.valueOf(likedId));
   }

   static void manageRelevantList(Long likedId, Long score, JedisCommands jedis, String listName, int size) {
      Map<String, String> mostRelevants = jedis.hgetAll(listName);
      if (mostRelevants == null || mostRelevants.size() < size) {
         jedis.hset(listName, String.valueOf(likedId), String.valueOf(score));
      } else {
         Map.Entry<String, String> smaller = getLessRelevant(mostRelevants, jedis);
         if (getLesserXThanY(smaller.getValue(), String.valueOf(score))) {
            jedis.hdel(listName, smaller.getKey());
            jedis.hset(listName, String.valueOf(likedId), String.valueOf(score));
         }
      }
   }

   static Map.Entry<String, String> getLessRelevant(Map<String, String> mostPopulars, JedisCommands jedis) {
      Map.Entry<String, String> lessLiked = null;
      for (Map.Entry<String, String> entry : mostPopulars.entrySet()) {
         if (lessLiked == null || getLesserXThanY(entry.getValue(), lessLiked.getValue())) {
            lessLiked = entry;
         }
      }
      return lessLiked;
   }

   static boolean getLesserXThanY(String x, String y) {
      if (Long.valueOf(x) < Long.valueOf(y)) {
         return Boolean.TRUE;
      }
      return Boolean.FALSE;
   }

   public static void unlike(Long likedId) {
      User user = Security.connectedUser();
      JedisCommands jedis = newConnection();
      doUnlike(likedId, user, jedis);
      renderJSON(Liked.fill(Collections.singleton(findLiked(likedId)), user, jedis));
   }

   static void doUnlike(Long likedId, User user, JedisCommands jedis) {
      jedis.hincrBy("l" + likedId, "count", -1);
      jedis.hdel("u" + user.id, "like:l" + likedId);
      jedis.hdel("l" + likedId, "user:u" + user.id);
   }

   public static void unIgnore(Long likedId) {
      User user = Security.connectedUser();
      JedisCommands jedis = newConnection();
      doUnignore(likedId, user, jedis);
      renderJSON(Liked.fill(Collections.singleton(findLiked(likedId)), user, jedis));
   }

   static void doUnignore(Long likedId, User user, JedisCommands jedis) {
      jedis.hincrBy("l" + likedId, "countIgnore", -1);
      jedis.hdel("ignore:u" + user.id, "like:l" + likedId);
   }

   public static void switchLike(Long likedId) {
      User user = Security.connectedUser();
      JedisCommands jedis = newConnection();
      if (Liked.isLiked(likedId, user, jedis)) {
         doUnlike(likedId, user, jedis);
      } else {
         doLike(likedId, String.valueOf(user.id), jedis);
         if (Liked.isIgnored(likedId, user, jedis)) {
            doUnignore(likedId, user, jedis);
         }
      }
      renderJSON(Liked.fill(Collections.singleton(findLiked(likedId)), user, jedis));
   }

   public static void switchIgnore(Long likedId) {
      User user = Security.connectedUser();
      JedisCommands jedis = newConnection();
      if (Liked.isIgnored(likedId, user, jedis)) {
         doUnignore(likedId, user, jedis);
      } else {
         doIgnore(likedId, user, jedis);
         if (Liked.isLiked(likedId, user, jedis)) {
            doUnlike(likedId, user, jedis);
         }
      }
      renderJSON(Liked.fill(Collections.singleton(findLiked(likedId)), user, jedis));
   }

   public static void addLiked(Liked liked) {
      if (StringUtils.isEmpty(liked.name) || StringUtils.isEmpty(liked.description)) {
         badRequest();
      }
      liked.save();
      User user = Security.connectedUser();
      JedisCommands jedis = newConnection();
      try {
         SearchService.addToIndex(liked);
      } catch (IOException e) {
         Logger.error(e, e.getMessage());
      }
      if(Secure.callFromExternalWebSite()) {
          doCORS();
          doLike(liked.id, request.headers.get("origin").value(), jedis);
      } else {
          doLike(liked.id, String.valueOf(user.id), jedis);
      }
      renderJSON(Liked.fill(liked, user, jedis));
   }

    private static void doCORS() {
        Http.Header hd = new Http.Header();
        hd.name = "Access-Control-Allow-Origin";
        hd.values = new ArrayList<String>();
        hd.values.add(request.headers.get("origin").value());
        Http.Response.current().headers.put("Access-Control-Allow-Origin", hd);
    }

    public static boolean isLiked(Long likedId) {
      User user = Security.connectedUser();
      return Liked.isLiked(likedId, user, newConnection());
   }

   public static boolean isIgnored(Long likedId) {
      User user = Security.connectedUser();
      return Liked.isIgnored(likedId, user, newConnection());
   }

   public static void recommendUser(int startIndex, int pageSize) throws TasteException {
      User user = Security.connectedUser();
      JedisCommands jedis = newConnection();
      int trainUsersLimit = 100;
      Map<String, String> ignoreList = jedis.hgetAll("ignore:u" + user.id);
      FastByIDMap<PreferenceArray> usersData = usersData(jedis, trainUsersLimit, ignoreList.keySet());
      usersData.put(user.id, getPreferences(jedis, trainUsersLimit++, user.id, new HashSet<String>()));
      List<RecommendedItem> recommendedItems = Paging.perform(_internalRecommend(startIndex * pageSize, user.id, usersData), startIndex, pageSize).getContent();
      Set<Liked> likedSet = new LinkedHashSet<Liked>(recommendedItems.size());
      for (RecommendedItem item : recommendedItems) {
         Liked liked = findLiked(item.getItemID());
         if (liked != null) {
            likedSet.add(liked);
         }
      }
      Liked.fill(likedSet, user, jedis);
      renderJSON(likedSet);
   }

   public static List<RecommendedItem> _internalRecommend(int howMany, Long userId, FastByIDMap<PreferenceArray> usersData) throws TasteException {
      RecommenderBuilder recommenderBuilder = new CrossingBooleanRecommenderBuilder();
      DataModel trainingModel = new CrossingDataModelBuilder().buildDataModel(usersData);
      Recommender recommender = recommenderBuilder.buildRecommender(trainingModel);
      return recommender.recommend(userId, howMany, null);
   }

   static FastByIDMap<PreferenceArray> usersData(JedisCommands jedis, int limit, Set<String> ignoredKeys) {
      FastByIDMap<PreferenceArray> result = new FastByIDMap<PreferenceArray>();
      Set<String> usersIds = jedis.smembers("users");
      int numUser = 0;
      for (String userId : usersIds) {
         if (numUser > limit) {
            break;
         }
         BooleanUserPreferenceArray preferenceArray = getPreferences(jedis, numUser, Long.valueOf(userId), ignoredKeys);
         result.put(Long.valueOf(userId), preferenceArray);
         numUser++;
      }

      return result;
   }

   static BooleanUserPreferenceArray getPreferences(JedisCommands jedis, int numUser, Long userId, Set<String> ignoredKeys) {
      Map<String, String> likedIds = jedis.hgetAll("u" + userId);
      BooleanUserPreferenceArray preferenceArray = new BooleanUserPreferenceArray(likedIds.size());
      preferenceArray.setUserID(numUser, userId);
      int numLiked = 0;
      for (Map.Entry<String, String> likedEntry : likedIds.entrySet()) {
         if (!ignoredKeys.contains("like:l" + likedEntry.getValue())) {
            preferenceArray.setItemID(numLiked++, Long.valueOf(likedEntry.getValue()));
         }
      }
      return preferenceArray;
   }


   protected static boolean userAlreadyExists(User user) {
      return (null != Security.findUser(user.email));
   }

   public static void recentUserLiked(int startIndex, int pageSize) {
      User user = Security.connectedUser();
      JedisCommands jedis = newConnection();
      List<Liked> list = Application.likedList(jedis, "user:" + user.getId() + ":recents", startIndex, pageSize).getContent();
      Liked.fill(list, user, jedis);
      renderJSON(list);
   }

}