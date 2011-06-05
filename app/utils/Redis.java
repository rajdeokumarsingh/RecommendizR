package utils;

import play.Play;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCommands;
import redis.clients.jedis.JedisShardInfo;

/**
 * @author Jean-Baptiste lemée
 */
public class Redis {

   static final JedisShardInfo redisConfig = new JedisShardInfo(
           Play.configuration.getProperty("redis.host", "localhost"),
           Integer.valueOf(Play.configuration.getProperty("redis.port", "6379")));

   static {
      redisConfig.setPassword(Play.configuration.getProperty("redis.password", "foobared"));
   }

   public static JedisCommands newConnection() {
      if (!Play.configuration.getProperty("application.mode").equalsIgnoreCase("prod")) {
         return new FakeJedis();
      }
      return new Jedis(redisConfig);
   }
}
