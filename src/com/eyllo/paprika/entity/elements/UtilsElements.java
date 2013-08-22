/**
 * 
 */
package com.eyllo.paprika.entity.elements;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.avro.generic.GenericArray;
import org.apache.avro.util.Utf8;

/**
 * @author renatomarroquin
 *
 */
public class UtilsElements {

  /**
   * Converts the persisting points into an mapping array.
   * @param pPoint persisting points
   * @return
   */
  @SuppressWarnings("unchecked")
  public static Map<String, Object>[] locationsToMapArray(PersistentPoint pPoint){
    Map<String, Object>[] resultingMap = null;
    if (pPoint != null) {
      int cont = 0;
      resultingMap = new Map[1];
      Map<String, Object> mapPoint = new HashMap<String, Object>();
      mapPoint.put("locStreetAddress", toString(pPoint.getAddress()));
      // [lon, lat] to be compatible with GeoJSON
      Double coordinates[] = toDoubleArray(pPoint.getCoordinates());
      mapPoint.put("geoPoint", coordinates);
      mapPoint.put("accuracy", new Double(pPoint.getAccuracy()));
      // TODO add state, country as separate properties
      //mapPoint.put("locState", toString(pPoint.getAddress()));
      //mapPoint.put("locCountry", toString(pPoint.getAddress()));
      resultingMap[cont++] = mapPoint;
    }
    return resultingMap;
  }
  /**
   * Transforms the sameAs map into an array.
   * @param pMap contains all the values of the sameAs properties
   * @return An array of Maps containing all properties
   */
  @SuppressWarnings("unchecked")
  public static Map<String, String>[] sameAsToMapArray(Map<Utf8, Utf8> pMap){
    Map<String, String>[] resultingMap = null;
    if (pMap != null){
      int cont = 0;
      resultingMap = new Map[pMap.size()];
      Iterator<Utf8> it = pMap.keySet().iterator();
      while (it.hasNext()) {
        Map<String, String> sameAsExtendedMap = new HashMap<String, String>();
        Utf8 key = it.next();
        sameAsExtendedMap.put("sameAsUrl", key.toString());
        sameAsExtendedMap.put("sameAsText", pMap.get(key).toString());
        resultingMap[cont++] = sameAsExtendedMap;
      }
    }
    return resultingMap;
  }
  /**
   * Converts an Utf8 value to a string
   * @param pString
   * @return
   */
  public static String toString(Utf8 pString){
    return pString!=null?pString.toString():"";
  }

  /**
   * Converts an Avro generic array into a simple array.
   * @param pArray
   * @return
   */
  public static Integer[] toIntArray(GenericArray<Integer> pArray){
    Integer resArray[] = null;
    if (pArray != null) {
      Iterator<Integer> it = pArray.iterator();
      resArray= new Integer[(int) pArray.size()];
      int cont = 0;
      while (it.hasNext())
       resArray[cont++] = it.next();
    }
    return resArray;
  }

  /**
   * Converts an Avro generic array into a simple array.
   * @param pArray
   * @return
   */
  public static Double[] toDoubleArray(GenericArray<Double> pArray){
    Double resArray[] = null;
    if (pArray != null) {
      Iterator<Double> it = pArray.iterator();
      resArray= new Double[(int) pArray.size()];
      int cont = 0;
      while (it.hasNext())
       resArray[cont++] = it.next();
    }
    return resArray;
  }

  /**
   * Converts an Avro generic array into a simple array.
   * @param pArray
   * @return
   */
  public static String[] toStringArray(GenericArray<Utf8> pArray){
    String resArray[] = null;
    if (pArray != null) {
      Iterator<Utf8> it = pArray.iterator();
      resArray= new String[(int) pArray.size()];
      int cont = 0;
      while (it.hasNext())
       resArray[cont++] = it.next().toString();
    }
    return resArray;
  }
}
