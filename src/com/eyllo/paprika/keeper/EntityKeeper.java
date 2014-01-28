/**
 * 
 */
package com.eyllo.paprika.keeper;

import java.util.List;
import java.util.Map;

import org.apache.gora.util.GoraException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eyllo.paprika.retriever.parser.elements.PersistentEntity;

/**
 * @author renatomarroquin
 *
 */
//TODO entityKeeper should be parametrized.
public class EntityKeeper {

  @SuppressWarnings("rawtypes")
  private AbstractDataLayer dataLayer;

  /**
   * Entities List
   */
  private List<PersistentEntity> entities;

  public EntityKeeper (String...pDataLayerType) {
    // call dataLayer factory to initialize
    dataLayer = AbstractDataLayer.dataLayerFactory(pDataLayerType);
  }

  public boolean save(Map pObjs, String pSchemaName, String pTypeName) {
    boolean flag = true;
    for (Object key : pObjs.keySet()) {
      if (!dataLayer.put(key, pObjs.get(key), pSchemaName, pTypeName)) {
        LOGGER.error("Error saving data. " + pObjs.get(key));
        flag = false;
      }
    }
    return flag;
  }

  @SuppressWarnings("rawtypes")
  public List retrieve(String pSchemaName, String pTypeName) {
    return dataLayer.scan(pSchemaName, pTypeName);
  }

  /**
   * Logger to help us write write info/debug/error messages
   */
  private static Logger LOGGER = LoggerFactory.getLogger(EntityKeeper.class);

  /**
   * @param args
  public static void main(String[] args) {
    EntityRetriever entRetriever = new EntityRetriever();
    String dataStoreType = "cassandra";
    String entitiesSource = RioGuiaParser.NAME;
    EntityKeeper entKeeper = new EntityKeeper(dataStoreType);
    if (args.length > 2){
        dataStoreType = args[0];
        entitiesSource = args[1];
    }
    // Getting the entities 
    //TODO Update this call
    //entKeeper.entities = entRetriever.getEntities(entitiesSource);
    // Updates entities' geolocation 
    entKeeper.entities = EntityRetriever.updateGeoInfo(entKeeper.entities);
    // Verifies entities' geolocation with geocoders available 
    entKeeper.entities = EntityRetriever.verifyGeoInfo(entKeeper.entities);
    //ParseUtils.printPersistentEntities(entKeeper.entities);
    //entKeeper.entities = EntityRetriever.getGeoValidatedEntities(entKeeper.entities);
    // Saves entities within its specific store 
    saveEntities(dataStoreType, entKeeper.entities, "entityStore");
    // Verifies all entities stored 
    verifySavedEntities(dataStoreType, entKeeper.entities, "entityStore");
  }
*/
  /**
   * Saves entities within a specific data store
   * @param pDataStoreType
   * @param pEntities
   * @param pStoreName    Entity's store name for our data store
   * @return
   */
  public static boolean saveEntities(String pDataStoreType,
        List<PersistentEntity> pEntities,
        String pStoreName){
      boolean flgSuccess = true;
      DataLayer<String, PersistentEntity> nKeepr = new DataLayer<String, PersistentEntity>();
      try {
        /** Creating data stores */
        nKeepr.createSpecificDataStore(pStoreName, pDataStoreType, String.class, PersistentEntity.class);
        /** Performing requests PUT requests */
        for (PersistentEntity pEnt : pEntities)
          nKeepr.putRequests(pStoreName, nKeepr.createKey(pEnt.getName().toString()), pEnt);
        //nKeepr.putRequests(ENT_STORE_NAME, nKeepr.createKey("http://www.00cafebistro.com.br"), createEntity());
        //verifyEntity(nKeepr.getRequests(ENT_STORE_NAME, nKeepr.createKey("http://www.00cafebistro.com.br")));
      } catch (GoraException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      return flgSuccess;
    }

    /**
     * 
     * @param pDataStoreType
     * @param pEntities
     * @param pStoreName    Entity's store name for our data store
     */
  public static void verifySavedEntities(String pDataStoreType,
      List<PersistentEntity> pEntities,
      String pStoreName){
    boolean flgVerif = true;
    DataLayer<String, PersistentEntity> nKeepr = new DataLayer<String, PersistentEntity>();
    Object obj = nKeepr.getRequests(pStoreName, pDataStoreType, null, null);
    if ( flgVerif )
      LOGGER.info("All entities were persisted and retrieved successfully");
    else
      LOGGER.info("All entities were NOT persisted and retrieved successfully");
  }

  //TODO this method should be the same as the other saveEntities
    public void saveEntities(Map<Object, PersistentEntity> fetchEntities) {
      // TODO Auto-generated method stub
      dataLayer.saveElements(fetchEntities);
    }

    public void deleteAll(String pSchemaName, String pColName, String...pExtraParams) {
      // TODO Auto-generated method stub
      dataLayer.deleteAll(pSchemaName, pColName, pExtraParams);
    }

}
