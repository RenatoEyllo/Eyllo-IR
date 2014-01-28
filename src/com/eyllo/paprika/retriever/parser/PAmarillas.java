/**
 * 
 */
package com.eyllo.paprika.retriever.parser;

import com.eyllo.paprika.retriever.RetrieverConstants;
import com.eyllo.paprika.retriever.parser.elements.PersistentEntity;

/**
 * @author renatomarroquin
 *
 */
public class PAmarillas extends AbstractParser {

  private static String NUM_PARAM_STR = "NUM_PARAM";
  private static String PLACE_PARAM_STR = "PLACE_PARAM";

  /** Default search page. */
  private static String DEFAULT_SEARCH_URL = "http://www.paginasamarillas.com.pe/s/" + PLACE_PARAM_STR + "/"+ NUM_PARAM_STR+"/50";
  private static final String DEFAULT_PA_URL = "http://www.paginasamarillas.com.pe/";

  /** Parser name. */
  private static final String NAME = "pamarillas";

  private static String places[] = {"restaurantes-+carnes+y+parrilladas"};//, "hoteles"};

  /** Default constructor. */
  public PAmarillas() {
    super(10000, 10000, NAME, DEFAULT_SEARCH_URL, true, 10000);
  }

  /** Constructor. */
  public PAmarillas(int pMaxPageNumber, int pMaxNumEntities) {
    super(pMaxPageNumber, pMaxNumEntities, NAME, DEFAULT_SEARCH_URL, true, 10000);
  }

  @Override
  public void parseSearchResults(String url) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void parseIndividualEnt(PersistentEntity pEntity) {
    // TODO Auto-generated method stub
    
  }

}
