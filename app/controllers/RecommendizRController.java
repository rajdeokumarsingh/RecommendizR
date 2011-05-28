package controllers;

import Utils.feed.RenderAtomXml;
import com.sun.syndication.feed.synd.SyndFeed;
import play.mvc.Controller;
import play.mvc.results.RenderXml;

/**
 * @author Jean-Baptiste lemée
 */
public class RecommendizRController extends Controller {

   /**
    * Return a 200 OK text/xml response
    *
    * @param feed The Atom feed object
    */
   protected static void renderAtomXml(SyndFeed feed) {
      SyndFeed _feed = feed;
      throw new RenderAtomXml(_feed);
   }
}
