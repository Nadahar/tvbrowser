/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (martin_oat@yahoo.de)
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
import java.util.*;
import java.awt.event.*;
import java.io.*;

import tvbrowser.core.*;

/**
 * TV-Browser
 *
 * @author Martin Oberhauser
 */
public class AppearanceSettingsTab extends devplugin.SettingsTab implements ActionListener {

  private JComboBox lfComboBox;
  private JTextField skinTextField;
  private JCheckBox skinCheckBox;

  private JRadioButton blankRadio, wallpaperRadio, columnsRadio;

  private JLabel skinTableBGLabel;
  private JButton skinTableBGBtn;
  private JTextField skinTableBGTextField;

  private JCheckBox mTimeCheck, prevNextCheck, updateCheck, settingsCheck, searchCheck;
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

    setLayout(new BorderLayout());
    JPanel content=new JPanel();
    content.setLayout(new BoxLayout(content,BoxLayout.Y_AXIS));
    content.setBorder(BorderFactory.createEmptyBorder(20,50,20,50));

    JPanel lfPanel=new JPanel(new BorderLayout(10,0));
    JLabel lfLabel=new JLabel("Java Look & Feel:");

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
    skinCheckBox=new JCheckBox("Use Skin:");
    skinTextField=new JTextField(Settings.getApplicationSkin());
    final JButton skinChooseBtn=new JButton("Change");
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
    tablePanel.setBorder(BorderFactory.createTitledBorder("Table background:"));
    ButtonGroup tablePanelBtnGroup=new ButtonGroup();
    blankRadio=new JRadioButton("blank");
    wallpaperRadio=new JRadioButton("Wallpaper");
    columnsRadio=new JRadioButton("Columns");
    tablePanelBtnGroup.add(blankRadio);
    tablePanelBtnGroup.add(wallpaperRadio);
    tablePanelBtnGroup.add(columnsRadio);

    int mode=Settings.getTableBGMode();
    if (mode==Settings.NONE) {
      blankRadio.setSelected(true);
    }else if (mode==Settings.WALLPAPER) {
      wallpaperRadio.setSelected(true);
    }else if (mode==Settings.COLUMNS) {
      columnsRadio.setSelected(true);
    }

    blankRadio.addActionListener(this);
    wallpaperRadio.addActionListener(this);
    columnsRadio.addActionListener(this);

    JPanel tableBGPanel=new JPanel(new BorderLayout(10,0));
    skinTableBGLabel=new JLabel("Skin:");
    skinTableBGTextField=new JTextField(Settings.getTableSkin());
    skinTableBGBtn=new JButton("Change");
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
    buttonPanel.setBorder(BorderFactory.createTitledBorder("Buttons"));

    JPanel visibleBtnsPanel=new JPanel(new BorderLayout());
    JPanel panel3=new JPanel(new BorderLayout());
    panel3.setBorder(BorderFactory.createTitledBorder("Show buttons:"));
    panel3.setLayout(new BoxLayout(panel3,BoxLayout.Y_AXIS));

    mTimeCheck=new JCheckBox("Time buttons");
    prevNextCheck=new JCheckBox("Next / Previous");
    updateCheck=new JCheckBox("Update");
    settingsCheck=new JCheckBox("Settings");
    searchCheck=new JCheckBox("Search");
    panel3.add(mTimeCheck);
    panel3.add(prevNextCheck);
    panel3.add(updateCheck);
    panel3.add(settingsCheck);
    panel3.add(searchCheck);

    mTimeCheck.setSelected(Settings.isTimeBtnVisible());
    prevNextCheck.setSelected(Settings.isPrevNextBtnVisible());
    updateCheck.setSelected(Settings.isUpdateBtnVisible());
    settingsCheck.setSelected(Settings.isPreferencesBtnVisible());
    searchCheck.setSelected(Settings.isSearchBtnVisible());

    visibleBtnsPanel.add(panel3,BorderLayout.NORTH);

    JPanel labelBtnsPanel=new JPanel(new BorderLayout());
    JPanel panel4=new JPanel(new BorderLayout());
    panel4.setBorder(BorderFactory.createTitledBorder("Label:"));
    panel4.setLayout(new BoxLayout(panel4,BoxLayout.Y_AXIS));
    textOnlyRadio=new JRadioButton("text only");
    picOnlyRadio=new JRadioButton("images only");
    textAndPicRadio=new JRadioButton("text and images");
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
    return "Appearance";
  }

  public void ok() {
    LookAndFeelObj obj=(LookAndFeelObj)lfComboBox.getSelectedItem();
    try {
      UIManager.setLookAndFeel(obj.getLFClassName());
      Settings.setLookAndFeel(obj.getLFClassName());
    } catch (InstantiationException e) { e.printStackTrace();
    } catch (ClassNotFoundException e) { e.printStackTrace();
    } catch (UnsupportedLookAndFeelException e) { e.printStackTrace();
    } catch (IllegalAccessException e) { e.printStackTrace();
    }

    Settings.setUseApplicationSkin(this.skinCheckBox.isSelected());
    Settings.setApplicationSkin(skinTextField.getText());

    Settings.setTableSkin(this.skinTableBGTextField.getText());
    if (wallpaperRadio.isSelected()) {
      Settings.setTableBGMode(Settings.WALLPAPER);
    }else if (columnsRadio.isSelected()) {
      Settings.setTableBGMode(Settings.COLUMNS);
    }else {
      Settings.setTableBGMode(Settings.NONE);
    }

    Settings.setTimeBtnVisible(mTimeCheck.isSelected());
    Settings.setPrevNextBtnVisible(prevNextCheck.isSelected());
    Settings.setUpdateBtnVisible(updateCheck.isSelected());
    Settings.setPreferencesBtnVisible(settingsCheck.isSelected());
    Settings.setSearchBtnVisible(searchCheck.isSelected());

    if (textOnlyRadio.isSelected()) {
      Settings.setButtonSettings(Settings.TEXT_ONLY);
    }else if (picOnlyRadio.isSelected()) {
      Settings.setButtonSettings(Settings.ICON_ONLY);
    }else {
      Settings.setButtonSettings(Settings.TEXT_AND_ICON);
    }

  }
}