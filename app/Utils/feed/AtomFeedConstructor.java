package Utils.feed;

import java.util.ArrayList;
import java.util.List;

import com.sun.syndication.feed.module.Module;
import com.sun.syndication.feed.synd.SyndCategory;
import com.sun.syndication.feed.synd.SyndCategoryImpl;
import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.feed.synd.SyndContentImpl;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.feed.synd.SyndFeedImpl;
import com.sun.syndication.feed.synd.SyndLink;
import com.sun.syndication.feed.synd.SyndLinkImpl;

public class AtomFeedConstructor {
   private final SyndFeed feed = new SyndFeedImpl();

   public static AtomFeedConstructor newFeed() {
      return new AtomFeedConstructor();
   }

   public AtomFeedConstructor() {

      feed.setFeedType("atom_1.0");
      feed.setEncoding("UTF-8");
      feed.setLinks(new ArrayList<SyndLink>());
   }

   public SyndFeed value() {
      return feed;
   }

   @SuppressWarnings("unchecked")
   public AtomFeedConstructor withAltLink(SyndLink alt) {
      this.feed.getLinks().add(alt);
      return this;
   }

   @SuppressWarnings("unchecked")
   public AtomFeedConstructor withLinks(SyndLink... links) {
      for (SyndLink link : links)
         feed.getLinks().add(link);
      return this;
   }

   @SuppressWarnings("unchecked")
   public AtomFeedConstructor withLinks(List<SyndLink> links) {
      for (SyndLink link : links)
         feed.getLinks().add(link);
      return this;
   }

   @SuppressWarnings("unchecked")
   public AtomFeedConstructor withEntries(SyndEntry... entries) {
      for (SyndEntry entry : entries)
         feed.getEntries().add(entry);
      return this;
   }

   @SuppressWarnings("unchecked")
   public AtomFeedConstructor withEntries(List<SyndEntry> entries) {
      for (SyndEntry entry : entries)
         feed.getEntries().add(entry);
      return this;
   }

   @SuppressWarnings("unchecked")
   public AtomFeedConstructor withModule(Module module) {
      List<Module> modules = feed.getModules();
      if (modules == null) {
         modules = new ArrayList<Module>();
      }
      modules.add(module);
      return this;
   }

   public enum LinkTypes {
      related, alternate, self, next
   }

   private static SyndLink newLink(LinkTypes type, String title, String href) {
      SyndLink link = new SyndLinkImpl();
      link.setRel(type.name());
      link.setTitle(title);
      link.setHref(href);
      return link;
   }

   public static SyndLink related(String title, String href) {
      return newLink(LinkTypes.related, title, href);
   }

   public static SyndLink next(String href) {
      return newLink(LinkTypes.next, "", href);
   }

   public static SyndLink self(String href) {
      return newLink(LinkTypes.self, "", href);
   }

   public static SyndLink alternate(String title, String href) {
      return newLink(LinkTypes.alternate, title, href);
   }

   public static SyndLink alternate(String href) {
      return newLink(LinkTypes.alternate, "", href);
   }

   public static SyndCategory cat(String name, String taxonomyUri) {
      SyndCategory cat1 = new SyndCategoryImpl();
      cat1.setName(name);
      cat1.setTaxonomyUri(taxonomyUri);
      return cat1;
   }

   public static SyndContent text(String value) {
      SyndContent textContent = new SyndContentImpl();
      textContent.setType("text");
      textContent.setValue(value);
      return textContent;

   }

}
