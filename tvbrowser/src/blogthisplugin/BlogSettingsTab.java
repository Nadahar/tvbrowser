/*
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 * CVS information:
 *  $RCSfile$
 *   $Source$
 *     $Date$
 *   $Author$
 * $Revision$
 */
package blogthisplugin;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Properties;

import javax.swing.Icon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import util.ui.Localizer;
import util.ui.PluginProgramConfigurationPanel;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import devplugin.SettingsTab;

/**
 * Settings for the BlogSettings
 * 
 * @author bodum
 */
public class BlogSettingsTab implements SettingsTab {
    /** Translator */
    private static final Localizer mLocalizer = Localizer
            .getLocalizerFor(BlogSettingsTab.class);

    /** Plugin */
    private BlogThisPlugin mPlugin;

    /** Settings for this Plugin */
    private Properties mSettings;

    /** ComboBox for Blog-Service */
    private JComboBox mServiceCombo;

    /** Label for the Url-Inputfield */
    private JLabel mServiceUrlLabel;

    /** Textfield for Url */
    private JTextField mServiceUrlField;
    
    private PluginProgramConfigurationPanel mConfigPanel;

    /**
     * Create Plugin-Settingstab
     * 
     * @param plugin Plugin
     * @param settings Settings of this Plugin
     */
    public BlogSettingsTab(BlogThisPlugin plugin, Properties settings) {
        mPlugin = plugin;
        mSettings = settings;
    }

    /**
     * Create the SettingsPanel
     */
    public JPanel createSettingsPanel() {
        final JPanel settingsPanel = new JPanel(new FormLayout(
                "5dlu, pref, 3dlu, pref, fill:pref:grow",
                "pref, 3dlu, pref, 10dlu, fill:default:grow"));
        settingsPanel.setBorder(Borders.DLU4_BORDER);

        String[] services = { "", "Blogger.com", "Wordpress",
                "b2evolution" };

        mServiceCombo = new JComboBox(services);

        if (mSettings.getProperty("BlogService", "").equals(
                BlogThisPlugin.BLOGGER)) {
            mServiceCombo.setSelectedIndex(1);
        } else if (mSettings.getProperty("BlogService", "").equals(
                BlogThisPlugin.WORDPRESS)) {
            mServiceCombo.setSelectedIndex(2);
        } else if (mSettings.getProperty("BlogService", "").equals(
                BlogThisPlugin.B2EVOLUTION)) {
            mServiceCombo.setSelectedIndex(3);
        }

        mServiceCombo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                updateAfterServiceSelection();
            }
        });

        mServiceUrlLabel = new JLabel(mLocalizer.msg("Url", "Url")+":");
        mServiceUrlLabel.setEnabled(mServiceCombo.getSelectedIndex() >= 2);

        mServiceUrlField = new JTextField();
        mServiceUrlField.setEnabled(mServiceCombo.getSelectedIndex() >= 2);
        mServiceUrlField.setText(mSettings.getProperty("BlogUrl"));

        CellConstraints cc = new CellConstraints();
        settingsPanel.add(new JLabel(mLocalizer.msg("Service", "Blog-Service")+":"), cc.xy(2, 1));
        settingsPanel.add(mServiceCombo, cc.xy(4, 1));
        settingsPanel.add(mServiceUrlLabel, cc.xy(2, 3));
        settingsPanel.add(mServiceUrlField, cc.xyw(4, 3, 2));

        mConfigPanel = new PluginProgramConfigurationPanel(mPlugin.getSelectedPluginProgramFormatings(), mPlugin.getAvailableLocalPluginProgramFormatings(), BlogThisPlugin.getDefaultFormating(),true,false);
        
        /*JButton extended = new JButton(mLocalizer.msg("Extended", "Extended Settings"));
        
        extended.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            showExtendedDialog(settingsPanel);
          }
        });*/
        
        settingsPanel.add(mConfigPanel, cc.xyw(1, 5, 5));

        return settingsPanel;
    }

    /**
     * Updates the GUI after a new Service is selected
     */
    private void updateAfterServiceSelection() {
        mServiceUrlLabel.setEnabled(mServiceCombo.getSelectedIndex() >= 2);
        mServiceUrlField.setEnabled(mServiceCombo.getSelectedIndex() >= 2);

        if (mServiceCombo.getSelectedIndex() == 2) {
            mServiceUrlField.setText(BlogThisPlugin.URL_WORDPRESS);
        } else if (mServiceCombo.getSelectedIndex() == 3) {
            mServiceUrlField.setText(BlogThisPlugin.URL_B2EVOLUTION);
        } else {
            mServiceUrlField.setText("");
        }
    }
    
    /**
     * Saves the Settings
     */
    public void saveSettings() {
        switch (mServiceCombo.getSelectedIndex()) {
        case 1:
            mSettings.setProperty("BlogService", BlogThisPlugin.BLOGGER);
            break;
        case 2:
            mSettings.setProperty("BlogService", BlogThisPlugin.WORDPRESS);
            break;
        case 3:
            mSettings.setProperty("BlogService", BlogThisPlugin.B2EVOLUTION);
            break;

        default:
            mSettings.remove("BlogService");
            break;
        }
        mSettings.setProperty("BlogUrl", mServiceUrlField.getText());
        
        mPlugin.setAvailableLocalPluginProgramFormatings(mConfigPanel.getAvailableLocalPluginProgramFormatings());
        mPlugin.setSelectedPluginProgramFormatings(mConfigPanel.getSelectedPluginProgramFormatings());
    }

    /*
     * (non-Javadoc)
     * 
     * @see devplugin.SettingsTab#getIcon()
     */
    public Icon getIcon() {
        return mPlugin.createImageIcon("apps", "internet-web-browser", 16);
    }

    /*
     * (non-Javadoc)
     * 
     * @see devplugin.SettingsTab#getTitle()
     */
    public String getTitle() {
        return mLocalizer.msg("Name", "Blog this!");
    }

}
