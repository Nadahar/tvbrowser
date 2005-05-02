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

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.*;

import tvbrowser.core.Settings;
import devplugin.SettingsTab;
import util.ui.TabLayout;
import util.ui.LinkButton;
import util.ui.UiUtilities;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.CellConstraints;

public class LookAndFeelSettingsTab implements SettingsTab {
  
  private static final util.ui.Localizer mLocalizer
      = util.ui.Localizer.getLocalizerFor(LookAndFeelSettingsTab.class);


  private JRadioButton mUseSkinLFRb;
  private JRadioButton mUseJavaLFRb;
  private JComboBox mLfComboBox;
  private JPanel mSettingsPn;
  private final JTextField mThemepackTf=new JTextField();
  private JButton mChooseBtn;

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
  
  
  private LookAndFeelObj[] getLookAndFeelObjs() {
      UIManager.LookAndFeelInfo[] info=UIManager.getInstalledLookAndFeels();
      LookAndFeelObj[] result=new LookAndFeelObj[info.length];
      for (int i=0;i<info.length;i++) {
        result[i]=new LookAndFeelObj(info[i]);
      }

      return result;
    }
  
  public JPanel createSettingsPanel() {

    mSettingsPn=new JPanel(new BorderLayout());
    mSettingsPn.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
    
    JPanel northPanel=new JPanel();
    northPanel.setLayout(new BoxLayout(northPanel,BoxLayout.Y_AXIS));


    JPanel main = new JPanel(new TabLayout(1));

    mSettingsPn.add(main, BorderLayout.NORTH);

    LookAndFeelObj[] obj=getLookAndFeelObjs();
     mLfComboBox=new JComboBox(obj);
     String lf = Settings.propLookAndFeel.getString();
     for (int i=0;i<obj.length;i++) {
       if (obj[i].getLFClassName().equals(lf)) {
         mLfComboBox.setSelectedItem(obj[i]);
       }
     }


    mChooseBtn=new JButton(mLocalizer.msg("change","change"));
    mThemepackTf.setText(Settings.propSkinLFThemepack.getString());

    JPanel lnfPanel = new JPanel(new TabLayout(1));
    FormLayout formLayout = new FormLayout("default,5dlu,default,40dlu,5dlu,default","default,3dlu,default,3dlu,default");
    lnfPanel.setLayout(formLayout);
    CellConstraints c = new CellConstraints();
    lnfPanel.setBorder(BorderFactory.createTitledBorder("Look&Feel"));
    lnfPanel.add(mUseJavaLFRb = new JRadioButton("Look & Feel:"), c.xy(1,1));
    lnfPanel.add(mLfComboBox, c.xy(3,1));
    lnfPanel.add(mUseSkinLFRb = new JRadioButton("SkinLF themepack:"), c.xy(1,3));
    lnfPanel.add(mThemepackTf, c.xyw(3,3,2));
    lnfPanel.add(mChooseBtn, c.xy(6,3));
    lnfPanel.add(new LinkButton(mLocalizer.msg("downloadThemepacks","Download more themepacks from tvbrowser.org"),"http://www.tvbrowser.org"), c.xyw(1,5,6));

    ButtonGroup group = new ButtonGroup();
    group.add(mUseJavaLFRb);
    group.add(mUseSkinLFRb);

    mUseJavaLFRb.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        setUseSkinLF(!mUseJavaLFRb.isSelected());
      }
    });

    mUseSkinLFRb.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        setUseSkinLF(mUseSkinLFRb.isSelected());
      }
    });

    setUseSkinLF(Settings.propIsSkinLFEnabled.getBoolean());
    if (Settings.propIsSkinLFEnabled.getBoolean()) {
      mUseSkinLFRb.setSelected(true);
    }
    else {
      mUseJavaLFRb.setSelected(true);
    }

    main.add(lnfPanel);



    
    JTextArea licenseTA=new JTextArea();
    licenseTA.setText("TV-Browser includes software developed " +
                      "by L2FProd.com (http://www.L2FProd.com/).\n"+
                      "Skin Look And Feel 1.2.7\n"+
                      "Copyright (c) 2000-2002 L2FProd.com.  All rights reserved.");
    licenseTA.setFont(new JLabel().getFont());
    licenseTA.setWrapStyleWord(true);
    licenseTA.setLineWrap(true);
    licenseTA.setFocusable(false);
    licenseTA.setEditable(false);
    licenseTA.setOpaque(false);
    licenseTA.setBorder(BorderFactory.createLoweredBevelBorder());


    JTextArea skinLFInfo = UiUtilities.createHelpTextArea(mLocalizer.msg("skinLFInfo",""));
    skinLFInfo.setBorder(BorderFactory.createLoweredBevelBorder());
    
    northPanel.add(skinLFInfo);
    

    mChooseBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        JFileChooser fileChooser=new JFileChooser(new File("themepacks"));
        fileChooser.setFileFilter(new util.ui.ExtensionFileFilter(".zip","SkinLF themepacks (*.zip)"));
        fileChooser.showOpenDialog(mSettingsPn);
        File f=fileChooser.getSelectedFile();
        if (f!=null) {
          mThemepackTf.setText(f.getAbsolutePath());
        }
      }
    });
    
    mSettingsPn.add(licenseTA,BorderLayout.SOUTH);
    
    return mSettingsPn;
  }


  private void setUseSkinLF(boolean b) {
    mLfComboBox.setEnabled(!b);
    mThemepackTf.setEnabled(b);
    mChooseBtn.setEnabled(b);

  }

  public void saveSettings() {
    LookAndFeelObj obj=(LookAndFeelObj)mLfComboBox.getSelectedItem();
    Settings.propLookAndFeel.setString(obj.getLFClassName());
 
    Settings.propIsSkinLFEnabled.setBoolean(mUseSkinLFRb.isSelected());
    Settings.propSkinLFThemepack.setString(mThemepackTf.getText());    
  }

 
  public Icon getIcon() {
    return null;
  }

  
  public String getTitle() {
    return "Look&Feel";
  }
  
  
}