package com.eyllo.paprika.external.api;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.xml.sax.SAXException;

import com.eyllo.paprika.parser.StoresParser;

public class GoogleGeocoding {

  // URL prefix to the geocoder
  private static final String GEOCODER_REQUEST_PREFIX_FOR_XML = "http://maps.google.com/maps/api/geocode/xml";

  private float latitude;
  
  private float longitude;
  
  private String formattedAddress;
  
  private static Logger LOGGER = LoggerFactory.getLogger(StoresParser.class);
  
  public GoogleGeocoding(){
    this.latitude = Float.NaN;
    this.longitude = Float.NaN;
  }

  /**
   * Geocodes and sets latitude and longitude for a specific address
   * @param pAddress
   */
  public void geoCodeAddress(String pAddress){
    
    // prepare a URL to the geocoder
    URL url;
    HttpURLConnection conn = null;
    
    try {
    
      url = new URL(GEOCODER_REQUEST_PREFIX_FOR_XML + "?address=" + URLEncoder.encode(pAddress, "UTF-8") + "&sensor=false");
      
      //System.out.println(url);
      // prepare an HTTP connection to the geocoder
      conn = (HttpURLConnection) url.openConnection();

      Document geocoderResultDocument = null;

      // open the connection and get results as InputSource.
      conn.connect();
      InputSource geocoderResultInputSource = new InputSource(conn.getInputStream());

      // read result and parse into XML Document
      geocoderResultDocument = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(geocoderResultInputSource);

      // prepare XPath
      XPath xpath = XPathFactory.newInstance().newXPath();

      // extract the result
      NodeList resultNodeList = null;

      // a) obtain the formatted_address field for every result
      resultNodeList = (NodeList) xpath.evaluate("/GeocodeResponse/result/formatted_address", geocoderResultDocument, XPathConstants.NODESET);
      for(int i=0; i<resultNodeList.getLength(); ++i) {
        this.setFormattedAddress(resultNodeList.item(i).getTextContent());
        LOGGER.debug("gcc " + resultNodeList.item(i).getTextContent());
      }

      // b) extract the locality for the first result
      /*resultNodeList = (NodeList) xpath.evaluate("/GeocodeResponse/result[1]/address_component[type/text()='locality']/long_name", geocoderResultDocument, XPathConstants.NODESET);
      for(int i=0; i<resultNodeList.getLength(); ++i) {
        System.out.println("gcc " + resultNodeList.item(i).getTextContent());
      }*/

      // c) extract the coordinates of the first result
      resultNodeList = (NodeList) xpath.evaluate("/GeocodeResponse/result[1]/geometry/location/*", geocoderResultDocument, XPathConstants.NODESET);
      for(int i=0; i<resultNodeList.getLength(); ++i) {
        Node node = resultNodeList.item(i);
        if("lat".equals(node.getNodeName())) this.setLatitude(Float.parseFloat(node.getTextContent()));
        if("lng".equals(node.getNodeName())) this.setLongitude(Float.parseFloat(node.getTextContent()));
      }
      LOGGER.debug("lat/lng=" + this.getLatitude() + "," + this.getLongitude());

      // c) extract the coordinates of the first result
      /*resultNodeList = (NodeList) xpath.evaluate("/GeocodeResponse/result[1]/address_component[type/text() = 'administrative_area_level_1']/country[short_name/text() = 'US']/*", geocoderResultDocument, XPathConstants.NODESET);
      lat = Float.NaN;
      lng = Float.NaN;
      for(int i=0; i<resultNodeList.getLength(); ++i) {
        Node node = resultNodeList.item(i);
        if("lat".equals(node.getNodeName())) lat = Float.parseFloat(node.getTextContent());
        if("lng".equals(node.getNodeName())) lng = Float.parseFloat(node.getTextContent());
      }
      System.out.println("lat/lng=" + lat + "," + lng);
       */

    } catch (MalformedURLException e) {
      LOGGER.error("Error while geodecoding");
      e.printStackTrace();
    } catch (UnsupportedEncodingException e) {
      LOGGER.error("Error while geodecoding");
      e.printStackTrace();
    } catch (IOException e) {
      LOGGER.error("Error while geodecoding");
      e.printStackTrace();
    } catch (XPathExpressionException e) {
      LOGGER.error("Error while geodecoding");
      e.printStackTrace();
    } catch (SAXException e) {
      LOGGER.error("Error while geodecoding");
      e.printStackTrace();
    } catch (ParserConfigurationException e) {
      LOGGER.error("Error while geodecoding");
      e.printStackTrace();
    } finally {
      conn.disconnect();
      LOGGER.debug("Disconnected from geodecoding");
    }

  }

  public float getLatitude() {
    return latitude;
  }

  public void setLatitude(float latitude) {
    this.latitude = latitude;
  }

  public float getLongitude() {
    return longitude;
  }

  public void setLongitude(float longitude) {
    this.longitude = longitude;
  }

  public String getFormattedAddress() {
    return formattedAddress;
  }

  public void setFormattedAddress(String formattedAddress) {
    this.formattedAddress = formattedAddress;
  }
}
