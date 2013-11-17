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
public class VejaSaoPauloParser {

  //http://vejasp.abril.com.br/estabelecimento/busca?per_page=1000&page=40&q=&fq=Restaurantes&bairro=&nome=&preco_maximo=&_=
  /**
   * List of entities to be filled up.
   */
  private List<PersistentEntity> pEntities;

  /**
   * Logger to help us write write info/debug/error messages
   */
  private static Logger LOGGER = LoggerFactory.getLogger(VejaSaoPauloParser.class);

  /**
   * Parser name.
   */
  public static final String NAME = "vejaSaoPaulo";

  /**
   * Default search URL for retrieving restaurants.
   */
  private final static String DEFAULT_VSP_FOOD_SEARCH_URL = "estabelecimento/busca?per_page=1000&page=NUM_PARAM&q=&fq=Restaurantes&bairro=&nome=&preco_maximo=&_=1374073064666";

  /**
   * Default main URL where entities will be gotten.
   */
  private final static String DEFAULT_VSP_URL = "http://vejasp.abril.com.br/";

  /**
   * ScenarioId to which the entity will be added to.
   */
  private static int scenarioId;

  /**
   * Variable to define the maximum number of pages to be searched.
   */
  private int maxPageNumber;

  /**
   * Default constructor
   */
  public VejaSaoPauloParser() {
    this.setMaxPageNumber(ConstantsParser.DEFAULT_SEARCH_PAGES);
    setScenarioId(ConstantsParser.DEFAULT_SCENARIOID);
    this.pEntities = new ArrayList<PersistentEntity>();
  }

  /**
   * Constructor.
   * @param pNumSearchPages number of pages to be evaluated.
   */
  public VejaSaoPauloParser(int pNumSearchPages) {
    this.setMaxPageNumber(pNumSearchPages);
    setScenarioId(ConstantsParser.DEFAULT_SCENARIOID);
    this.pEntities = new ArrayList<PersistentEntity>();
  }

  /**
   * Constructor.
   * @param pNumSearchPages number of pages to be evaluated.
   * @param pScenarioId scenarioId to be used.
   */
  public VejaSaoPauloParser(int pNumSearchPages, int pScenarioId){
    setScenarioId(pScenarioId);
    this.setMaxPageNumber(pNumSearchPages);
    this.pEntities = new ArrayList<PersistentEntity>();
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    VejaSaoPauloParser vjSap = new VejaSaoPauloParser();
    ParseUtils.writeJsonFile(vjSap.getEntities(), "/Users/renatomarroquin/Documents/workspace/workspaceEyllo/Eyllo-IR/res/vjSaoPaulo-10000.json");
  }

  /**
   * Gets entities from an specific URL
   * @return
   */
  public List<PersistentEntity> getEntities(){
      int iCnt = 0;
      String url = DEFAULT_VSP_URL.concat(DEFAULT_VSP_FOOD_SEARCH_URL);
      while ( iCnt < this.getMaxPageNumber()){
        iCnt+=1;
        LOGGER.info("Getting: "+ url.replace(ConstantsParser.PARAM_NUM, String.valueOf(iCnt)));
        this.parseSearchResults(url.replace(ConstantsParser.PARAM_NUM, String.valueOf(iCnt)));
        break;
      }
      LOGGER.info("Hubo # entidades : " + this.totalEntities());
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

  private boolean validateSite(Document pDoc) {
    if (pDoc.select("div[class*=error-content]") != null) 
      return true;
    return false;
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
      LOGGER.info("Parsing entity from: " + ParseUtils.getUri(pairs.getKey().toString()).toASCIIString());
      doc = ParseUtils.connectGetUrl(ParseUtils.getUri(pairs.getKey().toString()).toASCIIString());
      if (doc == null && !validateSite (doc)) {
        break;
      }
      else {
        doc.setBaseUri(VejaSaoPauloParser.DEFAULT_VSP_URL);
        StringBuilder strBuilder = new StringBuilder ();
        // getting working hours
        Elements workElems = doc.select("div[class*=information-unwanted]").select("div[class*=working-hours]");
        if (workElems !=  null && workElems.size() > 0){
          for(Element info : workElems.select("div[class*=hours]").select("p"))
            strBuilder.append(info.text().replace("-", "_")).append(ConstantsParser.INFO_SEP);
          pEntity.setSchedule(new Utf8(strBuilder.toString()));
        }

        // getting price range
        workElems = doc.select("div[class*=information-unwanted]").select("div[class*=price]").select("p[class*=price-range]");
        strBuilder.delete(0, strBuilder.length());
        if (workElems !=  null && workElems.size() > 0){
          strBuilder.append(doc.select("div[class*=price]").select("h3").first().text() + ConstantsParser.DESC_SEP);
          strBuilder.append(workElems.text());
          pEntity.addToExtraInfo(new Utf8(strBuilder.toString()));
          //LOGGER.debug(strBuilder.toString());
        }

        // getting payment information
        workElems = doc.select("div[class*=information-unwanted]").select("div[class*=payment]").select("p");
        strBuilder.delete(0, strBuilder.length());
        if (workElems != null && workElems.size() > 0){
          strBuilder.append(doc.select("div[class*=payment]").select("h3").first().text() + ConstantsParser.DESC_SEP);
          for (Element infoElem : workElems)
            if (!infoElem.text().trim().equals("")){
              strBuilder.append(infoElem.text().trim() + ConstantsParser.INFO_SEP);
            }
        }//END-IF_PAYMENT

        // getting services provided information
        workElems = doc.select("div[class*=information-unwanted]").select("div[class*=services]").select("div[class*=information-services]").select("p");
        strBuilder.delete(0, strBuilder.length());
        if (workElems != null && workElems.size() > 0){
          for (Element infoElem : workElems){
            if (infoElem.hasClass("observation")) {
              pEntity.addToExtraInfo(new Utf8("Observation :" + infoElem.text()));
            }
            else if (!infoElem.text().equals("")) {
              pEntity.addToServices(new Utf8(infoElem.text()));
            }
            //LOGGER.debug(infoElem.text());
          }
        }//END-IF_SERVICES

        // getting home url
        workElems = doc.select("div[class*=information-unwanted]").select("div[class*=website]");
        if (workElems != null && workElems.size() > 0){
          EylloLink homeLink = ParseUtils.detectUrl(workElems.select("div[class*=information-website]").select("p").select("a").first());
          if (homeLink != null){
            pEntity.setHomepage(new Utf8(homeLink.getLinkHref()));
            pEntity.putToSameAs(new Utf8(homeLink.getLinkHref()), new Utf8(homeLink.getLinkText()));
          }
        }//END-IF_URL
        pEntity.setDescription(new Utf8(""));
      }//END-IF_VALID_URL
    }//END-WHILE
  }
 
  /**
   * Parse search results from a search result site
   * @param pUrl
   */
  private void parseSearchResults(String pUrl){
    LOGGER.info("Started parsing: " + pUrl);
    Document doc = null;
    
    doc = ParseUtils.connectGetUrl(ParseUtils.getUri(pUrl).toASCIIString());
    doc.setBaseUri(DEFAULT_VSP_URL);
    Elements results = doc.select("div[class*=map-list-item]");
    for (Element result : results){
      PersistentEntity ent =  new PersistentEntity();
      Elements infoElement = result.select("div[class*=info-content]");
      LOGGER.debug(infoElement.select("p[class*=establishment-category]").first().ownText());
      String tmp = result.select("div[class*=info-content]").select("p[class*=establishment-category]").first().ownText();
      
      ent.setIndustry(new Utf8(tmp.split("/")[0]));
      ent.setLabel(new Utf8(tmp));
      // getting same as value to where it is
      EylloLink link = ParseUtils.detectUrl(infoElement.select("p[class*=establishment-name]").select("a").first());
      if (link != null){
        LOGGER.debug(DEFAULT_VSP_URL + link.getLinkHref());
        ent.putToSameAs(new Utf8(DEFAULT_VSP_URL + link.getLinkHref()),
            new Utf8(link.getLinkText()));
        ent.setName(new Utf8(link.getLinkText()));
      }
      // getting its address and phone
      PersistentPoint point = new PersistentPoint ();
      infoElement = result.select("div[class*=establishment-details]").select("p");
      ent.addToTelephones(new Utf8(infoElement.get(0).ownText()));
      point.setAddress(new Utf8(infoElement.get(0).text()));
      if (!result.attr("data-lng").toString().equals("") &&
          !result.attr("data-lat").toString().equals("")){
        // Format in [lon, lat], note, the order of lon/lat here in order to conform with GeoJSON.
        point.addToCoordinates(Double.parseDouble(result.attr("data-lng")));
        point.addToCoordinates(Double.parseDouble(result.attr("data-lat")));
        point.setAccuracy(EylloLocation.GEOCODER_VERIF_ACC_HIGH);
      }
      ent.setPersistentpoint(point);
      ent.addToScenarioId(getScenarioId());
      
      this.pEntities.add(ent);
    }
    LOGGER.info("Completed getting basic information from entities.");
  }
  /**
   * Returns the number of entities retrieved
   * @return
   */
  public int totalEntities(){
    return this.pEntities.size();
  }

  /**
   * @return the maxPageNumber
   */
  public int getMaxPageNumber() {
    return maxPageNumber;
  }

  /**
   * @param maxPageNumber the maxPageNumber to set
   */
  public void setMaxPageNumber(int maxPageNumber) {
    this.maxPageNumber = maxPageNumber;
  }

  /**
   * @return the scenarioId
   */
  public static int getScenarioId() {
    return scenarioId;
  }

  /**
   * @param scenarioId the scenarioId to set
   */
  public static void setScenarioId(int scenarioId) {
    VejaSaoPauloParser.scenarioId = scenarioId;
  }

}
