package com.eyllo.paprika.html.parser;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eyllo.paprika.entity.Entity;
import com.eyllo.paprika.entity.elements.EylloLink;

public class ParseUtils {

  /**
   * Logger to help us write write info/debug/error messages
   */
  private static Logger LOGGER = LoggerFactory.getLogger(ParseUtils.class);
  private static String DEFAULT_FILE_ENCODING = "UTF-8";
  
  /**
   * Detects if an element is a link and returns the object if it is
   * @param pElement
   * @return
   */
  public static EylloLink detectUrl(Element pElement){
    EylloLink tmpLink = null;
    if (pElement != null)
      if (pElement.tagName().equals("a")){
        tmpLink = new EylloLink();
        tmpLink.setLinkHref(pElement.attr("href")); // "http://example.com/"
        tmpLink.setLinkText(pElement.text()); // "example""
        tmpLink.setLink(pElement);
      }
    return tmpLink;
  }

  /**
   * Connects to a specific URL and returns its document
   * @param pUrl
   * @return
   */
  public static Document connectGetUrl(String pUrl){
    Document doc = null;
    try {
      doc = Jsoup.connect(pUrl).get();
    } catch (IOException e) {
      LOGGER.error("Error while connecting to " + pUrl);
      e.printStackTrace();
    }
    return doc;
  }

  public static void writeJsonFile(List<Entity> pEntities, String pFilePath){
    Writer fileWriter = null;
    try {
      fileWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(pFilePath), Charset.forName(DEFAULT_FILE_ENCODING)));
      for (Entity ent : pEntities)
        fileWriter.write(ent.toJson().concat("\n"));
      fileWriter.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
  
  /**
   * Method to build a JSONObject from a HashMap
   * @param tmp
   * @return
   */
  @SuppressWarnings({ "unchecked", "rawtypes"})
  public static JSONObject getJsonObj(Map tmp){
    JSONObject jsonObj = new JSONObject();
    jsonObj.putAll(tmp);
    return jsonObj;
  }
  
  public static void generateSeedFile(String pSeedFilePath){
    // 1. Read all sameAs attributes
    // 2. Write all sameAs links into an external file
  }

  /**
   * Prints entities' data
   * @param pEntities
   */
  public static void printEntities(List<Entity> pEntities){
    for (Entity ent : pEntities)
      System.out.println(ent.toString());
  }
}
