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
package util.ui;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.Scrollable;
import javax.swing.SpinnerDateModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import tvbrowser.extras.favoritesplugin.FavoritesPluginProxy;
import tvbrowser.extras.reminderplugin.ReminderPluginProxy;
import tvbrowser.ui.mainframe.MainFrame;
import tvbrowser.ui.settings.SettingsDialog;
import util.settings.PluginPictureSettings;
import util.settings.ProgramPanelSettings;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

import devplugin.Marker;
import devplugin.Plugin;
import devplugin.PluginAccess;
import devplugin.SettingsItem;

/**
 * A class that contains settings components for picture settings.
 * 
 * @author René Mach
 * @since 2.2.2
 * @deprecated since 2.6 Use {@link PluginPictureSettings instead}
 */
public class PictureSettingsPanel extends JPanel implements Scrollable {
  private static final long serialVersionUID = 1L;

  /** The localizer for this class */
  public static final Localizer mLocalizer = Localizer.getLocalizerFor(PictureSettingsPanel.class);

  /** Show the pictures never 
   * @deprecated since 2.6 Use {@link ProgramPanelSettings#SHOW_PICTURES_NEVER}*/
  public static final int SHOW_NEVER = ProgramPanelSettings.SHOW_PICTURES_NEVER;
  /** Always show the pictures 
   * @deprecated since 2.6 Use {@link ProgramPanelSettings#SHOW_PICTURES_EVER}*/
  public static final int SHOW_EVER = ProgramPanelSettings.SHOW_PICTURES_EVER;
  /** Show the pictures in time range
   * @deprecated since 2.6 Use {@link ProgramPanelSettings#SHOW_PICTURES_IN_TIME_RANGE}*/
  public static final int SHOW_IN_TIME_RANGE = ProgramPanelSettings.SHOW_PICTURES_IN_TIME_RANGE;
  /** Show the pictures for selected plugins 
    * @deprecated since 2.6 Use {@link ProgramPanelSettings#SHOW_PICTURES_FOR_PLUGINS}*/
  public static final int SHOW_FOR_PLUGINS = ProgramPanelSettings.SHOW_PICTURES_FOR_PLUGINS;
  /** Show the pictures for programs with selected duration
   * @deprecated since 2.6 Use {@link ProgramPanelSettings#SHOW_PICTURES_FOR_DURATION}*/
  public static final int SHOW_FOR_DURATION = ProgramPanelSettings.SHOW_PICTURES_FOR_DURATION;
  
  private JRadioButton mShowPicturesEver, mShowPicturesNever, mShowPicturesForSelection;
  private JCheckBox mShowPicturesInTimeRange, mShowPicturesForDuration, mShowPicturesForPlugins;
  private JSpinner mPictureStartTime, mPictureEndTime, mDuration;
  private JLabel mStartLabel, mEndLabel;
  private JCheckBox mShowDescription;
    
  private JLabel mPluginLabel;
  private Marker[] mClientPlugins;
  
  private JEditorPane mHelpLabel;
  private JButton choose;

  /**
   * Creates an instance of this class.
   * 
   * @param settings The current picture settings
   * @param showTitle Show the title in this panel.
   * @param addBorder If the panel should contains an empty border.
   * @param additionalPanel A JPanel with additional options.
   */
  public PictureSettingsPanel(ProgramPanelSettings settings, boolean showTitle, boolean addBorder, JPanel additionalPanel) {
    this(settings.getPictureShowingType(), settings.getPictureTimeRangeStart(), settings.getPictureTimeRangeEnd(), settings.isShowingPictureDescription(), showTitle, addBorder, settings.getDuration(), settings.getPluginIds(), additionalPanel);
  }
  
  /**
   * Creates an instance of this class.
   * 
   * @param settings The current picture settings
   * @param showTitle Show the title in this panel.
   * @param addBorder If the panel should contains an empty border.
   */
  public PictureSettingsPanel(ProgramPanelSettings settings, boolean showTitle, boolean addBorder) {
    this(settings.getPictureShowingType(), settings.getPictureTimeRangeStart(), settings.getPictureTimeRangeEnd(), settings.isShowingPictureDescription(), showTitle, addBorder, settings.getDuration(), settings.getPluginIds(), null);
  }
  
  /**
   * Creates an instance of this class.
   * 
   * @param type The picture showing type.
   * @param timeRangeStart The range start time.
   * @param timeRangeEnd Time range end time.
   * @param showDescription Show the picture description.
   * @param showTitle Show the title in this panel.
   * @param addBorder If the panel should contains an empty border.
   * @param duration The duration in minutes programs should have to show pictures.  
   * @param clientPluginIds The ids of the plugins that mark a program that should show pictures then.
   * @param additionalPanel A panel with additional settings.
   */
  public PictureSettingsPanel(int type, int timeRangeStart, int timeRangeEnd, boolean showDescription, boolean showTitle, boolean addBorder, int duration, String[] clientPluginIds, JPanel additionalPanel) {
    try {
    mShowPicturesNever = new JRadioButton(mLocalizer.msg("showNever","Show never"), type == ProgramPanelSettings.SHOW_PICTURES_NEVER);
    mShowPicturesEver = new JRadioButton(mLocalizer.msg("showEver","Show always"), type == ProgramPanelSettings.SHOW_PICTURES_EVER);
    mShowPicturesForSelection = new JRadioButton(mLocalizer.msg("showForSelection","Selection..."), type > 1);
    
    mShowPicturesInTimeRange = new JCheckBox(mLocalizer.msg("showInTimeRange","Show in time range:"), typeContainsType(type,ProgramPanelSettings.SHOW_PICTURES_IN_TIME_RANGE));
    mShowPicturesForDuration = new JCheckBox(mLocalizer.msg("showForDuration","Show for duration more than or equals to:"), typeContainsType(type,ProgramPanelSettings.SHOW_PICTURES_FOR_DURATION));
    
    ButtonGroup bg = new ButtonGroup();
        
    bg.add(mShowPicturesEver);
    bg.add(mShowPicturesNever);
    bg.add(mShowPicturesForSelection);
    
    String timePattern = mLocalizer.msg("timePattern","hh:mm a");       
    
    mPictureStartTime = new JSpinner(new SpinnerDateModel());
    mPictureStartTime.setEditor(new JSpinner.DateEditor(mPictureStartTime, timePattern));
    CaretPositionCorrector.createCorrector(((JSpinner.DateEditor)mPictureStartTime.getEditor()).getTextField(), new char[] {':'}, -1);
    
    mPictureEndTime = new JSpinner(new SpinnerDateModel());
    mPictureEndTime.setEditor(new JSpinner.DateEditor(mPictureEndTime, timePattern));
    CaretPositionCorrector.createCorrector(((JSpinner.DateEditor)mPictureEndTime.getEditor()).getTextField(), new char[] {':'}, -1);

    mDuration = new JSpinner(new SpinnerNumberModel(duration,10,240,1));
    
    Calendar cal = Calendar.getInstance();
    cal.set(Calendar.HOUR_OF_DAY, timeRangeStart / 60);
    cal.set(Calendar.MINUTE, timeRangeStart % 60);
    mPictureStartTime.setValue(cal.getTime());

    cal.set(Calendar.HOUR_OF_DAY, timeRangeEnd / 60);
    cal.set(Calendar.MINUTE, timeRangeEnd % 60);
    mPictureEndTime.setValue(cal.getTime());
        
    mShowDescription = new JCheckBox(mLocalizer.msg("showDescription","Show description for pictures"), showDescription);
    
    mHelpLabel = UiUtilities.createHtmlHelpTextArea(mLocalizer.msg("help","These settings affect only the showing of the pictures. The pictures can only be shown if the download of pictures in enabled. To enable the picture download look at the <a href=\"#link\">settings of the TV dataservices</a>."), new HyperlinkListener() {
      public void hyperlinkUpdate(HyperlinkEvent e) {
        if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
          SettingsDialog.getInstance().showSettingsTab(SettingsItem.TVDATASERVICES);
        }
      }
    });
    
    CellConstraints cc = new CellConstraints();
    
    FormLayout layout = new FormLayout(
        "5dlu, 12dlu, 15dlu, pref, 5dlu, pref, 5dlu, pref:grow, 5dlu",
        "pref,pref,pref,2dlu,pref,pref,2dlu,pref,2dlu,pref,pref,10dlu,pref,5dlu,pref,fill:10dlu:grow,pref,5dlu");
    
    PanelBuilder pb = new PanelBuilder(layout, this);
    
    if(addBorder)
      pb.setDefaultDialogBorder();
    
    int y = 1;
    
    if(showTitle) {
      layout.insertRow(1,new RowSpec("5dlu"));
      layout.insertRow(1,new RowSpec("pref"));
    
      pb.addSeparator(mLocalizer.msg("basics","Basic picture settings"), cc.xyw(1,y,9));
      y += 2;
    }
    
    pb.add(mShowPicturesNever, cc.xyw(2,y++,8));
    pb.add(mShowPicturesEver, cc.xyw(2,y++,8));
    pb.add(mShowPicturesForSelection, cc.xyw(2,y++,8));
    
    pb.add(mShowPicturesInTimeRange, cc.xyw(3,++y,7));
    mStartLabel = pb.addLabel(mLocalizer.msg("startTime","From:"), cc.xy(4,++y));
    pb.add(mPictureStartTime, cc.xy(6,y++));
    mEndLabel = pb.addLabel(mLocalizer.msg("endTime","To:"), cc.xy(4,++y));
    pb.add(mPictureEndTime, cc.xy(6,y++));
    
    pb.add(mShowPicturesForDuration, cc.xyw(3,++y,7));
    pb.add(mDuration, cc.xy(6,++y));
    final JLabel minutesLabel = pb.addLabel(mLocalizer.msg("minutes","Minutes"), cc.xy(8,y++));
    
    if(clientPluginIds != null) {
      JPanel mSubPanel = new JPanel(new FormLayout("15dlu,pref:grow,5dlu,pref","pref,2dlu,pref"));
      
      mShowPicturesForPlugins = new JCheckBox(mLocalizer.msg("showPicturesForPlugins","Show for programs that are marked by plugins:"), typeContainsType(type,ProgramPanelSettings.SHOW_PICTURES_FOR_PLUGINS));        
      mPluginLabel = new JLabel();
      mPluginLabel.setEnabled(typeContainsType(type,ProgramPanelSettings.SHOW_PICTURES_FOR_PLUGINS));
      
      choose = new JButton(mLocalizer.msg("selectPlugins","Choose Plugins"));
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
      choose.setEnabled(typeContainsType(type,ProgramPanelSettings.SHOW_PICTURES_FOR_PLUGINS));
      
      mShowPicturesForPlugins.addItemListener(new ItemListener() {
        public void itemStateChanged(ItemEvent e) {
          mPluginLabel.setEnabled(e.getStateChange() == ItemEvent.SELECTED);
          choose.setEnabled(e.getStateChange() == ItemEvent.SELECTED);
        }
      });
      
      String[] clientPluginIdArr = clientPluginIds;    
      
      ArrayList<Marker> clientPlugins = new ArrayList<Marker>();
      
      for(int i = 0; i < clientPluginIdArr.length; i++) {
        PluginAccess plugin = Plugin.getPluginManager().getActivatedPluginForId(clientPluginIdArr[i]);
        if(plugin != null)
          clientPlugins.add(plugin);
        else if(ReminderPluginProxy.getInstance().getId().compareTo(clientPluginIdArr[i]) == 0)
          clientPlugins.add(ReminderPluginProxy.getInstance());
        else if(FavoritesPluginProxy.getInstance().getId().compareTo(clientPluginIdArr[i]) == 0)
          clientPlugins.add(FavoritesPluginProxy.getInstance());
      }
      
      mClientPlugins = clientPlugins.toArray(new Marker[clientPlugins.size()]);
      
      handlePluginSelection();
      
      mSubPanel.add(mShowPicturesForPlugins, cc.xyw(1,1,4));
      mSubPanel.add(mPluginLabel, cc.xy(2,3));
      mSubPanel.add(choose, cc.xy(4,3));      
      
      layout.insertRow(y,new RowSpec("2dlu"));
      layout.insertRow(++y,new RowSpec("pref"));
      pb.add(mSubPanel, cc.xyw(3,y,6));      
      layout.insertRow(++y,new RowSpec("2dlu"));
      y++;
    }
    
    pb.addSeparator(mLocalizer.msg("options","Picture options"), cc.xyw(1,++y,9));y++;
    pb.add(mShowDescription, cc.xyw(2,++y,8));
    
    if(additionalPanel != null) {      
      layout.insertRow(++y,new RowSpec("pref"));
      pb.add(additionalPanel, cc.xyw(1,y,9));      
    }
    
    pb.add(mHelpLabel, cc.xyw(2,y+2,8));
    
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
        
        if(mShowPicturesForPlugins != null)
          mShowPicturesForPlugins.setEnabled(mShowPicturesForSelection.isSelected());
        if(mPluginLabel != null)
          mPluginLabel.setEnabled(mShowPicturesForSelection.isSelected() && mShowPicturesForPlugins.isSelected());
        if(choose != null)
          choose.setEnabled(mShowPicturesForSelection.isSelected() && mShowPicturesForPlugins.isSelected());
      }
    });
    
    mShowPicturesInTimeRange.getItemListeners()[0].itemStateChanged(null);
    mShowPicturesForDuration.getItemListeners()[0].itemStateChanged(null);
    mShowPicturesForSelection.getItemListeners()[0].itemStateChanged(null);
    mShowPicturesNever.getItemListeners()[0].itemStateChanged(null);
    }catch(Exception e) {e.printStackTrace();}
  }
  
  private void handlePluginSelection() {
    if(mClientPlugins.length > 0) {
      mPluginLabel.setText(mClientPlugins[0].toString());
      mPluginLabel.setEnabled(true);
    }
    else {
      mPluginLabel.setText(mLocalizer.msg("noPlugins","No Plugins choosen"));
      mPluginLabel.setEnabled(false);
    }
    
    for (int i = 1; i < (mClientPlugins.length > 4 ? 3 : mClientPlugins.length); i++) {
      mPluginLabel.setText(mPluginLabel.getText() + ", " + mClientPlugins[i]);
    }
    
    if(mClientPlugins.length > 4)
      mPluginLabel.setText(mPluginLabel.getText() + " (" + (mClientPlugins.length - 3) + " " + mLocalizer.ellipsisMsg("otherPlugins","others") + ")");
  }
  
  /**
   * @return The picture showing type of this settings
   */
  public int getPictureShowingType() {
    int value = ProgramPanelSettings.SHOW_PICTURES_NEVER;
    
    if(mShowPicturesEver.isSelected())
      value = ProgramPanelSettings.SHOW_PICTURES_EVER;
    else if(mShowPicturesForSelection.isSelected()) {
      if(mShowPicturesForDuration.isSelected())
        value += ProgramPanelSettings.SHOW_PICTURES_FOR_DURATION;
      if(mShowPicturesForPlugins != null && mShowPicturesForPlugins.isSelected() && mClientPlugins != null && mClientPlugins.length > 0)
        value += ProgramPanelSettings.SHOW_PICTURES_FOR_PLUGINS;
      if(mShowPicturesInTimeRange.isSelected())
        value += ProgramPanelSettings.SHOW_PICTURES_IN_TIME_RANGE;
    }
    
    return value;
  }

  /**
   * @return The time range start time.
   */
  public int getPictureTimeRangeStart() {
    Calendar cal = Calendar.getInstance();
    Date startTime = (Date) mPictureStartTime.getValue();
    cal.setTime(startTime);
    return cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE);
  }

  /**
   * @return The time range end time.
   */
  public int getPictureTimeRangeEnd() {
    Calendar cal = Calendar.getInstance();
    Date startTime = (Date) mPictureEndTime.getValue();
    cal.setTime(startTime);
    return cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE);
  }
  
  /**
   * @return The duration to use for showing of pictures.
   */
  public int getPictureDurationTime() {
    return ((Integer)mDuration.getValue()).intValue();
  }
  
  /**
   * @return If the picture should contain the description.
   */
  public boolean getPictureIsShowingDescription() {
    return mShowDescription.isSelected();
  }
  
  /**
   * @return The selected client plugins.
   */
  public String[] getClientPluginIds() {
    if(mShowPicturesForPlugins != null) {
      String[] clientPluginIdArr = new String[mClientPlugins.length];
    
      for (int i = 0; i < mClientPlugins.length; i++)
        clientPluginIdArr[i] = mClientPlugins[i].getId();
      
      return clientPluginIdArr;
    }
    
    return null;
  }
  
  /**
   * Checks if a given type to check contains a type.
   * 
   * @param typeToCheck The type to check.
   * @param containingType The type to which should the typeToCheck is to check for. 
   * @return True if the typeToCheck contains the containingType
   */
  public static boolean typeContainsType(int typeToCheck, int containingType) {
    if(containingType == ProgramPanelSettings.SHOW_PICTURES_FOR_PLUGINS)
      return 
        typeToCheck == ProgramPanelSettings.SHOW_PICTURES_FOR_PLUGINS || 
        typeToCheck == ProgramPanelSettings.SHOW_PICTURES_FOR_PLUGINS + ProgramPanelSettings.SHOW_PICTURES_IN_TIME_RANGE + ProgramPanelSettings.SHOW_PICTURES_FOR_DURATION ||
        typeToCheck == ProgramPanelSettings.SHOW_PICTURES_FOR_PLUGINS + ProgramPanelSettings.SHOW_PICTURES_IN_TIME_RANGE ||
        typeToCheck == ProgramPanelSettings.SHOW_PICTURES_FOR_PLUGINS + ProgramPanelSettings.SHOW_PICTURES_FOR_DURATION;
    else if(containingType == ProgramPanelSettings.SHOW_PICTURES_FOR_DURATION)
      return
        typeToCheck == ProgramPanelSettings.SHOW_PICTURES_FOR_DURATION ||
        typeToCheck == ProgramPanelSettings.SHOW_PICTURES_FOR_DURATION + ProgramPanelSettings.SHOW_PICTURES_FOR_PLUGINS + ProgramPanelSettings.SHOW_PICTURES_IN_TIME_RANGE ||
        typeToCheck == ProgramPanelSettings.SHOW_PICTURES_FOR_DURATION + ProgramPanelSettings.SHOW_PICTURES_FOR_PLUGINS ||
        typeToCheck == ProgramPanelSettings.SHOW_PICTURES_FOR_DURATION + ProgramPanelSettings.SHOW_PICTURES_IN_TIME_RANGE;
    else if(containingType == ProgramPanelSettings.SHOW_PICTURES_IN_TIME_RANGE)
      return
        typeToCheck == ProgramPanelSettings.SHOW_PICTURES_IN_TIME_RANGE || 
        typeToCheck == ProgramPanelSettings.SHOW_PICTURES_IN_TIME_RANGE + ProgramPanelSettings.SHOW_PICTURES_FOR_DURATION + ProgramPanelSettings.SHOW_PICTURES_FOR_PLUGINS ||
        typeToCheck == ProgramPanelSettings.SHOW_PICTURES_IN_TIME_RANGE + ProgramPanelSettings.SHOW_PICTURES_FOR_DURATION ||
        typeToCheck == ProgramPanelSettings.SHOW_PICTURES_IN_TIME_RANGE + ProgramPanelSettings.SHOW_PICTURES_FOR_PLUGINS;
    else 
      return typeToCheck == containingType;
  }

  public Dimension getPreferredScrollableViewportSize() {
    return getPreferredSize();
  }

  public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
    return 50;
  }

  public boolean getScrollableTracksViewportHeight() {
    return false;
  }

  public boolean getScrollableTracksViewportWidth() {
    return true;
  }

  public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
    return 20;
  }
}
