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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.*;

import tvbrowser.core.icontheme.IconLoader;
import tvbrowser.extras.common.ReminderConfiguration;
import tvbrowser.extras.favoritesplugin.FavoriteConfigurator;
import tvbrowser.extras.favoritesplugin.core.Exclusion;
import tvbrowser.extras.favoritesplugin.core.Favorite;
import tvbrowser.extras.favoritesplugin.wizards.ExcludeWizardStep;
import tvbrowser.extras.favoritesplugin.wizards.WizardHandler;
import tvbrowser.extras.common.LimitationConfiguration;
import tvbrowser.extras.common.DayListCellRenderer;
import tvbrowser.extras.reminderplugin.ReminderPlugin;
import util.exc.ErrorHandler;
import util.exc.TvBrowserException;
import util.ui.ChannelChooserDlg;
import util.ui.PluginChooserDlg;
import util.ui.TabLayout;
import util.ui.TimePeriodChooser;
import util.ui.UiUtilities;
import util.ui.WindowClosingIf;

import com.jgoodies.forms.builder.ButtonBarBuilder;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.factories.DefaultComponentFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import devplugin.Channel;
import devplugin.PluginAccess;

public class EditFavoriteDialog extends JDialog implements WindowClosingIf {

  public static final util.ui.Localizer mLocalizer = util.ui.Localizer.getLocalizerFor(EditFavoriteDialog.class);

  private Favorite mFavorite;

  private JCheckBox mReminderAfterDownloadCb;

  private JCheckBox mUseReminderCb;

  private JCheckBox mLimitChannelCb;

  private JCheckBox mLimitTimeCb;

  private JButton mChangeChannelsBtn;

  private JLabel mChannelLabel;

  private Channel[] mChannelArr;

  private JComboBox mLimitDaysCB;

  private JButton mNewExclusionBtn;

  private JButton mEditExclusionBtn;

  private JButton mDeleteExclusionBtn;

  private JList mExclusionsList;

  private TimePeriodChooser mTimePeriodChooser;

  private JCheckBox mPassProgramsCheckBox;

  private PluginAccess[] mPassProgramPlugins;

  private JLabel mPassProgramsLb;

  private JButton mChangePassProgramsBtn;

  private boolean mOkWasPressed;

  private FavoriteConfigurator mFavoriteConfigurator;
  
  private JLabel mName;

  public EditFavoriteDialog(Frame parent, Favorite fav) {
    super(parent, true);
    init(fav);
  }

  public EditFavoriteDialog(Dialog parent, Favorite fav) {
    super(parent, true);
    init(fav);
  }

  private void init(Favorite fav) {
    UiUtilities.registerForClosing(this);
    mOkWasPressed = false;
    mFavorite = fav;
    mFavoriteConfigurator = mFavorite.createConfigurator();

    setTitle(mLocalizer.msg("title", "Edit Favorite"));
    JPanel rootPn = (JPanel) getContentPane();
    rootPn.setLayout(new BorderLayout());
    rootPn.setBorder(Borders.DLU4_BORDER);
    
    JPanel content = new JPanel(new TabLayout(1));
    content.setBorder(new EmptyBorder(10, 10, 10, 10));

    content.add(DefaultComponentFactory.getInstance().createSeparator(mLocalizer.msg("section.head", "Favorite")));

    content.add(createTitleChangePanel());
    content.add(mFavoriteConfigurator.createConfigurationPanel());

    content.add(DefaultComponentFactory.getInstance().createSeparator(mLocalizer.msg("section.details", "Details")));
    content.add(createLimitPanel());

    content.add(DefaultComponentFactory.getInstance().createSeparator(
        mLocalizer.msg("section.exclusions", "Exclusion Criteria")));
    content.add(createExclusionPanel());

    content.add(DefaultComponentFactory.getInstance().createSeparator(mLocalizer.msg("section.reminder", "Reminder")));
    content.add(createReminderPanel());

    content.add(DefaultComponentFactory.getInstance().createSeparator(mLocalizer.msg("section.extras", "Extras")));
    content.add(createExtrasPanel());

    JButton cancelBtn = new JButton(mLocalizer.msg("cancel", "Cancel"));
    JButton okBtn = new JButton(mLocalizer.msg("ok", "OK"));

    cancelBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        hide();
      }
    });

    okBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        saveAndClose();
      }
    });

    ButtonBarBuilder buttons = new ButtonBarBuilder();
    buttons.addGriddedButtons(new JButton[] { okBtn, cancelBtn });

    JPanel buttonPanel = new JPanel(new BorderLayout());
    buttonPanel.add(buttons.getPanel(), BorderLayout.EAST);

    rootPn.add(BorderLayout.NORTH, content);
    rootPn.add(BorderLayout.SOUTH, buttonPanel);
        
    getRootPane().setDefaultButton(okBtn);
    
    pack();
  }
  
  private JPanel createTitleChangePanel() {
    CellConstraints cc = new CellConstraints();
    
    if(mFavorite.getName().length() < 1) {
      mName = new JLabel(mLocalizer.msg("defaultName","Is going to be created automatically"));
      mName.setEnabled(false);
    }
    else
      mName = new JLabel(mFavorite.getName());    
    
    JPanel panel = new JPanel(new FormLayout("pref,3dlu,pref:grow,3dlu,pref","pref"));
    panel.add(new JLabel(mLocalizer.msg("name","Name:")), cc.xy(1,1));
    panel.add(mName, cc.xy(3,1));    
    
    JButton changeTitle = new JButton(mLocalizer.msg("changeName","Change name"));
    changeTitle.setFocusable(false);
    
    changeTitle.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        setFavoriteName();
      }
    });
    
    panel.add(changeTitle, cc.xy(5,1));
    
    return panel;
  }
  
  private void setFavoriteName() {
    String newName = (String) JOptionPane.showInputDialog(this,
        mLocalizer.msg("name","Name:"), mLocalizer.msg("renameFav","Rename Favorite"), JOptionPane.PLAIN_MESSAGE, null, null,
        mName.getText());
    if (newName != null && newName.length() > 0 && 
        (newName.compareTo(mLocalizer.msg("defaultName","Is going to be created automatically")) != 0)) {
      mName.setText(newName);
      mName.setEnabled(true);          
    }
    else if(mName.getText().compareTo(mLocalizer.msg("defaultName","Is going to be created automatically")) == 0)
      mName.setEnabled(false);    
  }

  private String getChannelString(Channel[] channelArr) {
    if (channelArr != null && channelArr.length > 0) {
      StringBuffer buf = new StringBuffer();
      for (int i = 0; i < channelArr.length - 1; i++) {
        buf.append(channelArr[i]).append(", ");
      }
      if (channelArr.length > 0) {
        buf.append(channelArr[channelArr.length - 1]);
      }
      String result = buf.toString();
      if (result.length() > 50) {
        result = result.substring(0, 50);
        int inx = result.lastIndexOf(",");
        result = result.substring(0, inx) + ", ...";
      }
      return result;
    } else {
      return mLocalizer.msg("allChannels", "All channels");
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
    } else {
      from = 0;
      to = 24 * 60 - 1;
    }

    mTimePeriodChooser = new TimePeriodChooser(from, to, TimePeriodChooser.ALIGN_RIGHT);

    mChangeChannelsBtn = new JButton(mLocalizer.msg("change", "Change"));
    mChannelArr = mFavorite.getLimitationConfiguration().getChannels();
    mChannelLabel = new JLabel(getChannelString(mChannelArr));

    mLimitChannelCb = new JCheckBox(mLocalizer.msg("channels", "Channels:"));
    mLimitTimeCb = new JCheckBox(mLocalizer.msg("time", "Time:"));

    mLimitDaysCB = new JComboBox(new Object[] { new Integer(LimitationConfiguration.DAYLIMIT_DAILY),
        new Integer(LimitationConfiguration.DAYLIMIT_WEEKDAY), new Integer(LimitationConfiguration.DAYLIMIT_WEEKEND),
        new Integer(LimitationConfiguration.DAYLIMIT_MONDAY), new Integer(LimitationConfiguration.DAYLIMIT_TUESDAY),
        new Integer(LimitationConfiguration.DAYLIMIT_WEDNESDAY),
        new Integer(LimitationConfiguration.DAYLIMIT_THURSDAY), new Integer(LimitationConfiguration.DAYLIMIT_FRIDAY),
        new Integer(LimitationConfiguration.DAYLIMIT_SATURDAY), new Integer(LimitationConfiguration.DAYLIMIT_SUNDAY), });
    mLimitDaysCB.setRenderer(new DayListCellRenderer());
    mLimitDaysCB.setSelectedItem(new Integer((mFavorite.getLimitationConfiguration().getDayLimit())));

    boolean isLimitedByChannel = mFavorite.getLimitationConfiguration().isLimitedByChannel();
    boolean isLimitedByTime = mFavorite.getLimitationConfiguration().isLimitedByTime();

    mLimitChannelCb.setSelected(isLimitedByChannel);
    mLimitTimeCb.setSelected(isLimitedByTime);

    setLimitChannelEnabled(isLimitedByChannel);
    mTimePeriodChooser.setEnabled(isLimitedByTime);
    mLimitDaysCB.setEnabled(mLimitTimeCb.isSelected());

    mLimitChannelCb.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        setLimitChannelEnabled(mLimitChannelCb.isSelected());
      }
    });

    mLimitTimeCb.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        mTimePeriodChooser.setEnabled(mLimitTimeCb.isSelected());
        mLimitDaysCB.setEnabled(mLimitTimeCb.isSelected());
      }
    });

    mChangeChannelsBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        ChannelChooserDlg dlg = new ChannelChooserDlg(EditFavoriteDialog.this, mChannelArr, null,
            ChannelChooserDlg.SELECTABLE_ITEM_LIST);
        UiUtilities.centerAndShow(dlg);
        Channel[] chArr = dlg.getChannels();
        if (chArr != null) {
          mChannelArr = dlg.getChannels();
          if (mChannelArr.length == 0) {
            mLimitChannelCb.setSelected(false);
            setLimitChannelEnabled(false);
          }
          mChannelLabel.setText(getChannelString(mChannelArr));
        }
      }
    });

    JPanel limitPn = new JPanel(new BorderLayout());
    JPanel pn = new JPanel();
    pn.add(mTimePeriodChooser);
    pn.add(mLimitDaysCB);
    limitPn.add(pn, BorderLayout.EAST);

    CellConstraints cc = new CellConstraints();
    PanelBuilder panelBuilder = new PanelBuilder(new FormLayout("pref, pref:grow, pref", "pref, 5dlu, pref"));

    panelBuilder.add(mLimitChannelCb, cc.xy(1, 1));
    panelBuilder.add(mChannelLabel, cc.xy(2, 1));
    panelBuilder.add(mChangeChannelsBtn, cc.xy(3, 1));
    panelBuilder.add(mLimitTimeCb, cc.xy(1, 3));
    panelBuilder.add(limitPn, cc.xyw(2, 3, 2));

    return panelBuilder.getPanel();
  }

  private JPanel createReminderPanel() {
    JPanel panel = new JPanel(new GridLayout(-1, 1));
    panel.add(mUseReminderCb = new JCheckBox(mLocalizer.msg("reminderWindow", "Reminder window")));

    String[] s = mFavorite.getReminderConfiguration().getReminderServices();
    for (int i = 0; i < s.length; i++) {
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
    for (int i = 0; i < exclusions.length; i++) {
      listModel.addElement(exclusions[i]);
    }
    mExclusionsList.setCellRenderer(new ExclusionListCellRenderer());

    mExclusionsList.addListSelectionListener(new ListSelectionListener() {
      public void valueChanged(ListSelectionEvent e) {
        if (!e.getValueIsAdjusting()) {
          updateExclusionListButtons();
        }
      }
    });

    content.add(new JScrollPane(mExclusionsList), cc.xywh(2, 1, 1, 5));

    Icon newIcon = IconLoader.getInstance().getIconFromTheme("actions", "document-new", 16);
    Icon editIcon = IconLoader.getInstance().getIconFromTheme("actions", "document-edit", 16);
    Icon deleteIcon = IconLoader.getInstance().getIconFromTheme("actions", "edit-delete", 16);

    mNewExclusionBtn = new JButton(newIcon);
    mEditExclusionBtn = new JButton(editIcon);
    mDeleteExclusionBtn = new JButton(deleteIcon);

    mNewExclusionBtn.setMargin(UiUtilities.ZERO_INSETS);
    mEditExclusionBtn.setMargin(UiUtilities.ZERO_INSETS);
    mDeleteExclusionBtn.setMargin(UiUtilities.ZERO_INSETS);

    mNewExclusionBtn.setToolTipText(mLocalizer.msg("tooltip.newExclusion", "New exclusion criteria"));
    mEditExclusionBtn.setToolTipText(mLocalizer.msg("toolip.editExclusion", "Edit exclusion criteria"));
    mDeleteExclusionBtn.setToolTipText(mLocalizer.msg("tooltip.deleteExclusion", "Delete exclusion criteria"));

    content.add(mNewExclusionBtn, cc.xy(4, 1));
    content.add(mEditExclusionBtn, cc.xy(4, 3));
    content.add(mDeleteExclusionBtn, cc.xy(4, 5));

    final EditFavoriteDialog parent = this;
    
    mNewExclusionBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {

        WizardHandler handler = new WizardHandler(parent, new ExcludeWizardStep(mFavorite));
        Exclusion exclusion = (Exclusion) handler.show();
        if (exclusion != null) {
          ((DefaultListModel) mExclusionsList.getModel()).addElement(exclusion);
        }

      }
    });

    mEditExclusionBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        Exclusion oldExclusion = (Exclusion) mExclusionsList.getSelectedValue();
        WizardHandler handler = new WizardHandler(parent, new ExcludeWizardStep(mFavorite, oldExclusion));
        Exclusion newExclusion = (Exclusion) handler.show();
        if (newExclusion != null) {
          int inx = mExclusionsList.getSelectedIndex();
          ((DefaultListModel) mExclusionsList.getModel()).setElementAt(newExclusion, inx);
        }
      }
    });

    mDeleteExclusionBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        Exclusion exclusion = (Exclusion) mExclusionsList.getSelectedValue();
        if (exclusion != null) {
          ((DefaultListModel) mExclusionsList.getModel()).removeElement(exclusion);
        }
      }
    });

    updateExclusionListButtons();

    return content;
  }

  private void updateExclusionListButtons() {
    Object selectedItem = mExclusionsList.getSelectedValue();
    mEditExclusionBtn.setEnabled(selectedItem != null);
    mDeleteExclusionBtn.setEnabled(selectedItem != null);
  }

  private String getForwardPluginsLabelString(PluginAccess[] pluginArr) {
    if (pluginArr != null && pluginArr.length > 0) {
      StringBuffer buf = new StringBuffer();
      if (pluginArr.length > 0) {
        buf.append(pluginArr[0].getInfo().getName());
      }
      if (pluginArr.length > 1) {
        buf.append(", ");
        buf.append(pluginArr[1].getInfo().getName());
      }
      if (pluginArr.length > 2) {
        buf.append(" (");
        buf.append(pluginArr.length - 2);
        buf.append(" ");
        buf.append(mLocalizer.msg("more", "more"));
        buf.append("...)");
      }
      return buf.toString();
    } else {
      return mLocalizer.msg("dontpass", "don't pass programs");
    }
  }

  private JPanel createExtrasPanel() {

    JPanel panel = new JPanel(new FormLayout("pref, pref:grow, pref", "pref,3dlu,pref"));
    CellConstraints cc = new CellConstraints();

    mPassProgramPlugins = mFavorite.getForwardPlugins();
    mPassProgramsLb = new JLabel(getForwardPluginsLabelString(mPassProgramPlugins));
    mChangePassProgramsBtn = new JButton(mLocalizer.msg("change", "Change"));
    mChangePassProgramsBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        PluginChooserDlg dlg = new PluginChooserDlg(EditFavoriteDialog.this, mPassProgramPlugins, null);
        UiUtilities.centerAndShow(dlg);
        PluginAccess[] pluginArr = dlg.getPlugins();
        if (pluginArr != null) {
          mPassProgramPlugins = pluginArr;
          mPassProgramsLb.setText(getForwardPluginsLabelString(mPassProgramPlugins));
          if (pluginArr.length == 0) {
            mPassProgramsCheckBox.setSelected(false);
            updatePassProgramsPanel();
          }
        }

      }
    });

    panel.add(mReminderAfterDownloadCb = new JCheckBox(mLocalizer.msg("autoAlert",
        "Alert me, whenever a matching program is discovered")), cc.xyw(1, 1, 2));

    panel.add(mPassProgramsCheckBox = new JCheckBox(mLocalizer.msg("passProgramsTo", "Pass programs to")), cc.xy(1, 3));
    panel.add(mPassProgramsLb, cc.xy(2, 3));
    panel.add(mChangePassProgramsBtn, cc.xy(3, 3));
    mReminderAfterDownloadCb.setSelected(mFavorite.isRemindAfterDownload());

    mPassProgramsCheckBox.setSelected(mPassProgramPlugins != null && mPassProgramPlugins.length > 0);

    mPassProgramsCheckBox.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        updatePassProgramsPanel();
      }
    });

    updatePassProgramsPanel();

    return panel;
  }

  private void updatePassProgramsPanel() {
    mPassProgramsLb.setEnabled(mPassProgramsCheckBox.isSelected());
    mChangePassProgramsBtn.setEnabled(mPassProgramsCheckBox.isSelected());
    if (!mPassProgramsCheckBox.isSelected()) {
      mPassProgramPlugins = new PluginAccess[] {};
    }

  }

  public boolean getOkWasPressed() {
    return mOkWasPressed;
  }

  private void saveAndClose() {
    mFavoriteConfigurator.save();

    if (mLimitTimeCb.isSelected()) {
      mFavorite.getLimitationConfiguration().setTime(mTimePeriodChooser.getFromTime(), mTimePeriodChooser.getToTime());
      mFavorite.getLimitationConfiguration().setDayLimit(((Integer) mLimitDaysCB.getSelectedItem()).intValue());
    } else {
      mFavorite.getLimitationConfiguration().setIsLimitedByTime(false);
      mFavorite.getLimitationConfiguration().setDayLimit(LimitationConfiguration.DAYLIMIT_DAILY);
    }

    if (mLimitChannelCb.isSelected() && mChannelArr.length > 0) {
      mFavorite.getLimitationConfiguration().setChannels(mChannelArr);
    } else {
      mFavorite.getLimitationConfiguration().setIsLimitedByChannel(false);
    }

    mFavorite.setForwardPlugins(mPassProgramPlugins);

    int exclCnt = ((DefaultListModel) mExclusionsList.getModel()).size();
    Exclusion[] exclArr = new Exclusion[exclCnt];
    ((DefaultListModel) mExclusionsList.getModel()).copyInto(exclArr);
    mFavorite.setExclusions(exclArr);

    mFavorite.setRemindAfterDownload(mReminderAfterDownloadCb.isSelected());

    boolean wasReminderEnabled = mFavorite.getReminderConfiguration().containsService(
        ReminderConfiguration.REMINDER_DEFAULT);

    if (mUseReminderCb.isSelected()) {
      mFavorite.getReminderConfiguration().setReminderServices(new String[] { ReminderConfiguration.REMINDER_DEFAULT });
    } else {
      if (wasReminderEnabled) {
        ReminderPlugin.getInstance().removePrograms(mFavorite.getPrograms());
      }
      mFavorite.getReminderConfiguration().setReminderServices(new String[] {});
    }

    try {
      mFavorite.updatePrograms();
    } catch (TvBrowserException exc) {
      ErrorHandler.handle(mLocalizer.msg("error.updateFavoriteFailed", "Could not update favorite"), exc);
    }

    for (int i = 0; i < mPassProgramPlugins.length; i++) {
      mPassProgramPlugins[i].receivePrograms(mFavorite.getPrograms());
    }

    if (mUseReminderCb.isSelected()) {
      ReminderPlugin.getInstance().addPrograms(mFavorite.getPrograms());
      ReminderPlugin.getInstance().updateRootNode();
    }
    
    if(mName.getText().length() > 0 && mName.getText().compareTo(mLocalizer.msg("defaultName","Is going to be created automatically")) != 0)
      mFavorite.setName(mName.getText());
    
    mOkWasPressed = true;
    hide();
  }

  class ExclusionListCellRenderer extends DefaultListCellRenderer {

    private String createTimeMessage(int lowBnd, int upBnd, int dayOfWeek) {
      int mLow = lowBnd % 60;
      int hLow = lowBnd / 60;
      int mUp = upBnd % 60;
      int hUp = upBnd / 60;

      String lowTime = hLow + ":" + (mLow < 10 ? "0" : "") + mLow;
      String upTime = hUp + ":" + (mUp < 10 ? "0" : "") + mUp;

      if (dayOfWeek != Exclusion.DAYLIMIT_DAILY) {
        String dayStr = DayListCellRenderer.getDayString(dayOfWeek);
        if (lowBnd >= 0 && upBnd >= 0) {
          return mLocalizer.msg("datetimestring.between", "on {0} between {1} and {2}", dayStr, lowTime, upTime);
        } else if (lowBnd >= 0) {
          return mLocalizer.msg("datetimestring.after", "on {0} after {1}", dayStr, lowTime);
        } else if (upBnd >= 0) {
          return mLocalizer.msg("datetimestring.before", "on {0} after {1}", dayStr, upTime);
        } else {
          return mLocalizer.msg("datetimestring.on", "on {0}", dayStr);
        }
      } else {
        if (lowBnd >= 0 && upBnd >= 0) {
          return mLocalizer.msg("timestring.between", "on {0} between {1} and {2}", lowTime, upTime);
        } else if (lowBnd >= 0) {
          return mLocalizer.msg("timestring.after", "on {0} after {1}", lowTime);
        } else if (upBnd >= 0) {
          return mLocalizer.msg("timestring.before", "on {0} after {1}", upTime);
        } else {
          return null;
        }
      }
    }

    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
        boolean cellHasFocus) {

      JLabel defaultLabel = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

      if (value instanceof Exclusion) {
        Exclusion excl = (Exclusion) value;

        String title = excl.getTitle();
        String topic = excl.getTopic();
        Channel channel = excl.getChannel();
        String timeMsg = createTimeMessage(excl.getTimeLowerBound(), excl.getTimeUpperBound(), excl.getDayOfWeek());

        String text;
        if (title == null) {
          if (topic == null) {
            if (channel == null) {
              if (timeMsg == null) {
                text = "<invalid>";
              } else { // timeMsg != null
                text = mLocalizer.msg("exclude.time", "", timeMsg);
              }
            } else { // channel != null
              if (timeMsg == null) {
                text = mLocalizer.msg("exclude.channel", "", channel.getName());
              } else { // timeMsg != null
                text = mLocalizer.msg("exclude.channel-time", "", channel.getName(), timeMsg);
              }
            }

          } else { // topic != null
            if (channel == null) {
              if (timeMsg == null) {
                text = mLocalizer.msg("exclude.topic", "", topic);
              } else { // timeMsg != null
                text = mLocalizer.msg("exclude.topic-time", "", topic, timeMsg);
              }
            } else { // channel != null
              if (timeMsg == null) {
                text = mLocalizer.msg("exclude.topic-channel", "", topic, channel.getName());
              } else { // timeMsg != null
                text = mLocalizer.msg("exclude.topic-channel-time", "", topic, channel.getName(), timeMsg);
              }
            }
          }

        } else { // title != null
          if (topic == null) {
            if (channel == null) {
              if (timeMsg == null) {
                text = mLocalizer.msg("exclude.title", "", title);
              } else { // timeMsg != null
                text = mLocalizer.msg("exclude.title-time", "", title, timeMsg);
              }
            } else { // channel != null
              if (timeMsg == null) {
                text = mLocalizer.msg("exclude.title-channel", "", title, channel.getName());
              } else {
                text = mLocalizer.msg("exclude.title-channel-time", "", title, channel.getName(), timeMsg);
              }
            }
          } else { // topic != null
            if (channel == null) {
              if (timeMsg == null) {
                text = mLocalizer.msg("exclude.title-topic", "", title, topic);
              } else { // timeMsg != null
                text = mLocalizer.msg("exclude.title-topic-time", "", title, topic, timeMsg);
              }
            } else { // channel != null
              if (timeMsg == null) {
                text = mLocalizer.msg("exclude.title-topic-channel", "", title, topic, channel.getName());
              } else { // timeMsg != null
                text = mLocalizer.msg("exclude.title-topic-channel-time", "", new Object[] { title, topic,
                    channel.getName(), timeMsg });
              }
            }
          }
        }
        defaultLabel.setText(text);

      }
      return defaultLabel;
    }
  }



  /*
   * (non-Javadoc)
   *
   * @see util.ui.WindowClosingIf#close()
   */
  public void close() {
    hide();
  }

}