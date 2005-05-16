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
  
 class PluginInfoPanel extends JPanel {
    
   private static final Font PLAIN=new Font("Dialog",Font.PLAIN,12);
   
   private static final util.ui.Localizer mLocalizer
     = util.ui.Localizer.getLocalizerFor(PluginInfoPanel.class);

    
   private JLabel nameLabel;
   private JLabel versionLabel;
   private JLabel authorLabel;
   private JTextArea descriptionArea;
    
   public PluginInfoPanel(devplugin.PluginInfo info) {
      this();
      setPluginInfo(info);
   }
    
   public PluginInfoPanel() {
     setLayout(new BorderLayout(10,0));
      
     String msg;
      
     JPanel leftPanel=new JPanel(new BorderLayout());
     JPanel rightPanel=new JPanel(new BorderLayout());
      
     msg = mLocalizer.msg("name", "Name");
     leftPanel.add(new PluginLabel(msg), BorderLayout.NORTH);
     rightPanel.add(nameLabel=new JLabel("-"),BorderLayout.NORTH);
     nameLabel.setFont(PLAIN);
      
     JPanel panel1=new JPanel(new BorderLayout());
     JPanel panel2=new JPanel(new BorderLayout());
      
     msg = mLocalizer.msg("version", "Version");
     panel1.add(new PluginLabel(msg), BorderLayout.NORTH);
     panel2.add(versionLabel=new JLabel("-"),BorderLayout.NORTH);
     versionLabel.setFont(PLAIN);
      
     JPanel panel3=new JPanel(new BorderLayout());
     JPanel panel4=new JPanel(new BorderLayout());
      
     msg = mLocalizer.msg("author", "Author");
     panel3.add(new PluginLabel(msg), BorderLayout.NORTH);
     panel4.add(authorLabel=new JLabel("-"),BorderLayout.NORTH);
     authorLabel.setFont(PLAIN); 
      
     panel1.add(panel3,BorderLayout.CENTER);
     panel2.add(panel4,BorderLayout.CENTER);
      
     JPanel panel5=new JPanel(new BorderLayout());
     msg = mLocalizer.msg("description", "Description");
     panel5.add(new PluginLabel(msg), BorderLayout.NORTH);
      
     descriptionArea=new JTextArea(3,40);
     descriptionArea.setLineWrap(true);
     descriptionArea.setWrapStyleWord(true);
     descriptionArea.setEditable(false);
     descriptionArea.setOpaque(false);
     descriptionArea.setFont(PLAIN);
     descriptionArea.setBorder(BorderFactory.createEmptyBorder());
     panel3.add(panel5,BorderLayout.CENTER);
     panel4.add(descriptionArea,BorderLayout.CENTER);
      
     leftPanel.add(panel1,BorderLayout.CENTER);
     rightPanel.add(panel2,BorderLayout.CENTER);
      
     add(leftPanel,BorderLayout.WEST);
     add(rightPanel,BorderLayout.CENTER);
   }
    
   public void setDefaultBorder() {
     setBorder(BorderFactory.createTitledBorder(mLocalizer.msg("about","About this DataService:")));
       
   }
    
   public void setPluginInfo(devplugin.PluginInfo info) {
     nameLabel.setText(info.getName());
     
     if (info.getVersion() == null) {
         versionLabel.setText("");
     } else {
         versionLabel.setText(info.getVersion().toString());
     }
     authorLabel.setText(info.getAuthor());
     descriptionArea.setText(info.getDescription());
   }
   
   
   class PluginLabel extends JLabel {
     public PluginLabel(String name) {
       super(name);
       setFont(PLAIN);
     }
   }
 } 
  