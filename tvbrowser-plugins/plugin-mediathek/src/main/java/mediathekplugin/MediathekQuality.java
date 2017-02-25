package mediathekplugin;

import util.ui.Localizer;

public enum MediathekQuality {
  NORM, HD, LOW;
  
  private static final Localizer localizer = Localizer
      .getLocalizerFor(MediathekQuality.class);
    
  static MediathekQuality fromString(String value) {
    if (value==null) return NORM;
    if (value.equals("HD") || value.equals("HQ")) return HD;
    if (value.equals("Low") || value.equals("Small")) return LOW;
    return NORM;
  }
  
  String toSaveString(){
    switch(this) {
      case HD: return "HD";
      case LOW: return "Low";
      default: return "SD";
    }
  }

  
  public String toString(){
    switch(this) {
      case HD: return localizer.msg("hd", "HD");
      case LOW: return localizer.msg("low", "Low");
      default: return localizer.msg("norm", "SD");
    }
  }

}
