/**
 * 
 */
package com.eyllo.paprika.retriever.parser;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.avro.util.Utf8;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.eyllo.paprika.retriever.parser.elements.EylloLink;
import com.eyllo.paprika.retriever.parser.elements.EylloLocation;
import com.eyllo.paprika.retriever.parser.elements.PersistentEntity;
import com.eyllo.paprika.retriever.parser.elements.PersistentPoint;

/**
 * @author renatomarroquin
 *
 */
public class RioShowParser extends AbstractParser {

  private static String DEFAULT_SEARCH_URL = "http://rioshow.oglobo.globo.com/gastronomia/home.aspx";
  private String entityType = "Restaurant";

  /**
   * Default constructor.
   */
  public RioShowParser() {
    super();
    this.setName("rioshow");
    this.setScenarioId(11);
    this.setUserId(20);
    this.setUrl(DEFAULT_SEARCH_URL);
    setLogger(getClass());
  }

  @Override
  public List<PersistentEntity> fetchEntities() {
    Document doc = ParserUtils.connectGetUrl(ParserUtils.getUri(this.getUrl()).toASCIIString());
    doc.setBaseUri(this.getUrl());
  //  int cont = 0;
    // get initial list
    Elements elemsPosted = doc.select("div[class*=content]")
        .select("div[class*=lista_item]");
    if (elemsPosted != null) {
      for (Element elemPosted : elemsPosted) {
        EylloLink link = ParserUtils.detectUrl(elemPosted.children().get(1));
        if (link != null) {
          // Parse each result site
          this.parseSearchResults(link.getLinkHref());
         // cont ++;
        }
        else
          getLogger().warn("There was no link inside " + elemsPosted);
//        if (cont == 20)
  //        break;
      }
    }
    System.out.println("Habian " + this.pEntities.size());
    this.completeEntityInfo();
    return this.pEntities;
  }

  private boolean validateRioShow(Document doc) {
    boolean flg = false;
    if (doc.select("div[id*=erroContainer]").isEmpty())
      flg = true;
    return flg;
  }

  @Override
  public void parseSearchResults(String url) {
    getLogger().info("Started parsing: " + url);
    Document doc = null;
    
    doc = ParserUtils.connectGetUrl(ParserUtils.getUri(url).toASCIIString());
    doc.setBaseUri(url);
    if (validateRioShow(doc)) {
      Elements descElems = doc.select("div[id*=DescricaoEvento_divExibeSinopse]");
      if (descElems != null) {
        if (descElems.select("p") != null) {
          Elements infoLinks = descElems.select("p").first().select("a");
          if (infoLinks != null)
            for (Element elem : infoLinks) {
              EylloLink eyLink = ParserUtils.detectUrl(elem);
              if (eyLink != null) {
                PersistentEntity pEnt = new PersistentEntity();
                pEnt.setName(new Utf8(eyLink.getLinkText()));
                pEnt.putToSameAs(new Utf8(eyLink.getLinkHref()), new Utf8(eyLink.getLinkText()));
                //System.out.println(pEnt);
                this.pEntities.add(pEnt);
                //break;
              }
            }// END-FOR INFOLINKS
        }// END-IF P IN DESCRIPTIONS
      }// END-IF DESCRIPTIONS
    }
    else 
      getLogger().error("Error getting. Please verify site:" + url);
  }
  /**
   * Gets the entity type
   * @return String containing the entity type
   */
  public String getEntityType() {
    return entityType;
  }
  /**
   * @param args
   */
  public static void main(String[] args) {
    RioShowParser rioSh = new RioShowParser();
    //rioSh.getEntities(DEFAULT_SEARCH_URL);
    rioSh.setPath("/Users/renatomarroquin/Documents/workspace/workspaceEyllo/Eyllo-IR/res/");
    ParserUtils.writeJsonFile(rioSh.fetchEntities(), rioSh.getOutputFileName());
  }

  @Override
  public void parseIndividualEnt(PersistentEntity pEntity) {
    Document doc = null;
    Iterator<Entry<Utf8, Utf8>> it = pEntity.getSameAs().entrySet().iterator();
    getLogger().debug("aqui estoy");
    while (it.hasNext()) {
      Map.Entry<Utf8, Utf8> pairs = (Map.Entry<Utf8, Utf8>)it.next();
      // Reading individual URLs
      getLogger().debug("Parsing entity from: " + ParserUtils.getUri(pairs.getKey().toString()).toASCIIString());
      doc = ParserUtils.connectGetUrl(ParserUtils.getUri(pairs.getKey().toString()).toASCIIString());
      if (doc == null || !validateRioShow(doc))
        break;
      // getting geospatial information
      Element divAddress = doc.select("div[rel*=v:address]").first();
      if (divAddress != null) {
        PersistentPoint pPoint = new PersistentPoint();
        pPoint.setAddress(new Utf8(divAddress.text()));
        if (doc.select("div[rel*=v:geo]") != null) {
          System.out.println();
          double lat = Double.parseDouble(doc.select("div[rel*=v:geo]").select("span[property=v:latitude]").first().attr("content"));
          double lng = Double.parseDouble(doc.select("div[rel*=v:geo]").select("span[property=v:longitude]").first().attr("content"));
          getLogger().debug(pEntity.getName().toString() + " Longitude found: " + lng);
          getLogger().debug(pEntity.getName().toString() + " Latitude found: " + lat);
          pPoint.addToCoordinates(lng);
          pPoint.addToCoordinates(lat);
          pPoint.setAccuracy(EylloLocation.GEOCODER_VERIF_ACC_HIGH);
        }
        pEntity.setPersistentpoint(pPoint);
      }
    }
    // adding homepage
    Element homePage = doc.select("ul[id*=atribVirtualHeader]").first();
    if (homePage != null) {
      if (homePage.children().size() >= 1) {
        EylloLink eyLink = ParserUtils.detectUrl(homePage.children().get(0).select("a").first());
        if (eyLink != null) {
          pEntity.setHomepage(new Utf8(eyLink.getLinkHref()));
          pEntity.putToSameAs(new Utf8(eyLink.getLinkHref()), new Utf8(eyLink.getLinkText()));
        }
      }// END-IF CHILDREN SIZE
    }
    // adding telephones
    Elements telephones = doc.select("li[id*=atributosBaseEstabelecimento_liTelefone]");
    for (Element phone : telephones) {
      pEntity.addToTelephones(new Utf8(phone.text()));
    }
    // adding schedules
    Elements schedules = doc.select("ul[class=atibPrimariosHorario]");
    for (Element schedule : schedules) {
      pEntity.setSchedule(new Utf8(schedule.text()));
    }
    // adding services
    if (doc.select("ul[id=atributosBaseEstabelecimento_atribSecundariosHeader]").first() != null) {
      Elements services = doc.select("ul[id=atributosBaseEstabelecimento_atribSecundariosHeader]").first().children();
      for (Element service : services) {
        pEntity.addToServices(new Utf8(service.text()));
      }
    }
    // adding label
    Element label = doc.select("li[id*=atributosBaseEstabelecimento_liGenero]").first();
    if (label != null)
      pEntity.setLabel(new Utf8(this.getEntityType() + label.text()));
    // add scenarioId
    pEntity.addToScenarioId(this.getScenarioId());
    //System.out.println(pEntity);
  }
}
