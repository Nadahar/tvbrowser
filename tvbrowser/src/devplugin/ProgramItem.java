package devplugin;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;


public class ProgramItem {
   
  private Program mProgram;
  private Properties mProperties;
    
  
  public ProgramItem(Program prog) {
    mProgram = prog;
    mProperties = new Properties();
  }
  
  public ProgramItem() {
    this(null);
  }

  public void read(ObjectInputStream in) throws IOException, ClassNotFoundException {
    int version = in.readInt();
    Date date = new Date(in);
    String progId = (String)in.readObject();
    mProgram = Plugin.getPluginManager().getProgram(date, progId);

    int keyCnt = in.readInt();
    for (int i=0; i<keyCnt; i++) {
      String key = (String)in.readObject();
      String value = (String)in.readObject();
      mProperties.put(key, value);
    }
    
  }
  
  public void write(ObjectOutputStream out) throws IOException {
    out.writeInt(1); // version
    Date date = mProgram.getDate();
    date.writeData(out);
    String progId = mProgram.getID();
    out.writeObject(progId);
    
    Set keys = mProperties.keySet();
    out.writeInt(keys.size());
    Iterator it = keys.iterator();
    while (it.hasNext()) {
      String key = (String)it.next();
      String value = (String)mProperties.get(key);
      out.writeObject(key);
      out.writeObject(value);      
    }
    
  }
  
  public void setProgram(Program prog) {
    mProgram = prog;
  }
  
  public Program getProgram() {
    return mProgram;
  }
  
  public void setProperty(String key, String value) {
    mProperties.put(key, value);  
  }
  
  public String getProperty(String key) {
    return (String)mProperties.get(key);  
  }
  
  public String toString() {
    return mProgram.getTitle();
  }
    
}