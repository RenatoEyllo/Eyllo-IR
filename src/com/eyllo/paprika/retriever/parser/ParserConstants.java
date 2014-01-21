/**
 * 
 */
package com.eyllo.paprika.retriever.parser;

/**
 * @author renatomarroquin
 *
 */
public class ParserConstants {

  /** Default encoding for reading portuguese pages. */
  public static String ENCODING_ISO88591 = "ISO-8859-1";
  public static String ENCODING_UTF8 = "UTF-8";

  /** Parameter to be changed every iteration. */
  public static final String PARAM_NUM = "NUM_PARAM";

  /** Parameter to be changed every iteration. */
  public static final String PARAM_ENT_NAME = "ENT_PARAM";

  /** Parameter used for separating information. */
  public static final String INFO_SEP = " - ";

  /** Parameter used for separating description. */
  public static final String DESC_SEP = " : ";

  /** Parameter used for selecting a specific entity type. */
  public static final String ENTITY_RESTAURANTS = "restaurants";

  /** Parameter used for selecting a specific entity type. */
  public static final String ENTITY_HOTELS = "hotels";

  /** Maximum connection time to wait for connections. */
  public static final int MAX_CONN_TIME = 60000;

  /** Default number of pages to be searched. */
  public static final int DEFAULT_SEARCH_PAGES = 1;

  /** Default scenario for this site */
  public static final Integer DEFAULT_SCENARIOID = 20;

  /** Default output path for writing the files. */
  public static final String DEFAULT_OUTPUT_PATH = "/Users/renatomarroquin/Documents/workspace/workspaceEyllo/Eyllo-IR/res/";

  /** Default value for using local storage in the search process. */
  public static final boolean DEFAULT_USE_LOCAL = false;

  /** Default value to be used as server politeness between requests. */
  public static final int DEFAULT_REQ_POLITENESS = 5000;
}
