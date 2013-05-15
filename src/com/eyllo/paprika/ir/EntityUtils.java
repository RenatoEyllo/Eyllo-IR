package com.eyllo.paprika.ir;

import java.util.HashMap;
import java.util.Map;

public class EntityUtils {

  public static String ABSTRACT = "abstract";
  public static String ASSETS = "assets";
  public static String COMMENT = "comment";
  public static String DEPICTION = "depiction";
  public static String FOUNDATION = "foundation";
  public static String FOUNDATION_PLACE = "foundationPlace";
  public static String FOUNDING_YEAR = "foundingYear";
  public static String HAS_PHOTO_COLLECTION = "hasPhotoCollection";
  public static String HOME_PAGE = "homepage";
  public static String INDUSTRY = "industry";
  public static String LABEL = "label";
  public static String LOCATION = "location";
  public static String LOGO = "logo";
  public static String NAME = "name";
  public static String NET_INCOME = "netIncome";
  public static String NUM_EMPLOYEES = "numberOfEmployees";
  public static String PHONES = "telephones";
  public static String PRODUCTS = "products";
  public static String REVENUE = "revenue";
  public static String SAME_AS = "sameAs";
  public static String SUBJECT = "subject";
  public static String THUMBNAIL = "thumbnail";
  public static String TYPE = "type";
  public static String WIKI_PAGE_EXTERNAL_LINK = "wikiPageExternalLink";
  public static String WIKI_PAGE_USES_TEMPLATE = "wikiPageUsesTemplate";
  
  /**
   * Initializes the properties HashMap
   * Based on http://dbpedia.org/page/Banco_de_Cr%C3%A9dito_del_Per%C3%BA for choosing the properties
   * @return
   */
  public static Map<String, Object> initializeProperties(){
    Map<String, Object> properties = new HashMap<String, Object>();
    properties.put(ABSTRACT, null);
    properties.put(ASSETS, null);
    properties.put(FOUNDATION_PLACE, null);
    properties.put(FOUNDING_YEAR, null);
    properties.put(LOCATION, null);
    properties.put(NET_INCOME, null);
    properties.put(NUM_EMPLOYEES, null);
    properties.put(REVENUE, null);
    properties.put(THUMBNAIL, null);
    properties.put(TYPE, null);
    properties.put(WIKI_PAGE_EXTERNAL_LINK, null);
    properties.put(FOUNDATION, null);
    properties.put(HAS_PHOTO_COLLECTION, null);
    properties.put(HOME_PAGE, null);
    properties.put(LOGO, null);
    properties.put(INDUSTRY, null);
    properties.put(NAME, null);
    properties.put(PRODUCTS, null);
    properties.put(PHONES, null);
    properties.put(WIKI_PAGE_USES_TEMPLATE, null);
    properties.put(SUBJECT, null);
    properties.put(COMMENT, null);
    properties.put(LABEL, null);
    properties.put(SAME_AS, null);
    properties.put(DEPICTION, null);
    return properties;
  }
}
