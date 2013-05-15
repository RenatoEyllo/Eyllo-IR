package com.eyllo.paprika.html.elements;

import org.jsoup.nodes.Element;

/**
 * Class encapsulating the link element
 * @author renatomarroquin
 *
 */
public class Link {

  private String linkText;
  private String linkHref;
  private Element link;
  
  public Link(){
  }
  
  public Link(String pLinkText, String pLinkHref, Element pLink){
    this.setLink(pLink);
    this.setLinkText(pLinkText);
    this.setLinkHref(pLinkHref);
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
