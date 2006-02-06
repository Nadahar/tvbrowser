package tvbrowser.extras.programinfo;

import java.awt.*;
import java.util.Properties;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import util.ui.FontChooserPanel;

import devplugin.SettingsTab;

/**
 * The SettingsTab for the ProgramInfo viewer. 
 */
public class ProgramInfoSettingsTab implements SettingsTab {

  private static final util.ui.Localizer mLocalizer =
      util.ui.Localizer.getLocalizerFor(ProgramInfoSettingsTab.class);

  private JCheckBox mUserFont;
  private FontChooserPanel mTitleFont, mBodyFont;
  
  private Properties mSettings;
  
  /**
   * Constructor
   *
   */
  public ProgramInfoSettingsTab() {
    mSettings = ProgramInfo.getInstance().getSettings();
  }

	public JPanel createSettingsPanel() {
    JPanel content = new JPanel(new BorderLayout(0,4));    
    content.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
    
    JPanel center = new JPanel();
    center.setLayout(new BoxLayout(center,BoxLayout.Y_AXIS));
    center.setBorder(BorderFactory.createEmptyBorder(5,10,5,5));
    
    String temp = mSettings.getProperty("titlefont","Verdana");
    int size = Integer.parseInt(mSettings.getProperty("title","18"));
    
    mTitleFont = new FontChooserPanel(mLocalizer.msg("title","Title font"),new Font(temp,Font.PLAIN,size),false);
    mTitleFont.setMaximumSize(mTitleFont.getPreferredSize());
    mTitleFont.setAlignmentX(FontChooserPanel.LEFT_ALIGNMENT);
        
    temp = mSettings.getProperty("bodyfont","Verdana");
    size = Integer.parseInt(mSettings.getProperty("small","11"));

    mBodyFont = new FontChooserPanel(mLocalizer.msg("body","Description font"),new Font(temp,Font.PLAIN,size),false);    
    mBodyFont.setMaximumSize(mBodyFont.getPreferredSize());
    mBodyFont.setAlignmentX(FontChooserPanel.LEFT_ALIGNMENT);

    center.add(mTitleFont);
    center.add(mBodyFont);
    
    mUserFont = new JCheckBox(mLocalizer.msg("userfont","Use user fonts"));
    mUserFont.setSelected(mSettings.getProperty("userfont","false").equals("true"));
    
    mTitleFont.setEnabled(mUserFont.isSelected());
    mBodyFont.setEnabled(mUserFont.isSelected());
    
    mUserFont.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        mTitleFont.setEnabled(mUserFont.isSelected());
        mBodyFont.setEnabled(mUserFont.isSelected());
      }
    });
    
    content.add(mUserFont,BorderLayout.NORTH);
    content.add(center,BorderLayout.CENTER);
    
		return content;
	}
  
	
	public void saveSettings() {
    mSettings.setProperty("userfont",String.valueOf(mUserFont.isSelected()));
    
    Font f = mTitleFont.getChosenFont();
    mSettings.setProperty("titlefont",f.getFamily());
    mSettings.setProperty("title",String.valueOf(f.getSize()));
    
    f = mBodyFont.getChosenFont();    
    mSettings.setProperty("bodyfont",f.getFamily());
    mSettings.setProperty("small",String.valueOf(f.getSize()));    
	}


	public Icon getIcon() {
		return null;
	}

	
	public String getTitle() {
		return "ProgramInfo";
	}
}