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
 *
 * CVS information:
 *  $RCSfile$
 *   $Source$
 *     $Date$
 *   $Author$
 * $Revision$
 */

package tvbrowser.extras.favoritesplugin.wizards;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Calendar;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import tvbrowser.core.ChannelList;
import tvbrowser.core.filters.FilterManagerImpl;
import tvbrowser.extras.common.DayListCellRenderer;
import tvbrowser.extras.common.LimitationConfiguration;
import tvbrowser.extras.favoritesplugin.core.Exclusion;
import tvbrowser.extras.favoritesplugin.core.Favorite;
import util.ui.TimePeriodChooser;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

import devplugin.Channel;
import devplugin.Program;
import devplugin.ProgramFilter;

public class ExcludeWizardStep extends AbstractWizardStep {

  private static final util.ui.Localizer mLocalizer = util.ui.Localizer.getLocalizerFor(ExcludeWizardStep.class);

  private static final int MODE_CREATE_DERIVED_FROM_PROGRAM = 0;

  private static final int MODE_CREATE_EXCLUSION = 1;

  private static final int MODE_EDIT_EXCLUSION = 2;

  private Favorite mFavorite;

  private Program mProgram;

  private Exclusion mExclusion;

  private JCheckBox mTitleCb;
  private JCheckBox mTopicCb;
  private JCheckBox mFilterCb;
  private JCheckBox mChannelCb;
  private JCheckBox mTimeCb;
  private JCheckBox mDayCb;

  private JTextField mTitleTf;
  private JTextField mTopicTf;

  private JComboBox mFilterChooser;
  private JComboBox mChannelCB;
  private JComboBox mDayChooser;
  
  private TimePeriodChooser mTimePeriodChooser;

  private String mMainQuestion;
  private String mChannelQuestion;
  private String mTopicQuestion;
  private String mFilterQuestion;
  private String mTimeQuestion;
  private String mTitleQuestion;
  private String mDayQuestion;
  private String mDoneBtnText;

  private int mMode;

  private JPanel mContentPanel;
  
  /**
   * Creates a new Wizard Step instance to create a new exclusion
   * 
   * @param favorite
   */
  public ExcludeWizardStep(Favorite favorite) {
    init(MODE_CREATE_EXCLUSION, favorite, null, null);
  }

  /**
   * Creates a new Wizard Step instance to edit an existing exclusion
   * 
   * @param favorite
   * @param exclusion
   */
  public ExcludeWizardStep(Favorite favorite, Exclusion exclusion) {
    init(MODE_EDIT_EXCLUSION, favorite, null, exclusion);
  }

  /**
   * Creates a new Wizard Step instance to create a new exclusion derived from a
   * program
   * 
   * @param favorite
   * @param prog
   */
  public ExcludeWizardStep(Favorite favorite, Program prog) {
    init(MODE_CREATE_DERIVED_FROM_PROGRAM, favorite, prog, null);
  }

  private void init(int mode, Favorite favorite, Program prog, Exclusion exclusion) {
    mMode = mode;
    mFavorite = favorite;
    mProgram = prog;
    mExclusion = exclusion;
    mDoneBtnText = mLocalizer.msg("doneButton.exclusion","Create exclusion criteria now");

    if (mode == MODE_CREATE_EXCLUSION || mode == MODE_EDIT_EXCLUSION) {
      mMainQuestion = mLocalizer.msg("mainQuestion.edit",
          "Welche Sendungen wollen Sie ausschließen?");
      mChannelQuestion = mLocalizer.msg("channelQuestion.edit", "Sendungen auf diesem Sender:");
      mTopicQuestion = mLocalizer.msg("topicQuestion.edit", "Sendungen mit diesem Stichwort:");
      mTimeQuestion = mLocalizer.msg("timeQuestion.edit", "Sendungen in diesem Zeitraum:");
      mTitleQuestion = mLocalizer.msg("titleQuestion.edit", "Sendungen mit diesem Titel:");
      mDayQuestion = mLocalizer.msg("dayOfWeekQuestion.edit","Sendungen an diesem Tag:");
      mFilterQuestion = mLocalizer.msg("filterQuestion.edit","Sendungen des Filters:");
    } else {
      if(mFavorite != null) {
        mMainQuestion = mLocalizer.msg("mainQuestion.create",
            "Warum gehoert diese Sendung nicht zur Lieblingssendung '{0}'?", mFavorite.getName());
      }
      else {
        mMainQuestion = mLocalizer.msg("mainQuestion.createGlobal","Why do you want exclude this program?");
      }
      
      mChannelQuestion = mLocalizer.msg("channelQuestion.create", "Falscher Sender:");
      mTopicQuestion = mLocalizer.msg("topicQuestion.create", "Falsches Stichwort:");
      mTimeQuestion = mLocalizer.msg("timeQuestion.create", "Falsche Beginnzeit:");
      mTitleQuestion = mLocalizer.msg("titleQuestion.create", "Falscher Titel:");
      mDayQuestion = mLocalizer.msg("dayOfWeekQuestion.create","Falscher Tag:");
    }

  }

  public String getTitle() {
    return mLocalizer.msg("title", "Exclude Programs");
  }

  @Override
  public JPanel createContent(final WizardHandler handler) {
    mTitleCb = new JCheckBox(mTitleQuestion);
    mTitleTf = new JTextField();
    mFilterCb = new JCheckBox(mFilterQuestion);
    mFilterChooser = new JComboBox(FilterManagerImpl.getInstance().getAvailableFilters());
    
    mDayChooser = new JComboBox(new Object[] {
        LimitationConfiguration.DAYLIMIT_WEEKDAY,
        LimitationConfiguration.DAYLIMIT_WEEKEND,
        LimitationConfiguration.DAYLIMIT_MONDAY,
        LimitationConfiguration.DAYLIMIT_TUESDAY,
        LimitationConfiguration.DAYLIMIT_WEDNESDAY,
        LimitationConfiguration.DAYLIMIT_THURSDAY,
        LimitationConfiguration.DAYLIMIT_FRIDAY,
        LimitationConfiguration.DAYLIMIT_SATURDAY,
        LimitationConfiguration.DAYLIMIT_SUNDAY });
    mDayChooser.setRenderer(new DayListCellRenderer());
    CellConstraints cc = new CellConstraints();
    FormLayout layout = new FormLayout("5dlu, pref, default:grow",
    "pref, 5dlu, pref, 5dlu, pref, 5dlu, pref, 5dlu, pref, 5dlu, pref");
    PanelBuilder panelBuilder = new PanelBuilder(layout);

    mChannelCB = new JComboBox(ChannelList.getSubscribedChannels());

    int rowInx = 3;
    panelBuilder.add(new JLabel(mMainQuestion), cc.xyw(1, 1, 3));

    panelBuilder.add(mTitleCb, cc.xy(2, rowInx));
    panelBuilder.add(mTitleTf, cc.xy(3, rowInx));
    rowInx += 2;

    panelBuilder.add(mTopicCb = new JCheckBox(mTopicQuestion), cc.xy(2, rowInx));
    panelBuilder.add(mTopicTf = new JTextField(), cc.xy(3, rowInx));

    rowInx += 2;

    int filterIndex = rowInx;
    
    panelBuilder.add(mChannelCb = new JCheckBox(mChannelQuestion), cc.xy(2, rowInx));
    panelBuilder.add(mChannelCB, cc.xy(3, rowInx));
    rowInx += 2;

    panelBuilder.add(mDayCb = new JCheckBox(mDayQuestion), cc.xy(2, rowInx));
    panelBuilder.add(mDayChooser, cc.xy(3, rowInx));

    rowInx += 2;
    panelBuilder.add(mTimeCb = new JCheckBox(mTimeQuestion), cc.xy(2, rowInx));
    panelBuilder.add(mTimePeriodChooser = new TimePeriodChooser(TimePeriodChooser.ALIGN_LEFT), cc.xy(3, rowInx));

    if(mMode == MODE_EDIT_EXCLUSION || mMode == MODE_CREATE_EXCLUSION) {
      layout.insertRow(filterIndex, RowSpec.decode("pref"));
      layout.insertRow(filterIndex+1, RowSpec.decode("5dlu"));
      
      panelBuilder.add(mFilterCb, cc.xy(2, filterIndex));
      panelBuilder.add(mFilterChooser, cc.xy(3, filterIndex));
    }
    
    if (mMode == MODE_CREATE_DERIVED_FROM_PROGRAM && mProgram != null) {
      mTitleCb.setSelected(false);
      
      mDoneBtnText = mLocalizer.msg("doneButton.toBlacklist","Remove this program now");
      
      mTitleTf.setText(mProgram.getTitle());
      mChannelCB.setSelectedItem(mProgram.getChannel());
      int timeFrom = (mProgram.getHours() - 1) * 60;
      int timeTo = (mProgram.getHours() + 1) * 60;
      if (timeFrom < 0) {
        timeFrom = 0;
      }
      if (timeTo > 24 * 60 - 1) {
        timeTo = 24 * 60 - 1;
      }
      mTimePeriodChooser.setFromTime(timeFrom);
      mTimePeriodChooser.setToTime(timeTo);
      mDayChooser.setSelectedItem(mProgram.getDate().getCalendar().get(
          Calendar.DAY_OF_WEEK));
    } else if (mMode == MODE_EDIT_EXCLUSION) {
      String title = mExclusion.getTitle();
      String topic = mExclusion.getTopic();
      ProgramFilter filter = mExclusion.getFilter();
      Channel channel = mExclusion.getChannel();
      int timeFrom = mExclusion.getTimeLowerBound();
      int timeTo = mExclusion.getTimeUpperBound();
      int dayOfWeek = mExclusion.getDayOfWeek();
      if (title != null) {
        mTitleCb.setSelected(true);
        mTitleTf.setText(title);
      }
      if (topic != null) {
        mTopicCb.setSelected(true);
        mTopicTf.setText(topic);
      }
      if (filter != null) {
        mFilterCb.setSelected(true);
        mFilterChooser.setSelectedItem(filter);
      }
      if (channel != null) {
        mChannelCb.setSelected(true);
        mChannelCB.setSelectedItem(channel);
      }
      if (timeFrom >= 0 && timeTo >= 0) {
        mTimeCb.setSelected(true);
        mTimePeriodChooser.setFromTime(timeFrom);
        mTimePeriodChooser.setToTime(timeTo);
        mDayChooser.setSelectedItem(mExclusion.getDayOfWeek());
      }
      if (dayOfWeek != Exclusion.DAYLIMIT_DAILY) {
        mDayCb.setSelected(true);
        mDayChooser.setSelectedItem(dayOfWeek);
      }
    }

    updateButtons(handler);

    mChannelCb.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        updateButtons(handler);
      }
    });

    mTitleCb.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        updateButtons(handler);
      }
    });

    mTopicCb.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        updateButtons(handler);
      }
    });
    
    mFilterCb.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        updateButtons(handler);
      }
    });

    mTimeCb.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        updateButtons(handler);
      }
    });

    mDayCb.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        updateButtons(handler);
      }
    });
    
    mContentPanel = panelBuilder.getPanel();
    return mContentPanel;
  }

  private void updateButtons(WizardHandler handler) {
    boolean allowNext = false;
    if (mChannelCb.isSelected()) {
      allowNext = true;
    }

    if (mTopicCb.isSelected()) {
      allowNext = true;
    }
    if (mFilterCb.isSelected()) {
      allowNext = true;
    }

    if (mTimeCb.isSelected()) {
      allowNext = true;
    }

    if (mTitleCb.isSelected()) {
      allowNext = true;
    }
    mTitleTf.setEnabled(mTitleCb.isSelected());

    if (mDayCb.isSelected()) {
      allowNext = true;
    }

    mChannelCB.setEnabled(mChannelCb.isSelected());
    mTopicTf.setEnabled(mTopicCb.isSelected());
    mFilterChooser.setEnabled(mFilterCb.isSelected());
    mTimePeriodChooser.setEnabled(mTimeCb.isSelected());
    mDayChooser.setEnabled(mDayCb.isSelected());
    
    if(mMode == MODE_CREATE_DERIVED_FROM_PROGRAM && mProgram != null) {
      if(allowNext || mFavorite == null) {
        mDoneBtnText = mLocalizer.msg("doneButton.exclusion","Create exclusion criteria now");
      } else {
        mDoneBtnText = mLocalizer.msg("doneButton.toBlacklist","Only remove this program now");
      }
        
      handler.changeDoneBtnText();
      handler.allowFinish(allowNext || mFavorite != null);
    }
    else {
      handler.allowFinish(allowNext);
    }
  }

  public Object createDataObject(Object obj) {
    String title = null;
    String topic = null;
    String filterName = null;
    Channel channel = null;
    int timeFrom = -1;
    int timeTo = -1;
    int weekOfDay = Exclusion.DAYLIMIT_DAILY;

    if (mTitleCb.isSelected()) {
      title = mTitleTf.getText();
    }

    if (mTopicCb.isSelected()) {
      topic = mTopicTf.getText();
    }
    
    if(mFilterCb.isSelected()) {
      filterName = ((ProgramFilter)mFilterChooser.getSelectedItem()).getName();
    }

    if (mChannelCb.isSelected()) {
      channel = (Channel) mChannelCB.getSelectedItem();
    }

    if (mTimeCb.isSelected()) {
      timeFrom = mTimePeriodChooser.getFromTime();
      timeTo = mTimePeriodChooser.getToTime();
    }

    if (mDayCb.isSelected()) {
      weekOfDay = ((Integer) mDayChooser.getSelectedItem()).intValue();
    }

    if (mDoneBtnText.compareTo(mLocalizer.msg("doneButton.toBlacklist","Remove this program now")) == 0) {
      return "blacklist";
    } else {
      return new Exclusion(title, topic, channel, timeFrom, timeTo, weekOfDay, filterName);
    }

  }

  public int[] getButtons() {
    return new int[] { WizardStep.BUTTON_DONE, WizardStep.BUTTON_CANCEL };
  }

  public WizardStep next() {
    return null;
  }

  public WizardStep back() {
    return null;
  }

  public boolean isValid() {
    if (mTitleCb.isSelected() && (mTitleTf.getText() == null || mTitleTf.getText().trim().length() == 0)) {
      JOptionPane.showMessageDialog(mContentPanel,
          mLocalizer.msg("invalidInput.noTitle", "Please enter a title."),
          mLocalizer.msg("invalidInput.noTitleTitle", "Enter Topic"),
          JOptionPane.WARNING_MESSAGE);
      return false;
    }
    if (mTopicCb.isSelected() && (mTopicTf.getText() == null || mTopicTf.getText().trim().length() == 0)) {
      JOptionPane.showMessageDialog(mContentPanel,
          mLocalizer.msg("invalidInput.noTopic", "Please enter a topic."),
          mLocalizer.msg("invalidInput.noTopicTitle", "Enter Topic"),
          JOptionPane.WARNING_MESSAGE);
      return false;
    }
    return true;
  }
  
  /*
   * (non-Javadoc)
   * @see tvbrowser.extras.favoritesplugin.wizards.AbstractWizardStep#isSingleStep()
   */
  @Override
  public boolean isSingleStep() {
    return true;
  }
  
  @Override
  public String getDoneBtnText() {
    return mDoneBtnText;
  }
}