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

package util.ui.customizableitems;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
//import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
//import java.awt.ScrollPane;
//import java.net.URL;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
//import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
//import javax.swing.JScrollPane;
//import javax.swing.JTextArea;
import javax.swing.ListCellRenderer;
//import javax.swing.event.HyperlinkEvent;
//import javax.swing.event.HyperlinkListener;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

import tvbrowser.core.Settings;
import tvbrowser.core.icontheme.IconLoader;
import tvbrowser.ui.update.SoftwareUpdateItem;
//import util.browserlauncher.Launch;
import util.ui.Localizer;
import util.ui.TextAreaIcon;
import util.ui.UiUtilities;
import util.ui.customizableitems.SelectableItemList.MyListUI;
//import util.ui.html.ExtendedHTMLEditorKit;
import util.ui.html.HTMLTextHelper;
import devplugin.Channel;
import devplugin.Version;

/**
 * A ListCellRenderer for SelectableItems.
 * 
 * @author René Mach
 */
public class SelectableItemRenderer implements ListCellRenderer {
  private static final ImageIcon NEW_VERSION_ICON = IconLoader.getInstance().getIconFromTheme("status", "software-update-available", 16);
  private static final Localizer mLocalizer = Localizer.getLocalizerFor(SelectableItemRenderer.class);
  private int mSelectionWidth;
  private boolean mIsEnabled = true;
  
  public Component getListCellRendererComponent(JList list, Object value,
  int index, boolean isSelected, boolean cellHasFocus) {
    JPanel p = new JPanel(new BorderLayout(2,0));
    p.setBorder(BorderFactory.createEmptyBorder(0,2,0,0));
    
    SelectableItem selectableItem = (SelectableItem) value;

    JCheckBox cb = new JCheckBox("",selectableItem.isSelected());
    mSelectionWidth = cb.getPreferredSize().width;
    
    cb.setOpaque(false);
    
    p.add(cb, BorderLayout.WEST);
    
    if(selectableItem.getItem() instanceof Channel) {
      JLabel l = new JLabel();
      
      if(Settings.propShowChannelNamesInChannellist.getBoolean()) {
        l.setText(selectableItem.getItem().toString());
      }
      
      if(!mIsEnabled)
        l.setEnabled(false);
      
      l.setOpaque(false);
      
      if(Settings.propShowChannelIconsInChannellist.getBoolean()) {
        l.setIcon(UiUtilities.createChannelIcon(((Channel)selectableItem.getItem()).getIcon()));
      }
      
      p.add(l, BorderLayout.CENTER);
      
      if(isSelected && mIsEnabled)
        l.setForeground(list.getSelectionForeground());
      else
        l.setForeground(list.getForeground());
    } else if(selectableItem.getItem() instanceof SoftwareUpdateItem) {
      cb.setVerticalAlignment(JCheckBox.TOP);
      cb.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(3,3,0,0),cb.getBorder()));
      
      CellConstraints cc = new CellConstraints();
      FormLayout layout = new FormLayout("5dlu,default,5dlu,default:grow","2dlu,default,2dlu,fill:pref:grow,2dlu");
      PanelBuilder pb = new PanelBuilder(layout);
      pb.getPanel().setOpaque(false);
      
      SoftwareUpdateItem item = (SoftwareUpdateItem)selectableItem.getItem();
      
      JLabel label = pb.addLabel(HTMLTextHelper.convertHtmlToText(item.getName()) + " " + item.getVersion(), cc.xy(2,2));
      label.setFont(label.getFont().deriveFont(Font.BOLD, label.getFont().getSize2D()+2));
      
      JLabel label3 = new JLabel();
      
      Version installedVersion = item.getInstalledVersion();
      if ((installedVersion != null) && (installedVersion.compareTo(item.getVersion()) < 0)) { 
        label.setIcon(NEW_VERSION_ICON);
        
        label3.setText("(" + mLocalizer.msg("installedVersion","Installed version: ") + installedVersion.toString()+")");
        label3.setFont(label3.getFont().deriveFont((float)label3.getFont().getSize2D()+2));
        
        pb.add(label3, cc.xy(4,2));
      }
      
      if (isSelected && mIsEnabled) {
        label.setForeground(list.getSelectionForeground());
        
        /*JTextArea text = new JTextArea(HTMLTextHelper.convertHtmlToText(item.getDescription()));
        text.setEditable(false);
        text.setLineWrap(true);*/
        
        
        
        
        /*JEditorPane mDescriptionPane = new JEditorPane();

        mDescriptionPane.setEditorKit(new ExtendedHTMLEditorKit());
        mDescriptionPane.setEditable(false);
        
        mDescriptionPane.addHyperlinkListener(new HyperlinkListener() {
          public void hyperlinkUpdate(HyperlinkEvent evt) {
            if (evt.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
              URL url = evt.getURL();
              if (url != null) {
                Launch.openURL(url.toString());
              }
            }
          }
        });
        */
       // StringBuffer content = new StringBuffer();
        String author = item.getProperty("author");
        String website = item.getWebsite();

      /*  content.append(
            "<html><div style=\"color:#").append(Integer.toHexString(list.getSelectionForeground().getRed())).append(Integer.toHexString(list.getSelectionForeground().getGreen())).append(Integer.toHexString(list.getSelectionForeground().getBlue())).append(";font-family:").append(new JLabel().getFont().getName()).append(";font-size:").append(new JLabel().getFont().getSize()).append(";\">").append("<p>").append(item.getDescription()).append("</p><html>");*/
        
        if (author != null) {
          layout.appendRow(new RowSpec("2dlu"));
          layout.appendRow(new RowSpec("default"));
          layout.appendRow(new RowSpec("2dlu"));
          
          JLabel autor = pb.addLabel("Autor: ", cc.xy(2,7));
          autor.setFont(autor.getFont().deriveFont(Font.BOLD));
          autor.setForeground(list.getSelectionForeground());
          
          pb.addLabel(HTMLTextHelper.convertHtmlToText(author), cc.xy(4,7)).setForeground(list.getSelectionForeground());
          
          //content.append("<tr><th>").append(/*mLocalizer.msg("author", "Author")*/"Autor: ").append("</th><td>").append(author)
            //  .append("</td></tr>");
        }
        
        if (website != null) {
          if(author == null) {
            layout.appendRow(new RowSpec("2dlu"));
          }
          
          layout.appendRow(new RowSpec("default"));
          layout.appendRow(new RowSpec("2dlu"));
          
          JLabel webs = pb.addLabel("Website: ", cc.xy(2,author == null ? 7 : 9));
          webs.setFont(webs.getFont().deriveFont(Font.BOLD));
          webs.setForeground(list.getSelectionForeground());
          
          pb.addLabel(website, cc.xy(4,author == null ? 7 : 9)).setForeground(list.getSelectionForeground());
         // content.append("<tr><th>").append(/*mLocalizer.msg("website", "Website")*/"Website: ").append("</th><td><a href=\"").append(
         //     website).append("\">").append(website).append("</a></td></tr>");
        }
        
        TextAreaIcon icon = new TextAreaIcon(HTMLTextHelper.convertHtmlToText(item.getDescription()), new JLabel().getFont(),list.getPreferredScrollableViewportSize().width - 15, 2);
        
        JLabel iconLabel = new JLabel("");
        iconLabel.setForeground(list.getSelectionForeground());
        iconLabel.setIcon(icon);
        
        pb.add(iconLabel, cc.xyw(2,4,3));
        
        
        label3.setForeground(list.getSelectionForeground());
      } else {
        if(!item.isStable()) {
          label.setForeground(new Color(200, 0, 0));
        }
        else {
          label.setForeground(list.getForeground());
        }

        JLabel label2 = pb.addLabel(HTMLTextHelper.convertHtmlToText(item.getDescription().length() > 100 ? item.getDescription().substring(0,100) + "..." : item.getDescription()), cc.xyw(2,4,3));
        
        label2.setForeground(list.getForeground());        
        label3.setForeground(Color.gray);
      }
      
      p.add(pb.getPanel(), BorderLayout.CENTER);
    } else {
      cb.setText(selectableItem.getItem().toString());
    }
    
    ((MyListUI)list.getUI()).setCellHeight(index, p.getPreferredSize().height);
    
    if (isSelected && mIsEnabled) {
      p.setOpaque(true);
      p.setBackground(list.getSelectionBackground());
      cb.setForeground(list.getSelectionForeground());
      
    } else {
      p.setOpaque(false);
      p.setForeground(list.getForeground());
      cb.setForeground(list.getForeground());
    }
    cb.setEnabled(list.isEnabled());

    return p;
  }
  
  /**
   * @param value If the renderer should be enabled
   */
  public void setEnabled(boolean value) {
    mIsEnabled = value;
  }
  
  /**
   * @return The selection width.
   */
  public int getSelectionWidth() {
    return mSelectionWidth;
  }

}