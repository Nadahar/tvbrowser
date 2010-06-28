package primarydatamanager.primarydataservice.util.httpsession;

public class Field {
  
  private String mKey, mValue;
  
  public Field(String key, String value) {
    mKey = key;
    mValue = value;
  }
  
  public String getKey() {
    return mKey;
  }
  
  public String getValue() {
    return mValue;
  }
  
}