/**
 * 
 */
package com.eyllo.paprika.keeper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.elasticsearch.ElasticSearchException;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.index.query.GeoDistanceFilterBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.elasticsearch.common.xcontent.XContentFactory.*;

import com.eyllo.paprika.retriever.parser.elements.PersistentEntity;

/**
 * @author renatomarroquin
 *
 */
public class IndexLayer<K, V> extends AbstractDataLayer<K, V> {

  private static final String CLUSTER_NAME = "elasticsearch";
  public static final String ENTRY_IP = "localhost";

  /**
   * Logger to help us write write info/debug/error messages
   */
  private static Logger LOGGER = LoggerFactory.getLogger(IndexLayer.class);
  
  /**
   * Default port to connect to a ElasticSearch node.
   */
  public static final int PORT = 9300;

  /**
   * Transport client for connecting to ElasticSearch.
   */
  private Client elSearchClient;
  
  public IndexLayer(String...pDataLayerParams) {
    super();
    getLogger().info("Initializing IndexLayer for data persistency.");
    this.initializeDataLayer();
  }

  @Override
  /**
   * Initializes the transport client for ElasticSearch.
   */
  public void initializeDataLayer(String...pInitParams) {
    if (elSearchClient == null) {
      Settings settings = ImmutableSettings.settingsBuilder()
          .put("cluster.name", CLUSTER_NAME).build();
      elSearchClient = (new TransportClient(settings)
      .addTransportAddress(new InetSocketTransportAddress(ENTRY_IP, PORT)));
    }
  }

  public void index(List<PersistentEntity> pEntities, String pIndexName,
      String pAttrName, String pAttrValue){
    for (PersistentEntity pEnt : pEntities){
      this.index(pEnt, pIndexName, pAttrName, pAttrValue);
    }
  }

  public void testIndex( String pIndexName,
          String pAttrName, String pAttrValue){
        IndexResponse response;
        try {
            response = getElSearchClient().prepareIndex(pIndexName, pAttrName, pAttrValue)
                .setSource(
                    jsonBuilder()
                            .startObject()
                               .field("user", "kimchy")
                              .field("postDate", new Date())
                                .field("message", "trying out Elastic Search")
                           .endObject()
                          )
                .execute()
                .actionGet();
            System.out.println(response.toString());
        } catch (ElasticSearchException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
      
  }

  /**
   * Indexes a PersistentEntity into a specific index, and type.
   * @param pEnt        PersistentEntity.
   * @param pIndexName  Index to be used.
   * @param pTypeName   Type name where persistentEntity will be stored.
   * @param pTypeId     TypeId to identify persistentEntity to be stored.
   * @return Map<String, Object> Containing all information obtained.
   */
  public Map<String, Object> index(PersistentEntity pEnt, String pIndexName,
      String pTypeName, String pTypeId){
    IndexResponse response = getElSearchClient().prepareIndex(pIndexName, pTypeName, pTypeId)
        .setSource(pEnt.toMap()
            //jsonBuilder()
            //        .startObject()
            //            .field("user", "kimchy")
            //            .field("postDate", new Date())
            //            .field("message", "trying out Elastic Search")
            //        .endObject()
                  )
        .execute()
        .actionGet();
    return response.getHeaders();
    //System.out.println(response.getHeaders().toString());
  }

  /**
   * Indexes a PersistentEntity into a specific index, and type.
   * @param pIndexedObj JSONObject to be persisted.
   * @param pIndexName  Index to be used.
   * @param pTypeName   Type name where persistentEntity will be stored.
   * @param pTypeId     TypeId to identify persistentEntity to be stored.
   * @return Map<String, Object> Containing all information obtained.
   */
  public Long index(JSONObject pIndexedObj, String pIndexName,
      String pTypeName, String pTypeId) {
    Long versionNumber = -1L;
    try {
      IndexResponse response = getElSearchClient().prepareIndex(pIndexName, pTypeName, pTypeId)
          .setSource(pIndexedObj.toJSONString())
          .execute()
          .actionGet();
      versionNumber = response.getVersion();
    } catch (Exception e) {
      getLogger().error("Error trying to index data inside " + pIndexName + pTypeName + pTypeId);
      e.printStackTrace();
    }
    return versionNumber;
  }

  //TODO this objects shouldn't be being castes into specific objects, maybe using reflection?
  @Override
  public boolean put(Object key, Object value, String... params) {
    boolean opFlag = true;
    JSONObject pIndexedObj = (JSONObject) value;
    String pTypeId = key.toString();
    String pIndexName = "";
    String pTypeName = "";
    if (params !=null && params.length == 2) {
      pIndexName = params[0];
      pTypeName = params[1];
    }
    Long version = index(pIndexedObj, pIndexName, pTypeName, pTypeId);
    System.out.println("Version number of the element: " + version);
    if (version < 0)
      opFlag = false;
    return opFlag;
  }

  /**
   * Gets the value of a specific indexed element.
   * @param pIndexName
   * @param pAttrName
   * @param pAttrValue
   * @param pOperThreaded
   * @return
   */
  public GetResponse get(String pIndexName, String pAttrName,
      String pAttrValue, boolean pOperThreaded) {
    GetResponse response = null;
    response = getElSearchClient().prepareGet(pIndexName, pAttrName, pAttrValue)
        .setOperationThreaded(pOperThreaded)
        .execute()
        .actionGet();
    return response;
  }

  @Override
  @SuppressWarnings({ "rawtypes", "unchecked" })
  public List<V> scan(String pIndexName, String pAttrName, String ... pExtra) {
    List arrayList = null;
    
    try {
      QueryBuilder qb = QueryBuilders.matchAllQuery();
      SearchResponse scrollResp = getElSearchClient().prepareSearch(pIndexName)
              .setSearchType(SearchType.SCAN)
              .setScroll(new TimeValue(60000))
              .setQuery(qb)
              .setSize(100)
              .execute().actionGet(); //100 hits per shard will be returned for each scroll
      arrayList = new ArrayList ();
      //Scroll until no hits are returned
      while (true) {
          scrollResp = getElSearchClient()
              .prepareSearchScroll(scrollResp.getScrollId())
              .setScroll(new TimeValue(600000)).execute().actionGet();
          for (SearchHit hit : scrollResp.getHits())
            arrayList.add(hit);
          //Break condition: No hits are returned
          if (scrollResp.getHits().getHits().length == 0) {
              break;
          }
      }
    } catch (org.elasticsearch.indices.IndexMissingException e) {
      getLogger().error("Error while reading data from " + pIndexName);
      e.printStackTrace();
    }
    return arrayList;
  }

  /**
   * Gets the value of a specific indexed element.
   * @param pIndexName
   * @param pAttrName
   * @param pAttrValue
   * @param pOperThreaded
   * @return
   */
  public GetResponse get(String pIndexName, String pAttrName, String pAttrValue) {
    return get(pIndexName, pAttrName, pAttrValue, false);
  }

  public GeoDistanceFilterBuilder getDistanceFilter(int pDistance, String pDistanceUnit, double pLat, double pLng, String pFieldName){
      /*
       * return FilterBuilders.geoDistanceFilter("pin.location")
        .point(40, -70)
        .distance(200, DistanceUnit.KILOMETERS)
        .optimizeBbox("memory")                    // Can be also "indexed" or "none"
        .geoDistance(GeoDistance.ARC);            // Or GeoDistance.PLANE*/
      
      return FilterBuilders.geoDistanceFilter(pFieldName)
        .point(pLat, pLng)
        .distance(pDistance, UtilsStore.getDistanceUnit(pDistanceUnit))
        .optimizeBbox(ConstantsStore.BOUNDING_BOX_MEMORY)                    // Can be also "indexed" or "none"
        .geoDistance(ConstantsStore.DEFAULT_GEO_DIST);            // Or GeoDistance.PLANE
  }

  public void search(String pIndexName){
      //QueryBuilder qb = QueryBuilders.matchQuery("name", "kimchy elasticsearch");
    SearchRequestBuilder srb = elSearchClient.prepareSearch(pIndexName);                
    srb.setQuery(QueryBuilders.matchAllQuery());
    srb.setFilter(
        getDistanceFilter(10, "km", -23.5291716, -46.7272395, "geoPoint")
        );
    SearchResponse ll = srb.execute().actionGet();
    System.out.println(ll.toString());
    
  }

  @Override
  public boolean saveElements(Map<K, V> pElems, String... params) {
    // TODO Auto-generated method stub
    return false;
  }

  /**
   * Performs clean up for the class.
   */
  public void cleanup(){
    if (getElSearchClient() != null) {
      getElSearchClient().close();
    }
  }

  /**
   * Main method used for testing inner-class components.
   * @param args
  public static void main(String args[]){
    // defining a social retriever object
    //String userName = "naturanet";
    SocialRetriever socRetriever = new SocialRetriever();
    Properties props = UtilsConnection.
        getProperties(SocialRetriever.DEFAULT_SOCIALCONNS_PROP);
    socRetriever.setSocialConn(SocialConnectionFactory.
        createConnection(props,UtilsConnection.TWITTER));
    List<JSONObject> tweets = socRetriever.getSocialConn().fetchUserPosts(userName,10);
    System.out.println(tweets);

    // defining an indexing layer object
    String indexEntities = "eyllo_entities";
    String indexAttrName = "eyllo_entity";
    
    IndexLayer iLayer = new IndexLayer();
    //List<PersistentEntity> entities = new ApontadorParser("hotels", 1, 20).getEntities();
    //List<PersistentEntity> entities = EntityRetriever.getEntities(ApontadorParser.NAME);
    //for (PersistentEntity ent : entities){
    //  iLayer.index(ent, indexEntities, indexAttrName, UUID.randomUUID().toString());
    //}
    
    iLayer.search(indexEntities);
    //GetResponse response = iLayer.get("twitter", "tweet", "1");

    //System.out.println(response.getSourceAsString());
    //on shutdown
    iLayer.cleanup();
  }*/

  /**
   * @return the elSearchClient
   */
  public Client getElSearchClient() {
    if (elSearchClient == null) {
      initializeDataLayer();
    }
    return elSearchClient;
  }

  /**
   * @param elSearchClient the elSearchClient to set
   */
  public void setElSearchClient(Client elSearchClient) {
    this.elSearchClient = elSearchClient;
  }

  @Override
  public V get(K key) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public AbstractDataLayer<K, V> getDataLayer() {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * @return the lOGGER
   */
  public static Logger getLogger() {
    return LOGGER;
  }

  /**
   * @param lOGGER the lOGGER to set
   */
  public static void setLogger(Logger lOGGER) {
    LOGGER = lOGGER;
  }
}
