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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.UIManager;

import tvbrowser.core.Settings;
import util.ui.LinkButton;

import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.factories.DefaultComponentFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

import devplugin.SettingsTab;

public class LookAndFeelSettingsTab implements SettingsTab {
  
  private static final util.ui.Localizer mLocalizer
      = util.ui.Localizer.getLocalizerFor(LookAndFeelSettingsTab.class);


  private JRadioButton mUseSkinLFRb;
  private JRadioButton mUseJavaLFRb;
  private JComboBox mLfComboBox;
  private JPanel mSettingsPn;
  private final JTextField mThemepackTf=new JTextField();
  private JButton mChooseBtn;
  private JCheckBox mShowChannelIconsCb, mProgramtableChIconsCb, mChannellistChIconsCb,
                    mShowChannelNamesCb;

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
    FormLayout layout = new FormLayout("right:pref, 3dlu, fill:pref:grow, 3dlu, pref", 
        "");
    
    CellConstraints cc = new CellConstraints();
    mSettingsPn = new JPanel(layout);
    mSettingsPn.setBorder(Borders.DLU4_BORDER);

    layout.appendRow(new RowSpec("pref"));
    mSettingsPn.add(DefaultComponentFactory.getInstance().createSeparator("Aussehen"), cc.xyw(1, 1, 5));
   
    layout.appendRow(new RowSpec("3dlu")); layout.appendRow(new RowSpec("pref"));

    mSettingsPn.add(new JLabel("Theme:"), cc.xy(1, 3));

    LookAndFeelObj[] obj=getLookAndFeelObjs();
    mLfComboBox=new JComboBox(obj);
    String lf = Settings.propLookAndFeel.getString();
    for (int i=0;i<obj.length;i++) {
      if (obj[i].getLFClassName().equals(lf)) {
        mLfComboBox.setSelectedItem(obj[i]);
      }
    }
    
    mSettingsPn.add(mLfComboBox, cc.xy(3, 3));
    mSettingsPn.add(new JButton("Config"), cc.xy(5, 3));

    layout.appendRow(new RowSpec("3dlu")); layout.appendRow(new RowSpec("pref"));

    mSettingsPn.add(new JLabel("Icons:"), cc.xy(1, 5));
    mSettingsPn.add(new JComboBox(), cc.xy(3, 5));

    layout.appendRow(new RowSpec("3dlu")); layout.appendRow(new RowSpec("pref"));

    mSettingsPn.add(new LinkButton("You can find more Icons on our Web-Page.", "http://www.tvbrowser.org/iconthemes.php"), cc.xy(3, 7));

    layout.appendRow(new RowSpec("3dlu")); layout.appendRow(new RowSpec("pref"));

    mSettingsPn.add(DefaultComponentFactory.getInstance().createSeparator("Senderlogos"), cc.xyw(1, 9, 5));
    
    layout.appendRow(new RowSpec("3dlu")); layout.appendRow(new RowSpec("pref"));
    
    mSettingsPn.add(createChannelIconPanel(), cc.xyw(1, 11, 5));
    
    return mSettingsPn;    
    /*
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



    JPanel channelIconPanel = new JPanel(new TabLayout(1));
    channelIconPanel.setBorder(BorderFactory.createTitledBorder(mLocalizer.msg("channelIcons.title","Senderlogos")));

    mShowChannelIconsCb = new JCheckBox(mLocalizer.msg("channelIcons.show","Senderlogso anzeigen"));
    mProgramtableChIconsCb = new JCheckBox(mLocalizer.msg("channelIcons.programtable","Programmtabelle"));
    mChannellistChIconsCb = new JCheckBox(mLocalizer.msg("channelIcons.channellist","Kanalliste"));

    mShowChannelNamesCb = new JCheckBox(mLocalizer.msg("showChannelName", "Show channel name"));

    mShowChannelIconsCb.setSelected(Settings.propEnableChannelIcons.getBoolean());
    mProgramtableChIconsCb.setSelected(Settings.propShowChannelIconsInProgramTable.getBoolean());
    mChannellistChIconsCb.setSelected(Settings.propShowChannelIconsInChannellist.getBoolean());
    mShowChannelNamesCb.setSelected(Settings.propShowChannelNames.getBoolean());

    mShowChannelIconsCb.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        boolean enabled = mShowChannelIconsCb.isSelected();
        mProgramtableChIconsCb.setEnabled(enabled);
        mChannellistChIconsCb.setEnabled(enabled);
        mProgramtableChIconsCb.setSelected(enabled);
        mChannellistChIconsCb.setSelected(enabled);
        mShowChannelNamesCb.setEnabled(enabled);
        if (!enabled) {
          mShowChannelNamesCb.setSelected(true);
        }
      }
    });

    boolean enabled = mShowChannelIconsCb.isSelected();
    mProgramtableChIconsCb.setEnabled(enabled);
    mChannellistChIconsCb.setEnabled(enabled);
    mShowChannelNamesCb.setEnabled(enabled);
    if (!enabled) {
      mShowChannelNamesCb.setSelected(true);
    }
    mProgramtableChIconsCb.setBorder(BorderFactory.createEmptyBorder(0,15,0,0));
    mChannellistChIconsCb.setBorder(BorderFactory.createEmptyBorder(0,15,0,0));

    channelIconPanel.add(mShowChannelIconsCb);
    channelIconPanel.add(mProgramtableChIconsCb);
    channelIconPanel.add(mChannellistChIconsCb);
    channelIconPanel.add(mShowChannelNamesCb);

    main.add(channelIconPanel);




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
    */
    
  }

  private JPanel createChannelIconPanel() {
    FormLayout layout = new FormLayout("4dlu, fill:pref:grow", "");
    JPanel channelIconPanel = new JPanel(layout);

    mShowChannelIconsCb = new JCheckBox(mLocalizer.msg("channelIcons.show","Senderlogso anzeigen"));
    mProgramtableChIconsCb = new JCheckBox(mLocalizer.msg("channelIcons.programtable","Programmtabelle"));
    mChannellistChIconsCb = new JCheckBox(mLocalizer.msg("channelIcons.channellist","Kanalliste"));

    mShowChannelNamesCb = new JCheckBox(mLocalizer.msg("showChannelName", "Show channel name"));

    mShowChannelIconsCb.setSelected(Settings.propEnableChannelIcons.getBoolean());
    mProgramtableChIconsCb.setSelected(Settings.propShowChannelIconsInProgramTable.getBoolean());
    mChannellistChIconsCb.setSelected(Settings.propShowChannelIconsInChannellist.getBoolean());
    mShowChannelNamesCb.setSelected(Settings.propShowChannelNames.getBoolean());

    mShowChannelIconsCb.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        boolean enabled = mShowChannelIconsCb.isSelected();
        mProgramtableChIconsCb.setEnabled(enabled);
        mChannellistChIconsCb.setEnabled(enabled);
        mProgramtableChIconsCb.setSelected(enabled);
        mChannellistChIconsCb.setSelected(enabled);
        mShowChannelNamesCb.setEnabled(enabled);
        if (!enabled) {
          mShowChannelNamesCb.setSelected(true);
        }
      }
    });

    boolean enabled = mShowChannelIconsCb.isSelected();
    mProgramtableChIconsCb.setEnabled(enabled);
    mChannellistChIconsCb.setEnabled(enabled);
    mShowChannelNamesCb.setEnabled(enabled);
    if (!enabled) {
      mShowChannelNamesCb.setSelected(true);
    }

    mProgramtableChIconsCb.setBorder(BorderFactory.createEmptyBorder(0,15,0,0));
    mChannellistChIconsCb.setBorder(BorderFactory.createEmptyBorder(0,15,0,0));
    mShowChannelNamesCb.setBorder(BorderFactory.createEmptyBorder(0,15,0,0));

    CellConstraints cc = new CellConstraints();
    
    layout.appendRow(new RowSpec("pref"));  
    channelIconPanel.add(mShowChannelIconsCb, cc.xyw(1, 1, 2));
    layout.appendRow(new RowSpec("3dlu")); layout.appendRow(new RowSpec("pref"));
    channelIconPanel.add(mProgramtableChIconsCb, cc.xy(2, 3));
    layout.appendRow(new RowSpec("3dlu")); layout.appendRow(new RowSpec("pref"));
    channelIconPanel.add(mChannellistChIconsCb, cc.xy(2, 5));
    layout.appendRow(new RowSpec("3dlu")); layout.appendRow(new RowSpec("pref"));
    channelIconPanel.add(mShowChannelNamesCb, cc.xy(2, 7));
    
    return channelIconPanel;
  }



  private void setUseSkinLF(boolean b) {
    mLfComboBox.setEnabled(!b);
    mThemepackTf.setEnabled(b);
    mChooseBtn.setEnabled(b);

  }

  public void saveSettings() {
    LookAndFeelObj obj=(LookAndFeelObj)mLfComboBox.getSelectedItem();
    Settings.propLookAndFeel.setString(obj.getLFClassName());
 
//    Settings.propSkinLFThemepack.setString(mThemepackTf.getText());

    boolean enableChannelIcons = mShowChannelIconsCb.isSelected();
    Settings.propEnableChannelIcons.setBoolean(enableChannelIcons);
    Settings.propShowChannelIconsInChannellist.setBoolean(mChannellistChIconsCb.isSelected());
    Settings.propShowChannelIconsInProgramTable.setBoolean(mProgramtableChIconsCb.isSelected());
    Settings.propShowChannelNames.setBoolean(mShowChannelNamesCb.isSelected());
  }

 
  public Icon getIcon() {
    return null;
  }

  
  public String getTitle() {
    return "Look&Feel";
  }
  
  
}