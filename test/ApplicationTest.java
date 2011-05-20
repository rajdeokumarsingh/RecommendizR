import static Utils.Redis.newConnection;

import java.util.HashMap;
import java.util.Map;

import org.junit.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.Play;
import play.test.*;
import play.mvc.Http.*;
import redis.clients.jedis.JedisCommands;

public class ApplicationTest extends FunctionalTest {

   private static final Logger log = LoggerFactory.getLogger(ApplicationTest.class);

   @BeforeClass
   public static void init() {
      Play.configuration.setProperty("application.mode", "TEST");
   }

   @Test
   public void testThatIndexPageWorks() {
      Response response = GET("/");
      assertIsOk(response);
      assertContentType("text/html", response);
      assertCharset("utf-8", response);
   }

   @Test
   public void testHomePageWorks() {
      Response response = GET("/home");
      assertIsOk(response);
      assertContentType("text/html", response);
      assertCharset("utf-8", response);
   }

   @Test
   public void testUnknowLikedPageWorks() {
      Response response = GET("/liked/198767");
      assertIsNotFound(response);
      assertContentType("text/html", response);
      assertCharset("utf-8", response);
   }

   @Test
   public void testAddLikedPageWorks() {
      // shouldn't pass because we are not authentified.
      Map<String, String> parameters = new HashMap<String, String>();
      parameters.put("liked.name", "fakename");
      parameters.put("liked.description", "fakedescription");
      Response response = POST("/liked");
      assertStatus(302, response);
   }
}