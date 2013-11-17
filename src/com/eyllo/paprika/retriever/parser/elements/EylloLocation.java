package com.eyllo.paprika.retriever.parser.elements;

import java.util.HashMap;
import java.util.Map;

import org.bingmaps.rest.models.Confidence;

import com.eyllo.paprika.retriever.parser.ParseUtils;

/**
 * Class in charge of handling all the location information
 * @author renatomarroquin
 */
public class EylloLocation {

  /**
   * Accurary values for each location
   */
  public static final int INITIAL_ACC_LOW = 0;
  public static final int INITIAL_ACC_MEDIUM = 1;
  public static final int INITIAL_ACC_HIGH = 2;
  // it can be from 3 - 5
  public static final int GEOCODER_VERIF_ACC_LOW = 3;
  public static final int GEOCODER_VERIF_ACC_MEDIUM = 4;
  public static final int GEOCODER_VERIF_ACC_HIGH = 5;
  public static final int HUMAN_VERIF_ACC = 7;
  public static final int ALL_VERIF_ACC = 10;
  
  public static final double LOC_PREC_THRESHOLD_LOW = 0.01;
  public static final double LOC_PREC_THRESHOLD_MEDIUM = 0.001;
  public static final double LOC_PREC_THRESHOLD_HIGH = 0.0001;
  
  /**
   * Default address to be geocoded
   */
  private String address;
  
  /**
   * Latitude found for an specific address
   */
  private double latitude;
  
  /**
   * Longitude found for an specific address
   */
  private double longitude;
  
  /**
   * Accuracy for the specific address found
   */
  private double accuracy;
  
  /**  
   * JSON tags
   */
  private static String MAIN_JSON_ELEM = "loc";
  private static String LONG_JSON_ELEM = "lng";
  private static String LAT_JSON_ELEM = "lat";
  private static String ACC_JSON_ELEM = "accuracy";
  
  @Override
  public String toString(){
    return this.toString();
  }

  /**
   * "location":{"longitude":-43.952023,"latitude":-19.921711}
   * @return
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public String toJson(){
    Map location = new HashMap();
    location.put(LAT_JSON_ELEM, this.getLatitude());
    location.put(LONG_JSON_ELEM, this.getLongitude());
    location.put(ACC_JSON_ELEM, this.getAccuracy());
    Map tmp = new HashMap();
    tmp.put(MAIN_JSON_ELEM, location);
    return ParseUtils.getJsonObj(tmp).toJSONString();
  }

  /**
   * Helper method to export to JSON format
   * @return
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public Map getAttribMap(){
    Map location = new HashMap();
    location.put(LAT_JSON_ELEM, this.getLatitude());
    location.put(LONG_JSON_ELEM, this.getLongitude());
    location.put(ACC_JSON_ELEM, this.getAccuracy());
    return location;
  }

  /**
   * @return the address
   */
  public String getAddress() {
    return address;
  }

  /**
   * @param address the address to set
   */
  public void setAddress(String address) {
    this.address = address;
  }

  /**
   * @return the latitude
   */
  public double getLatitude() {
    return latitude;
  }

  /**
   * @param latitude the latitude to set
   */
  public void setLatitude(double latitude) {
    this.latitude = latitude;
  }

  /**
   * @return the longitude
   */
  public double getLongitude() {
    return longitude;
  }

  /**
   * @param longitude the longitude to set
   */
  public void setLongitude(double longitude) {
    this.longitude = longitude;
  }

    /**
     * @return the accuracy
     */
    public double getAccuracy() {
        return accuracy;
    }
    
    /**
     * @param accuracy the accuracy to set
     */
    public void setAccuracy(double accuracy) {
        this.accuracy = accuracy;
    }
    
    /**
     * Sets Accuracy based on geocoder's confidence level
     * @param pConfidence
     */
    public void setAccuracyFromGeocoder(int pConfidence){
        switch (pConfidence ){
            case Confidence.High: this.setAccuracy(EylloLocation.INITIAL_ACC_HIGH);break;
            case Confidence.Medium: this.setAccuracy(EylloLocation.INITIAL_ACC_MEDIUM);break;
            case Confidence.Low: this.setAccuracy(EylloLocation.INITIAL_ACC_LOW); break;
            case Confidence.Unknown: this.setAccuracy(EylloLocation.INITIAL_ACC_LOW); break;
        }
    }
}
