package com.eyllo.paprika.retriever.parser;

import static com.eyllo.paprika.retriever.parser.ParserConstants.DEFAULT_STRING_MAX_LENGTH;
import static com.eyllo.paprika.retriever.parser.ParserConstants.DEFAULT_STRING_FINAL_CHARS;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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

  /** SP Transport Parser. */
  public static String NAME = "sptrans";
  /** Site home page. */
  private static final String HOME_PAGE = "http://www.sptrans.com.br/";
  /** Default API URL. */
  private static final String OV_DEFAULT_API_URL = "http://api.olhovivo.sptrans.com.br/v0";
  /** API Token. */
  private static final String OV_DEFAULT_API_TOKEN = "51d279ac1e7cc3ceac47590e9f0acbbafa79bb9ee7fb8e164b77837eede65aaf";
  /** API Authenticate URL. */
  private static final String OV_API_AUTH = "/Login/Autenticar?token=";
  /** API Search URL. */
  private static final String OV_SEARCH_TERM = "TERM";
  /** API URL for getting specific search terms. */
  private static final String OV_API_SEARCH = "/Linha/Buscar?termosBusca=" + OV_SEARCH_TERM;
  /** API URL for getting stop signs of a bus line. */
  private static final String OV_API_STOPS_SEARCH = "/Parada/BuscarParadasPorLinha?codigoLinha=";
  /** API URL for getting stops forecast depending on line numbers. */
  private static final String OV_API_STOPS_FORECAST_LINE_SEARCH = "/Previsao/Linha?codigoLinha=";
  /** API URL for getting stops forecast depending on stops' numbers. */
  private static final String OV_API_STOPS_FORECAST_SEARCH = "/Previsao/Parada?codigoParada=";
  /** Cookie name for authentication. */
  private static final String OV_COOKIE_NAME = "apiCredentials";
  //TODO this parameter should be passed, not HARDCODED!!!!
  /** Stops file Path. */
  private static String stopFilePath = "/home/renato/workspace/Eyllo-IR/res/stops.txt";

  /** Authentication cookie used inside the API. */
  private String authCookie;

  /** Schema to save temporary data into indexing layer */
  private final String pLinesSchemaName = "main_linhas";
  private final String pLinesTypeName = "linha";
  private final String pStopsSchemaName = "linhas_paradas";
  private final String pStopsTypeName = "paradas";
  private final String pStopsForecastSchemaName = "previsao_linhas_paradas";
  private final String pStopsForecastTypeName = "previsao_paradas";
  private final int SCENARIO_ID = 27;

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
      pFetchUrl = OV_DEFAULT_API_URL;
    if (pOutPath.isEmpty())
      pOutPath = ParserConstants.DEFAULT_OUTPUT_PATH;
    if (pPoliteness == 0)
      pPoliteness = ParserConstants.DEFAULT_REQ_POLITENESS;
    this.setParserName(NAME);
    initialize(pMaxPageNumber, pMaxNumEntities, pOutPath, pFetchUrl, pLocal, pPoliteness);
    getLogger().info("Running parser *" + getParserName() + "* using MAX values for retrieving.");
    this.setAuthCookie(getCookieSpAuthenticate());
  }

  /**
   * Constructor.
   * @param pMaxPageNumber maximum number of pages to be visited.
   * @param pMaxNumEntities maximum number of entities to be obtained.
   */
  public SPTransParser(int pMaxPageNumber, int pMaxNumEntities, boolean pLocal) {
    super(pMaxPageNumber, pMaxNumEntities,
        ParserConstants.DEFAULT_OUTPUT_PATH, OV_DEFAULT_API_URL,
        pLocal, ParserConstants.DEFAULT_REQ_POLITENESS);
    this.setAuthCookie(getCookieSpAuthenticate());
    
  }

  /**
   * Default constructor.
   */
  public SPTransParser() {
    super(Integer.MAX_VALUE, Integer.MAX_VALUE, OV_DEFAULT_API_URL);
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
          String url = (OV_DEFAULT_API_URL + OV_API_SEARCH).replace(OV_SEARCH_TERM, searchCombs[iCnt]);
          parseSearchResults(url);
          // get stops based on each line and create their geoTags to insert them
          //getLogger().debug("Getting: "+ fetchUrl.replace(ParserConstants.PARAM_NUM, String.valueOf(iCnt)));
          //this.parseSearchResults(fetchUrl.replace(ParserConstants.PARAM_NUM, String.valueOf(iCnt)));
          iCnt += 1;
          this.waitPolitely();
        }
        getLogger().info("Hubo # entidades : " + this.totalEntities());
        this.completeEntityInfo();
      }
    return this.pEntities;
  }

  @Override
  public void parseSearchResults(String url) {
    Map<String, String> cookies = new HashMap<String, String> ();
    cookies.put(OV_COOKIE_NAME, this.getAuthCookie());
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
      // This needs to be done within local storage as a different request is needed
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

  /**
   * Parses individual entities.
   */
  @Override
  public void parseIndividualEnt(PersistentEntity pEntity) {
    //parseStopsFromLines ();
    parseStopsForecastFromStops ();
  }

  @SuppressWarnings({ "rawtypes", "unused" })
  public void parseStopsForecastFromStops() {
    if (useLocal()) {
      Date updTime = null, frcTime = null;
      SimpleDateFormat inFormat = new SimpleDateFormat("HH:mm");
      // 1. Get all data from the keeper
      List busStops = eKeeper.retrieve(pStopsSchemaName, pStopsTypeName);
      if (busStops == null) {
        loadStopsFromGtfs();
        busStops = eKeeper.retrieve(pStopsSchemaName, pStopsTypeName);
      }
     // 2. For each JSON object from the keeper, perform a request.
      for(Object busLine : busStops) {
        // Get bus stop information
        String stopCode = ((SearchHit)busLine).getSource().get("stop_id").toString();
        String stopName = ((SearchHit)busLine).getSource().get("stop_name").toString();
        String stopAddress = stopName + ((SearchHit)busLine).getSource().get("stop_desc").toString();
        if (stopAddress.length() > DEFAULT_STRING_MAX_LENGTH)
          stopAddress = stopAddress.substring(0, DEFAULT_STRING_MAX_LENGTH-3) + DEFAULT_STRING_FINAL_CHARS;
        String stopLng = ((SearchHit)busLine).getSource().get("stop_lon").toString();
        String stopLat = ((SearchHit)busLine).getSource().get("stop_lat").toString();
        // Perform request
        String url = OV_DEFAULT_API_URL + OV_API_STOPS_FORECAST_SEARCH + stopCode ;
        Map<String, String> cookies = new HashMap<String, String> ();
        cookies.put(OV_COOKIE_NAME, this.getAuthCookie());
        Document doc = ParserUtils.connectCookiePostUrl(url, cookies);
        System.out.println(doc.text());
        /*TODO These elements should be parse correctly. **/
        if (doc != null && !doc.text().equals("")) {
          JSONObject completeJsonObj = (JSONObject)ParserUtils.getJsonObj(doc.text());
          if (!completeJsonObj.get("p").toString().contains("null") &&
              !completeJsonObj.get("p").toString().isEmpty()) {

            JSONArray linesArray = (JSONArray) ((JSONObject)completeJsonObj.get("p")).get("l");
            System.out.println(url + ": " + linesArray.size());
            StringBuilder stopDesc = new StringBuilder();

            if (linesArray != null && linesArray.size() > 0) {
              Map<String, JSONObject> pObjs = new HashMap<String, JSONObject>();
              for (Object busLineGotten : linesArray) {
                JSONObject busLineJson = (JSONObject) busLineGotten;
                // Bus line code
                System.out.println(busLineJson.get("c"));
                // Bus sign
                System.out.println(busLineJson.get("lt0") + " - " + busLineJson.get("lt1"));
                // Bus arrival time
                JSONArray busesArrivals = (JSONArray) busLineJson.get("vs");
                // if there are any bus arrivals, then get the first one as it is the only one we care
                if (busesArrivals.size() >0) {
                  JSONObject busArrival = (JSONObject)busesArrivals.get(0);
                  System.out.println(busArrival.get("t"));
                  try {
                    updTime = inFormat.parse(completeJsonObj.get("hr").toString());
                    System.out.println(updTime.toString());
                    frcTime = inFormat.parse(busArrival.get("t").toString());
                  } catch (ParseException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                  }
                }
                String difference = String.valueOf((frcTime.getTime() - updTime.getTime())/60000);
                System.out.println("Time to arrival: " + difference + " minutes.");
                stopDesc.append(busLineJson.get("c")).append(ParserConstants.INFO_SEP);
                stopDesc.append(busLineJson.get("lt0")).append(ParserConstants.DESC_SEP).append(busLineJson.get("lt1"));
                stopDesc.append(ParserConstants.INFO_SEP);
                stopDesc.append(difference);
                //pObjs.put(tmpObj.get("CodigoParada").toString(), tmpObj);
                //eKeeper.save(pObjs, pStopsForecastSchemaName, pStopsForecastTypeName);
              }//END-FOR_LINES_ARRAY
              // 4. Create PersistentEntity objects to export
              PersistentEntity pEnt = olhoVivoForecastToPE(stopName, stopDesc.toString(), stopLng, stopLat, stopAddress);
              this.pEntities.add(pEnt);
              System.out.println(pEnt.toJson());
            }//END-IF_LINES_ARRAY
          }//END-IF_STOP_LINES
        }
      }
    }
  }

  /**
   * Loads bus stops from a GTFS file.
   */
  @SuppressWarnings("unchecked")
  public void loadStopsFromGtfs() {
    BufferedReader br = null;
    String line = "";
    String cvsSplitBy = ",";
    try {
      br = new BufferedReader(new FileReader(stopFilePath));
      // Ignore file header.
      br.readLine();
      while ((line = br.readLine()) != null) {
        // use comma as separator
        String[] lineParts = line.split(cvsSplitBy);
        //"stop_id","stop_name","stop_desc","stop_lat","stop_lon"
        //18833,"Santo Amaro","Term. Santo Amaro Ref.: Av Pe Jose Maria Cep:04753-60",-23.654365,-46.713015
        Map<String, JSONObject> pObjs = new HashMap<String, JSONObject>();
        JSONObject tmpObj = new JSONObject();
        tmpObj.put("stop_id", lineParts[0]);
        tmpObj.put("stop_name", lineParts[1]);
        tmpObj.put("stop_desc", lineParts[2]);
        tmpObj.put("stop_lat", lineParts[3]);
        tmpObj.put("stop_lon", lineParts[4]);
        pObjs.put(lineParts[0], tmpObj);
        //System.out.println(pObjs);
        if (!pObjs.isEmpty())
          eKeeper.save(pObjs, pStopsSchemaName, pStopsTypeName);
      }
    } catch (FileNotFoundException e) {
      getLogger().error("Error trying to load Stops from GTFS file.");
      e.printStackTrace();
    } catch (IOException e) {
      getLogger().error("Error trying to load Stops from GTFS file.");
      e.printStackTrace();
    } finally {
      if (br != null) {
        try {
          br.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
  }

  public void parseStopsFromLines() {
    if (useLocal()) {
      // 1. Get all data from the keeper
      //TODO this should come according the isGetLocal parameter as well
      List<?> busLines = eKeeper.retrieve(pLinesSchemaName, pLinesTypeName);
     // 2. For each JSON object from the keeper, perform a request.
      for(Object busLine : busLines) {
        String lineCode = ((SearchHit)busLine).getSource().get("CodigoLinha").toString();
        String url = OV_DEFAULT_API_URL + OV_API_STOPS_SEARCH + lineCode;
        Map<String, String> cookies = new HashMap<String, String> ();
        cookies.put(OV_COOKIE_NAME, this.getAuthCookie());
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
            //this.pEntities.add(olhoVivoJsonToPersistentEntity(tmpObj));
          }
        }
        this.waitPolitely();
      }
    } else {
      //TODO
      getLogger().warn("Behaviour for not using local mode hasn't been implemented yet.");
    }
    //for (PersistentEntity pEnt : this.pEntities)
    //  System.out.println(pEnt.toJson());
  }

  /**
   * Retrieves and parses bus arrivals using bus line's codes.
   */
  public void parseStopsForecastFromLines() {
    //TODO this should come according the isGetLocal parameter as well
    if (useLocal()) {
      // 1. Get all data from the keeper
      List<?> busLines = eKeeper.retrieve(pLinesSchemaName, pLinesTypeName);
     // 2. For each JSON object from the keeper, perform a request.
      for(Object busLine : busLines) {
        String lineCode = ((SearchHit)busLine).getSource().get("CodigoLinha").toString();
        String url = OV_DEFAULT_API_URL + OV_API_STOPS_FORECAST_LINE_SEARCH + lineCode;
        Map<String, String> cookies = new HashMap<String, String> ();
        cookies.put(OV_COOKIE_NAME, this.getAuthCookie());
        Document doc = ParserUtils.connectCookiePostUrl(url, cookies);
        //TODO These elements should be parse correctly.
        System.out.println(doc.text());
      }
    }
  }

  /**
   * Gets saved stops as a List of PersistentEntity.
   * @return List containing all PersistentEntity representing stops.
   */
  public List<PersistentEntity> getSavedStops() {
    getLogger().info("Setting entities from saved information.");
    List<?> lineStops = eKeeper.retrieve(pStopsSchemaName, pStopsTypeName);
    // Keeping retrieved entities in-memory to be saved by Retriever
    getLogger().info("Deleting in-memory stored entities.");
    List<PersistentEntity> stopsList = new ArrayList<PersistentEntity>();
    for(Object lineStop : lineStops) {
      JSONObject tmpObj = ParserUtils.getJsonObj(((SearchHit)lineStop).sourceAsMap());
      System.out.println(tmpObj);
      stopsList.add(olhoVivoJsonToPE(tmpObj));
    }
    return stopsList;
  }

  /**
   * Gets a cookie after authenticating.
   * @return
   */
  private String getCookieSpAuthenticate() {
    String authUrl = OV_DEFAULT_API_URL + OV_API_AUTH + OV_DEFAULT_API_TOKEN;
    String cookieVal = ParserUtils.getCookie(authUrl, OV_COOKIE_NAME);
    return cookieVal;
  }

  /**
   * Converts a list of parameters into a PersistentEntity
   * @param params
   * @return
   */
  private PersistentEntity olhoVivoForecastToPE(String...params) {
    PersistentEntity pEntity = new PersistentEntity();
    pEntity.addToScenarioId(SCENARIO_ID);
    pEntity.setHomepage(new Utf8(HOME_PAGE));
    pEntity.setName(new Utf8(params[0]));
    pEntity.setDescription(new Utf8(params[1]));
    PersistentPoint pPoint = new PersistentPoint();
    pPoint.addToCoordinates(Double.parseDouble(params[2]));
    pPoint.addToCoordinates(Double.parseDouble(params[3]));
    pPoint.setAddress(new Utf8(params[4]));
    pPoint.setAccuracy(EylloLocation.GEOCODER_VERIF_ACC_HIGH);
    pEntity.setPersistentpoint(pPoint);
    return pEntity;
  }

  /**
   * Transforms a JSON string from OlhoVivo API into a PersistentEntity
   * @param pJSONObject from OlhoVivo API.
   * @return PersistentEntity from JSON object.
   */
  private PersistentEntity olhoVivoJsonToPE(JSONObject pJSONObject) {
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
}
