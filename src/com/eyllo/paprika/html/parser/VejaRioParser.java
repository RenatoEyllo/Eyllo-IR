/**
 * 
 */
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
import com.eyllo.paprika.entity.elements.PersistentEntity;
import com.eyllo.paprika.entity.elements.PersistentPoint;

/**
 * Extracts entity information from a VejaRio magazine - http://vejario.abril.com.br/
 * @author renatomarroquin
 */
public class VejaRioParser {

  /**
   * List of PersistentEntities that will be fetched
   */
  private List<PersistentEntity> pEntities;

  private static String DEFAULT_VJR_URL = "http://vejario.abril.com.br/";
  private static String DEFAULT_VJR_SEARCH_URL = "http://vejario.abril.com.br/listagem-do-guia.php?guia_id=1&guia_bairros=Ipanema&guia_especialidades=&guia_nome=";
  // http://vejario.abril.com.br/listagem-do-guia.php?guia_id=1&guia_bairros=Ipanema&guia_especialidades=&guia_nome=
  // http://vejasp.abril.com.br/estabelecimento/busca?per_page=1000&page=40&q=&fq=Restaurantes&bairro=&nome=&preco_maximo=&_=
  //private static String DEFAULT_VJR_DOC = "/Users/renatomarroquin/Documents/workspace/workspaceEyllo/Eyllo-IR/res/vejario/VejaRioIpanema.html";
  private static Integer DEFAULT_SCENARIOID = 6;
  private static String DEFAULT_CITY = "Rio de Janeiro-RJ";

  /**
   * VejaRio parser's name
   */
  public static String NAME = "vejario";

  /**
   * Logger to help us write write info/debug/error messages
   */
  private static Logger LOGGER = LoggerFactory.getLogger(VejaRioParser.class);
  
  /**
   * Default constructor
   */
  public VejaRioParser(){
    this.pEntities = new ArrayList<PersistentEntity>();
  }
  
  /**
   * @param args
   */
  public static void main(String[] args) {
    VejaRioParser vjrParser = new VejaRioParser();
    ParseUtils.printPersistentEntities(vjrParser.pEntities);
    vjrParser.parseSearchResults(DEFAULT_VJR_SEARCH_URL);
    ParseUtils.printPersistentEntities(vjrParser.pEntities);
    //vjrParser.completeEntityInfo();
    //ParseUtils.writeJsonFile(vjrParser.entities, DEFAULT_JSON_OUTPUT + DEFAULT_JSON_FILE);
    //ParseUtils.generateSeedFile("");
    //ParseUtils.persistEntities(this.entities);
  }

  /**
   * Gets entities from an specific URL
   * @return
   */
  public List<PersistentEntity> getEntities(){
      //VejaRioParser vjrParser = new VejaRioParser();
     this.parseSearchResults(DEFAULT_VJR_SEARCH_URL);
     this.completeEntityInfo();
     return this.pEntities;
  }
  
  /**
   * Parses individual sites to obtain extra information
   */
  public void completeEntityInfo(){
      if (this.pEntities != null & this.pEntities.size() >0)
        for (PersistentEntity ent : this.pEntities){
          this.parseIndividualEnt(ent);
        }
      else
        LOGGER.error("Entities not found");
    }
  
  /**
   * Parsing individual sameAs links from individual entities.
   * @param pEntity
   */
  public void parseIndividualEnt(PersistentEntity pEntity){
    Document doc = null;
    ///List<EylloLink> links = (List<EylloLink>)pEntity.getProperties(EntityUtils.SAME_AS);
    //Map<Utf8,Utf8> sameAsLinks = pEntity.getSameAs();
    Iterator<Entry<Utf8, Utf8>> it = pEntity.getSameAs().entrySet().iterator();
    while (it.hasNext()) {
        Map.Entry<Utf8, Utf8> pairs = (Map.Entry<Utf8, Utf8>)it.next();
        
        // Reading individual URLs
        doc = ParseUtils.connectGetUrl(ParseUtils.getUri(pairs.getValue().toString()).toASCIIString());

        // Parsing individual sites
        if (doc != null){
          try{
            Element guiaText = doc.select("div.text.guia").first();
          ///pEntity.setProperties(EntityUtils.SUBJECT, doc.select("h1").first().ownText());
            pEntity.setSubject(new Utf8(doc.select("h1").first().ownText()));

            // Getting the information within the entity's site
            Elements infoBlocks = guiaText.children();
            for ( int iCnt = 0; iCnt < infoBlocks.size(); iCnt ++){
              Element infoBlock = infoBlocks.get(iCnt);
              switch(iCnt){
                case 0:
                  EylloLink homePage = ParseUtils.detectUrl(infoBlock.select("p[id=g_site]").select("a").first());
                  if (homePage != null)
                    pEntity.setHomepage(new Utf8(homePage.getLinkHref()));
                    ///pEntity.setProperties(EntityUtils.HOME_PAGE, homePage.getLinkHref());
                  break;
                case 1:
                    pEntity.setDescription(new Utf8(infoBlock.select("p").first().ownText()));
                  ///pEntity.setProperties(EntityUtils.DESCRIPTION, infoBlock.select("p").first().ownText());
                  break;
                case 2:
                  extractExtraInfo(infoBlock.select("p"), pEntity);
                  break;
                default:
                  LOGGER.info("New element detected inside " + pairs.getValue().toString());
                  LOGGER.info(infoBlock.toString());
              }// END-OUTER-SWITCH
            }// END-FOR
          }catch (NullPointerException e){
            LOGGER.error("Error while parsing URL: " + pairs.getValue().toString());
            e.printStackTrace();
          }
        }// END-IF
    }// END-WHILE
  }

  /**
   * Extracts the extra information from individual entities from VejaRio sites
   * @param extraInfos
   * @param pEntity
   */
  public void extractExtraInfo(Elements extraInfos, PersistentEntity pEntity){
    LOGGER.info("Extracting extra information about entities " + pEntity.getName().toString());
    ///EylloLocation entLoc = new EylloLocation();
    PersistentPoint entLoc = new PersistentPoint();
    for (int iCntInf = 0; iCntInf < extraInfos.size(); iCntInf++){
          if (extraInfos.get(iCntInf).text().toLowerCase().contains("endereço")){
            ///entLoc.setAddress(extraInfos.get(iCntInf).ownText());
            entLoc.setAddress(new Utf8(extraInfos.get(iCntInf).ownText()));
            ///pEntity.addLocations(entLoc);
            pEntity.setPersistentpoint(entLoc);
          }
          else if (extraInfos.get(iCntInf).text().toLowerCase().contains("bairro")){
            // adding address
            ///entLoc.setAddress(entLoc.getAddress() + VejaRioParser.INFO_SEP + extraInfos.get(iCntInf).ownText());
              entLoc.setAddress(new Utf8(entLoc.getAddress().toString() + ConstantsParser.INFO_SEP + extraInfos.get(iCntInf).ownText()));
            // adding city
            ///entLoc.setAddress(entLoc.getAddress() + VejaRioParser.INFO_SEP + VejaRioParser.DEFAULT_CITY);
              entLoc.setAddress(new Utf8(entLoc.getAddress().toString() + ConstantsParser.INFO_SEP + VejaRioParser.DEFAULT_CITY));
          }
          else if (extraInfos.get(iCntInf).text().toLowerCase().contains("cep")){
            //System.out.println("CEP");
            ///entLoc.setAddress(entLoc.getAddress() + VejaRioParser.INFO_SEP + extraInfos.get(iCntInf).text());
              entLoc.setAddress(new Utf8(entLoc.getAddress().toString() + ConstantsParser.INFO_SEP + extraInfos.get(iCntInf).text()));
          }
          else if (extraInfos.get(iCntInf).text().toLowerCase().contains("telefone"))
            ///pEntity.setProperties(EntityUtils.PHONES, extraInfos.get(iCntInf).ownText());
              pEntity.addToTelephones(new Utf8(extraInfos.get(iCntInf).ownText()));
          else if (extraInfos.get(iCntInf).text().toLowerCase().contains("horário"))
            ///pEntity.setProperties(EntityUtils.SCHEDULE, extraInfos.get(iCntInf).ownText());
              pEntity.setSchedule(new Utf8(extraInfos.get(iCntInf).ownText()));
          else if (extraInfos.get(iCntInf).text().toLowerCase().contains("serviços")){
            ///StringBuilder services = new StringBuilder();
            for (Element el : extraInfos.get(iCntInf).children().select("a"))
                pEntity.addToServices(new Utf8(el.text()));
              ///services.append(el.text()).append(VejaRioParser.INFO_SEP);
            ///pEntity.setProperties(EntityUtils.SERVICES, services.toString());
          }
          else if (extraInfos.get(iCntInf).text().toLowerCase().contains("cartões de crédito")){
            StringBuilder creditCardInfo = new StringBuilder();
            creditCardInfo.append(extraInfos.get(iCntInf).text());
            for (Element el : extraInfos.get(iCntInf).children().select("img"))
                pEntity.addToExtraInfo(new Utf8(el.attr("title")));
              ///creditCardInfo.append(el.attr("title")).append(VejaRioParser.INFO_SEP);
            ///pEntity.setProperties(EntityUtils.EXTRA_INFO, creditCardInfo.toString());
            
          }
          else 
            ///pEntity.setProperties(EntityUtils.EXTRA_INFO, extraInfos.get(iCntInf).text());
              pEntity.addToExtraInfo(new Utf8(extraInfos.get(iCntInf).text()));
    }// END-FOR
    LOGGER.info("Finished extracting extra information about entities");
  }

  /**
   * Parse search results from a search result site
   * @param pUrl
   */
  public void parseSearchResults(String pUrl){
    LOGGER.info("Starting to parse data from " + pUrl);
    Document doc = ParseUtils.connectGetUrl(pUrl);

    Element div = doc.select("div[id=bsc_resultado]").first();
    Elements resBlocks = div.children();
    int xxyy = 0;
    for (Element resBlock : resBlocks){
      //Entity tmpEntity = new Entity();
      PersistentEntity tmpPerEntity = new PersistentEntity();
      // setting default scenarioID for VejaRio magazine
      tmpPerEntity.addToScenarioId(DEFAULT_SCENARIOID);
    ///tmpEntity.setProperties(EntityUtils.SCENARIO_ID, DEFAULT_SCENARIOID);
      // getting classification
      tmpPerEntity.setSubject(new Utf8(resBlock.select("h3").text()));
    ///tmpEntity.setProperties(EntityUtils.SUBJECT, resBlock.select("h3").text());
      // getting name and sameAs link
      
      EylloLink sasLink = ParseUtils.detectUrl(resBlock.select("h2").first().children().first());
      if (sasLink != null){
        ///tmpEntity.setProperties(EntityUtils.NAME, sasLink.getLinkText());
        tmpPerEntity.setName(new Utf8(sasLink.getLinkText()));
        ///sasLink.setLinkHref(VejaRioParser.DEFAULT_VJR_URL + sasLink.getLinkHref());
        ///tmpEntity.addLink(sasLink);
        tmpPerEntity.putToSameAs(new Utf8(sasLink.getLinkText()), new Utf8(VejaRioParser.DEFAULT_VJR_URL + sasLink.getLinkHref()));
      }
    ///this.entities.add(tmpEntity);
      this.pEntities.add(tmpPerEntity);
      if (++xxyy == 100)
        break;
    }/**/

    LOGGER.info("Finished collecting initial entities.");
    //ParseUtils.printEntities(this.entities);
    //LOGGER.info(String.valueOf("tuvimos " + this.entities.size()));
  }
}
