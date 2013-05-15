package com.eyllo.paprika.parser;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eyllo.paprika.html.elements.Link;
import com.eyllo.paprika.ir.Entity;
import com.eyllo.paprika.ir.EntityUtils;

public class TeleListasParser {

  private List<Entity> entities;
  
  private static String DEFAULT_TL_URL = "http://www.telelistas.net/rj/rio+de+janeiro/lanchonetes+restaurantes/?pagina=1";
  private static String DEFAULT_TL_DOC = "/Users/renatomarroquin/Documents/workspace/workspaceCompanies/Eyllo-IR/res/tl/Lanchonetes(Restaurantes)-RJTeleListas.net.html";
  
  private static String DEFAULT_ENCODING = "UTF-8";

  private static Logger LOGGER = LoggerFactory.getLogger(TeleListasParser.class);
  
  public static void main(String[] args) {
    // TODO Auto-generated method stub
    TeleListasParser tlParser = new TeleListasParser();
    tlParser.parseRestaurants(DEFAULT_TL_URL);
  }
  
  public void parseRestaurants(String pUrl) {
    LOGGER.info("Cleaning objects to re-fill them with new data");

    //this.jsonObjects.clear();
    Document doc = null;
    File input;
    try {
      input = new File(DEFAULT_TL_DOC);
      //doc = Jsoup.parse(new URL(DEFAULT_TL_URL), 30000);
      doc = Jsoup.parse(input, DEFAULT_ENCODING, DEFAULT_TL_URL);
    } catch (MalformedURLException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    //doc = Jsoup.connect(DEFAULT_TL_URL.format(URLEncoder.encode(DEFAULT_TL_URL, "UTF-8"))).get();
    Element div = doc.select("div[id=ctl00_Content_Regs]").first();
    Elements boxTables = div.children().select("table");
    for(Element boxTable : boxTables){
      Element infoTable = boxTable.children().select("table").first();
      Elements infoTableRows = infoTable.children().first().children();
      Entity tmpEntity = new Entity();
      
      for(Element infoRow : infoTableRows){
        System.out.println("seeing tr");
        for(Element infoElement : infoRow.children()){
          System.out.println("seeing td " + infoElement.className() + " ");
          // gets the text inside i.e. phone number
          System.out.println("->"+infoElement.ownText());
          String phone = detectPhone(infoElement.ownText());
          if (!phone.equals(""))
            tmpEntity.setProperties(EntityUtils.PHONES, phone);
          if (infoElement.className().toString().equals("text_resultado_ib"))
            for (Element child : infoElement.children()){
              // gets whatever else inside the <td> like links
              System.out.println("+"+child.getAllElements().toString());
              // TODO detect URL to create main URL
              Link tmpLink = detectUrl(child);
              if (tmpLink != null){
                tmpEntity.setProperties(EntityUtils.NAME, tmpLink.getLinkText());
                tmpEntity.addLink(tmpLink);
              }
            }
          //if (infoElement.className().toString().equals("text_endereco_ib"))
          //  for (Element child : infoElement.children())
          //    System.out.println("+"+child.toString());
          //infoElement.className();
          if (infoElement.className().toString().equals("ib_ser"))
              for (Element child : infoElement.children()){
                System.out.println("+"+child.toString());
              }
        }
        
      }
      System.out.println(infoTable.children().first().children().size());
      break;
//      if (boxTable.tag().toString() == "table"){
        //System.out.println(boxTable.toString());
        //System.out.println(boxTable.tagName());
        //break;
  //    }
    }
    LOGGER.info("Finished parsing.");
  }

  /**
   * Detects if the text contains a phone number
   * @param pText
   * @return
   */
  public String detectPhone(String pText){
    String phone = "";
    if (pText.contains("Tel:"))
      phone = pText.replace("Tel:", "").trim();
    return phone;
  }
  /**
   * Detects if an element is a link and returns the object if it is
   * @param pElement
   * @return
   */
  public Link detectUrl(Element pElement){
    Link tmpLink = null;
    if (pElement.tagName().equals("a")){
      tmpLink = new Link();
      tmpLink.setLinkHref(pElement.attr("href")); // "http://example.com/"
      tmpLink.setLinkText(pElement.text()); // "example""
      tmpLink.setLink(pElement);
    }
    return tmpLink;
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
