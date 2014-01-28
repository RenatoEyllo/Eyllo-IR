/**
 * 
 */
package com.eyllo.paprika.keeper;

import java.util.List;
import java.util.Map;

import com.eyllo.paprika.retriever.parser.ParserUtils;
import com.eyllo.paprika.retriever.parser.elements.PersistentEntity;

/**
 * @author renato
 *
 */
public class FileLayer extends AbstractDataLayer {

  /**
   * File path where data is going to be read and written.
   */
  private String filePath;

  public FileLayer(String...pDataLayerParams) {
    initializeDataLayer(pDataLayerParams);
  }

  @Override
  public void initializeDataLayer(String...pInitParams) {
    if (pInitParams != null && pInitParams.length > 1)
      this.setFilePath(pInitParams[1]);
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
    ParserUtils.writeJsonFile((List<PersistentEntity>) pElems.values(), getFilePath());
    return true;
  }

  public String getFilePath() {
    return filePath;
  }
  public void setFilePath(String pFilePath) {
    filePath = pFilePath;
  }

  @Override
  public void deleteAll(String pSchemaName, String pColName,
      String... pExtraParams) {
    // TODO Auto-generated method stub
    
  }
}
