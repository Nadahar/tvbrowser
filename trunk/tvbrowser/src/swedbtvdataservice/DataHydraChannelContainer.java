/*
 * DataHydraChannelContainer.java
 *
 * Created on March 5, 2005, 8:21 PM
 */

package swedbtvdataservice;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;

import util.misc.StringPool;

public class DataHydraChannelContainer {

  private String id;
  private String name;
  private String baseUrl;
  private String iconUrl;

  private Hashtable<String, Long> lastUpdate = new Hashtable<String, Long>();

  /**
   * Creates a new instance of DataHydraChannelContainer
   */
  public DataHydraChannelContainer(String id, String name, String baseUrl, String iconUrl, String timeString) {
    this.name = name;
    this.baseUrl = baseUrl;
    this.iconUrl = StringPool.getString(iconUrl);
    this.id = id;
    try {
      devplugin.Date now = new devplugin.Date();
      StringTokenizer ST = new StringTokenizer(timeString, "_");
      while (ST.hasMoreTokens()) {
        String part = ST.nextToken();
        StringTokenizer ST2 = new StringTokenizer(part, "-");
        String dateinfo = ST2.nextToken();
        StringTokenizer ST3 = new StringTokenizer(dateinfo, ":");
        devplugin.Date day = new devplugin.Date(Integer.parseInt(ST3.nextToken()), Integer.parseInt(ST3.nextToken()), Integer.parseInt(ST3.nextToken()));
        if (now.compareTo(day) <= 0) {
          lastUpdate.put(dateinfo, Long.valueOf(ST2.nextToken()));
        }
      }
    } catch (Exception E) {
    }
    //lastUpdate = time;
  }


  /**
   * Getter for property baseUrl.
   *
   * @return Value of property baseUrl.
   */
  public java.lang.String getBaseUrl() {
    return baseUrl;
  }

  /**
   * Setter for property baseUrl.
   *
   * @param baseUrl New value of property baseUrl.
   */
  public void setBaseUrl(java.lang.String baseUrl) {
    this.baseUrl = baseUrl;
  }

  /**
   * Getter for property name.
   *
   * @return Value of property name.
   */
  public java.lang.String getName() {
    return name;
  }

  /**
   * Setter for property name.
   *
   * @param name New value of property name.
   */
  public void setName(java.lang.String name) {
    this.name = name;
  }

  /**
   * Getter for property id.
   *
   * @return Value of property id.
   */
  public java.lang.String getId() {
    return id;
  }

  /**
   * Setter for property id.
   *
   * @param id New value of property id.
   */
  public void setId(java.lang.String id) {
    this.id = id;
  }

  public java.lang.String getIconUrl() {
    return iconUrl;
  }

  /**
   * Setter for property iconUrl.
   *
   * @param iconUrl New value of property iconUrl.
   */
  public void setIconUrl(java.lang.String iconUrl) {
    this.iconUrl = iconUrl;
  }

  public String toString() {
    return id + " : " + name + " : " + baseUrl + ":" + iconUrl;
  }

  /**
   * Getter for property lastUpdate.
   *
   * @return Value of property lastUpdate.
   */
  public long getLastUpdate(devplugin.Date day) {
    Long temp = lastUpdate.get(Integer.toString(day.getYear()) + ":" + Integer.toString(day.getMonth()) + ":" + Integer.toString(day.getDayOfMonth()));
    if (temp == null) {
      return 0;
    } else {
      return temp.longValue();
    }
  }

  /**
   * Setter for property lastUpdate.
   *
   * @param lastUpdate New value of property lastUpdate.
   */
  public void setLastUpdate(final devplugin.Date day, final long lastUpdate) {
    this.lastUpdate.put(Integer.toString(day.getYear()) + ':'
        + Integer.toString(day.getMonth()) + ':'
        + Integer.toString(day.getDayOfMonth()), lastUpdate);
  }

  public String getLastUpdateString() {
    StringBuffer buffer = new StringBuffer();
    Enumeration<String> enu = lastUpdate.keys();
    while (enu.hasMoreElements()) {
      String date = (String) enu.nextElement();
      buffer.append(date);
      buffer.append('-');
      buffer.append(lastUpdate.get(date).toString());
      buffer.append('_');
    }
    return buffer.toString();
  }
}
