/**
 * 
 */
package com.eyllo.paprika.social.connection;

import java.util.List;

import org.json.simple.JSONObject;

/**
 * @author renatomarroquin
 *
 */
public interface AbstractSocialConnection {

  /**
   * Method to be re-implemented to initialize connections.
   */
  public void initialize();

  public void searchKeyword(String pKeyword, long pMaxResults);
  
  /**
   * Searches for a specific single user.
   * @param pUser
   */
  public void searchUser(String pUser);

  /**
   * Searches for users whose name is similar to pUser.
   * @param pUser
   */
  public void searchUser(String pUser, long pMaxResults);

  /**
   * Fetches the maximum number of posts defined.
   * @param pUser
   */
  public void fetchUserPosts(String pUser);

  /**
   * Fetches a certain number of posts defined.
   * @param pUser
   * @return 
   */
  public List<JSONObject> fetchUserPosts(String pUser, long pMaxResults);
}
