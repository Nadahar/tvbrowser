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

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.*;

import tvbrowser.core.Settings;
import devplugin.SettingsTab;

public class LookAndFeelSettingsTab implements SettingsTab {
  
  private static final util.ui.Localizer mLocalizer
      = util.ui.Localizer.getLocalizerFor(LookAndFeelSettingsTab.class);

  private JCheckBox mUseSkinLFCb;
  private JCheckBox mSkinCheckBox;
  private JTextField mSkinTextField;
  private JComboBox mLfComboBox;
  private JPanel mSettingsPn;
  private JLabel mLookAndFeelLb;
  private final JTextField mThemepackTf=new JTextField();
  
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
    String msg;
    
    mSettingsPn=new JPanel(new BorderLayout());
    mSettingsPn.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
    
    JPanel northPanel=new JPanel();
    northPanel.setLayout(new BoxLayout(northPanel,BoxLayout.Y_AXIS));
    
    
    //  Background
    JPanel skinPanel = new JPanel(new BorderLayout(5, 0));
    northPanel.add(skinPanel);
    msg = mLocalizer.msg("background", "Background");
    mSkinCheckBox = new JCheckBox(msg);
    mSkinTextField = new JTextField(Settings.propApplicationSkin.getString());
    msg = mLocalizer.msg("change", "Change");
    final JButton skinChooseBtn = new JButton(msg);
    skinPanel.add(mSkinCheckBox,BorderLayout.WEST);
    skinPanel.add(mSkinTextField,BorderLayout.CENTER);
    skinPanel.add(skinChooseBtn,BorderLayout.EAST);
    mSkinCheckBox.setSelected(Settings.propUseApplicationSkin.getBoolean());
    skinChooseBtn.setEnabled(Settings.propUseApplicationSkin.getBoolean());
    mSkinTextField.setEnabled(Settings.propUseApplicationSkin.getBoolean());

    mSkinCheckBox.addActionListener(new ActionListener() {
       public void actionPerformed(ActionEvent event) {
         skinChooseBtn.setEnabled(mSkinCheckBox.isSelected());
         mSkinTextField.setEnabled(mSkinCheckBox.isSelected());
       }
    }
    );

    skinChooseBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        JFileChooser fileChooser=new JFileChooser();
        String[] extArr = { ".jpg", ".jpeg", ".gif", ".png"};
        fileChooser.setFileFilter(new util.ui.ExtensionFileFilter(extArr, ".jpg, .gif, png"));
        fileChooser.showOpenDialog(mSettingsPn);
        File f=fileChooser.getSelectedFile();
        if (f!=null) {
          mSkinTextField.setText(f.getAbsolutePath());
        }
      }
    });

    
  // Look and feel
     JPanel lfPanel = new JPanel(new FlowLayout(FlowLayout.LEADING));
     northPanel.add(lfPanel);
    
     msg = mLocalizer.msg("lookAndFeel", "Aussehen");
     mLookAndFeelLb=new JLabel(msg);
     lfPanel.add(mLookAndFeelLb);

     LookAndFeelObj[] obj=getLookAndFeelObjs();
     mLfComboBox=new JComboBox(obj);
     String lf = Settings.propLookAndFeel.getString();
     for (int i=0;i<obj.length;i++) {
       if (obj[i].getLFClassName().equals(lf)) {
         mLfComboBox.setSelectedItem(obj[i]);
       }
     }
     lfPanel.add(mLfComboBox);

    
    
    JTextArea licenseTA=new JTextArea();
    licenseTA.setText("TV-Browser includes software developed " +
                      "by L2FProd.com (http://www.L2FProd.com/).\n"+
                      "Skin Look And Feel 1.2.7\n"+
                      "Copyright (c) 2000-2002 L2FProd.com.  All rights reserved.");
    
    licenseTA.setWrapStyleWord(true);
    licenseTA.setLineWrap(true);
    licenseTA.setFocusable(false);
    licenseTA.setEditable(false);
    licenseTA.setOpaque(false);
    licenseTA.setBorder(BorderFactory.createLoweredBevelBorder());
    
    
    JTextArea skinLFInfo=new JTextArea();
    skinLFInfo.setText(mLocalizer.msg("skinLFInfo",""));
    
    skinLFInfo.setWrapStyleWord(true);
    skinLFInfo.setLineWrap(true);
    skinLFInfo.setFocusable(false);
    skinLFInfo.setEditable(false);
    skinLFInfo.setOpaque(false);
    skinLFInfo.setBorder(BorderFactory.createLoweredBevelBorder());
    
    northPanel.add(skinLFInfo);
    
    mUseSkinLFCb=new JCheckBox(mLocalizer.msg("useSkinLF","use SkinLF"));
    mUseSkinLFCb.setBorder(BorderFactory.createEmptyBorder(7,0,7,0));
    JPanel useSkinLFPanel=new JPanel(new BorderLayout());
    useSkinLFPanel.add(mUseSkinLFCb);
    
    
    JPanel themepackPanel=new JPanel(new BorderLayout(7,0));
    themepackPanel.setBorder(BorderFactory.createTitledBorder("Themepack"));
    
    final JButton chooseBtn=new JButton(mLocalizer.msg("change","change"));
    mThemepackTf.setText(Settings.propSkinLFThemepack.getString());
    themepackPanel.add(mThemepackTf,BorderLayout.CENTER);
    themepackPanel.add(chooseBtn,BorderLayout.EAST);   
    
    //northPanel.add(licenseTA);
    northPanel.add(useSkinLFPanel);
    northPanel.add(themepackPanel);
    mSettingsPn.add(northPanel,BorderLayout.NORTH);
    
    boolean enabled = Settings.propIsSkinLFEnabled.getBoolean();
    mUseSkinLFCb.setSelected(enabled);
    chooseBtn.setEnabled(enabled);
    mThemepackTf.setEnabled(enabled); 
    mLookAndFeelLb.setEnabled(!enabled);
    mLfComboBox.setEnabled(!enabled);
    
    mUseSkinLFCb.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        boolean btnEnabled= mUseSkinLFCb.isSelected();
        chooseBtn.setEnabled(btnEnabled);
        mThemepackTf.setEnabled(btnEnabled); 
        mLookAndFeelLb.setEnabled(! btnEnabled);
        mLfComboBox.setEnabled(! btnEnabled);
      }
    });
    
    chooseBtn.addActionListener(new ActionListener() {
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

 
  public void saveSettings() {
    LookAndFeelObj obj=(LookAndFeelObj)mLfComboBox.getSelectedItem();
    Settings.propLookAndFeel.setString(obj.getLFClassName());
 
    Settings.propUseApplicationSkin.setBoolean(mSkinCheckBox.isSelected());
    Settings.propApplicationSkin.setString(mSkinTextField.getText());
    
    Settings.propIsSkinLFEnabled.setBoolean(mUseSkinLFCb.isSelected());
    Settings.propSkinLFThemepack.setString(mThemepackTf.getText());    
  }

 
  public Icon getIcon() {
    return null;
  }

  
  public String getTitle() {
    return "Look&Feel";
  }
  
  
}