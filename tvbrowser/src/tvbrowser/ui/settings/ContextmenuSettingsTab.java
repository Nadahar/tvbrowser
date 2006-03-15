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
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import javax.swing.Action;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionListener;

import tvbrowser.core.ContextMenuManager;
import tvbrowser.core.SeparatorMenuItem;
import tvbrowser.core.Settings;
import tvbrowser.core.icontheme.IconLoader;
import tvbrowser.core.plugin.PluginProxy;
import tvbrowser.core.plugin.PluginProxyManager;
import tvbrowser.core.plugin.PluginStateAdapter;
import tvbrowser.ui.settings.util.LineButton;
import util.ui.LineComponent;
import util.ui.customizableitems.SortableItemList;

import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.factories.DefaultComponentFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import devplugin.ActionMenu;
import devplugin.ContextMenuIf;
import devplugin.Plugin;
import devplugin.Program;

public class ContextmenuSettingsTab implements devplugin.SettingsTab {

  class ContextMenuCellRenderer extends DefaultListCellRenderer {
    private boolean mShowUsage = false;
    
    public ContextMenuCellRenderer(boolean showUsage) {
      mShowUsage = showUsage;
    }

    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
        boolean cellHasFocus) {

      JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

      if (value instanceof SeparatorMenuItem) {
        LineComponent comp = new LineComponent(label.getForeground());
        comp.setBackground(label.getBackground());
        comp.setOpaque(label.isOpaque());
        comp.setPreferredSize(label.getPreferredSize());
        return comp;
      } else if (value instanceof ContextMenuIf) {
        ContextMenuIf menuIf = (ContextMenuIf) value;
        Program exampleProgram = Plugin.getPluginManager().getExampleProgram();

        // Get the context menu item text
        StringBuffer text = new StringBuffer();
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

        if (mShowUsage) {
          /*
           * If the Plugin is the Plugin for double and middle click make the text
           * bold and italic.
           */
          if (menuIf.equals(mDefaultIf) && menuIf.equals(mMiddleClickIf)) {
            label.setFont(label.getFont().deriveFont(Font.BOLD));
            text.append(" - ").append(mLocalizer.msg("doubleClick", "double-click")).append(" + ").append(mLocalizer.msg("middleClick", "middle-click"));
          } else if (menuIf.equals(mDefaultIf)) {
            label.setFont(label.getFont().deriveFont(Font.BOLD));
            text.append(" - ").append(mLocalizer.msg("doubleClick", "double-click"));
          } else if (menuIf.equals(mMiddleClickIf)) {
            label.setFont(label.getFont().deriveFont(Font.BOLD));
            text.append(" - ").append(mLocalizer.msg("middleClick", "middle-click"));
          }
        }

        label.setText(text.toString());
        label.setIcon(icon);
        return label;
      }

      return label;
    }

  }

  private ContextMenuIf mDefaultIf, mMiddleClickIf;

  private SortableItemList mList;

  public static final util.ui.Localizer mLocalizer = util.ui.Localizer.getLocalizerFor(ContextmenuSettingsTab.class);

  public ContextmenuSettingsTab() {
    mList = new SortableItemList();

    mList.getList().addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent e) {
        mList.requestFocus();
        if (SwingUtilities.isLeftMouseButton(e) && (e.getClickCount() == 2)) {
          int inx = mList.getList().locationToIndex(e.getPoint());
          if (inx >= 0) {
            mList.getList().ensureIndexIsVisible(inx);
            mDefaultIf = (ContextMenuIf) mList.getList().getSelectedValue();
            mList.updateUI();
          }
        }
        if (SwingUtilities.isMiddleMouseButton(e) && (e.getClickCount() == 1)) {
          int inx = mList.getList().locationToIndex(e.getPoint());
          if (inx >= 0) {
            mList.getList().ensureIndexIsVisible(inx);
            mList.getList().setSelectedIndex(inx);
            mMiddleClickIf = (ContextMenuIf) mList.getList().getSelectedValue();
            mList.updateUI();
          }
        }
      }
    });
    mList.setCellRenderer(new ContextMenuCellRenderer(true));
    fillListbox();
    int num = mList.getList().getModel().getSize();
    if (num > 15)
      num = 15;
    mList.getList().setVisibleRowCount(num);

    PluginProxyManager.getInstance().addPluginStateListener(new PluginStateAdapter() {
      public void pluginActivated(Plugin p) {
        fillListbox();
      }

      public void pluginDeactivated(Plugin p) {
        fillListbox();
      }
    });
    
    JButton addSeparator = new LineButton();
    addSeparator.setToolTipText(mLocalizer.msg("separator", "Add Separator"));
    addSeparator.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        int pos = mList.getList().getSelectedIndex();
        if (pos < 0)
          pos = mList.getList().getModel().getSize();
        mList.addElement(pos, new SeparatorMenuItem());
        mList.getList().setSelectedIndex(pos);
        mList.getList().ensureIndexIsVisible(pos);
      }
    });
    
    mList.addButton(addSeparator);
    
    final JButton garbage = new JButton(IconLoader.getInstance().getIconFromTheme("actions", "edit-delete", 22));
    garbage.setToolTipText(mLocalizer.msg("garbage", "Remove Separator"));
    garbage.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        Object[] items = mList.getList().getSelectedValues();
        for (int i=0;i<items.length;i++) {
          mList.removeElement(items[i]);
        }
      };
    });
    
    mList.getList().addListSelectionListener(new ListSelectionListener() {
      public void valueChanged(javax.swing.event.ListSelectionEvent e) {
        Object[] items = mList.getList().getSelectedValues();
        if (items.length == 0) {
          garbage.setEnabled(false);
          return;
        }
        for (int i=0;i<items.length;i++) {
          if (!(items[i] instanceof SeparatorMenuItem)) {
            garbage.setEnabled(false);
            return;
          }
        }
        
        garbage.setEnabled(true);
      };
    });

    garbage.setEnabled(false);
    mList.addButton(garbage);    
  }

  public JPanel createSettingsPanel() {
    mDefaultIf = ContextMenuManager.getInstance().getDefaultContextMenuIf();
    mMiddleClickIf = ContextMenuManager.getInstance().getMiddleClickIf();

    JPanel contentPanel = new JPanel(new FormLayout("5dlu, pref, 3dlu, pref, fill:pref:grow, 3dlu",
        "pref, 5dlu, pref, 3dlu, pref, 3dlu, pref"));
    contentPanel.setBorder(Borders.DIALOG_BORDER);

    CellConstraints cc = new CellConstraints();
    contentPanel.add(DefaultComponentFactory.getInstance().createSeparator(mLocalizer.msg("title", "Title")), cc.xyw(1,
        1, 6));

    contentPanel.add(mList, cc.xyw(2, 3, 4));

    contentPanel.add(new JLabel(mLocalizer.msg("doubleClickLabel", "Double Click")), cc.xy(2, 5));
    
    ArrayList items = new ArrayList();
    Object[] objects = mList.getItems();
    for (int i=0;i<objects.length;i++) {
      if (!(objects[i] instanceof SeparatorMenuItem)) {
        items.add(objects[i]);
      }
    }
    
    final JComboBox box = new JComboBox(items.toArray());
    box.setSelectedItem(mDefaultIf);
    box.setMaximumRowCount(15);
    box.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        mDefaultIf = (ContextMenuIf) box.getSelectedItem();
        mList.getList().updateUI();
      }
    });
    
    box.setRenderer(new ContextMenuCellRenderer(false));
    contentPanel.add(box, cc.xy(4, 5));

    contentPanel.add(new JLabel(mLocalizer.msg("middleClickLabel", "Middle Click")), cc.xy(2, 7));
    final JComboBox box2 = new JComboBox(items.toArray());
    box2.setSelectedItem(mMiddleClickIf);
    box2.setMaximumRowCount(15);
    box2.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        mMiddleClickIf = (ContextMenuIf) box2.getSelectedItem();
        mList.getList().updateUI();
      }
    });
    
    box2.setRenderer(new ContextMenuCellRenderer(false));
    contentPanel.add(box2, cc.xy(4, 7));

    return contentPanel;
  }

  private void fillListbox() {
    if (mList == null) {
      return;
    }
    mList.removeAllElements();

    ContextMenuIf[] menuIfList = ContextMenuManager.getInstance().getAvailableContextMenuIfs();
    Program exampleProgram = Plugin.getPluginManager().getExampleProgram();
    for (int i = 0; i < menuIfList.length; i++) {
      if (menuIfList[i] instanceof SeparatorMenuItem) {
        mList.addElement(menuIfList[i]);
      } else {
        ActionMenu actionMenu = menuIfList[i].getContextMenuActions(exampleProgram);
        if (actionMenu != null) {
          mList.addElement(menuIfList[i]);
        }
      }
        
    }
  }

  public void saveSettings() {
    Object o[] = mList.getItems();

    ArrayList pluginIDsList = new ArrayList();
    String[] orderIDs = new String[o.length];
    for (int i = 0; i < o.length; i++) {
      ContextMenuIf menuIf = (ContextMenuIf) o[i];
      orderIDs[i] = menuIf.getId();
      if (menuIf instanceof PluginProxy)
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