/**
 * 
 */
package com.eyllo.paprika.keeper;

import java.util.List;
import java.util.Map;

/**
 * @author renatomarroquin
 *
 */
public class MongoLayer extends AbstractDataLayer {

  /* (non-Javadoc)
   * @see com.eyllo.paprika.keeper.AbstractDataLayer#initializeDataLayer(java.lang.String[])
   */
  @Override
  public void initializeDataLayer(String... pInitParams) {
    // TODO Auto-generated method stub

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
   */
  @Override
  public boolean saveElements(Map pElems, String... params) {
    // TODO Auto-generated method stub
    return false;
  }

  /* (non-Javadoc)
   * @see com.eyllo.paprika.keeper.AbstractDataLayer#getDataLayer()
   */
  @Override
  public AbstractDataLayer getDataLayer() {
    // TODO Auto-generated method stub
    return null;
  }

  /* (non-Javadoc)
   * @see com.eyllo.paprika.keeper.AbstractDataLayer#scan(java.lang.String, java.lang.String, java.lang.String[])
   */
  @Override
  public List scan(String pIndexName, String pAttrName, String... pExtra) {
    // TODO Auto-generated method stub
    return null;
  }

}
