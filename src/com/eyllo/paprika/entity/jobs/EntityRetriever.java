/**
 * 
 */
package com.eyllo.paprika.entity.jobs;

import java.util.ArrayList;
import java.util.List;

//import org.bingmaps.rest.models.Confidence;

import com.eyllo.paprika.entity.Entity;
import com.eyllo.paprika.entity.EntityUtils;
import com.eyllo.paprika.entity.elements.EylloLocation;
import com.eyllo.paprika.geocoder.AbstractGeocoder;
import com.eyllo.paprika.geocoder.GeocoderFactory;
import com.eyllo.paprika.html.parser.ParseUtils;
import com.eyllo.paprika.html.parser.VejaRioParser;

/**
 * @author renatomarroquin
 *
 */
public class EntityRetriever {

    private List<Entity> entities;
    public static String DEFAULT_GEOCODER = "google";
    private AbstractGeocoder geocoder;
    private static String DEFAULT_JSON_OUTPUT = "/Users/renatomarroquin/Documents/workspace/workspaceEyllo/Eyllo-IR/res/vejario/output";
    private static String DEFAULT_JSON_FILE = "/vejaRio.json";
    
    /**
     * @param args
     */
    public static void main(String[] args) {
        // 1. Retrieve entities
        EntityRetriever entRet = new EntityRetriever();
        entRet.entities = VejaRioParser.getEntities();
        // 2. Store entities
        // 3. Complete entities information
        entRet.updateGeoInfo();
        entRet.verifyGeoInfo();
        ParseUtils.writeJsonFile(entRet.entities, DEFAULT_JSON_OUTPUT + DEFAULT_JSON_FILE);
    }

    private void verifyGeoInfo(){
        List<AbstractGeocoder> geocoders = GeocoderFactory.getAllGeocoders();
        
        for (Entity ent : this.entities){
            @SuppressWarnings("unchecked")
            ArrayList<EylloLocation> locList = (ArrayList<EylloLocation>) ent.getProperties(EntityUtils.LOCATION);
            // This will iterate as the number of addresses we have for each entity i.e. 1
            for(EylloLocation entLoc : locList){
                if (entLoc.getAddress().equals(""))
                    continue;
                //if (!entLoc.getAddress().contains("Rua Joana Angélica, 40"))
                //    continue;
                double lat_prec = 0, lat_found = entLoc.getLatitude();
                double lng_prec = 0, lng_found = entLoc.getLongitude();
                // This will iterate at most the number of geocoders we have
                for (AbstractGeocoder geoC : geocoders){
                    geoC.geoCodeAddress(cleanAddress(entLoc.getAddress()));
                    // Verifying latitude value
                    if (Double.isNaN(lat_found)){
                        lat_found = geoC.getLatitude();
                        entLoc.setLatitude(geoC.getLatitude());
                        entLoc.setAccuracyFromGeocoder(geoC.getLocationConfidence());
                    }
                    else{
                        lat_prec += Math.abs(lat_found) - Math.abs(geoC.getLatitude());
                        lat_found = geoC.getLatitude();
                    }
                    // Verifying longitude value
                    if (Double.isNaN(lng_found)){
                        lng_found = geoC.getLongitude();
                        entLoc.setLongitude(geoC.getLongitude());
                        entLoc.setAccuracyFromGeocoder(geoC.getLocationConfidence());
                    }
                    else{
                        lng_prec += Math.abs(lng_found) - Math.abs(geoC.getLongitude());
                        lng_found = geoC.getLongitude();
                    }
                }
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
                }
            }
        }
    }

    /**
     * Updates geoInformation about entity's location
     */
    private void updateGeoInfo(){
        // 1. Read entities which its geo information is not complete
        // 2. Complete its geo Information
        this.geocoder = GeocoderFactory.getGeocoder("google");
        for (Entity ent : this.entities){
            //if (ent.getLocations().size() > 0)
              //  if (ent.getLocations().get(0).getAddress().contains("Rua Joana Angélica, 40"))
                    this.setGeoLocations(ent, this.geocoder);
            //break;
        }
    }

    /**
     * Gets specific geographic locations for an entity's address
     */
    @SuppressWarnings("unchecked")
    private void setGeoLocations(Entity pEntity, AbstractGeocoder pGeoCoder){
      ArrayList<EylloLocation> locList = (ArrayList<EylloLocation>) pEntity.getProperties(EntityUtils.LOCATION);
      for(EylloLocation entLoc : locList){
        
        pGeoCoder.geoCodeAddress(cleanAddress(entLoc.getAddress()));
        entLoc.setLatitude(pGeoCoder.getLatitude());
        //entLoc.setLatitude(1);
        entLoc.setLongitude(pGeoCoder.getLongitude());
        //entLoc.setLongitude(1);
        entLoc.setAccuracyFromGeocoder(pGeoCoder.getLocationConfidence());
      }
    }
    
    private String cleanAddress(String pAddress){
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
