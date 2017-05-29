package compat;

import java.lang.reflect.Field;

import javax.swing.border.Border;

import com.jgoodies.forms.factories.Borders;

import devplugin.Version;
import tvbrowser.TVBrowser;

public final class BordersCompat {
  public static Border getDialogBorder() {
    Border result = null;
    String name = "DIALOG";
    
    if(TVBrowser.VERSION.compareTo(new Version(3,30,true)) < 0) {
      name = "DIALOG_BORDER";
    }
    
    try {
      Class<?> borders = Borders.class;
      Field border = borders.getDeclaredField(name);
      result = (Border)border.get(borders);
    } catch (Exception e) {
      // ignore
    }
    
    return result;
  }
}
