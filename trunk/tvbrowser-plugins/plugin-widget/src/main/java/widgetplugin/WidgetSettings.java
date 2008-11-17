package widgetplugin;

import java.util.Properties;

public class WidgetSettings {

  private static final String SETTING_PORT_NUMBER = "portNumber";
  private static final String SETTING_PORT_NUMBER_DEFAULT = "34567";
  private static final String SETTING_REFRESH = "refresh";
  
  private Properties mSettings;

  public WidgetSettings(Properties settings) {
    mSettings = settings;
  }

  public int getPortNumber() {
    return Integer.valueOf(mSettings.getProperty(SETTING_PORT_NUMBER,
        SETTING_PORT_NUMBER_DEFAULT));
  }

  public boolean getRefresh() {
    return Boolean.valueOf(mSettings.getProperty(SETTING_REFRESH, "true"));
  }

  public void setPortNumber(int port) {
    mSettings.setProperty(SETTING_PORT_NUMBER, String.valueOf(port));
  }

  public void setRefresh(boolean selected) {
    mSettings.setProperty(SETTING_REFRESH, selected ? "true" : "false");
  }

  public Properties storeSettings() {
    return mSettings;
  }

}
