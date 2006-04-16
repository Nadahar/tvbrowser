package tvbrowser.extras.favoritesplugin.wizards;

import tvbrowser.extras.favoritesplugin.core.Favorite;
import tvbrowser.extras.favoritesplugin.core.Exclusion;
import tvbrowser.extras.favoritesplugin.core.TitleFavorite;
import tvbrowser.extras.common.LimitationConfiguration;
import tvbrowser.extras.common.DayListCellRenderer;
import tvbrowser.core.ChannelList;

import javax.swing.*;


import devplugin.Program;
import devplugin.Channel;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.builder.PanelBuilder;

import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;
import java.util.Calendar;

import util.ui.TimePeriodChooser;

public class ExcludeWizardStep extends  AbstractWizardStep {

  private static final util.ui.Localizer mLocalizer
    = util.ui.Localizer.getLocalizerFor(ExcludeWizardStep.class);

  private static final int MODE_CREATE_DERIVED_FROM_PROGRAM = 0;
  private static final int MODE_CREATE_EXCLUSION = 1;
  private static final int MODE_EDIT_EXCLUSION = 2;


  private Favorite mFavorite;
  private Program mProgram;
  private Exclusion mExclusion;

  private JCheckBox mTitleCb;
  private JCheckBox mTopicCb;
  private JCheckBox mChannelCb;
  private JCheckBox mTimeCb;
  private JCheckBox mDayCb;
  private JTextField mTitleTf;
  private JTextField mTopicTf;
  private JComboBox mChannelCB;
  private TimePeriodChooser mTimePeriodChooser;
  private JComboBox mDayChooser;

  private String mMainQuestion;
  private String mChannelQuestion;
  private String mTopicQuestion;
  private String mTimeQuestion;
  private String mTitleQuestion;
  private String mDayQuestion;

  private int mMode;

  /**
   * Creates a new Wizard Step instance to create a new exclusion
   * @param favorite
   */
  public ExcludeWizardStep(Favorite favorite) {
    init(MODE_CREATE_EXCLUSION, favorite, null, null);
  }

  /**
   * Creates a new Wizard Step instance to edit an existing exclusion
   * @param favorite
   * @param exclusion
   */
  public ExcludeWizardStep(Favorite favorite, Exclusion exclusion) {
    init(MODE_EDIT_EXCLUSION, favorite, null, exclusion);
  }

  /**
   * Creates a new Wizard Step instance to create a new exclusion derived from a program
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

    if (mode == MODE_CREATE_EXCLUSION || mode == MODE_EDIT_EXCLUSION) {
      mMainQuestion = mLocalizer.msg("mainQuestion.edit","Welche Sendungen wollen Sie ausschließen?");
      mChannelQuestion = mLocalizer.msg("channelQuestion.edit","Sendungen auf diesem Sender:");
      mTopicQuestion = mLocalizer.msg("topicQuestion.edit","Sendungen mit diesem Stichwort:");
      mTimeQuestion = mLocalizer.msg("timeQuestion.edit","Sendungen in diesem Zeitraum:");
      mTitleQuestion = mLocalizer.msg("titleQuestion.edit","Sendungen mit diesem Titel:");
      mDayQuestion = "Sendungen an diesem Tag:";
    }
    else {
      mMainQuestion = mLocalizer.msg("mainQuestion.create","Warum gehoert diese Sendung nicht zur Lieblingssendung '{0}'?", mFavorite.getName());
      mChannelQuestion = mLocalizer.msg("channelQuestion.create","Falscher Sender:");
      mTopicQuestion = mLocalizer.msg("topicQuestion.create","Falsches Stichwort:");
      mTimeQuestion = mLocalizer.msg("timeQuestion.create","Falsche Beginnzeit:");
      mTitleQuestion = mLocalizer.msg("titleQuestion.create","Falscher Titel:");
      mDayQuestion = "Falscher Tag:";
    }




  }

  public String getTitle() {
    return mLocalizer.msg("title","Exclude Programs");
  }

  public JPanel createContent(final WizardHandler handler) {
    mTitleCb = new JCheckBox(mTitleQuestion);
    mTitleTf = new JTextField();
    mDayChooser = new JComboBox(new Object[] {
        new Integer(LimitationConfiguration.DAYLIMIT_WEEKDAY), new Integer(LimitationConfiguration.DAYLIMIT_WEEKEND),
        new Integer(LimitationConfiguration.DAYLIMIT_MONDAY), new Integer(LimitationConfiguration.DAYLIMIT_TUESDAY),
        new Integer(LimitationConfiguration.DAYLIMIT_WEDNESDAY),
        new Integer(LimitationConfiguration.DAYLIMIT_THURSDAY), new Integer(LimitationConfiguration.DAYLIMIT_FRIDAY),
        new Integer(LimitationConfiguration.DAYLIMIT_SATURDAY), new Integer(LimitationConfiguration.DAYLIMIT_SUNDAY), });
    mDayChooser.setRenderer(new DayListCellRenderer());
    CellConstraints cc = new CellConstraints();
    PanelBuilder panelBuilder = new PanelBuilder(
            new FormLayout(
                "5dlu, pref, pref:grow",
                "pref, 5dlu, pref, 5dlu, pref, 5dlu, pref, 5dlu, pref, 5dlu, pref"));

    mChannelCB = new JComboBox(ChannelList.getSubscribedChannels());


    int rowInx = 3;
    panelBuilder.add(new JLabel(mMainQuestion), cc.xyw(1,1,3));
    panelBuilder.add(mChannelCb = new JCheckBox(mChannelQuestion), cc.xy(2,rowInx));
    panelBuilder.add(mChannelCB, cc.xy(3,rowInx));
    rowInx+=2;
    if (!(mFavorite instanceof TitleFavorite)) {
      panelBuilder.add(mTitleCb, cc.xy(2,rowInx));
      panelBuilder.add(mTitleTf, cc.xy(3,rowInx));
      rowInx+=2;
    }
    panelBuilder.add(mTopicCb = new JCheckBox(mTopicQuestion), cc.xy(2,rowInx));
    panelBuilder.add(mTopicTf = new JTextField(), cc.xy(3,rowInx));

    rowInx+=2;
    panelBuilder.add(mDayCb = new JCheckBox(mDayQuestion), cc.xy(2,rowInx));
    panelBuilder.add(mDayChooser, cc.xy(3,rowInx));


    rowInx+=2;
    panelBuilder.add(mTimeCb = new JCheckBox(mTimeQuestion), cc.xy(2, rowInx));
    panelBuilder.add(mTimePeriodChooser = new TimePeriodChooser(TimePeriodChooser.ALIGN_LEFT), cc.xy(3, rowInx));



    if (mMode == MODE_CREATE_DERIVED_FROM_PROGRAM && mProgram != null) {
      mChannelCb.setSelected(true);
      mTitleTf.setText(mProgram.getTitle());
      mChannelCB.setSelectedItem(mProgram.getChannel());
      int timeFrom = (mProgram.getHours()-1) *60;
      int timeTo = (mProgram.getHours()+1) *60;
      if (timeFrom <0) {
        timeFrom = 0;
      }
      if (timeTo > 24*60 - 1) {
        timeTo = 24*60 - 1;
      }
      mTimePeriodChooser.setFromTime(timeFrom);
      mTimePeriodChooser.setToTime(timeTo);
      mDayChooser.setSelectedItem(new Integer(mProgram.getDate().getCalendar().get(Calendar.DAY_OF_WEEK)));
    }
    else if (mMode == MODE_EDIT_EXCLUSION) {
      String title = mExclusion.getTitle();
      String topic = mExclusion.getTopic();
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
      if (channel !=null) {
        mChannelCb.setSelected(true);
        mChannelCB.setSelectedItem(channel);
      }
      if (timeFrom >=0 && timeTo >= 0) {
        mTimeCb.setSelected(true);
        mTimePeriodChooser.setFromTime(timeFrom);
        mTimePeriodChooser.setToTime(timeTo);
        mDayChooser.setSelectedItem(new Integer(mExclusion.getDayOfWeek()));
      }
      if (dayOfWeek != Exclusion.DAYLIMIT_DAILY) {
        mDayCb.setSelected(true);
        mDayChooser.setSelectedItem(new Integer(dayOfWeek));
      }
    }

    updateButtons(handler);

    mChannelCb.addItemListener(new ItemListener(){
      public void itemStateChanged(ItemEvent e) {
        updateButtons(handler);
      }
    });

    mTitleCb.addItemListener(new ItemListener(){
      public void itemStateChanged(ItemEvent e) {
        updateButtons(handler);
      }
    });

    mTopicCb.addItemListener(new ItemListener(){
      public void itemStateChanged(ItemEvent e) {
        updateButtons(handler);
      }
    });

    mTimeCb.addItemListener(new ItemListener(){
      public void itemStateChanged(ItemEvent e) {
        updateButtons(handler);
      }
    });

    mDayCb.addItemListener(new ItemListener(){
      public void itemStateChanged(ItemEvent e) {
        updateButtons(handler);
      }
    });




    return panelBuilder.getPanel();
  }

  private void updateButtons(WizardHandler handler) {
    boolean allowNext = false;
    if (mChannelCb.isSelected()) {
      allowNext = true;
    }

    if (mTopicCb.isSelected()) {
      allowNext = true;
    }

    if (mTimeCb.isSelected()) {
      allowNext = true;
    }
    
    if (!(mFavorite instanceof TitleFavorite)) {
      if (mTitleCb.isSelected()) {
        allowNext = true;
      }
      mTitleTf.setEnabled(mTitleCb.isSelected());
    }

    if (mDayCb.isSelected()) {
      allowNext = true;
    }

    mChannelCB.setEnabled(mChannelCb.isSelected());
    mTopicTf.setEnabled(mTopicCb.isSelected());
    mTimePeriodChooser.setEnabled(mTimeCb.isSelected());
    mDayChooser.setEnabled(mDayCb.isSelected());

    handler.allowFinish(allowNext);
  }


  public Object createDataObject(Object obj) {
    String title = null;
    String topic = null;
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

    if (mChannelCb.isSelected()) {
      channel = (Channel)mChannelCB.getSelectedItem();
    }

    if (mTimeCb.isSelected()) {
      timeFrom = mTimePeriodChooser.getFromTime();
      timeTo = mTimePeriodChooser.getToTime();
    }

    if (mDayCb.isSelected()) {
      weekOfDay = ((Integer)mDayChooser.getSelectedItem()).intValue();
    }

    return new Exclusion(title, topic, channel, timeFrom, timeTo, weekOfDay);

  }


  public int[] getButtons() {
    return new int[]{ WizardStep.BUTTON_DONE, WizardStep.BUTTON_CANCEL};
  }

  public WizardStep next() {
    return null;
  }

  public WizardStep back() {
    return null;
  }

  public boolean isValid() {
    if (mTitleCb.isSelected() && (mTitleTf.getText()==null || mTitleTf.getText().trim().length() == 0)) {
      JOptionPane.showMessageDialog(null, mLocalizer.msg("invalidInput.noTitle","Please enter a title."));
      return false;
    }
    if (mTopicCb.isSelected() && (mTopicTf.getText()==null || mTopicTf.getText().trim().length() == 0)) {
      JOptionPane.showMessageDialog(null, mLocalizer.msg("invalidInput.noTopic","Please enter a topic."));
      return false;
    }
    return true;
  }
}
