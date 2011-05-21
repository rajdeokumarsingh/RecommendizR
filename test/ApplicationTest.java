import static Utils.Redis.newConnection;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.junit.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.Play;
import play.libs.Crypto;
import play.mvc.Http;
import play.test.*;
import play.mvc.Http.*;

public class ApplicationTest extends FunctionalTest {

   private static final Logger log = LoggerFactory.getLogger(ApplicationTest.class);

   @BeforeClass
   public static void init() {
      Play.configuration.setProperty("application.mode", "TEST");
   }

   @Before
   public void setup() {
      Fixtures.deleteDatabase();
      Fixtures.loadModels("data.yml");

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
   public void testAddLikedPageNotConnected() {
      Map<String, String> parameters = new HashMap<String, String>();
      parameters.put("liked.name", "fakename");
      parameters.put("liked.description", "fakedescription");
      Response response = POST("/liked", parameters);
      assertStatus(Http.StatusCode.FORBIDDEN, response);
   }

   @Test
   public void testAddLikedPageWorks() {
      Map<String, String> parameters = new HashMap<String, String>();
      parameters.put("liked.name", "fakename");
      parameters.put("liked.description", "fakedescription");
      Response response = POST(connectedRequest(), "/liked", parameters);
      assertIsOk(response);
      assertContentType("application/json", response);
      assertCharset("utf-8", response);
      assertContentEquals("{\"name\":\"fakename\",\"description\":\"fakedescription\",\"liked\":true,\"ignored\":false,\"like\":1,\"ignore\":0,\"id\":1}", response);
   }

   @Test
   public void testAddLikedEmpty() {
      Map<String, String> parameters = new HashMap<String, String>();
      parameters.put("liked.name", "");
      parameters.put("liked.description", "");
      Response response = POST(connectedRequest(), "/liked", parameters);
      assertStatus(Http.StatusCode.BAD_REQUEST, response);
   }

   @Test
   public void testAddLikedNullDescription() {
      Map<String, String> parameters = new HashMap<String, String>();
      parameters.put("liked.name", "test");
      Response response = POST(connectedRequest(), "/liked", parameters);
      assertStatus(Http.StatusCode.BAD_REQUEST, response);
   }

   @Test
   public void testAddLikedNullName() {
      Map<String, String> parameters = new HashMap<String, String>();
      parameters.put("liked.description", "test");
      Response response = POST(connectedRequest(), "/liked", parameters);
      assertStatus(Http.StatusCode.BAD_REQUEST, response);
   }

   @Test
   public void testAddLikedEmptyParameters() {
      Map<String, String> parameters = new HashMap<String, String>();
      Response response = POST(connectedRequest(), "/liked", parameters);
      assertStatus(Http.StatusCode.BAD_REQUEST, response);
   }

   @Test
   public void testAddLikedNullParameters() {
      Response response = POST(connectedRequest(), "/liked");
      assertStatus(Http.StatusCode.BAD_REQUEST, response);
   }

   /*
    * force connection for test purpose.
    */
   private Request connectedRequest() {
      return connectedRequest("test@test.com");
   }

   /*
   * force connection for test purpose.
   */
   private Request connectedRequest(String username) {
      Request request = newRequest();
      Http.Cookie rememberme = new Http.Cookie();
      rememberme.value = Crypto.sign(username) + "-" + username;
      request.cookies.put("rememberme", rememberme);
      return request;
   }

   private static Response POST(Request request, String url, Map<String, String> parameters) {
      return POST(request, url, parameters, new HashMap<String, File>());
   }
}