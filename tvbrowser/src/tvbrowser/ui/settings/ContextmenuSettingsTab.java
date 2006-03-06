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
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;

import tvbrowser.core.ContextMenuManager;
import tvbrowser.core.Settings;
import tvbrowser.core.plugin.PluginProxy;
import tvbrowser.core.plugin.PluginProxyManager;
import tvbrowser.core.plugin.PluginStateAdapter;
import util.ui.UiUtilities;
import util.ui.customizableitems.SortableItemList;
import devplugin.ActionMenu;
import devplugin.ContextMenuIf;
import devplugin.Plugin;
import devplugin.Program;

public class ContextmenuSettingsTab implements devplugin.SettingsTab, ActionListener {

  class ContextMenuCellRenderer extends DefaultListCellRenderer {

    public Component getListCellRendererComponent(JList list, Object value,
                                                  int index, boolean isSelected, boolean cellHasFocus) {

      JLabel label = (JLabel) super.getListCellRendererComponent(list, value,
          index, isSelected, cellHasFocus);

      if (value instanceof ContextMenuIf) {
        ContextMenuIf menuIf = (ContextMenuIf) value;
        Program exampleProgram = Plugin.getPluginManager().getExampleProgram();

        JPopupMenu menu=new JPopupMenu();

        // Get the context menu item text
        String text = null;
        Icon icon = null;
        //Action[] actionArr = plugin.getContextMenuActions(exampleProgram);
        ActionMenu actionMenu = menuIf.getContextMenuActions(exampleProgram);
        if (actionMenu != null) {
          Action action = actionMenu.getAction();
          if (action != null) {
            text = (String) action.getValue(Action.NAME);
            icon = (Icon) action.getValue(Action.SMALL_ICON);
          }
          else if(menuIf instanceof PluginProxy) {
            
            text = ((PluginProxy)menuIf).getInfo().getName();
            icon = ((PluginProxy)menuIf).getMarkIcon();
          }
          else {
            text = "unknown";
            icon = null;
          }
        }

        Font f;
        /* If the Plugin is the Plugin for double and middle
         * click make the text bold and italic.
         */
        if (menuIf.equals(mDefaultIf) && menuIf.equals(mMiddleClickIf)) {
          f=new Font("Dialog",Font.BOLD + Font.ITALIC,12);
        }
        else if (menuIf.equals(mDefaultIf)) {
          f=new Font("Dialog",Font.BOLD,12);
          text += " - "+mLocalizer.msg("doubleClick","double-click");
        }
        else if (menuIf.equals(mMiddleClickIf)) {
          f=new Font("Dialog",Font.ITALIC,12);
          text += " - "+mLocalizer.msg("middleClick",",middle-click");
        }
        else {
          f=new Font("Dialog",Font.PLAIN,12);
        }
        label.setFont(f);




        label.setText(text);

        label.setBorder(BorderFactory.createEmptyBorder(0,5,0,0));
        label.setOpaque(false);
        label.setBackground(menu.getBackground());
        JPanel panel=new JPanel(new BorderLayout());
        panel.add(label,BorderLayout.CENTER);
        panel.add(new JLabel(icon),BorderLayout.WEST);
        if (isSelected) {
          panel.setBackground(Color.gray);
        }
        return panel;
      }

      return label;
    }



  }

  private JButton mDefaultPluginBt, mMiddleClickPluginBt;
  private ContextMenuIf mDefaultIf, mMiddleClickIf;
  private SortableItemList mList;

  public static final util.ui.Localizer mLocalizer
      = util.ui.Localizer.getLocalizerFor(ContextmenuSettingsTab.class);

  public ContextmenuSettingsTab() {
    mList=new SortableItemList(mLocalizer.msg("title","context menu"));    
    mList.getList().setVisibleRowCount(10);
    mList.getList().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

    mList.getList().addMouseListener(new MouseAdapter(){
      public void mouseClicked(MouseEvent e){
        if(SwingUtilities.isLeftMouseButton(e) && (e.getClickCount() == 2)) {
          int inx = mList.getList().locationToIndex(e.getPoint());
          if (inx>=0) {
            mList.getList().ensureIndexIsVisible(inx);
            mDefaultIf = (ContextMenuIf) mList.getList().getSelectedValue();
            mList.updateUI();
          }
        }
        if(SwingUtilities.isMiddleMouseButton(e) && (e.getClickCount() == 1)) {
          int inx = mList.getList().locationToIndex(e.getPoint());
          if (inx>=0) {
            mList.getList().ensureIndexIsVisible(inx);
            mList.getList().setSelectedIndex(inx);
            mMiddleClickIf = (ContextMenuIf) mList.getList().getSelectedValue();
            mList.updateUI();
          }
        }
      }
    });
    mList.setCellRenderer(new ContextMenuCellRenderer());
    mList.getList().setOpaque(false);
    fillListbox(); 
    
    PluginProxyManager.getInstance().addPluginStateListener(
        new PluginStateAdapter() {
          public void pluginActivated(Plugin p) {
            fillListbox();
          }

          public void pluginDeactivated(Plugin p) {
            fillListbox();
          }
        });
  }


  public JPanel createSettingsPanel() {
    mDefaultIf = ContextMenuManager.getInstance().getDefaultContextMenuIf();
    mMiddleClickIf = ContextMenuManager.getInstance().getMiddleClickIf();

    JPanel contentPanel=new JPanel(new BorderLayout(0,15));
    contentPanel.setBorder(BorderFactory.createEmptyBorder(5,8,5,8));

    JPanel panel1=new JPanel();
    panel1.setLayout(new BoxLayout(panel1,BoxLayout.Y_AXIS));
    panel1.add(mList);


    mDefaultPluginBt=new JButton(mLocalizer.msg("defaultPluginBtn",""));
    mDefaultPluginBt.addActionListener(this);
    JPanel panel2=new JPanel(new BorderLayout());
    panel2.setBorder(BorderFactory.createEmptyBorder(5, 0, 2, 0));
    panel2.add(mDefaultPluginBt,BorderLayout.CENTER);
    panel1.add(panel2);

    mMiddleClickPluginBt=new JButton(mLocalizer.msg("middleClickPluginBtn",""));
    mMiddleClickPluginBt.addActionListener(this);
    JPanel panel3=new JPanel(new BorderLayout());
    panel3.add(mMiddleClickPluginBt,BorderLayout.CENTER);
    panel3.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0));
    panel1.add(panel3);

    contentPanel.add(panel1,BorderLayout.NORTH);

    JTextArea descBox = UiUtilities.createHelpTextArea(mLocalizer.msg("description",""));
    contentPanel.add(descBox,BorderLayout.CENTER);

    return contentPanel;
  }

  private void fillListbox() {
    if (mList==null) {
      return;
    }
    mList.removeAllElements();

    ContextMenuIf[] menuIfList = ContextMenuManager.getInstance().getAvailableContextMenuIfs();
    Program exampleProgram = Plugin.getPluginManager().getExampleProgram();
    for (int i = 0; i < menuIfList.length; i++) {
      ActionMenu actionMenu = menuIfList[i].getContextMenuActions(exampleProgram);
      if (actionMenu != null) {
        mList.addElement(menuIfList[i]);
      }
    }
  }


  public void actionPerformed(ActionEvent event) {
    Object o=event.getSource();
    if (o==mDefaultPluginBt) {
      mDefaultIf = (ContextMenuIf) mList.getList().getSelectedValue();
      mList.updateUI();
    }
    if(o==mMiddleClickPluginBt) {
      mMiddleClickIf = (ContextMenuIf) mList.getList().getSelectedValue();
      mList.updateUI();
    }
  }


  public void saveSettings() {
    Object o[] = mList.getItems();

    ArrayList pluginIDsList = new ArrayList();
    String[] orderIDs = new String[o.length];
    for (int i = 0; i < o.length; i++) {
      ContextMenuIf menuIf = (ContextMenuIf) o[i];
      orderIDs[i] = menuIf.getId();
      if(menuIf instanceof PluginProxy)
        pluginIDsList.add(menuIf.getId());
    }
    
    String[] pluginIDs = new String[pluginIDsList.size()];
    pluginIDsList.toArray(pluginIDs);
           
    Settings.propContextMenuOrder.setStringArray(orderIDs);
    Settings.propPluginOrder.setStringArray(pluginIDs);

    PluginProxyManager.getInstance().setPluginOrder(pluginIDs);

    if (!mList.contains(mDefaultIf)) {
      mDefaultIf = null;
    }
    if (!mList.contains(mMiddleClickIf)) {
      mMiddleClickIf = null;
    }

    ContextMenuManager.getInstance().setDefaultContextMenuIf(mDefaultIf);
    if (mDefaultIf != null) {
      Settings.propDefaultContextMenuIf.setString(mDefaultIf.getId());
    } else {
      Settings.propDefaultContextMenuIf.setString(null);
    }

    ContextMenuManager.getInstance().setMiddleClickIf(mMiddleClickIf);
    if (mMiddleClickIf != null) {
      Settings.propMiddleClickIf.setString(mMiddleClickIf.getId());
    } else {
      Settings.propMiddleClickIf.setString(null);
    }
  }

  
  public Icon getIcon() {
    return null;
  }

  
  public String getTitle() {
    return mLocalizer.msg("title", "context menu");
  }
}