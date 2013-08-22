//  
/**
 * 
 */
package com.eyllo.paprika.social;

import java.util.Properties;

import com.eyllo.paprika.social.connection.AbstractSocialConnection;
import com.eyllo.paprika.social.connection.UtilsConnection;
import com.eyllo.paprika.social.connection.SocialConnectionFactory;

/**
 * @author renatomarroquin
 *
 */
public class SocialRetriever {

  /**
   * Default social network properties' file name.
   */
  public static String DEFAULT_SOCIALCONNS_PROP = "socialConns.properties";

  /**
   * Connection to a specific social network data source.
   */
  private AbstractSocialConnection socialConn;

  /**
   * @param args
   */
  public static void main(String[] args) {
    String userName = "naturanet";//"xxxxxcasfasdfa";
    // 1. set connection
    SocialRetriever socRet = new SocialRetriever();
    Properties props = UtilsConnection.getProperties(DEFAULT_SOCIALCONNS_PROP);
    socRet.setSocialConn(SocialConnectionFactory.createConnection(props,UtilsConnection.TWITTER));

    // 2. set twitter user
    socRet.getSocialConn().searchUser(userName,100);
    //socRet.getSocialConn().fetchUserPosts(userName,10);
    // 3. get feeds or tweets
  }

  //almacenar en la base de datos
  /*
   * public void Almacenar(Status tweet){
      String id_tweet = Long.toString( tweet.getId() );
      String text = tweet.getText();
      String from = tweet.getSource();
      String to = tweet.getUser().getScreenName();
      
      //System.out.println(from.length());
      
      mysql m = new mysql("twitter", "127.0.0.1", "twitter", "twitter");
      m.Insertar(id_tweet, text, from, to);
  }
  
  //Recuperar listado de ultimos tweets escritos 3210
  public void ObtenerTweets(){
      for (int i = 1; i< 25; i++){ //25
          Paging pagina = new Paging(i,200);  //200
          ResponseList<Status> listado;
          try {
              listado = twitter_.getUserTimeline("naturanet",pagina);
              if (listado.size() > 0){
                  for (int j = 0; j < listado.size(); j++){
                      Almacenar( listado.get(j) );
                  }
              }
          } catch (TwitterException e) {
              // TODO Auto-generated catch block
              e.printStackTrace();
          }
      }
  }*/

  /**
   * @return the socialConn
   */
  public AbstractSocialConnection getSocialConn() {
    return socialConn;
  }

  /**
   * @param socialConn the socialConn to set
   */
  public void setSocialConn(AbstractSocialConnection socialConn) {
    this.socialConn = socialConn;
  }
}
