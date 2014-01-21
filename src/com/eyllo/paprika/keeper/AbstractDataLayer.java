package com.eyllo.paprika.keeper;

import java.util.List;

/**
 * Class used to represent different data layers that
 * could be used to persist data.
 * @author renatomarroquin
 *
 */
public abstract class AbstractDataLayer<K, V> {

  /**
   * Default Constructor.
   */
  public AbstractDataLayer() {
    initializeDataLayer();
  }

  @SuppressWarnings("rawtypes")
  public static AbstractDataLayer dataLayerFactory(String pDataLayerType) {
    AbstractDataLayer constructed = null;
    if (pDataLayerType.equals("index"))
      constructed = new IndexLayer();
   return constructed;
  }

  /**
   * Initializes a data layer.
   */
  public abstract void initializeDataLayer();

  /**
   * Puts a value identified by a key.
   * @param key     to identify value to be persisted.
   * @param value   to be persisted.
   * @param params  that could be needed to perform operation.
   * @return boolean depending on the operation result.
   */
  public abstract boolean put(K key, V value, String ... params);

  /**
   * Gets a value identified by a key.
   * @param key     to identify value to be obtained.
   * @return V      value to be obtained.
   */
  public abstract V get(K key);

  /**
   * Gets an instance of the DataLayer being used.
   * @return
   */
  public abstract AbstractDataLayer<K, V> getDataLayer();

  /**
   * Gets a set of values from a range of objects.
   * @param pIndexName  Schema/Database name.
   * @param pAttrName   Sub-schema/Table name.
   * @param pExtra      Start/End key for the range query.
   * @return
   */
  public abstract List<V> scan(String pIndexName, String pAttrName, String...pExtra);

}
