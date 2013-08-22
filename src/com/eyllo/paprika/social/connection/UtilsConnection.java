/**
 * 
 */
package com.eyllo.paprika.social.connection;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class used to maintain common objects for the social connection package.
 * @author renatomarroquin
 */
public class UtilsConnection {

  /**
   * String for building Twitter's data source connection.
   */
  public static final String TWITTER = "twitter";

  /**
   * String for defining the depth level of retrieving comments or users.
   */
  public static final String DEPTH_LEVEL = "depth_level";

  /**
   * String for defining the consumer key used in OAuth.
   */
  public static final String CONSUMER_KEY = "consumer_key";

  /**
   * String for defining the consumer secret key used in OAuth.
   */
  public static final String CONSUMER_SECRET = "consumer_secret";

  /**
   * String for defining the access token key used in OAuth.
   */
  public static final String ACCESS_TOKEN = "access_token";

  /**
   * String for defining the secret access token key used in OAuth.
   */
  public static final String ACCESS_TOKEN_SECRET = "access_token_secret";

  /**
   * Logger to help us write write info/debug/error messages
   */
  private static Logger LOGGER = LoggerFactory.getLogger(UtilsConnection.class);

  /**
   * Reads a properties file with a specific path.
   * @param pPath
   * @return
   */
  public static Properties getProperties(String pPath){
    Properties prop = new Properties();
    ClassLoader loader = Thread.currentThread().getContextClassLoader();           
    InputStream stream = loader.getResourceAsStream(pPath);
    try {
      prop.load(stream);
    } catch (IOException e) {
      LOGGER.error("Error while loading properties file. Please verify its path!");
      e.printStackTrace();
    }
    return prop;
  }
}
