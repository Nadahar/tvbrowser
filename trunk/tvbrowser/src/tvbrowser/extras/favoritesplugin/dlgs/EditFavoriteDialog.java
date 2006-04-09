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

package tvbrowser.extras.favoritesplugin.dlgs;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.DefaultComponentFactory;

import javax.swing.*;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;


import tvbrowser.extras.favoritesplugin.core.*;
import tvbrowser.extras.favoritesplugin.wizards.ExcludeWizardStep;
import tvbrowser.extras.favoritesplugin.wizards.WizardHandler;
import tvbrowser.extras.favoritesplugin.FavoriteConfigurator;
import tvbrowser.extras.common.ReminderConfiguration;
import tvbrowser.extras.reminderplugin.ReminderPlugin;
import tvbrowser.core.icontheme.IconLoader;
import devplugin.Channel;
import util.ui.TabLayout;
import util.ui.ChannelChooserDlg;
import util.ui.UiUtilities;
import util.ui.TimePeriodChooser;
import util.exc.TvBrowserException;
import util.exc.ErrorHandler;


public class EditFavoriteDialog extends JDialog {

  public static final util.ui.Localizer mLocalizer
    = util.ui.Localizer.getLocalizerFor(EditFavoriteDialog.class);

  private Favorite mFavorite;
  private JCheckBox mReminderAfterDownloadCb;
  private JCheckBox mUseReminderCb;
  private JCheckBox mLimitChannelCb;
  private JCheckBox mLimitTimeCb;
  private JButton mChangeChannelsBtn;
  private JLabel mChannelLabel;

  private Channel[] mChannelArr;

  private JButton mNewExclusionBtn;
  private JButton mEditExclusionBtn;
  private JButton mDeleteExclusionBtn;
  private JList mExclusionsList;

  private TimePeriodChooser mTimePeriodChooser;

  private boolean mOkWasPressed;
  private FavoriteConfigurator mFavoriteConfigurator;


  public EditFavoriteDialog(Frame parent, Favorite fav) {
    super(parent, true);
    init(fav);
  }

  public EditFavoriteDialog(Dialog parent, Favorite fav) {
    super(parent, true);
    init(fav);
  }


  private void init(Favorite fav) {

    mOkWasPressed = false;
    mFavorite = fav;
    mFavoriteConfigurator = mFavorite.createConfigurator();

    setTitle(mLocalizer.msg("title","Edit Favorite"));
    Container rootPn = getContentPane();
    rootPn.setLayout(new BorderLayout());

    JPanel content = new JPanel(new TabLayout(1));
    content.setBorder(new EmptyBorder(10,10,10,10));


    content.add(DefaultComponentFactory.getInstance().createSeparator(mLocalizer.msg("section.head","Favorite")));

    content.add(mFavoriteConfigurator.createConfigurationPanel());


    content.add(DefaultComponentFactory.getInstance().createSeparator(mLocalizer.msg("section.details","Details")));
    content.add(createLimitPanel());

    content.add(DefaultComponentFactory.getInstance().createSeparator(mLocalizer.msg("section.exclusions","Exclusion Criteria")));
    content.add(createExclusionPanel());

    content.add(DefaultComponentFactory.getInstance().createSeparator(mLocalizer.msg("section.reminder","Reminder")));
    content.add(createReminderPanel());

    content.add(DefaultComponentFactory.getInstance().createSeparator(mLocalizer.msg("section.extras","Extras")));
    content.add(createExtrasPanel());


    JPanel buttonPn = new JPanel();
    JButton cancelBtn = new JButton(mLocalizer.msg("cancel","Cancel"));
    JButton okBtn = new JButton(mLocalizer.msg("ok","OK"));

    buttonPn.add(okBtn);
    buttonPn.add(cancelBtn);

    cancelBtn.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        hide();
      }
    });

    okBtn.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        saveAndClose();
      }
    });

    rootPn.add(BorderLayout.NORTH,  content);
    rootPn.add(BorderLayout.SOUTH, buttonPn);

    pack();
  }


  //private JPanel createHeaderPanel() {



   /* if (mFavorite instanceof AdvancedFavorite) {
      return new JPanel();
    }
    else {

      String searchText = mFavorite.getSearchFormSettings().getSearchText();
      mSearchTextTf = new JTextField(searchText);
      if (mFavorite instanceof TitleFavorite) {
        JPanel panel = new JPanel(new GridLayout(-1, 1));
        panel.add(new JLabel(mLocalizer.msg("title-favorite.term","Any program whose title contains this term will be marked as a favorite:")));

        panel.add(mSearchTextTf);
        return panel;
      }
      else if (mFavorite instanceof TopicFavorite) {
        JPanel panel = new JPanel(new GridLayout(-1, 1));
        panel.add(new JLabel(mLocalizer.msg("topic-favorite.term","Any program containing this term will be marked as a favorite:")));
        panel.add(mSearchTextTf);
        return panel;
      }
      else if (mFavorite instanceof ActorsFavorite) {
        JPanel panel = new JPanel();
        panel.add(new JLabel("Sendungen werden als Favorite markiert, falls einer dieser Schauspieler vorkommt:"));
        return panel;
      }
      return new JPanel();
    }  */
   // return null;
  //}


  private String getChannelString(Channel[] channelArr) {
    if (channelArr != null && channelArr.length > 0) {
      StringBuffer buf = new StringBuffer();
      for (int i=0; i<channelArr.length-1; i++) {
        buf.append(channelArr[i]).append(", ");
      }
      if (channelArr.length > 0) {
        buf.append(channelArr[channelArr.length-1]);
      }
      String result = buf.toString();
      if (result.length() > 50) {
        result = result.substring(0, 50);
        int inx = result.lastIndexOf(",");
        result = result.substring(0, inx) +", ...";
      }
      return result;
    }
    else {
      return mLocalizer.msg("allChannels","All channels");
    }
  }

  private void setLimitChannelEnabled(boolean enabled) {
    mChangeChannelsBtn.setEnabled(enabled);
    mChannelLabel.setEnabled(enabled);
    mChannelLabel.setText(getChannelString(mChannelArr));
  }



  private JPanel createLimitPanel() {

    int from, to;
    if (mFavorite.getLimitationConfiguration().isLimitedByTime()) {
      from = mFavorite.getLimitationConfiguration().getTimeFrom();
      to = mFavorite.getLimitationConfiguration().getTimeTo();
    }
    else {
      from = 0;
      to = 24*60 - 1;
    }

    mTimePeriodChooser = new TimePeriodChooser(from, to, TimePeriodChooser.ALIGN_RIGHT);

    mChangeChannelsBtn = new JButton(mLocalizer.msg("change","Change"));
    mChannelArr = mFavorite.getLimitationConfiguration().getChannels();
    mChannelLabel = new JLabel(getChannelString(mChannelArr));


    mLimitChannelCb = new JCheckBox(mLocalizer.msg("channels","Channels:"));
    mLimitTimeCb = new JCheckBox(mLocalizer.msg("time","Time:"));

    boolean isLimitedByChannel = mFavorite.getLimitationConfiguration().isLimitedByChannel();
    boolean isLimitedByTime = mFavorite.getLimitationConfiguration().isLimitedByTime();

    mLimitChannelCb.setSelected(isLimitedByChannel);
    mLimitTimeCb.setSelected(isLimitedByTime);

    setLimitChannelEnabled(isLimitedByChannel);
    mTimePeriodChooser.setEnabled(isLimitedByTime);

    mLimitChannelCb.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        setLimitChannelEnabled(mLimitChannelCb.isSelected());
      }
    });

    mLimitTimeCb.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        mTimePeriodChooser.setEnabled(mLimitTimeCb.isSelected());
      }
    });




    mChangeChannelsBtn.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        ChannelChooserDlg dlg = new ChannelChooserDlg(EditFavoriteDialog.this, mChannelArr, "bla bla bla");
        UiUtilities.centerAndShow(dlg);
        mChannelArr = dlg.getChannels();
        if (mChannelArr.length == 0) {
          mLimitChannelCb.setSelected(false);
          setLimitChannelEnabled(false);
        }
      }
    });


    CellConstraints cc = new CellConstraints();
    PanelBuilder panelBuilder = new PanelBuilder(
              new FormLayout(
                  "pref, pref:grow, pref",
                  "pref, 5dlu, pref"));


    panelBuilder.add(mLimitChannelCb, cc.xy(1,1));
    panelBuilder.add(mChannelLabel, cc.xy(2,1));
    panelBuilder.add(mChangeChannelsBtn, cc.xy(3,1));
    panelBuilder.add(mLimitTimeCb, cc.xy(1,3));
    panelBuilder.add(mTimePeriodChooser, cc.xyw(2,3,2));

    return panelBuilder.getPanel();
  }

  private JPanel createReminderPanel() {
    JPanel panel = new JPanel(new GridLayout(-1, 1));
    panel.add(mUseReminderCb = new JCheckBox(mLocalizer.msg("reminderWindow","Reminder window")));
    panel.add(new JCheckBox("E-Mail"));
    panel.add(new JCheckBox("ICQ"));

    String[] s = mFavorite.getReminderConfiguration().getReminderServices();
    for (int i=0; i<s.length; i++) {
      if (ReminderConfiguration.REMINDER_DEFAULT.equals(s[i])) {
        mUseReminderCb.setSelected(true);
      }
    }

    return panel;
  }

  private JPanel createExclusionPanel() {


     JPanel content = new JPanel(new FormLayout("5dlu, fill:pref:grow, 3dlu, pref",
        "pref, 3dlu, pref, 3dlu, pref, 3dlu, fill:pref:grow"));

    CellConstraints cc = new CellConstraints();


    Exclusion[] exclusions = mFavorite.getExclusions();

    DefaultListModel listModel = new DefaultListModel();
    mExclusionsList = new JList(listModel);
    for (int i=0; i<exclusions.length; i++) {
      listModel.addElement(exclusions[i]);
    }
    mExclusionsList.setCellRenderer(new ExclusionListCellRenderer());

    mExclusionsList.addListSelectionListener(new ListSelectionListener(){
      public void valueChanged(ListSelectionEvent e) {
        if (!e.getValueIsAdjusting()) {
          updateExclusionListButtons();
        }
      }
    });

    content.add(new JScrollPane(mExclusionsList), cc.xywh(2,1,1,5));


    Icon newIcon = IconLoader.getInstance().getIconFromTheme("actions", "document-new", 16);
    Icon editIcon = IconLoader.getInstance().getIconFromTheme("actions", "document-edit", 16);
    Icon deleteIcon = IconLoader.getInstance().getIconFromTheme("actions", "edit-delete", 16);


    mNewExclusionBtn = new JButton(newIcon);
    mEditExclusionBtn = new JButton(editIcon);
    mDeleteExclusionBtn = new JButton(deleteIcon);

    mNewExclusionBtn.setMargin(UiUtilities.ZERO_INSETS);
    mEditExclusionBtn.setMargin(UiUtilities.ZERO_INSETS);
    mDeleteExclusionBtn.setMargin(UiUtilities.ZERO_INSETS);

    mNewExclusionBtn.setToolTipText(mLocalizer.msg("tooltip.newExclusion","New exclusion criteria"));
    mEditExclusionBtn.setToolTipText(mLocalizer.msg("toolip.editExclusion","Edit exclusion criteria"));
    mDeleteExclusionBtn.setToolTipText(mLocalizer.msg("tooltip.deleteExclusion","Delete exclusion criteria"));

    content.add(mNewExclusionBtn, cc.xy(4,1));
    content.add(mEditExclusionBtn, cc.xy(4,3));
    content.add(mDeleteExclusionBtn, cc.xy(4,5));



    mNewExclusionBtn.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent event) {

        WizardHandler handler = new WizardHandler(getParent(), new ExcludeWizardStep(mFavorite));
        Exclusion exclusion = (Exclusion) handler.show();
        if (exclusion != null) {
          ((DefaultListModel)mExclusionsList.getModel()).addElement(exclusion);
        }

      }
    });

    mEditExclusionBtn.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        Exclusion oldExclusion = (Exclusion)mExclusionsList.getSelectedValue();
        WizardHandler handler = new WizardHandler(getParent(), new ExcludeWizardStep(mFavorite, oldExclusion));
        Exclusion newExclusion = (Exclusion) handler.show();
        if (newExclusion != null) {
          int inx = mExclusionsList.getSelectedIndex();
          ((DefaultListModel)mExclusionsList.getModel()).setElementAt(newExclusion, inx);
        }
      }
    });

    mDeleteExclusionBtn.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        Exclusion exclusion = (Exclusion)mExclusionsList.getSelectedValue();
        if (exclusion != null) {
          ((DefaultListModel)mExclusionsList.getModel()).removeElement(exclusion);
        }
      }
    });

    updateExclusionListButtons();

    return content;
  }


  private void updateExclusionListButtons() {
    Object selectedItem = mExclusionsList.getSelectedValue();
    mEditExclusionBtn.setEnabled(selectedItem!=null);
    mDeleteExclusionBtn.setEnabled(selectedItem!=null);
  }

  private JPanel createExtrasPanel() {
    JPanel panel = new JPanel(new BorderLayout());
    panel.add(mReminderAfterDownloadCb = new JCheckBox(
            mLocalizer.msg("autoAlert","Alert me, whenever a matching program is discovered")), BorderLayout.WEST);
    mReminderAfterDownloadCb.setSelected(mFavorite.isRemindAfterDownload());
    return panel;
  }

  public boolean getOkWasPressed() {
    return mOkWasPressed;
  }

  private void saveAndClose() {



    mFavoriteConfigurator.save();

    if (mLimitTimeCb.isSelected()) {
      mFavorite.getLimitationConfiguration().setTime(mTimePeriodChooser.getFromTime(), mTimePeriodChooser.getToTime());
    }
    else {
      mFavorite.getLimitationConfiguration().setIsLimitedByTime(false);
    }

    if (mLimitChannelCb.isSelected() && mChannelArr.length > 0) {
      mFavorite.getLimitationConfiguration().setChannels(mChannelArr);
    }
    else {
      mFavorite.getLimitationConfiguration().setIsLimitedByChannel(false);
    }

    int exclCnt = ((DefaultListModel)mExclusionsList.getModel()).size();
    Exclusion[] exclArr = new Exclusion[exclCnt];
    ((DefaultListModel)mExclusionsList.getModel()).copyInto(exclArr);
    mFavorite.setExclusions(exclArr);


    mFavorite.setRemindAfterDownload(mReminderAfterDownloadCb.isSelected());

    boolean wasReminderEnabled = mFavorite.getReminderConfiguration().containsService(ReminderConfiguration.REMINDER_DEFAULT);

    if (mUseReminderCb.isSelected()) {
      if (!wasReminderEnabled) {
        ReminderPlugin.getInstance().addPrograms(mFavorite.getPrograms());
      }
      mFavorite.getReminderConfiguration().setReminderServices(new String[]{ReminderConfiguration.REMINDER_DEFAULT});
    }
    else {
      if (wasReminderEnabled) {
        ReminderPlugin.getInstance().removePrograms(mFavorite.getPrograms());
      }
      mFavorite.getReminderConfiguration().setReminderServices(new String[]{});
    }




    if (mUseReminderCb.isSelected() != wasReminderEnabled) {
      ReminderPlugin.getInstance().updateRootNode();
    }

    try {
      mFavorite.updatePrograms();
    } catch (TvBrowserException exc) {
      ErrorHandler.handle(mLocalizer.msg("error.updateFavoriteFailed","Could not update favorite"), exc);
    }

    mOkWasPressed = true;
    hide();
  }



  class ExclusionListCellRenderer extends DefaultListCellRenderer {


    private String createTimeMessage(int lowBnd, int upBnd) {
      int mLow = lowBnd%60;
      int hLow = lowBnd/60;
      int mUp = upBnd%60;
      int hUp = upBnd/60;

      String lowTime = hLow+":"+(mLow<10?"0":"")+mLow;
      String upTime = hUp+":"+(mUp<10?"0":"")+mUp;

      if (lowBnd >=0 && upBnd >=0) {
        return mLocalizer.msg("timestring.between","between {0} and {1}", lowTime, upTime);
      }
      else if (lowBnd >=0) {
       return mLocalizer.msg("timestring.after","after {0}", lowTime);
      }
      else if (upBnd >=0) {
        return mLocalizer.msg("timestring.before","after {0}", upTime);
      }
      else {
        return null;
      }
    }

    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {

      JLabel defaultLabel = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

      if (value instanceof Exclusion) {
        Exclusion excl = (Exclusion)value;

        String title = excl.getTitle();
        String topic = excl.getTopic();
        Channel channel = excl.getChannel();
        String timeMsg = createTimeMessage(excl.getTimeLowerBound(), excl.getTimeUpperBound());

        String text;
        if (title == null) {
          if (topic == null) {
            if (channel == null) {
              if (timeMsg == null) {
                text = "<invalid>";
              }
              else { // timeMsg != null
                text = mLocalizer.msg("exclude.time","", timeMsg);
              }
            }
            else {  // channel != null
              if (timeMsg == null) {
                text = mLocalizer.msg("exclude.channel","", channel.getName());
              }
              else {  // timeMsg != null
                text = mLocalizer.msg("exclude.channel-time","", channel.getName(), timeMsg);
              }
            }

          }
          else {  // topic != null
            if (channel == null) {
              if (timeMsg == null) {
                text = mLocalizer.msg("exclude.topic","", topic);
              }
              else { // timeMsg != null
                text = mLocalizer.msg("exclude.topic-time","", topic, timeMsg);
              }
            }
            else { // channel != null
              if (timeMsg == null) {
                text = mLocalizer.msg("exclude.topic-channel","", topic, channel.getName());
              }
              else {  // timeMsg != null
                text = mLocalizer.msg("exclude.topic-channel-time","", topic, channel.getName(), timeMsg);
              }
            }
          }

        }else {  // title != null
          if (topic == null) {
            if (channel == null) {
              if (timeMsg == null) {
                text = mLocalizer.msg("exclude.title","", title);
              }
              else { // timeMsg != null
                text = mLocalizer.msg("exclude.title-time","", title, timeMsg);
              }
            }
            else {  // channel != null
              if (timeMsg == null) {
                text = mLocalizer.msg("exclude.title-channel","", title, channel.getName());
              }
              else {
                text = mLocalizer.msg("exclude.title-channel-time","", title, channel.getName(), timeMsg);
              }
            }
          }
          else {  // topic != null
            if (channel == null) {
              if (timeMsg == null) {
                text = mLocalizer.msg("exclude.title-topic","", title, topic);
              }
              else { // timeMsg != null
                text = mLocalizer.msg("exclude.title-topic-time","", title, topic, timeMsg);
              }
            }
            else {  // channel != null
              if (timeMsg == null) {
                text = mLocalizer.msg("exclude.title-topic-channel","", title, topic, channel.getName());
              }
              else { // timeMsg != null
                text = mLocalizer.msg("exclude.title-topic-channel-time","", new Object[]{title, topic, channel.getName(), timeMsg});
              }
            }
          }
        }
        defaultLabel.setText(text);

      }
      return defaultLabel;
    }
  }



}
