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

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

import tvbrowser.core.Settings;

public class FontsSettingsTab implements devplugin.SettingsTab {
 
  /** The localizer for this class. */
     private static final util.ui.Localizer mLocalizer
     = util.ui.Localizer.getLocalizerFor(FontsSettingsTab.class);
 
  private JCheckBox mUseDefaultFontsCB; 
  
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
    
    checkBoxPanel.add(mUseDefaultFontsCB);
    
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
      Settings.propProgramTitleFont.setFont(mTitleFontPanel.getChoosenFont());
      Settings.propProgramInfoFont.setFont(mInfoFontPanel.getChoosenFont());  
      Settings.propChannelNameFont.setFont(mChannelNameFontPanel.getChoosenFont());
      Settings.propProgramTimeFont.setFont(mTimeFontPanel.getChoosenFont());
      Settings.propUseDefaultFonts.setBoolean(mUseDefaultFontsCB.isSelected());
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


class FontChooserPanel extends JPanel {
 
  /** The localizer for this class. */
       private static final util.ui.Localizer mLocalizer
       = util.ui.Localizer.getLocalizerFor(FontChooserPanel.class);
       
  private JComboBox mFontCB, mStyleCB, mSizeCB;
  private JLabel mTitle;
  
    private static java.awt.GraphicsEnvironment ge =
      java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment();
    private static final String[] FONTNAMES = ge.getAvailableFontFamilyNames();
  
    private static final Integer[] FONTSIZES=new Integer[12];
    
    private static final String[] FONTSTYLES={
                mLocalizer.msg("plain", "plain"),
                mLocalizer.msg("bold", "bold"),
                mLocalizer.msg("italic", "italic")};
    
    {
      for (int i=0;i<FONTSIZES.length;i++) {
        FONTSIZES[i]=new Integer(i+8);
      }
    }
  
  public FontChooserPanel(String title, Font font) {
    
    setLayout(new BorderLayout());
    mTitle=new JLabel(title);
    add(mTitle,BorderLayout.NORTH);
    JPanel panel1=new JPanel(new FlowLayout());
    
    mFontCB=new JComboBox(FONTNAMES);
    mStyleCB=new JComboBox(FONTSTYLES);
    mSizeCB=new JComboBox(FONTSIZES);
    
    for (int i=0;i<mFontCB.getItemCount();i++) {
      String item=(String)mFontCB.getItemAt(i);
      if (item.equals(font.getName())) {
        mFontCB.setSelectedIndex(i);
        break;
      }
    }
    
    for (int i=0;i<mSizeCB.getItemCount();i++) {
      Integer item=(Integer)mSizeCB.getItemAt(i);
      if (item.intValue()==font.getSize()) {
        mSizeCB.setSelectedIndex(i);
      }
    }
    
    if (font.getStyle()==Font.BOLD) {
      mStyleCB.setSelectedIndex(1);
    }else if (font.getStyle()==Font.ITALIC) {
      mStyleCB.setSelectedIndex(2);
    }
    
    panel1.add(mFontCB);
    panel1.add(mStyleCB);
    panel1.add(mSizeCB);
    
    add(panel1,BorderLayout.CENTER);
     
  }
  
  public void setEnabled(boolean enabled) {
    super.setEnabled(enabled);
    mFontCB.setEnabled(enabled);
    mStyleCB.setEnabled(enabled);
    mSizeCB.setEnabled(enabled);
    mTitle.setEnabled(enabled);
    
  }
  
  public Font getChoosenFont() {
    Font result;
    int style;
    int inx=mStyleCB.getSelectedIndex();
    if (inx==0) {
      style=Font.PLAIN;
    }else if (inx==1) {
      style=Font.BOLD;
    }else {
      style=Font.ITALIC;
    }
    result=new Font(
      (String)mFontCB.getSelectedItem(),
      style,
      ((Integer)mSizeCB.getSelectedItem()).intValue()
    );
    
    
    return result;
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



/*
class FontChooser extends JPanel {
  
  private JComboBox mFontCB, mStyleCB, mSizeCB;
  
  private static java.awt.GraphicsEnvironment ge =
    java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment();
  private static final String[] FONTNAMES = ge.getAvailableFontFamilyNames();
  
  
  
  
  public FontChooser(String title) {
    super();
    setLayout(new BorderLayout());
    add(new JLabel(title),BorderLayout.WEST);
    JPanel panel1=new JPanel(new FlowLayout());
    
    mFontCB=new JComboBox(FONTNAMES);
    mStyleCB=new JComboBox();
    mSizeCB=new JComboBox();
    
    panel1.add(mFontCB);
    panel1.add(mStyleCB);
    panel1.add(mSizeCB);
    
    add(panel1,BorderLayout.EAST);
  }
  
}
*/


