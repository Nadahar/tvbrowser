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
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionListener;

import tvbrowser.core.Settings;
import tvbrowser.core.contextmenu.ConfigMenuItem;
import tvbrowser.core.contextmenu.ContextMenuManager;
import tvbrowser.core.contextmenu.SeparatorMenuItem;
import tvbrowser.core.icontheme.IconLoader;
import tvbrowser.core.plugin.PluginProxy;
import tvbrowser.core.plugin.PluginProxyManager;
import tvbrowser.core.plugin.PluginStateAdapter;
import tvbrowser.ui.settings.util.LineButton;
import util.ui.LineComponent;
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

  private ContextMenuIf mDefaultIf, mMiddleClickIf;

  private SortableItemList mList;

  public static final util.ui.Localizer mLocalizer = util.ui.Localizer.getLocalizerFor(ContextmenuSettingsTab.class);

  private JComboBox mDoubleClickBox;

  private JComboBox mMiddleClickBox;

  private boolean mFillingList;

  private int mSelectionWidth;

  private ArrayList mDeactivatedItems;

  public JPanel createSettingsPanel() {
    createList();
    
    mDefaultIf = ContextMenuManager.getInstance().getDefaultContextMenuIf();
    mMiddleClickIf = ContextMenuManager.getInstance().getMiddleClickIf();

    JPanel contentPanel = new JPanel(new FormLayout("5dlu, pref, 3dlu, pref, fill:pref:grow, 3dlu",
        "pref, 5dlu, pref, 3dlu, fill:pref:grow, 3dlu, pref, 3dlu, pref, 3dlu, pref"));
    contentPanel.setBorder(Borders.DIALOG_BORDER);

    CellConstraints cc = new CellConstraints();
    contentPanel.add(DefaultComponentFactory.getInstance().createSeparator(mLocalizer.msg("title", "Title")), cc.xyw(1,
        1, 6));

    contentPanel.add(UiUtilities.createHelpTextArea(mLocalizer.msg("ItemOrder", "Item Order:")), cc.xyw(2, 3, 4));
    
    contentPanel.add(mList, cc.xyw(2, 5, 4));

    contentPanel.add(new JLabel(mLocalizer.msg("MouseButtons", "Mouse Buttons:")), cc.xyw(2, 7, 4));

    contentPanel.add(new JLabel(mLocalizer.msg("doubleClickLabel", "Double Click")), cc.xy(2, 9));
    
    mDoubleClickBox = new JComboBox();
    mDoubleClickBox.setSelectedItem(mDefaultIf);
    mDoubleClickBox.setMaximumRowCount(15);
    mDoubleClickBox.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (!mFillingList) {
          mDefaultIf = (ContextMenuIf) mDoubleClickBox.getSelectedItem();
          mList.getList().updateUI();
        }
      }
    });
    
    mDoubleClickBox.setRenderer(new ContextMenuCellRenderer(false));
    contentPanel.add(mDoubleClickBox, cc.xy(4, 9));

    contentPanel.add(new JLabel(mLocalizer.msg("middleClickLabel", "Middle Click")), cc.xy(2, 11));
    mMiddleClickBox = new JComboBox();
    mMiddleClickBox.setSelectedItem(mMiddleClickIf);
    mMiddleClickBox.setMaximumRowCount(15);
    mMiddleClickBox.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (!mFillingList) {
          mMiddleClickIf = (ContextMenuIf) mMiddleClickBox.getSelectedItem();
          mList.getList().updateUI();
        }
      }
    });
    
    mMiddleClickBox.setRenderer(new ContextMenuCellRenderer(false));
    contentPanel.add(mMiddleClickBox, cc.xy(4, 11));

    fillListbox();

    return contentPanel;
  }

  private void createList() {
    mList = new SortableItemList();

    mList.getList().addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent e) {
        if (e.getX() > mSelectionWidth) {
          mList.requestFocus();
          if (SwingUtilities.isLeftMouseButton(e) && (e.getClickCount() == 2)) {
            int inx = mList.getList().locationToIndex(e.getPoint());
            if (inx >= 0) {
              ContextMenuIf item = (ContextMenuIf) mList.getList().getModel().getElementAt(inx);
              if (!(item instanceof SeparatorMenuItem) && !(item instanceof ConfigMenuItem)) {
                mList.getList().ensureIndexIsVisible(inx);
                mList.getList().setSelectedIndex(inx);
                mDefaultIf = item;
                mDoubleClickBox.setSelectedItem(mDefaultIf);
                mList.updateUI();
              }
            }
          }
          if (SwingUtilities.isMiddleMouseButton(e) && (e.getClickCount() == 1)) {
            int inx = mList.getList().locationToIndex(e.getPoint());
            if (inx >= 0) {
              ContextMenuIf item = (ContextMenuIf) mList.getList().getModel().getElementAt(inx);
              if (!(item instanceof SeparatorMenuItem) && !(item instanceof ConfigMenuItem)) {
                mList.getList().ensureIndexIsVisible(inx);
                mList.getList().setSelectedIndex(inx);
                mMiddleClickIf = item;
                mMiddleClickBox.setSelectedItem(mMiddleClickIf);
                mList.updateUI();
              }
            }
          }
        }
      }
      public void mouseReleased(MouseEvent evt) {
        if (evt.getX() < mSelectionWidth) {
          int index = mList.getList().locationToIndex(evt.getPoint());
          if (index != -1) {
            Object item = mList.getList().getModel().getElementAt(index);
            if (!mDeactivatedItems.remove(item)) {
              mDeactivatedItems.add(item);
            }
              
            mList.repaint();
          }
        }
      }
    });
    
    mList.setCellRenderer(new ContextMenuCellRenderer(true));

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
  
  private void fillListbox() {
    mFillingList = true;
    if (mList == null) {
      return;
    }
    mList.removeAllElements();

    ArrayList items = new ArrayList();

    ContextMenuIf[] menuIfList = ContextMenuManager.getInstance().getAvailableContextMenuIfs(true, false);
    Program exampleProgram = Plugin.getPluginManager().getExampleProgram();
    for (int i = 0; i < menuIfList.length; i++) {
      if (menuIfList[i] instanceof SeparatorMenuItem) {
        mList.addElement(menuIfList[i]);
      } else if (menuIfList[i] instanceof ConfigMenuItem) {
          mList.addElement(menuIfList[i]);
      } else {
        ActionMenu actionMenu = menuIfList[i].getContextMenuActions(exampleProgram);
        if (actionMenu != null) {
          mList.addElement(menuIfList[i]);
          items.add(menuIfList[i]);
        }
      }
    }
    
    mDeactivatedItems = new ArrayList(ContextMenuManager.getInstance().getDisabledContextMenuIfs());
    
    mDoubleClickBox.removeAllItems();
    mMiddleClickBox.removeAllItems();
    for (int i=0;i<items.size();i++) {
      mDoubleClickBox.addItem(items.get(i));
      mMiddleClickBox.addItem(items.get(i));
    }
    mDoubleClickBox.setSelectedItem(mDefaultIf);
    mMiddleClickBox.setSelectedItem(mMiddleClickIf);
    mFillingList = false;
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
    
    String[] deactivated = new String[mDeactivatedItems.size()];
    for (int i=0;i<mDeactivatedItems.size();i++) {
      deactivated[i] = ((ContextMenuIf)mDeactivatedItems.get(i)).getId();
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
    private boolean mUseInList = false;
    private JCheckBox mItemSelected;
    private JLabel mItemLabel;
    private JPanel mItemPanel;
    
    public ContextMenuCellRenderer(boolean useInList) {
      mUseInList = useInList;
      
      mItemSelected = new JCheckBox();
      mItemSelected.setOpaque(false);
      mSelectionWidth = mItemSelected.getPreferredSize().width;
      
      mItemLabel = new JLabel();
      mItemPanel = new JPanel(new BorderLayout());
      mItemPanel.add(mItemSelected, BorderLayout.WEST);
      mItemPanel.add(mItemLabel, BorderLayout.CENTER);
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
      } else if (value instanceof ConfigMenuItem) {
        mItemSelected.setSelected(!mDeactivatedItems.contains(value));

        mItemLabel.setFont(label.getFont());
        mItemLabel.setIcon(IconLoader.getInstance().getIconFromTheme("categories", "preferences-desktop", 16));
        mItemLabel.setText(value.toString());

        mItemPanel.setForeground(label.getForeground());
        mItemPanel.setBackground(label.getBackground());
        mItemPanel.setOpaque(label.isOpaque());

        return mItemPanel;
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

        mItemLabel.setFont(label.getFont());
        mItemLabel.setIcon(icon);

        if (mUseInList) {
          mItemSelected.setSelected(!mDeactivatedItems.contains(value));
          
          if (menuIf.equals(mDefaultIf) && menuIf.equals(mMiddleClickIf)) {
            mItemLabel.setFont(mItemLabel.getFont().deriveFont(Font.BOLD));
            text.append(" - ").append(mLocalizer.msg("doubleClick", "double-click")).append(" + ").append(mLocalizer.msg("middleClick", "middle-click"));
          } else if (menuIf.equals(mDefaultIf)) {
            mItemLabel.setFont(mItemLabel.getFont().deriveFont(Font.BOLD));
            text.append(" - ").append(mLocalizer.msg("doubleClick", "double-click"));
          } else if (menuIf.equals(mMiddleClickIf)) {
            mItemLabel.setFont(mItemLabel.getFont().deriveFont(Font.BOLD));
            text.append(" - ").append(mLocalizer.msg("middleClick", "middle-click"));
          }
          mItemLabel.setText(text.toString());

          mItemPanel.setForeground(label.getForeground());
          mItemPanel.setBackground(label.getBackground());
          mItemPanel.setOpaque(label.isOpaque());
          
          return mItemPanel;
        }

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