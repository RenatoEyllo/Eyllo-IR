/**
 * 
 */
package com.eyllo.paprika.social.connection;

import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eyllo.paprika.social.UtilsSocial;

import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.conf.ConfigurationBuilder;

/**
 * @author renatomarroquin
 *
 */
public class TwitterConnection implements AbstractSocialConnection{

  public int depthLevel;
  private String consumer_key;
  private String consumer_secret;
  private String access_token;
  private String access_token_secret;
  private Twitter twitter_conn;

  /**
   * Maximum number of tweets retrieved per page.
   */
  private static final int MAX_TWEETS_PER_PAGE = 200;

  /**
   * Minimum number of tweets retrieved per page.
   */
  private static final int MIN_TWEETS_PER_PAGE = 20;

  /**
   * Maximum number of pages retrieves.
   */
  private static final int MAX_TWEETS_PAGES = 25;

  /**
   * Default minimum number of users retrieved.
   */
  private static final int DEFAULT_MIN_USER = 0;

  /**
   * Logger to help us write write info/debug/error messages
   */
  private static Logger LOGGER = LoggerFactory.getLogger(TwitterConnection.class);

  /**
   * Default constructor
   */
  public TwitterConnection(){
  }

  /**
   * Constructor
   */
  public TwitterConnection(int pDepthLevel, String pConsumerKey,
      String pConsumerSecret, String pAccessToken, String pAccessTokenSecret){
    this.setDepthLevel(pDepthLevel);
    this.setConsumerKey(pConsumerKey);
    this.setConsumerSecret(pConsumerSecret);
    this.setAccessToken(pAccessToken);
    this.setAccessTokenSecret(pAccessTokenSecret);
  }

  /**
   * Establishes the authentication and the connection object
   */
  @Override
  public void initialize(){
      ConfigurationBuilder cb = new ConfigurationBuilder();
      cb.setDebugEnabled(true)
          .setOAuthConsumerKey(this.getConsumerKey())
          .setOAuthConsumerSecret(this.getConsumerSecret())
          .setOAuthAccessToken(this.getAccessToken())
          .setOAuthAccessTokenSecret(this.getAccessTokenSecret());
      twitter_conn = new TwitterFactory(cb.build()).getInstance();
  }

  @Override
  public void searchKeyword(String pKeyword, long pMaxResults) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void searchUser(String pUser, long pMaxResults) {
    LOGGER.info("Searching for user: " + pUser);
    try {
      // TODO we can get up to 51 pages each one of 20 tweets
      ResponseList<User> users = getTwitterConn().searchUsers(pUser, 51);
      if (users.size() == 0)
        System.out.println("there was no user with that name");
      System.out.println(users.size());
      for (User user : users){
        System.out.println(user.getURL() + " ;;; " + user.getScreenName());
      }
    } catch (TwitterException e) {
      LOGGER.error("Error searching user: " + pUser);
      e.printStackTrace();
    }
    
  }

  @Override
  public void searchUser(String pUser) {
    searchUser(pUser, DEFAULT_MIN_USER);
  }

  @Override
  public void fetchUserPosts(String pUser){
    this.fetchUserPosts(pUser, MAX_TWEETS_PAGES * MAX_TWEETS_PER_PAGE);
  }

  @Override
  public List<JSONObject> fetchUserPosts(String pUser, long pMaxResults) {//1
    LOGGER.info("Fetching posts for " + pUser);
    List<JSONObject> fetchedPosts = new ArrayList<JSONObject>();
    long tweetNumber = pMaxResults / MAX_TWEETS_PER_PAGE;
    long pageNumber = tweetNumber / MAX_TWEETS_PAGES;

    // The minimum number of posts is 20 by the 3rd party library
    if (tweetNumber == 0)
      tweetNumber = MIN_TWEETS_PER_PAGE;
    if (tweetNumber > MAX_TWEETS_PER_PAGE)
      tweetNumber = MAX_TWEETS_PER_PAGE;

    if (pageNumber == 0)
      pageNumber = 1;
    if (pageNumber > MAX_TWEETS_PAGES)
      pageNumber = MAX_TWEETS_PAGES;
    System.out.println(pageNumber);
    System.out.println(tweetNumber);
    
    for (int i = 1; i<= pageNumber; i++){ //25
      Paging twPaging = new Paging(i,tweetNumber);  //200
      ResponseList<Status> listado;
      try {
          listado = this.getTwitterConn().getUserTimeline(pUser,twPaging);
          System.out.println(getTwitterConn().showUser(pUser).getURL());
          //"https://twitter.com/#!"+status.getUser().getScreenName()+"/"+status.getId()
          if (listado.size() > 0){
              for (int j = 0; j < listado.size(); j++){
                fetchedPosts.add(UtilsSocial.getJson(listado.get(j)));
                //System.out.println( listado.get(j).getText() );
              }
              System.out.println(listado.size());
          }
      } catch (TwitterException e) {
        LOGGER.error("Error fetching user's posts!");
        e.printStackTrace();
      }
    }
    //results = DataObjectFactory.getRawJSON(listado);
    return fetchedPosts;
  }
  
  /**
   * Sets the depth level for retrieving tweets
   * @param DEPTH_LEVEL_
   */
  public void setDepthLevel(int pDepthLevel){
      depthLevel = pDepthLevel;
  }

  /**
   * Gets the depth level for retrieving tweets
   * @param DEPTH_LEVEL_
   */
  public int getDepthLevel(){
      return depthLevel;
  }

  /**
   * Sets the consumer key
   * @param CONSUMER_KEY_
   */
  public void setConsumerKey(String CONSUMER_KEY_){
      this.consumer_key = CONSUMER_KEY_;
  }

  /**
   * Gets the consumer key
   * @param CONSUMER_KEY_
   */
  public String getConsumerKey(){
      return this.consumer_key;
  }

  /**
   * Sets the secret consumer key
   * @param CONSUMER_SECRET_
   */
  public void setConsumerSecret(String CONSUMER_SECRET_){
      this.consumer_secret = CONSUMER_SECRET_;
  }

  /**
   * Gets the secret consumer key
   * @param CONSUMER_SECRET_
   */
  public String getConsumerSecret(){
      return this.consumer_secret;
  }

  /**
   * Sets the access_token
   * @param ACCESS_TOKEN_
   */
  public void setAccessToken(String ACCESS_TOKEN_){
      this.access_token = ACCESS_TOKEN_;
  }

  /**
   * Gets the access_token
   * @param ACCESS_TOKEN_
   */    
  public String getAccessToken(){
      return this.access_token;
  }

  /**
   * Sets the access_token_secret
   * @param ACCESS_TOKEN_SECRET_
   */
  public void setAccessTokenSecret(String ACCESS_TOKEN_SECRET_){
      this.access_token_secret = ACCESS_TOKEN_SECRET_;
  }

  /**
   * Gets the access_token_secret
   * @param ACCESS_TOKEN_SECRET_
   */
  public String getAccessTokenSecret(){
      return this.access_token_secret;
  }

  /**
   * Gets twitter connection
   * @return
   */
  public Twitter getTwitterConn(){
      if (twitter_conn == null) initialize();
      return twitter_conn;
  }

}
