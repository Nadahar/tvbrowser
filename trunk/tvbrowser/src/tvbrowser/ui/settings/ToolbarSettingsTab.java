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


import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.ArrayList;

import tvbrowser.ui.customizableitems.CustomizableItemsPanel;
import tvbrowser.ui.customizableitems.CustomizableItemsListener;
import tvbrowser.ui.mainframe.toolbar.DefaultToolBarModel;
import tvbrowser.ui.mainframe.toolbar.*;
import tvbrowser.ui.mainframe.MainFrame;
import tvbrowser.core.Settings;


/**
 * Created by: Martin Oberhauser (martin@tvbrowser.org)
 * Date: 08.01.2005
 * Time: 16:02:53
 */
public class ToolbarSettingsTab implements devplugin.SettingsTab {

  public static final util.ui.Localizer mLocalizer
       = util.ui.Localizer.getLocalizerFor(ToolbarSettingsTab.class);


  private CustomizableItemsPanel mOrderPanel;
  private JComboBox mShowCB, mLocationCB;
  private JCheckBox mUseBigIconsCb, mShowToolbarCb;
  private JLabel mLocationLb, mIconsLb;

  public ToolbarSettingsTab() {

  }


  private JPanel createLeftAlignmentPanel(JPanel content) {
    JPanel result = new JPanel(new BorderLayout());
    result.add(content,BorderLayout.WEST);
    return result;
  }

  public JPanel createSettingsPanel() {
    JPanel panel = new JPanel(new BorderLayout());
    panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

    JPanel content = new JPanel();
    content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

    mShowToolbarCb = new JCheckBox(mLocalizer.msg("showToolbar","Show toolbar"));
    JPanel cbPanel = new JPanel(new BorderLayout());
    cbPanel.add(mShowToolbarCb);
    content.add(cbPanel);

    mShowToolbarCb.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        enableContent(mShowToolbarCb.isSelected());
      }
    });



    JPanel orderPn = createOrderPanel();
    JPanel pn1 = createLeftAlignmentPanel(orderPn);
    pn1.setBorder(BorderFactory.createTitledBorder(""));
    content.add(pn1);

    JPanel pn = new JPanel(new GridLayout(3,2));

    pn.add(mLocationLb = new JLabel(mLocalizer.msg("location","Location")));
    mLocationCB = new JComboBox(new String[]{
      mLocalizer.msg("top","top"),
      mLocalizer.msg("left","left"),
    });
    pn.add(mLocationCB);

    String location = Settings.propToolbarLocation.getString();
    if ("west".equals(location)) {
      mLocationCB.setSelectedIndex(1);
    }


    pn.add(mIconsLb = new JLabel(mLocalizer.msg("icons","Icons")));
    mShowCB = new JComboBox(new String[]{
      ContextMenu.mLocalizer.msg("text.and.icon","text and icon"),
      ContextMenu.mLocalizer.msg("text","text"),
      ContextMenu.mLocalizer.msg("icon","icon")
    });

    String style = Settings.propToolbarButtonStyle.getString();
    if ("text".equals(style)) {
      mShowCB.setSelectedIndex(1);
    }
    else if ("icon".equals(style)) {
      mShowCB.setSelectedIndex(2);
    }

    pn.add(mShowCB);
    pn.add(new JPanel());
    mUseBigIconsCb = new JCheckBox(ContextMenu.mLocalizer.msg("bigIcons","Use big icons"));
    mUseBigIconsCb.setSelected(Settings.propToolbarUseBigIcons.getBoolean());

    pn.add(mUseBigIconsCb);

    JPanel pn2 = createLeftAlignmentPanel(pn);
    pn2.setBorder(BorderFactory.createTitledBorder(""));
    content.add(pn2);

    panel.add(content, BorderLayout.NORTH);

    boolean toolbarIsVisible = Settings.propIsTooolbarVisible.getBoolean();
    mShowToolbarCb.setSelected(toolbarIsVisible);
    enableContent(toolbarIsVisible);

    return panel;
  }


  private void enableContent(boolean enabled) {
    mOrderPanel.setEnabled(enabled);
    mShowCB.setEnabled(enabled);
    mLocationCB.setEnabled(enabled);
    mUseBigIconsCb.setEnabled(enabled);
    mLocationLb.setEnabled(enabled);
    mIconsLb.setEnabled(enabled);
  }

  private JPanel createOrderPanel() {
    JPanel content = new JPanel();
    final DefaultToolBarModel toolbarModel = DefaultToolBarModel.getInstance();
    Action[] currentActions = toolbarModel.getActions();
    ArrayList notSelectedActionsList = new ArrayList(Arrays.asList(toolbarModel.getAvailableActions()));

    for (int i=0; i<currentActions.length; i++) {
      if (notSelectedActionsList.contains(currentActions[i])) {
        notSelectedActionsList.remove(currentActions[i]);
      }
    }
    Action[] availableActions = new Action[notSelectedActionsList.size()];
    notSelectedActionsList.toArray(availableActions);

    mOrderPanel = new CustomizableItemsPanel(
        mLocalizer.msg("buttonsAvailable","Available Buttons"),
        mLocalizer.msg("buttonsToolbar","Toolbar buttons"));

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

    ToolBar toolbar = MainFrame.getInstance().getToolbar();
    int inx = mShowCB.getSelectedIndex();
    if (inx == 0) {
      toolbar.setStyle(ToolBar.STYLE_ICON|ToolBar.STYLE_TEXT);
    }
    else if (inx == 1) {
      toolbar.setStyle(ToolBar.STYLE_TEXT);
    }
    else if (inx == 2) {
      toolbar.setStyle(ToolBar.STYLE_ICON);
    }


    if (mLocationCB.getSelectedIndex() == 1) {
     toolbar.setToolbarLocation(BorderLayout.WEST);
    }
    else{
      toolbar.setToolbarLocation(BorderLayout.NORTH);
    }

    Settings.propIsTooolbarVisible.setBoolean(mShowToolbarCb.isSelected());

    toolbar.setUseBigIcons(mUseBigIconsCb.isSelected());
    toolbar.storeSettings();
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
