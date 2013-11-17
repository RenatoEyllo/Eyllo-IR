/**
 * 
 */
package com.eyllo.paprika.retriever.parser;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.avro.util.Utf8;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eyllo.paprika.retriever.parser.elements.EylloLink;
import com.eyllo.paprika.retriever.parser.elements.EylloLocation;
import com.eyllo.paprika.retriever.parser.elements.PersistentEntity;
import com.eyllo.paprika.retriever.parser.elements.PersistentPoint;

/**
 * @author renatomarroquin
 *
 */
public class RioGuiaParser {

  public static final String NAME = "rioGuia";

  private List<PersistentEntity> pEntities;
  
  private static String NUM_PARAM_STR = "NUM_PARAM";
  
  private final static String DEFAULT_RG_FOOD_SEARCH_URL = "/onde-comer/resultado?Pesquisa[estabelecimento]=all&Pesquisa[cozinha]=all&Pesquisa[bairro]=all&Pesquisa[preco]=all&page=NUM_PARAM";
  private final static String DEFAULT_RG_URL = "http://www.rioguiaoficial.com.br";

  /**
   * Default scenario for this site
   */
  private static Integer DEFAULT_SCENARIOID = 11;

  /**
   * Logger to help us write write info/debug/error messages
   */
  private static Logger LOGGER = LoggerFactory.getLogger(RioGuiaParser.class);

  /**
   * Default constructor
   */
  public RioGuiaParser(){
    this.pEntities = new ArrayList<PersistentEntity>();
  }

  /**
   * Gets entities from an specific URL
   * @return
   */
  public List<PersistentEntity> getEntities(){
      int iCnt = 0;
      String url = DEFAULT_RG_URL.concat(DEFAULT_RG_FOOD_SEARCH_URL);
      while ( iCnt < 52){
        LOGGER.debug("Getting: "+ url.replace(NUM_PARAM_STR, String.valueOf(iCnt)));
        this.parseSearchResults(url.replace(NUM_PARAM_STR, String.valueOf(iCnt)));
        iCnt+=1;
        //break;
      }
      System.out.println("Hubo # entidades : " + this.totalEntities());
    this.completeEntityInfo();
    return this.pEntities;
  }

  /**
   * Completes entity's information
   */
  public void completeEntityInfo(){
    LOGGER.info("Completing information");
    if (this.pEntities != null & this.pEntities.size() >0)
      for (PersistentEntity ent : this.pEntities){
        this.parseIndividualEnt(ent);
      }
    else
      LOGGER.error("Entities not found");
    LOGGER.info("Finished Completing information");
  }

  /**
   * Parsing existing entities
   * @param pEntity
   */
  public void parseIndividualEnt(PersistentEntity pEntity){
    Document doc = null;
    Iterator<Entry<Utf8, Utf8>> it = pEntity.getSameAs().entrySet().iterator();
    while (it.hasNext()) {
      Map.Entry<Utf8, Utf8> pairs = (Map.Entry<Utf8, Utf8>)it.next();
      
      // Reading individual URLs
      LOGGER.debug("Parsing entity from: " + ParseUtils.getUri(pairs.getKey().toString()).toASCIIString());
      doc = ParseUtils.connectGetUrl(ParseUtils.getUri(pairs.getKey().toString()).toASCIIString());
      if (doc == null)
        break;
      
      doc.setBaseUri(RioGuiaParser.DEFAULT_RG_URL);
      // Get address
      Element locElem = doc.select("div[class*=localizacao]").first();
      // adding persistentPoint
      PersistentPoint pPoint = extractPersistentPoint(doc, locElem);
      pEntity.setPersistentpoint(pPoint);
    }
  }

  /**
   * Extracts geolocation from a specific entity
   * @param pDoc
   * @param pLocElement
   * @return
   */
  private PersistentPoint extractPersistentPoint(Document pDoc, Element pLocElement){
    PersistentPoint pPoint = null;
    if (pLocElement != null && pLocElement.children() != null){
      Element el = (Element) pLocElement.children().toArray()[1];
      pPoint = new PersistentPoint();
      //LOGGER.info(el.ownText());
      String address = el.ownText();
      pPoint.setAddress(new Utf8(address));
      if (!address.equals("")){
        int javaScript = pDoc.select("script").html().indexOf("latitude:");
        
        if (javaScript > 0){
          String geoString = pDoc.select("script").html().substring(javaScript, javaScript + 100);
          String coordinates[] = geoString.split(",");
          pPoint.addToCoordinates(Double.parseDouble(coordinates[1].trim().replace("longitude: ", "")));
          pPoint.addToCoordinates(Double.parseDouble(coordinates[0].trim().replace("latitude: ", "")));
          pPoint.setAccuracy(EylloLocation.GEOCODER_VERIF_ACC_HIGH);
          
          //LOGGER.info(coordinates[0].trim().replace("latitude: ", ""));
          //LOGGER.info(coordinates[1].trim().replace("longitude: ", ""));
          /*int poss = geoString.indexOf("),");
          geoString = geoString.replace(geoString.substring(poss, geoString.length()),"");
          String coordinates[] = geoString.trim().split(",");
          System.out.println(geoString);
          pEntity.getPersistentpoint().addToCoordinates(Double.parseDouble(coordinates[1]));
          pEntity.getPersistentpoint().addToCoordinates(Double.parseDouble(coordinates[0]));
          */
          //System.out.println(pEntity);
        }
      }
    }
    return pPoint;
  }

  /**
   * Parse search results from a search result site
   * @param pUrl
   */
  private void parseSearchResults(String pUrl){
    LOGGER.info("Started parsing: " + pUrl);
    //File input = new File(pUrl);
    Document doc = null;
    
    doc = ParseUtils.connectGetUrl(ParseUtils.getUri(pUrl).toASCIIString());
    doc.setBaseUri(DEFAULT_RG_URL);
    Elements results = doc.select("div[class*=itensResultados]");
    for (Element result : results){
      PersistentEntity pEnt = new PersistentEntity();
      pEnt.addToScenarioId(RioGuiaParser.DEFAULT_SCENARIOID);
      EylloLink link = ParseUtils.detectUrl(result.children().select("p[class*=itenTitulo]").select("a").first());
      if ( link != null){
        pEnt.setName(new Utf8(link.getLinkText()));
        pEnt.putToSameAs(new Utf8(RioGuiaParser.DEFAULT_RG_URL + link.getLinkHref()), new Utf8(link.getLinkText()));
      }
      pEnt.setDescription(new Utf8(result.children().select("p[class*=itenTexto]").first().ownText()));
      this.pEntities.add(pEnt);
    }
  }

  /**
   * Returns the number of entities retrieved
   * @return
   */
  public int totalEntities(){
    return this.pEntities.size();
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    RioGuiaParser rioG = new RioGuiaParser();
    ParseUtils.writeJsonFile(rioG.getEntities(), "/Users/renatomarroquin/Documents/workspace/workspaceEyllo/Eyllo-IR/res/rioGuia.json");
  }

}
