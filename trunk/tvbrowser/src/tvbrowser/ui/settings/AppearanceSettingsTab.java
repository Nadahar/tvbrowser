/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (darras@users.sourceforge.net)
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

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;

import tvbrowser.core.*;
import tvbrowser.ui.SkinPanel;

import util.exc.*;

/**
 * TV-Browser
 *
 * @author Martin Oberhauser
 */
public class AppearanceSettingsTab extends devplugin.SettingsTab implements ActionListener {

  private static final util.ui.Localizer mLocalizer
    = util.ui.Localizer.getLocalizerFor(AppearanceSettingsTab.class);
  
  private JComboBox lfComboBox;
  private JTextField skinTextField;
  private JCheckBox skinCheckBox;

  private JRadioButton blankRadio, wallpaperRadio, columnsRadio;

  private JLabel skinTableBGLabel;
  private JButton skinTableBGBtn;
  private JTextField skinTableBGTextField;

  private JCheckBox mTimeCheck, updateCheck, settingsCheck;
  private JRadioButton textOnlyRadio, picOnlyRadio, textAndPicRadio;

  class LookAndFeelObj {
    private UIManager.LookAndFeelInfo info;
    public LookAndFeelObj(UIManager.LookAndFeelInfo info) {
      this.info=info;
    }
    public String toString() {
      return info.getName();
    }
    public String getLFClassName() {
      return info.getClassName();
    }
  }


  public AppearanceSettingsTab()  {
    String msg;

    setLayout(new BorderLayout());
    JPanel content=new JPanel();
    content.setLayout(new BoxLayout(content,BoxLayout.Y_AXIS));
    content.setBorder(BorderFactory.createEmptyBorder(20,30,20,30));

    JPanel lfPanel=new JPanel(new BorderLayout(10,0));
    msg = mLocalizer.msg("lookAndFeel", "Aussehen");
    JLabel lfLabel = new JLabel(msg);

    LookAndFeelObj[] obj=getLookAndFeelObjs();
    lfComboBox=new JComboBox(obj);
    String lf=Settings.getLookAndFeel();
    for (int i=0;i<obj.length;i++) {
      if (obj[i].getLFClassName().equals(lf)) {
        lfComboBox.setSelectedItem(obj[i]);
      }
    }

    lfPanel.add(lfLabel,BorderLayout.WEST);
    lfPanel.add(lfComboBox,BorderLayout.CENTER);
    lfLabel.setPreferredSize(new Dimension(200,(int)lfLabel.getPreferredSize().getHeight()));

    JPanel skinPanel=new JPanel(new BorderLayout(10,0));
    msg = mLocalizer.msg("skin", "Background");
    skinCheckBox = new JCheckBox(msg);
    skinTextField = new JTextField(Settings.getApplicationSkin());
    msg = mLocalizer.msg("change", "Change");
    final JButton skinChooseBtn = new JButton(msg);
    skinPanel.add(skinCheckBox,BorderLayout.WEST);
    skinPanel.add(skinTextField,BorderLayout.CENTER);
    skinPanel.add(skinChooseBtn,BorderLayout.EAST);
    skinCheckBox.setPreferredSize(new Dimension(200,(int)skinCheckBox.getPreferredSize().getHeight()));
    skinCheckBox.setSelected(Settings.useApplicationSkin());
    skinChooseBtn.setEnabled(Settings.useApplicationSkin());
    skinTextField.setEnabled(Settings.useApplicationSkin());

    skinCheckBox.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        skinChooseBtn.setEnabled(skinCheckBox.isSelected());
        skinTextField.setEnabled(skinCheckBox.isSelected());
      }
    }
    );

    final Component parent=this;
    skinChooseBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        JFileChooser fileChooser=new JFileChooser();
        fileChooser.showOpenDialog(parent);
        File f=fileChooser.getSelectedFile();
        if (f!=null) {
          skinTextField.setText(f.getAbsolutePath());
        }
      }
    }
    );

    JPanel tablePanel=new JPanel();
    JPanel panel1=new JPanel(new BorderLayout());
    JPanel panel2=new JPanel();
    panel2.setLayout(new BoxLayout(panel2,BoxLayout.Y_AXIS));
    tablePanel.setLayout(new BoxLayout(tablePanel,BoxLayout.Y_AXIS));
    msg = mLocalizer.msg("tableBackground", "Table background");
    tablePanel.setBorder(BorderFactory.createTitledBorder(msg));
    ButtonGroup tablePanelBtnGroup = new ButtonGroup();
    blankRadio = new JRadioButton(mLocalizer.msg("blank", "Blank"));
    wallpaperRadio = new JRadioButton(mLocalizer.msg("wallpaper", "Wallpaper"));
    columnsRadio = new JRadioButton(mLocalizer.msg("columns", "Columns"));
    tablePanelBtnGroup.add(blankRadio);
    tablePanelBtnGroup.add(wallpaperRadio);
    tablePanelBtnGroup.add(columnsRadio);

    int mode=Settings.getTableBGMode();
    if (mode==SkinPanel.NONE) {
      blankRadio.setSelected(true);
    }else if (mode==SkinPanel.WALLPAPER) {
      wallpaperRadio.setSelected(true);
    }else if (mode==SkinPanel.COLUMNS) {
      columnsRadio.setSelected(true);
    }

    blankRadio.addActionListener(this);
    wallpaperRadio.addActionListener(this);
    columnsRadio.addActionListener(this);

    JPanel tableBGPanel=new JPanel(new BorderLayout(10,0));
    skinTableBGLabel=new JLabel(mLocalizer.msg("skin", "Background"));
    skinTableBGTextField=new JTextField(Settings.getTableSkin());
    skinTableBGBtn=new JButton(mLocalizer.msg("change", "Change"));
    tableBGPanel.add(skinTableBGLabel,BorderLayout.WEST);
    tableBGPanel.add(skinTableBGTextField,BorderLayout.CENTER);
    tableBGPanel.add(skinTableBGBtn,BorderLayout.EAST);

    skinTableBGBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        JFileChooser fileChooser=new JFileChooser();
        fileChooser.showOpenDialog(parent);
        File selection = fileChooser.getSelectedFile();
        if (selection != null) {
          skinTableBGTextField.setText(selection.getAbsolutePath());
        }
      }
    }
    );

    panel2.add(blankRadio);
    panel2.add(Box.createRigidArea(new Dimension(0,5)));
    panel2.add(wallpaperRadio);
    panel2.add(Box.createRigidArea(new Dimension(0,5)));
    panel2.add(columnsRadio);
    panel2.add(Box.createRigidArea(new Dimension(0,5)));
    panel1.add(panel2,BorderLayout.WEST);
    tablePanel.add(panel1);
    tablePanel.add(tableBGPanel);

    JPanel buttonPanel=new JPanel(new GridLayout(1,0));
    msg = mLocalizer.msg("buttons", "Buttons");
    buttonPanel.setBorder(BorderFactory.createTitledBorder(msg));

    JPanel visibleBtnsPanel=new JPanel(new BorderLayout());
    JPanel panel3=new JPanel(new BorderLayout());
    msg = mLocalizer.msg("showButtons", "Show buttons");
    panel3.setBorder(BorderFactory.createTitledBorder(msg));
    panel3.setLayout(new BoxLayout(panel3,BoxLayout.Y_AXIS));

    mTimeCheck = new JCheckBox(mLocalizer.msg("buttons.time", "Time buttons"));
    updateCheck = new JCheckBox(mLocalizer.msg("buttons.update", "Update"));
    settingsCheck = new JCheckBox(mLocalizer.msg("buttons.settings", "Settings"));
    panel3.add(mTimeCheck);
    panel3.add(updateCheck);
    panel3.add(settingsCheck);
    

    mTimeCheck.setSelected(Settings.isTimeBtnVisible());
    updateCheck.setSelected(Settings.isUpdateBtnVisible());
    settingsCheck.setSelected(Settings.isPreferencesBtnVisible());
    
    visibleBtnsPanel.add(panel3,BorderLayout.NORTH);

    JPanel labelBtnsPanel=new JPanel(new BorderLayout());
    JPanel panel4=new JPanel(new BorderLayout());
    msg = mLocalizer.msg("label", "Label");
    panel4.setBorder(BorderFactory.createTitledBorder(msg));
    panel4.setLayout(new BoxLayout(panel4,BoxLayout.Y_AXIS));
    textOnlyRadio = new JRadioButton(mLocalizer.msg("textOnly", "Text only"));
    picOnlyRadio = new JRadioButton(mLocalizer.msg("imagesOnly", "Images only"));
    textAndPicRadio = new JRadioButton(mLocalizer.msg("textAndImages", "Text and images"));
    ButtonGroup labelBtnsGroup=new ButtonGroup();
    labelBtnsGroup.add(textOnlyRadio);
    labelBtnsGroup.add(picOnlyRadio);
    labelBtnsGroup.add(textAndPicRadio);

    if (Settings.getButtonSettings()==Settings.TEXT_ONLY) {
      textOnlyRadio.setSelected(true);
    }else if (Settings.getButtonSettings()==Settings.ICON_ONLY) {
      picOnlyRadio.setSelected(true);
    }else {
      textAndPicRadio.setSelected(true);
    }

    panel4.add(textOnlyRadio);
    panel4.add(picOnlyRadio);
    panel4.add(textAndPicRadio);
    labelBtnsPanel.add(panel4,BorderLayout.NORTH);

    buttonPanel.add(visibleBtnsPanel);
    buttonPanel.add(labelBtnsPanel);

    content.add(lfPanel);
    content.add(Box.createRigidArea(new Dimension(0,10)));
    content.add(skinPanel);
    content.add(Box.createRigidArea(new Dimension(0,10)));
    content.add(tablePanel);
    content.add(Box.createRigidArea(new Dimension(0,10)));
    content.add(buttonPanel);

    add(content,BorderLayout.NORTH);

  }


  private LookAndFeelObj[] getLookAndFeelObjs() {

    UIManager.LookAndFeelInfo[] info=UIManager.getInstalledLookAndFeels();
    LookAndFeelObj[] result=new LookAndFeelObj[info.length];
    for (int i=0;i<info.length;i++) {
      result[i]=new LookAndFeelObj(info[i]);
    }

    return result;
  }

  public void actionPerformed(ActionEvent event) {

    Object source=event.getSource();
    if (source==blankRadio) {
      if (blankRadio.isSelected()) {
        skinTableBGLabel.setEnabled(false);
        skinTableBGBtn.setEnabled(false);
        skinTableBGTextField.setEnabled(false);
      }
    }else if (source==wallpaperRadio || source==columnsRadio) {
      if (wallpaperRadio.isSelected() || columnsRadio.isSelected()) {
        skinTableBGLabel.setEnabled(true);
        skinTableBGBtn.setEnabled(true);
        skinTableBGTextField.setEnabled(true);
      }
    }


  }

  public String getName() {
    return mLocalizer.msg("appearance", "Appearance");
  }

  public void ok() {
    LookAndFeelObj obj=(LookAndFeelObj)lfComboBox.getSelectedItem();
    try {
      UIManager.setLookAndFeel(obj.getLFClassName());
      Settings.setLookAndFeel(obj.getLFClassName());
    }
    catch (Exception exc) {
      String msg = mLocalizer.msg("error.1", "Unable to set look and feel.", exc);
      ErrorHandler.handle(msg, exc);
    }

    Settings.setUseApplicationSkin(this.skinCheckBox.isSelected());
    Settings.setApplicationSkin(skinTextField.getText());

    Settings.setTableSkin(this.skinTableBGTextField.getText());
    if (wallpaperRadio.isSelected()) {
      Settings.setTableBGMode(SkinPanel.WALLPAPER);
    }else if (columnsRadio.isSelected()) {
      Settings.setTableBGMode(SkinPanel.COLUMNS);
    }else {
      Settings.setTableBGMode(SkinPanel.NONE);
    }

    Settings.setTimeBtnVisible(mTimeCheck.isSelected());
    Settings.setUpdateBtnVisible(updateCheck.isSelected());
    Settings.setPreferencesBtnVisible(settingsCheck.isSelected());
    

    if (textOnlyRadio.isSelected()) {
      Settings.setButtonSettings(Settings.TEXT_ONLY);
    }else if (picOnlyRadio.isSelected()) {
      Settings.setButtonSettings(Settings.ICON_ONLY);
    }else {
      Settings.setButtonSettings(Settings.TEXT_AND_ICON);
    }

  }
}