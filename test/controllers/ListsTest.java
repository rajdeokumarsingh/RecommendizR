package controllers;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.mvc.Http;
import play.mvc.Http.Response;

public class ListsTest extends ControllersTest {

   private static final Logger log = LoggerFactory.getLogger(ListsTest.class);

   // Play 1.3 or play 1.2.2RC1 is needed.
   @Test
   public void testMostLiked() {
      addLiked("number one", "Description one");
      addLiked("number two", "Description two");
      addLiked("number three", "Description three");
      Response response = GET("/liked/most");
   }
}