package Utils.feed;

import com.sun.syndication.feed.module.Module;
import com.sun.syndication.feed.module.opensearch.OpenSearchModule;
import com.sun.syndication.feed.module.opensearch.impl.OpenSearchModuleImpl;

public class AtomSearchConstructor {
   private final OpenSearchModule openSearchModule = new OpenSearchModuleImpl();

   private AtomSearchConstructor() {

   }

   public static AtomSearchConstructor newSearch() {
      return new AtomSearchConstructor();

   }

   public Module value() {
      return openSearchModule;
   }

   public AtomSearchConstructor withTotalResults(int rowCount) {
      openSearchModule.setTotalResults(rowCount);
      return this;
   }

   public AtomSearchConstructor withStartIndex(int startIndex) {
      openSearchModule.setStartIndex(startIndex);
      return this;
   }

   public AtomSearchConstructor withItemsPerPage(int itemsPerPage) {
      openSearchModule.setItemsPerPage(itemsPerPage);
      return this;

   }
}
