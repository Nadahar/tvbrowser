package movieawardplugin;

import java.util.HashMap;

public class MovieAwardCategorie {
  private String mId;
  private HashMap<String, String> mNames = new HashMap<String, String>();

  public MovieAwardCategorie(String id) {
    mId = id;
  }

  public String getId() {
    return mId;
  }

  public void addName(String lang, String name) {
    mNames.put(lang, name);  
  }
}
