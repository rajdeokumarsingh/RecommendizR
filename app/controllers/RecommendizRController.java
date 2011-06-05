package controllers;

import utils.feed.RenderAtomJSon;
import utils.feed.RenderAtomXml;
import com.sun.syndication.feed.synd.SyndFeed;
import play.mvc.Controller;

/**
 * @author Jean-Baptiste lemée
 */
public class RecommendizRController extends Controller {

   /**
    * Return a 200 OK application/atom+xml response
    *
    * @param feed The Atom feed object
    */
   protected static void renderAtomXml(SyndFeed feed) {
      SyndFeed _feed = feed;
      throw new RenderAtomXml(_feed);
   }

   /**
    * Return a 200 OK application/atom+json response
    *
    * @param feed The Atom feed object
    */
   protected static void renderAtomJSon(SyndFeed feed) {
      SyndFeed _feed = feed;
      throw new RenderAtomJSon(_feed);
   }
}
