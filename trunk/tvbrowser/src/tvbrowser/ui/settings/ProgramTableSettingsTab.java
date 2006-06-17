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

package tvbrowser.ui.settings;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.util.Calendar;
import java.util.Date;

import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerDateModel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import tvbrowser.core.Settings;
import tvbrowser.ui.settings.tablebackgroundstyles.BlankBackgroundStyle;
import tvbrowser.ui.settings.tablebackgroundstyles.DayTimeBackgroundStyle;
import tvbrowser.ui.settings.tablebackgroundstyles.SingleImageBackgroundStyle;
import tvbrowser.ui.settings.tablebackgroundstyles.TableBackgroundStyle;
import tvbrowser.ui.settings.tablebackgroundstyles.TimeBlockBackgroundStyle;
import tvbrowser.ui.settings.util.ColorButton;
import tvbrowser.ui.settings.util.ColorLabel;
import util.ui.UiUtilities;

import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.factories.DefaultComponentFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;
import com.jgoodies.forms.layout.Sizes;

import devplugin.SettingsItem;
import devplugin.SettingsTab;

/**
 * 
 * @author Til Schneider, www.murfman.de
 */
public class ProgramTableSettingsTab implements SettingsTab, ActionListener {

  private static final util.ui.Localizer mLocalizer = util.ui.Localizer.getLocalizerFor(ProgramTableSettingsTab.class);

  private JPanel mSettingsPn;

  private JComboBox mProgramArrangementCB;

  private JComboBox mBackgroundStyleCB;

  private JButton mConfigBackgroundStyleBt;

  private JSlider mColWidthSl;

  private JButton mDefaultBtn;

  private JSpinner mStartOfDayTimeSp, mEndOfDayTimeSp;

  private JCheckBox mMouseOverCb;

  private ColorLabel mMouseOverColorLb;

  private JRadioButton mShowNameAndIcon;

  private JRadioButton mShowIcon;

  private JRadioButton mShowName;

  public void actionPerformed(ActionEvent event) {
    Object source = event.getSource();
    if (source == mDefaultBtn) {
      mColWidthSl.setValue(200);
    }
  }

  /**
   * Creates the settings panel for this tab.
   */
  public JPanel createSettingsPanel() {
    FormLayout layout = new FormLayout("5dlu, pref, 3dlu, pref, 3dlu, pref, fill:pref:grow 3dlu", "");
    mSettingsPn = new JPanel(layout);
    mSettingsPn.setBorder(Borders.DIALOG_BORDER);

    CellConstraints cc = new CellConstraints();

    // Layout-Rows ****************************************
    layout.appendRow(new RowSpec("pref"));
    layout.appendRow(new RowSpec("5dlu"));
    layout.appendRow(new RowSpec("pref"));
    layout.appendRow(new RowSpec("5dlu"));
    
    mSettingsPn.add(DefaultComponentFactory.getInstance().createSeparator(mLocalizer.msg("layout", "Layout")), cc.xyw(1,1,8));
    mSettingsPn.add(new JLabel(mLocalizer.msg("programArrangement", "Program arrangement")), cc.xy(2,3));
    
    // program table layout
    String[] arrangementArr = { mLocalizer.msg("timeSynchronous", "Time synchronous"),
        mLocalizer.msg("realSynchronous", "Real time synchronous"), 
        mLocalizer.msg("compact", "Compact"),mLocalizer.msg("realCompact", "Real compact")};
    mProgramArrangementCB = new JComboBox(arrangementArr);
    if (Settings.propTableLayout.getString().equals("compact"))
      mProgramArrangementCB.setSelectedIndex(2);
    else if (Settings.propTableLayout.getString().equals("realCompact"))
      mProgramArrangementCB.setSelectedIndex(3);
    else if (Settings.propTableLayout.getString().equals("timeSynchronous"))
      mProgramArrangementCB.setSelectedIndex(0);
    else
      mProgramArrangementCB.setSelectedIndex(1);

    mSettingsPn.add(mProgramArrangementCB, cc.xy(4, 3));

    // Column Rows ***************************************
    layout.appendRow(new RowSpec("pref"));
    layout.appendRow(new RowSpec("5dlu"));
    layout.appendRow(new RowSpec("pref"));
    layout.appendRow(new RowSpec("5dlu"));
    
    mSettingsPn.add(DefaultComponentFactory.getInstance().createSeparator(mLocalizer.msg("columnwidth", "column width")), cc.xyw(1,5,8));
    
    // column width
    JPanel sliderPn = new JPanel(new BorderLayout());

    mColWidthSl = new JSlider(SwingConstants.HORIZONTAL, 50, 300, Settings.propColumnWidth.getInt());

    mColWidthSl.setPreferredSize(new Dimension(200, 25));

    final JLabel colWidthLb = new JLabel(Integer.toString(mColWidthSl.getValue()), JLabel.RIGHT);
    Dimension dim = colWidthLb.getPreferredSize();
    colWidthLb.setPreferredSize(new Dimension(Sizes.dialogUnitXAsPixel(20, mSettingsPn), dim.height));
    
    mColWidthSl.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        colWidthLb.setText(Integer.toString(mColWidthSl.getValue()));
      }
    });

    sliderPn.add(mColWidthSl, BorderLayout.CENTER);
    sliderPn.add(colWidthLb, BorderLayout.EAST);

    mSettingsPn.add(sliderPn, cc.xyw(2,7,3));
    
    mDefaultBtn = new JButton(mLocalizer.msg("reset", "reset"));
    mDefaultBtn.addActionListener(this);

    mSettingsPn.add(mDefaultBtn, cc.xy(6,7));
    
    // Column Rows ***************************************
    layout.appendRow(new RowSpec("pref"));
    layout.appendRow(new RowSpec("5dlu"));
    layout.appendRow(new RowSpec("pref"));
    layout.appendRow(new RowSpec("3dlu"));
    layout.appendRow(new RowSpec("pref"));
    layout.appendRow(new RowSpec("5dlu"));
    
    mSettingsPn.add(DefaultComponentFactory.getInstance().createSeparator(mLocalizer.msg("range", "Range")), cc.xyw(1,9,8));
    
    mSettingsPn.add(new JLabel(mLocalizer.msg("startOfDay", "Start of day")), cc.xy(2, 11));

    mStartOfDayTimeSp = new JSpinner(new SpinnerDateModel());
    mStartOfDayTimeSp.setEditor(new JSpinner.DateEditor(mStartOfDayTimeSp, Settings.getTimePattern()));
    mSettingsPn.add(mStartOfDayTimeSp, cc.xy(4, 11));
    mSettingsPn.add(new JLabel("(" + mLocalizer.msg("today", "today") + ")"), cc.xy(6, 11));
    
    mSettingsPn.add(new JLabel(mLocalizer.msg("endOfDay", "End of day")), cc.xy(2, 13));
    
    mEndOfDayTimeSp = new JSpinner(new SpinnerDateModel());
    mEndOfDayTimeSp.setEditor(new JSpinner.DateEditor(mEndOfDayTimeSp, Settings.getTimePattern()));
    mSettingsPn.add(mEndOfDayTimeSp, cc.xy(4, 13));
    mSettingsPn.add(new JLabel("(" + mLocalizer.msg("nextDay", "next day") + ")"), cc.xy(6, 13));
    
    int minutes;
    Calendar cal = Calendar.getInstance();
    minutes = Settings.propProgramTableStartOfDay.getInt();
    cal.set(Calendar.HOUR_OF_DAY, minutes / 60);
    cal.set(Calendar.MINUTE, minutes % 60);
    mStartOfDayTimeSp.setValue(cal.getTime());

    minutes = Settings.propProgramTableEndOfDay.getInt();
    cal.set(Calendar.HOUR_OF_DAY, minutes / 60);
    cal.set(Calendar.MINUTE, minutes % 60);
    mEndOfDayTimeSp.setValue(cal.getTime());
    
    // Table Background ***************************************
    layout.appendRow(new RowSpec("pref"));
    layout.appendRow(new RowSpec("5dlu"));
    layout.appendRow(new RowSpec("pref"));
    layout.appendRow(new RowSpec("5dlu"));

    mSettingsPn.add(DefaultComponentFactory.getInstance().createSeparator(mLocalizer.msg("tableBackground", "Table background")), cc.xyw(1,15,8));

    mSettingsPn.add(new JLabel(mLocalizer.msg("tableBackgroundStyle", "Table background style")), cc.xy(2,17));
    
    TableBackgroundStyle[] styles = getTableBackgroundStyles();
    mBackgroundStyleCB = new JComboBox(styles);

    String style = Settings.propTableBackgroundStyle.getString();
    for (int i = 0; i < styles.length; i++) {
      if (styles[i].getSettingsString().equals(style)) {
        mBackgroundStyleCB.setSelectedIndex(i);
        break;
      }
    }

    mBackgroundStyleCB.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        updateBackgroundStyleConfigureButton();
      }
    });

    mSettingsPn.add(mBackgroundStyleCB, cc.xy(4, 17));
    
    mConfigBackgroundStyleBt = new JButton(mLocalizer.msg("configure", "Configure..."));

    mConfigBackgroundStyleBt.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        ConfigureBackgroundStyleDialog dlg = new ConfigureBackgroundStyleDialog(mBackgroundStyleCB,
            (TableBackgroundStyle) mBackgroundStyleCB.getSelectedItem());
        dlg.show();
      }
    });

    mSettingsPn.add(mConfigBackgroundStyleBt, cc.xy(6, 17));

    // Miscellaneous *********************************************
    layout.appendRow(new RowSpec("pref"));
    layout.appendRow(new RowSpec("5dlu"));
    layout.appendRow(new RowSpec("pref"));
    layout.appendRow(new RowSpec("3dlu"));
    layout.appendRow(new RowSpec("pref"));
    layout.appendRow(new RowSpec("3dlu"));
    layout.appendRow(new RowSpec("pref"));
    layout.appendRow(new RowSpec("3dlu"));
    layout.appendRow(new RowSpec("pref"));
    layout.appendRow(new RowSpec("5dlu"));
    
    mSettingsPn.add(DefaultComponentFactory.getInstance().createSeparator(mLocalizer.msg("channelIcons.Title", "Channel Icons")), cc.xyw(1,19,8));
    
    mShowNameAndIcon = new JRadioButton(mLocalizer.msg("channelIcons.showNameAndIcon", "Show Channel Name and Icon"));
    mShowIcon = new JRadioButton(mLocalizer.msg("channelIcons.showIcon", "Show only Channel Icon"));
    mShowName = new JRadioButton(mLocalizer.msg("channelIcons.showName", "Show only Channel Name"));
    
    ButtonGroup group = new ButtonGroup();
    group.add(mShowIcon);
    group.add(mShowNameAndIcon);
    group.add(mShowName);
    
    mSettingsPn.add(mShowNameAndIcon, cc.xyw(2,21,7));
    mSettingsPn.add(mShowIcon, cc.xyw(2,23,7));
    mSettingsPn.add(mShowName, cc.xyw(2,25,7));
    
    if (Settings.propShowChannelIconsInProgramTable.getBoolean() &&
        Settings.propShowChannelNamesInProgramTable.getBoolean()) {
      mShowNameAndIcon.setSelected(true);
    } else if (Settings.propShowChannelIconsInProgramTable.getBoolean()) {
      mShowIcon.setSelected(true);
    } else {
      Settings.propShowChannelNamesInProgramTable.setBoolean(true);
      mShowName.setSelected(true);
    }
    updateIconSelection();
    Settings.propEnableChannelIcons.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
          updateIconSelection();
      }
    });
    
    JEditorPane pane = UiUtilities.createHtmlHelpTextArea(mLocalizer.msg("channelIcons.help","To disable/enable Channel Icons globally, please look <a href=\"#link\">here</a>."), new HyperlinkListener() {
      public void hyperlinkUpdate(HyperlinkEvent e) {
        if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
          SettingsDialog.getInstance().showSettingsTab(SettingsItem.LOOKANDFEEL);
        }
      }
    });
    
    mSettingsPn.add(pane, cc.xyw(2,27,7));
    
    // Miscellaneous *********************************************
    layout.appendRow(new RowSpec("pref"));
    layout.appendRow(new RowSpec("5dlu"));
    layout.appendRow(new RowSpec("pref"));
    layout.appendRow(new RowSpec("5dlu"));

    mSettingsPn.add(DefaultComponentFactory.getInstance().createSeparator(mLocalizer.msg("Miscellaneous", "Miscellaneous")), cc.xyw(1,29,8));

    mMouseOverCb = new JCheckBox(mLocalizer.msg("MouseOver", "Mouse-Over-Effect"));
    mMouseOverCb.setSelected(Settings.propMouseOver.getBoolean());

    mSettingsPn.add(mMouseOverCb, cc.xy(2,31));
    
    mMouseOverColorLb = new ColorLabel(Settings.propMouseOverColor.getColor());
    mMouseOverColorLb.setStandardColor(Settings.propMouseOverColor.getDefaultColor());
    mMouseOverColorLb.setEnabled(Settings.propMouseOver.getBoolean());
    final ColorButton mouseOverColorChangeBtn = new ColorButton(mMouseOverColorLb);
    final JLabel colorLb = new JLabel(mLocalizer.msg("color", "Color:"));
    mouseOverColorChangeBtn.setEnabled(Settings.propMouseOver.getBoolean());
    colorLb.setEnabled(Settings.propMouseOver.getBoolean());
    mMouseOverCb.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        boolean enabled = mMouseOverCb.isSelected();
        mMouseOverColorLb.setEnabled(enabled);
        mouseOverColorChangeBtn.setEnabled(enabled);
        colorLb.setEnabled(enabled);
      }
    });

    JPanel pn1 = new JPanel();
    pn1.add(colorLb);
    pn1.add(mMouseOverColorLb);
    pn1.add(mouseOverColorChangeBtn);

    mSettingsPn.add(pn1, cc.xy(4, 31));
    
    updateBackgroundStyleConfigureButton();

    return mSettingsPn;
  }

  private void updateIconSelection() {
    if (!Settings.propEnableChannelIcons.getBoolean()) {
      mShowNameAndIcon.setEnabled(false);
      mShowIcon.setEnabled(false);
      mShowName.setSelected(true);
    } else {
      mShowNameAndIcon.setEnabled(true);
      mShowIcon.setEnabled(true);
    }
  }
  
  private void updateBackgroundStyleConfigureButton() {
    TableBackgroundStyle style = (TableBackgroundStyle) mBackgroundStyleCB.getSelectedItem();
    mConfigBackgroundStyleBt.setEnabled(style.hasContent());
  }

  public static JButton createBrowseButton(final Component parent, final JTextField tf) {
    JButton bt = new JButton(mLocalizer.msg("change", "Change"));
    bt.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        File file = new File(tf.getText());
        JFileChooser fileChooser = new JFileChooser(file.getParent());
        String[] extArr = { ".jpg", ".jpeg", ".gif", ".png" };
        fileChooser.setFileFilter(new util.ui.ExtensionFileFilter(extArr, ".jpg, .gif, png"));
        fileChooser.showOpenDialog(parent);
        File selection = fileChooser.getSelectedFile();
        if (selection != null) {
          tf.setText(selection.getAbsolutePath());
        }
      }
    });

    Dimension size = bt.getPreferredSize();

    if (tf.getPreferredSize().height > size.height) {
      size.height = tf.getPreferredSize().height;
      bt.setPreferredSize(size);
    }

    return bt;
  }

  /**
   * Called by the host-application, if the user wants to save the settings.
   */
  public void saveSettings() {
    if (mProgramArrangementCB.getSelectedIndex() == 2)
      Settings.propTableLayout.setString("compact");
    else if (mProgramArrangementCB.getSelectedIndex() == 3)
      Settings.propTableLayout.setString("realCompact");    
    else if (mProgramArrangementCB.getSelectedIndex() == 0)
      Settings.propTableLayout.setString("timeSynchronous");
    else
      Settings.propTableLayout.setString("realSynchronous");

    String backgroundStyle = ((TableBackgroundStyle) mBackgroundStyleCB.getSelectedItem()).getSettingsString();

    Settings.propTableBackgroundStyle.setString(backgroundStyle);

    Settings.propColumnWidth.setInt(mColWidthSl.getValue());

    Calendar cal = Calendar.getInstance();
    Date startTime = (Date) mStartOfDayTimeSp.getValue();
    cal.setTime(startTime);
    int minutes = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE);
    Settings.propProgramTableStartOfDay.setInt(minutes);

    Date endTime = (Date) mEndOfDayTimeSp.getValue();
    cal.setTime(endTime);
    minutes = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE);
    Settings.propProgramTableEndOfDay.setInt(minutes);

    Settings.propMouseOver.setBoolean(mMouseOverCb.isSelected());

    Settings.propMouseOverColor.setColor(mMouseOverColorLb.getColor());
    
    if (mShowNameAndIcon.isSelected()) {
      Settings.propShowChannelIconsInProgramTable.setBoolean(true);
      Settings.propShowChannelNamesInProgramTable.setBoolean(true);
    } else if (mShowName.isSelected()) {
      Settings.propShowChannelIconsInProgramTable.setBoolean(false);
      Settings.propShowChannelNamesInProgramTable.setBoolean(true);
    } else if (mShowIcon.isSelected()) {
      Settings.propShowChannelIconsInProgramTable.setBoolean(true);
      Settings.propShowChannelNamesInProgramTable.setBoolean(false);
    }
  }

  /**
   * Returns the name of the tab-sheet.
   */
  public Icon getIcon() {
    return null;
  }

  /**
   * Returns the title of the tab-sheet.
   */
  public String getTitle() {
    return mLocalizer.msg("programTable", "Program table");
  }

  private TableBackgroundStyle[] getTableBackgroundStyles() {

    return new TableBackgroundStyle[] { new BlankBackgroundStyle(), new SingleImageBackgroundStyle(),
        new TimeBlockBackgroundStyle(), new DayTimeBackgroundStyle() };

  }

  class ConfigureBackgroundStyleDialog {

    private JDialog mDialog;

    private TableBackgroundStyle mStyle;

    public ConfigureBackgroundStyleDialog(Component parent, TableBackgroundStyle style) {
      mStyle = style;

      mDialog = UiUtilities.createDialog(parent, true);
      mDialog.setTitle(mLocalizer.msg("configureBackgroundStyleDialogTitle", "Configure background style '{0}'", style
          .getName()));

      JPanel dialogContent = (JPanel) mDialog.getContentPane();
      dialogContent.setBorder(new EmptyBorder(10, 10, 11, 11));
      dialogContent.setLayout(new BorderLayout(0, 15));

      JPanel content = new JPanel(new BorderLayout());

      content.add(style.createSettingsContent(), BorderLayout.NORTH);
      dialogContent.add(content, BorderLayout.CENTER);

      JPanel buttonPn = new JPanel(new BorderLayout());
      JPanel pn = new JPanel();
      pn.setLayout(new FlowLayout(FlowLayout.RIGHT, 5, 0));

      JButton okBtn = new JButton(mLocalizer.msg("ok", "OK"));
      JButton cancelBtn = new JButton(mLocalizer.msg("cancel", "Cancel"));
      pn.add(okBtn);
      pn.add(cancelBtn);

      okBtn.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          mStyle.storeSettings();
          mDialog.setVisible(false);
        }
      });

      cancelBtn.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          mDialog.setVisible(false);
        }
      });

      buttonPn.add(pn, BorderLayout.EAST);
      dialogContent.add(buttonPn, BorderLayout.SOUTH);
      mDialog.pack();

    }

    public void show() {
      UiUtilities.centerAndShow(mDialog);
    }

  }

}