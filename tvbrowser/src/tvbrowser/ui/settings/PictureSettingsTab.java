/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (martin@tvbrowser.org)
 *
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
 */
package tvbrowser.ui.settings;

import java.awt.Color;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.SpinnerDateModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

import devplugin.Marker;
import devplugin.Plugin;
import devplugin.PluginAccess;
import devplugin.SettingsItem;
import tvbrowser.TVBrowser;
import tvbrowser.core.Settings;
import tvbrowser.core.filters.FilterList;
import tvbrowser.core.filters.GenericFilterMap;
import tvbrowser.core.filters.UserFilter;
import tvbrowser.extras.favoritesplugin.FavoritesPluginProxy;
import tvbrowser.extras.reminderplugin.ReminderPluginProxy;
import tvbrowser.ui.filter.dlgs.EditFilterDlg;
import tvbrowser.ui.mainframe.MainFrame;
import util.settings.PluginPictureSettings;
import util.settings.ProgramPanelSettings;
import util.ui.CaretPositionCorrector;
import util.ui.Localizer;
import util.ui.MarkerChooserDlg;
import util.ui.PluginsPictureSettingsPanel;
import util.ui.UiUtilities;

/**
 * The settings tab for the program panel picture settings.
 *
 * @author René Mach
 * @since 2.2.2
 */
public class PictureSettingsTab extends AbstractSettingsTab {
  private static final Localizer mLocalizer = Localizer.getLocalizerFor(PictureSettingsTab.class);

  private JRadioButton mShowPicturesEver, mShowPicturesNever, mShowPicturesForSelection, mShowPicturesForFilter;
  private JCheckBox mShowPicturesInTimeRange, mShowPicturesForDuration, mShowPicturesForPlugins;
  private JSpinner mPictureStartTime, mPictureEndTime, mDuration;
  private JLabel mStartLabel, mEndLabel;
  private JCheckBox mShowDescription;
  private JCheckBox mShowPictureBorderProgramTable;

  private JLabel mPluginLabel;
  private Marker[] mClientPlugins;

  private JButton choose;

  private PluginsPictureSettingsPanel mPluginsPictureSettings;

  private JSpinner mDescriptionLines;

  private JLabel mDescriptionLabel;

  private JTextArea mRestartMessage;
  
  private JButton mRestartButton;
  
  private SettingsDialog mSettingsDialog;
  
  private static int PLUGIN_PICTURE_SELECTION_ORIGINAL = -1;
    
  public PictureSettingsTab(SettingsDialog settingsDialog) {
    mSettingsDialog = settingsDialog;
  }

  public JPanel createSettingsPanel() {
    try {
      mShowPicturesNever = new JRadioButton(mLocalizer.msg("showNever", "Show never"), Settings.propPictureType.getInt() == ProgramPanelSettings.SHOW_PICTURES_NEVER);
      mShowPicturesEver = new JRadioButton(mLocalizer.msg("showEver", "Show always"), Settings.propPictureType.getInt() == ProgramPanelSettings.SHOW_PICTURES_EVER);
      mShowPicturesForSelection = new JRadioButton(mLocalizer.msg("showForSelection", "Selection..."), Settings.propPictureType.getInt() > 1 && Settings.propPictureType.getInt() < 10);
      mShowPicturesForFilter = new JRadioButton(mLocalizer.msg("showForFilter", "For filter..."), Settings.propPictureType.getInt() == ProgramPanelSettings.SHOW_PICTURES_FOR_FILTER);

      mShowPicturesInTimeRange = new JCheckBox(mLocalizer.msg("showInTimeRange", "Show in time range:"), ProgramPanelSettings.typeContainsType(Settings.propPictureType.getInt(), ProgramPanelSettings.SHOW_PICTURES_IN_TIME_RANGE));
      mShowPicturesForDuration = new JCheckBox(mLocalizer.msg("showForDuration", "Show for duration more than or equals to:"), ProgramPanelSettings.typeContainsType(Settings.propPictureType.getInt(), ProgramPanelSettings.SHOW_PICTURES_FOR_DURATION));

      mShowPictureBorderProgramTable = new JCheckBox(mLocalizer.msg("showPictureBorder","Show border around picture"), Settings.propShowProgramTablePictureBorder.getBoolean());
      
      ButtonGroup bg = new ButtonGroup();

      bg.add(mShowPicturesEver);
      bg.add(mShowPicturesNever);
      bg.add(mShowPicturesForSelection);
      bg.add(mShowPicturesForFilter);

      String timePattern = mLocalizer.msg("timePattern", "hh:mm a");

      mPictureStartTime = new JSpinner(new SpinnerDateModel());
      mPictureStartTime.setEditor(new JSpinner.DateEditor(mPictureStartTime, timePattern));
      CaretPositionCorrector.createCorrector(((JSpinner.DateEditor) mPictureStartTime.getEditor()).getTextField(), new char[]{':'}, -1);

      mPictureEndTime = new JSpinner(new SpinnerDateModel());
      mPictureEndTime.setEditor(new JSpinner.DateEditor(mPictureEndTime, timePattern));
      CaretPositionCorrector.createCorrector(((JSpinner.DateEditor) mPictureEndTime.getEditor()).getTextField(), new char[]{':'}, -1);

      mDuration = new JSpinner(new SpinnerNumberModel(Settings.propPictureDuration.getInt(), 10, 240, 1));

      Calendar cal = Calendar.getInstance();
      cal.set(Calendar.HOUR_OF_DAY, Settings.propPictureStartTime.getInt() / 60);
      cal.set(Calendar.MINUTE, Settings.propPictureStartTime.getInt() % 60);
      mPictureStartTime.setValue(cal.getTime());

      cal.set(Calendar.HOUR_OF_DAY, Settings.propPictureEndTime.getInt() / 60);
      cal.set(Calendar.MINUTE, Settings.propPictureEndTime.getInt() % 60);
      mPictureEndTime.setValue(cal.getTime());

      mShowDescription = new JCheckBox(mLocalizer.msg("showDescription", "Show description for pictures"), Settings.propIsPictureShowingDescription.getBoolean());
      mShowDescription.addItemListener(new ItemListener() {
        @Override
        public void itemStateChanged(ItemEvent e) {
          mShowPictureBorderProgramTable.setEnabled(e.getStateChange() == ItemEvent.DESELECTED);
        }
      });
      
      JEditorPane helpLabel = UiUtilities.createHtmlHelpTextArea(mLocalizer.msg("help", "These settings affect only the showing of the pictures. The pictures can only be shown if the download of pictures in enabled. To enable the picture download look at the <a href=\"#link\">settings of the TV dataservices</a>."), new HyperlinkListener() {
        public void hyperlinkUpdate(HyperlinkEvent e) {
          if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
            SettingsDialog.getInstance().showSettingsTab(SettingsItem.PLUGINS);
          }
        }
      });
      
      
      FormLayout layout = new FormLayout(
              "5dlu, 12dlu, 15dlu, default, 5dlu, default, 5dlu, default:grow, default, 5dlu",
              "default,5dlu,default,default,default,2dlu,default,default,2dlu,default," +
              "2dlu,default,default,5dlu,default,default,default,10dlu,default,5dlu,"+
              "default,10dlu,default,5dlu,fill:0dlu:grow,default");

      PanelBuilder pb = new PanelBuilder(layout/*, new ScrollableJPanel()*/);

      pb.border(Borders.DIALOG);

      int y = 1;

      pb.addSeparator(mLocalizer.msg("basics", "Picture settings for the program table"), CC.xyw(1, y, 10));

      pb.add(mShowPicturesNever, CC.xyw(2, y+=2, 9));
      pb.add(mShowPicturesEver, CC.xyw(2, y+=1, 9));
      pb.add(mShowPicturesForSelection, CC.xyw(2, y+=1, 9));

      pb.add(mShowPicturesInTimeRange, CC.xyw(3, y+=2, 8));
      mStartLabel = pb.addLabel(mLocalizer.msg("startTime", "From:"), CC.xy(4, y+=1));
      pb.add(mPictureStartTime, CC.xy(6, y));
      mEndLabel = pb.addLabel(mLocalizer.msg("endTime", "To:"), CC.xy(4, y+=2));
      pb.add(mPictureEndTime, CC.xy(6, y));

      pb.add(mShowPicturesForDuration, CC.xyw(3, y+=2, 8));
      pb.add(mDuration, CC.xy(6, y+=1));
      final JLabel minutesLabel = pb.addLabel(mLocalizer.msg("minutes", "Minutes"), CC.xy(8, y));
      y++;
      if (Settings.propPicturePluginIds.getStringArray() != null) {
        JPanel mSubPanel = new JPanel(new FormLayout("15dlu,pref:grow,5dlu,pref", "pref,2dlu,pref"));

        mShowPicturesForPlugins = new JCheckBox(mLocalizer.msg("showPicturesForPlugins", "Show for programs that are marked by plugins:"), ProgramPanelSettings.typeContainsType(Settings.propPictureType.getInt(), ProgramPanelSettings.SHOW_PICTURES_FOR_PLUGINS));
        mPluginLabel = new JLabel();
        mPluginLabel.setEnabled(ProgramPanelSettings.typeContainsType(Settings.propPictureType.getInt(), ProgramPanelSettings.SHOW_PICTURES_FOR_PLUGINS));

        choose = new JButton(mLocalizer.msg("selectPlugins", "Choose Plugins"));
        choose.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            Window parent = UiUtilities.getLastModalChildOf(MainFrame
                .getInstance());
            MarkerChooserDlg chooser = new MarkerChooserDlg(parent,
                mClientPlugins, null);

            chooser.setLocationRelativeTo(parent);
            chooser.setVisible(true);

            mClientPlugins = chooser.getMarker();

            handlePluginSelection();
          }
        });
        choose.setEnabled(ProgramPanelSettings.typeContainsType(Settings.propPictureType.getInt(), ProgramPanelSettings.SHOW_PICTURES_FOR_PLUGINS));

        mShowPicturesForPlugins.addItemListener(new ItemListener() {
          public void itemStateChanged(ItemEvent e) {
            mPluginLabel.setEnabled(e.getStateChange() == ItemEvent.SELECTED);
            choose.setEnabled(e.getStateChange() == ItemEvent.SELECTED);
          }
        });

        String[] clientPluginIdArr = Settings.propPicturePluginIds.getStringArray();

        ArrayList<Marker> clientPlugins = new ArrayList<Marker>();

        for (String arr : clientPluginIdArr) {
          PluginAccess plugin = Plugin.getPluginManager().getActivatedPluginForId(arr);
          if (plugin != null) {
            clientPlugins.add(plugin);
          } else if (ReminderPluginProxy.getInstance().getId().compareTo(arr) == 0) {
            clientPlugins.add(ReminderPluginProxy.getInstance());
          } else if (FavoritesPluginProxy.getInstance().getId().compareTo(arr) == 0) {
            clientPlugins.add(FavoritesPluginProxy.getInstance());
          }
        }

        mClientPlugins = clientPlugins.toArray(new Marker[clientPlugins.size()]);

        handlePluginSelection();

        mSubPanel.add(mShowPicturesForPlugins, CC.xyw(1, 1, 4));
        mSubPanel.add(mPluginLabel, CC.xy(2, 3));
        mSubPanel.add(choose, CC.xy(4, 3));

        layout.insertRow(y, RowSpec.decode("2dlu"));
        layout.insertRow(y+=1, RowSpec.decode("pref"));
        pb.add(mSubPanel, CC.xyw(3, y, 7));
        layout.insertRow(y+=1, RowSpec.decode("2dlu"));
        y++;
      }
      
      final JButton editFilter = new JButton(mLocalizer.msg("editFilter", "Edit filter"));
      editFilter.setEnabled(mShowPicturesForFilter.isSelected());
      editFilter.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
          final UserFilter filter = GenericFilterMap.getInstance().getGenericPictureFilter();
          
          final EditFilterDlg editFilter = new EditFilterDlg(UiUtilities.getLastModalChildOf(MainFrame.getInstance()), FilterList.getInstance(), filter, false);
          
          if(editFilter.getOkWasPressed()) {
            GenericFilterMap.getInstance().updateGenericPictureFilter(filter);
          }
        }
      });
      
      mShowPicturesForFilter.addItemListener(new ItemListener() {
        @Override
        public void itemStateChanged(ItemEvent e) {
          editFilter.setEnabled(e.getStateChange() == ItemEvent.SELECTED);
        }
      });
            
      layout.insertRow(y, RowSpec.decode("default"));
      
      pb.add(mShowPicturesForFilter, CC.xyw(2, y, 9));
      
      y++;
      
      layout.insertRow(y, RowSpec.decode("2dlu"));
      
      y++;
      
      layout.insertRow(y, RowSpec.decode("default"));
      
      pb.add(editFilter, CC.xyw(3, y++, 4));
      
      pb.add(mShowDescription, CC.xyw(2, y+=1, 9));

      mDescriptionLines = new JSpinner(new SpinnerNumberModel(Settings.propPictureDescriptionLines.getInt(), 1, 20, 1));
      pb.add(mDescriptionLines, CC.xyw(3, y+=1, 4));
      mDescriptionLabel = new JLabel(mLocalizer.msg("lines", "lines"));
  	  pb.add(mDescriptionLabel, CC.xy(8, y));
      pb.add(mShowPictureBorderProgramTable, CC.xyw(3,y+=1,8));
  	  mDescriptionLabel.setEnabled(mShowDescription.isSelected());
  	  mDescriptionLines.setEnabled(mShowDescription.isSelected());
  	  mShowPictureBorderProgramTable.setEnabled(!mShowDescription.isSelected());
  	  mShowDescription.addActionListener(new ActionListener() {
  
  		@Override
  		public void actionPerformed(ActionEvent e) {
  		  mDescriptionLines.setEnabled(mShowDescription.isSelected());
  		  mDescriptionLabel.setEnabled(mShowDescription.isSelected());
  		}});
    
      pb.addSeparator(mLocalizer.msg("pluginPictureTitle", "Default picture settings for the program lists of the Plugins"), CC.xyw(1, y+=2, 9));
      pb.add(mPluginsPictureSettings = new PluginsPictureSettingsPanel(new PluginPictureSettings(Settings.propPluginsPictureSetting.getInt()), true), CC.xyw(2, y+=2, 8));
      pb.add(helpLabel, CC.xyw(1, y+=2, 10));
      
      if(PLUGIN_PICTURE_SELECTION_ORIGINAL == -1) {
        PLUGIN_PICTURE_SELECTION_ORIGINAL = mPluginsPictureSettings.getSettings().getType();
      }
      
      mRestartMessage = UiUtilities.createHelpTextArea(mLocalizer.msg("restartNote", "Please Restart"));
      mRestartMessage.setForeground(Color.RED);
      mRestartMessage.setVisible(PLUGIN_PICTURE_SELECTION_ORIGINAL != mPluginsPictureSettings.getSettings().getType());
      
      mRestartButton = new JButton(mLocalizer.msg("restart", "Restart now"));
      mRestartButton.setVisible(PLUGIN_PICTURE_SELECTION_ORIGINAL != mPluginsPictureSettings.getSettings().getType());
      mRestartButton.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
          mSettingsDialog.saveSettings();
          TVBrowser.addRestart();
          MainFrame.getInstance().quit();
        }
      });
      
      mPluginsPictureSettings.addChangeListener(new ChangeListener() {
        @Override
        public void stateChanged(ChangeEvent e) {
          mRestartMessage.setVisible(PLUGIN_PICTURE_SELECTION_ORIGINAL != mPluginsPictureSettings.getSettings().getType());
          mRestartButton.setVisible(PLUGIN_PICTURE_SELECTION_ORIGINAL != mPluginsPictureSettings.getSettings().getType());
        }
      });
      
      y+=3;
      pb.add(mRestartMessage, CC.xyw(1, y, 8));
      pb.add(mRestartButton, CC.xy(9, y));
      
      mShowPicturesInTimeRange.addItemListener(new ItemListener() {
        public void itemStateChanged(ItemEvent e) {
          mPictureStartTime.setEnabled(mShowPicturesInTimeRange.isSelected());
          mPictureEndTime.setEnabled(mShowPicturesInTimeRange.isSelected());
          mStartLabel.setEnabled(mShowPicturesInTimeRange.isSelected());
          mEndLabel.setEnabled(mShowPicturesInTimeRange.isSelected());
        }
      });

      mShowPicturesForDuration.addItemListener(new ItemListener() {
        public void itemStateChanged(ItemEvent e) {
          mDuration.setEnabled(mShowPicturesForDuration.isSelected());
          minutesLabel.setEnabled(mShowPicturesForDuration.isSelected());
        }
      });

      mShowPicturesNever.addItemListener(new ItemListener() {
        public void itemStateChanged(ItemEvent e) {
          mShowDescription.setEnabled(!mShowPicturesNever.isSelected());
          mDescriptionLines.setEnabled(!mShowPicturesNever.isSelected() && mShowDescription.isSelected());
          mShowPictureBorderProgramTable.setEnabled(!mShowPicturesNever.isSelected() && !mShowDescription.isSelected());
        }
      });

      mShowPicturesForSelection.addItemListener(new ItemListener() {
        public void itemStateChanged(ItemEvent e) {
          mShowPicturesForDuration.setEnabled(mShowPicturesForSelection.isSelected());
          mShowPicturesInTimeRange.setEnabled(mShowPicturesForSelection.isSelected());
          mStartLabel.setEnabled(mShowPicturesForSelection.isSelected() && mShowPicturesInTimeRange.isSelected());
          mEndLabel.setEnabled(mShowPicturesForSelection.isSelected() && mShowPicturesInTimeRange.isSelected());
          minutesLabel.setEnabled(mShowPicturesForSelection.isSelected() && mShowPicturesForDuration.isSelected());
          mPictureStartTime.setEnabled(mShowPicturesForSelection.isSelected() && mShowPicturesInTimeRange.isSelected());
          mPictureEndTime.setEnabled(mShowPicturesForSelection.isSelected() && mShowPicturesInTimeRange.isSelected());
          mDuration.setEnabled(mShowPicturesForSelection.isSelected() && mShowPicturesForDuration.isSelected());
          
          if (mShowPicturesForPlugins != null) {
            mShowPicturesForPlugins.setEnabled(mShowPicturesForSelection.isSelected());
          }
          if (mPluginLabel != null) {
            mPluginLabel.setEnabled(mShowPicturesForSelection.isSelected() && mShowPicturesForPlugins.isSelected());
          }
          if (choose != null) {
            choose.setEnabled(mShowPicturesForSelection.isSelected() && mShowPicturesForPlugins.isSelected());
          }
        }
      });

      mShowPicturesInTimeRange.getItemListeners()[0].itemStateChanged(null);
      mShowPicturesForDuration.getItemListeners()[0].itemStateChanged(null);
      mShowPicturesForSelection.getItemListeners()[0].itemStateChanged(null);
      mShowPicturesNever.getItemListeners()[0].itemStateChanged(null);

      return pb.getPanel();

    } catch (Exception e) {
      e.printStackTrace();
    }


    return null;
  }

  @Override
  public Icon getIcon() {
    return getPictureIcon();
  }

  public String getTitle() {
    return Localizer.getLocalization(Localizer.I18N_PICTURES);
  }

  public void saveSettings() {
    Settings.propPictureType.setInt(getPictureShowingType());
    Settings.propPictureStartTime.setInt(getPictureTimeRangeStart());
    Settings.propPictureEndTime.setInt(getPictureTimeRangeEnd());
    Settings.propPictureDuration.setInt((Integer) mDuration.getValue());
    Settings.propIsPictureShowingDescription.setBoolean(mShowDescription.isSelected());
    
    if(!mShowDescription.isSelected()) {
      Settings.propShowProgramTablePictureBorder.setBoolean(mShowPictureBorderProgramTable.isSelected());
    }
    else {
      Settings.propShowProgramTablePictureBorder.setBoolean(true);
    }

    if (ProgramPanelSettings.typeContainsType(getPictureShowingType(), ProgramPanelSettings.SHOW_PICTURES_FOR_PLUGINS)) {
      Settings.propPicturePluginIds.setStringArray(getClientPluginIds());
    }

    Settings.propPluginsPictureSetting.setInt(mPluginsPictureSettings.getSettings().getType());
    Settings.propPictureDescriptionLines.setInt((Integer) mDescriptionLines.getValue());
  }

  /**
   * @since 2.6
   */
  private void handlePluginSelection() {
    if (mClientPlugins.length > 0) {
      mPluginLabel.setText(mClientPlugins[0].toString());
      mPluginLabel.setEnabled(true);
    } else {
      mPluginLabel.setText(mLocalizer.msg("noPlugins", "No Plugins choosen"));
      mPluginLabel.setEnabled(false);
    }

    for (int i = 1; i < (mClientPlugins.length > 4 ? 3 : mClientPlugins.length); i++) {
      mPluginLabel.setText(mPluginLabel.getText() + ", " + mClientPlugins[i]);
    }

    if (mClientPlugins.length > 4) {
      mPluginLabel.setText(mPluginLabel.getText() + " (" + (mClientPlugins.length - 3) + " " + mLocalizer.ellipsisMsg("otherPlugins", "others") + ")");
    }
  }

  /**
   * @return The picture showing type of this settings
   * @since 2.6
   */
  private int getPictureShowingType() {
    int value = ProgramPanelSettings.SHOW_PICTURES_NEVER;

    if (mShowPicturesEver.isSelected()) {
      value = ProgramPanelSettings.SHOW_PICTURES_EVER;
    } else if (mShowPicturesForSelection.isSelected()) {
      if (mShowPicturesForDuration.isSelected()) {
        value += ProgramPanelSettings.SHOW_PICTURES_FOR_DURATION;
      }
      if (mShowPicturesForPlugins != null && mShowPicturesForPlugins.isSelected() && mClientPlugins != null && mClientPlugins.length > 0) {
        value += ProgramPanelSettings.SHOW_PICTURES_FOR_PLUGINS;
      }
      if (mShowPicturesInTimeRange.isSelected()) {
        value += ProgramPanelSettings.SHOW_PICTURES_IN_TIME_RANGE;
      }
    } else if(mShowPicturesForFilter.isSelected()) {
      value = ProgramPanelSettings.SHOW_PICTURES_FOR_FILTER;
    }

    return value;
  }

  /**
   * @return The time range start time.
   * @since 2.6
   */
  private int getPictureTimeRangeStart() {
    Calendar cal = Calendar.getInstance();
    Date startTime = (Date) mPictureStartTime.getValue();
    cal.setTime(startTime);
    return cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE);
  }

  /**
   * @return The time range end time.
   * @since 2.6
   */
  private int getPictureTimeRangeEnd() {
    Calendar cal = Calendar.getInstance();
    Date startTime = (Date) mPictureEndTime.getValue();
    cal.setTime(startTime);
    return cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE);
  }

  /**
   * @return The selected client plugins.
   * @since 2.6
   */
  private String[] getClientPluginIds() {
    if (mShowPicturesForPlugins != null) {
      String[] clientPluginIdArr = new String[mClientPlugins.length];

      for (int i = 0; i < mClientPlugins.length; i++) {
        clientPluginIdArr[i] = mClientPlugins[i].getId();
      }

      return clientPluginIdArr;
    }

    return null;
  }
}
