package util.program;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import tvbrowser.core.plugin.programformating.GlobalPluginProgramFormating;
import tvbrowser.core.plugin.programformating.GlobalPluginProgramFormatingManager;

/**
 * A class for a program configuration setting.
 * This is the super class of global and local
 * program configurations.
 * 
 * @author René Mach
 * @since 2.5.1
 */
public abstract class AbstractPluginProgramFormating {
  /**
   * Gets the name of this configuration
   * 
   * @return The name of this configuration
   */
  public abstract String getName();
  
  /**
   * Gets the value for title formating
   * 
   * @return The value for title formating
   */
  public abstract String getTitleValue();
  
  /**
   * Gets the value for the content formating
   * 
   * @return The value for the content formating
   */
  public abstract String getContentValue();
  
  /**
   * Gets the value for the formating
   * 
   * @return The value for the formating
   */
  public abstract String getEncodingValue();
  
  /**
   * Creates an instance of this class from an ObjectInputStream.
   * 
   * @param in The stream to read the values for the instance.
   * @return The created instance or <code>null</code> if the instance could not be created.
   * @throws IOException Thrown if something goes wrong;
   * @throws ClassNotFoundException Thrown if something goes wrong;
   */
  public static final AbstractPluginProgramFormating readData(ObjectInputStream in) throws IOException, ClassNotFoundException {
    in.readInt();
    String clazz = (String)in.readObject();
    
    try {
      AbstractPluginProgramFormating instance = (AbstractPluginProgramFormating)Class.forName(clazz).newInstance();
      instance.loadData(in);
      
      if(instance instanceof GlobalPluginProgramFormating)
        instance = GlobalPluginProgramFormatingManager.getInstance().getFormatingInstanceForInstance((GlobalPluginProgramFormating)instance);
      
      return instance;
    }catch(Exception e) {}
    
    return null;
  }
  
  /**
   * Saves the information about the program configuration
   * values in an ObjectOutputStream.
   * 
   * @param out The stream to write the data to.
   * @throws IOException Thrown if something goes wrong.
   */
  public final void writeData(ObjectOutputStream out) throws IOException {
    out.writeInt(1); // version
    out.writeObject(this.getClass().getName());
    storeData(out);
  }
  
  protected abstract void storeData(ObjectOutputStream out) throws IOException;
  protected abstract void loadData(ObjectInputStream in) throws IOException, ClassNotFoundException;
  
  /**
   * Gets if the configuration is valid.
   * 
   * @return If the configuration is valid
   */
  public abstract boolean isValid();
  
  /**
   * Gets the id of this instance.
   * 
   * @return The id of this instance.
   */
  public abstract String getId();
}
