package tvbrowser.extras.favoritesplugin.wizards;

import tvbrowser.extras.favoritesplugin.core.Favorite;
import tvbrowser.extras.favoritesplugin.core.Exclusion;
import tvbrowser.extras.favoritesplugin.core.TitleFavorite;
import tvbrowser.core.ChannelList;

import javax.swing.*;


import devplugin.Program;
import devplugin.Channel;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.builder.PanelBuilder;

import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;

import util.ui.TimePeriodChooser;

public class ExcludeWizardStep implements WizardStep {

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
  private JTextField mTitleTf;
  private JTextField mTopicTf;
  private JComboBox mChannelCB;
  private TimePeriodChooser mTimePeriodChooser;

  private String mMainQuestion;
  private String mChannelQuestion;
  private String mTopicQuestion;
  private String mTimeQuestion;
  private String mTitleQuestion;

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
    }
    else {
      mMainQuestion = mLocalizer.msg("mainQuestion.create","Warum gehoert diese Sendung nicht zur Lieblingssendung '{0}'?", mFavorite.getName());
      mChannelQuestion = mLocalizer.msg("channelQuestion.create","Falscher Sender:");
      mTopicQuestion = mLocalizer.msg("topicQuestion.create","Falsches Stichwort:");
      mTimeQuestion = mLocalizer.msg("timeQuestion.create","Falsche Beginnzeit:");
    }

    mTitleQuestion = mLocalizer.msg("titleQuestion","Falscher Titel:");


  }

  public String getTitle() {
    return mLocalizer.msg("title","Exclude Programs");
  }

  public JPanel getContent(final WizardHandler handler) {



    mTitleCb = new JCheckBox(mTitleQuestion);
    mTitleTf = new JTextField();

    CellConstraints cc = new CellConstraints();
    PanelBuilder panelBuilder = new PanelBuilder(
            new FormLayout(
                "5dlu, pref, pref:grow",
                "pref, 5dlu, pref, 5dlu, pref, 5dlu, pref, 5dlu, pref"));

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
    panelBuilder.add(mTimeCb = new JCheckBox(mTimeQuestion), cc.xy(2, rowInx));
    panelBuilder.add(mTimePeriodChooser = new TimePeriodChooser(TimePeriodChooser.ALIGN_LEFT), cc.xy(3, rowInx));

    mChannelCb.setSelected(true);

    if (mMode == MODE_CREATE_DERIVED_FROM_PROGRAM && mProgram != null) {
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
    }
    else if (mMode == MODE_EDIT_EXCLUSION) {
      String title = mExclusion.getTitle();
      String topic = mExclusion.getTopic();
      Channel channel = mExclusion.getChannel();
      int timeFrom = mExclusion.getTimeLowerBound();
      int timeTo = mExclusion.getTimeUpperBound();
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
      }
    }



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

    if (!(mFavorite instanceof TitleFavorite)) {
      if (mTitleCb.isSelected()) {
        allowNext = true;
      }
    }
    handler.allowFinish(allowNext);
  }


  public Object createDataObject(Object obj) {
    String title = null;
    String topic = null;
    Channel channel = null;
    int timeFrom = -1;
    int timeTo = -1;

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

    return new Exclusion(title, topic, channel, timeFrom, timeTo);

  }


  public int[] getButtons() {
    return new int[]{ WizardStep.BUTTON_DONE, WizardStep.BUTTON_CANCEL};
  }

  public WizardStep next() {
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
