package com.eyllo.paprika.entity.store;

import java.util.HashMap;
import java.util.Map;

import org.apache.gora.cassandra.store.CassandraStore;
import org.apache.gora.persistency.Persistent;
import org.apache.gora.persistency.impl.PersistentBase;
import org.apache.gora.store.DataStore;
import org.apache.gora.store.DataStoreFactory;
import org.apache.gora.util.GoraException;
import org.apache.hadoop.conf.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataLayer <K, T extends PersistentBase>{

    /**
     * Data store to handle user storage
     */
    protected Map<String, DataStore> dataStores = new HashMap<String, DataStore>();

    /**
     * Logger to help us write write info/debug/error messages
     */
    private static Logger LOGGER = LoggerFactory.getLogger(DataLayer.class);

    /**
     * Class of the data store to be used
     */
    protected Class<? extends DataStore> dataStoreClass;

    /**
     * Configuration to be used when setting data stores up
     */
    private Configuration conf;

    /**
     * Creates a specific data store based on its type paremeter
     * @param pDataStoreName
     * @param pDataStoreType
     * @param keyClass
     * @param persistentClass
     * @return
     * @throws GoraException
     */
    public <K, T extends Persistent> DataStore<K,T> createSpecificDataStore(
            String pDataStoreName, String pDataStoreType, Class<K> keyClass, Class<T> persistentClass) 
                    throws GoraException {
      LOGGER.info("Creating a data store for: " + pDataStoreType);
      // Getting the specific data store
      dataStoreClass = getSpecificDataStore(pDataStoreType);
      DataStore<K,T> dataStore = createDataStore(keyClass, persistentClass);
      // Setting the recently created data store into a centralized structure
      if (dataStore != null)
        dataStores.put(pDataStoreName, dataStore);
      LOGGER.info("Finished creating a data store for: " + pDataStoreType);
      // Returning the data store created
      return dataStore;
   }
    
    /**
     * Creates a specific key from a string
     * @param pKey
     * @return
     */
    public K createKey(String pKey){
        return (K) pKey;
    }

    /**
     * Performs a getRequest
     * @param pDataStoreName
     * @param pKey
     * @return
     */
    public Object getRequests(String pDataStoreName, K pKey){
      LOGGER.debug("Performing get requests for " + pDataStoreName);
      DataStore<K, T> dataStore = getDataStore(pDataStoreName);
      Object obj = dataStore.get(pKey);
      return obj;
    }

    /**
     * Performs a putRequest
     * @param pDataStoreName
     * @param pKey
     * @param pValue
     */
    public void putRequests(String pDataStoreName, K pKey, T pValue){
      LOGGER.debug("Performing put requests for " + pDataStoreName);
      DataStore<K, T> dataStore = getDataStore(pDataStoreName);
      dataStore.put(pKey, pValue);
      dataStore.flush();
    }

    /**
     * Gets the specific data store based on its name
     * @param pDataStoreName
     * @return
     */
    public DataStore<K, T> getDataStore(String pDataStoreName){
      return dataStores.get(pDataStoreName);
    }

    /**
     * Returns the specific type of class for the requested data store
     * @param pDataStoreName
     * @return
     */
    private Class<? extends DataStore> getSpecificDataStore(String pDataStoreName){
      if (pDataStoreName.toLowerCase() == "cassandra")
          return CassandraStore.class;
      //if (pDataStoreName == "DynamoDB")
        //  return DynamoDBStore.class;
      return null;
    }
    
    /**
     * Creates a generic data store using the data store class set using the class property
     * @param keyClass
     * @param persistentClass
     * @return
     * @throws GoraException
     */
    @SuppressWarnings("unchecked")
    public <K, T extends Persistent> DataStore<K,T>
      createDataStore(Class<K> keyClass, Class<T> persistentClass) throws GoraException {
      DataStoreFactory.createProps();
      DataStore<K,T> dataStore = 
          DataStoreFactory.createDataStore((Class<? extends DataStore<K,T>>)dataStoreClass, 
                                            keyClass, persistentClass,
                                            conf);

      return dataStore;
    }
}
