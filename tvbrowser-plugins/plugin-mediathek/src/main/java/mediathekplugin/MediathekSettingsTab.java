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
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

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
  private JComboBox<MediathekQuality> mQuality;
  
  private JCheckBox mAutoUpdate;
  private JSpinner mAutoUpdateInterval;
  private JTextField mProgramPath;
  private JLabel mLabelUpdateInterval;
  private JLabel mLabelProgramPath;
  private JButton mSelectProgram;
  
  
  public JPanel createSettingsPanel() {
    final CellConstraints cc = new CellConstraints();
    EnhancedPanelBuilder panelBuilder = new EnhancedPanelBuilder("5dlu, default, 3dlu, default, fill:default:grow, 3dlu, default");

    panelBuilder.addRow();
    JEditorPane help = UiUtilities.createHtmlHelpTextArea(localizer.msg("help", "The <a href=\"{0}\">Mediathek</a> application needs to be installed.", "https://mediathekview.de/"));
    panelBuilder.add(help, cc.xyw(2, panelBuilder.getRowCount(), 6));

    panelBuilder.addRow();
    JLabel labelPath = new JLabel(localizer.msg("path", "Mediathek data path"));
    panelBuilder.add(labelPath, cc.xy(2, panelBuilder.getRowCount()));

    mPath = new JTextField(mSettings.getMediathekPath());
    panelBuilder.add(mPath, cc.xyw(4, panelBuilder.getRowCount(), 2));
    
    JButton select = new JButton(Localizer.getLocalization(Localizer.I18N_SELECT));
    select.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        File path = new File(mSettings.getMediathekPath());
        if (path.exists()) {
          path = path.getParentFile();
        } else {
          path = new File(System.getProperty("user.home"));
        }
        JFileChooser choose = new JFileChooser(path);
        choose.setFileSelectionMode(JFileChooser.FILES_ONLY);
        choose.showDialog(UiUtilities.getLastModalChildOf(MediathekPlugin.getInstance().getFrame()), Localizer.getLocalization(Localizer.I18N_SELECT));
        
        if(choose.getSelectedFile() != null && choose.getSelectedFile().getName().equals("filme.json")) {
          mPath.setText(choose.getSelectedFile().getAbsolutePath());
        }
      }
    });
    
    panelBuilder.add(select, cc.xy(7,panelBuilder.getRowCount()));
    
    panelBuilder.addRow();
    JLabel labelQuality = new JLabel(localizer.msg("quality", "Quality"));
    panelBuilder.add(labelQuality, cc.xy(2, panelBuilder.getRowCount()));
    
    mQuality = new JComboBox<MediathekQuality>(MediathekQuality.values());
    mQuality.setSelectedItem(mSettings.getMediathekQuality());
    panelBuilder.add(mQuality, cc.xyw(4, panelBuilder.getRowCount(), 2));

    panelBuilder.addRow();
    panelBuilder.addSeparator(localizer.msg("autoupdate", "Autoupdate"));

    panelBuilder.addRow();
    mAutoUpdate = new JCheckBox(localizer.msg("enableautoupdate", "Enable Autoupdate"), mSettings.getMediathekUpdateInterval()>0);
    panelBuilder.add(mAutoUpdate, cc.xyw(2, panelBuilder.getRowCount(), 6));
    
    panelBuilder.addRow();
    mLabelUpdateInterval = new JLabel(localizer.msg("autoupdateinterval", "Autoupdate Interval (minutes)"));
    mLabelUpdateInterval.setEnabled(mAutoUpdate.isSelected());
    panelBuilder.add(mLabelUpdateInterval, cc.xy(2, panelBuilder.getRowCount()));
    mAutoUpdateInterval = new JSpinner(new SpinnerNumberModel(Math.abs(mSettings.getMediathekUpdateInterval()),15,600,15));
    mAutoUpdateInterval.setEnabled(mAutoUpdate.isSelected());
    panelBuilder.add(mAutoUpdateInterval, cc.xyw(4, panelBuilder.getRowCount(), 2));
    
    mAutoUpdate.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        mLabelUpdateInterval.setEnabled(e.getStateChange() == ItemEvent.SELECTED);
        mAutoUpdateInterval.setEnabled(e.getStateChange() == ItemEvent.SELECTED);
      }
    });
    
    
    panelBuilder.addRow();
    mLabelProgramPath = new JLabel(localizer.msg("programpath", "Mediathek installation path"));
    panelBuilder.add(mLabelProgramPath, cc.xy(2, panelBuilder.getRowCount()));
    mProgramPath = new JTextField(mSettings.getMediathekProgramPath());
    panelBuilder.add(mProgramPath, cc.xyw(4, panelBuilder.getRowCount(), 2));    
    mSelectProgram = new JButton(Localizer.getLocalization(Localizer.I18N_SELECT));
    mSelectProgram.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        File path = new File(mSettings.getMediathekProgramPath());
        if (path.exists()) {
          path = path.getParentFile();
        } else {
          path = new File(System.getProperty("user.home"));
        }
        JFileChooser choose = new JFileChooser(path);
        choose.setFileSelectionMode(JFileChooser.FILES_ONLY);
        choose.showDialog(UiUtilities.getLastModalChildOf(MediathekPlugin.getInstance().getFrame()), Localizer.getLocalization(Localizer.I18N_SELECT));
        
        if(choose.getSelectedFile() != null && choose.getSelectedFile().getName().equals("MediathekView.jar")) {
          mProgramPath.setText(choose.getSelectedFile().getAbsolutePath());
        }
      }
    }); 
    panelBuilder.add(mSelectProgram, cc.xy(7,panelBuilder.getRowCount()));
    
    panelBuilder.addRow();
    JButton update = new JButton(localizer.msg("updatemediathek", "Update Mediathek"));
    update.addActionListener(new ActionListener(){
      public void actionPerformed(final ActionEvent e) {
        MediathekPlugin.getInstance().startMediathekUpdate();
      }
    });
    panelBuilder.add(update, cc.xyw(2,panelBuilder.getRow(), 3));
    
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
    mSettings.setMediathekQuality((MediathekQuality) mQuality.getSelectedItem());
    int interval = 30;
    try {
      interval = Integer.parseInt(mAutoUpdateInterval.getValue().toString());
    } catch (NumberFormatException e) {      
    }
    
    if (!mAutoUpdate.isSelected()) {
      interval = -interval;
    }    
    mSettings.setMediathekUpdateInterval(interval);
    mSettings.setMediathekProgramPath(mProgramPath.getText().trim());
    MediathekPlugin.getInstance().settingsChanged();
  }

  public MediathekSettingsTab(final MediathekSettings settings) {
    this.mSettings = settings;
  }
}
