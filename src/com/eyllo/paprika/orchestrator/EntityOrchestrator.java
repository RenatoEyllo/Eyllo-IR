package com.eyllo.paprika.orchestrator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eyllo.paprika.retriever.EntityRetriever;
import com.eyllo.paprika.retriever.RetrieverUtils;

public class EntityOrchestrator {

  /** Logger to help us write write info/debug/error messages. */
  private static Logger LOGGER = LoggerFactory.getLogger(EntityOrchestrator.class);
  //TODO
  //1. Read configuration file
  //2. Start different processes. These processes could be done concurrently if needed.
  //2.1. Retriever
  //    A. Retriever
  //    B. Geo-validator
  //3. Matcher/Repaired
  //4. Enhancer
  /**
   * @param args
   */
  public static void main(String[] args) {
    // 1. Retrieve entities
    //AbstractParser absParser = null; //new SPTransParser(100, 100, true);
    EntityRetriever entRet = new EntityRetriever();

    try {
      entRet.startRetriever(RetrieverUtils.getPropertiesFile("/home/renato/workspace/Eyllo-IR/conf/retriever.properties"));
    } catch (InterruptedException e) {
      getLogger().error("Eror while running Retriever using properties.");
      e.printStackTrace();
    }

    //entRet.entities = new ApontadorParser("hotels", 50, 20).getEntities();
    //absParser.fetchEntities();
    //absParser.completeEntityInfo();
    //ParserUtils.writeJsonFile(absParser.getEntities(), absParser.getOutputFileName());
      //entRet.entities = absParser.getEntities();

      // 2. Store entities
      // 3. Complete entities information
      //entRet.entities = updateGeoInfo(entRet.entities);
      //entRet.entities = verifyGeoInfo(entRet.entities);
      //TODO update specific parsers to return their own file name
      //ParserUtils.writeJsonFile(entRet.entities,
      //    DEFAULT_JSON_OUTPUT + absParser.getOutputFileName());
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
  public static void setLogger(Logger lOGGER) {
    LOGGER = lOGGER;
  }
}
