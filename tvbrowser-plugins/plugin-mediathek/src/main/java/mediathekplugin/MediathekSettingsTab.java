/*
 * Copyright Michael Keppler
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package mediathekplugin;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import util.ui.EnhancedPanelBuilder;
import util.ui.Localizer;
import util.ui.UiUtilities;

import com.jgoodies.forms.layout.CellConstraints;

import devplugin.SettingsTab;

public final class MediathekSettingsTab implements SettingsTab {

  private static final Localizer localizer = Localizer
      .getLocalizerFor(MediathekSettingsTab.class);
  private MediathekSettings mSettings;
  private JTextField mPath;

  public JPanel createSettingsPanel() {
    final CellConstraints cc = new CellConstraints();
    EnhancedPanelBuilder panelBuilder = new EnhancedPanelBuilder("5dlu, default, 3dlu, default, fill:default:grow, 3dlu, default");

    panelBuilder.addRow();
    JEditorPane help = UiUtilities.createHtmlHelpTextArea(localizer.msg("help", "The <a href=\"{0}\">Mediathek</a> application needs to be installed.", "http://zdfmediathk.sourceforge.net/"));
    panelBuilder.add(help, cc.xyw(2, panelBuilder.getRowCount(), 6));

    panelBuilder.addRow();
    JLabel label = new JLabel(localizer.msg("path", "Mediathek installation path"));
    panelBuilder.add(label, cc.xy(2, panelBuilder.getRowCount()));

    mPath = new JTextField(mSettings.getMediathekPath());
    panelBuilder.add(mPath, cc.xyw(4, panelBuilder.getRowCount(), 2));
    
    JButton select = new JButton(Localizer.getLocalization(Localizer.I18N_SELECT));
    select.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        String value = mSettings.guessMediathekPath(false);
        
        if(value.trim().length() == 0) {
          JFileChooser choose = new JFileChooser(new File(System.getProperty("user.home")));
          choose.setFileSelectionMode(JFileChooser.FILES_ONLY);
          choose.showDialog(UiUtilities.getLastModalChildOf(MediathekPlugin.getInstance().getFrame()), Localizer.getLocalization(Localizer.I18N_SELECT));
          
          if(choose.getSelectedFile() != null && choose.getSelectedFile().getName().equals(".filme")) {
            mPath.setText(choose.getSelectedFile().getAbsolutePath());
          }
        }
        else {
          mPath.setText(value);
          JOptionPane.showMessageDialog(UiUtilities.getLastModalChildOf(MediathekPlugin.getInstance().getFrame()), localizer.msg("selectionOk", "File successfully located."));
        }
      }
    });
    
    panelBuilder.add(select, cc.xy(7,panelBuilder.getRowCount()));
    
    return panelBuilder.getPanel();
  }

  public Icon getIcon() {
    return MediathekPlugin.getInstance().getPluginIcon();
  }

  public String getTitle() {
    return localizer.msg("title", "Mediathek");
  }

  public void saveSettings() {
    mSettings.setMediathekPath(mPath.getText().trim());
    MediathekPlugin.getInstance().readMediathekContents();
  }

  public MediathekSettingsTab(final MediathekSettings settings) {
    this.mSettings = settings;
  }
}
