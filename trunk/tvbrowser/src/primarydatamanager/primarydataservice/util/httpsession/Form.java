package primarydatamanager.primarydataservice.util.httpsession;

import java.util.ArrayList;

public class Form {
  
  private ArrayList<Field> mFields;
  
  public Form() {
    mFields = new ArrayList<Field>();
  }
  
  public void addField(String key, String value) {
    addField(new Field(key, value));
  }
  
  public void addField(Field field) {
    mFields.add(field);
  }
  
  public Field[] getFields() {
    Field[] result = new Field[mFields.size()];
    mFields.toArray(result);
    return result;
  }
  
}