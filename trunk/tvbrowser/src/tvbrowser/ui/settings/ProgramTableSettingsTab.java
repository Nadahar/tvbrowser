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
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.util.Calendar;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerDateModel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import tvbrowser.core.Settings;
import tvbrowser.ui.settings.tablebackgroundstyles.BlankBackgroundStyle;
import tvbrowser.ui.settings.tablebackgroundstyles.DayTimeBackgroundStyle;
import tvbrowser.ui.settings.tablebackgroundstyles.SingleImageBackgroundStyle;
import tvbrowser.ui.settings.tablebackgroundstyles.TableBackgroundStyle;
import tvbrowser.ui.settings.tablebackgroundstyles.TimeBlockBackgroundStyle;
import tvbrowser.ui.settings.util.ColorButton;
import tvbrowser.ui.settings.util.ColorLabel;
import util.ui.TabLayout;
import util.ui.UiUtilities;
import devplugin.SettingsTab;


/**
 *
 * @author Til Schneider, www.murfman.de
 */
public class ProgramTableSettingsTab implements SettingsTab, ActionListener {

  private static final util.ui.Localizer mLocalizer
      = util.ui.Localizer.getLocalizerFor(ProgramTableSettingsTab.class);

  private JPanel mSettingsPn;

  private JComboBox mProgramArrangementCB;
  private JComboBox mBackgroundStyleCB;
  private JButton mConfigBackgroundStyleBt;
  private JSlider mColWidthSl;
  private JButton mDefaultBtn;
  private JSpinner mStartOfDayTimeSp, mEndOfDayTimeSp;
  private JCheckBox mMouseOverCb;
  private JCheckBox mTitelAlwaysVisible;

  //private ColorLabel mProgramItemOnAirColorLb, mProgramItemProgressColorLb, mProgramItemMarkedColorLb;
  private ColorLabel mMouseOverColorLb;

  /**
   * Creates a new instance of ProgramTableSettingsTab.
   */
  public ProgramTableSettingsTab() {

  }



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
    JPanel p1;

    mSettingsPn = new JPanel(new BorderLayout());
    mSettingsPn.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

    JPanel main = new JPanel(new TabLayout(1));
    mSettingsPn.add(main, BorderLayout.NORTH);

    // program table layout
    p1 = new JPanel(new FlowLayout(FlowLayout.LEADING, 5, 0));
    p1.setBorder(BorderFactory.createTitledBorder(mLocalizer.msg("layout","Layout")));
    main.add(p1);

    p1.add(new JLabel(mLocalizer.msg("programArrangement", "Program arrangement")));
    String[] arrangementArr = {
      mLocalizer.msg("compact", "Compact"),
      mLocalizer.msg("timeSynchronous", "Time synchronous")
    };
    mProgramArrangementCB = new JComboBox(arrangementArr);
    if (Settings.propTableLayout.getString().equals("compact")) {
      mProgramArrangementCB.setSelectedIndex(0);
    } else {
      mProgramArrangementCB.setSelectedIndex(1);
    }
    p1.add(mProgramArrangementCB);


    // column width
    JPanel colWidthPn=new JPanel(new BorderLayout());
    JPanel sliderPn = new JPanel();


    colWidthPn.setBorder(BorderFactory.createTitledBorder(mLocalizer.msg("columnwidth","column width")));
    mColWidthSl=new JSlider(SwingConstants.HORIZONTAL, 50, 300, Settings.propColumnWidth.getInt());

    colWidthPn.add(sliderPn,BorderLayout.WEST);
    mColWidthSl.setPreferredSize(new Dimension(200,25));

    final JLabel colWidthLb = new JLabel(""+mColWidthSl.getValue());

    mColWidthSl.addChangeListener(new ChangeListener(){
      public void stateChanged(ChangeEvent e) {
        colWidthLb.setText(""+mColWidthSl.getValue());
      }
    });

    sliderPn.add(mColWidthSl);
    sliderPn.add(colWidthLb);

    mDefaultBtn=new JButton(mLocalizer.msg("reset","reset"));
    mDefaultBtn.addActionListener(this);

    JPanel pn4 = new JPanel(new BorderLayout());
    pn4.add(mDefaultBtn, BorderLayout.NORTH);

    colWidthPn.add(pn4,BorderLayout.EAST);

    main.add(colWidthPn);

    // day range

    JPanel pn=new JPanel(new GridLayout(2,2,0,3));
    pn.setBorder(BorderFactory.createTitledBorder(mLocalizer.msg("range","Range")));

    pn.add(new JLabel(mLocalizer.msg("startOfDay","Start of day")));
    JPanel panel1=new JPanel(new BorderLayout(7,0));

    String timePattern = mLocalizer.msg("timePattern", "hh:mm a");

    mStartOfDayTimeSp = new JSpinner(new SpinnerDateModel());
    mStartOfDayTimeSp.setEditor(new JSpinner.DateEditor(mStartOfDayTimeSp, timePattern));

    panel1.add(mStartOfDayTimeSp,BorderLayout.WEST);
    panel1.add(new JLabel("("+mLocalizer.msg("today","today")+")"));
    pn.add(panel1);

    pn.add(new JLabel(mLocalizer.msg("endOfDay","End of day")));
    panel1=new JPanel(new BorderLayout(7,0));

    mEndOfDayTimeSp = new JSpinner(new SpinnerDateModel());
    mEndOfDayTimeSp.setEditor(new JSpinner.DateEditor(mEndOfDayTimeSp, timePattern));

    panel1.add(mEndOfDayTimeSp,BorderLayout.WEST);
    panel1.add(new JLabel("("+mLocalizer.msg("nextDay","next day")+")"));
    pn.add(panel1);

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

    main.add(pn);

    // table background style

    JPanel pn1 = new JPanel(new BorderLayout());
    pn1.setBorder(BorderFactory.createTitledBorder(mLocalizer.msg("tableBackground", "Table background")));
    JPanel pn2 = new JPanel();
    pn2.add(new JLabel(mLocalizer.msg("tableBackgroundStyle", "Table background style")));

    TableBackgroundStyle[] styles = getTableBackgroundStyles();
    mBackgroundStyleCB = new JComboBox(styles);

    String style = Settings.propTableBackgroundStyle.getString();
    for (int i=0; i<styles.length; i++) {
      if (styles[i].getSettingsString().equals(style)) {
        mBackgroundStyleCB.setSelectedIndex(i);
        break;
      }
    }

    mBackgroundStyleCB.addItemListener(new ItemListener(){
      public void itemStateChanged(ItemEvent e) {
        updateBackgroundStyleConfigureButton();
      }
    });

    pn2.add(mBackgroundStyleCB);
    mConfigBackgroundStyleBt = new JButton(mLocalizer.msg("configure","Configure..."));

    mConfigBackgroundStyleBt.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        ConfigureBackgroundStyleDialog dlg = new ConfigureBackgroundStyleDialog(mBackgroundStyleCB, (TableBackgroundStyle)mBackgroundStyleCB.getSelectedItem());
        dlg.show();
      }
    });

    JPanel pn3 = new JPanel(new BorderLayout());
    pn3.add(mConfigBackgroundStyleBt, BorderLayout.NORTH);
    pn1.add(pn3, BorderLayout.EAST);

    pn1.add(pn2, BorderLayout.WEST);
    main.add(pn1);

    // miscellaneous

    JPanel misc = new JPanel(new TabLayout(1));

    misc.setBorder(BorderFactory.createTitledBorder(mLocalizer.msg("Miscellaneous", "Miscellaneous")));

    JPanel mouseOverPn = new JPanel(new BorderLayout());
    mMouseOverCb = new JCheckBox(mLocalizer.msg("MouseOver","Mouse-Over-Effect"));
    mMouseOverCb.setSelected(Settings.propMouseOver.getBoolean());

    mMouseOverColorLb = new ColorLabel(Settings.propMouseOverColor.getColor());
    mMouseOverColorLb.setStandardColor(Settings.propMouseOverColor.getDefaultColor());
    mMouseOverColorLb.setEnabled(Settings.propMouseOver.getBoolean());
    final ColorButton mouseOverColorChangeBtn = new ColorButton(mMouseOverColorLb);    
    final JLabel colorLb = new JLabel(mLocalizer.msg("color","Color:"));
    mouseOverColorChangeBtn.setEnabled(Settings.propMouseOver.getBoolean());
    colorLb.setEnabled(Settings.propMouseOver.getBoolean());
    mMouseOverCb.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        boolean enabled = mMouseOverCb.isSelected();
        mMouseOverColorLb.setEnabled(enabled);
        mouseOverColorChangeBtn.setEnabled(enabled);
        colorLb.setEnabled(enabled);
      }
    });


    mouseOverPn.add(mMouseOverCb, BorderLayout.WEST);
    pn1 = new JPanel();
    pn1.add(colorLb);
    pn1.add(mMouseOverColorLb);
    pn1.add(mouseOverColorChangeBtn);
    mouseOverPn.add(pn1, BorderLayout.EAST);
    misc.add(mouseOverPn);

    mTitelAlwaysVisible = new JCheckBox(mLocalizer.msg("TitleAlwaysVisible","Progam-Title always Visible"));
    mTitelAlwaysVisible.setSelected(Settings.propTitelAlwaysVisible.getBoolean());

   /*
    Hide this because the ProgramPanel is not ready for it
    misc.add(mTitelAlwaysVisible);
    */
    
    main.add(misc);


    // colors
    /*
    JPanel colors = new JPanel();

    Color programItemProgressColor = Settings.propProgramTableColorOnAirDark.getColor();
    Color programItemOnAirColor = Settings.propProgramTableColorOnAirLight.getColor();
    Color programItemMarkedColor = Settings.propProgramTableColorMarked.getColor();


    colors.setBorder(BorderFactory.createTitledBorder(mLocalizer.msg("Colors", "Colors")));
    FormLayout formLayout = new FormLayout("default, 5dlu, default, 5dlu, default",
            "default, 3dlu, default, 3dlu, default");
    CellConstraints c = new CellConstraints();
    colors.setLayout(formLayout);

    colors.add(new JLabel(mLocalizer.msg("color.programOnAir","Hintergrundfarbe fuer laufende Sendung")), c.xy(1,1));
    colors.add(mProgramItemOnAirColorLb = new ColorLabel(programItemOnAirColor), c.xy(3,1));
    colors.add(new ColorButton(mProgramItemOnAirColorLb), c.xy(5,1));

    colors.add(new JLabel(mLocalizer.msg("color.programProgress", "Fortschrittanzeige fuer laufende Sendung")), c.xy(1,3));
    colors.add(mProgramItemProgressColorLb = new ColorLabel(programItemProgressColor), c.xy(3,3));
    colors.add(new ColorButton(mProgramItemProgressColorLb), c.xy(5,3));

    colors.add(new JLabel(mLocalizer.msg("color.programMarked","Markierung durch Plugins")), c.xy(1,5));
    colors.add(mProgramItemMarkedColorLb = new ColorLabel(programItemMarkedColor), c.xy(3,5));
    colors.add(new ColorButton(mProgramItemProgressColorLb), c.xy(5,5));

    main.add(colors);
     */

    updateBackgroundStyleConfigureButton();

    return mSettingsPn;
  }


  private void updateBackgroundStyleConfigureButton() {
    TableBackgroundStyle style = (TableBackgroundStyle)mBackgroundStyleCB.getSelectedItem();
    mConfigBackgroundStyleBt.setEnabled(style.hasContent());
  }

  public static JButton createBrowseButton(final Component parent, final JTextField tf) {
    JButton bt = new JButton(mLocalizer.msg("change", "Change"));
    bt.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        File file = new File(tf.getText());
        JFileChooser fileChooser = new JFileChooser(file.getParent());
        String[] extArr = { ".jpg", ".jpeg", ".gif", ".png"};
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
    if (mProgramArrangementCB.getSelectedIndex() == 0) {
      Settings.propTableLayout.setString("compact");
    } else {
      Settings.propTableLayout.setString("timeSynchronous");
    }

    String backgroundStyle = ((TableBackgroundStyle)mBackgroundStyleCB.getSelectedItem()).getSettingsString();

    Settings.propTableBackgroundStyle.setString(backgroundStyle);

    Settings.propColumnWidth.setInt(mColWidthSl.getValue());

    Calendar cal=Calendar.getInstance();
    Date startTime = (Date) mStartOfDayTimeSp.getValue();
    cal.setTime(startTime);
    int minutes = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE);
    Settings.propProgramTableStartOfDay.setInt(minutes);

    Date endTime = (Date) mEndOfDayTimeSp.getValue();
    cal.setTime(endTime);
    minutes = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE);
    Settings.propProgramTableEndOfDay.setInt(minutes);

    Settings.propMouseOver.setBoolean(mMouseOverCb.isSelected());
    Settings.propTitelAlwaysVisible.setBoolean(mTitelAlwaysVisible.isSelected());

    Settings.propMouseOverColor.setColor(mMouseOverColorLb.getColor());
//    Settings.propProgramTableColorMarked.setColor(mProgramItemMarkedColorLb.getColor());
//    Settings.propProgramTableColorOnAirDark.setColor(mProgramItemProgressColorLb.getColor());
//    Settings.propProgramTableColorOnAirLight.setColor(mProgramItemOnAirColorLb.getColor());
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

    return new TableBackgroundStyle[]{
      new BlankBackgroundStyle(),
      new SingleImageBackgroundStyle(),
      new TimeBlockBackgroundStyle(),
      new DayTimeBackgroundStyle()
    };

  }








class ConfigureBackgroundStyleDialog {

  private JDialog mDialog;
  private TableBackgroundStyle mStyle;

  public ConfigureBackgroundStyleDialog(Component parent, TableBackgroundStyle style) {
    mStyle = style;

    mDialog = UiUtilities.createDialog(parent, true);
    mDialog.setTitle(mLocalizer.msg("configureBackgroundStyleDialogTitle","Configure background style '{0}'", style.getName()));

    JPanel dialogContent = (JPanel)mDialog.getContentPane();
    dialogContent.setBorder(new EmptyBorder(10,10,11,11));
    dialogContent.setLayout(new BorderLayout(0, 15));

    JPanel content = new JPanel(new BorderLayout());


    content.add(style.createSettingsContent(), BorderLayout.NORTH);
    dialogContent.add(content, BorderLayout.CENTER);

    JPanel buttonPn = new JPanel(new BorderLayout());
    JPanel pn = new JPanel();
    pn.setLayout(new FlowLayout(FlowLayout.RIGHT, 5, 0));

    JButton okBtn = new JButton(mLocalizer.msg("ok","OK"));
    JButton cancelBtn = new JButton(mLocalizer.msg("cancel","Cancel"));
    pn.add(okBtn);
    pn.add(cancelBtn);

    okBtn.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        mStyle.storeSettings();
        mDialog.setVisible(false);
      }
    });

    cancelBtn.addActionListener(new ActionListener(){
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