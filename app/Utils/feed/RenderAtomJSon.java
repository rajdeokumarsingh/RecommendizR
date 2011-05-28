package Utils.feed;

import org.w3c.dom.Document;

import Utils.json.JSONException;
import Utils.json.XML;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedOutput;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.json.JsonHierarchicalStreamDriver;
import play.Logger;
import play.exceptions.UnexpectedException;
import play.mvc.Http.Request;
import play.mvc.Http.Response;
import play.mvc.results.Result;

/**
 * 200 OK with a application/atom+json
 */
public class RenderAtomJSon extends Result {

   String feed;

   public RenderAtomJSon(SyndFeed feed) {
      SyndFeedOutput out = new SyndFeedOutput();
      try {

         XStream xstreamJSon = new XStream(new JsonHierarchicalStreamDriver());
         this.feed = XML.toJSONObject(out.outputString(feed)).toString();
      } catch (FeedException e) {
         Logger.error(e, "Erreur lors de la sérialisation du flux : " + e.getMessage());
      } catch (JSONException e) {
        Logger.error(e, "Erreur lors de la sérialisation du flux : " + e.getMessage());
      }
   }

   public void apply(Request request, Response response) {
      try {
         setContentTypeIfNotSet(response, "application/atom+json");

         response.out.write(feed.getBytes("utf-8"));
      } catch (Exception e) {
         throw new UnexpectedException(e);
      }
   }

}
