package tvbrowser.ui.settings;

import javax.swing.Icon;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import tvbrowser.core.Settings;
import tvbrowser.core.icontheme.IconLoader;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import devplugin.SettingsTab;

/**
 * Settings for network stuff.
 * 
 * @author René Mach
 * @since 2.5.1
 */
public class NetworkSettingsTab implements SettingsTab {
  protected static final util.ui.Localizer mLocalizer = util.ui.Localizer
  .getLocalizerFor(NetworkSettingsTab.class);
  
  private JSpinner mConnectionTimeout, mNetworkCheckTimeout;
  
  public JPanel createSettingsPanel() {
    PanelBuilder pb = new PanelBuilder(new FormLayout("5dlu, pref, 3dlu, 0dlu:grow", "pref, 5dlu, pref, 10dlu, pref, 5dlu, pref"));
    pb.setDefaultDialogBorder();
    
    CellConstraints cc = new CellConstraints();
    
    pb.addSeparator(mLocalizer.msg("waitTime","Wait time for connection test"), cc.xyw(1,1,4));
    pb.add(mNetworkCheckTimeout = new JSpinner(new SpinnerNumberModel(Settings.propNetworkCheckTimeout.getInt()/1000,10,90,5)), cc.xy(2,3));
    pb.addLabel(mLocalizer.msg("seconds","Seconds"), cc.xy(4,3));
    
    pb.addSeparator(mLocalizer.msg("cancelTime","Timeout for not responding connections"), cc.xyw(1,5,4));
    pb.add(mConnectionTimeout = new JSpinner(new SpinnerNumberModel(Settings.propDefaultNetworkConnectionTimeout.getInt()/1000,5,60,5)), cc.xy(2,7));
    pb.addLabel(mLocalizer.msg("seconds","Seconds"), cc.xy(4,7));
    
    return pb.getPanel();
  }

  public Icon getIcon() {
    return IconLoader.getInstance().getIconFromTheme("status", "network-transmit-receive", 16);
  }

  public String getTitle() {
    return mLocalizer.msg("title","Network");
  }

  public void saveSettings() {
    Settings.propDefaultNetworkConnectionTimeout.setInt(((Integer)mConnectionTimeout.getValue()).intValue() * 1000);
    Settings.propNetworkCheckTimeout.setInt(((Integer)mNetworkCheckTimeout.getValue()).intValue() * 1000);
  }
}
