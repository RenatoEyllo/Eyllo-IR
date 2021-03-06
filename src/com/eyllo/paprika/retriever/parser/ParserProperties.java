/**
 * 
 */
package com.eyllo.paprika.retriever.parser;

/**
 * Class containing default values for parsing web sites.
 * @author renatomarroquin
 *
 */
public class ParserProperties {

  /* Default values while parsing. */
  /** Parameter to be changed every iteration. */
  public static final String PARAM_NUM = "NUM_PARAM";
  /** Parameter to be changed every iteration. */
  public static final String PARAM_ENT_NAME = "ENT_PARAM";
  /** Parameter used for selecting a specific entity type. */
  public static final String ENTITY_RESTAURANTS = "restaurants";
  /** Parameter used for selecting a specific entity type. */
  public static final String ENTITY_HOTELS = "hotels";
  /** Default number of pages to be searched. */
  public static final int DEFAULT_SEARCH_PAGES = 1;

  /* Default values for outputting parsing results. */
  /** Default characters for ending a long string. */
  public static final String DEFAULT_STRING_FINAL_CHARS = "...";
  /** Default scenario for this site */
  public static final Integer DEFAULT_SCENARIOID = 20;
  /** Default output path for writing the files. */
  public static final String DEFAULT_OUTPUT_PATH = "/Users/renatomarroquin/Documents/workspace/workspaceEyllo/Eyllo-IR/res/";
  /** Default maximum length value for a string. */
  public static final int DEFAULT_STRING_MAX_LENGTH = 37;
  /** Default encoding for reading portuguese pages. */
  public static String ENCODING_ISO88591 = "ISO-8859-1";
  public static String ENCODING_UTF8 = "UTF-8";
  /** Parameter used for separating information. */
  public static final String INFO_SEP = " - ";
  /** Parameter used for separating description. */
  public static final String DESC_SEP = " : ";

  /* Default parser connection parameters. */
  /** Default fetchUrlTokens to be used when connecting to an URL. */
  public static final String DEFAULT_FETCH_URL_TOKENS = "";
  /** Default value for using local storage in the search process. */
  public static final boolean DEFAULT_USE_LOCAL = false;
  /** Default value to be used as server politeness between requests. */
  public static final int DEFAULT_REQ_POLITENESS = 5000;
  /** Maximum connection time to wait for connections. */
  public static final int MAX_CONN_TIME = 60000;
}
