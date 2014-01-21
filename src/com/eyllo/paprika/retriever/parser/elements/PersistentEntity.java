/**
 *Licensed to the Apache Software Foundation (ASF) under one
 *or more contributor license agreements.  See the NOTICE file
 *distributed with this work for additional information
 *regarding copyright ownership.  The ASF licenses this file
 *to you under the Apache License, Version 2.0 (the"
 *License"); you may not use this file except in compliance
 *with the License.  You may obtain a copy of the License at
 *
  * http://www.apache.org/licenses/LICENSE-2.0
 * 
 *Unless required by applicable law or agreed to in writing, software
 *distributed under the License is distributed on an "AS IS" BASIS,
 *WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *See the License for the specific language governing permissions and
 *limitations under the License.
 */

package com.eyllo.paprika.retriever.parser.elements;

import java.nio.ByteBuffer;
import java.util.Date;
import java.util.Map;
import java.util.HashMap;
import java.util.logging.Logger;

import org.apache.avro.Protocol;
import org.apache.avro.Schema;
import org.apache.avro.AvroRuntimeException;
import org.apache.avro.Protocol;
import org.apache.avro.util.Utf8;
import org.apache.avro.ipc.AvroRemoteException;
import org.apache.avro.generic.GenericArray;
import org.apache.avro.specific.FixedSize;
import org.apache.avro.specific.SpecificExceptionBase;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.avro.specific.SpecificRecord;
import org.apache.avro.specific.SpecificFixed;
import org.apache.gora.persistency.StateManager;
import org.apache.gora.persistency.impl.PersistentBase;
import org.apache.gora.persistency.impl.StateManagerImpl;
import org.apache.gora.persistency.StatefulHashMap;
import org.apache.gora.persistency.ListGenericArray;

import com.eyllo.paprika.entity.EntityUtils;
import com.eyllo.paprika.retriever.parser.ParserConstants;
import com.eyllo.paprika.retriever.parser.ParserUtils;
import com.eyllo.paprika.retriever.parser.elements.PersistentPoint;

@SuppressWarnings("all")
public class PersistentEntity extends PersistentBase {
  public static final Schema _SCHEMA = Schema.parse("{\"type\":\"record\",\"name\":\"PersistentEntity\",\"namespace\":\"com.eyllo.paprika.entity.generated\",\"fields\":[{\"name\":\"description\",\"type\":\"string\"},{\"name\":\"assets\",\"type\":\"string\"},{\"name\":\"comment\",\"type\":{\"type\":\"map\",\"values\":\"string\"}},{\"name\":\"depiction\",\"type\":\"string\"},{\"name\":\"extraInfo\",\"type\":{\"type\":\"array\",\"items\":\"string\"}},{\"name\":\"foundation\",\"type\":\"string\"},{\"name\":\"foundationPlace\",\"type\":\"string\"},{\"name\":\"foundingYear\",\"type\":\"string\"},{\"name\":\"hasPhotoCollection\",\"type\":\"boolean\"},{\"name\":\"industry\",\"type\":\"string\"},{\"name\":\"label\",\"type\":\"string\"},{\"name\":\"homepage\",\"type\":\"string\"},{\"name\":\"name\",\"type\":\"string\"},{\"name\":\"scenarioId\",\"type\":{\"type\":\"array\",\"items\":\"int\"}},{\"name\":\"services\",\"type\":{\"type\":\"array\",\"items\":\"string\"}},{\"name\":\"persistentpoint\",\"type\":{\"type\":\"record\",\"name\":\"PersistentPoint\",\"fields\":[{\"name\":\"accuracy\",\"type\":\"double\"},{\"name\":\"address\",\"type\":\"string\"},{\"name\":\"coordinates\",\"type\":{\"type\":\"array\",\"items\":\"double\"}}]}},{\"name\":\"logo\",\"type\":\"string\"},{\"name\":\"netIncome\",\"type\":\"double\"},{\"name\":\"numberOfEmployees\",\"type\":\"double\"},{\"name\":\"telephones\",\"type\":{\"type\":\"array\",\"items\":\"string\"}},{\"name\":\"products\",\"type\":{\"type\":\"array\",\"items\":\"string\"}},{\"name\":\"revenue\",\"type\":\"double\"},{\"name\":\"sameAs\",\"type\":{\"type\":\"map\",\"values\":\"string\"}},{\"name\":\"schedule\",\"type\":\"string\"},{\"name\":\"subject\",\"type\":\"string\"},{\"name\":\"thumbnail\",\"type\":\"string\"},{\"name\":\"type\",\"type\":\"string\"},{\"name\":\"wikiPageExternalLink\",\"type\":\"string\"},{\"name\":\"wikiPageUsesTemplate\",\"type\":\"string\"}]}");
  public static enum Field {
    DESCRIPTION(0,"description"),
    ASSETS(1,"assets"),
    COMMENT(2,"comment"),
    DEPICTION(3,"depiction"),
    EXTRA_INFO(4,"extraInfo"),
    FOUNDATION(5,"foundation"),
    FOUNDATION_PLACE(6,"foundationPlace"),
    FOUNDING_YEAR(7,"foundingYear"),
    HAS_PHOTO_COLLECTION(8,"hasPhotoCollection"),
    INDUSTRY(9,"industry"),
    LABEL(10,"label"),
    HOMEPAGE(11,"homepage"),
    NAME(12,"name"),
    SCENARIO_ID(13,"scenarioId"),
    SERVICES(14,"services"),
    PERSISTENTPOINT(15,"persistentpoint"),
    LOGO(16,"logo"),
    NET_INCOME(17,"netIncome"),
    NUMBER_OF_EMPLOYEES(18,"numberOfEmployees"),
    TELEPHONES(19,"telephones"),
    PRODUCTS(20,"products"),
    REVENUE(21,"revenue"),
    SAME_AS(22,"sameAs"),
    SCHEDULE(23,"schedule"),
    SUBJECT(24,"subject"),
    THUMBNAIL(25,"thumbnail"),
    TYPE(26,"type"),
    WIKI_PAGE_EXTERNAL_LINK(27,"wikiPageExternalLink"),
    WIKI_PAGE_USES_TEMPLATE(28,"wikiPageUsesTemplate"),
    ;
    private int index;
    private String name;
    Field(int index, String name) {this.index=index;this.name=name;}
    public int getIndex() {return index;}
    public String getName() {return name;}
    public String toString() {return name;}
  };
  public static final String[] _ALL_FIELDS = {"description","assets","comment","depiction","extraInfo","foundation","foundationPlace","foundingYear","hasPhotoCollection","industry","label","homepage","name","scenarioId","services","persistentpoint","logo","netIncome","numberOfEmployees","telephones","products","revenue","sameAs","schedule","subject","thumbnail","type","wikiPageExternalLink","wikiPageUsesTemplate",};
  static {
    PersistentBase.registerFields(PersistentEntity.class, _ALL_FIELDS);
  }
  private Utf8 description;
  private Utf8 assets;
  private Map<Utf8,Utf8> comment;
  private Utf8 depiction;
  private GenericArray<Utf8> extraInfo;
  private Utf8 foundation;
  private Utf8 foundationPlace;
  private Utf8 foundingYear;
  private boolean hasPhotoCollection;
  private Utf8 industry;
  private Utf8 label;
  private Utf8 homepage;
  private Utf8 name;
  private GenericArray<Integer> scenarioId;
  private GenericArray<Utf8> services;
  private PersistentPoint persistentpoint;
  private Utf8 logo;
  private double netIncome;
  private double numberOfEmployees;
  private GenericArray<Utf8> telephones;
  private GenericArray<Utf8> products;
  private double revenue;
  private Map<Utf8,Utf8> sameAs;
  private Utf8 schedule;
  private Utf8 subject;
  private Utf8 thumbnail;
  private Utf8 type;
  private Utf8 wikiPageExternalLink;
  private Utf8 wikiPageUsesTemplate;
  public PersistentEntity() {
    this(new StateManagerImpl());
  }
  public PersistentEntity(StateManager stateManager) {
    super(stateManager);
    comment = new StatefulHashMap<Utf8,Utf8>();
    extraInfo = new ListGenericArray<Utf8>(getSchema().getField("extraInfo").schema());
    scenarioId = new ListGenericArray<Integer>(getSchema().getField("scenarioId").schema());
    services = new ListGenericArray<Utf8>(getSchema().getField("services").schema());
    telephones = new ListGenericArray<Utf8>(getSchema().getField("telephones").schema());
    products = new ListGenericArray<Utf8>(getSchema().getField("products").schema());
    sameAs = new StatefulHashMap<Utf8,Utf8>();
  }
  public PersistentEntity newInstance(StateManager stateManager) {
    return new PersistentEntity(stateManager);
  }
  public Schema getSchema() { return _SCHEMA; }
  public Object get(int _field) {
    switch (_field) {
    case 0: return description;
    case 1: return assets;
    case 2: return comment;
    case 3: return depiction;
    case 4: return extraInfo;
    case 5: return foundation;
    case 6: return foundationPlace;
    case 7: return foundingYear;
    case 8: return hasPhotoCollection;
    case 9: return industry;
    case 10: return label;
    case 11: return homepage;
    case 12: return name;
    case 13: return scenarioId;
    case 14: return services;
    case 15: return persistentpoint;
    case 16: return logo;
    case 17: return netIncome;
    case 18: return numberOfEmployees;
    case 19: return telephones;
    case 20: return products;
    case 21: return revenue;
    case 22: return sameAs;
    case 23: return schedule;
    case 24: return subject;
    case 25: return thumbnail;
    case 26: return type;
    case 27: return wikiPageExternalLink;
    case 28: return wikiPageUsesTemplate;
    default: throw new AvroRuntimeException("Bad index");
    }
  }
  @SuppressWarnings(value="unchecked")
  public void put(int _field, Object _value) {
    if(isFieldEqual(_field, _value)) return;
    getStateManager().setDirty(this, _field);
    switch (_field) {
    case 0:description = (Utf8)_value; break;
    case 1:assets = (Utf8)_value; break;
    case 2:comment = (Map<Utf8,Utf8>)_value; break;
    case 3:depiction = (Utf8)_value; break;
    case 4:extraInfo = (GenericArray<Utf8>)_value; break;
    case 5:foundation = (Utf8)_value; break;
    case 6:foundationPlace = (Utf8)_value; break;
    case 7:foundingYear = (Utf8)_value; break;
    case 8:hasPhotoCollection = (Boolean)_value; break;
    case 9:industry = (Utf8)_value; break;
    case 10:label = (Utf8)_value; break;
    case 11:homepage = (Utf8)_value; break;
    case 12:name = (Utf8)_value; break;
    case 13:scenarioId = (GenericArray<Integer>)_value; break;
    case 14:services = (GenericArray<Utf8>)_value; break;
    case 15:persistentpoint = (PersistentPoint)_value; break;
    case 16:logo = (Utf8)_value; break;
    case 17:netIncome = (Double)_value; break;
    case 18:numberOfEmployees = (Double)_value; break;
    case 19:telephones = (GenericArray<Utf8>)_value; break;
    case 20:products = (GenericArray<Utf8>)_value; break;
    case 21:revenue = (Double)_value; break;
    case 22:sameAs = (Map<Utf8,Utf8>)_value; break;
    case 23:schedule = (Utf8)_value; break;
    case 24:subject = (Utf8)_value; break;
    case 25:thumbnail = (Utf8)_value; break;
    case 26:type = (Utf8)_value; break;
    case 27:wikiPageExternalLink = (Utf8)_value; break;
    case 28:wikiPageUsesTemplate = (Utf8)_value; break;
    default: throw new AvroRuntimeException("Bad index");
    }
  }
  public Utf8 getDescription() {
    return (Utf8) get(0);
  }
  public void setDescription(Utf8 value) {
    put(0, value);
  }
  public Utf8 getAssets() {
    return (Utf8) get(1);
  }
  public void setAssets(Utf8 value) {
    put(1, value);
  }
  public Map<Utf8, Utf8> getComment() {
    return (Map<Utf8, Utf8>) get(2);
  }
  public Utf8 getFromComment(Utf8 key) {
    if (comment == null) { return null; }
    return comment.get(key);
  }
  public void putToComment(Utf8 key, Utf8 value) {
    getStateManager().setDirty(this, 2);
    comment.put(key, value);
  }
  public Utf8 removeFromComment(Utf8 key) {
    if (comment == null) { return null; }
    getStateManager().setDirty(this, 2);
    return comment.remove(key);
  }
  public Utf8 getDepiction() {
    return (Utf8) get(3);
  }
  public void setDepiction(Utf8 value) {
    put(3, value);
  }
  public GenericArray<Utf8> getExtraInfo() {
    return (GenericArray<Utf8>) get(4);
  }
  public void addToExtraInfo(Utf8 element) {
    getStateManager().setDirty(this, 4);
    extraInfo.add(element);
  }
  public Utf8 getFoundation() {
    return (Utf8) get(5);
  }
  public void setFoundation(Utf8 value) {
    put(5, value);
  }
  public Utf8 getFoundationPlace() {
    return (Utf8) get(6);
  }
  public void setFoundationPlace(Utf8 value) {
    put(6, value);
  }
  public Utf8 getFoundingYear() {
    return (Utf8) get(7);
  }
  public void setFoundingYear(Utf8 value) {
    put(7, value);
  }
  public boolean getHasPhotoCollection() {
    return (Boolean) get(8);
  }
  public void setHasPhotoCollection(boolean value) {
    put(8, value);
  }
  public Utf8 getIndustry() {
    return (Utf8) get(9);
  }
  public void setIndustry(Utf8 value) {
    put(9, value);
  }
  public Utf8 getLabel() {
    return (Utf8) get(10);
  }
  public void setLabel(Utf8 value) {
    put(10, value);
  }
  public Utf8 getHomepage() {
    return (Utf8) get(11);
  }
  public void setHomepage(Utf8 value) {
    put(11, value);
  }
  public Utf8 getName() {
    return (Utf8) get(12);
  }
  public void setName(Utf8 value) {
    put(12, value);
  }
  public GenericArray<Integer> getScenarioId() {
    return (GenericArray<Integer>) get(13);
  }
  public void addToScenarioId(int element) {
    getStateManager().setDirty(this, 13);
    scenarioId.add(element);
  }
  public GenericArray<Utf8> getServices() {
    return (GenericArray<Utf8>) get(14);
  }
  public void addToServices(Utf8 element) {
    getStateManager().setDirty(this, 14);
    services.add(element);
  }
  public PersistentPoint getPersistentpoint() {
    return (PersistentPoint) get(15);
  }
  public void setPersistentpoint(PersistentPoint value) {
    put(15, value);
  }
  public Utf8 getLogo() {
    return (Utf8) get(16);
  }
  public void setLogo(Utf8 value) {
    put(16, value);
  }
  public double getNetIncome() {
    return (Double) get(17);
  }
  public void setNetIncome(double value) {
    put(17, value);
  }
  public double getNumberOfEmployees() {
    return (Double) get(18);
  }
  public void setNumberOfEmployees(double value) {
    put(18, value);
  }
  public GenericArray<Utf8> getTelephones() {
    return (GenericArray<Utf8>) get(19);
  }
  public void addToTelephones(Utf8 element) {
    getStateManager().setDirty(this, 19);
    telephones.add(element);
  }
  public GenericArray<Utf8> getProducts() {
    return (GenericArray<Utf8>) get(20);
  }
  public void addToProducts(Utf8 element) {
    getStateManager().setDirty(this, 20);
    products.add(element);
  }
  public double getRevenue() {
    return (Double) get(21);
  }
  public void setRevenue(double value) {
    put(21, value);
  }
  public Map<Utf8, Utf8> getSameAs() {
    return (Map<Utf8, Utf8>) get(22);
  }
  public Utf8 getFromSameAs(Utf8 key) {
    if (sameAs == null) { return null; }
    return sameAs.get(key);
  }
  public void putToSameAs(Utf8 key, Utf8 value) {
    getStateManager().setDirty(this, 22);
    sameAs.put(key, value);
  }
  public Utf8 removeFromSameAs(Utf8 key) {
    if (sameAs == null) { return null; }
    getStateManager().setDirty(this, 22);
    return sameAs.remove(key);
  }
  public Utf8 getSchedule() {
    return (Utf8) get(23);
  }
  public void setSchedule(Utf8 value) {
    put(23, value);
  }
  public Utf8 getSubject() {
    return (Utf8) get(24);
  }
  public void setSubject(Utf8 value) {
    put(24, value);
  }
  public Utf8 getThumbnail() {
    return (Utf8) get(25);
  }
  public void setThumbnail(Utf8 value) {
    put(25, value);
  }
  public Utf8 getType() {
    return (Utf8) get(26);
  }
  public void setType(Utf8 value) {
    put(26, value);
  }
  public Utf8 getWikiPageExternalLink() {
    return (Utf8) get(27);
  }
  public void setWikiPageExternalLink(Utf8 value) {
    put(27, value);
  }
  public Utf8 getWikiPageUsesTemplate() {
    return (Utf8) get(28);
  }
  public void setWikiPageUsesTemplate(Utf8 value) {
    put(28, value);
  }
  /**
   * Returns the object in JSON format
   * {"scenarioId":6,
   * "userId":"2",
   * "title":"LojasAmericanas",
   * "text":"Barro Preto",
   * "location":{"longitude":-43.952023,"latitude":-19.921711},
   * "type":"text",
   * "infobox":{"text":"Avenida Augusto de Lima n.�� 1313 - Barro Preto , Belo Horizonte","title":"Barro Preto"}
   * }
   * @return
   */
  public String toJson() {
    return toJson(18);
  }
  // TODO get a better way to get the necessary information
  public String toJson(int pUserId){
    try{
      Map jsonMap = new HashMap();
      StringBuilder strBuilder = new StringBuilder();
      //TODO change the iterator value
      jsonMap.put(EntityUtils.SCENARIO_ID, this.getScenarioId().iterator().next());
      jsonMap.put("userId", pUserId);
      jsonMap.put("title", this.getName()!=null?this.getName().toString():"");
      // extra info
      for (Utf8 eInfo : this.getExtraInfo())
        strBuilder.append(eInfo.toString()).append(ParserConstants.INFO_SEP);
      jsonMap.put("text", strBuilder.toString());
      //location
      Map infoBox = new HashMap();
      Map geoJson = new HashMap();
      ///ArrayList<EylloLocation> locList = (ArrayList<EylloLocation>) this.properties.get(EntityUtils.LOCATION);
      PersistentPoint entLoc = this.getPersistentpoint();
      ///for(EylloLocation entLoc : locList){
        geoJson.put("type", "Point");
        geoJson.put("coordinates", entLoc.getCoordinates());
        geoJson.put("accuracy", entLoc.getAccuracy());
        //jsonMap.put("location", entLoc.getAttribMap());
        String infoBoxStr = this.getDescription()==null?"":this.getDescription().toString();
        infoBoxStr = infoBoxStr + entLoc.getAddress()==null?"":entLoc.getAddress().toString();
        infoBox.put("text", infoBoxStr);
      ///}
      jsonMap.put("text", infoBoxStr);
      jsonMap.put("type", "text");
      //infobox
      infoBox.put(EntityUtils.HOME_PAGE, this.getHomepage()!=null?this.getHomepage().toString():"");
      /// telephones
      if (strBuilder.length() > 1)
        strBuilder.delete(0, strBuilder.length()-1);
      for (Utf8 phone : this.getTelephones())
        strBuilder.append(phone.toString()).append(ParserConstants.INFO_SEP);
      /// services
      if (strBuilder.length() > 1)
        strBuilder.delete(0, strBuilder.length()-1);
      for (Utf8 service : this.getServices())
        strBuilder.append(service.toString()).append(ParserConstants.INFO_SEP);
      infoBox.put(EntityUtils.SCHEDULE, this.getSchedule()!=null?this.getSchedule().toString():"");
      //infoBox.put("extraInfo", strBuilder.toString());
      jsonMap.put("infobox", infoBox);
      jsonMap.put("loc", geoJson);
      return ParserUtils.getJsonObj(jsonMap).toJSONString();
    }catch(Exception e){
      e.printStackTrace();
      return "";
    }
  }

  public Map<String, Object> toMap(){
    Map<String, Object> resMap = new HashMap<String, Object>();
    Map<String, Object> sameAsMap = new HashMap<String, Object>();
    //TODO create job to update twitter user
    resMap.put("twUserId","0");
    resMap.put("description", UtilsElements.toString(this.getDescription()));
    //TODO this should be an array
    resMap.put("assets", UtilsElements.toString(this.getAssets()));
    resMap.put("name", UtilsElements.toString(this.getName()));
    resMap.put("homepage", UtilsElements.toString(this.getHomepage()));
    resMap.put("label", UtilsElements.toString(this.getLabel()));
    resMap.put("logo", UtilsElements.toString(this.getLogo()));
    resMap.put("industry", UtilsElements.toString(this.getIndustry()));
    resMap.put("foundation", UtilsElements.toString(this.getFoundation()));
    resMap.put("foundationPlace", UtilsElements.toString(this.getFoundationPlace()));
    resMap.put("foundingYear", new Date());
    resMap.put("scenarioId", UtilsElements.toIntArray(this.getScenarioId()));
    resMap.put("services", UtilsElements.toStringArray(this.getServices()));
    resMap.put("telephones", UtilsElements.toStringArray(this.getTelephones()));
    resMap.put("products", UtilsElements.toStringArray(this.getProducts()));
    resMap.put("schedule", UtilsElements.toString(this.getSchedule()));
    resMap.put("thumbnail", UtilsElements.toString(this.getThumbnail()));
    // sameAs properties
    sameAsMap.put("type", "object");
    sameAsMap.put("sameAsExtended", UtilsElements.sameAsToMapArray(this.getSameAs()));
    resMap.put("sameAs", sameAsMap);
    // location properties
    resMap.put("locations", UtilsElements.locationsToMapArray(this.getPersistentpoint()));
    resMap.put("image", "");
    resMap.put("video", "");
    resMap.put("extraInfo", UtilsElements.toStringArray(this.getExtraInfo()));
    /**
            "locations": 
            {
                "type": "object",
                "_comment_" : "Used as an array of locations",
                "properties" : {
                    "locStreetAddress" : {"type" : "string"},
                    "locState" : {"type" : "string"},
                    "locCountry" : {"type" : "string"},
                    "geoPoint" : {"type" : "geo_point"}
                }, 
                "index_name" : "locations"
            },
        }
     */
    return resMap;
  }
}
