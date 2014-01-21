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
  static private String parserName;

  /** Scenario Id used */
  private int scenarioId;

  /** User Id used */
  private int userId;

  /** Path for the output file */
  private String outPath;

  /** List of entities to be filled up. */
  protected List<PersistentEntity> pEntities;

  /** URL where to start fetching data */
  private String fetchUrl;

  /** Maximum number of pages to be read. */
  private int maxPageNumber;

  /** Maximum number of entities to be collected. */
  private int maxNumEntities;

  /** Default output file name */
  private String outputName = ParserConstants.PARAM_ENT_NAME + ".json";

  /** If the search process will involve local search. */
  private boolean useLocal;

  /** Time (in mili seconds) between external requests. */
  private long req_politeness;

  /** Logger to help us write write info/debug/error messages. */
  private static Logger LOGGER = LoggerFactory.getLogger(AbstractParser.class);;

  /**
   * Constructor.
   * @param pMaxPageNumber to be visited.
   * @param pMaxNumEntities to be gathered.
   * @param pName to identify parser.
   * @param pOutPath to export entities to JSON file.
   * @param pFetchUrl from where to extract entities.
   * @param pLocal Whether or not the search process will include local search.
   * @param pPoliteness  Time to wait between external request.
   */
  public AbstractParser(int pMaxPageNumber, int pMaxNumEntities,
      String pFetchUrl, String pOutPath,
      boolean pLocal, int pPoliteness) {
    initialize(pMaxPageNumber, pMaxNumEntities, pOutPath, pFetchUrl, pLocal, pPoliteness);
  }

  /**
   * Constructor using default outputPath, not using local search, and default server politeness.
   * @param pMaxPageNumber
   * @param pMaxNumEntities
   * @param pName
   * @param pFetchUrl
   */
  public AbstractParser(int pMaxPageNumber, int pMaxNumEntities,
      String pFetchUrl) {
    initialize(pMaxPageNumber, pMaxNumEntities,
        ParserConstants.DEFAULT_OUTPUT_PATH, pFetchUrl,
        ParserConstants.DEFAULT_USE_LOCAL, ParserConstants.DEFAULT_REQ_POLITENESS);
  }

  /**
   * Constructor using default values for maximum number of pages,
   * maximum number of entities, outputPath, server politeness,
   * and not using local search. 
   * @param pName   Parser name.
   * @param pFetchUrl   To get entities from.
   */
  public AbstractParser(String pFetchUrl) {
    initialize(Integer.MAX_VALUE, Integer.MAX_VALUE,
        ParserConstants.DEFAULT_OUTPUT_PATH, pFetchUrl,
        ParserConstants.DEFAULT_USE_LOCAL, ParserConstants.DEFAULT_REQ_POLITENESS);
  }

  /**
   * Initializer
   * @param pMaxPageNumber To be searched.
   * @param pMaxNumEntities To be fetched.
   * @param pName Describes parser name.
   * @param pOutPath Where entities will be written.
   * @param pFetchUrl To go and fetch entities.
   * @param pUseLocal Whether or not the search process will include local search.
   * @param pPoliteness Time to wait between external request.
   */
  public void initialize(int pMaxPageNumber, int pMaxNumEntities,
      String pOutPath, String pFetchUrl,
      boolean pUseLocal, int pPoliteness) {
    pEntities = new ArrayList<PersistentEntity> ();
    maxPageNumber = pMaxPageNumber;
    maxNumEntities = pMaxNumEntities;
    outPath = pOutPath;
    fetchUrl = pFetchUrl;
    setUseLocal(pUseLocal);
    setPoliteness(pPoliteness);
    
  }

  /**
   * Gets entities from an specific URL
   * @return
   */
  public List<PersistentEntity> fetchEntities(){
      int iCnt = 0;
      while ( iCnt < this.getMaxPageNumber()){
        getLogger().debug("Getting: "+ fetchUrl.replace(ParserConstants.PARAM_NUM, String.valueOf(iCnt)));
        waitPolitely();
        this.parseSearchResults(fetchUrl.replace(ParserConstants.PARAM_NUM, String.valueOf(iCnt)));
        iCnt+=1;
      }
      getLogger().info("Hubo # entidades : " + this.totalEntities());
    this.completeEntityInfo();
    return this.pEntities;
  }

  /**
   * Gets entities kept in memory after process.
   * @return
   */
  public List<PersistentEntity> getEntities(){
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
  public String getOutputFileName() {
    return this.outPath + this.outputName.replace(
        ParserConstants.PARAM_ENT_NAME, getParserName());
  }

  /**
   * Waits politely for n mili seconds.
   */
  public void waitPolitely() {
    try {
      wait(this.getPoliteness());
    } catch (InterruptedException e) {
      getLogger().error("Error while waiting to perform a new server request.");
      e.printStackTrace();
    }
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
  public static String getParserName() {
    return parserName;
  }

  /**
   * Sets the parser name
   * @param parserName Parser name to be used.
   */
  public static void setName(String pName) {
    parserName = pName;
  }

  /**
   * @return the url
   */
  public String getFetchUrl() {
    return fetchUrl;
  }

  /**
   * @param url the url to set
   */
  public void setFetchUrl(String url) {
    this.fetchUrl = url;
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
  public String getOutPath() {
    return outputName;
  }

  /**
   * @return the path
   */
  public String setOutPath(String pOutputName) {
    return pOutputName;
  }

  /**
   * @return the maxNumEntities
   */
  public int getMaxNumEntities() {
    return maxNumEntities;
  }

  /**
   * @param maxNumEntities the maxNumEntities to set
   */
  public void setMaxNumEntities(int maxNumEntities) {
    this.maxNumEntities = maxNumEntities;
  }

  /**
   * @return the useLocal
   */
  public boolean useLocal() {
    return useLocal;
  }

  /**
   * @param useLocal the useLocal to set
   */
  public void setUseLocal(boolean useLocal) {
    this.useLocal = useLocal;
  }

  /**
   * @return the politeness
   */
  public long getPoliteness() {
    return req_politeness;
  }

  /**
   * @param politeness the politeness to set
   */
  public void setPoliteness(int politeness) {
    this.req_politeness = politeness;
  }
}
