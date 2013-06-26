package com.eyllo.paprika.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.eyllo.paprika.entity.elements.EylloLink;
import com.eyllo.paprika.entity.elements.EylloLocation;
import com.eyllo.paprika.html.parser.ParseUtils;

public class Entity {

  private List<EylloLink> links;
  private List<EylloLocation> locations;
  private Map<String, Object> properties;

  /**
   * Default constructor for an entity
   */
  public Entity(){
    links = new ArrayList<EylloLink>();
    locations = new ArrayList<EylloLocation>();
    properties = EntityUtils.initializeProperties();
    this.setProperties(EntityUtils.SAME_AS, links);
    this.setProperties(EntityUtils.LOCATION, locations);
  }
  
  /**
   * Returns the object as a string
   */
  @Override
  public String toString(){
    StringBuilder strBuilder = new StringBuilder();
    Iterator<Entry<String, Object>> it = properties.entrySet().iterator();
    while (it.hasNext()) {
        Map.Entry<String, Object> pairs = (Map.Entry<String, Object>)it.next();
        strBuilder.append(pairs.getKey()).append(":");
        strBuilder.append(pairs.getValue()==null?"":pairs.getValue().toString()).append("\n");
        //System.out.println(pairs.getKey() + " = " + pairs.getValue());
        //it.remove(); // avoids a ConcurrentModificationException
    }
    return strBuilder.toString();
  }

  /**
   * Returns the object in JSON format
   * {"scenarioId":6,
   * "userId":"2",
   * "title":"LojasAmericanas",
   * "text":"Barro Preto",
   * "location":{"longitude":-43.952023,"latitude":-19.921711},
   * "type":"text",
   * "infobox":{"text":"Avenida Augusto de Lima n.º 1313 - Barro Preto , Belo Horizonte","title":"Barro Preto"}
   * }
   * @return
   */
  // TODO get a better way to get the necessary information
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public String toJson(){
    Map jsonMap = new HashMap();
    jsonMap.put(EntityUtils.SCENARIO_ID, this.properties.get(EntityUtils.SCENARIO_ID));
    jsonMap.put("userId", 2);
    jsonMap.put("title", this.properties.get(EntityUtils.NAME));
    jsonMap.put("text", this.properties.get(EntityUtils.EXTRA_INFO));
    //location
    Map infoBox = new HashMap();
    Map geoJson = new HashMap();
    ArrayList<EylloLocation> locList = (ArrayList<EylloLocation>) this.properties.get(EntityUtils.LOCATION);
    for(EylloLocation entLoc : locList){
      geoJson.put("type", "Point");
      List<Double> coordinates = new ArrayList();
      coordinates.add(entLoc.getLongitude());
      coordinates.add(entLoc.getLatitude());
      geoJson.put("coordinates", coordinates);
      geoJson.put("accuracy", entLoc.getAccuracy());
      //jsonMap.put("location", entLoc.getAttribMap());
      infoBox.put("text", entLoc.getAddress());
    }
    jsonMap.put("type", "text");
    //infobox
    infoBox.put(EntityUtils.HOME_PAGE, this.properties.get(EntityUtils.HOME_PAGE));
    infoBox.put(EntityUtils.PHONES, this.properties.get(EntityUtils.PHONES));
    infoBox.put(EntityUtils.SERVICES, this.properties.get(EntityUtils.SERVICES));
    infoBox.put(EntityUtils.SCHEDULE, this.properties.get(EntityUtils.SCHEDULE));
    jsonMap.put("infobox", infoBox);
    jsonMap.put("loc", geoJson);
    return ParseUtils.getJsonObj(jsonMap).toJSONString();
  }
  
  /**
   * @return the properties
   */
  public Object getProperties(String pProperty) {
    return properties.get(pProperty);
  }

  /**
   * @param properties the properties to set
   */
  public void setProperties(String pProperty, Object pObject) {
    if (properties.get(pProperty) != null)
      pObject = pObject.toString() + EntityUtils.VALUE_SEP + properties.get(pProperty).toString();
    this.properties.put(pProperty, pObject);
  }

  /**
   * Replaces a property's value
   * @param pProperty
   * @param pObject
   */
  public void replaceProperty(String pProperty, Object pObject){
    this.properties.put(pProperty, pObject);
  }

  /**
   * @return the links
   */
  public List<EylloLink> getLinks() {
    return links;
  }

  /**
   * @param links the links to set
   */
  public void addLink(EylloLink links) {
    this.links.add(links);
  }

  /**
   * @return the locations
   */
  public List<EylloLocation> getLocations() {
    return locations;
  }

  /**
   * @param locations the locations to set
   */
  public void addLocations(EylloLocation locations) {
    this.locations.add(locations);
  }
}
