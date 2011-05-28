package Utils.feed;

import java.util.ArrayList;
import java.util.List;

import com.sun.syndication.feed.module.Module;
import com.sun.syndication.feed.synd.SyndCategory;
import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndEntryImpl;
import com.sun.syndication.feed.synd.SyndLink;

public class AtomEntryConstructor {
   private final SyndEntry entry = new SyndEntryImpl();

   private AtomEntryConstructor(String title) {
      entry.setTitle(title);
      entry.setLinks(new ArrayList<SyndLink>());

   }

   public SyndEntry value() {
      return entry;
   }

   public static AtomEntryConstructor newEntry(String title) {
      return new AtomEntryConstructor(title);

   }

   @SuppressWarnings("unchecked")
   public AtomEntryConstructor withAlternate(SyndLink alternate) {
      entry.getLinks().add(alternate);
      return this;
   }

   @SuppressWarnings("unchecked")
   public AtomEntryConstructor inCategories(SyndCategory... categories) {
      for (SyndCategory cat : categories) {
         entry.getCategories().add(cat);
      }
      return this;
   }

   @SuppressWarnings("unchecked")
   public AtomEntryConstructor inCategories(List<SyndCategory> categories) {
      for (SyndCategory cat : categories) {
         entry.getCategories().add(cat);
      }
      return this;
   }

   @SuppressWarnings("unchecked")
   public AtomEntryConstructor withLinks(SyndLink... links) {
      for (SyndLink link : links) {
         entry.getLinks().add(link);
      }
      return this;
   }

   @SuppressWarnings("unchecked")
   public AtomEntryConstructor withLinks(List<SyndLink> links) {
      for (SyndLink link : links) {
         entry.getLinks().add(link);
      }
      return this;
   }

   public AtomEntryConstructor hasDescription(SyndContent description) {
      entry.setDescription(description);
      return this;
   }

   @SuppressWarnings("unchecked")
   public AtomEntryConstructor withModule(Module module) {
      List<Module> modules = entry.getModules();
      if (modules == null) {
         modules = new ArrayList<Module>();
      }
      modules.add(module);
      return this;
   }
}
