package com.eyllo.paprika.retriever.parser;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.simple.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Entities.EscapeMode;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author renatomarroquin
 *
 */
public class StoresParser {

  private List<JSONObject> jsonObjects;
  
  private static String DEFAULT_KOPENHAGEN_URL = "/Users/renatomarroquin/Documents/workspace/workspaceCompanies/Eyllo-IR/res/kopenhagen/";
  
  private static String DEFAULT_LOJAS_AMERICANAS_URL = "http://ri.lasa.com.br/lojas/iframe?estado=";
  
  private static String DEFAULT_ENCODING = "UTF-8";

  private static Logger LOGGER = LoggerFactory.getLogger(StoresParser.class);
  
  private int scenarioId = 6;
  
  public StoresParser(){
    this.jsonObjects = new ArrayList<JSONObject>();
  }

  /**
   * Parsing the Kopenhagen web site directory
   * @throws Exception
   */
  @SuppressWarnings({ "rawtypes", "unchecked" })
  public void parseKopenhagen(String pUrl) throws Exception {
    //String url = DEFAULT_KOPENHAGEN_URL;
    String baseUri = "http://www.kopenhagen.com.br/site/nossas-lojas/";
    HashMap results = new HashMap();
    HashMap location = new HashMap();
    HashMap infoBox = new HashMap();
    
    String encoding = "UTF-8";
    String userId = "2";
    String recordType = "text";
    String storeName = "Kopenhagen";
    String infoBoxText = "";

    File input;
    /**
     * GoogleGeocoder gcc =  new GoogleGeocoder();
     */
    
    
    // Getting the file to be parsed
    input = new File(pUrl);
    Document doc = Jsoup.parse(input, encoding, baseUri);
    doc.outputSettings().escapeMode(EscapeMode.xhtml);
    
    // Getting the specific div
    Elements divs = doc.select("div");
    List<Element> divElements = null;
    for (Element div: divs){
      if (div.attr("id").equals("lojas")){
        divElements = div.getAllElements();
        break;
      }
    }

    // Iterating through the divs
    for(Element divNode: divElements){
      if (divNode.attr("id").length() > 0){
        if (divNode.attr("id").substring(0, 1).equals("p")){
          Elements liElements = divNode.select("li");
          for(Element liElement : liElements){
            // Getting the actual stores
            results.put("userId", Integer.parseInt(userId));
            results.put("type", recordType);
            results.put("title", storeName);
            if (liElement.attr("id").length() > 4)
              if (liElement.attr("id").substring(0, 4).equals("loja")){
                // Getting stores' information
                LOGGER.debug("Loja-id" + ":" + liElement.attr("id"));
                Elements storeInfoElements = liElement.getAllElements();
                for (Element storeInfoElem: storeInfoElements){
                  /*if (storeInfoElem.attr("class").equals("nr-loja"))
                    System.out.println("nr-loja" + ":" + storeInfoElem.html());
                  else */if (storeInfoElem.attr("class").equals("title-end-loja")){
                    results.put("text", storeInfoElem.html());
                    infoBox.put("title", storeInfoElem.html());
                  }
                  //System.out.println("title-end-loja" + ":" + storeInfoElem.html());
                  else if (storeInfoElem.attr("class").equals("servicos")){
                    Elements servicesElems = storeInfoElem.select("li");
                    LOGGER.debug("servicos" + ":");
                    for (Element servElem : servicesElems){
                      LOGGER.debug(servElem.select("img").first().attr("alt"));
                      infoBoxText = infoBoxText + servElem.select("img").first().attr("alt") + "<br />";
                    }
                    infoBox.put("text", infoBoxText);
                    results.put("infobox", infoBox);
                    infoBox = new HashMap();
                    results.put("scenarioId", scenarioId);
                    //scenarioId++;
                    this.jsonObjects.add(ParserUtils.getJsonObj(results));
                    results = new HashMap();
                  }
                  // Handling the address
                  else if (storeInfoElem.tagName().equals("p")){
                    String addressInfo[] = storeInfoElem.html().split("<br />");
                    for (int iCnt = 0; iCnt < addressInfo.length; iCnt++){
                      LOGGER.debug("addressInfo- " + addressInfo[iCnt].trim());
                      if (iCnt == 0) {
                      /**
                       *   gcc.geoCodeAddress(addressInfo[iCnt]);
                       *   location.put("lat", gcc.getLatitude());
                       *   location.put("lng", gcc.getLongitude());
                       */
                        
                        //addressInfo[iCnt] = gcc.getFormattedAddress();
                      }
                      infoBoxText = infoBoxText + addressInfo[iCnt] + "<br />";
                    }
                    results.put("location", location);
                    location = new HashMap();
                  }
                }
              }
          }//ENDFOR LI ELEMENTS
         }//ENDIF LI
      }// ENDIF DIV
      else
        LOGGER.debug("There wasn't a li element with loja");
    }// ENDFOR 
  }

  /**
   * Parsing LojasAmericanas Brazilian retail store
   */
  @SuppressWarnings({ "rawtypes", "unchecked" })
  public void parseLojasAmericanas(){
    LOGGER.info("Cleaning objects to re-fill them with new data");
    this.jsonObjects.clear();
    LOGGER.info("Getting LojasAmericanas");
    String[] brStates = {"MG", "RJ","AC","AL","AP","AM","BA","CE","DF","ES","GO","MA","MT","MS","PA","PB","PR","PE","PI","RN","RS","RO","RR","SC","SP","SE","TO"};
    HashMap results = new HashMap();
    /**
     * HashMap location = new HashMap();
     */
    HashMap infoBox = new HashMap();
    
    String userId = "2";
    String recordType = "text";
    String storeName = "LojasAmericanas";
    
    /**
     * GoogleGeocoder gcc = new GoogleGeocoder();
     */
    
    try {
      for(String state : brStates){
        Document doc = null;
        String localUrl = DEFAULT_LOJAS_AMERICANAS_URL + state;
        LOGGER.info("Doing it for " + state);
        try{
          doc = Jsoup.connect(localUrl).get();
        }
        catch (SocketTimeoutException se){
          LOGGER.error(localUrl);
          continue;
        }
        Elements divs = doc.select("div");
        
        for (Element div: divs){
          if (div.attr("class").equals("lojas")){
            
            String cityName = div.select("h3").text().split("-")[0].trim();
            LOGGER.debug("Parsing city: " + div.select("h3").text());
            // List of stores
            Elements liElements = div.select("li");
            for (Element liStore : liElements){
              results.put("userId", Integer.parseInt(userId));
              results.put("type", recordType);
              results.put("title", storeName);
              results.put("scenarioId", scenarioId);
              String sucName = liStore.select("a").first().text();
              results.put("text", sucName);
              
              // Setting up infobox
              infoBox.put("title", sucName);
              //System.out.println("StoreName: " + liStore.select("a").first().text());
              //System.out.println("StoreAddress: " + liStore.select("address").first().text().replaceAll("Veja Mapa", "") + ", " + cityName);
              String storeAddress = liStore.select("address").first().text().replaceAll("Veja Mapa", "") + ", " + cityName;
              infoBox.put("text", storeAddress);
              results.put("infobox", infoBox);
              
              /**
               * gcc.geoCodeAddress(storeAddress);
               * location.put("lat", gcc.getLatitude()); 
               * location.put("lng", gcc.getLongitude()); 
               * results.put("location", location);
               */
              
              
              this.jsonObjects.add(ParserUtils.getJsonObj(results));
              results = new HashMap();
              /**
               * location = new HashMap();
               */
              infoBox = new HashMap();
              //scenarioId++;
              
              //System.out.println("StoreNewAddress: " + gcc.getFormattedAddress());
              //System.out.println("Latitude: " + gcc.getLatitude());
              //System.out.println("longitude: " + gcc.getLongitude());
            }//ENDFOR LI ELEMENTS
            break;
          }//ENDIF 
        }//ENDFOR DIVS
        //break;
      }//ENDFOR STATES
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  public static void main(String args[]) throws Exception {
    StoresParser sp = new StoresParser();
    sp.parseKopenhagenBrasil(DEFAULT_KOPENHAGEN_URL);
    sp.writeJsonObj("./res/kopenhagen.json");
    sp.parseLojasAmericanas();
    sp.writeJsonObj("./res/lojasamericanas.json");
    
  }
  
  /**
   * Parsing Kopenhagen Brasil Store files
   * @param pPath
   */
  public void parseKopenhagenBrasil(String pPath){
    LOGGER.info("Cleaning objects to re-fill them with new data");
    this.jsonObjects.clear();
    File file = new File(pPath);
    try {
      if (file.isDirectory()) {
        File[] files = file.listFiles();
        for (File innerFile : files) {
          LOGGER.info("File: " + innerFile.getPath());
          this.parseKopenhagen(innerFile.getPath());
        }
      }
    } catch (Exception e) {
      LOGGER.error("Error while parsing Kopenhagen pages");
      e.printStackTrace();
    }
  }

  /**
   * Write JSON objects into files
   * @param pFilePath
   */
  private void writeJsonObj(String pFilePath){
    Writer fileWriter = null;
    try {
      fileWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(pFilePath), DEFAULT_ENCODING));
      for (JSONObject jsonObj: this.jsonObjects)
        fileWriter.write(jsonObj.toJSONString());
      fileWriter.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

}
