package nextviewdataupdatewatcherplugin;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JPanel;

import devplugin.SettingsTab;

public class NextViewDataUpdateWatcherPluginSettingTab implements SettingsTab {
//  private static final Logger mLog = java.util.logging.Logger.getLogger(NextViewDataUpdateWatcherPluginSettingTab.class.getName());
  private static final util.ui.Localizer mLocalizer = util.ui.Localizer.getLocalizerFor(NextViewDataUpdateWatcherPluginSettingTab.class);
  private Icon mIcon;


  public Icon getIcon() {
    if (mIcon == null) {
      mIcon = new ImageIcon(getClass().getResource("icons/nxtvepg.png"));
    }
    return mIcon;
  }
  
  public String getTitle() {
    return mLocalizer.msg("tabName", "Mixed Data Auto Updater");
  }
  
  public JPanel createSettingsPanel() {
    return new JPanel();
  }
    
  public void saveSettings() {
  }
  
  
}
