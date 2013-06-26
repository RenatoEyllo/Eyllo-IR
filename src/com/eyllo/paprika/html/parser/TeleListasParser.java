package com.eyllo.paprika.html.parser;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eyllo.paprika.entity.Entity;
import com.eyllo.paprika.entity.EntityUtils;
import com.eyllo.paprika.entity.elements.EylloLink;

public class TeleListasParser {

  private List<Entity> entities;
  
  private static String DEFAULT_TL_URL = "http://www.telelistas.net/rj/rio+de+janeiro/lanchonetes+restaurantes/?pagina=1";
  private static String DEFAULT_TL_DOC = "/Users/renatomarroquin/Documents/workspace/workspaceCompanies/Eyllo-IR/res/tl/Lanchonetes(Restaurantes)-RJTeleListas.net.html";
  
  /**
   * Default encoding for reading portuguese pages
   */
  private static String DEFAULT_ENCODING = "ISO-8859-1";//"UTF-8"

  /**
   * Logger to help us write write info/debug/error messages
   */
  private static Logger LOGGER = LoggerFactory.getLogger(TeleListasParser.class);
  
  /**
   * Default constructor
   */
  public TeleListasParser(){
    this.entities = new ArrayList<Entity>();
  }

  public static void main(String[] args) {
    TeleListasParser tlParser = new TeleListasParser();
    tlParser.parseRestaurants(DEFAULT_TL_URL);
  }
  
  public void parseRestaurants(String pUrl) {
    LOGGER.info("Starting to parse data from " + pUrl);
    Document doc = null;
    File input;

    try {
      input = new File(DEFAULT_TL_DOC);
      //doc = Jsoup.parse(new URL(DEFAULT_TL_URL), 30000); //doc = Jsoup.connect(DEFAULT_TL_URL.format(URLEncoder.encode(DEFAULT_TL_URL, "UTF-8"))).get();
      doc = Jsoup.parse(input, DEFAULT_ENCODING, DEFAULT_TL_URL);
    } catch (MalformedURLException e) {
      LOGGER.error("Error while trying to parse document " + DEFAULT_TL_DOC);
      e.printStackTrace();
    } catch (IOException e) {
      LOGGER.error("Error while trying to parse document " + DEFAULT_TL_DOC);
      e.printStackTrace();
    }

    Element div = doc.select("div[id=ctl00_Content_Regs]").first();
    Elements boxTables = div.children();//.select("table");

    for(Element boxTable : boxTables){
      // verifying if we are dealing with an info table
      if (boxTable.tagName() == "table"){
        //System.out.println(boxTable.text());
        Element infoTable = boxTable.children().select("table").first();
        Elements infoTableRows = infoTable.children().first().children();
        Entity tmpEntity = new Entity();
        // working with information rows
        for(Element infoRow : infoTableRows){
          // working with information cells //System.out.println("seeing tr");
          for(Element infoElement : infoRow.children()){
            //System.out.println("seeing td " + infoElement.className() + " ");
            /** gets the phone number of the entity */
            detectPhone(tmpEntity, infoElement.ownText());
            /** gets the main name of the entity */
            detectName(tmpEntity, infoElement);
            /** gets the entity address */
            detectLocation(tmpEntity, infoElement);
            /** gets the home page information */
            detectHomePage(tmpEntity, infoElement);
          }//END-FOR-TDs
        }//END-FOR-TR-s
        entities.add(tmpEntity);//System.out.println(infoTable.children().first().children().size());
      }//END-IF-TABLEs
    }//END-FOR-ELEMENTs
    LOGGER.info("Finished parsing.");

    LOGGER.info("Printing entities collected.");
    //ParseUtils.printEntities(entities);
  }

  /**
   * Detects if the text we are looking at has the home page link
   * @param pEntity
   * @param pInfoElement
   */
  public void detectHomePage(Entity pEntity, Element pInfoElement){
    if (pInfoElement.className().toString().equals("ib_ser"))
      for (Element child : pInfoElement.children()){
        if (!child.toString().equals("")){
          if (pEntity.getProperties(EntityUtils.HOME_PAGE) == null){
            EylloLink mainSite = ParseUtils.detectUrl(child);
            if (mainSite.getLinkText().equals("site"))
              pEntity.setProperties(EntityUtils.HOME_PAGE, getHomePageLink(mainSite.getLinkHref()));
          }
        }
      }
  }
  /**
   * Detects if the entity location is within an element "text_endereco_ib"
   * @param pEntity
   * @param pInfoElement
   */
  public void detectLocation(Entity pEntity, Element pInfoElement){
    if (pInfoElement.className().toString().equals("text_endereco_ib"))
      //for (Element child : infoElement.children()){
      //  System.out.println("+"+child.ownText());
        if (!pInfoElement.ownText().trim().equals(""))
          pEntity.setProperties(EntityUtils.LOCATION, pInfoElement.ownText().trim());
  }

  /**
   * Detects if the entity name is within an element "text_resultado_ib"
   * @param pEntity
   * @param pInfoElement
   */
  public void detectName(Entity pEntity, Element pInfoElement){
    if (pInfoElement.className().toString().equals("text_resultado_ib"))
      for (Element child : pInfoElement.children()){
        //System.out.println("+"+child.getAllElements().toString());
        // Detect URL to create main URL which contains the main name of the entity
        EylloLink tmpLink = ParseUtils.detectUrl(child);
        if (tmpLink != null){
          pEntity.setProperties(EntityUtils.NAME, tmpLink.getLinkText());
          pEntity.addLink(tmpLink);
        }
      }
  }

  /**
   * Extracts the main website from an external link
   * @param pOuterLink
   * @return
   */
  public String getHomePageLink(String pOuterLink){
    String homePage = "";
    int linkPos = pOuterLink.indexOf("link=") + 5;
    if (linkPos > 0){
      homePage = pOuterLink.substring(linkPos,pOuterLink.length());
      homePage = homePage.substring(homePage.indexOf("www"), homePage.length());
    }
    return homePage;
  }

  /**
   * Detects if the text contains a phone number
   * @param pText
   * @return
   */
  public void detectPhone(Entity pEntity, String pText){
    String phone = "";
    if (pText.contains("Tel:"))
      phone = pText.replace("Tel:", "").trim();
    if (!phone.equals(""))
      pEntity.setProperties(EntityUtils.PHONES, phone);
  }

  /**
   * @return the entities
   */
  public List<Entity> getEntities() {
    return entities;
  }

  /**
   * @param entities the entities to set
   */
  public void setEntities(List<Entity> entities) {
    this.entities = entities;
  }
}
