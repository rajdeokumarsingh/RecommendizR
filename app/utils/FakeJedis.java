package utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import redis.clients.jedis.BinaryClient;
import redis.clients.jedis.JedisCommands;
import redis.clients.jedis.SortingParams;
import redis.clients.jedis.Tuple;

/**
 * @author Jean-Baptiste lemée
 */
public class FakeJedis implements JedisCommands {

   private static Map<String, String> basicMap = new ConcurrentHashMap<String, String>();
   private static Map<String, Map<String, String>> namedMap = new ConcurrentHashMap<String, Map<String, String>>();
   private static Map<String, Set<String>> namedSet = new ConcurrentHashMap<String, Set<String>>();
   private static Map<String, LinkedList<String>> namedLinkedList = new ConcurrentHashMap<String, LinkedList<String>>();

   public String set(String s, String s1) {
      return set(basicMap, s, s1);
   }

   public String set(Map<String, String> map, String s, String s1) {
      map.put(s, s1);
      return "OK";
   }

   public String get(String s) {
      return basicMap.get(s);
   }

   private String get(Map<String, String> map, String s) {
      return map.get(s);
   }

   public Boolean exists(String s) {
      throw new UnsupportedOperationException();
   }

   public String type(String s) {
      throw new UnsupportedOperationException();
   }

   public Long expire(String s, int i) {
      throw new UnsupportedOperationException();
   }

   public Long expireAt(String s, long l) {
      throw new UnsupportedOperationException();
   }

   public Long ttl(String s) {
      throw new UnsupportedOperationException();
   }

   public String getSet(String s, String s1) {
      throw new UnsupportedOperationException();
   }

   public Long setnx(String s, String s1) {
      throw new UnsupportedOperationException();
   }

   public String setex(String s, int i, String s1) {
      throw new UnsupportedOperationException();
   }

   public Long decrBy(String s, long l) {
      return incrBy(s, -l);
   }

   public Long decr(String s) {
      return decrBy(s, 1);
   }

   public Long incrBy(String s, long l) {
      return incrBy(basicMap, s, l);
   }

   private Long incrBy(Map<String, String> map, String s, long l) {
      long init = get(map, s) == null ? 0l : Long.valueOf(get(map, s));
      long newValue = init + l;
      set(map, s, String.valueOf(newValue));
      return newValue;
   }

   public Long incr(String s) {
      return incrBy(s, 1);
   }

   public Long append(String s, String s1) {
      throw new UnsupportedOperationException();
   }

   public String substr(String s, int i, int i1) {
      throw new UnsupportedOperationException();
   }

   public Long hset(String s, String s1, String s2) {
      if (namedMap.get(s) == null) {
         namedMap.put(s, new ConcurrentHashMap<String, String>());
      }
      Map<String, String> sMap = namedMap.get(s);
      if (sMap.get(s1) == null) {
         sMap.put(s1, s2);
         return 0l;
      } else {
         sMap.put(s1, s2);
         return 1l;
      }
   }

   public String hget(String s, String s1) {
      if (namedMap.get(s) == null) {
         return null;
      }
      return namedMap.get(s).get(s1);
   }

   public Long hsetnx(String s, String s1, String s2) {
      throw new UnsupportedOperationException();
   }

   public String hmset(String s, Map<String, String> stringStringMap) {
      throw new UnsupportedOperationException();
   }

   public List<String> hmget(String s, String... strings) {
      throw new UnsupportedOperationException();
   }

   public Long hincrBy(String s, String s1, long l) {
      if (namedMap.get(s) == null) {
         namedMap.put(s, new ConcurrentHashMap<String, String>());
      }
      Map<String, String> sMap = namedMap.get(s);
      return incrBy(sMap, s1, l);
   }

   public Boolean hexists(String s, String s1) {
      if (namedMap.get(s) == null) {
         return false;
      }

      return namedMap.get(s).get(s1) != null;
   }

   public Long hdel(String s, String s1) {
      if (namedMap.get(s) == null) {
         return 0l;
      }
      Map<String, String> sMap = namedMap.get(s);
      if (sMap.get(s1) == null) {
         return 0l;
      } else {
         sMap.remove(s1);
         return 1l;
      }
   }

   public Long hlen(String s) {
      if (namedMap.get(s) == null) {
         return 0l;
      }
      return Long.valueOf(namedMap.get(s).size());
   }

   public Set<String> hkeys(String s) {
      return basicMap.keySet();
   }

   public Collection<String> hvals(String s) {
      if (namedMap.get(s) == null) {
         return new ArrayList<String>();
      }

      return namedMap.get(s).values();
   }

   public Map<String, String> hgetAll(String s) {
      if (namedMap.get(s) == null) {
         return new ConcurrentHashMap<String, String>();
      }

      return namedMap.get(s);
   }

   public Long rpush(String s, String s1) {
      if (namedLinkedList.get(s) == null) {
         namedLinkedList.put(s, new LinkedList<String>());
      }
      if (namedLinkedList.get(s).contains(s1)) {
         return 0l;
      }
      namedLinkedList.get(s).addLast(s1);
      return 1l;
   }

   public Long lpush(String s, String s1) {
      if (namedLinkedList.get(s) == null) {
         namedLinkedList.put(s, new LinkedList<String>());
      }
      if (namedLinkedList.get(s).contains(s1)) {
         return 0l;
      }
      namedLinkedList.get(s).addFirst(s1);
      return 1l;
   }

   public Long llen(String s) {
      if (namedLinkedList.get(s) == null) {
         return 0l;
      }

      return Long.valueOf(namedLinkedList.get(s).size());
   }

   public List<String> lrange(String s, int i, int i1) {
      if (namedLinkedList.get(s) == null) {
         return new LinkedList<String>();
      } else {
         return namedLinkedList.get(s).subList(i, Math.min(i1, namedLinkedList.get(s).size() - 1));
      }
   }

   public String ltrim(String s, int i, int i1) {
      throw new UnsupportedOperationException();
   }

   public String lindex(String s, int i) {
      throw new UnsupportedOperationException();
   }

   public String lset(String s, int i, String s1) {
      throw new UnsupportedOperationException();
   }

   public Long lrem(String s, int i, String s1) {
      throw new UnsupportedOperationException();
   }

   public String lpop(String s) {
      throw new UnsupportedOperationException();
   }

   public String rpop(String s) {
      throw new UnsupportedOperationException();
   }

   public Long sadd(String s, String s1) {
      if (namedSet.get(s) == null) {
         namedSet.put(s, new HashSet<String>());
      }
      if (namedSet.get(s).contains(s1)) {
         return 0l;
      }
      namedSet.get(s).add(s1);
      return 1l;
   }

   public Set<String> smembers(String s) {
      if (namedSet.get(s) == null) {
         return new HashSet<String>();
      }
      return namedSet.get(s);
   }

   public Long srem(String s, String s1) {
      if (namedSet.get(s) == null) {
         return 0l;
      }
      if (namedSet.get(s).contains(s1)) {
         namedSet.get(s).remove(s1);
         return 1l;
      }
      return 0l;
   }

   public String spop(String s) {
      throw new UnsupportedOperationException();
   }

   public Long scard(String s) {
      throw new UnsupportedOperationException();
   }

   public Boolean sismember(String s, String s1) {
      throw new UnsupportedOperationException();
   }

   public String srandmember(String s) {
      throw new UnsupportedOperationException();
   }

   public Long zadd(String s, double v, String s1) {
      throw new UnsupportedOperationException();
   }

   public Set<String> zrange(String s, int i, int i1) {
      throw new UnsupportedOperationException();
   }

   public Long zrem(String s, String s1) {
      throw new UnsupportedOperationException();
   }

   public Double zincrby(String s, double v, String s1) {
      throw new UnsupportedOperationException();
   }

   public Long zrank(String s, String s1) {
      throw new UnsupportedOperationException();
   }

   public Long zrevrank(String s, String s1) {
      throw new UnsupportedOperationException();
   }

   public Set<String> zrevrange(String s, int i, int i1) {
      throw new UnsupportedOperationException();
   }

   public Set<Tuple> zrangeWithScores(String s, int i, int i1) {
      throw new UnsupportedOperationException();
   }

   public Set<Tuple> zrevrangeWithScores(String s, int i, int i1) {
      throw new UnsupportedOperationException();
   }

   public Long zcard(String s) {
      throw new UnsupportedOperationException();
   }

   public Double zscore(String s, String s1) {
      throw new UnsupportedOperationException();
   }

   public List<String> sort(String s) {
      throw new UnsupportedOperationException();
   }

   public List<String> sort(String s, SortingParams sortingParams) {
      throw new UnsupportedOperationException();
   }

   public Long zcount(String s, double v, double v1) {
      throw new UnsupportedOperationException();
   }

   public Set<String> zrangeByScore(String s, double v, double v1) {
      throw new UnsupportedOperationException();
   }

   public Set<String> zrangeByScore(String s, double v, double v1, int i, int i1) {
      throw new UnsupportedOperationException();
   }

   public Set<Tuple> zrangeByScoreWithScores(String s, double v, double v1) {
      throw new UnsupportedOperationException();
   }

   public Set<Tuple> zrangeByScoreWithScores(String s, double v, double v1, int i, int i1) {
      throw new UnsupportedOperationException();
   }

   public Long zremrangeByRank(String s, int i, int i1) {
      throw new UnsupportedOperationException();
   }

   public Long zremrangeByScore(String s, double v, double v1) {
      throw new UnsupportedOperationException();
   }

   public Long linsert(String s, BinaryClient.LIST_POSITION list_position, String s1, String s2) {
      throw new UnsupportedOperationException();
   }

   public static void reset() {
      basicMap.clear();
      namedMap.clear();
      namedSet.clear();
      namedLinkedList.clear();
   }
}
