/**
 * 
 */
package com.eyllo.paprika.html.parser;

//import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eyllo.paprika.entity.Entity;
import com.eyllo.paprika.entity.EntityUtils;
import com.eyllo.paprika.entity.elements.EylloLink;
import com.eyllo.paprika.entity.elements.EylloLocation;

/**
 * Extracts entity information from a VejaRio magazine - http://vejario.abril.com.br/
 * @author renatomarroquin
 */
public class VejaRioParser {

  private List<Entity> entities;

  /**
   * Default encoding for reading portuguese pages
   */
  //private static String DEFAULT_ENCODING = "UTF-8";//"ISO-8859-1"
  
  public static String INFO_SEP = " - ";
  private static String DEFAULT_VJR_URL = "http://vejario.abril.com.br/";
  private static String DEFAULT_VJR_SEARCH_URL = "http://vejario.abril.com.br/listagem-do-guia.php?guia_id=1&guia_bairros=Ipanema&guia_especialidades=&guia_nome=";
  // http://vejario.abril.com.br/listagem-do-guia.php?guia_id=1&guia_bairros=Ipanema&guia_especialidades=&guia_nome=
  // http://vejasp.abril.com.br/estabelecimento/busca?per_page=1000&page=40&q=&fq=Restaurantes&bairro=&nome=&preco_maximo=&_=
  private static String DEFAULT_VJR_DOC = "/Users/renatomarroquin/Documents/workspace/workspaceEyllo/Eyllo-IR/res/vejario/VejaRioIpanema.html";
  private static Integer DEFAULT_SCENARIOID = 6;

  /**
   * Logger to help us write write info/debug/error messages
   */
  private static Logger LOGGER = LoggerFactory.getLogger(VejaRioParser.class);
  
  /**
   * Default constructor
   */
  public VejaRioParser(){
    this.entities = new ArrayList<Entity>();
  }
  
  /**
   * @param args
   */
  public static void main(String[] args) {
    VejaRioParser vjrParser = new VejaRioParser();
    vjrParser.parseSearchResults(DEFAULT_VJR_SEARCH_URL);
    vjrParser.completeEntityInfo();
    //ParseUtils.writeJsonFile(vjrParser.entities, DEFAULT_JSON_OUTPUT + DEFAULT_JSON_FILE);
    //ParseUtils.generateSeedFile("");
    //ParseUtils.persistEntities(this.entities);
  }

  /**
   * Gets entities from an specific URL
   * @return
   */
  public static List<Entity> getEntities(){
      VejaRioParser vjrParser = new VejaRioParser();
      vjrParser.parseSearchResults(DEFAULT_VJR_SEARCH_URL);
      vjrParser.completeEntityInfo();
      return vjrParser.entities;
  }
  
  /**
   * Parses individual sites to obtain extra information
   */
  public void completeEntityInfo(){
      if (this.entities != null & this.entities.size() >0)
        for (Entity ent : this.entities){
          this.parseIndividualEnt(ent);
        }
      else
        LOGGER.error("Entities not found");
    }
  
  /**
   * Parsing individual sameAs links from individual entities.
   * @param pEntity
   */
  @SuppressWarnings("unchecked")
  public void parseIndividualEnt(Entity pEntity){
    Document doc = null;
    List<EylloLink> links = (List<EylloLink>)pEntity.getProperties(EntityUtils.SAME_AS);

    for (EylloLink link : links){
      // Reading individual URLs
      URI uri = null;
      try {
        URL url = new URL(link.getLinkHref());
        String nullFragment = null;
        uri = new URI(url.getProtocol(), url.getHost(), url.getPath(), url.getQuery(), nullFragment);
      } catch (URISyntaxException e) {
        LOGGER.error("Error while trying to read the following URL: " + link.getLinkHref());
        e.printStackTrace();
      } catch (MalformedURLException e) {
        LOGGER.error("Error while trying to read the following URL: " + link.getLinkHref());
        e.printStackTrace();
      }
      doc = ParseUtils.connectGetUrl(uri.toASCIIString());

      // Parsing individual sites
      if (doc != null){
        try{
          Element guiaText = doc.select("div.text.guia").first();
          pEntity.setProperties(EntityUtils.SUBJECT, doc.select("h1").first().ownText());

          // Getting the information within the entity's site
          Elements infoBlocks = guiaText.children();
          for ( int iCnt = 0; iCnt < infoBlocks.size(); iCnt ++){
            Element infoBlock = infoBlocks.get(iCnt);
            switch(iCnt){
              case 0:
                EylloLink homePage = ParseUtils.detectUrl(infoBlock.select("p[id=g_site]").select("a").first());
                if (homePage != null)
                  pEntity.setProperties(EntityUtils.HOME_PAGE, homePage.getLinkHref());
                break;
              case 1:
                pEntity.setProperties(EntityUtils.ABSTRACT, infoBlock.select("p").first().ownText());
                break;
              case 2:
                extractExtraInfo(infoBlock.select("p"), pEntity);
                break;
              default:
                LOGGER.info("New element detected inside " + uri.toASCIIString());
                LOGGER.info(infoBlock.toString());
            }// END-OUTER-SWITCH
          }// END-FOR
        }catch (NullPointerException e){
          //System.out.println(doc.toString());
          System.out.println(uri.toASCIIString());
          e.printStackTrace();
        }
        
      }// END-IF
    }
  }

  /**
   * Extracts the extra information from individual entities from VejaRio sites
   * @param extraInfos
   * @param pEntity
   */
  public void extractExtraInfo(Elements extraInfos, Entity pEntity){
    LOGGER.info("Extracting extra information about entities" + pEntity.getProperties(EntityUtils.NAME));
    EylloLocation entLoc = new EylloLocation();
    for (int iCntInf = 0; iCntInf < extraInfos.size(); iCntInf++){
          if (extraInfos.get(iCntInf).text().toLowerCase().contains("endereo")){
            entLoc.setAddress(extraInfos.get(iCntInf).ownText());
            pEntity.addLocations(entLoc);
          }
          else if (extraInfos.get(iCntInf).text().toLowerCase().contains("bairro"))
            entLoc.setAddress(entLoc.getAddress() + VejaRioParser.INFO_SEP + extraInfos.get(iCntInf).ownText());
          else if (extraInfos.get(iCntInf).text().toLowerCase().contains("cep")){
            //System.out.println("CEP");
            entLoc.setAddress(entLoc.getAddress() + VejaRioParser.INFO_SEP + extraInfos.get(iCntInf).text());
          }
          else if (extraInfos.get(iCntInf).text().toLowerCase().contains("telefone"))
            pEntity.setProperties(EntityUtils.PHONES, extraInfos.get(iCntInf).ownText());
          else if (extraInfos.get(iCntInf).text().toLowerCase().contains("hor‡rio"))
            pEntity.setProperties(EntityUtils.SCHEDULE, extraInfos.get(iCntInf).ownText());
          else if (extraInfos.get(iCntInf).text().toLowerCase().contains("servios")){
            StringBuilder services = new StringBuilder();
            for (Element el : extraInfos.get(iCntInf).children().select("a"))
              services.append(el.text()).append(VejaRioParser.INFO_SEP);
            pEntity.setProperties(EntityUtils.SERVICES, services.toString());
          }
          else if (extraInfos.get(iCntInf).text().toLowerCase().contains("cart›es de crŽdito")){
            StringBuilder creditCardInfo = new StringBuilder();
            creditCardInfo.append(extraInfos.get(iCntInf).text());
            for (Element el : extraInfos.get(iCntInf).children().select("img"))
              creditCardInfo.append(el.attr("title")).append(VejaRioParser.INFO_SEP);
            pEntity.setProperties(EntityUtils.EXTRA_INFO, creditCardInfo.toString());
          }
          else 
            pEntity.setProperties(EntityUtils.EXTRA_INFO, extraInfos.get(iCntInf).text());
    }// END-FOR
    LOGGER.info("Finished extracting extra information about entities");
  }

  /**
   * Parse search results from a search result site
   * @param pUrl
   */
  public void parseSearchResults(String pUrl){
    LOGGER.info("Starting to parse data from " + pUrl);
    Document doc = null;
    //File input;

    try {
      //input = new File(DEFAULT_VJR_DOC);
      doc = Jsoup.parse(new URL(pUrl), 30000); //doc = Jsoup.connect(DEFAULT_TL_URL.format(URLEncoder.encode(DEFAULT_TL_URL, "UTF-8"))).get();
      //doc = Jsoup.parse(input, DEFAULT_ENCODING, pUrl);
    } catch (MalformedURLException e) {
      LOGGER.error("Error while trying to parse document " + DEFAULT_VJR_DOC);
      e.printStackTrace();
    } catch (IOException e) {
      LOGGER.error("Error while trying to parse document " + DEFAULT_VJR_DOC);
      e.printStackTrace();
    }

    Element div = doc.select("div[id=bsc_resultado]").first();
    Elements resBlocks = div.children();
    //int xxyy = 0;
    for (Element resBlock : resBlocks){
      Entity tmpEntity = new Entity();
      // setting default scenarioID for VejaRio magazine
      tmpEntity.setProperties(EntityUtils.SCENARIO_ID, DEFAULT_SCENARIOID);
      // getting classification
      tmpEntity.setProperties(EntityUtils.SUBJECT, resBlock.select("h3").text());
      // getting name and sameAs link
      EylloLink sasLink = ParseUtils.detectUrl(resBlock.select("h2").first().children().first());
      if (sasLink != null){
        tmpEntity.setProperties(EntityUtils.NAME, sasLink.getLinkText());
        sasLink.setLinkHref(VejaRioParser.DEFAULT_VJR_URL + sasLink.getLinkHref());
        tmpEntity.addLink(sasLink);
      }
      this.entities.add(tmpEntity);
      
      //if (++xxyy == 10)
      //  break;
    }

    LOGGER.info("Finished collecting initial entities.");
    //ParseUtils.printEntities(this.entities);
    //LOGGER.info(String.valueOf("tuvimos " + this.entities.size()));
  }
}
