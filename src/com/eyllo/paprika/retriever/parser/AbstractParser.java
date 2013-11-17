/**
 * 
 */
package com.eyllo.paprika.retriever.parser;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eyllo.paprika.retriever.parser.elements.PersistentEntity;

/**
 * @author renatomarroquin
 *
 */
public abstract class AbstractParser {

  /**  Parser name. */
  private String parserName;

  /** Scenario Id used */
  private int scenarioId;

  /** User Id used */
  private int userId;

  /** Path for the output file */
  private String path;

  /** List of entities to be filled up. */
  protected List<PersistentEntity> pEntities;

  /** URL where to start fetching data */
  private String url;

  /** Maximum number of pages to be read. */
  private int maxPageNumber;

  /** Default output file name */
  private String outputName = ConstantsParser.PARAM_ENT_NAME + ".json";

  /** Logger to help us write write info/debug/error messages */
  private static Logger LOGGER;

  /**
   * Default constructor.
   */
  public AbstractParser() {
    pEntities = new ArrayList<PersistentEntity> ();
  }

  /**
   * Gets entities from an specific URL
   * @return
   */
  public List<PersistentEntity> getEntities(String url){
      int iCnt = 0;
      while ( iCnt < this.getMaxPageNumber()){
        getLogger().debug("Getting: "+ url.replace(ConstantsParser.PARAM_NUM, String.valueOf(iCnt)));
        this.parseSearchResults(url.replace(ConstantsParser.PARAM_NUM, String.valueOf(iCnt)));
        iCnt+=1;
      }
      getLogger().info("Hubo # entidades : " + this.totalEntities());
    this.completeEntityInfo();
    return this.pEntities;
  }

  /** 
   * Parses specific search results one by one.
   * @param url where individual entities will be found
   */
  public abstract void parseSearchResults(String url);

  /**
   * Completes the entity information.
   */
  public void completeEntityInfo() {
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
  public abstract void parseIndividualEnt(PersistentEntity pEntity);

  /**
   * Gets the output file name for the Apontador class.
   * @return
   */
  public String getOutputFileName(){
    return this.path + this.outputName.replace(
        ConstantsParser.PARAM_ENT_NAME, getParserName());
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
   * Returns the number of entities retrieved
   * @return
   */
  public int totalEntities(){
    return this.pEntities.size();
  }

  /**
   * @return the scenarioId
   */
  public int getScenarioId() {
    return scenarioId;
  }

  /**
   * @param scenarioId the scenarioId to set
   */
  public void setScenarioId(int scenarioId) {
    this.scenarioId = scenarioId;
  }

  /**
   * Gets the parser name.
   * @return name of the parser.
   */
  public String getParserName() {
    return this.parserName;
  }

  /**
   * Sets the parser name
   * @param parserName Parser name to be used.
   */
  public void setName(String parserName) {
    this.parserName = parserName;
  }

  /**
   * @return the url
   */
  public String getUrl() {
    return url;
  }

  /**
   * @param url the url to set
   */
  public void setUrl(String url) {
    this.url = url;
  }

  /**
   * @return the lOGGER
   */
  public static Logger getLogger() {
    return LOGGER;
  }

  /**
   * @param lOGGER the lOGGER to set
   */
  public static void setLogger(Class<?> parserClass) {
    LOGGER = LoggerFactory.getLogger(parserClass);
  }

  /**
   * @return the userId
   */
  public int getUserId() {
    return userId;
  }

  /**
   * @param userId the userId to set
   */
  public void setUserId(int userId) {
    this.userId = userId;
  }

  /**
   * @return the path
   */
  public String getPath() {
    return path;
  }

  /**
   * @param path the path to set
   */
  public void setPath(String path) {
    this.path = path;
  }
}
