package mixeddataautoupdateplugin;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JPanel;

import devplugin.SettingsTab;

public class MixedDataAutoUpdatePluginSettingTab implements SettingsTab {
//  private static java.util.logging.Logger mLog = java.util.logging.Logger.getLogger(MixedDataAutoUpdatePluginSettingTab.class.getName());
  private static final util.ui.Localizer mLocalizer = util.ui.Localizer.getLocalizerFor(MixedDataAutoUpdatePluginSettingTab.class);
  private Icon mIcon;


  public Icon getIcon() {
    if (mIcon == null) {
      mIcon = new ImageIcon(getClass().getResource("icons/mixed.png"));
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
