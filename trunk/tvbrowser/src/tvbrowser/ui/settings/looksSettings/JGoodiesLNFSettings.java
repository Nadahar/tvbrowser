package tvbrowser.ui.settings.looksSettings;

import javax.swing.JDialog;

import com.jgoodies.looks.plastic.PlasticLookAndFeel;
import com.jgoodies.looks.plastic.PlasticTheme;

public class JGoodiesLNFSettings extends JDialog {

  public JGoodiesLNFSettings(JDialog parent) {
    super(parent, true);
    
    PlasticTheme[] themes = (PlasticTheme[])PlasticLookAndFeel.getInstalledThemes().toArray(new PlasticTheme[0]);
    
    for (int i = 0; i < themes.length; i++) {
      System.out.println(themes[i].getName() + "-" + themes[i].toString());
    }
  }
  
}
