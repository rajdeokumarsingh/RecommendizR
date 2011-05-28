package Utils.feed;

import java.io.IOException;
import java.io.StringWriter;


import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedOutput;
import com.thoughtworks.xstream.XStream;
import play.Logger;
import play.exceptions.UnexpectedException;
import play.mvc.Http.Request;
import play.mvc.Http.Response;
import play.mvc.results.Result;

/**
 * 200 OK with a application/atom+xml
 */
public class RenderAtomXml extends Result {

   String feed;

   public RenderAtomXml(SyndFeed feed) {
      StringWriter writer = new StringWriter();
      SyndFeedOutput out = new SyndFeedOutput();
      try {
         out.output(feed, writer);
      } catch (IOException e) {
         Logger.error(e, "Erreur d'entré/sortie (StringWriter) lors de la sérialisation du flux : " + e.getMessage());
      } catch (FeedException e) {
         Logger.error(e, "Erreur lors de la sérialisation du flux : " + e.getMessage());
      }
      this.feed = writer.toString();
   }

   public void apply(Request request, Response response) {
      try {
         setContentTypeIfNotSet(response, "application/atom+xml");


         response.out.write(feed.getBytes("utf-8"));
      } catch (Exception e) {
         throw new UnexpectedException(e);
      }
   }

}
