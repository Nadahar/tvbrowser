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

package tvbrowser.extras.programinfo;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import tvbrowser.core.icontheme.IconLoader;
import util.program.ProgramTextCreator;
import util.ui.FontChooserPanel;
import util.ui.OrderChooser;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.l2fprod.common.swing.plaf.LookAndFeelAddons;

import devplugin.Plugin;
import devplugin.ProgramFieldType;
import devplugin.SettingsTab;

/**
 * The SettingsTab for the ProgramInfo viewer.
 */
public class ProgramInfoSettingsTab implements SettingsTab {

  private static final util.ui.Localizer mLocalizer = util.ui.Localizer
      .getLocalizerFor(ProgramInfoSettingsTab.class);

  private JCheckBox mUserFont, mAntiAliasing;
  private FontChooserPanel mTitleFont, mBodyFont;
  private Properties mSettings;
  private JComboBox mLook;
  private OrderChooser mList;

  private String mOldOrder, mOldLook, mOldTitleFont, mOldBodyFont,
      mOldTitleFontSize, mOldBodyFontSize, mOldUserFontSelected,
      mOldAntiAliasingSelected;
  
  private String[] mLf = {
      "com.l2fprod.common.swing.plaf.aqua.AquaLookAndFeelAddons",
      "com.l2fprod.common.swing.plaf.metal.MetalLookAndFeelAddons",
      "com.l2fprod.common.swing.plaf.motif.MotifLookAndFeelAddons",
      "com.l2fprod.common.swing.plaf.windows.WindowsLookAndFeelAddons",
      "com.l2fprod.common.swing.plaf.windows.WindowsClassicLookAndFeelAddons"
  };

  /**
   * Constructor
   * 
   */
  public ProgramInfoSettingsTab() {
    mSettings = ProgramInfo.getInstance().getSettings();
  }

  public JPanel createSettingsPanel() {
    mOldLook = mSettings.getProperty("look", "");
    mOldAntiAliasingSelected = mSettings.getProperty("antialiasing", "false");
    mOldUserFontSelected = mSettings.getProperty("userfont", "false");
    mOldTitleFontSize = mSettings.getProperty("title", "18");
    mOldBodyFontSize = mSettings.getProperty("small", "11");
    mOldTitleFont = mSettings.getProperty("titlefont", "Verdana");
    mOldBodyFont = mSettings.getProperty("bodyfont", "Verdana");
    mOldOrder = mSettings.getProperty("order", "");

    String[] lf = {"Aqua", "Metal", "Motif", "Windows XP",
    "Windows Classic"};
    
    mLook = new JComboBox(lf);
    
    String look = mOldLook.length() > 0 ? mOldLook : LookAndFeelAddons.getBestMatchAddonClassName();
    
    for(int i = 0; i < mLf.length; i++)
      if(look.toLowerCase().indexOf(mLf[i].toLowerCase()) != -1) {
        mLook.setSelectedIndex(i);
        break;
      }

    mAntiAliasing = new JCheckBox(mLocalizer
        .msg("antialiasing", "Antialiasing"));
    mAntiAliasing.setSelected(mOldAntiAliasingSelected.compareToIgnoreCase("true") == 0);

    mUserFont = new JCheckBox(mLocalizer.msg("userfont", "Use user fonts"));
    mUserFont.setSelected(mOldUserFontSelected.compareToIgnoreCase("true") == 0);

    int size = Integer.parseInt(mOldTitleFontSize);

    mTitleFont = new FontChooserPanel(mLocalizer.msg("title", "Title font"),
        new Font(mOldTitleFont, Font.PLAIN, size), false);
    mTitleFont.setMaximumSize(mTitleFont.getPreferredSize());
    mTitleFont.setAlignmentX(FontChooserPanel.LEFT_ALIGNMENT);
    mTitleFont.setBorder(BorderFactory.createEmptyBorder(5, 20, 0, 0));

    size = Integer.parseInt(mOldBodyFontSize);

    mBodyFont = new FontChooserPanel(
        mLocalizer.msg("body", "Description font"), new Font(mOldBodyFont,
            Font.PLAIN, size), false);
    mBodyFont.setMaximumSize(mBodyFont.getPreferredSize());
    mBodyFont.setAlignmentX(FontChooserPanel.LEFT_ALIGNMENT);
    mBodyFont.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));

    mTitleFont.setEnabled(mUserFont.isSelected());
    mBodyFont.setEnabled(mUserFont.isSelected());

    mUserFont.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        mTitleFont.setEnabled(mUserFont.isSelected());
        mBodyFont.setEnabled(mUserFont.isSelected());
      }
    });

    Object[] order;
    
    if (mOldOrder.indexOf(";") == -1) {
      if(mSettings.getProperty("setupwasdone","false").compareTo("false") == 0)
        order = ProgramTextCreator.getDefaultOrder();
      else
        order = new Object[0];
      
      mList = new OrderChooser(order,ProgramTextCreator.getDefaultOrder(),true);
    }
    else {
      String[] id = mOldOrder.trim().split(";");
      order = new Object[id.length];
      for (int i = 0; i < order.length; i++)
        try {
          order[i] = ProgramFieldType
              .getTypeForId(Integer.parseInt((String) id[i]));
          
          if(((ProgramFieldType)order[i]).getTypeId() == ProgramFieldType.UNKOWN_FORMAT)
            order[i] = ProgramTextCreator.getDurationTypeString();
          
        } catch (Exception e) {
          order[i] = id[i];
        }
      mList = new OrderChooser(order,ProgramTextCreator.getDefaultOrder(),true);
    }

    JButton previewBtn = new JButton(mLocalizer.msg("preview", "Prewview"));
    previewBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        saveSettings();
        ProgramInfo.getInstance().showProgramInformation(
            Plugin.getPluginManager().getExampleProgram(), false);
        restoreSettings();
      }
    });

    JButton defaultBtn = new JButton(mLocalizer.msg("default", "Default"));
    defaultBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        resetSettings();
      }
    });

    CellConstraints cc = new CellConstraints();

    PanelBuilder panelBuilder = new PanelBuilder(
        new FormLayout(
            "5dlu, pref, pref:grow",
            "pref, 5dlu, pref, 10dlu, pref, 5dlu, pref, pref, pref, pref, 10dlu, pref, 5dlu, pref, 5dlu, pref"));
    panelBuilder.setDefaultDialogBorder();
    panelBuilder.addSeparator(mLocalizer.msg("design", "Design"), cc.xyw(1, 1,
        3));
    panelBuilder.add(mLook, cc.xy(2, 3));
    panelBuilder.addSeparator(mLocalizer.msg("font", "Font"), cc.xyw(1, 5, 3));
    panelBuilder.add(mAntiAliasing, cc.xy(2, 7));
    panelBuilder.add(mUserFont, cc.xy(2, 8));
    panelBuilder.add(mTitleFont, cc.xy(2, 9));
    panelBuilder.add(mBodyFont, cc.xy(2, 10));
    panelBuilder.addSeparator(mLocalizer.msg("order", "Order"), cc
        .xyw(1, 12, 3));
    panelBuilder.add(mList, cc.xyw(2, 14, 2));
    panelBuilder.addSeparator("", cc.xyw(1, 16, 3));

    JPanel buttonsPn = new JPanel(new BorderLayout());
    buttonsPn.add(defaultBtn, BorderLayout.WEST);
    buttonsPn.add(previewBtn, BorderLayout.EAST);
    buttonsPn.setBorder(panelBuilder.getPanel().getBorder());

    JPanel content = new JPanel(new BorderLayout());
    content.add(panelBuilder.getPanel(), BorderLayout.CENTER);
    content.add(buttonsPn, BorderLayout.SOUTH);

    return content;
  }

  private void resetSettings() {
    mList.setOrder(ProgramTextCreator.getDefaultOrder(),ProgramTextCreator.getDefaultOrder());
    mAntiAliasing.setSelected(false);
    mUserFont.setSelected(false);
    
    String look = LookAndFeelAddons.getBestMatchAddonClassName();
    
    for(int i = 0; i < mLf.length; i++)
      if(look.toLowerCase().indexOf(mLf[i].toLowerCase()) != -1) {
        mLook.setSelectedIndex(i);
        break;
      }
  }

  public void saveSettings() {
    mSettings.setProperty("antialiasing", String.valueOf(mAntiAliasing
        .isSelected()));
    mSettings.setProperty("userfont", String.valueOf(mUserFont.isSelected()));

    Font f = mTitleFont.getChosenFont();
    mSettings.setProperty("titlefont", f.getFamily());
    mSettings.setProperty("title", String.valueOf(f.getSize()));

    f = mBodyFont.getChosenFont();
    mSettings.setProperty("bodyfont", f.getFamily());
    mSettings.setProperty("small", String.valueOf(f.getSize()));
    
    mSettings.setProperty("look", mLf[mLook.getSelectedIndex()]);
    ProgramInfo.getInstance().setLook();

    Object[] o = mList.getOrder();

    String temp = "";

    for (int i = 0; i < o.length; i++)
      if (o[i] instanceof String)
        temp += ProgramFieldType.UNKOWN_FORMAT;
      else
        temp += ((ProgramFieldType) o[i]).getTypeId() + ";";

    mSettings.setProperty("order", temp);
    mSettings.setProperty("setupwasdone", "true");
    ProgramInfo.getInstance().setOrder();
  }

  private void restoreSettings() {
    mSettings.setProperty("antialiasing", mOldAntiAliasingSelected);
    mSettings.setProperty("userfont", mOldUserFontSelected);
    mSettings.setProperty("titlefont", mOldTitleFont);
    mSettings.setProperty("title", mOldTitleFontSize);
    mSettings.setProperty("bodyfont", mOldBodyFont);
    mSettings.setProperty("small", mOldBodyFontSize);    
    mSettings.setProperty("look", mOldLook);
    
    ProgramInfo.getInstance().setLook();
    mSettings.setProperty("order", mOldOrder);
    ProgramInfo.getInstance().setOrder();
  }

  public Icon getIcon() {
    return IconLoader.getInstance().getIconFromTheme("actions",
        "edit-find", 16);
  }

  public String getTitle() {
    return "ProgramInfo";
  }
}