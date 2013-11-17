/**
 * 
 */
package com.eyllo.paprika.enhancer.social.connections;

import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author renatomarroquin
 *
 */
public class SocialConnectionFactory {

  /**
   * Key string to be used for getting the social data source to be used.
   */
  private static final String SOCIAL_DATA_SOURCE = "social_data_source";

  /**
   * Logger to help us write write info/debug/error messages
   */
  private static Logger LOGGER = LoggerFactory.getLogger(SocialConnectionFactory.class);

  /**
   * Default depth level used for searching comments.
   */
  public static int DEFAULT_DEPTH_LEVEL = 0;

  /**
   * Default constructor
   */
  public SocialConnectionFactory() {
  }

  /**
   * Creates a connection to a specific social network using loaded properties.
   * @param pProperties
   * @return AbstractSocialConnection to a specific social network.
   */
  public static AbstractSocialConnection createConnection(Properties pProperties, String pDataSource){
    pProperties.put(SOCIAL_DATA_SOURCE, pDataSource);
    return createConnection(pProperties);
  }

  /**
   * Creates a connection to a specific social network using loaded properties.
   * @param pProperties
   * @return AbstractSocialConnection to a specific social network.
   */
  public static AbstractSocialConnection createConnection(Properties pProperties){
    AbstractSocialConnection socConn = null;
    String strSocDataSource = pProperties.getProperty(SOCIAL_DATA_SOURCE);
    if (pProperties != null && strSocDataSource != null && !strSocDataSource.equals("")){
      /** Creating a Twitter connection*/
      if (strSocDataSource.equals(UtilsConnection.TWITTER)){
        String tmpStr = pProperties.getProperty(UtilsConnection.DEPTH_LEVEL);
        int pDepthLevel = tmpStr!=null?Integer.parseInt(tmpStr):DEFAULT_DEPTH_LEVEL;
        if (verifyProperties(pProperties)){
          socConn = new TwitterConnection(pDepthLevel,
              pProperties.getProperty(UtilsConnection.CONSUMER_KEY),
              pProperties.getProperty(UtilsConnection.CONSUMER_SECRET),
              pProperties.getProperty(UtilsConnection.ACCESS_TOKEN),
              pProperties.getProperty(UtilsConnection.ACCESS_TOKEN_SECRET));
          socConn.initialize();
        }
        else
          LOGGER.error("OAuth connection parameters have not been defined!");
      }
      else
        LOGGER.error("Social netword data source has not been defined!");
    }
    return socConn;
  }

  /**
   * Verifies if the properties file loaded is complete or not.
   * @param pProperties
   * @return True or false depending on completeness.
   */
  private static boolean verifyProperties(Properties pProperties){
    boolean completeParams = false;
    if (pProperties.getProperty(UtilsConnection.CONSUMER_KEY) != null &&
        pProperties.getProperty(UtilsConnection.CONSUMER_SECRET) != null &&
        pProperties.getProperty(UtilsConnection.ACCESS_TOKEN) != null &&
        pProperties.getProperty(UtilsConnection.ACCESS_TOKEN_SECRET) != null )
      completeParams = true;
    return completeParams;
  }
}
