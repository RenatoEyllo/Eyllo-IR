/**
 * 
 */
package com.eyllo.paprika.retriever;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
//import org.bingmaps.rest.models.Confidence;

import java.util.Properties;

import org.apache.avro.generic.GenericArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eyllo.paprika.entity.EntityUtils;
import com.eyllo.paprika.geocoder.AbstractGeocoder;
import com.eyllo.paprika.geocoder.GeocoderFactory;
import com.eyllo.paprika.keeper.EntityKeeper;
import com.eyllo.paprika.retriever.parser.AbstractParser;
import com.eyllo.paprika.retriever.parser.ApontadorParser;
import com.eyllo.paprika.retriever.parser.RioGuiaParser;
import com.eyllo.paprika.retriever.parser.SPTransParser;
import com.eyllo.paprika.retriever.parser.VejaRioParser;
import com.eyllo.paprika.retriever.parser.VejaSaoPauloParser;
import com.eyllo.paprika.retriever.parser.elements.EylloLocation;
import com.eyllo.paprika.retriever.parser.elements.PersistentEntity;
import com.eyllo.paprika.retriever.parser.elements.PersistentPoint;

/**
 * Class in charge of manage parser executions.
 * @author renatomarroquin
 *
 */
public class EntityRetriever {

  /** Geocoder object. */
  private static AbstractGeocoder geocoder;
  /** Parser to be used. */
  private AbstractParser parser;
  /** Number of runs to be performed by EntityRetriever. */
  private int numRuns;
  /** Time interleaved between EntityRetriever runs. */
  private int timeInterleaved;
  /** Specifies how entities retrieved will be stored. */
  private String backendEntities;

  /** Logger to help us write write info/debug/error messages. */
  private static Logger LOGGER = LoggerFactory.getLogger(EntityRetriever.class);

  /**
   * Starts Retriever with values set through properties file.
   * @param pParserProperties
   * @throws InterruptedException
   */
  public void startRetriever(Properties pParserProperties) throws InterruptedException {
    setUpRetrieverProps(pParserProperties);
    // Keeper in charge to storing results for each parser run
    EntityKeeper entKeeper = new EntityKeeper(this.backendEntities,
        pParserProperties.getProperty(RetrieverConstants.RPARSER_OUTPATH),
        pParserProperties.getProperty(RetrieverConstants.RET_BACKEND_SRVR_PORT));
    while (this.numRuns > 0) {
      entKeeper.saveEntities(parser.fetchEntities());
      waitPolitely(this.timeInterleaved);
      System.out.println(parser.getParserName());
      this.numRuns --;
    }
  }

  /**
   * Waits politely for n mili seconds.
   */
  public synchronized void waitPolitely(long pTimeInterleaved) {
    try {
      wait(pTimeInterleaved);
    } catch (InterruptedException e) {
      getLogger().error("Error while waiting to perform a new server request.");
      e.printStackTrace();
    }
  }

  /**
   * Sets up Retriever properties.
   * @param pRetrieverProps
   */
  public void setUpRetrieverProps(Properties pRetrieverProps) {
    String tmpValue = pRetrieverProps.getProperty(RetrieverConstants.RET_BACKEND_ENTITIES);
    backendEntities = (tmpValue == null|| tmpValue.isEmpty())?RetrieverConstants.RET_DEF_BACKEND_ENT:tmpValue;
    tmpValue = pRetrieverProps.getProperty(RetrieverConstants.RET_RUNS_NUM);
    numRuns = (tmpValue == null|| tmpValue.isEmpty())?RetrieverConstants.DEFAULT_NUM_RUNS:Integer.parseInt(tmpValue);
    tmpValue = pRetrieverProps.getProperty(RetrieverConstants.RET_RUNS_INTERLEAVE);
    timeInterleaved = (tmpValue == null|| tmpValue.isEmpty())?RetrieverConstants.DEFAULT_TIME_INTERLEAVED:Integer.parseInt(tmpValue);
    parser = getCorrectParser(pRetrieverProps);
    if (parser == null)
      getLogger().warn("Retriever could NOT create the specific parser.");
  }
  /**
   * Gets the correct parser using the properties file.
   * @param pParserProps  built from specific properties file.
   * @return AbstractParser built using properties within the file.
   */
  private AbstractParser getCorrectParser(Properties pParserProps) {
    AbstractParser parser = null;
    ArrayList<Object> initargs = new ArrayList<Object> ();
    //pMaxPageNumber
    String tmpValue = pParserProps.getProperty(RetrieverConstants.RPARSER_MAXPAGENUM);
    initargs.add((tmpValue == null || tmpValue.isEmpty())?Integer.MAX_VALUE:Integer.parseInt(tmpValue));
    //pMaxNumEntities
    tmpValue = pParserProps.getProperty(RetrieverConstants.RPARSER_MAXNUMENT);
    initargs.add((tmpValue == null)?Integer.MAX_VALUE:Integer.parseInt(tmpValue));
    //pFetchUrl
    tmpValue = pParserProps.getProperty(RetrieverConstants.RPARSER_FETCHURL);
    initargs.add((tmpValue == null)?"":tmpValue);
    //pOutPath
    tmpValue = pParserProps.getProperty(RetrieverConstants.RPARSER_OUTPATH);
    initargs.add((tmpValue == null)?"":tmpValue);
    //pLocal
    tmpValue = pParserProps.getProperty(RetrieverConstants.RPARSER_LOCALSEARCH);
    initargs.add((tmpValue == null)?false:Boolean.parseBoolean(tmpValue));
    //pPoliteness
    tmpValue = pParserProps.getProperty(RetrieverConstants.RPARSER_REQPOLITENESS);
    initargs.add((tmpValue == null)?Integer.MAX_VALUE:Integer.parseInt(tmpValue));

    try {
      tmpValue = this.getParserClassName(pParserProps.getProperty(RetrieverConstants.RPARSER_NAME));
      if (tmpValue != null) {
        Constructor<?> constr = Class.forName(tmpValue).getConstructor(AbstractParser.constrParams);
        parser = (AbstractParser) constr.newInstance(initargs.toArray());  
      } else {
        getLogger().error("Parser not found.");
      }
    } catch (NoSuchMethodException e) {
      getLogger().error("Error trying to get the correct constructor.");
      e.printStackTrace();
    } catch (SecurityException e) {
      getLogger().error("Error trying to get the correct constructor.");
      e.printStackTrace();
    } catch (ClassNotFoundException e) {
      getLogger().error("Error trying to get the correct constructor.");
      e.printStackTrace();
    } catch (InstantiationException e) {
      getLogger().error("Error trying to get the correct constructor.");
      e.printStackTrace();
    } catch (IllegalAccessException e) {
      getLogger().error("Error trying to get the correct constructor.");
      e.printStackTrace();
    } catch (IllegalArgumentException e) {
      getLogger().error("Error trying to get the correct constructor.");
      e.printStackTrace();
    } catch (InvocationTargetException e) {
      getLogger().error("Error trying to get the correct constructor.");
      e.printStackTrace();
    }
    return parser;
  }

  /**
   * Gets entities from a specific data source, but also validates them
   * @param pEntitiesSource
   * @param pEntities
   * @return
   */
  public List<PersistentEntity> getGeoValidatedEntities(String pEntitiesSource, List<PersistentEntity> pEntities){
    /** Getting the entities */
      //TODO update this call for starting geovalidation
      //pEntities = getEntities(pEntitiesSource);
      /** Updates entities' geolocation */
      pEntities = updateGeoInfo(pEntities);
      /** Verifies entities' geolocation with geocoders available */
      pEntities = verifyGeoInfo(pEntities);
      return pEntities;
    }

    /**
     * Retrieves an entity set based on the parser used
     * @param pEntitiesSource
     * @return
    public List<PersistentEntity> getEntities(String pEntitiesSource){
      EntityRetriever entRet = new EntityRetriever();
      if (pEntitiesSource.equals(VejaRioParser.NAME)){
        entRet.entities = new VejaRioParser().getEntities();
      }
      //if (pEntitiesSource.equals(PaginasAmarillasParser.NAME)){
      //  entRet.entities = new PaginasAmarillasParser().getEntities();
      //}
      if (pEntitiesSource.equals(RioGuiaParser.NAME)){
        entRet.entities = new RioGuiaParser().getEntities();
      }
      if (pEntitiesSource.equals(VejaSaoPauloParser.NAME)){
       entRet.entities = new VejaSaoPauloParser().getEntities();
    }
      if (pEntitiesSource.equals(ApontadorParser.NAME)){
        entRet.entities = new ApontadorParser().getEntities();
      }
      return entRet.entities;
    }*/

    /**
     * Gets a parser class name according to its predefined name.
     * @param pParserName
     * @return
     */
    public String getParserClassName(String pParserName) {
    	String parserClassName = null;
        if (pParserName.equals(VejaRioParser.NAME)){
          parserClassName = VejaRioParser.class.getName();
        }
        else if (pParserName.equals(SPTransParser.NAME)){
          parserClassName = SPTransParser.class.getName();
        }
        else if (pParserName.equals(RioGuiaParser.NAME)){
          parserClassName = RioGuiaParser.class.getName();
        }
        else if (pParserName.equals(VejaSaoPauloParser.NAME)){
          parserClassName = VejaSaoPauloParser.class.getName();
        }
        else if (pParserName.equals(ApontadorParser.NAME)){
          parserClassName = ApontadorParser.class.getName();
        }
        return parserClassName;
    }
    /**
     * Verifies the geolocalization obtained for each entity
     * @param pEntities
     * @return
     */
    public static List<PersistentEntity> verifyGeoInfo(List<PersistentEntity> pEntities){
        List<AbstractGeocoder> geocoders = GeocoderFactory.getAllGeocoders();
        for (PersistentEntity ent : pEntities){
            ///ArrayList<EylloLocation> locList = (ArrayList<EylloLocation>) ent.getProperties(EntityUtils.LOCATION);
            PersistentPoint entLoc = ent.getPersistentpoint();
            // This will iterate as the number of addresses we have for each entity i.e. 1
            //for(EylloLocation entLoc : locList){
                if (entLoc == null || entLoc.getAddress() == null || entLoc.getAddress().equals(""))
                    continue;
                //if (!entLoc.getAddress().contains("Rua Joana Ang�lica, 40"))
                //    continue;
                GenericArray<Double> locCoord = entLoc.getCoordinates();
                double lat_prec = 0, lat_found = 0;// = entLoc.getLatitude();
                double lng_prec = 0, lng_found = 0;// = entLoc.getLongitude();
                int iCnt = 0;
                for (double coord:  locCoord){
                    if (iCnt == 0) lat_found = coord;
                    if (iCnt == 1) lng_found = coord;
                    iCnt ++;
                }
                getLogger().info("Coordinates: " + entLoc.getCoordinates());
                // This will iterate at most the number of geocoders we have
                for (AbstractGeocoder geoC : geocoders){
                    geoC.geoCodeAddress(cleanAddress(entLoc.getAddress().toString()));
                    // Verifying latitude value
                    if (Double.isNaN(lat_found)){
                      getLogger().info("Coordinates eliminados");
                      entLoc.getCoordinates().clear();
                      lat_found = geoC.getLatitude();
                      ///entLoc.setLatitude(geoC.getLatitude());
                      entLoc.addToCoordinates(lat_found);
                      entLoc.setAccuracyFromGeocoder(geoC.getLocationConfidence());
                    }
                    else{
                        lat_prec += Math.abs(lat_found) - Math.abs(geoC.getLatitude());
                        lat_found = geoC.getLatitude();
                    }
                    // Verifying longitude value
                    if (Double.isNaN(lng_found)){
                        lng_found = geoC.getLongitude();
                        ///entLoc.setLongitude(geoC.getLongitude());
                        entLoc.addToCoordinates(lng_found);
                        entLoc.setAccuracyFromGeocoder(geoC.getLocationConfidence());
                    }
                    else{
                        lng_prec += Math.abs(lng_found) - Math.abs(geoC.getLongitude());
                        lng_found = geoC.getLongitude();
                    }
                }//END-FOR GEOCODERS
                getLogger().info("Coordinates Actualizadas: " + entLoc.getCoordinates());
                double finalLatPrec = Math.abs(lat_prec);
                double finalLngPrec = Math.abs(lng_prec);
                // if any of them are still 0, that means that we didn't have any to compare with
                if (finalLatPrec != 0 || finalLngPrec != 0){
                    if ( finalLatPrec < EylloLocation.LOC_PREC_THRESHOLD_LOW 
                            || finalLngPrec < EylloLocation.LOC_PREC_THRESHOLD_LOW)
                        entLoc.setAccuracy(EylloLocation.GEOCODER_VERIF_ACC_LOW);
                    if (finalLatPrec < EylloLocation.LOC_PREC_THRESHOLD_MEDIUM
                            || finalLngPrec < EylloLocation.LOC_PREC_THRESHOLD_MEDIUM)
                        entLoc.setAccuracy(EylloLocation.GEOCODER_VERIF_ACC_MEDIUM);
                    if (finalLatPrec < EylloLocation.LOC_PREC_THRESHOLD_HIGH
                            || finalLngPrec < EylloLocation.LOC_PREC_THRESHOLD_HIGH)
                        entLoc.setAccuracy(EylloLocation.GEOCODER_VERIF_ACC_HIGH);
                }//END-IF FINAL
            //}//END-FOR LOCATIONS
        }//END-FOR ENTITIES
        return pEntities;
    }

    /**
     * Updates geoInformation about entity's location
     */
    public static List<PersistentEntity> updateGeoInfo(List<PersistentEntity> pEntities){
        // 1. Read entities which its geo information is not complete
        // 2. Complete its geo Information
        geocoder = GeocoderFactory.getGeocoder("google");
        for (PersistentEntity ent : pEntities){
          //if (ent.getLocations().size() > 0)
          //  if (ent.getLocations().get(0).getAddress().contains("Rua Joana Ang�lica, 40"))
          ent = setGeoLocations(ent, geocoder);
          //break;
        }
        return pEntities;
    }

    /**
     * Gets specific geographic locations for an entity's address
     */
    public static PersistentEntity setGeoLocations(PersistentEntity pEntity, AbstractGeocoder pGeoCoder){
      ///ArrayList<EylloLocation> locList = (ArrayList<EylloLocation>) pEntity.getProperties(EntityUtils.LOCATION);
      PersistentPoint entLoc = pEntity.getPersistentpoint();
      if (entLoc != null && entLoc.getAddress() != null){
        pGeoCoder.geoCodeAddress(cleanAddress(entLoc.getAddress().toString()));
        entLoc.getCoordinates().clear();
        ///entLoc.setLatitude(pGeoCoder.getLatitude());
        entLoc.getCoordinates().add(pGeoCoder.getLatitude());
        //entLoc.setLongitude(pGeoCoder.getLongitude());
        entLoc.getCoordinates().add(pGeoCoder.getLongitude());
        entLoc.setAccuracyFromGeocoder(pGeoCoder.getLocationConfidence());
      }
      else
        getLogger().debug(pEntity.getName().toString() + "Entity did not have an address.");
      
      return pEntity;
    }

    /**
     * Cleans an address within a string
     * @param pAddress
     * @return
     */
    private static String cleanAddress(String pAddress){
        StringBuilder cleanAdd = new StringBuilder();
        if (pAddress.toLowerCase().contains("loja") || pAddress.toLowerCase().contains("lja")){
            String parts [] = pAddress.split("-");
            for (String part : parts){
                if (!part.toLowerCase().contains("loja"))
                    cleanAdd.append(part).append(EntityUtils.INFO_SEP);
                //if || !part.toLowerCase().contains("lja"))
            }
        }
        if (cleanAdd.toString().trim().equals(""))
            cleanAdd.append(pAddress);
        String regex = "\\s{2,}"; 
        return cleanAdd.toString().replace("-", "").replace(":"," ").toString().replaceAll(regex, " ").trim();
    }

    public static Logger getLogger() {
      return LOGGER;
    }

    public static void setLogger(Logger lOGGER) {
      LOGGER = lOGGER;
    }

    /**
     * @return the numRuns
     */
    public int getNumRuns() {
      return numRuns;
    }

    /**
     * @param numRuns the numRuns to set
     */
    public void setNumRuns(int numRuns) {
      this.numRuns = numRuns;
    }

    /**
     * @return the timeInterleave
     */
    public int getTimeInterleaved() {
      return timeInterleaved;
    }

    /**
     * @param timeInterleave the timeInterleave to set
     */
    public void setTimeInterleaved(int timeInterleave) {
      this.timeInterleaved = timeInterleave;
    }

    /**
     * @return the persistencyPlace
     */
    public String getPersistencyPlace() {
      return backendEntities;
    }

    /**
     * @param persistencyPlace the persistencyPlace to set
     */
    public void setPersistencyPlace(String persistencyPlace) {
      this.backendEntities = persistencyPlace;
    }
}
