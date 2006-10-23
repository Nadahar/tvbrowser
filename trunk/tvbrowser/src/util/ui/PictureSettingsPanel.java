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

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Calendar;
import java.util.Date;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.SpinnerDateModel;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

import devplugin.SettingsItem;

import tvbrowser.ui.settings.SettingsDialog;
import util.settings.ProgramPanelSettings;

/**
 * A class that contains settings components for picture settings.
 * 
 * @author René Mach
 * @since 2.2.2
 */
public class PictureSettingsPanel extends JPanel {
  private static final long serialVersionUID = 1L;

  /** The localizer for this class */
  public static Localizer mLocalizer = Localizer.getLocalizerFor(PictureSettingsPanel.class);
  
  /** Show the pictures in time range */
  public static final int SHOW_IN_TIME_RANGE = 0;
  /** Always show the pictures */
  public static final int SHOW_EVER = 1;
  /** Show the pictures never */
  public static final int SHOW_NEVER = 2;
  /** Show the pictures for selected plugins */
  public static final int SHOW_FOR_PLUGINS = 3;
  
  private JRadioButton mShowPicturesInTimeRange, mShowPicturesEver, mShowPicturesNever;
  private JSpinner mPictureStartTime, mPictureEndTime;
  private JLabel mStartLabel, mEndLabel;
  private JCheckBox mShowDescription;
  
  private JEditorPane mHelpLabel;

  /**
   * Creates an instance of this class.
   * 
   * @param settings The current picture settings
   * @param showTitle Show the title in this panel.
   * @param addBorder If the panel should contains an empty border.
   */
  public PictureSettingsPanel(ProgramPanelSettings settings, boolean showTitle, boolean addBorder) {
    this(settings.getPictureShowingType(), settings.getPictureTimeRangeStart(), settings.getPictureTimeRangeEnd(), settings.isShowingPictureDescription(), showTitle, addBorder, null, null);
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
   */
  public PictureSettingsPanel(int type, int timeRangeStart, int timeRangeEnd, boolean showDescription, boolean showTitle, boolean addBorder, JPanel additionalPanel, JRadioButton additionalRadioButton) {
    mShowPicturesInTimeRange = new JRadioButton(mLocalizer.msg("showInTimeRange","Show in time range:"), type == SHOW_IN_TIME_RANGE);
    mShowPicturesEver = new JRadioButton(mLocalizer.msg("showEver","Show always"), type == SHOW_EVER);
    mShowPicturesNever = new JRadioButton(mLocalizer.msg("showNever","Show never"), type == SHOW_NEVER);
    
    ButtonGroup bg = new ButtonGroup();
    
    bg.add(mShowPicturesInTimeRange);
    bg.add(mShowPicturesEver);
    bg.add(mShowPicturesNever);
    
    String timePattern = mLocalizer.msg("timePattern","hh:mm a");       
    
    mPictureStartTime = new JSpinner(new SpinnerDateModel());
    mPictureStartTime.setEditor(new JSpinner.DateEditor(mPictureStartTime, timePattern));
    CaretPositionCorrector.createCorrector(((JSpinner.DateEditor)mPictureStartTime.getEditor()).getTextField(), new char[] {':'}, -1);
    
    mPictureEndTime = new JSpinner(new SpinnerDateModel());
    mPictureEndTime.setEditor(new JSpinner.DateEditor(mPictureEndTime, timePattern));
    CaretPositionCorrector.createCorrector(((JSpinner.DateEditor)mPictureEndTime.getEditor()).getTextField(), new char[] {':'}, -1);

    Calendar cal = Calendar.getInstance();
    cal.set(Calendar.HOUR_OF_DAY, timeRangeStart / 60);
    cal.set(Calendar.MINUTE, timeRangeStart % 60);
    mPictureStartTime.setValue(cal.getTime());

    cal.set(Calendar.HOUR_OF_DAY, timeRangeEnd / 60);
    cal.set(Calendar.MINUTE, timeRangeEnd % 60);
    mPictureEndTime.setValue(cal.getTime());
        
    mShowDescription = new JCheckBox(mLocalizer.msg("showDescription","Show description for pictures"), showDescription);
    
    mHelpLabel = UiUtilities.createHtmlHelpTextArea(mLocalizer.msg("help","These settings affect only the showing of the pictures. The pictures can only be shown if the download of pictures in enabled. To enable the picture download look at the <a href=\"#link\">settings of the tv dataservices</a>."), new HyperlinkListener() {
      public void hyperlinkUpdate(HyperlinkEvent e) {
        if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
          SettingsDialog.getInstance().showSettingsTab(SettingsItem.TVDATASERVICES);
        }
      }
    });
    
    CellConstraints cc = new CellConstraints();
    
    FormLayout layout = new FormLayout(
        "5dlu, 12dlu, pref, 5dlu, pref, pref:grow, 5dlu",
        "pref,pref,pref,2dlu,pref,pref,10dlu,pref,fill:10dlu:grow,pref");
    
    PanelBuilder pb = new PanelBuilder(layout, this);
    
    if(addBorder)
      pb.setDefaultDialogBorder();
    
    int y = 1;
    
    if(showTitle) {
      layout.insertRow(1,new RowSpec("5dlu"));
      layout.insertRow(1,new RowSpec("pref"));
    
      pb.addSeparator(mLocalizer.msg("pictures","Pictures"), cc.xyw(1,y,7));
      y += 2;
    }
    
    pb.add(mShowPicturesInTimeRange, cc.xyw(2,y++,6));
    mStartLabel = pb.addLabel(mLocalizer.msg("startTime","From:"), cc.xy(3,y));
    pb.add(mPictureStartTime, cc.xy(5,y++));
    mEndLabel = pb.addLabel(mLocalizer.msg("endTime","To:"), cc.xy(3,y));
    pb.add(mPictureEndTime, cc.xy(5,y++));
    
    if(additionalPanel != null) {
      
      if(additionalRadioButton != null)
        bg.add(additionalRadioButton);
      
      layout.insertRow(++y,new RowSpec("pref"));
      pb.add(additionalPanel, cc.xyw(2,y,6));      
      layout.insertRow(++y,new RowSpec("2dlu"));
    }
    
    pb.add(mShowPicturesEver, cc.xyw(2,++y,6));
    pb.add(mShowPicturesNever, cc.xyw(2,++y,6));y++;
    pb.add(mShowDescription, cc.xyw(2,++y,6));
    pb.add(mHelpLabel, cc.xyw(2,y+2,6));
    
    mShowPicturesInTimeRange.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        mPictureStartTime.setEnabled(mShowPicturesInTimeRange.isSelected());
        mPictureEndTime.setEnabled(mShowPicturesInTimeRange.isSelected());
        mStartLabel.setEnabled(mShowPicturesInTimeRange.isSelected());
        mEndLabel.setEnabled(mShowPicturesInTimeRange.isSelected());
      }
    });
    
    mShowPicturesNever.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        mShowDescription.setEnabled(!mShowPicturesNever.isSelected());
      }
    });
    
    mShowPicturesInTimeRange.getItemListeners()[0].itemStateChanged(null);
    mShowPicturesNever.getItemListeners()[0].itemStateChanged(null);
  }
  
  /**
   * @return The picture showing type of this settings
   */
  public int getPictureShowingType() {
    return mShowPicturesInTimeRange.isSelected() ? SHOW_IN_TIME_RANGE : (mShowPicturesEver.isSelected() ? SHOW_EVER : SHOW_NEVER);
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
   * @return If the picture should contain the description.
   */
  public boolean getPictureIsShowingDescription() {
    return mShowDescription.isSelected();
  }
}
