package com.eyllo.paprika.html.parser;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.avro.util.Utf8;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eyllo.paprika.entity.elements.EylloLink;
import com.eyllo.paprika.entity.elements.EylloLocation;
import com.eyllo.paprika.entity.elements.PersistentEntity;
import com.eyllo.paprika.entity.elements.PersistentPoint;

public class PaginasAmarillasParser {

  public static final Object NAME = "paginasAmarillas";

  private List<PersistentEntity> pEntities;
  //http://www.paginasamarillas.com.pe/resultVertical.do?keyword=hoteles&status=P&fromBox=searchBox&seed=67436&stateId=arequipa&cityId=&suburbId=&verticalId=2#
  
  private static String DEFAULT_PA_URL = "http://www.paginasamarillas.com.pe/";
  private static String NUM_PARAM_STR = "NUM_PARAM";
  private static String PLACE_PARAM_STR = "PLACE_PARAM";
  
  //private static String DEFAULT_PA_SEARCH_URL = "/Users/renatomarroquin/Documents/workspace/workspaceEyllo/Eyllo-IR/res/paginasAmarillas/HotelesArequipa.html";//"http://www.paginasamarillas.com.pe/s/hoteles/30/50";
  private static String DEFAULT_PA_SEARCH_URL = "http://www.paginasamarillas.com.pe/s/PLACE_PARAM/"+ NUM_PARAM_STR+"/50";
  private static String places[] = {"restaurantes-+carnes+y+parrilladas"};//, "hoteles"};
//  private static String DEFAULT_PA_ENT_URL = "/Users/renatomarroquin/Documents/workspace/workspaceEyllo/Eyllo-IR/res/paginasAmarillas/CasonaPlazaHotel.html";
  
  private static Integer DEFAULT_SCENARIOID = 5;
  
  /**
   * Logger to help us write write info/debug/error messages
   */
  private static Logger LOGGER = LoggerFactory.getLogger(PaginasAmarillasParser.class);
  
  /**
   * Default constructor
   */
  public PaginasAmarillasParser(){
    this.pEntities = new ArrayList<PersistentEntity>();
  }

  /**
   * Returns the number of entities retrieved
   * @return
   */
  public int totalEntities(){
    return this.pEntities.size();
  }

  /**
   * Gets entities from an specific URL
   * @return
   */
  public List<PersistentEntity> getEntities(){
    for (String place : places){
      LOGGER.debug("Parsing Entities from: " + place);
      String url = DEFAULT_PA_SEARCH_URL.replaceAll(PLACE_PARAM_STR, place);
      int iCnt = 0;
      while (verifySearchUrl(url.replace(NUM_PARAM_STR, String.valueOf(iCnt)))){
        iCnt+=1;
        System.out.println("Getting: "+ url.replace(NUM_PARAM_STR, String.valueOf(iCnt)));
        this.parseSearchResults(url.replace(NUM_PARAM_STR, String.valueOf(iCnt)));
        //break;
      }
      LOGGER.info("Hubo # entidades : " + this.totalEntities());
    }
    LOGGER.info("Complete information");
    this.completeEntityInfo();
    return this.pEntities;
  }

  /**
   * Validates if a specific URL does not contain any error while querying it
   * @param pUrl
   * @return
   */
  public boolean verifySearchUrl(String pUrl){
    LOGGER.info("Verifying: " + pUrl);
    boolean flgValidated = true;
    Document doc = null;
    doc = ParseUtils.connectGetUrl(ParseUtils.getUri(pUrl).toASCIIString());
    if (doc.html().indexOf("Error!") > 0 ||
        doc.html().indexOf("Problemas al realizar su solicitud. Intente nuevamente por favor.") > 0)
      flgValidated = false;
    return flgValidated;
  }

  /**
   * Completes entity's information
   */
  public void completeEntityInfo(){
    if (this.pEntities != null & this.pEntities.size() >0)
      for (PersistentEntity ent : this.pEntities){
        this.parseIndividualEnt(ent);
      }
    else
      LOGGER.error("Entities not found");
  }

  public void parseIndividualEnt(PersistentEntity pEntity){
    Document doc = null;
    Iterator<Entry<Utf8, Utf8>> it = pEntity.getSameAs().entrySet().iterator();
    while (it.hasNext()) {
      Map.Entry<Utf8, Utf8> pairs = (Map.Entry<Utf8, Utf8>)it.next();
      
      // Reading individual URLs
      doc = ParseUtils.connectGetUrl(ParseUtils.getUri(pairs.getKey().toString()).toASCIIString());
      LOGGER.debug("Parsing entity from: " + ParseUtils.getUri(pairs.getKey().toString()).toASCIIString());
      doc.setBaseUri(DEFAULT_PA_URL);
      Elements extraInfos = doc.select("div.acerca");
      
      // it will take all the acercas
      for (Element extra : extraInfos){
        if (extra.select("div.descripcion") != null
            && extra.select("div.descripcion").first() != null
            && extra.select("div.descripcion").first().children() != null){
          Elements datums = extra.select("div.descripcion").first().children();
          for (Element datum : datums){
            String datumText = datum.text().toLowerCase();
            if (datumText.contains("actividad:"))
              pEntity.setIndustry(new Utf8(ParseUtils.toCamelCase(datumText.replace("actividad:", ""))));
            else if (datumText.contains("formas de pago:"))
              pEntity.addToExtraInfo(new Utf8(ParseUtils.toCamelCase(datumText)));
            else if (datumText.contains("tipo de alojamiento:"))
              pEntity.setLabel(new Utf8(ParseUtils.toCamelCase(datumText.replace("tipo de alojamiento:", ""))));
            else if (datumText.contains("sucursal")){
              String locStrings[] = datumText.substring(datumText.indexOf(":") + 1, datumText.length()).split("tlf.");
              if (locStrings.length == 2){
                pEntity.getPersistentpoint().setAddress(new Utf8(ParseUtils.toCamelCase(locStrings[0])));
                pEntity.addToTelephones(new Utf8(ParseUtils.toCamelCase(locStrings[1])));
              }
            }
            else if (datumText.contains("sitio web:")){
              Utf8 hPage = new Utf8(datumText.replace("sitio web:", ""));
              pEntity.setHomepage(hPage);
              pEntity.putToSameAs(hPage, hPage);
            }
            else if (datumText.contains("areas comunes:"))
              pEntity.addToExtraInfo(new Utf8(ParseUtils.toCamelCase(datumText)));
            else if (datumText.contains("formas de pago:"))
              pEntity.addToExtraInfo(new Utf8(ParseUtils.toCamelCase(datumText)));
            else if (datumText.contains("servicios generales:")){
              String serStrings[] = datumText.split(",");
              for (String serString : serStrings)
                pEntity.addToServices(new Utf8(ParseUtils.toCamelCase(serString)));
            }
          }
        }
      }
         
         int javaScript = doc.select("script").html().indexOf("LatLng(");
         if (javaScript > 0){
           String geoString = doc.select("script").html().substring(javaScript, javaScript + 50).replace("LatLng(", "");
           int poss = geoString.indexOf("),");
           geoString = geoString.replace(geoString.substring(poss, geoString.length()),"");
           String coordinates[] = geoString.trim().split(",");
           // longitude
           pEntity.getPersistentpoint().addToCoordinates(Double.parseDouble(coordinates[1]));
           // latitude
           pEntity.getPersistentpoint().addToCoordinates(Double.parseDouble(coordinates[0]));
           // accuracy
           pEntity.getPersistentpoint().setAccuracy(EylloLocation.GEOCODER_VERIF_ACC_HIGH);
         }
    }
  }

  /**
   * Parse search results from a search result site
   * @param pUrl
   */
  public void parseSearchResults(String pUrl){
    LOGGER.debug("Started parsing: " + pUrl);
    //File input = new File(pUrl);
    Document doc = null;
    
    //doc = Jsoup.parse(input, ParseUtils.UTF8_ENCODING, pUrl);
    doc = ParseUtils.connectGetUrl(ParseUtils.getUri(pUrl).toASCIIString());
    doc.setBaseUri(DEFAULT_PA_URL);
    
    Elements results = doc.select("div[class*=resultados]").select("div[class*=posicion]");
    for (Element res : results){
      PersistentEntity pEnt = new PersistentEntity();
      pEnt.addToScenarioId(PaginasAmarillasParser.DEFAULT_SCENARIOID);
      pEnt.setLogo(getLogo(res.select("div[class*=logo-bus]").first()));
      Element entText = res.select("div[class*=texto-bus]").first();
      
      // setting name and alternative url
      EylloLink eylloLink = ParseUtils.detectUrl(entText.select("h2").select("a").first());
      if (eylloLink != null){
        pEnt.setName(new Utf8(eylloLink.getLinkText()));
        pEnt.putToSameAs(new Utf8(DEFAULT_PA_URL + eylloLink.getLinkHref()),
                          new Utf8(eylloLink.getLink().toString()));
      }
      
      // setting slogan if found as a comment of DEFAULT_PA_URL as its maker
      String paEntSlogan = entText.select("p").first().ownText();
      if (paEntSlogan.length() > 0)
        pEnt.putToComment(new Utf8(DEFAULT_PA_URL), new Utf8(paEntSlogan));
      
      Elements extraElements = entText.select("p").select("a");
      for (int iCnt = 0; iCnt < extraElements.size(); iCnt ++){
        switch (iCnt){
          case 0:
            pEnt.setSubject(new Utf8(extraElements.get(iCnt).ownText()));
            //System.out.println(extraElements.get(iCnt).ownText());
            break;
          case 1:
            PersistentPoint tempPoint = new PersistentPoint();
            tempPoint.setAddress(new Utf8(extraElements.get(iCnt).ownText()));
            pEnt.setPersistentpoint(tempPoint);
            //System.out.println(extraElements.get(iCnt).ownText());
            break;
          default:
              LOGGER.debug("Extra elements found but not considered: " + extraElements.get(iCnt));
        }// END-SWITCH
      }// END-FOR EXTRA_ELEMS
      this.pEntities.add(pEnt);
    }// END-FOR RESULTS
    //System.out.println(results.size());
    //ParseUtils.printPersistentEntities(pEntities);
    LOGGER.debug("Finished parsing: " + pUrl);
  }

  /**
   * Gets the logo information from an image element
   * @param pLogo
   * @return
   */
  public Utf8 getLogo(Element pLogo){
    Utf8 lPath = new Utf8("");
    if (pLogo != null){
      String logoPath = pLogo.select("img").attr("src");
      if (logoPath != null & !logoPath.equals(""))
        lPath = new Utf8(logoPath.length()>0?DEFAULT_PA_URL + logoPath:logoPath);
    }
    return lPath;
  }

  /**
   * Main function to help us testing specific parser
   * @param args
   */
  public static void main(String[] args) {
    PaginasAmarillasParser pa = new PaginasAmarillasParser();
    //pa.parseSearchResults(DEFAULT_PA_SEARCH_URL);
    //pa.getEntities();
    ParseUtils.writeJsonFile(pa.getEntities(), "/Users/renatomarroquin/Documents/workspace/workspaceEyllo/Eyllo-IR/res/paginasAmarillas.json");
  }

}