/*
 * channelContainer.java
 *
 * Created on March 5, 2005, 8:21 PM
 */

package swedb;

import java.util.Hashtable;
import java.util.Properties;
import java.util.Date;
import java.util.Enumeration;

/**
 *
 * @author  pumpkin
 */
public class ChannelContainer {
  
  private String id;
  private String name;
  private String baseUrl;
  private long lastUpdate;
  
  /** Creates a new instance of channelContainer */
  public ChannelContainer(String id, String name, String baseUrl, long time) {
    this.name = name;
    this.baseUrl = baseUrl;
    this.id = id;
    lastUpdate = time;
  }


  /** Getter for property baseUrl.
   * @return Value of property baseUrl.
   */
  public java.lang.String getBaseUrl() {
    return baseUrl;
  }
  
  /** Setter for property baseUrl.
   * @param baseUrl New value of property baseUrl.
   */
  public void setBaseUrl(java.lang.String baseUrl) {
    this.baseUrl = baseUrl;
  }
  
  /** Getter for property name.
   * @return Value of property name.
   */
  public java.lang.String getName() {
    return name;
  }
  
  /** Setter for property name.
   * @param name New value of property name.
   */
  public void setName(java.lang.String name) {
    this.name = name;
  }
  
  /** Getter for property id.
   * @return Value of property id.
   */
  public java.lang.String getId() {
    return id;
  }
  
  /** Setter for property id.
   * @param id New value of property id.
   */
  public void setId(java.lang.String id) {
    this.id = id;
  }
  
  public String toString(){
    return id+" : "+name+" : "+baseUrl;
  }
  
  /** Getter for property lastUpdate.
   * @return Value of property lastUpdate.
   */
  public long getLastUpdate() {
    return lastUpdate;
  }
  
  /** Setter for property lastUpdate.
   * @param lastUpdate New value of property lastUpdate.
   */
  public void setLastUpdate(long lastUpdate) {
    this.lastUpdate = lastUpdate;
  }
  
}
