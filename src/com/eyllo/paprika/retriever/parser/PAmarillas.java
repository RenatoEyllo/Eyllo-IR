/**
 * 
 */
package com.eyllo.paprika.retriever.parser;

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
  private static final String name = "pamarillas";

  private static String places[] = {"restaurantes-+carnes+y+parrilladas"};//, "hoteles"};

  /** Default constructor. */
  public PAmarillas() {
    super(name, DEFAULT_SEARCH_URL);
  }

  /** Constructor. */
  public PAmarillas(int pMaxPageNumber, int pMaxNumEntities) {
    super(pMaxPageNumber, pMaxNumEntities,name, DEFAULT_SEARCH_URL);
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
