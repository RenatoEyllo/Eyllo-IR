/**
 * 
 */
package com.eyllo.paprika.retriever;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
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
import com.eyllo.paprika.retriever.parser.AbstractParser;
import com.eyllo.paprika.retriever.parser.ApontadorParser;
import com.eyllo.paprika.retriever.parser.ParserUtils;
import com.eyllo.paprika.retriever.parser.RioGuiaParser;
import com.eyllo.paprika.retriever.parser.SPTransParser;
import com.eyllo.paprika.retriever.parser.VejaRioParser;
import com.eyllo.paprika.retriever.parser.VejaSaoPauloParser;
import com.eyllo.paprika.retriever.parser.elements.EylloLocation;
import com.eyllo.paprika.retriever.parser.elements.PersistentEntity;
import com.eyllo.paprika.retriever.parser.elements.PersistentPoint;

/**
 * @author renatomarroquin
 *
 */
public class EntityRetriever {

  /** Entities to be retrieved. */
  private List<PersistentEntity> entities;

  /** Geocoder object. */
  private static AbstractGeocoder geocoder;

  /** Logger to help us write write info/debug/error messages. */
  private static Logger LOGGER = LoggerFactory.getLogger(EntityRetriever.class);

  /**
   * @param args
   */
  public static void main(String[] args) {
      // 1. Retrieve entities
    AbstractParser absParser = null; //new SPTransParser(100, 100, true);
    EntityRetriever entRet = new EntityRetriever();
    Properties prop = new Properties();
  	InputStream input = null;
   
    try {
        input = new FileInputStream("/home/renato/workspace/Eyllo-IR/conf/retriever.properties");
        // load a properties file
        prop.load(input);
        entRet.initiateRetriever(prop);
	} catch (FileNotFoundException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
  		
      //entRet.entities = new ApontadorParser("hotels", 50, 20).getEntities();
      absParser.fetchEntities();
      //absParser.completeEntityInfo();
      ParserUtils.writeJsonFile(absParser.getEntities(), absParser.getOutputFileName());
      //entRet.entities = absParser.getEntities();

      // 2. Store entities
      // 3. Complete entities information
      //entRet.entities = updateGeoInfo(entRet.entities);
      //entRet.entities = verifyGeoInfo(entRet.entities);
      //TODO update specific parsers to return their own file name
      //ParserUtils.writeJsonFile(entRet.entities,
      //    DEFAULT_JSON_OUTPUT + absParser.getOutputFileName());
  }

  public void initiateRetriever(Properties pParserProperties) {
    AbstractParser absParser = getCorrectParser(pParserProperties);
    System.out.println(absParser.getParserName());
  }

  private AbstractParser getCorrectParser(Properties pParserProps) {
    
    AbstractParser parser = null;
    ArrayList<Object> initargs = new ArrayList<Object>();

    String tmpValue = pParserProps.getProperty(EntityRetrieverConstants.RPARSER_MAXPAGENUM);
    if (tmpValue != null)
      initargs.add(tmpValue);
    tmpValue = pParserProps.getProperty(EntityRetrieverConstants.RPARSER_MAXNUMENT);
    if (tmpValue != null)
      initargs.add(tmpValue);
    
    tmpValue = pParserProps.getProperty(EntityRetrieverConstants.RPARSER_FETCHURL);
    if (tmpValue != null)
      initargs.add(tmpValue);
    tmpValue = pParserProps.getProperty(EntityRetrieverConstants.RPARSER_OUTPATH);
    if (tmpValue != null)
      initargs.add(tmpValue);
    tmpValue = pParserProps.getProperty(EntityRetrieverConstants.RPARSER_LOCALSEARCH);
    if (tmpValue != null)
      initargs.add(tmpValue);
    tmpValue = pParserProps.getProperty(EntityRetrieverConstants.RPARSER_REQPOLITENESS);
    if (tmpValue != null)
      initargs.add(tmpValue);

    //int pMaxPageNumber, int pMaxNumEntities,
    //String pName, String pOutPath, String pFetchUrl,
    //boolean pLocal, int pPoliteness
    try {
      tmpValue = this.getParserClassName(pParserProps.getProperty(EntityRetrieverConstants.RPARSER_NAME));
      if (tmpValue != null) {
        Constructor<?> constr = Class.forName(tmpValue).getConstructor(initargs.getClass().getClasses());
        parser = (AbstractParser) constr.newInstance(initargs.toArray());  
      }
    } catch (NoSuchMethodException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (SecurityException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (ClassNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (InstantiationException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (IllegalAccessException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (IllegalArgumentException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (InvocationTargetException e) {
      // TODO Auto-generated catch block
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
      pEntities = getEntities(pEntitiesSource);
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
     */
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
    }

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
        else if (pParserName.equals(SPTransParser.getParserName())){
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
                LOGGER.info("Coordinates: " + entLoc.getCoordinates());
                // This will iterate at most the number of geocoders we have
                for (AbstractGeocoder geoC : geocoders){
                    geoC.geoCodeAddress(cleanAddress(entLoc.getAddress().toString()));
                    // Verifying latitude value
                    if (Double.isNaN(lat_found)){
                      LOGGER.info("Coordinates eliminados");
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
                LOGGER.info("Coordinates Actualizadas: " + entLoc.getCoordinates());
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
        LOGGER.debug(pEntity.getName().toString() + "Entity did not have an address.");
      
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
}
