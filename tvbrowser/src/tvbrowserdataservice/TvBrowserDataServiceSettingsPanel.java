

package tvbrowserdataservice;

import javax.swing.*;
import java.awt.*;
import java.util.Properties;

import tvdataservice.SettingsPanel;
import tvbrowserdataservice.file.*;



public class TvBrowserDataServiceSettingsPanel extends SettingsPanel {

	private Properties mSettings;
  private JCheckBox[] mLevelCheckboxes;
  
  private static SettingsPanel mInstance;
    
  /** The localizer for this class. */
    public static final util.ui.Localizer mLocalizer
      = util.ui.Localizer.getLocalizerFor(TvBrowserDataServiceSettingsPanel.class);
  
    
  protected TvBrowserDataServiceSettingsPanel(Properties settings) {
  
    mSettings=settings;
    setLayout(new BorderLayout());
    JPanel levelList=new JPanel();
    levelList.setLayout(new BoxLayout(levelList,BoxLayout.Y_AXIS));
    
    levelList.add(new JLabel(mLocalizer.msg("downloadLevel","Download this data")));
    
    TvDataLevel[] levelArr=DayProgramFile.LEVEL_ARR;
    
    String[] levelIds=settings.getProperty("level","").split(":::");
    
    for (int i=0;i<levelIds.length;i++) {
      System.out.println(levelIds[i]);
    }
    
    
    mLevelCheckboxes=new JCheckBox[levelArr.length];
    for (int i=0;i<levelArr.length;i++) {
      mLevelCheckboxes[i]=new JCheckBox(levelArr[i].getDescription());
      levelList.add(mLevelCheckboxes[i]);
      if (levelArr[i].isRequired()) {
        mLevelCheckboxes[i].setSelected(true);
        mLevelCheckboxes[i].setEnabled(false);
      }
      else {
        for (int j=0;j<levelIds.length;j++) {
          if (levelIds[j].equals(levelArr[i].getId())) {
            mLevelCheckboxes[i].setSelected(true);
          }
        }
      }
    }
       
    add(levelList,BorderLayout.WEST);
   
    
  }
  
  public static SettingsPanel getInstance(Properties settings) {
    if (mInstance==null) {
      mInstance=new TvBrowserDataServiceSettingsPanel(settings);
    }
    return mInstance;
  }
  
  
  
	public void ok() {
    String setting="";
		for (int i=0;i<mLevelCheckboxes.length;i++) {
      if (mLevelCheckboxes[i].isSelected()) {
        setting+=":::"+DayProgramFile.LEVEL_ARR[i].getId();
      }
		}
    if (setting.length()>3) {
      setting=setting.substring(3);
    }
    mSettings.setProperty("level",setting);
	}
  
  
}