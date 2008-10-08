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
import java.awt.Color;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import tvbrowser.extras.common.ReminderConfiguration;
import tvbrowser.extras.favoritesplugin.FavoriteConfigurator;
import tvbrowser.extras.favoritesplugin.FavoritesPlugin;
import tvbrowser.extras.favoritesplugin.core.Favorite;
import tvbrowser.extras.common.LimitationConfiguration;
import tvbrowser.extras.common.DayListCellRenderer;
import tvbrowser.extras.reminderplugin.ReminderPlugin;
import tvbrowser.extras.reminderplugin.ReminderPluginProxy;
import util.exc.ErrorHandler;
import util.exc.TvBrowserException;
import util.ui.ChannelChooserDlg;
import util.ui.Localizer;
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
import devplugin.ProgramReceiveIf;
import devplugin.ProgramReceiveTarget;

/**
 * A class for editing favorites.
 */
public class EditFavoriteDialog extends JDialog implements WindowClosingIf {

  private static final util.ui.Localizer mLocalizer = util.ui.Localizer.getLocalizerFor(EditFavoriteDialog.class);

  private Favorite mFavorite;

  private JCheckBox mReminderAfterDownloadCb;

  private JCheckBox mUseReminderCb;

  private JCheckBox mLimitChannelCb;

  private JCheckBox mLimitTimeCb;

  private JButton mChangeChannelsBtn;

  private JLabel mChannelLabel;

  private Channel[] mChannelArr;

  private JComboBox mLimitDaysCB;
  
  private TimePeriodChooser mTimePeriodChooser;

  private JCheckBox mPassProgramsCheckBox;

  private ProgramReceiveTarget[] mPassProgramPlugins;

  private JLabel mPassProgramsLb;

  private JButton mChangePassProgramsBtn;

  private boolean mOkWasPressed;

  private FavoriteConfigurator mFavoriteConfigurator;
  
  private JLabel mName;
  
  private ExclusionPanel mExclusionPanel;

  /**
   * Creates an instance of this dialog.
   * <p>
   * @param parent The parent frame of this dialog.
   * @param fav The favorite for this dialog.
   */
  public EditFavoriteDialog(Frame parent, Favorite fav) {
    super(parent, true);
    init(fav);
  }

  /**
   * Creates an instance of this dialog.
   * <p>
   * @param parent The parent dialog of this dialog.
   * @param fav The favorite for this dialog.
   */
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
    content.add(mExclusionPanel = new ExclusionPanel(mFavorite.getExclusions(),this,mFavorite)/*createExclusionPanel()*/);

    content.add(DefaultComponentFactory.getInstance().createSeparator(mLocalizer.msg("section.reminder", "Reminder")));
    content.add(createReminderPanel());

    content.add(DefaultComponentFactory.getInstance().createSeparator(mLocalizer.msg("section.extras", "Extras")));
    content.add(createExtrasPanel());

    JButton cancelBtn = new JButton(Localizer.getLocalization(Localizer.I18N_CANCEL));
    JButton okBtn = new JButton(Localizer.getLocalization(Localizer.I18N_OK));

    cancelBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        setVisible(false);
      }
    });

    okBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        saveAndClose();
      }
    });

    ButtonBarBuilder buttons = new ButtonBarBuilder();
    buttons.addGlue();
    buttons.addGriddedButtons(new JButton[] { okBtn, cancelBtn });

    rootPn.add(BorderLayout.NORTH, content);
    rootPn.add(BorderLayout.SOUTH, buttons.getPanel());
    
    getRootPane().setDefaultButton(okBtn);
    pack();
  }
  
  private JPanel createTitleChangePanel() {
    CellConstraints cc = new CellConstraints();
    
    if(mFavorite.getName().length() < 1) {
      mName = new JLabel(mLocalizer.msg("defaultName","Is going to be created automatically"));
      mName.setEnabled(false);
    } else {
      mName = new JLabel(mFavorite.getName());
    }    
    
    JPanel panel = new JPanel(new FormLayout("pref,3dlu,30dlu:grow,3dlu,pref","pref"));
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
    if (isValidName(newName)) {
      mName.setText(newName);
      mName.setEnabled(true);          
    }
    else if(mName.getText().compareTo(mLocalizer.msg("defaultName","Is going to be created automatically")) == 0) {
      mName.setEnabled(false);
    }    
  }

  private boolean isValidName(String name) {
    return name != null && name.length() > 0 && 
        (name.compareTo(mLocalizer.msg("defaultName","Is going to be created automatically")) != 0);
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

      if(mChannelLabel != null) {
        mChannelLabel.setForeground((new JLabel()).getForeground());
      }
      
      return result;
    } else if(!mLimitChannelCb.isSelected()){
      return mLocalizer.msg("allChannels", "All channels");
    } else {
      return mLocalizer.msg("noChannels", "No channels");
    }
  }

  private void setLimitChannelEnabled(boolean enabled) {
    mChangeChannelsBtn.setEnabled(enabled);
    mChannelLabel.setEnabled(enabled);
    mChannelLabel.setText(getChannelString(mChannelArr));
    
    if(mChannelArr == null || mChannelArr.length < 1) {
      mChannelLabel.setForeground(Color.red);
    } else {
      mChannelLabel.setForeground((new JLabel()).getForeground());
    }
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

    mLimitChannelCb = new JCheckBox(mLocalizer.msg("channels", "Channels:"));
    mLimitTimeCb = new JCheckBox(mLocalizer.msg("time", "Time:"));
    
    mChannelLabel = new JLabel(getChannelString(mChannelArr));

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

    CellConstraints cc = new CellConstraints();

    JPanel limitPn = new JPanel(new BorderLayout());
    JPanel pn = new JPanel(new FormLayout("pref, 3dlu, pref", "pref"));
    pn.add(mTimePeriodChooser, cc.xy(1,1));
    pn.add(mLimitDaysCB, cc.xy(3,1));
    limitPn.add(pn, BorderLayout.EAST);

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

 /* private JPanel createExclusionPanel() {

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

    mExclusionsList.addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent e) {
        if(SwingUtilities.isLeftMouseButton(e) && e.getClickCount() >= 2
            && mEditExclusionBtn.isEnabled()) {
          mEditExclusionBtn.getActionListeners()[0].actionPerformed(null);
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
  }*/

  private String getForwardPluginsLabelString(ProgramReceiveTarget[] receiveTargetArr) {
    ArrayList<ProgramReceiveIf> plugins = new ArrayList<ProgramReceiveIf>();
    
    for(int i = 0; i < receiveTargetArr.length; i++) {
      if(!plugins.contains(receiveTargetArr[i].getReceifeIfForIdOfTarget())) {
        ProgramReceiveIf target = receiveTargetArr[i].getReceifeIfForIdOfTarget();
        
        if(target != null) {
          plugins.add(target);
        }
      }
    }
    
    ProgramReceiveIf[] pluginArr = plugins.toArray(new ProgramReceiveIf[plugins.size()]);
    
    if (pluginArr != null && pluginArr.length > 0) {
      StringBuffer buf = new StringBuffer();
      if (pluginArr.length > 0) {
        buf.append(pluginArr[0].toString());
      }
      if (pluginArr.length > 1) {
        buf.append(", ");
        buf.append(pluginArr[1].toString());
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
        PluginChooserDlg dlg = new PluginChooserDlg(EditFavoriteDialog.this, mPassProgramPlugins, null, ReminderPluginProxy.getInstance(), FavoritesPlugin.getInstance().getClientPluginTargetIds());
        UiUtilities.centerAndShow(dlg);
        ProgramReceiveTarget[] pluginArr = dlg.getReceiveTargets();
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

    mPassProgramsCheckBox.setSelected(mPassProgramPlugins != null && mPassProgramPlugins.length > 0 && !mPassProgramsLb.getText().equals(mLocalizer.msg("dontpass", "don't pass programs")));
    mPassProgramsCheckBox.setEnabled(FavoritesPlugin.getInstance().getClientPluginTargetIds().length == 0);
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
      mPassProgramPlugins = new ProgramReceiveTarget[] {};
    }

  }

  /**
   * Gets if the ok button was pressed
   * <p>
   * @return <code>True</code> if the ok button was pressed, <code>false</code> otherwise.
   */
  public boolean getOkWasPressed() {
    return mOkWasPressed;
  }

  private void saveAndClose() {
    if (!mFavoriteConfigurator.check()) {
      return;
    }
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
    mFavorite.setExclusions(mExclusionPanel.getExclusions());

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
      mFavorite.updatePrograms(false);
    } catch (TvBrowserException exc) {
      ErrorHandler.handle(mLocalizer.msg("error.updateFavoriteFailed", "Could not update favorite"), exc);
    }
    
    for (ProgramReceiveTarget target : mPassProgramPlugins) {
      target.getReceifeIfForIdOfTarget().receivePrograms(mFavorite.getPrograms(),target);
    }

    if (mUseReminderCb.isSelected() && !wasReminderEnabled) {
      ReminderPlugin.getInstance().addPrograms(mFavorite.getPrograms());
      ReminderPlugin.getInstance().updateRootNode(true);
    }
    
    if(mName.getText().length() > 0 && mName.getText().compareTo(mLocalizer.msg("defaultName","Is going to be created automatically")) != 0) {
      mFavorite.setName(mName.getText());
    }
    
    mOkWasPressed = true;
    setVisible(false);
  }
  
  private boolean arrayContains(ProgramReceiveTarget[] targetArr, ProgramReceiveTarget target) {
    for(ProgramReceiveTarget arrayEntry : targetArr) {
      if(arrayEntry.getReceiveIfId().equals(target.getReceiveIfId()) && arrayEntry.getTargetId().equals(target.getTargetId())) {
        return true;
      }
    }
    
    return false;
  }
  
  public void close() {
    setVisible(false);
  }

}