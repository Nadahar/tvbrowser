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

import tvbrowser.core.Settings;

import java.awt.*;
import java.awt.event.*;
import java.io.File;

import javax.swing.*;


import devplugin.SettingsTab;

public class SkinLFSettingsTab implements SettingsTab {
  
  private static final util.ui.Localizer mLocalizer
      = util.ui.Localizer.getLocalizerFor(SkinLFSettingsTab.class);

  private JCheckBox mUseSkinLFCb;
  private JPanel mSettingsPn;
  private final JTextField mThemepackTf=new JTextField();
  
  public JPanel createSettingsPanel() {
    mSettingsPn=new JPanel(new BorderLayout());
    mSettingsPn.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
    
    JPanel northPanel=new JPanel();
    northPanel.setLayout(new BoxLayout(northPanel,BoxLayout.Y_AXIS));
    JTextArea licenseTA=new JTextArea();
    licenseTA.setText("TV-Browser includes software developed " +
                      "by L2FProd.com (http://www.L2FProd.com/).\n"+
                      "Skin Look And Feel 1.2.3\n"+
                      "Copyright (c) 2000-2002 L2FProd.com.  All rights reserved.");
    
    licenseTA.setWrapStyleWord(true);
    licenseTA.setLineWrap(true);
    licenseTA.setFocusable(false);
    licenseTA.setEditable(false);
    licenseTA.setOpaque(false);
    licenseTA.setBorder(BorderFactory.createLoweredBevelBorder());
    
    
    
    mUseSkinLFCb=new JCheckBox(mLocalizer.msg("useSkinLF","use SkinLF"));
    mUseSkinLFCb.setBorder(BorderFactory.createEmptyBorder(7,0,7,0));
    JPanel useSkinLFPanel=new JPanel(new BorderLayout());
    useSkinLFPanel.add(mUseSkinLFCb);
    
    
    JPanel themepackPanel=new JPanel(new BorderLayout(7,0));
    themepackPanel.setBorder(BorderFactory.createTitledBorder("Themepack"));
    
    final JButton chooseBtn=new JButton(mLocalizer.msg("change","change"));
    mThemepackTf.setText(Settings.getSkinLFThemepack());
    themepackPanel.add(mThemepackTf,BorderLayout.CENTER);
    themepackPanel.add(chooseBtn,BorderLayout.EAST);   
    
    northPanel.add(licenseTA);
    northPanel.add(useSkinLFPanel);
    northPanel.add(themepackPanel);
    mSettingsPn.add(northPanel,BorderLayout.NORTH);
    
    boolean enabled=Settings.isSkinLFEnabled();
    mUseSkinLFCb.setSelected(enabled);
    chooseBtn.setEnabled(enabled);
    mThemepackTf.setEnabled(enabled); 
    
    mUseSkinLFCb.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        boolean enabled= mUseSkinLFCb.isSelected();
        chooseBtn.setEnabled(enabled);
        mThemepackTf.setEnabled(enabled); 
      }
    });
    
    chooseBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        JFileChooser fileChooser=new JFileChooser("./");
        fileChooser.setFileFilter(new util.ui.ExtensionFileFilter(".zip","SkinLF themepacks (*.zip)"));
        fileChooser.showOpenDialog(mSettingsPn);
        File f=fileChooser.getSelectedFile();
        if (f!=null) {
          mThemepackTf.setText(f.getAbsolutePath());
        }
      }
    });
    return mSettingsPn;
  }

 
  public void saveSettings() {
    Settings.setSkinLFEnabled(mUseSkinLFCb.isSelected());
    Settings.setSkinLFThemepack(mThemepackTf.getText());    
  }

 
  public Icon getIcon() {
    return null;
  }

  
  public String getTitle() {
    return "SkinLF";
  }
  
  
}