package com.eyllo.paprika.ir;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
    return this.toString();
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
    Object obj = properties.get(pProperty);
    if (obj != null)
      obj = pObject.toString() + obj.toString();
    this.properties.put(pProperty, obj);
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
