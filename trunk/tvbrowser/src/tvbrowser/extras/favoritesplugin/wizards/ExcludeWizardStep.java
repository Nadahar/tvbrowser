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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Calendar;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.commons.lang3.StringUtils;

import tvbrowser.core.ChannelList;
import tvbrowser.core.filters.FilterManagerImpl;
import tvbrowser.extras.common.DayListCellRenderer;
import tvbrowser.extras.common.LimitationConfiguration;
import tvbrowser.extras.favoritesplugin.core.Exclusion;
import tvbrowser.extras.favoritesplugin.core.Favorite;
import tvbrowser.extras.favoritesplugin.core.FavoriteFilter;
import tvbrowser.ui.filter.dlgs.SelectFilterDlg;
import tvbrowser.ui.mainframe.MainFrame;
import util.ui.TimePeriodChooser;
import util.ui.UiUtilities;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

import devplugin.Channel;
import devplugin.Plugin;
import devplugin.Program;
import devplugin.ProgramFieldType;
import devplugin.ProgramFilter;
import devplugin.ProgramInfoHelper;

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
  private JCheckBox mEpisodeTitleCb;
  private JCheckBox mCategoryCb;

  private JTextField mTitleTf;
  private JTextField mTopicTf;
  private JTextField mEpisodeTitleTf;

  private JComboBox mFilterChooser;
  private JComboBox mChannelCB;
  private JComboBox mDayChooser;
  private JComboBox mCategoryChooser;

  private TimePeriodChooser mTimePeriodChooser;

  private String mMainQuestion;
  private String mChannelQuestion;
  private String mTopicQuestion;
  private String mCateogryQuestion;
  private String mFilterQuestion;
  private String mTimeQuestion;
  private String mTitleQuestion;
  private String mEpisodeTitleQuestion;
  private String mDayQuestion;
  private String mDoneBtnText;

  private int mMode;

  private JPanel mContentPanel;
  private JButton mEditFilter;

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
          "What programs do you want to exclude?");
      mChannelQuestion = mLocalizer.msg("channelQuestion.edit", "Programs aired on this channel:");
      mTopicQuestion = mLocalizer.msg("topicQuestion.edit", "Programs containing this term:");
      mTimeQuestion = mLocalizer.msg("timeQuestion.edit", "Programs that start during this period:");
      mTitleQuestion = mLocalizer.msg("titleQuestion.edit", "Programs with this title:");
      mEpisodeTitleQuestion = mLocalizer.msg("episodeTitleQuestion.edit", "Program with this episode title:");
      mDayQuestion = mLocalizer.msg("dayOfWeekQuestion.edit","Programs on this day of week:");
      mFilterQuestion = mLocalizer.msg("filterQuestion.edit","Programs of the filter:");
      mCateogryQuestion = mLocalizer.msg("categoryQuestion.edit", "Programs with category:");
    } else {
      if(mFavorite != null) {
        mMainQuestion = mLocalizer.msg("mainQuestion.create",
            "Warum gehoert diese Sendung nicht zur Lieblingssendung '{0}'?", mFavorite.getName());
      }
      else {
        mMainQuestion = mLocalizer.msg("mainQuestion.createGlobal","Why do you want exclude this program?");
      }

      mChannelQuestion = mLocalizer.msg("channelQuestion.create", "Wrong channel:");
      mTopicQuestion = mLocalizer.msg("topicQuestion.create", "Wrong topic:");
      mTimeQuestion = mLocalizer.msg("timeQuestion.create", "Wrong start time:");
      mTitleQuestion = mLocalizer.msg("titleQuestion.create", "Wrong episode title:");
      mEpisodeTitleQuestion = mLocalizer.msg("episodeTitleQuestion.create", "Wrong episode number:");
      mDayQuestion = mLocalizer.msg("dayOfWeekQuestion.create","Wrong day:");
      mCateogryQuestion = mLocalizer.msg("categoryQuestion.create", "Wrong category:");
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
    mEditFilter = new JButton(SelectFilterDlg.mLocalizer.msg("title", "Edit Filters"));
    
    ProgramFilter[] avilableFilter = FilterManagerImpl.getInstance().getAvailableFilters();
    
    ArrayList<ProgramFilter> useableFilter = new ArrayList<ProgramFilter>();
    
    for(ProgramFilter filter : avilableFilter) {
      if(!(filter instanceof FavoriteFilter)) {
        useableFilter.add(filter);
      }
    }
    
    mFilterChooser = new JComboBox(useableFilter.toArray(new ProgramFilter[useableFilter.size()]));

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
    
    FormLayout layout = new FormLayout("5dlu, pref, default:grow, 3dlu, default",
    "pref, 5dlu, pref, 5dlu, pref, 5dlu, pref, 5dlu, pref, 5dlu, pref, 5dlu, pref, 5dlu, pref");
    PanelBuilder panelBuilder = new PanelBuilder(layout);

    mCategoryChooser = new JComboBox(ProgramInfoHelper.getInfoIconMessages());
    
    mChannelCB = new JComboBox(ChannelList.getSubscribedChannels());

    int rowInx = 3;
    panelBuilder.add(new JLabel(mMainQuestion), CC.xyw(1, 1, 5));

    panelBuilder.add(mTitleCb, CC.xy(2, rowInx));
    panelBuilder.add(mTitleTf, CC.xyw(3, rowInx, 3));
    rowInx += 2;

    panelBuilder.add(mTopicCb = new JCheckBox(mTopicQuestion), CC.xy(2, rowInx));
    panelBuilder.add(mTopicTf = new JTextField(), CC.xyw(3, rowInx, 3));
    
    rowInx += 2;
    
    panelBuilder.add(mEpisodeTitleCb = new JCheckBox(mEpisodeTitleQuestion), CC.xy(2,rowInx));
    panelBuilder.add(mEpisodeTitleTf = new JTextField(), CC.xyw(3, rowInx, 3));
    
    rowInx += 2;
    
    int filterIndex = rowInx;
        
    panelBuilder.add(mCategoryCb = new JCheckBox(mCateogryQuestion), CC.xy(2, rowInx));
    panelBuilder.add(mCategoryChooser, CC.xyw(3, rowInx, 3));
    
    rowInx += 2;

    panelBuilder.add(mChannelCb = new JCheckBox(mChannelQuestion), CC.xy(2, rowInx));
    panelBuilder.add(mChannelCB, CC.xyw(3, rowInx, 3));
    rowInx += 2;

    panelBuilder.add(mDayCb = new JCheckBox(mDayQuestion), CC.xy(2, rowInx));
    panelBuilder.add(mDayChooser, CC.xyw(3, rowInx, 3));

    rowInx += 2;
    panelBuilder.add(mTimeCb = new JCheckBox(mTimeQuestion), CC.xy(2, rowInx));
    panelBuilder.add(mTimePeriodChooser = new TimePeriodChooser(TimePeriodChooser.ALIGN_LEFT), CC.xyw(3, rowInx, 3));

    if(mMode == MODE_EDIT_EXCLUSION || mMode == MODE_CREATE_EXCLUSION) {
      layout.insertRow(filterIndex, RowSpec.decode("pref"));
      layout.insertRow(filterIndex+1, RowSpec.decode("5dlu"));

      panelBuilder.add(mFilterCb, CC.xy(2, filterIndex));
      panelBuilder.add(mFilterChooser, CC.xy(3, filterIndex));
      
      
      mEditFilter.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
          SelectFilterDlg filterDlg = SelectFilterDlg.create(UiUtilities.getLastModalChildOf(MainFrame.getInstance()));
          filterDlg.setVisible(true);
          
          Object selected = mFilterChooser.getSelectedItem();
          
          ((DefaultComboBoxModel)mFilterChooser.getModel()).removeAllElements();
          
          for(ProgramFilter filter :Plugin.getPluginManager().getFilterManager().getAvailableFilters()) {
            ((DefaultComboBoxModel)mFilterChooser.getModel()).addElement(filter);
          }
          
          mFilterChooser.setSelectedItem(selected);
        }
      });
      
      panelBuilder.add(mEditFilter, CC.xy(5, filterIndex));
    }

    if (mMode == MODE_CREATE_DERIVED_FROM_PROGRAM && mProgram != null) {
      mTitleCb.setSelected(false);

      mDoneBtnText = mLocalizer.msg("doneButton.toBlacklist","Remove this program now");

      mTitleTf.setText(mProgram.getTitle());
      
      String episode = mProgram.getTextField(ProgramFieldType.EPISODE_TYPE);
      
      if(episode != null && episode.trim().length() > 0) {
        mEpisodeTitleTf.setText(episode);
      }
      
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
      String episode = mExclusion.getEpisodeTitle();
      ProgramFilter filter = mExclusion.getFilter();
      Channel channel = mExclusion.getChannel();
      int timeFrom = mExclusion.getTimeLowerBound();
      int timeTo = mExclusion.getTimeUpperBound();
      int dayOfWeek = mExclusion.getDayOfWeek();
      int bitIndex = ProgramInfoHelper.getIndexForBit(mExclusion.getCategory());
      
      if (title != null) {
        mTitleCb.setSelected(true);
        mTitleTf.setText(title);
      }
      if (episode != null) {
        mEpisodeTitleCb.setSelected(true);
        mEpisodeTitleTf.setText(episode);
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
      if(bitIndex != -1) {
        mCategoryCb.setSelected(true);
        mCategoryChooser.setSelectedIndex(bitIndex);
      }
    }

    updateButtons(handler);
    
    ItemListener buttonUpdate = new ItemListener() {
      @Override
      public void itemStateChanged(ItemEvent e) {
        updateButtons(handler);
      }
    };

    mChannelCb.addItemListener(buttonUpdate);
    mTitleCb.addItemListener(buttonUpdate);
    mTopicCb.addItemListener(buttonUpdate);
    mEpisodeTitleCb.addItemListener(buttonUpdate);
    mCategoryCb.addItemListener(buttonUpdate);
    mFilterCb.addItemListener(buttonUpdate);
    mTimeCb.addItemListener(buttonUpdate);
    mDayCb.addItemListener(buttonUpdate);

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
    
    if (mEpisodeTitleCb.isSelected()) {
      allowNext = true;
    }
    
    if(mCategoryCb.isSelected()) {
      allowNext = true;
    }

    mChannelCB.setEnabled(mChannelCb.isSelected());
    mTopicTf.setEnabled(mTopicCb.isSelected());
    mEpisodeTitleTf.setEnabled(mEpisodeTitleCb.isSelected());
    mFilterChooser.setEnabled(mFilterCb.isSelected());
    mEditFilter.setEnabled(mFilterCb.isSelected());
    mTimePeriodChooser.setEnabled(mTimeCb.isSelected());
    mDayChooser.setEnabled(mDayCb.isSelected());
    mCategoryChooser.setEnabled(mCategoryCb.isSelected());

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
    String episodeTitle = null;
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
    
    if(mEpisodeTitleCb.isSelected()) {
      episodeTitle = mEpisodeTitleTf.getText();
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
      return new Exclusion(title, topic, channel, timeFrom, timeTo, weekOfDay, filterName, episodeTitle, ProgramInfoHelper.getBitForIndex(mCategoryChooser.getSelectedIndex()));
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
    if (mTitleCb.isSelected() && StringUtils.isBlank(mTitleTf.getText())) {
      JOptionPane.showMessageDialog(mContentPanel,
          mLocalizer.msg("invalidInput.noTitle", "Please enter a title."),
          mLocalizer.msg("invalidInput.noTitleTitle", "Enter Topic"),
          JOptionPane.WARNING_MESSAGE);
      return false;
    }
    if (mTopicCb.isSelected() && StringUtils.isBlank(mTopicTf.getText())) {
      JOptionPane.showMessageDialog(mContentPanel,
          mLocalizer.msg("invalidInput.noTopic", "Please enter a topic."),
          mLocalizer.msg("invalidInput.noTopicTitle", "Enter Topic"),
          JOptionPane.WARNING_MESSAGE);
      return false;
    }
    return true;
  }

  @Override
  public boolean isSingleStep() {
    return true;
  }

  @Override
  public String getDoneBtnText() {
    return mDoneBtnText;
  }
}