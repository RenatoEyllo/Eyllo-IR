package com.eyllo.paprika.retriever.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.avro.util.Utf8;
import org.elasticsearch.search.SearchHit;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.jsoup.nodes.Document;

import com.eyllo.paprika.keeper.EntityKeeper;
import com.eyllo.paprika.retriever.parser.elements.EylloLocation;
import com.eyllo.paprika.retriever.parser.elements.PersistentEntity;
import com.eyllo.paprika.retriever.parser.elements.PersistentPoint;

/**
 * Class to extract public transportation data from SP city.
 * @author renatomarroquin
 */
public class SPTransParser extends AbstractParser {

  public static String NAME = "spTrans";
  private static final String HOME_PAGE = "http://www.sptrans.com.br/";

  /** Default API URL. */
  private static final String OLHOVIVO_DEFAULT_API_URL = "http://api.olhovivo.sptrans.com.br/v0";
  /** API Token. */
  private static final String OLHOVIVO_DEFAULT_API_TOKEN = "51d279ac1e7cc3ceac47590e9f0acbbafa79bb9ee7fb8e164b77837eede65aaf";
  /** API Authenticate URL. */
  private static final String OLHOVIVO_API_AUTH = "/Login/Autenticar?token=";
  /** API Search URL. */
  private static final String OLHOVIVO_SEARCH_TERM = "TERM";
  /** API URL for getting specific search terms. */
  private static final String OLHOVIVO_API_SEARCH = "/Linha/Buscar?termosBusca=" + OLHOVIVO_SEARCH_TERM;
  /** API URL for getting stop signs of a bus line. */
  private static final String OLHOVIVO_API_STOPS_SEARCH = "/Parada/BuscarParadasPorLinha?codigoLinha=";
  /** Cookie name for authentication. */
  private static final String OLHOVIVO_COOKIE_NAME = "apiCredentials";

  /** Authentication cookie used inside the API. */
  private String authCookie;

  /** Schema to save temporary data into indexing layer */
  private final String pLinesSchemaName = "main_linhas";
  private final String pLinesTypeName = "linha";
  private final String pStopsSchemaName = "linhas_paradas";
  private final String pStopsTypeName = "paradas";
  private final int SCENARIO_ID = 27;
  private boolean useLocal = false;

  private EntityKeeper eKeeper = new EntityKeeper ("index");

  /**
   * Constructor.
   * @param pMaxPageNumber to be visited.
   * @param pMaxNumEntities to be gathered.
   * @param pName to identify parser.
   * @param pOutPath to export entities to JSON file.
   * @param pFetchUrl from where to extract entities.
   * @param pLocal Whether or not the search process will include local search.
   * @param pPoliteness  Time to wait between external request.
   */
  public SPTransParser(int pMaxPageNumber, int pMaxNumEntities,
      String pFetchUrl, String pOutPath,
      boolean pLocal, int pPoliteness) {
    if (pFetchUrl.isEmpty())
      pFetchUrl = OLHOVIVO_DEFAULT_API_URL;
    if (pOutPath.isEmpty())
      pOutPath = ParserConstants.DEFAULT_OUTPUT_PATH;
    if (pPoliteness == 0)
      pPoliteness = ParserConstants.DEFAULT_REQ_POLITENESS;
    this.setParserName(NAME);
    initialize(pMaxPageNumber, pMaxNumEntities, pOutPath, pFetchUrl, pLocal, pPoliteness);
    getLogger().info("Running parser " + getParserName() + " using MAX values for retrieving.");
    this.setAuthCookie(getCookieSpAuthenticate());
  }

  /**
   * Constructor.
   * @param pMaxPageNumber maximum number of pages to be visited.
   * @param pMaxNumEntities maximum number of entities to be obtained.
   */
  public SPTransParser(int pMaxPageNumber, int pMaxNumEntities, boolean pLocal) {
    super(pMaxPageNumber, pMaxNumEntities,
        ParserConstants.DEFAULT_OUTPUT_PATH, OLHOVIVO_DEFAULT_API_URL,
        pLocal, ParserConstants.DEFAULT_REQ_POLITENESS);
    this.setAuthCookie(getCookieSpAuthenticate());
    
  }

  /**
   * Default constructor.
   */
  public SPTransParser() {
    super(Integer.MAX_VALUE, Integer.MAX_VALUE, OLHOVIVO_DEFAULT_API_URL);
    getLogger().info("Running parser " + getParserName() + " using MAX values for retrieving.");
    this.setAuthCookie(getCookieSpAuthenticate());
  }

  /**
   * Gets entities from an specific URL
   * @return
   */
  @Override
  public List<PersistentEntity> fetchEntities(){
      int iCnt = 0;
      if (!this.getAuthCookie().equals("")) {
        // here we will take each bus line as being a pageNumber
        String []searchCombs = getSearchCombinations();
        while ( iCnt < this.getMaxPageNumber() & iCnt < searchCombs.length){
          // get all lines and insert them into a different collection paprika.sptransLinhas
          String url = (OLHOVIVO_DEFAULT_API_URL + OLHOVIVO_API_SEARCH).replace(OLHOVIVO_SEARCH_TERM, searchCombs[iCnt]);
          this.waitPolitely();
          parseSearchResults(url);
          // get stops based on each line and create their geoTags to insert them
          //getLogger().debug("Getting: "+ fetchUrl.replace(ParserConstants.PARAM_NUM, String.valueOf(iCnt)));
          //this.parseSearchResults(fetchUrl.replace(ParserConstants.PARAM_NUM, String.valueOf(iCnt)));
          iCnt += 1;
        }
        getLogger().info("Hubo # entidades : " + this.totalEntities());
        this.completeEntityInfo();
      }
    return this.pEntities;
  }

  @Override
  public void parseSearchResults(String url) {
    Map<String, String> cookies = new HashMap<String, String> ();
    cookies.put(OLHOVIVO_COOKIE_NAME, this.getAuthCookie());
    Document doc = ParserUtils.connectCookiePostUrl(url, cookies);
    if (doc != null) {
      JSONArray jArray = (JSONArray)ParserUtils.getJsonObj(doc.text());
      System.out.println(url + ": " + jArray.size());
      Map<String, JSONObject> pObjs = new HashMap<String, JSONObject>();
      for (Object jObj : jArray) {
        JSONObject tmpObj = (JSONObject)jObj;
        pObjs.put(tmpObj.get("CodigoLinha").toString(), tmpObj);
        System.out.println(jObj);
      }
      if (!pObjs.isEmpty())
        eKeeper.save(pObjs, pLinesSchemaName, pLinesTypeName);
    }
  }

  /**
   * Completes the entity information.
   */
  @Override
  public void completeEntityInfo() {
    parseIndividualEnt(null);
  }

  private PersistentEntity jsonToPersistentEntity(JSONObject pJSONObject) {
    PersistentEntity pEntity = new PersistentEntity();
    pEntity.addToScenarioId(SCENARIO_ID);
    pEntity.setHomepage(new Utf8(HOME_PAGE));
    pEntity.setName(new Utf8(pJSONObject.get("Nome").toString()));
    pEntity.setDescription(new Utf8("CodigoLinha:" + pJSONObject.get("CodigoLinha")));
    PersistentPoint pPoint = new PersistentPoint();
    pPoint.addToCoordinates(Double.parseDouble(pJSONObject.get("Longitude").toString()));
    pPoint.addToCoordinates(Double.parseDouble(pJSONObject.get("Latitude").toString()));
    pPoint.setAddress(new Utf8(pJSONObject.get("Endereco").toString()));
    pPoint.setAccuracy(EylloLocation.GEOCODER_VERIF_ACC_HIGH);
    pEntity.setPersistentpoint(pPoint);
    return pEntity;
  }

  @Override
  public void parseIndividualEnt(PersistentEntity pEntity) {
    if (!isGetLocal()) {
      // 1. Get all data from the keeper
      //TODO this should come according the isGetLocal parameter as well
      List busLines = eKeeper.retrieve(pLinesSchemaName, pLinesTypeName);
     // 2. For each JSON object from the keeper, perform a request.
      for(Object busLine : busLines) {
        String lineCode = ((SearchHit)busLine).getSource().get("CodigoLinha").toString();
        String url = OLHOVIVO_DEFAULT_API_URL + OLHOVIVO_API_STOPS_SEARCH + lineCode;
        Map<String, String> cookies = new HashMap<String, String> ();
        cookies.put(OLHOVIVO_COOKIE_NAME, this.getAuthCookie());
        Document doc = ParserUtils.connectCookiePostUrl(url, cookies);
        if (doc != null) {
          JSONArray jArray = (JSONArray)ParserUtils.getJsonObj(doc.text());
          System.out.println(url + ": " + jArray.size());
          Map<String, JSONObject> pObjs = new HashMap<String, JSONObject>();
          for (Object jObj : jArray) {
            // 3. Give new data to keeper
            JSONObject tmpObj = (JSONObject)jObj;
            tmpObj.put("CodigoLinha", lineCode);
            pObjs.put(tmpObj.get("CodigoParada").toString(), tmpObj);
            eKeeper.save(pObjs, pStopsSchemaName, pStopsTypeName);
            // 4. Create PersistentEntity objects to export
            this.pEntities.add(jsonToPersistentEntity(tmpObj));
          }
        }
      }
    }
    else {
      getLogger().info("Setting entities from saved information.");
      List lineStops = eKeeper.retrieve(pStopsSchemaName, pStopsTypeName);
      getLogger().info("Deleting in-memory stored entities.");
      this.pEntities = new ArrayList<PersistentEntity>();
      for(Object lineStop : lineStops) {
        JSONObject tmpObj = ParserUtils.getJsonObj(((SearchHit)lineStop).sourceAsMap());
        System.out.println(tmpObj);
        this.pEntities.add(jsonToPersistentEntity(tmpObj));
      }
    }
    for (PersistentEntity pEnt : this.pEntities)
      System.out.println(pEnt.toJson());
  }

  private String getCookieSpAuthenticate() {
    String authUrl = OLHOVIVO_DEFAULT_API_URL + OLHOVIVO_API_AUTH + OLHOVIVO_DEFAULT_API_TOKEN;
    String cookieVal = ParserUtils.getCookie(authUrl, OLHOVIVO_COOKIE_NAME);
    return cookieVal;
  }

  /**
   * Gets search combinations to try getting all bus lines available.
   * @return searchCombinations.
   */
  private String[] getSearchCombinations() {
    String [] vowels = {"a", "e", "i", "o", "u"};
    String [] consonants = {"s", "l", "p", "t", "c", "b", "d", "f"};
    String [] sCombinations = new String[vowels.length * consonants.length + vowels.length + consonants.length];
    int cont = 0, cont2 = 0, contComb = vowels.length + consonants.length;
    System.arraycopy( vowels, 0, sCombinations, 0, vowels.length );
    System.arraycopy( consonants, 0, sCombinations, vowels.length, consonants.length );
    while (cont < vowels.length) {
      cont2 = 0;
      while (cont2 < consonants.length) {
        sCombinations[contComb] = vowels[cont] + consonants[cont2];
        contComb += 1;
        cont2 += 1;
      }
      cont +=1;
    }
    return sCombinations;
  }

  /**
   * @return the authCookie
   */
  public String getAuthCookie() {
    return authCookie;
  }

  /**
   * @param authCookie the authCookie to set
   */
  public void setAuthCookie(String authCookie) {
    this.authCookie = authCookie;
  }

  /**
   * @return the getLocal
   */
  public boolean isGetLocal() {
    return useLocal;
  }

  /**
   * @param getLocal the getLocal to set
   */
  public void setGetLocal(boolean getLocal) {
    this.useLocal = getLocal;
  }
}
