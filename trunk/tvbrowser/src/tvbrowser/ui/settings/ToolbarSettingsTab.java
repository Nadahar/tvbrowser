/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (martin@tvbrowser.org)
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


import util.ui.OrderChooser;
import tvbrowser.ui.mainframe.toolbar.ToolBarModel;
import tvbrowser.ui.mainframe.toolbar.DefaultToolBarModel;
import tvbrowser.ui.mainframe.toolbar.ToolBar;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

import tvbrowser.ui.customizableitems.CustomizableItemsPanel;
import tvbrowser.ui.customizableitems.CustomizableItemsListener;
import tvbrowser.ui.mainframe.toolbar.DefaultToolBarModel;
import tvbrowser.core.plugin.PluginProxy;
import tvbrowser.core.Settings;
import devplugin.Program;
import devplugin.Plugin;
import devplugin.ActionMenu;

/**
 * Created by: Martin Oberhauser (martin@tvbrowser.org)
 * Date: 08.01.2005
 * Time: 16:02:53
 */
public class ToolbarSettingsTab implements devplugin.SettingsTab {

  private CustomizableItemsPanel mOrderPanel;

  public ToolbarSettingsTab() {

  }

  public JPanel createSettingsPanel() {
    JPanel panel = new JPanel(new BorderLayout());
    JPanel content = new JPanel();
    content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

    content.add(createOrderPanel());


    panel.add(content, BorderLayout.NORTH);
    return panel;
  }


  private JPanel createOrderPanel() {
    JPanel content = new JPanel();
    final DefaultToolBarModel toolbarModel = DefaultToolBarModel.getInstance();
    //Action[] availableActions = ;
    Action[] currentActions = toolbarModel.getActions();
    ArrayList notSelectedActionsList = new ArrayList(Arrays.asList(toolbarModel.getAvailableActions()));

    for (int i=0; i<currentActions.length; i++) {
      if (notSelectedActionsList.contains(currentActions[i])) {
        notSelectedActionsList.remove(currentActions[i]);
      }
    }
    Action[] availableActions = new Action[notSelectedActionsList.size()];
    notSelectedActionsList.toArray(availableActions);

    mOrderPanel = new CustomizableItemsPanel("Available Buttons","Toolbar buttons");

    mOrderPanel.setCellRenderer(new ActionCellRenderer());
    mOrderPanel.setElementsLeft(availableActions);
    mOrderPanel.setElementsRight(currentActions);

    mOrderPanel.insertElementLeft(0, toolbarModel.getSeparatorAction());

    mOrderPanel.addCustomizableItemsListener(new CustomizableItemsListener(){
      public void leftListSelectionChanged(ListSelectionEvent event) {
      }
      public void rightListSelectionChanged(ListSelectionEvent event) {
      }

      public void itemsTransferredToLeftList(Object[] items) {
        // Remove separator from left list
        Action separator = toolbarModel.getSeparatorAction();
        for (int i=0; i<items.length; i++) {
          Action action = (Action)(items[i]);
          if (separator.equals(action)) {
            mOrderPanel.removeLeft(action);
            mOrderPanel.removeLeft(action);
            mOrderPanel.insertElementLeft(0, separator);
          }
        }
      }

      public void itemsTransferredToRightList(Object[] items) {
        // Add separator to the left list
        for (int i=0; i<items.length; i++) {
          if (toolbarModel.getSeparatorAction().equals(items[i])) {
            mOrderPanel.insertElementLeft(0, toolbarModel.getSeparatorAction());
            return;
          }
        }
      }
    });

    content.add(mOrderPanel);
    return content;
  }

  public void saveSettings() {
    ListModel model = mOrderPanel.getRightList().getModel();
    int size = model.getSize();
    String[] ids = new String[size];
    for (int i=0; i<size; i++) {
      Action action = (Action)model.getElementAt(i);
      ids[i] = (String)action.getValue(ToolBar.ACTION_ID_KEY);
    }
    DefaultToolBarModel.getInstance().setButtonIds(ids);
    Settings.propToolbarButtons.setStringArray(ids);
  }

  public Icon getIcon() {
    return null;
  }

  public String getTitle() {
    return "Toolbar";
  }


  class ActionCellRenderer extends DefaultListCellRenderer {

    public Component getListCellRendererComponent(JList list, Object value,
         int index, boolean isSelected, boolean cellHasFocus) {

         JLabel label = (JLabel) super.getListCellRendererComponent(list, value,
           index, isSelected, cellHasFocus);

         if (value instanceof Action) {
           Action action = (Action)value;
           label.setText((String)action.getValue(Action.NAME));
         }

         return label;
       }



  }


}
