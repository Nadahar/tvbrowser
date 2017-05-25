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

import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;

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
import tvbrowser.core.ChannelList;
import tvbrowser.core.filters.FilterManagerImpl;
import tvbrowser.extras.common.DayListCellRenderer;
import tvbrowser.extras.common.LimitationConfiguration;
import tvbrowser.extras.favoritesplugin.core.Exclusion;
import tvbrowser.extras.favoritesplugin.core.Exclusion.ProgramFieldExclusion;
import tvbrowser.extras.favoritesplugin.core.Favorite;
import tvbrowser.extras.favoritesplugin.core.FavoriteFilter;
import tvbrowser.ui.filter.dlgs.SelectFilterDlg;
import tvbrowser.ui.mainframe.MainFrame;
import util.ui.TimePeriodChooser;
import util.ui.UiUtilities;
import util.ui.WrapperFilter;

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
  private JCheckBox mProgramFieldCb;

  private JTextField mTitleTf;
  private JTextField mTopicTf;
  private JTextField mEpisodeTitleTf;
  private JTextField mProgramFieldTextTf;

  private JComboBox<WrapperFilter> mFilterChooser;
  private JComboBox<Channel> mChannelCB;
  private JComboBox<Object> mDayChooser;
  private JComboBox<String> mCategoryChooser;
  private JComboBox<ProgramFieldType> mProgramFieldChooser;

  private TimePeriodChooser mTimePeriodChooser;

  private String mMainQuestion;
  private String mChannelQuestion;
  private String mTopicQuestion;
  private String mCateogryQuestion;
  private String mProgramFieldQuestion;
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
      mProgramFieldQuestion = mLocalizer.msg("programFieldQuestion.edit", "Programs with:");
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
      mProgramFieldQuestion = mLocalizer.msg("programFieldQuestion.create", "Wrong:");
    }

  }

  public String getTitle() {
    return mLocalizer.msg("title", "Exclude Programs");
  }

  @Override
  public JPanel createContent(final WizardHandler handler) {
    mTitleCb = new JCheckBox(mTitleQuestion);
    mTitleTf = new JTextField();
    mTopicTf = new JTextField();
    mEpisodeTitleTf = new JTextField();
    mProgramFieldTextTf = new JTextField();
    mFilterCb = new JCheckBox(mFilterQuestion);
    mEditFilter = new JButton(SelectFilterDlg.mLocalizer.msg("title", "Edit Filters"));
    
    final CaretListener textFieldButtonUpdateListener = new CaretListener() {
      @Override
      public void caretUpdate(CaretEvent e) {
        updateButtons(handler);
      }
    };
    
    mTitleTf.addCaretListener(textFieldButtonUpdateListener);
    mEpisodeTitleTf.addCaretListener(textFieldButtonUpdateListener);
    mTopicTf.addCaretListener(textFieldButtonUpdateListener);
    mProgramFieldTextTf.addCaretListener(textFieldButtonUpdateListener);
    
    ProgramFilter[] avilableFilter = FilterManagerImpl.getInstance().getAvailableFilters();
    
    ArrayList<WrapperFilter> useableFilter = new ArrayList<WrapperFilter>();
    
    for(ProgramFilter filter : avilableFilter) {
      if(!(filter instanceof FavoriteFilter)) {
        useableFilter.add(new WrapperFilter(filter));
      }
    }
    
    mFilterChooser = new JComboBox<>(useableFilter.toArray(new WrapperFilter[useableFilter.size()]));
    
    mDayChooser = new JComboBox<>(new Object[] {
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
    
    FormLayout layout = new FormLayout("5dlu, default, default, default:grow, 3dlu, default",
    "default, 5dlu, default, 5dlu, default, 5dlu, default, 5dlu, default, 5dlu, default, 5dlu, default, 5dlu, default, 5dlu, default");
    PanelBuilder panelBuilder = new PanelBuilder(layout);

    mCategoryChooser = new JComboBox<>(ProgramInfoHelper.getInfoIconMessages());
    
    mChannelCB = new JComboBox<>(ChannelList.getSubscribedChannels());

    int rowInx = 3;
    panelBuilder.add(new JLabel(mMainQuestion), CC.xyw(1, 1, 6));

    panelBuilder.add(mTitleCb, CC.xyw(2, rowInx, 2));
    panelBuilder.add(mTitleTf, CC.xyw(4, rowInx, 3));
    rowInx += 2;

    panelBuilder.add(mTopicCb = new JCheckBox(mTopicQuestion), CC.xyw(2, rowInx, 2));
    panelBuilder.add(mTopicTf, CC.xyw(4, rowInx, 3));
    
    rowInx += 2;
    
    panelBuilder.add(mEpisodeTitleCb = new JCheckBox(mEpisodeTitleQuestion), CC.xyw(2,rowInx, 2));
    panelBuilder.add(mEpisodeTitleTf, CC.xyw(4, rowInx, 3));
    
    rowInx += 2;
    
    int filterIndex = rowInx;
        
    panelBuilder.add(mCategoryCb = new JCheckBox(mCateogryQuestion), CC.xyw(2, rowInx, 2));
    panelBuilder.add(mCategoryChooser, CC.xyw(4, rowInx, 3));
    
    rowInx += 2;

    panelBuilder.add(mChannelCb = new JCheckBox(mChannelQuestion), CC.xyw(2, rowInx, 2));
    panelBuilder.add(mChannelCB, CC.xyw(4, rowInx, 3));
    rowInx += 2;

    panelBuilder.add(mDayCb = new JCheckBox(mDayQuestion), CC.xyw(2, rowInx, 2));
    panelBuilder.add(mDayChooser, CC.xyw(4, rowInx, 3));

    rowInx += 2;
    panelBuilder.add(mTimeCb = new JCheckBox(mTimeQuestion), CC.xyw(2, rowInx, 2));
    panelBuilder.add(mTimePeriodChooser = new TimePeriodChooser(TimePeriodChooser.ALIGN_LEFT), CC.xyw(4, rowInx, 3));
    
    rowInx += 2;
    panelBuilder.add(mProgramFieldCb = new JCheckBox(mProgramFieldQuestion), CC.xy(2, rowInx));
    panelBuilder.add(mProgramFieldChooser = new JComboBox<>(), CC.xy(3, rowInx));
    panelBuilder.add(mProgramFieldTextTf, CC.xyw(4, rowInx, 3));
    
    final ArrayList<ProgramFieldType> listProgramFields = new ArrayList<ProgramFieldType>();
    
    listProgramFields.add(ProgramFieldType.AGE_LIMIT_TYPE);
    listProgramFields.add(ProgramFieldType.EPISODE_NUMBER_TYPE);
    listProgramFields.add(ProgramFieldType.GENRE_TYPE);
    listProgramFields.add(ProgramFieldType.LAST_PRODUCTION_YEAR_TYPE);
    listProgramFields.add(ProgramFieldType.ORIGIN_TYPE);
    listProgramFields.add(ProgramFieldType.ORIGINAL_TITLE_TYPE);
    listProgramFields.add(ProgramFieldType.ORIGINAL_EPISODE_TYPE);
    listProgramFields.add(ProgramFieldType.PART_NUMBER_TYPE);
    listProgramFields.add(ProgramFieldType.SERIES_TYPE);
    listProgramFields.add(ProgramFieldType.PRODUCTION_YEAR_TYPE);
    listProgramFields.add(ProgramFieldType.PRODUCTION_COMPANY_TYPE);
    listProgramFields.add(ProgramFieldType.SEASON_NUMBER_TYPE);
    
    Collections.sort(listProgramFields, ProgramFieldType.getComparatorLocalizedNames());
    
    for(ProgramFieldType fieldType : listProgramFields) {
      mProgramFieldChooser.addItem(fieldType);
    }
    
    if(mMode == MODE_EDIT_EXCLUSION || mMode == MODE_CREATE_EXCLUSION) {
      layout.insertRow(filterIndex, RowSpec.decode("pref"));
      layout.insertRow(filterIndex+1, RowSpec.decode("5dlu"));

      panelBuilder.add(mFilterCb, CC.xyw(2, filterIndex, 2));
      panelBuilder.add(mFilterChooser, CC.xy(4, filterIndex));
      
      mEditFilter.addActionListener(e -> {
        SelectFilterDlg filterDlg = SelectFilterDlg.create(UiUtilities.getLastModalChildOf(MainFrame.getInstance()));
        filterDlg.setVisible(true);
        
        Object selected = mFilterChooser.getSelectedItem();
        
        ((DefaultComboBoxModel<WrapperFilter>)mFilterChooser.getModel()).removeAllElements();
        
        for(ProgramFilter filter :Plugin.getPluginManager().getFilterManager().getAvailableFilters()) {
          ((DefaultComboBoxModel<WrapperFilter>)mFilterChooser.getModel()).addElement(new WrapperFilter(filter));
        }
        
        mFilterChooser.setSelectedItem(selected);
      });
      
      panelBuilder.add(mEditFilter, CC.xy(6, filterIndex));
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
      ProgramFieldExclusion programFieldExclusion = mExclusion.getProgramFieldExclusion();
      
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
        mFilterChooser.setSelectedItem(new WrapperFilter(filter));
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
      if(programFieldExclusion != null) {
        mProgramFieldCb.setSelected(true);
        mProgramFieldChooser.setSelectedItem(programFieldExclusion.getProgramFieldType());
        mProgramFieldTextTf.setText(programFieldExclusion.getProgramFieldText());
      }
    }

    updateButtons(handler);
    
    ItemListener buttonUpdate = e -> {
      updateButtons(handler);
    };

    mChannelCb.addItemListener(buttonUpdate);
    mTitleCb.addItemListener(buttonUpdate);
    mTopicCb.addItemListener(buttonUpdate);
    mEpisodeTitleCb.addItemListener(buttonUpdate);
    mCategoryCb.addItemListener(buttonUpdate);
    mFilterCb.addItemListener(buttonUpdate);
    mTimeCb.addItemListener(buttonUpdate);
    mDayCb.addItemListener(buttonUpdate);
    mProgramFieldCb.addItemListener(buttonUpdate);

    mContentPanel = panelBuilder.getPanel();
    return mContentPanel;
  }

  private void updateButtons(WizardHandler handler) {
    boolean allowNext = false;
    if (mChannelCb.isSelected()) {
      allowNext = true;
    }

    if (mTopicCb.isSelected()) {
      allowNext = allowNext || !mTopicTf.getText().trim().isEmpty();
    }
    if (mFilterCb.isSelected()) {
      allowNext = true;
    }

    if (mTimeCb.isSelected()) {
      allowNext = true;
    }

    if (mTitleCb.isSelected()) {
      allowNext = allowNext || !mTitleTf.getText().trim().isEmpty();
    }
    
    if (mDayCb.isSelected()) {
      allowNext = true;
    }
    
    if (mEpisodeTitleCb.isSelected()) {
      allowNext = allowNext || !mEpisodeTitleTf.getText().trim().isEmpty();
    }
    
    if(mCategoryCb.isSelected()) {
      allowNext = true;
    }
    
    if(mProgramFieldCb.isSelected()) {
      allowNext = allowNext || !mProgramFieldTextTf.getText().trim().isEmpty();
    }

    mTitleTf.setEnabled(mTitleCb.isSelected());
    mChannelCB.setEnabled(mChannelCb.isSelected());
    mTopicTf.setEnabled(mTopicCb.isSelected());
    mEpisodeTitleTf.setEnabled(mEpisodeTitleCb.isSelected());
    mFilterChooser.setEnabled(mFilterCb.isSelected());
    mEditFilter.setEnabled(mFilterCb.isSelected());
    mTimePeriodChooser.setEnabled(mTimeCb.isSelected());
    mDayChooser.setEnabled(mDayCb.isSelected());
    mCategoryChooser.setEnabled(mCategoryCb.isSelected());
    mProgramFieldChooser.setEnabled(mProgramFieldCb.isSelected());
    mProgramFieldTextTf.setEnabled(mProgramFieldCb.isSelected());

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
    int category = 0;
    ProgramFieldExclusion programFieldExclusion = null;

    if (mTitleCb.isSelected()) {
      title = mTitleTf.getText().trim();
      
      if(title.isEmpty()) {
        title = null;
      }
    }

    if (mTopicCb.isSelected()) {
      topic = mTopicTf.getText().trim();
      
      if(topic.isEmpty()) {
        topic = null;
      }
    }
    
    if(mEpisodeTitleCb.isSelected()) {
      episodeTitle = mEpisodeTitleTf.getText().trim();
      
      if(episodeTitle.isEmpty()) {
        episodeTitle = null;
      }
    }

    if(mFilterCb.isSelected()) {
      filterName = ((WrapperFilter)mFilterChooser.getSelectedItem()).getName();
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

    if(mCategoryCb.isSelected()) {
      category = ProgramInfoHelper.getBitForIndex(mCategoryChooser.getSelectedIndex());
    }
    
    if(mProgramFieldCb.isSelected() && !mProgramFieldTextTf.getText().trim().isEmpty()) {
      programFieldExclusion = new ProgramFieldExclusion(((ProgramFieldType)mProgramFieldChooser.getSelectedItem()).getTypeId(), mProgramFieldTextTf.getText());
    }
    
    if (mDoneBtnText.compareTo(mLocalizer.msg("doneButton.toBlacklist","Remove this program now")) == 0) {
      return "blacklist";
    } else {
      return new Exclusion(title, topic, channel, timeFrom, timeTo, weekOfDay, filterName, episodeTitle, category, programFieldExclusion);
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
    return true;
    /*
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
    return true;*/
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