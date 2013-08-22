/**
 * 
 */
package com.eyllo.paprika.store;

import java.util.List;
//import java.util.Properties;
import java.util.UUID;

import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
//import org.json.simple.JSONObject;




import com.eyllo.paprika.entity.elements.PersistentEntity;
import com.eyllo.paprika.entity.jobs.EntityRetriever;
import com.eyllo.paprika.html.parser.RioGuiaParser;
import com.eyllo.paprika.html.parser.VejaSaoPauloParser;
//import com.eyllo.paprika.social.SocialRetriever;
//import com.eyllo.paprika.social.connection.SocialConnectionFactory;
//import com.eyllo.paprika.social.connection.UtilsConnection;

/**
 * @author renatomarroquin
 *
 */
public class IndexLayer {

  private static final String CLUSTER_NAME = "elasticsearches";
  public static final String ENTRY_IP = "localhost";

  /**
   * Default port to connect to a ElasticSearch node.
   */
  public static final int PORT = 9300;

  /**
   * Transport client for connecting to ElasticSearch.
   */
  private Client elSearchClient;

  /**
   * Initializes the transport client for ElasticSearch.
   */
  public void initialize(){
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

  public void index(PersistentEntity pEnt, String pIndexName,
      String pAttrName, String pAttrValue){
    IndexResponse response = getElSearchClient().prepareIndex(pIndexName, pAttrName, pAttrValue)
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
    System.out.println(response.toString());
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
      String pAttrValue, boolean pOperThreaded){
    GetResponse response = null;
    response = getElSearchClient().prepareGet(pIndexName, pAttrName, pAttrValue)
        .setOperationThreaded(pOperThreaded)
        .execute()
        .actionGet();
    return response;
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

  public void search(){
    
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
   */
  public static void main(String args[]){
    // defining a social retriever object
    /*String userName = "naturanet";
    SocialRetriever socRetriever = new SocialRetriever();
    Properties props = UtilsConnection.
        getProperties(SocialRetriever.DEFAULT_SOCIALCONNS_PROP);
    socRetriever.setSocialConn(SocialConnectionFactory.
        createConnection(props,UtilsConnection.TWITTER));
    List<JSONObject> tweets = socRetriever.getSocialConn().fetchUserPosts(userName,10);
    System.out.println(tweets);*/

    // defining an indexing layer object
    String indexEntities = "eyllo_entities";
    String indexAttrName = "eyllo_entity";
    
    IndexLayer iLayer = new IndexLayer();
    //List<PersistentEntity> entities = new ApontadorParser("hotels", 1, 20).getEntities();
    List<PersistentEntity> entities = EntityRetriever.getEntities(VejaSaoPauloParser.NAME);
    for (PersistentEntity ent : entities){
      iLayer.index(ent, indexEntities, indexAttrName, UUID.randomUUID().toString());
    }

    GetResponse response = iLayer.get("twitter", "tweet", "1");

    System.out.println(response.getSourceAsString());
    //on shutdown
    iLayer.cleanup();
  }

  /**
   * @return the elSearchClient
   */
  public Client getElSearchClient() {
    if (elSearchClient == null) {
      initialize();
    }
    return elSearchClient;
  }

  /**
   * @param elSearchClient the elSearchClient to set
   */
  public void setElSearchClient(Client elSearchClient) {
    this.elSearchClient = elSearchClient;
  }
}
