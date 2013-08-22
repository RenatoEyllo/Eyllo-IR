/**
 * 
 */
package com.eyllo.paprika.entity.jobs;

import java.util.List;
//import org.bingmaps.rest.models.Confidence;

import org.apache.avro.generic.GenericArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eyllo.paprika.entity.EntityUtils;
import com.eyllo.paprika.entity.elements.EylloLocation;
import com.eyllo.paprika.entity.elements.PersistentEntity;
import com.eyllo.paprika.entity.elements.PersistentPoint;
import com.eyllo.paprika.geocoder.AbstractGeocoder;
import com.eyllo.paprika.geocoder.GeocoderFactory;
import com.eyllo.paprika.html.parser.ApontadorParser;
import com.eyllo.paprika.html.parser.PaginasAmarillasParser;
import com.eyllo.paprika.html.parser.ParseUtils;
import com.eyllo.paprika.html.parser.RioGuiaParser;
import com.eyllo.paprika.html.parser.VejaRioParser;
import com.eyllo.paprika.html.parser.VejaSaoPauloParser;

/**
 * @author renatomarroquin
 *
 */
public class EntityRetriever {

    ///private List<Entity> entities;
    private List<PersistentEntity> entities;
    public static String DEFAULT_GEOCODER = "google";
    private static AbstractGeocoder geocoder;
    private static String DEFAULT_JSON_OUTPUT = "/Users/renatomarroquin/Documents/workspace/workspaceEyllo/Eyllo-IR/res/";
    /**
     * Logger to help us write write info/debug/error messages
     */
    private static Logger LOGGER = LoggerFactory.getLogger(EntityRetriever.class);
    
    /**
     * @param args
     */
    public static void main(String[] args) {
        // 1. Retrieve entities
        EntityRetriever entRet = new EntityRetriever();
        entRet.entities = new ApontadorParser("hotels", 50, 20).getEntities();
        // 2. Store entities
        // 3. Complete entities information
        //entRet.entities = updateGeoInfo(entRet.entities);
        //entRet.entities = verifyGeoInfo(entRet.entities);
        //TODO update specific parsers to return their own file name
        ParseUtils.writeJsonFile(entRet.entities,
            DEFAULT_JSON_OUTPUT + ApontadorParser.getOutputFileName());
    }

    /**
     * Gets entities from a specific data source, but also validates them
     * @param pEntitiesSource
     * @param pEntities
     * @return
     */
    public static List<PersistentEntity> getGeoValidatedEntities(String pEntitiesSource, List<PersistentEntity> pEntities){
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
    public static List<PersistentEntity> getEntities(String pEntitiesSource){
      EntityRetriever entRet = new EntityRetriever();
      if (pEntitiesSource.equals(VejaRioParser.NAME)){
        entRet.entities = new VejaRioParser().getEntities();
      }
      if (pEntitiesSource.equals(PaginasAmarillasParser.NAME)){
        entRet.entities = new PaginasAmarillasParser().getEntities();
      }
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
