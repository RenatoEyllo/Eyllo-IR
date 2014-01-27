/**
 * 
 */
package com.eyllo.paprika.keeper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eyllo.paprika.retriever.parser.ParserUtils;
import com.eyllo.paprika.retriever.parser.elements.PersistentEntity;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.WriteResult;

/**
 * @author renatomarroquin
 *
 */
public class MongoLayer extends AbstractDataLayer {

  /** Mongo client to connect to a Mongo data store. */
  private MongoClient mongoClient;
  /** A Map with a list of databases. */
  private HashMap<String, DB> dbMap;
  /** Mongo server address. */
  private static String strServer = "mongodb-us-e.cloudapp.net";
  /** Mongo server port. */
  private static int strPort = 27017;
  /** Logger to help us write write info/debug/error messages. */
  private static Logger LOGGER = LoggerFactory.getLogger(MongoLayer.class);
  /** Value used for connection idle/life time. */
  private static final int fifteenMins = 1000*60*15;
  /** Value used for connection idle/life time. */
  private static final int twentyMins = 1000*60*20;

  public MongoLayer(String...pDataLayerParams) {
    initializeDataLayer(pDataLayerParams);
  }

  /** 
   * Initializes Mongo data layer.
   * @see com.eyllo.paprika.keeper.AbstractDataLayer#initializeDataLayer(java.lang.String[])
   */
  @Override
  public void initializeDataLayer(String... pInitParams) {
    try {
      MongoClientOptions ops = MongoClientOptions.builder().autoConnectRetry(true)
      .maxWaitTime(fifteenMins)
      .socketTimeout(0) //never times out
      .connectTimeout(0) //never times out
      .socketKeepAlive(true)
      .maxConnectionIdleTime(fifteenMins)
      .maxConnectionLifeTime(twentyMins)
      .build();
      setMongoClient(new MongoClient(strServer,ops));
      setDbMap(new HashMap<String, DB>());
    } catch (java.net.UnknownHostException e){
      getLogger().error("Error connecting to Mongo Layer.", e.getMessage());
      e.printStackTrace();
    }
  }

  /* (non-Javadoc)
   * @see com.eyllo.paprika.keeper.AbstractDataLayer#put(java.lang.Object, java.lang.Object, java.lang.String[])
   */
  @Override
  public boolean put(Object key, Object value, String... params) {
    // TODO Auto-generated method stub
    return false;
  }

  /* (non-Javadoc)
   * @see com.eyllo.paprika.keeper.AbstractDataLayer#get(java.lang.Object)
   */
  @Override
  public Object get(Object key) {
    // TODO Auto-generated method stub
    return null;
  }

  /* (non-Javadoc)
   * @see com.eyllo.paprika.keeper.AbstractDataLayer#saveElements(java.util.Map, java.lang.String[])
   * params[0] -> 
   * params[1] -> database name
   * params[2] -> collection name
   */
  @Override
  public boolean saveElements(Map pElems, String... params) {
    WriteResult result = null;
    if (params != null && params.length > 2) {
      DBCollection DBCollection = getCollection(params[1], params[2]);
      for (Object pEnt : pElems.values()) {
        JSONObject teste = (JSONObject) ParserUtils.getJsonObj(((PersistentEntity) pEnt).toJson());
        DBObject doc = (DBObject) teste;
        result = DBCollection.save(doc);
      }
    }
    if (result != null)
      return true;
    return false;
  }

  /* (non-Javadoc)
   * @see com.eyllo.paprika.keeper.AbstractDataLayer#getDataLayer()
   */
  @Override
  public AbstractDataLayer getDataLayer() {
    return this;
  }

  /* (non-Javadoc)
   * @see com.eyllo.paprika.keeper.AbstractDataLayer#scan(java.lang.String, java.lang.String, java.lang.String[])
   */
  @Override
  public List scan(String pIndexName, String pAttrName, String... pExtra) {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * return a collection. 
   * Add the database to the dbMap if the first time we try to use it.
   * @param database
   * @param collectionName
   * @return db.getCollection(collectionName) (collection data)
   */
  private DBCollection getCollection(String database, String collectionName) {
      try {
          // get the database
          DB db = getDB(database);        
          // return the collection
          getLogger().info("getCollection", " database:" + database + ", collection name:" + collectionName);
          return db.getCollection(collectionName);
      } catch (Exception e) {
        getLogger().error("getCollection", e.getMessage() + " database:" + database + " collectionName:" + collectionName);
          e.printStackTrace();
      }
      return null;
  }

  /**
   * return a database
   * @param database
   * @return db 
   */
  //TODO: set as private
  public DB getDB(String database){
      try {
          DB db;
          // check if we already have this database in the Map
          if(dbMap.containsKey(database)){
              db = dbMap.get(database);
          } else {
              db = mongoClient.getDB(database);
              // add the database to the Map
              dbMap.put(database, db);            
          }
          getLogger().info("getDB", " database:" + database);
          // return the database
          return db;
      } catch (Exception e) {
        getLogger().error("getDB", e.getMessage() + " database:" + database);
          e.printStackTrace();
      }
      return null;
  }

  /**
   * Gets Mongo server address.
   * @return String representing mongo server address.
   */
  public static String getStrServer() {
      return strServer;
  }

  /**
   * Gets Mongo server port.
   * @return String representing mongo server port.
   */
  public static int getStrPort() {
      return strPort;
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
  /**
   * @return the mongoClient
   */
  public MongoClient getMongoClient() {
    return mongoClient;
  }
  /**
   * @param mongoClient the mongoClient to set
   */
  public void setMongoClient(MongoClient mongoClient) {
    this.mongoClient = mongoClient;
  }
  /**
   * @return the dbMap
   */
  public HashMap<String, DB> getDbMap() {
    return dbMap;
  }
  /**
   * @param dbMap the dbMap to set
   */
  public void setDbMap(HashMap<String, DB> dbMap) {
    this.dbMap = dbMap;
  }

}
