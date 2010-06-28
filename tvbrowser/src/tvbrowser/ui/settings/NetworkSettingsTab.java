package tvbrowser.ui.settings;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import tvbrowser.core.Settings;
import tvbrowser.core.icontheme.IconLoader;
import util.io.NetworkUtilities;
import util.ui.EnhancedPanelBuilder;

import com.jgoodies.forms.layout.CellConstraints;

import devplugin.SettingsTab;

/**
 * Settings for network stuff.
 * 
 * @author René Mach
 * @since 2.5.1
 */
public class NetworkSettingsTab implements SettingsTab {
  private static final util.ui.Localizer mLocalizer = util.ui.Localizer
  .getLocalizerFor(NetworkSettingsTab.class);
  
  private JSpinner mConnectionTimeout, mNetworkCheckTimeout;
  
  private JCheckBox mConnectionTest;
  
  public JPanel createSettingsPanel() {
    EnhancedPanelBuilder pb = new EnhancedPanelBuilder("5dlu, pref, 3dlu, 0dlu:grow");
    pb.setDefaultDialogBorder();
    
    CellConstraints cc = new CellConstraints();
    
    pb.addParagraph(mLocalizer.msg("connectionTestTitle","Internet connection test"));
    
    pb.addRow();
    pb.add(mConnectionTest = new JCheckBox(mLocalizer.msg("connectionTestText","Internet connection test activated"), Settings.propInternetConnectionCheck.getBoolean()), cc.xyw(2, pb.getRowCount(), 3));

    pb.addRow();
    pb.add(mNetworkCheckTimeout = new JSpinner(new SpinnerNumberModel(Settings.propNetworkCheckTimeout.getInt()/1000,10,90,5)), cc.xy(2, pb.getRowCount()));
    final JLabel label = pb.addLabel(mLocalizer.msg("waitTime","Seconds maximum waiting time for connection test"), cc.xy(4, pb.getRowCount()));
    
    pb.addRow();
    pb.add(new JLabel(mLocalizer.msg("sites", "Websites used for checking")), cc.xyw(2, pb.getRowCount(), 3));
    
    pb.addRow();
    final JList urlList = new JList(NetworkUtilities.getConnectionCheckUrls());
    urlList.setEnabled(false);
    pb.add(new JScrollPane(urlList), cc.xyw(2, pb.getRowCount(), 3));
    
    mConnectionTest.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        boolean enabled = e.getStateChange() == ItemEvent.SELECTED;
        mNetworkCheckTimeout.setEnabled(enabled);
        label.setEnabled(enabled);
      }
    });
    
    mNetworkCheckTimeout.setEnabled(mConnectionTest.isSelected());
    label.setEnabled(mConnectionTest.isSelected());
    
    pb.addParagraph(mLocalizer.msg("cancelTime","Timeout for not responding connections"));
    
    pb.addRow();
    pb.add(mConnectionTimeout = new JSpinner(new SpinnerNumberModel(Settings.propDefaultNetworkConnectionTimeout.getInt()/1000,5,60,5)), cc.xy(2, pb.getRowCount()));
    pb.addLabel(mLocalizer.msg("seconds","Seconds"), cc.xy(4, pb.getRowCount()));
    
    return pb.getPanel();
  }

  public Icon getIcon() {
    return IconLoader.getInstance().getIconFromTheme("status", "network-transmit-receive", 16);
  }

  public String getTitle() {
    return mLocalizer.msg("title","Network");
  }

  public void saveSettings() {
    Settings.propInternetConnectionCheck.setBoolean(mConnectionTest.isSelected());
    Settings.propDefaultNetworkConnectionTimeout.setInt(((Integer)mConnectionTimeout.getValue()).intValue() * 1000);
    Settings.propNetworkCheckTimeout.setInt(((Integer)mNetworkCheckTimeout.getValue()).intValue() * 1000);
  }
}
