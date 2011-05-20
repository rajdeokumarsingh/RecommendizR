package controllers;


import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashSet;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import models.Liked;
import models.User;
import play.Play;
import play.test.UnitTest;
import redis.clients.jedis.Jedis;

public class ApplicationTest extends UnitTest {
   private static final Logger log = LoggerFactory.getLogger(ApplicationTest.class);

   @BeforeClass
   public static void init() {
      Play.configuration.setProperty("application.mode", "TEST");
   }

   @Test
   public void fillWhenUserIsConnected() {

   }


}
