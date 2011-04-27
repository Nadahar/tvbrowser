package util.settings;

import java.io.File;
import java.io.IOException;

import junit.framework.TestCase;
import util.settings.PropertyManager;
import util.settings.StringArrayProperty;

public class StringArrayPropertiesTest extends TestCase {

  public void testStripSlashes() {
    PropertyManager manager = new PropertyManager();
    
    String[] start = {};
    StringArrayProperty firstProp = new StringArrayProperty(manager, "firsttest", start);
    StringArrayProperty secondProp = new StringArrayProperty(manager, "secondtest", start);
    StringArrayProperty thirdProp = new StringArrayProperty(manager, "thirdtest", start);
    
    try {
      manager.readFromStream(this.getClass().getResourceAsStream("StringArrayProperties.properties"));
    } catch (IOException e) {
      e.printStackTrace();
      assertTrue("Probleme beim Lesen der Datei", false);
    }
    
    assertEquals(4,        firstProp.getStringArray().length);
    assertEquals("Hallo",  firstProp.getStringArray()[0]);
    assertEquals("zwei",   firstProp.getStringArray()[1]);
    assertEquals("drei",   firstProp.getStringArray()[2]);
    assertEquals("vier",   firstProp.getStringArray()[3]);
    
    assertEquals(2,       secondProp.getStringArray().length);
    assertEquals("Hallo,zwei,drei,vier", secondProp.getStringArray()[0]);
    assertEquals("f\u00FCnf", secondProp.getStringArray()[1]);
    
    assertEquals(5,       thirdProp.getStringArray().length);
    assertEquals("Hallo\\", thirdProp.getStringArray()[0]);
    assertEquals("zwei\\", thirdProp.getStringArray()[1]);
    assertEquals("dr\\ei", thirdProp.getStringArray()[2]);
    assertEquals("vier\n", thirdProp.getStringArray()[3]);
    assertEquals("sechs", thirdProp.getStringArray()[4]);
    
    String[] testArray = {"Hallo", "Test,zwei", "drei\\vier"};
    
    thirdProp.setStringArray(testArray);
    
    assertEquals("Hallo,Test\\,zwei,drei\\\\vier", thirdProp.toString());
  }
  
}
