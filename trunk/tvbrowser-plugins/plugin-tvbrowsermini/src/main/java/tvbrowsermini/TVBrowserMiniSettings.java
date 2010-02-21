package tvbrowsermini;

import java.io.File;
import java.util.Properties;

import devplugin.Plugin;
import devplugin.ProgramFieldType;

public class TVBrowserMiniSettings extends PropertyBasedSettings {

  private static final String KEY_DEVICE = "device";
  private static final String KEY_EXPORT_DAYS = "exportDays";
  private static final String KEY_ACCEPT = "accept";
  private static final String KEY_PATH = "path";
  private static final ProgramFieldType[] ALWAYS_EXPORTED_FIELDS = { ProgramFieldType.TITLE_TYPE, ProgramFieldType.START_TIME_TYPE};

  public TVBrowserMiniSettings(final Properties properties) {
    super(properties);
  }

  public String getPath() {
    return get(KEY_PATH, Plugin.getPluginManager().getTvBrowserSettings().getTvBrowserUserHome() + File.separator
        + "tvdata.tvd");
  }

  public boolean getAccepted() {
    return get(KEY_ACCEPT, 0) == 1;
  }

  public int getDaysToExport() {
    int days = get(KEY_EXPORT_DAYS, 14);
    if (days == 0) {
      return 28;
    }
    return days;
  }

  public boolean isDeviceAndroid() {
    return get(KEY_DEVICE, 0) == 1;
  }

  public void setPath(final String path) {
    set(KEY_PATH, path);
  }

  public void setAccepted(final boolean accepted) {
    set(KEY_ACCEPT, accepted ? 1 : 0);
  }

  public void setDaysToExport(final int days) {
    set(KEY_EXPORT_DAYS, days);
  }

  public void setAndroidDevice(boolean android) {
    set(KEY_DEVICE, android ? 1 : 0);
  }

  private String getFieldTypeKey(final ProgramFieldType fieldType) {
    return "fieldType" + fieldType.getTypeId();
  }

  public boolean getProgramField(final ProgramFieldType fieldType) {
    for (ProgramFieldType always : ALWAYS_EXPORTED_FIELDS) {
      if (fieldType.equals(always)) {
        return true;
      }
    }
    return get(getFieldTypeKey(fieldType), fieldType.equals(ProgramFieldType.SHORT_DESCRIPTION_TYPE) || fieldType.equals(ProgramFieldType.EPISODE_TYPE));
  }

  protected void setProgramField(final ProgramFieldType type, final boolean active) {
    set(getFieldTypeKey(type), active);
  }

  public ProgramFieldType[] getAlwaysExportedFields() {
    return ALWAYS_EXPORTED_FIELDS.clone();
  }

}