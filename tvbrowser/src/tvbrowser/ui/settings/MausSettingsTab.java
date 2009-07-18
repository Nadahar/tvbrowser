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

import java.awt.Component;

import javax.swing.Action;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;

import tvbrowser.core.Settings;
import tvbrowser.core.contextmenu.ConfigMenuItem;
import tvbrowser.core.contextmenu.ContextMenuManager;
import tvbrowser.core.contextmenu.DoNothingContextMenuItem;
import tvbrowser.core.contextmenu.LeaveFullScreenMenuItem;
import tvbrowser.core.contextmenu.SeparatorMenuItem;
import tvbrowser.core.icontheme.IconLoader;
import tvbrowser.core.plugin.PluginProxy;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import devplugin.ActionMenu;
import devplugin.ContextMenuIf;
import devplugin.Plugin;
import devplugin.Program;

public class MausSettingsTab implements devplugin.SettingsTab {

  private ContextMenuIf mLeftSingleClickIf, mDoubleClickIf, mMiddleClickIf;

  private static final util.ui.Localizer mLocalizer = util.ui.Localizer
      .getLocalizerFor(MausSettingsTab.class);

  private JComboBox mLeftSingleClickBox;
  
  private JComboBox mDoubleClickBox;

  private JComboBox mMiddleClickBox;

  public JPanel createSettingsPanel() {
    mLeftSingleClickIf = ContextMenuManager.getInstance().getLeftSingleClickIf();
    mDoubleClickIf = ContextMenuManager.getInstance().getDefaultContextMenuIf();
    mMiddleClickIf = ContextMenuManager.getInstance().getMiddleClickIf();

    PanelBuilder contentPanel = new PanelBuilder(new FormLayout("5dlu, pref, 3dlu, pref, fill:pref:grow, 3dlu",
        "pref, 5dlu, pref, 3dlu, pref, 3dlu, pref, 3dlu, pref"));
    contentPanel.setBorder(Borders.DIALOG_BORDER);

    CellConstraints cc = new CellConstraints();
    contentPanel.addSeparator(mLocalizer.msg("title", "Title"), cc.xyw(1,
        1, 6));    
    
    contentPanel.add(new JLabel(mLocalizer.msg("MouseButtons", "Mouse Buttons:")), cc.xyw(2, 3, 4));
    
    contentPanel.add(new JLabel(mLocalizer.msg("leftSingleClickLabel", "Left Single Click")), cc.xy(2, 5));
    
    mLeftSingleClickBox = new JComboBox();
    mLeftSingleClickBox.setSelectedItem(mLeftSingleClickIf);
    mLeftSingleClickBox.setMaximumRowCount(15);
    
    mLeftSingleClickBox.setRenderer(new ContextMenuCellRenderer());
    contentPanel.add(mLeftSingleClickBox, cc.xy(4, 5));
    
    contentPanel.add(new JLabel(mLocalizer.msg("doubleClickLabel", "Double Click")), cc.xy(2, 7));
    
    mDoubleClickBox = new JComboBox();
    mDoubleClickBox.setSelectedItem(mDoubleClickIf);
    mDoubleClickBox.setMaximumRowCount(15);
    
    mDoubleClickBox.setRenderer(new ContextMenuCellRenderer());
    contentPanel.add(mDoubleClickBox, cc.xy(4, 7));

    contentPanel.add(new JLabel(mLocalizer.msg("middleClickLabel", "Middle Click")), cc.xy(2, 9));
    mMiddleClickBox = new JComboBox();
    mMiddleClickBox.setSelectedItem(mMiddleClickIf);
    mMiddleClickBox.setMaximumRowCount(15);
    
    mMiddleClickBox.setRenderer(new ContextMenuCellRenderer());
    contentPanel.add(mMiddleClickBox, cc.xy(4, 9));

    fillListbox();

    return contentPanel.getPanel();
  }

  private void fillListbox() {
    mLeftSingleClickBox.removeAllItems();
    mDoubleClickBox.removeAllItems();
    mMiddleClickBox.removeAllItems();

    mLeftSingleClickBox.addItem(DoNothingContextMenuItem.getInstance());
    mDoubleClickBox.addItem(DoNothingContextMenuItem.getInstance());
    mMiddleClickBox.addItem(DoNothingContextMenuItem.getInstance());
    
    ContextMenuIf[] menuIfList = ContextMenuManager.getInstance().getAvailableContextMenuIfs(true, false);
    Program exampleProgram = Plugin.getPluginManager().getExampleProgram();
    for (int i = 0; i < menuIfList.length; i++) {
      if (menuIfList[i] instanceof SeparatorMenuItem) {
      } else if (menuIfList[i] instanceof ConfigMenuItem || menuIfList[i] instanceof LeaveFullScreenMenuItem) {
      } else {
        ActionMenu actionMenu = menuIfList[i].getContextMenuActions(exampleProgram);
        if (actionMenu != null) {
          mLeftSingleClickBox.addItem(menuIfList[i]);
          mDoubleClickBox.addItem(menuIfList[i]);
          mMiddleClickBox.addItem(menuIfList[i]);
        }
      }
    }
    
    mLeftSingleClickBox.setSelectedItem(mLeftSingleClickIf);
    mDoubleClickBox.setSelectedItem(mDoubleClickIf);
    mMiddleClickBox.setSelectedItem(mMiddleClickIf);
  }

  public void saveSettings() {
    mLeftSingleClickIf = (ContextMenuIf) mLeftSingleClickBox.getSelectedItem();
    mDoubleClickIf = (ContextMenuIf) mDoubleClickBox.getSelectedItem();
    mMiddleClickIf = (ContextMenuIf) mMiddleClickBox.getSelectedItem();

    ContextMenuManager.getInstance().setLeftSingleClickIf(mLeftSingleClickIf);
    if (mLeftSingleClickIf != null) {
      Settings.propLeftSingleClickIf.setString(mLeftSingleClickIf.getId());
    } else {
      Settings.propLeftSingleClickIf.setString(null);
    }
    
    ContextMenuManager.getInstance().setDefaultContextMenuIf(mDoubleClickIf);
    if (mDoubleClickIf != null) {
      Settings.propDoubleClickIf.setString(mDoubleClickIf.getId());
    } else {
      Settings.propDoubleClickIf.setString(null);
    }

    ContextMenuManager.getInstance().setMiddleClickIf(mMiddleClickIf);
    if (mMiddleClickIf != null) {
      Settings.propMiddleClickIf.setString(mMiddleClickIf.getId());
    } else {
      Settings.propMiddleClickIf.setString(null);
    }
  }

  public Icon getIcon() {
    return IconLoader.getInstance().getIconFromTheme("devices", "input-mouse", 16);
  }

  public String getTitle() {
    return mLocalizer.msg("title", "context menu");
  }
  
  private static class ContextMenuCellRenderer extends DefaultListCellRenderer {
    private JLabel mItemLabel;
    
    public ContextMenuCellRenderer() {
      mItemLabel = new JLabel();
    }

    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
        boolean cellHasFocus) {

      JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

      if (value instanceof ContextMenuIf) {
        ContextMenuIf menuIf = (ContextMenuIf) value;
        Program exampleProgram = Plugin.getPluginManager().getExampleProgram();

        // Get the context menu item text
        StringBuilder text = new StringBuilder();
        Icon icon = null;
        // Action[] actionArr = plugin.getContextMenuActions(exampleProgram);
        ActionMenu actionMenu = menuIf.getContextMenuActions(exampleProgram);
        if (actionMenu != null) {
          Action action = actionMenu.getAction();
          if (action != null) {
            text.append((String) action.getValue(Action.NAME));
            icon = (Icon) action.getValue(Action.SMALL_ICON);
          } else if (menuIf instanceof PluginProxy) {
            text.append(((PluginProxy) menuIf).getInfo().getName());
            icon = ((PluginProxy) menuIf).getMarkIcon();
          } else {
            text.append("unknown");
            icon = null;
          }
        }

        mItemLabel.setIcon(icon);
        mItemLabel.setForeground(label.getForeground());
        mItemLabel.setBackground(label.getBackground());
        mItemLabel.setText(text.toString());
        mItemLabel.setOpaque(label.isOpaque());

        return mItemLabel;
      }

      return label;
    }

  }
  
}