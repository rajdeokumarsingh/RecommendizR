package controllers;

import static utils.Redis.newConnection;

import java.util.HashMap;
import java.util.Map;

import utils.Twitter;
import com.google.gson.JsonObject;
import models.User;
import models.oauthclient.Credentials;
import play.Logger;
import play.libs.Crypto;
import play.libs.OpenID;
import play.modules.facebook.FbGraph;
import play.modules.facebook.FbGraphException;
import play.mvc.Router;

public class Security extends Secure.Security {

   private static final String LOGIN_KEY = "username";

   /**
    * Le parametre action ne sert à rien ?
    */
   public static void authenticate(String action, String openid_identifier, String openid_identifier_type) {
      if (OpenID.isAuthenticationResponse()) {
         OpenID.UserInfo verifiedUser = OpenID.getVerifiedID();
         if (verifiedUser == null) {
            flash.error("Erreur OpenID generique");
            Application.index();
         }

         String userEmail = verifiedUser.extensions.get("email");
         if (userEmail == null) {
            final String errorMessage = "L'identification de votre compte sur le site Recommandizer s'effectue avec votre email." +
                    " Vous devez authoriser le domaine recommendizr.com à accéder à votre email pour vous authentifier.";
            flash.error(errorMessage);
            Logger.info(errorMessage);
            Application.index();
         }

         User user = User.findByMailOrCreate(userEmail, newConnection());
         connect(user, true);
         Application.index();

      } else {
         if (openid_identifier == null) {
            flash.error("Param openid_identifier is null");
            Application.index();
         }
         if (openid_identifier.trim().isEmpty()) {
            flash.error("Param openid_identifier is empty");
            Application.index();
         }
         if ("twitter".equalsIgnoreCase(openid_identifier_type)) {
            doTwitter();
         } else if ("openid".equalsIgnoreCase(openid_identifier_type)) {
            doOpenId(openid_identifier);
         } else {
            flash.error("Param openid_identifier_type is not authorize");
            Application.index();
         }
      }
   }

   private static void doOpenId(String openid_identifier) {
      // Verify the id
      if (!OpenID.id(openid_identifier).required("email", "http://axschema.org/contact/email").verify()) {
         flash.put("error", "Impossible de s'authentifier avec l'URL utilisée.");
         Application.index();
      }
   }

   private static void doTwitter() {
      // 1: get the request token
      Map<String, Object> args = new HashMap<String, Object>(); // put the origin url here
      String callbackURL = Router.getFullUrl(request.controller + ".oauthTwitterCallback", args);
      Credentials creds = new Credentials();
      try {
         Twitter.getConnector().authenticate(creds, callbackURL);
      } catch (Exception e) {
         flash.error("OAuth failed");
         Application.index();
      }
   }

   public static void facebookLogin() {
      try {
         JsonObject profile = FbGraph.getObject("me"); // fetch the logged in user
         String email = profile.get("email").getAsString(); // retrieve the email
         User user = User.findByMailOrCreate(email, newConnection());
         connect(user, true);
         Application.index();
      } catch (FbGraphException fbge) {
         flash.error(fbge.getMessage());
         if (fbge.getType() != null && fbge.getType().equals("OAuthException")) {
            forbidden();
         }
      }
   }

   public static void oauthTwitterCallback(String callback, String oauth_token, String oauth_verifier) {
      try {
         User user = Twitter.oauthCallback(oauth_verifier);
         connect(user, true);
      } catch (Exception e) {
         forbidden(e.getMessage());
      }
      Application.index();
      //redirect(origin)
   }

   public static void logout() throws Throwable {
      session.clear();
      response.removeCookie("rememberme");
      Application.index();
   }

   public static void userName() throws Throwable {
      User user = connectedUser();
      if (null != user) {
         renderText(user.email);
      } else {
         notFound();
      }
   }

   public static User connectedUser() {
      return findUser(Secure.connected());
   }

   static void connect(User user, boolean rememberme) {
      // Mark user as connected
      session.put(Secure.LOGIN_KEY, user.email);
      if (rememberme) {
         response.setCookie("rememberme", Crypto.sign(user.email) + "-"
                 + user.email, "30d");
      }
   }

   static User findUser(String mail) {
      User user = User.findByMail(mail);
      return user;
   }

   static boolean check(String profile) {
      return profile.equals(session.get(LOGIN_KEY));
   }
}