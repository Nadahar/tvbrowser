package sharedchannelautoupdateplugin;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JPanel;

import devplugin.SettingsTab;

public class SharedChannelAutoUpdatePluginSettingTab implements SettingsTab {
//  private static final Logger mLog = java.util.logging.Logger.getLogger(SharedChannelAutoUpdatePluginSettingTab.class.getName());
  private static final util.ui.Localizer mLocalizer = util.ui.Localizer.getLocalizerFor(SharedChannelAutoUpdatePluginSettingTab.class);
  private Icon mIcon;


  public Icon getIcon() {
    if (mIcon == null) {
      mIcon = new ImageIcon(getClass().getResource("icons/shared.png"));
    }
    return mIcon;
  }
  
  public String getTitle() {
    return mLocalizer.msg("tabName", "Shared Channel Auto Updater");
  }
  
  public JPanel createSettingsPanel() {
    return new JPanel();
  }
    
  public void saveSettings() {
  }
  
  
}
