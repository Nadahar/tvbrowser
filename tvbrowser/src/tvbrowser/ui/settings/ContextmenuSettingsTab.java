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
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import javax.swing.Action;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.event.ListSelectionListener;

import tvbrowser.core.Settings;
import tvbrowser.core.contextmenu.ConfigMenuItem;
import tvbrowser.core.contextmenu.ContextMenuManager;
import tvbrowser.core.contextmenu.LeaveFullScreenMenuItem;
import tvbrowser.core.contextmenu.SeparatorMenuItem;
import tvbrowser.core.plugin.PluginProxy;
import tvbrowser.core.plugin.PluginProxyManager;
import tvbrowser.core.plugin.PluginStateAdapter;
import tvbrowser.ui.settings.util.LineButton;
import util.ui.FixedSizeIcon;
import util.ui.LineComponent;
import util.ui.TVBrowserIcons;
import util.ui.UiUtilities;
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

  private SortableItemList mList;

  /**
   * localizer of this class
   */
  public static final util.ui.Localizer mLocalizer = util.ui.Localizer.getLocalizerFor(ContextmenuSettingsTab.class);

  private int mSelectionWidth;

  private ArrayList<ContextMenuIf> mDeactivatedItems;

  public JPanel createSettingsPanel() {
    createList();
    
    JPanel contentPanel = new JPanel(new FormLayout("5dlu, pref, 3dlu, pref, fill:pref:grow, 3dlu",
        "pref, 5dlu, pref, 3dlu, fill:pref:grow"));
    contentPanel.setBorder(Borders.DIALOG_BORDER);

    CellConstraints cc = new CellConstraints();
    contentPanel.add(DefaultComponentFactory.getInstance().createSeparator(mLocalizer.msg("title", "Title")), cc.xyw(1,
        1, 6));

    contentPanel.add(UiUtilities.createHelpTextArea(mLocalizer.msg("ItemOrder", "Item Order:")), cc.xyw(2, 3, 4));
    
    contentPanel.add(mList, cc.xyw(2, 5, 4));

    fillListbox();

    return contentPanel;
  }

  private void createList() {
    mList = new SortableItemList();
    
    mList.getList().addMouseListener(new MouseAdapter() {
      @Override
      public void mouseReleased(MouseEvent evt) {
        if (evt.getX() < mSelectionWidth) {
          int index = mList.getList().locationToIndex(evt.getPoint());
          if (index != -1) {
            ContextMenuIf item = (ContextMenuIf) mList.getList().getModel().getElementAt(index);
            if (!mDeactivatedItems.remove(item)) {
              mDeactivatedItems.add(item);
            }
              
            mList.repaint();
          }
        }
      }
    });
    
    mList.setCellRenderer(new ContextMenuCellRenderer());

    PluginProxyManager.getInstance().addPluginStateListener(new PluginStateAdapter() {
      public void pluginActivated(PluginProxy proxy) {
        fillListbox();
      }

      public void pluginDeactivated(PluginProxy proxy) {
        fillListbox();
      }
    });
    
    JButton addSeparator = new LineButton();
    addSeparator.setToolTipText(mLocalizer.msg("separator", "Add Separator"));
    addSeparator.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        int pos = mList.getList().getSelectedIndex();
        if (pos < 0) {
          pos = mList.getList().getModel().getSize();
        }
        mList.addElement(pos, new SeparatorMenuItem());
        mList.getList().setSelectedIndex(pos);
        mList.getList().ensureIndexIsVisible(pos);
      }
    });
    
    mList.addButton(addSeparator);
    
    final JButton garbage = new JButton(TVBrowserIcons.delete(TVBrowserIcons.SIZE_LARGE));
    garbage.setToolTipText(mLocalizer.msg("garbage", "Remove Separator"));
    garbage.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        Object[] items = mList.getList().getSelectedValues();
        for (Object item : items) {
          mList.removeElement(item);
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
  
  private void fillListbox() {
    if (mList == null) {
      return;
    }
    mList.removeAllElements();

    ArrayList<ContextMenuIf> items = new ArrayList<ContextMenuIf>();

    ContextMenuIf[] menuIfList = ContextMenuManager.getInstance().getAvailableContextMenuIfs(true, false);
    Program exampleProgram = Plugin.getPluginManager().getExampleProgram();
    for (ContextMenuIf menuIf : menuIfList) {
      if (menuIf instanceof SeparatorMenuItem) {
        mList.addElement(menuIf);
      } else if (menuIf instanceof ConfigMenuItem || menuIf instanceof LeaveFullScreenMenuItem) {
          mList.addElement(menuIf);
      } else {
        ActionMenu actionMenu = menuIf.getContextMenuActions(exampleProgram);
        if (actionMenu != null) {
          mList.addElement(menuIf);
          items.add(menuIf);
        }
      }
    }
    
    mDeactivatedItems = new ArrayList<ContextMenuIf>(ContextMenuManager.getInstance().getDisabledContextMenuIfs());
  }

  public void saveSettings() {
    Object o[] = mList.getItems();

    ArrayList<String> pluginIDsList = new ArrayList<String>();
    String[] orderIDs = new String[o.length];
    for (int i = 0; i < o.length; i++) {
      ContextMenuIf menuIf = (ContextMenuIf) o[i];
      orderIDs[i] = menuIf.getId();
      if (menuIf instanceof PluginProxy) {
        pluginIDsList.add(menuIf.getId());
      }
    }

    String[] pluginIDs = new String[pluginIDsList.size()];
    pluginIDsList.toArray(pluginIDs);

    Settings.propContextMenuOrder.setStringArray(orderIDs);
    Settings.propPluginOrder.setStringArray(pluginIDs);
    
    PluginProxyManager.getInstance().setPluginOrder(pluginIDs);

    String[] deactivated = new String[mDeactivatedItems.size()];
    for (int i=0;i<mDeactivatedItems.size();i++) {
      deactivated[i] = (mDeactivatedItems.get(i)).getId();
    }
    Settings.propContextMenuDisabledItems.setStringArray(deactivated);
  }

  public Icon getIcon() {
    return null;
  }

  public String getTitle() {
    return mLocalizer.msg("title", "context menu");
  }
  
  class ContextMenuCellRenderer extends DefaultListCellRenderer {
    private JCheckBox mItemSelected;
    private JLabel mItemLabel;
    private JPanel mItemPanel;
    
    public ContextMenuCellRenderer() {
      mItemSelected = new JCheckBox();
      mItemSelected.setOpaque(false);
      mSelectionWidth = mItemSelected.getPreferredSize().width;
      
      mItemLabel = new JLabel();
      mItemPanel = new JPanel(new BorderLayout());
      mItemPanel.add(mItemSelected, BorderLayout.WEST);
      mItemPanel.add(mItemLabel, BorderLayout.CENTER);
    }

    @Override
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
        boolean cellHasFocus) {

      JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
      
      if (value instanceof SeparatorMenuItem) {
        LineComponent comp = new LineComponent(label.getForeground());
        comp.setBackground(label.getBackground());
        comp.setOpaque(isSelected);
        comp.setPreferredSize(label.getPreferredSize());
        return comp;
      } else if (value instanceof ConfigMenuItem) {
        mItemSelected.setSelected(!mDeactivatedItems.contains(value));

        mItemLabel.setIcon(TVBrowserIcons.preferences(TVBrowserIcons.SIZE_SMALL));
        mItemLabel.setText(value.toString());
        mItemLabel.setForeground(label.getForeground());

        mItemPanel.setBackground(label.getBackground());
        mItemPanel.setOpaque(isSelected);
        
        return mItemPanel;
      } else if (value instanceof LeaveFullScreenMenuItem) {
        mItemSelected.setSelected(!mDeactivatedItems.contains(value));

        mItemLabel.setIcon(null);
        mItemLabel.setText(value.toString());
        mItemLabel.setForeground(label.getForeground());

        mItemPanel.setBackground(label.getBackground());
        mItemPanel.setOpaque(isSelected);
        
        return mItemPanel;
      } else if (value instanceof ContextMenuIf) {
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
            if (icon != null) {
              icon = new FixedSizeIcon(16, 16, icon);
            }
          } else if (menuIf instanceof PluginProxy) {
            text.append(((PluginProxy) menuIf).getInfo().getName());
            icon = ((PluginProxy) menuIf).getMarkIcon();
          } else {
            text.append("unknown");
            icon = null;
          }
        }

        mItemLabel.setIcon(icon);
        mItemLabel.setText(text.toString());
        mItemLabel.setForeground(label.getForeground());
        
        mItemSelected.setSelected(!mDeactivatedItems.contains(value));
        
        mItemPanel.setBackground(label.getBackground());
        mItemPanel.setOpaque(isSelected);
        return mItemPanel;
      }
      
      return label;
    }

  }
  
}