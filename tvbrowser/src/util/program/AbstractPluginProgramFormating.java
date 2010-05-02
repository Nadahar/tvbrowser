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
  protected String mId;
  protected String mName;
  protected String mTitleValue;
  protected String mContentValue;
  protected String mEncodingValue;

  protected AbstractPluginProgramFormating(String id, String name, String titleValue, String contentValue,
      String encodingValue) {
    mId = id;
    mName = name;
    mTitleValue = titleValue;
    mContentValue = contentValue;
    mEncodingValue = encodingValue;
  }

  /**
   * Gets the name of this configuration
   *
   * @return The name of this configuration
   */
  public String getName() {
    return mName;
  }

  /**
   * Gets the value for title formating
   *
   * @return The value for title formating
   */
  public String getTitleValue() {
    return mTitleValue;
  }

  /**
   * Gets the value for the content formating
   *
   * @return The value for the content formating
   */
  public String getContentValue() {
    return mContentValue;
  }

  /**
   * Gets the value for the formating
   *
   * @return The value for the formating
   */
  public String getEncodingValue() {
    return mEncodingValue;
  }

  /**
   * Sets the name of this configuration
   *
   * @param value The new name
   */
  public void setName(String value) {
    mName = value;
  }

  /**
   * Sets the title formating value
   *
   * @param value The new title value
   */
  public void setTitleValue(String value) {
    mTitleValue = value;
  }

  /**
   * Sets the content formating value
   *
   * @param value The new content value
   */
  public void setContentValue(String value) {
    mContentValue = value;
  }

  /**
   * Sets the encoding value of this configuration
   *
   * @param value The new encoding
   */
  public void setEncodingValue(String value) {
    mEncodingValue = value;
  }

  public void setId(final String id) {
    mId = id;
  }


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

      if(instance instanceof GlobalPluginProgramFormating) {
        instance = GlobalPluginProgramFormatingManager.getInstance().getFormatingInstanceForInstance((GlobalPluginProgramFormating)instance);
      }

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

  public String getId() {
    return mId;
  }

  public String toString() {
    return getName();
  }
}
