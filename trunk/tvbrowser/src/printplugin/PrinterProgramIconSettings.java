package printplugin;

import java.awt.*;

import java.util.*;
import javax.swing.Icon;

import tvbrowser.core.Settings;

import devplugin.*;


public class PrinterProgramIconSettings implements ProgramIconSettings {
  
  private static final Font PROGRAMTITLEFONT=new Font("Dialog",Font.BOLD,12);
  private static final Font PROGRAMTEXTFONT=new Font("Dialog",Font.PLAIN,10);
  private static final Font PROGRAMTIMEFONT=new Font("Dialog",Font.BOLD,12);
  
  private static final Color COLOR_ON_AIR_DARK  = new Color(128, 128, 255, 80);
  private static final Color COLOR_ON_AIR_LIGHT = new Color(128, 128, 255, 40);
  private static final Color COLOR_MARKED       = new Color(255, 0, 0, 40);

  private static ProgramIconSettings mInstance;
  
  private PrinterProgramIconSettings() {    
  }
  
  public static ProgramIconSettings getInstance() {
    if (mInstance == null) {
      mInstance = new PrinterProgramIconSettings();
    }
    return mInstance;
  }
	
	public Font getTitleFont() {
		return PROGRAMTITLEFONT;
	}

	
	public Font getTextFont() {
		return PROGRAMTEXTFONT;
	}

	
	public Font getTimeFont() {
		return PROGRAMTIMEFONT;
	}

	
	public int getTimeFieldWidth() {
		return 40;
	}

	

	
	public ProgramFieldType[] getProgramInfoFields() {
		return new ProgramFieldType[]{
        ProgramFieldType.SHORT_DESCRIPTION_TYPE,
        ProgramFieldType.DESCRIPTION_TYPE
    };
	}

	
	public Icon[] getPluginIcons(Program program) {
    
    return new Icon[]{};
    
    /*
    ArrayList list = new ArrayList();
    
    String[] iconPluginArr = Settings.getProgramTableIconPlugins();
    Plugin[] pluginArr = Plugin.getPluginManager().getInstalledPlugins();
    for (int i = 0; i < iconPluginArr.length; i++) {
      // Find the plugin with this class name and add its icons
      for (int j = 0; j < pluginArr.length; j++) {
        String className = pluginArr[j].getClass().getName();
        if (iconPluginArr[i].equals(className)) {
          // This is the right plugin -> Add its icons
          Icon[] iconArr = pluginArr[j].getProgramTableIcons(program);
          if (iconArr != null) {
            for (int k = 0; k < iconArr.length; k++) {
              list.add(iconArr[k]);
            }
          }
        }
      }
    }
    
    Icon[] asArr = new Icon[list.size()];
    list.toArray(asArr);
    return asArr;*/
	}

	
	public Color getColorOnAir_dark() {
		return COLOR_ON_AIR_DARK;
	}

	
	public Color getColorOnAir_light() {
		return COLOR_ON_AIR_LIGHT;
	}

	public Color getColorMarked() {
		return COLOR_MARKED;
	}

	
	public boolean getPaintExpiredProgramsPale() {
		return false;
	}

	
	public boolean getPaintProgramOnAir() {
		return false;
	}

	
	public boolean getPaintMarkedPrograms() {
		return false;
	}
  
}