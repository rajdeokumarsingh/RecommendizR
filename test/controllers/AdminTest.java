package controllers;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.Play;
import play.libs.Crypto;
import play.mvc.Http;
import play.mvc.Http.Cookie;
import play.mvc.Http.Request;
import play.mvc.Http.Response;
import play.mvc.Http.StatusCode;
import play.test.Fixtures;
import play.test.FunctionalTest;

public class AdminTest extends ControllersTest {

   private static final Logger log = LoggerFactory.getLogger(AdminTest.class);

   @Test
   public void testAdminPageNotAllowed() {
      Response response = GET(connectedRequest(), "/admin/indexing");
      assertStatus(Http.StatusCode.FORBIDDEN, response);
   }

   @Test
   public void testAdminPageNotConnected() {
      Response response = GET("/admin/indexing");
      assertStatus(Http.StatusCode.FORBIDDEN, response);
   }

   @Test
   public void testAdminPageUnknownUser() {
      Response response = GET(connectedRequest("unknown@gmail.com"), "/admin/indexing");
      assertStatus(Http.StatusCode.FORBIDDEN, response);
   }

   @Test
   public void testAdminPageWorks() {
      Response response = GET(connectedRequest("jblemee@gmail.com"), "/admin/indexing");
      assertIsOk(response);
      assertContentType("text/html", response);
      //assertCharset("utf-8", response);
      assertContentEquals("OK", response);
   }

}