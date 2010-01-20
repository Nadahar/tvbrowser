package mixedupdatelockplugin;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JPanel;

import devplugin.SettingsTab;

public class MixedUpdateLockPluginSettingTab implements SettingsTab {
//  private static final Logger mLog = java.util.logging.Logger.getLogger(MixedUpdateLockPluginSettingTab.class.getName());
  private static final util.ui.Localizer mLocalizer = util.ui.Localizer.getLocalizerFor(MixedUpdateLockPluginSettingTab.class);
  private Icon mIcon;


  public Icon getIcon() {
    if (mIcon == null) {
      mIcon = new ImageIcon(getClass().getResource("icons/lock.png"));
    }
    return mIcon;
  }
  
  public String getTitle() {
    return mLocalizer.msg("tabName", "Mixed Data Update Lock");
  }
  
  public JPanel createSettingsPanel() {
    return new JPanel();
  }
    
  public void saveSettings() {
  }
  
  
}
