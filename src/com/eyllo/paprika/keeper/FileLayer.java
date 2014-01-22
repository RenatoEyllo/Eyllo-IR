/**
 * 
 */
package com.eyllo.paprika.keeper;

import java.util.List;
import java.util.Map;

/**
 * @author renato
 *
 */
public class FileLayer extends AbstractDataLayer {

  @Override
  public void initializeDataLayer() {
  }

  @Override
  public boolean put(Object key, Object value, String... params) {
    return false;
  }

  @Override
  public Object get(Object key) {
    return null;
  }

  @Override
  public AbstractDataLayer getDataLayer() {
    return null;
  }

  @Override
  public List scan(String pIndexName, String pAttrName, String... pExtra) {
    return null;
  }

  /**
   * Method to do a bulk write of elements within the Map.
   */
  @Override
  public boolean saveElements(Map pElems, String... params) {
    return false;
  }

}
