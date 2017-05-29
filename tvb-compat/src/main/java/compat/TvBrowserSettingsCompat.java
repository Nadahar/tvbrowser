package compat;

import tvbrowser.core.Settings;

public final class TvBrowserSettingsCompat {
  public static String getTimePattern() {
    return Settings.getTimePattern();
  }
}
