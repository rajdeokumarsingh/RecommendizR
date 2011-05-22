package controllers;

import java.io.IOException;

import services.SearchService;
import play.Logger;
import play.mvc.Controller;
import play.mvc.With;

/**
 * @author Jean-Baptiste lemée
 */
@With(Secure.class)
public class Admin extends Controller {

   @Check("jblemee@gmail.com")
   public static void indexing() {
      try {
         SearchService.buildIndexes();
      } catch (IOException e) {
         Logger.error(e, e.getMessage());
         error(e.getMessage());
      }

      renderHtml("OK");
   }
}
