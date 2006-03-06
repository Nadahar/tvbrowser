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

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

import tvbrowser.core.Settings;
import util.ui.FontChooserPanel;

public class FontsSettingsTab implements devplugin.SettingsTab {
 
  /** The localizer for this class. */
     private static final util.ui.Localizer mLocalizer
     = util.ui.Localizer.getLocalizerFor(FontsSettingsTab.class);
 
  private JCheckBox mUseDefaultFontsCB;
  private JCheckBox mEnableAntialiasingCB;
  
  private FontChooserPanel mTitleFontPanel, mInfoFontPanel,
    mChannelNameFontPanel, mTimeFontPanel;
 
  public FontsSettingsTab() {
   
  }
 

  public JPanel createSettingsPanel() {
    
    JPanel mainPanel=new JPanel(new BorderLayout());
    mainPanel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
    JPanel content=new JPanel();
    content.setLayout(new BoxLayout(content,BoxLayout.Y_AXIS));
    
    JPanel checkBoxPanel=new JPanel(new BorderLayout());
    checkBoxPanel.setBorder(BorderFactory.createEmptyBorder(0,0,3,0));
    
    mUseDefaultFontsCB=new JCheckBox(mLocalizer.msg("UseDefaultFonts", "Use default fonts"));
    mUseDefaultFontsCB.setSelected(Settings.propUseDefaultFonts.getBoolean());
    mEnableAntialiasingCB=new JCheckBox(mLocalizer.msg("EnableAntialiasing", "Enable antialiasing"));
    mEnableAntialiasingCB.setSelected(Settings.propEnableAntialiasing.getBoolean());
    
    checkBoxPanel.add(mEnableAntialiasingCB, BorderLayout.NORTH);
    checkBoxPanel.add(mUseDefaultFontsCB, BorderLayout.CENTER);
    
    content.add(checkBoxPanel);
    
    final FontChooser fontPanel=new FontChooser();
    
    mTitleFontPanel=new FontChooserPanel(mLocalizer.msg("ProgramTitle", "Program title"),Settings.propProgramTitleFont.getFont());
    mInfoFontPanel=new FontChooserPanel(mLocalizer.msg("ProgramInfo", "Program information"),Settings.propProgramInfoFont.getFont());
    mChannelNameFontPanel=new FontChooserPanel(mLocalizer.msg("ChannelNames", "Channel names"),Settings.propChannelNameFont.getFont());
    mTimeFontPanel=new FontChooserPanel(mLocalizer.msg("Time", "Time"),Settings.propProgramTimeFont.getFont());

    fontPanel.add(mTitleFontPanel);
    fontPanel.add(mInfoFontPanel);
    fontPanel.add(mChannelNameFontPanel);
    fontPanel.add(mTimeFontPanel);
        
    content.add(fontPanel);
    mainPanel.add(content,BorderLayout.NORTH);
    
    
    
    mUseDefaultFontsCB.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        fontPanel.setEnabled(!mUseDefaultFontsCB.isSelected());
      }
    });
    
    fontPanel.setEnabled(!mUseDefaultFontsCB.isSelected());
    
    return mainPanel;
    
    
    
     
   /* JPanel mainPn=new JPanel(new BorderLayout());
    
    JPanel content=new JPanel();
    mainPn.add(content,BorderLayout.NORTH);
    content.setLayout(new BoxLayout(content,BoxLayout.Y_AXIS));
      
    content.add(new JCheckBox("use default fonts"));
    
    JPanel fontPn=new JPanel(new GridLayout(0,1));
    
    fontPn.setBorder(BorderFactory.createTitledBorder("Fonts"));
    
    fontPn.add(new FontChooser("Program title"));
    fontPn.add(new FontChooser("Program information"));
    fontPn.add(new FontChooser("Channel names"));
    fontPn.add(new FontChooser("Time"));
    
    content.add(fontPn);
     
     
     
     return mainPn;*/
   }

  
    /**
     * Called by the host-application, if the user wants to save the settings.
     */
    public void saveSettings() {
      Settings.propProgramTitleFont.setFont(mTitleFontPanel.getChosenFont());
      Settings.propProgramInfoFont.setFont(mInfoFontPanel.getChosenFont());
      Settings.propChannelNameFont.setFont(mChannelNameFontPanel.getChosenFont());
      Settings.propProgramTimeFont.setFont(mTimeFontPanel.getChosenFont());
      Settings.propUseDefaultFonts.setBoolean(mUseDefaultFontsCB.isSelected());
      Settings.propEnableAntialiasing.setBoolean(mEnableAntialiasingCB.isSelected());
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
      return mLocalizer.msg("Fonts", "Fonts");
    }
    
  
}



  class FontChooser extends JPanel {
   
    /** The localizer for this class. */
         private static final util.ui.Localizer mLocalizer
         = util.ui.Localizer.getLocalizerFor(FontChooser.class);
   
    private java.util.HashSet mSet;
    
    public FontChooser() {
      setLayout(new GridLayout(0,1,0,3));
    
      setBorder(BorderFactory.createTitledBorder(mLocalizer.msg("UserDefinedFonts", "User defined fonts")));
      mSet=new java.util.HashSet();
    }
    
    public void add(FontChooserPanel panel) {
      super.add(panel);
      mSet.add(panel);
    }
    
    public void setEnabled(boolean enabled) {
      super.setEnabled(enabled);
      java.util.Iterator it=mSet.iterator();
      while (it.hasNext()) {
        ((JPanel)it.next()).setEnabled(enabled);
      }
      
    }
    
  }

