package com.eyllo.paprika.retriever;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to help common operations within Entity Retriever.
 * @author renato
 *
 */
public class RetrieverUtils {

  /** Logger to help us write write info/debug/error messages. */
  private static Logger LOGGER = LoggerFactory.getLogger(RetrieverUtils.class);

  /**
   * Gets properties from a file loaded from a specific URL.
   * @param pPropPath where properties are located at.
   * @return Properties loaded.
   */
  public static Properties getPropertiesFile(String pPropPath) {
    Properties prop = new Properties();
    InputStream input = null;
    try {
      input = new FileInputStream(pPropPath);
      // load a properties file
      prop.load(input);
    } catch (FileNotFoundException e) {
      getLogger().error("Error trying to read properties file.");
      e.printStackTrace();
    } catch (IOException e) {
      getLogger().error("Error trying to read properties file.");
      e.printStackTrace();
    }
    return prop;
  }

  /**
   * Gets logger.
   * @return Logger.
   */
  public static Logger getLogger() {
    return LOGGER;
  }

  /**
   * Sets logger.
   * @param lOGGER.
   */
  public static void setLogger(Logger lOGGER) {
    LOGGER = lOGGER;
  }
}