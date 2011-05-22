package controllers;

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

public class ApplicationTest extends ControllersTest {

   private static final Logger log = LoggerFactory.getLogger(ApplicationTest.class);

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
}