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
import java.awt.Color;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextArea;

import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.factories.DefaultComponentFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.Sizes;
  
 class PluginInfoPanel extends JPanel {
    
   private static final Font PLAIN=new Font("Dialog",Font.PLAIN,12);
   
   private static final util.ui.Localizer mLocalizer
     = util.ui.Localizer.getLocalizerFor(PluginInfoPanel.class);
    
   private JLabel nameLabel;
   private JLabel versionLabel;
   private JLabel authorLabel;
   private JLabel descriptionLabel;
    
   public PluginInfoPanel(devplugin.PluginInfo info) {
      this();
      setPluginInfo(info);
   }
    
   public PluginInfoPanel() {
     setLayout(new FormLayout("5dlu,pref,10dlu,default:grow,5dlu","pref,5dlu,top:pref,top:pref,top:pref,top:pref"));
     CellConstraints cc = new CellConstraints();
      
     add(new PluginLabel(mLocalizer.msg("name", "Name")), cc.xy(2,3));
     add(nameLabel=new JLabel("-"), cc.xy(4,3));
     nameLabel.setFont(PLAIN);
     
     add(new PluginLabel(mLocalizer.msg("version", "Version")), cc.xy(2,4));
     add(versionLabel=new JLabel("-"), cc.xy(4,4));
     versionLabel.setFont(PLAIN);
     
     add(new PluginLabel(mLocalizer.msg("author", "Author")), cc.xy(2,5));
     add(authorLabel=new JLabel("-"), cc.xy(4,5));
     authorLabel.setFont(PLAIN); 
      
     add(new PluginLabel(mLocalizer.msg("description", "Description")), cc.xy(2,6));
     add(descriptionLabel=new JLabel(), cc.xy(4,6));
     descriptionLabel.setFont(PLAIN);
   }
    
   public void setDefaultBorder(boolean plugin) {
     CellConstraints cc = new CellConstraints();
     
     setBorder(Borders.createEmptyBorder(Sizes.dluY(0),Sizes.dluY(0),Sizes.DLUY11,Sizes.dluY(0)));
     
     if(plugin)
       add(DefaultComponentFactory.getInstance().createSeparator(mLocalizer.msg("about","About this Plugin:")), cc.xyw(1,1,5));
     else
       add(DefaultComponentFactory.getInstance().createSeparator(mLocalizer.msg("aboutDataService","About this DataService:")), cc.xyw(1,1,5));
   }
    
   public void setPluginInfo(devplugin.PluginInfo info) {
     nameLabel.setText(info.getName());
     
     if (info.getVersion() == null)
       versionLabel.setText("");
     else
       versionLabel.setText("<html>" + info.getVersion().toString() + "</html>");
     
     authorLabel.setText("<html>" + info.getAuthor() + "</html>");
     descriptionLabel.setText("<html>" + info.getDescription() + "</html>");
   }
   
   
   class PluginLabel extends JLabel {
     public PluginLabel(String name) {
       super(name);
       setFont(PLAIN);
     }
   }
 } 
  