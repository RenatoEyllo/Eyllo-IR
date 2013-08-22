package com.eyllo.paprika.html.parser;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eyllo.paprika.entity.elements.EylloLink;
import com.eyllo.paprika.entity.elements.PersistentEntity;

public class ParseUtils {

  /**
   * Logger to help us write write info/debug/error messages
   */
  private static Logger LOGGER = LoggerFactory.getLogger(ParseUtils.class);

  /**
   * Default encoding for reading portuguese pages
   */
  public static String ISO_ENCODING = "ISO-8859-1";
  public static String UTF8_ENCODING = "UTF-8";
  
  /**
   * Detects if an element is a link and returns the object if it is
   * @param pElement
   * @return
   */
  public static EylloLink detectUrl(Element pElement){
    EylloLink tmpLink = null;
    if (pElement != null)
      if (pElement.tagName().equals("a")){
        if (!pElement.attr("href").contains("javascript")){
          tmpLink = new EylloLink();
          tmpLink.setLinkHref(pElement.attr("href")); // "http://example.com/"
          tmpLink.setLinkText(pElement.text()); // "example""
          tmpLink.setLink(pElement);
        }
        
      }
    return tmpLink;
  }

  /**
   * Returns an URI from a specific URL
   * @param pUrl
   * @return
   */
  public static URI getUri(String pUrl){
    URI uri = null;
    try {
      URL url = new URL(pUrl);
      String nullFragment = null;
      uri = new URI(url.getProtocol(), url.getHost(), url.getPath(), url.getQuery(), nullFragment);
    } catch (URISyntaxException e) {
      LOGGER.error("Error while trying to read the following URL: " + pUrl);
      e.printStackTrace();
    } catch (MalformedURLException e) {
      LOGGER.error("Error while trying to read the following URL: " + pUrl);
      e.printStackTrace();
    }
    return uri;
  }

  /**
   * Connects to a specific URL and returns its document
   * @param pUrl
   * @return
   */
  public static Document connectGetUrl(String pUrl){
    Document doc = null;
    try {
      doc = Jsoup.connect(pUrl).timeout(ConstantsParser.MAX_CONN_TIME).get();
    } catch (IOException e) {
      LOGGER.error("Error while connecting to " + pUrl);
      e.printStackTrace();
    }
    return doc;
  }

  /**
   * Method in charged of writing a list of entities into a file
   * @param pEntities   List of entities to be persisted.
   * @param pFilePath   File path where entities will be written.
   */
  public static void writeJsonFile(List<PersistentEntity> pEntities, String pFilePath){
    Writer fileWriter = null;
    try {
      fileWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(pFilePath), Charset.forName(UTF8_ENCODING)));
      for (PersistentEntity ent : pEntities){
        if ( ent != null && ent.getPersistentpoint() != null && 
             ent.getPersistentpoint().getAddress() != null &&
            !ent.getPersistentpoint().getAddress().toString().equals("") &&
            ent.getPersistentpoint().getCoordinates().size() > 0)
        fileWriter.write(ent.toJson().concat("\n"));
      }
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
  public static void printPersistentEntities(List<PersistentEntity> pEntities){
    for (PersistentEntity ent : pEntities)
      System.out.println(ent.toString());
  }
  
  /**
   * Method that will let us camel case strings
   * @param init
   * @return
   */
  public static String toCamelCase(final String init) {
    if (init==null)
        return null;

    final StringBuilder ret = new StringBuilder(init.length());

    for (final String word : init.split(" ")) {
        if (!word.isEmpty()) {
            ret.append(Character.toUpperCase(word.charAt(0)));
            ret.append(word.substring(1).toLowerCase());
        }
        if (!(ret.length()==init.length()))
            ret.append(" ");
    }
    return ret.toString().trim();
  }
}
