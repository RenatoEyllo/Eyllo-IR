package com.eyllo.paprika.ir;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.eyllo.paprika.html.elements.Link;

public class Entity {

  private List<Link> links;
  private Map<String, Object> properties;

  public Entity(){
    links = new ArrayList<Link>();
    properties = EntityUtils.initializeProperties();
    this.setProperties(EntityUtils.SAME_AS, links);
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
        it.remove(); // avoids a ConcurrentModificationException
    }
    return strBuilder.toString();
  }

  /**
   * Returns the object in JSON format
   * @return
   */
  public String toJson(){
    return this.toString();
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
      pObject = properties.get(pProperty).toString() + EntityUtils.VALUE_SEP + pObject.toString();
    this.properties.put(pProperty, pObject);
  }

  /**
   * @return the links
   */
  public List<Link> getLinks() {
    return links;
  }

  /**
   * @param links the links to set
   */
  public void addLink(Link links) {
    this.links.add(links);
  }
}
