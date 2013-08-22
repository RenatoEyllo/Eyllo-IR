/**
 * 
 */
package com.eyllo.paprika.social;

import org.json.simple.JSONObject;

import twitter4j.Status;

/**
 * @author renatomarroquin
 *
 */
public class UtilsSocial {

  /**
   * Transforms a twitter4j.Status object into a JSONObject
   * @param pStatus a twitter4j.Status object
   * @return JSONObject
   */
  @SuppressWarnings("unchecked")
  public static JSONObject getJson(Status pStatus) {
    JSONObject jsonObj = null;
    if (pStatus != null) {
      jsonObj = new JSONObject();
      jsonObj.put("createdAt", pStatus.getCreatedAt());
      jsonObj.put("id", pStatus.getId());
      jsonObj.put("text", pStatus.getText());
      jsonObj.put("source", pStatus.getSource());
      jsonObj.put("isTruncated", pStatus.isTruncated());
      jsonObj.put("inReplyToStatusId", pStatus.getInReplyToStatusId());
      jsonObj.put("inReplyToUserId", pStatus.getInReplyToUserId());
      jsonObj.put("isFavorited", pStatus.isFavorited());
      jsonObj.put("isRetweeted", pStatus.isRetweeted());
      jsonObj.put("favoriteCount", pStatus.getFavoriteCount());
      jsonObj.put("inReplyToScreenName", pStatus.getInReplyToScreenName());
      jsonObj.put("geoLocation", pStatus.getGeoLocation());
      jsonObj.put("place", pStatus.getPlace());
      jsonObj.put("retweetCount", pStatus.getRetweetCount());
      jsonObj.put("isPossiblySensitive", pStatus.isPossiblySensitive());
      jsonObj.put("isoLanguageCode", pStatus.getIsoLanguageCode());
      jsonObj.put("contributorsIDs", pStatus.getContributors());
      jsonObj.put("retweetedStatus", pStatus.getRetweetedStatus());
      jsonObj.put("userMentionEntities", pStatus.getUserMentionEntities());
      jsonObj.put("urlEntities", pStatus.getURLEntities());
      jsonObj.put("hashtagEntities", pStatus.getHashtagEntities());
      jsonObj.put("mediaEntities", pStatus.getMediaEntities());
      jsonObj.put("currentUserRetweetId", pStatus.getCurrentUserRetweetId());
      jsonObj.put("user", pStatus.getUser());
      
    }
    return jsonObj;
  }
}
