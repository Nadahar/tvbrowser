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
import java.io.File;
import java.util.HashSet;
import java.util.Iterator;

import javax.swing.*;

import tvbrowser.core.Settings;
import devplugin.SettingsTab;

public class DirectoriesSettingsTab implements SettingsTab {
  
  /** The localizer for this class. */
    private static final util.ui.Localizer mLocalizer
    = util.ui.Localizer.getLocalizerFor(DirectoriesSettingsTab.class);
 
  
  private JCheckBox mUseDefaultFolderCB;
  private DirectoryChooserPanel mTVDataFolderPanel; 
    
  public DirectoriesSettingsTab() {
    
  }
    /**
     * Creates the settings panel for this tab.
     */
  public JPanel createSettingsPanel() {
    String msg;
    
    JPanel mainPanel=new JPanel(new BorderLayout());
    mainPanel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
    JPanel content=new JPanel();
    content.setLayout(new BoxLayout(content,BoxLayout.Y_AXIS));
    
    JPanel checkBoxPanel=new JPanel(new BorderLayout());
    checkBoxPanel.setBorder(BorderFactory.createEmptyBorder(0,0,3,0));
    
    mUseDefaultFolderCB=new JCheckBox(mLocalizer.msg("defaultfolders", "Use default folders"));
    mUseDefaultFolderCB.setSelected(Settings.propUseDefaultDirectories.getBoolean());
    
    checkBoxPanel.add(mUseDefaultFolderCB);
    
    content.add(checkBoxPanel);
    
    final DirectoryChooser directoriesPanel=new DirectoryChooser();

    msg = mLocalizer.msg("tvdatadir", "tv data folder");
    String tvDataDir = Settings.propTVDataDirectory.getString();    
    mTVDataFolderPanel=new DirectoryChooserPanel(msg, tvDataDir);
    directoriesPanel.add(mTVDataFolderPanel);
    directoriesPanel.setEnabled(!mUseDefaultFolderCB.isSelected());

    content.add(directoriesPanel);
    mainPanel.add(content,BorderLayout.NORTH);
    
    
    
    mUseDefaultFolderCB.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        directoriesPanel.setEnabled(!mUseDefaultFolderCB.isSelected());
      }
    });
    
    
    return mainPanel;
  }
  
    /**
     * Called by the host-application, if the user wants to save the settings.
     */
  public void saveSettings() {
    if (mUseDefaultFolderCB.isSelected()) {
      String defaultDir = Settings.propTVDataDirectory.getDefault();
      Settings.propTVDataDirectory.setString(defaultDir);
    }
    else {  
      Settings.propTVDataDirectory.setString(mTVDataFolderPanel.getText());
    }
    Settings.propUseDefaultDirectories.setBoolean(mUseDefaultFolderCB.isSelected());
  }

  
    /**
     * Returns the name of the tab-sheet.
     */
  public Icon getIcon() {
    return new ImageIcon("imgs/Open16.gif");
  }
  
  
    /**
     * Returns the title of the tab-sheet.
     */
  public String getTitle() {

  return mLocalizer.msg("directories", "Directories");
 }

  



class DirectoryChooserPanel extends JPanel {
 
  private JTextField mTextField;
  private JButton mBtn;
  private JLabel mLabel;
  
  public DirectoryChooserPanel(String title, String text) {
    
    setLayout(new BorderLayout(7,0));
    mLabel=new JLabel(title);
    mLabel.setBorder(BorderFactory.createEmptyBorder(0,13,0,0));
    add(mLabel,BorderLayout.WEST);
    
    mTextField=new JTextField(text);
    add(mTextField,BorderLayout.CENTER);
    
    mBtn=new JButton(mLocalizer.msg("change","change"));
    mBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        JFileChooser fc =new JFileChooser();
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fc.setApproveButtonText("Ok");
        fc.setCurrentDirectory(new File(mTextField.getText()));
        int retVal=fc.showOpenDialog(getParent());
        if (retVal==JFileChooser.APPROVE_OPTION) {
          File f=fc.getSelectedFile();
          mTextField.setText(f.getAbsolutePath());
        }
      }
    });
    
    add(mBtn,BorderLayout.EAST); 
     
  }
  
  public void setEnabled(boolean enabled) {
    super.setEnabled(enabled);
    mTextField.setEnabled(enabled);
    mBtn.setEnabled(enabled);  
    mLabel.setEnabled(enabled);
  }
  
  public String getText() {
    return mTextField.getText();
  }
}
}
  class DirectoryChooser extends JPanel {
   
    /** The localizer for this class. */
       private static final util.ui.Localizer mLocalizer
       = util.ui.Localizer.getLocalizerFor(DirectoryChooser.class);
 
   
    private HashSet mSet;
    
    public DirectoryChooser() {
      setLayout(new GridLayout(0,1,0,3));
    
      setBorder(BorderFactory.createTitledBorder(mLocalizer.msg("UserDefinedFolders", "User defined folders")));
      mSet=new HashSet();
    }
    
    public void add(DirectoryChooserPanel panel) {
      super.add(panel);
      mSet.add(panel);
    }
    
    public void setEnabled(boolean enabled) {
      super.setEnabled(enabled);
      Iterator it=mSet.iterator();
      while (it.hasNext()) {
        ((JPanel)it.next()).setEnabled(enabled);
      }
      
    }
    
  }
