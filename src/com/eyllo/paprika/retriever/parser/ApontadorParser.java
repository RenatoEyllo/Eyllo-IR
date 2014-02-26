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
public class ApontadorParser {

  /** Parser name. */
  public static final String NAME = "apontador";

  /** List of entities to be filled up. */
  private List<PersistentEntity> pEntities;

  /**
   * Variable to select which entity type to get.
   */
  private static String entityType;

  /**
   * ScenarioId to which the entity will be added to.
   */
  private static int scenarioId;

  /**
   * Variable to define the maximum number of pages to be searched.
   */
  private int maxPageNumber;

  /**
   * Logger to help us write write info/debug/error messages
   */
  private static Logger LOGGER = LoggerFactory.getLogger(ApontadorParser.class);

  /** Default search URL for retrieving restaurants. */
  private final static String DEFAULT_APO_RESTS_SEARCH_URL =
      "/em/rj/restaurantes?page=" + ParserProperties.PARAM_NUM;

  /** Default search URL for retrieving hotels. */
  private final static String DEFAULT_APO_HOTELS_SEARCH_URL =
      "/em/rj_rio-de-janeiro/hoteis-e-pousadas?page=" + ParserProperties.PARAM_NUM;

  /** Default main URL where entities will be gotten. */
  private final static String DEFAULT_APO_URL = "http://www.apontador.com.br";

  /** Default output file name */
  private static String DEFAULT_APO_OUTPUT_FILE_NAME =
      "apontador-" + ParserProperties.PARAM_ENT_NAME + ".json";

  /**
   * Default constructor
   */
  public ApontadorParser(){
    setEntityType("");
    setScenarioId(ParserProperties.DEFAULT_SCENARIOID);
    this.setMaxPageNumber(ParserProperties.DEFAULT_SEARCH_PAGES);
    this.pEntities = new ArrayList<PersistentEntity>();
  }

  /**
   * Constructor
   * @param pEntityType to decide what to retrieve.
   */
  public ApontadorParser(String pEntityType){
    setEntityType(pEntityType);
    setScenarioId(ParserProperties.DEFAULT_SCENARIOID);
    this.setMaxPageNumber(ParserProperties.DEFAULT_SEARCH_PAGES);
    this.pEntities = new ArrayList<PersistentEntity>();
  }

  /**
   * Constructor
   * @param pEntityType to decide what to retrieve.
   * @param pNumSearchPages to decide how many pages will be searched.
   */
  public ApontadorParser(String pEntityType, int pNumSearchPages){
    setEntityType(pEntityType);
    setScenarioId(ParserProperties.DEFAULT_SCENARIOID);
    this.setMaxPageNumber(pNumSearchPages);
    this.pEntities = new ArrayList<PersistentEntity>();
  }

  /**
   * 
   * @param pEntityType
   * @param pNumSearchPages
   * @param pScenarioId
   */
  public ApontadorParser(String pEntityType, int pNumSearchPages, int pScenarioId){
    setEntityType(pEntityType);
    setScenarioId(pScenarioId);
    this.setMaxPageNumber(pNumSearchPages);
    this.pEntities = new ArrayList<PersistentEntity>();
  }

  /**
   * Returns the number of entities retrieved
   * @return
   */
  public int totalEntities(){
    return this.pEntities.size();
  }

  /**
   * Gets a specific URL type to get entities from.
   * @return
   */
  private String getEntitySearchUrl(){
    if (getEntityType().equals(ParserProperties.ENTITY_RESTAURANTS)) {
      return DEFAULT_APO_RESTS_SEARCH_URL;
    }
    else if (getEntityType().equals(ParserProperties.ENTITY_HOTELS)) {
      return DEFAULT_APO_HOTELS_SEARCH_URL;
    }
    else {
      LOGGER.error("Entity type has NOT been defined.");
      LOGGER.warn("Parsing restaurants instead.");
      return DEFAULT_APO_RESTS_SEARCH_URL;
    }
  }

  /**
   * Gets entities from an specific URL
   * @return
   */
  public List<PersistentEntity> getEntities(){
      int iCnt = 0;
      String url = DEFAULT_APO_URL.concat(getEntitySearchUrl());
      while ( iCnt < this.getMaxPageNumber()){
        LOGGER.debug("Getting: "+ url.replace(ParserProperties.PARAM_NUM, String.valueOf(iCnt)));
        this.parseSearchResults(url.replace(ParserProperties.PARAM_NUM, String.valueOf(iCnt)));
        iCnt+=1;
        //break;
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
      LOGGER.debug("Parsing entity from: " + ParserUtils.getUri(pairs.getKey().toString()).toASCIIString());
      doc = ParserUtils.connectGetUrl(ParserUtils.getUri(pairs.getKey().toString()).toASCIIString());
      if (doc == null)
        break;
      
      doc.setBaseUri(ApontadorParser.DEFAULT_APO_URL);
      // Get address
      String latitude = doc.select("span[class*=latitude]").select("span[class*=value-title]").first().attr("title");
      String longitude = doc.select("span[class*=longitude]").select("span[class*=value-title]").first().attr("title");
      String address = doc.select("p[class*=endereco adr]").first().text();
      LOGGER.debug("Address found:" + address);
      LOGGER.debug("Latitude found:" + latitude);
      LOGGER.debug("Longitude found:" + longitude);
      if (latitude != null && longitude != null
          && !latitude.equals("") && !longitude.equals("")
          && address != null && !address.equals("")){
        // It is always first longitude and then latitude
        PersistentPoint pPoint = pEntity.getPersistentpoint()!=null?pEntity.getPersistentpoint():new PersistentPoint();
        pPoint.setAddress(new Utf8(address));;
        pPoint.addToCoordinates(Double.parseDouble(longitude));
        pPoint.addToCoordinates(Double.parseDouble(latitude));
        pPoint.setAccuracy(EylloLocation.GEOCODER_VERIF_ACC_HIGH);
        pEntity.setPersistentpoint(pPoint);
      }
    }
  }

  /**
   * Parse search results from a search result site
   * @param pUrl
   */
  private void parseSearchResults(String pUrl){
    LOGGER.info("Started parsing: " + pUrl);
    //File input = new File(pUrl);
    Document doc = null;
    
    doc = ParserUtils.connectGetUrl(ParserUtils.getUri(pUrl).toASCIIString());
    doc.setBaseUri(DEFAULT_APO_URL);
    Elements results = doc.select("div[class*=result hreview-aggregate]");
    for (Element result : results){
      PersistentEntity pEnt = new PersistentEntity();
      pEnt.addToScenarioId(getScenarioId());
      // getting classification
      pEnt.setLabel(new Utf8(result.children().select("div[class*=info]").select("p[class*=category]").first().ownText()));
      // getting name and URL
      EylloLink link = ParserUtils.detectUrl(result.children().select("div[class*=info]").select("a").first());
      if ( link != null){
        LOGGER.debug(ApontadorParser.DEFAULT_APO_URL + link.getLinkHref());
        LOGGER.debug(link.getLinkText());
        pEnt.setName(new Utf8(link.getLinkText()));
        pEnt.putToSameAs(new Utf8(ApontadorParser.DEFAULT_APO_URL + link.getLinkHref()), new Utf8(link.getLinkText()));
      }
      // getting the address
      PersistentPoint pPoint = new PersistentPoint();
      pPoint.setAddress(new Utf8(result.children().select("div[class*=more_info]").select("p[class*=adr address]").first().text()));
      // getting telephones
      Elements phones = result.children().select("div[class*=more_info]").select("p[class*=telephone]").select("span[class*=tel]");
      for (Element phone : phones){
        pEnt.addToTelephones(new Utf8(phone.text()));
        //System.out.println(phone.text());
      }
      pEnt.setDescription(new Utf8(""));
      // getting last review
      //Element lastReview = result.children().select("div[class*=last_review]").first();
      
      pEnt.setPersistentpoint(pPoint);
      this.pEntities.add(pEnt);
    }
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    ApontadorParser apontParser = new ApontadorParser();
    ParserUtils.writeJsonFile(apontParser.getEntities(), "/Users/renatomarroquin/Documents/workspace/workspaceEyllo/Eyllo-IR/res/apontador.json");
    //apontParser.getEntities();
  }

  /**
   * Gets the output file name for the Apontador class.
   * @return
   */
  public static String getOutputFileName(){
    return DEFAULT_APO_OUTPUT_FILE_NAME.replace(
        ParserProperties.PARAM_ENT_NAME, getEntityType());
  }

  /**
   * @return the entityType
   */
  public static String getEntityType() {
    return entityType;
  }

  /**
   * @param entityType the entityType to set
   */
  public static void setEntityType(String pEntityType) {
    entityType = pEntityType;
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
    ApontadorParser.scenarioId = scenarioId;
  }

}
