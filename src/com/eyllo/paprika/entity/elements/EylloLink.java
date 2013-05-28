package com.eyllo.paprika.entity.elements;

import java.util.HashMap;
import java.util.Map;

import org.jsoup.nodes.Element;

import com.eyllo.paprika.html.parser.ParseUtils;

/**
 * Class encapsulating the link element
 * @author renatomarroquin
 *
 */
public class EylloLink {

  private String linkText;
  private String linkHref;
  private Element link;

  private static String MAIN_JSON_ELEM = "link";
  private static String TEXT_JSON_ELEM = "text";
  private static String HREF_JSON_ELEM = "href";
  
  /**
   * String to separate link values
   */
  public static String VAL_SEP = ":";

  /**
   * Default constructor
   */
  public EylloLink(){
  }

  /**
   * Constructor with parameters
   * @param pLinkText
   * @param pLinkHref
   * @param pLink
   */
  public EylloLink(String pLinkText, String pLinkHref, Element pLink){
    this.setLink(pLink);
    this.setLinkText(pLinkText);
    this.setLinkHref(pLinkHref);
  }

  /**
   * "link":{"text":"Google","href":"www.google.com"}
   * @return
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public String toJson(){
    Map link = new HashMap();
    link.put(TEXT_JSON_ELEM, this.getLinkText());
    link.put(HREF_JSON_ELEM, this.getLinkHref());
    Map tmp = new HashMap();
    tmp.put(MAIN_JSON_ELEM, link);
    return ParseUtils.getJsonObj(tmp).toJSONString();
  }

  /**
   * Method to return link information as a string
   */
  @Override
  public String toString(){
    StringBuilder strBuilder = new StringBuilder();
    strBuilder.append(this.linkText).append(EylloLink.VAL_SEP);
    strBuilder.append(this.linkHref);
    return strBuilder.toString();
  }

  /**
   * @return the linkText
   */
  public String getLinkText() {
    return linkText;
  }
  /**
   * @param linkText the linkText to set
   */
  public void setLinkText(String linkText) {
    this.linkText = linkText;
  }
  /**
   * @return the linkHref
   */
  public String getLinkHref() {
    return linkHref;
  }
  /**
   * @param linkHref the linkHref to set
   */
  public void setLinkHref(String linkHref) {
    this.linkHref = linkHref;
  }
  /**
   * @return the link
   */
  public Element getLink() {
    return link;
  }
  /**
   * @param link the link to set
   */
  public void setLink(Element link) {
    this.link = link;
  }
  
}
