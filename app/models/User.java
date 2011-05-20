package models;

import javax.persistence.Entity;

import play.Logger;
import play.data.validation.Email;
import play.db.jpa.Model;
import redis.clients.jedis.JedisCommands;

/**
 * @author Jean-Baptiste Lemée
 */
@Entity
public class User extends Model {

   @Email
   public String email;

   public String twitter;

   public User(Long id) {
      super();
      this.id = id;
   }

   public User() {
      super();
   }

   /**
    * Recherche par email.
    */
   public static User findByMail(String mail) {
      if (mail == null) {
         return null;
      }
      return User.find("from User z where email=:mail").bind("mail", mail.trim()).first();
   }

   public static User findByTwitter(String twitter) {
      if (twitter == null) {
         return null;
      }
      return User.find("from User z where twitter=:twitter").bind("twitter", twitter.trim().toLowerCase()).first();
   }

   public static User findByMailOrCreate(String userEmail, JedisCommands jedis) {
      User user = findByMail(userEmail);
      if (user == null) {
         Logger.info("User creation for email : " + userEmail);
         user = new User();
         user.email = userEmail;
         create(jedis, user);
      }
      return user;
   }

   public static User findByTwitterOrCreate(String twitterName, JedisCommands jedis) {
      User user = findByTwitter(twitterName);
      if (user == null) {
         Logger.info("User creation for twitter : " + twitterName);
         user = new User();
         user.twitter = twitterName;
         user.email = "@" + twitterName;
         create(jedis, user);
      }
      if (user.email == null) {
         // correctif de migration, A virer quand tout sera stable.
         user.email = "@" + twitterName;
         user.save();
      }
      return user;
   }

   public static void create(JedisCommands jedis, User user) {
      user.save();
      jedis.sadd("users", String.valueOf(user.id));
   }

   @Override
   public String toString() {
      return email;
   }
}
