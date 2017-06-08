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
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.Action;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import devplugin.ActionMenu;
import devplugin.ContextMenuIf;
import devplugin.Plugin;
import devplugin.Program;
import tvbrowser.core.Settings;
import tvbrowser.core.contextmenu.ConfigMenuItem;
import tvbrowser.core.contextmenu.ContextMenuManager;
import tvbrowser.core.contextmenu.LeaveFullScreenMenuItem;
import tvbrowser.core.contextmenu.SeparatorMenuItem;
import tvbrowser.core.plugin.PluginManagerImpl;
import tvbrowser.core.plugin.PluginProxy;
import tvbrowser.core.plugin.PluginProxyManager;
import tvbrowser.core.plugin.PluginStateAdapter;
import tvbrowser.ui.settings.util.LineButton;
import util.ui.FixedSizeIcon;
import util.ui.LineComponent;
import util.ui.Localizer;
import util.ui.TVBrowserIcons;
import util.ui.UiUtilities;
import util.ui.WindowClosingIf;
import util.ui.customizableitems.SelectableItem;
import util.ui.customizableitems.SelectableItemList;
import util.ui.customizableitems.SelectableItemRendererCenterComponentIf;
import util.ui.customizableitems.SortableItemList;

public class ContextmenuSettingsTab implements devplugin.SettingsTab {
  public static final String SEPARATOR_SUB_MENUS_DISABLED = "##_#_##";
  public static final String SEPARATOR_SUB_MENUS_DISABLED_IDS = ";;_#_;;";
  
  private SortableItemList<ContextMenuIf> mList;
  private ArrayList<ContextMenuIf> mEditableMenus;

  /**
   * localizer of this class
   */
  private static final util.ui.Localizer LOCALIZER = util.ui.Localizer
      .getLocalizerFor(ContextmenuSettingsTab.class);

  private int mSelectionWidth;

  private ArrayList<ContextMenuIf> mDeactivatedItems;
  private HashMap<ContextMenuIf, HashSet<Integer>> mDisabledSubMenusMap;

  public JPanel createSettingsPanel() {
    mEditableMenus = new ArrayList<>();
    mDisabledSubMenusMap = ContextMenuManager.getDisabledSubMenuMap();
    
    createList();

    PanelBuilder contentPanel = new PanelBuilder(new FormLayout("5dlu, pref, 3dlu, pref, fill:pref:grow, 3dlu",
        "pref, 5dlu, pref, 3dlu, fill:pref:grow"));
    contentPanel.border(Borders.DIALOG);

    CellConstraints cc = new CellConstraints();
    contentPanel.addSeparator(LOCALIZER.msg("title", "Title"), cc.xyw(1,
        1, 6));

    contentPanel.add(UiUtilities.createHelpTextArea(LOCALIZER.msg("ItemOrder", "Item Order:")), cc.xyw(2, 3, 4));

    contentPanel.add(mList, cc.xyw(2, 5, 4));

    fillListbox();

    return contentPanel.getPanel();
  }

  private void showSubMenuConfigurationDialog(final ContextMenuIf menuIf) {
    final JDialog d = UiUtilities.createDialog(SettingsDialog.getInstance().getDialog(), true);
    d.setTitle(LOCALIZER.msg("subMenuDialogTitle","Shown deactivateable sub menus"));
    
    final ActionMenu menu = menuIf.getContextMenuActions(PluginManagerImpl.getInstance().getExampleProgram());
    
    final ActionMenu[] subItems = menu.getSubItems();
    
    final ArrayList<ActionMenu> available = new ArrayList<ActionMenu>();
    final ArrayList<ActionMenu> selected = new ArrayList<ActionMenu>();
    
    final HashSet<Integer> disabledMenus = mDisabledSubMenusMap.get(menuIf);
    
    for(ActionMenu subItem : subItems) {
      if(subItem.getActionId() != ActionMenu.ID_ACTION_NONE) {
        available.add(subItem);
        
        if(disabledMenus == null || !disabledMenus.contains(subItem.getActionId())) {
          selected.add(subItem);
        }
      }
    }
    
    final SelectableItemList<ActionMenu> listItems = new SelectableItemList<>(selected.toArray(new ActionMenu[selected.size()]), available.toArray(new ActionMenu[available.size()]));
    listItems.addCenterRendererComponent(ActionMenu.class, new SelectableItemRendererCenterComponentIf<ActionMenu>() {
      
      @Override
      public JPanel createCenterPanel(JList<? extends SelectableItem<ActionMenu>> list, ActionMenu value, int index, boolean isSelected, boolean isEnabled, JScrollPane parentScrollPane, int leftColumnWidth) {
        JLabel label = new JLabel();
        
        label.setHorizontalAlignment(SwingConstants.LEADING);
        label.setVerticalAlignment(SwingConstants.CENTER);

        JPanel panel = new JPanel(new BorderLayout());
        
        panel.add(label, BorderLayout.WEST);
        
        Object icon = value.getAction().getValue(Action.SMALL_ICON);
        
        if(icon != null && icon instanceof Icon) {
          label.setIcon((Icon)icon);
        }
        
        label.setText(value.getTitle());
        
        if (isSelected && isEnabled) {
          panel.setOpaque(true);
          label.setForeground(list.getSelectionForeground());
          panel.setBackground(list.getSelectionBackground());
        } else {
          panel.setOpaque(false);
          label.setForeground(list.getForeground());
          panel.setBackground(list.getBackground());
        }
        
        return panel;
      }
      
      @Override
      public void calculateSize(JList<? extends SelectableItem<ActionMenu>> list, int index, JPanel contentPane) {}
    });
   
    final JButton cancel = new JButton(Localizer.getLocalization(Localizer.I18N_CANCEL));
    cancel.addActionListener(e -> {
      d.dispose();
    });
    
    final JButton ok = new JButton(Localizer.getLocalization(Localizer.I18N_OK));
    ok.addActionListener(e -> {
      final HashSet<Integer> result = new HashSet<>();
      
      final List<ActionMenu> selection = listItems.getSelectionList();
      
      for(ActionMenu menuAction : available) {
        if(!selection.contains(menuAction)) {
          result.add(menuAction.getActionId());
        }
      }
      
      if(result.isEmpty()) {
        mDisabledSubMenusMap.remove(menuIf);
      }
      else {
        mDisabledSubMenusMap.put(menuIf, result);
      }
      
      d.dispose();
    });
    
    UiUtilities.registerForClosing(new WindowClosingIf() {
      @Override
      public JRootPane getRootPane() {
        return d.getRootPane();
      }
      
      @Override
      public void close() {
        d.dispose();
      }
    });
    
    final JPanel content = new JPanel(new FormLayout("100dlu:grow,default,5dlu,default","fill:100dlu:grow,5dlu,default"));
    content.setBorder(Borders.DIALOG);
    content.add(listItems, CC.xyw(1, 1, 4));
    content.add(cancel, CC.xy(2, 3));
    content.add(ok, CC.xy(4, 3));
    
    d.setContentPane(content);
    Settings.layoutWindow("contextMenuConfigSubMenusDialog", d, new Dimension(200, 200));
    d.setVisible(true);
  }
  
  private void createList() {
    final JButton configSubMenus = new JButton(TVBrowserIcons.preferences(TVBrowserIcons.SIZE_LARGE));
    configSubMenus.setEnabled(false);
    configSubMenus.setToolTipText(LOCALIZER.msg("subMenuButtonTooltip","Configure shown deactivateable sub menus"));
    configSubMenus.addActionListener(e -> {
      showSubMenuConfigurationDialog(mList.getList().getSelectedValue());
    });
    
    mList = new SortableItemList<>();

    mList.getList().addMouseListener(new MouseAdapter() {
      @Override
      public void mousePressed(MouseEvent e) {
        int index = mList.getList().locationToIndex(e.getPoint());
        
        if (index != -1) {
          ContextMenuIf item = (ContextMenuIf) mList.getList().getModel().getElementAt(index);
          
          configSubMenus.setEnabled(isEditable(item));
        }
      }
      
      @Override
      public void mouseClicked(MouseEvent e) {
        if(configSubMenus.isEnabled() && e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)) {
          showSubMenuConfigurationDialog(mList.getList().getSelectedValue());
        }
      }
      
      @Override
      public void mouseReleased(MouseEvent evt) {
        if (evt.getX() < mSelectionWidth) {
          int index = mList.getList().locationToIndex(evt.getPoint());
          if (index != -1) {
            ContextMenuIf item = (ContextMenuIf) mList.getList().getModel().getElementAt(index);
            
            if (!mDeactivatedItems.remove(item)) {
              mDeactivatedItems.add(item);
              configSubMenus.setEnabled(false);
            }
            else {
              configSubMenus.setEnabled(isEditable(item));
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
    
    mList.addButton(configSubMenus);

    JButton addSeparator = new LineButton();
    addSeparator.setToolTipText(LOCALIZER.msg("separator", "Add Separator"));
    addSeparator.addActionListener(e -> {
      int pos = mList.getList().getSelectedIndex();
      if (pos < 0) {
        pos = mList.getList().getModel().getSize();
      }
      mList.addElement(pos, new SeparatorMenuItem());
      mList.getList().setSelectedIndex(pos);
      mList.getList().ensureIndexIsVisible(pos);
    });

    mList.addButton(addSeparator);
    
    final JButton garbage = new JButton(TVBrowserIcons.delete(TVBrowserIcons.SIZE_LARGE));
    garbage.setToolTipText(LOCALIZER.msg("garbage", "Remove Separator"));
    garbage.addActionListener(e -> {
      List<ContextMenuIf> items = mList.getList().getSelectedValuesList();
      
      for (ContextMenuIf item : items) {
        mList.removeElement(item);
      }
    });

    mList.getList().addListSelectionListener(e -> {
      List<ContextMenuIf> items = mList.getList().getSelectedValuesList();
      if (items.size() == 0) {
        garbage.setEnabled(false);
        return;
      }
      for (int i=0;i<items.size();i++) {
        if (!(items.get(i) instanceof SeparatorMenuItem)) {
          garbage.setEnabled(false);
          return;
        }
      }

      garbage.setEnabled(true);
    });

    garbage.setEnabled(false);
    mList.addButton(garbage);
  }

  private final boolean isEditable(final ContextMenuIf menuIf) {
    return !(menuIf instanceof SeparatorMenuItem)
        && !(menuIf instanceof ConfigMenuItem)
        && !(menuIf instanceof LeaveFullScreenMenuItem)
        && !mDeactivatedItems.contains(menuIf)
        && mEditableMenus.contains(menuIf)
        && mList.getList().getSelectedIndices().length == 1;
  }
  
  private void fillListbox() {
    if (mList == null) {
      return;
    }
    mList.removeAllElements();

    ArrayList<ContextMenuIf> items = new ArrayList<ContextMenuIf>();
    
    mEditableMenus.clear();
    
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
          
          final ActionMenu[] subItems = actionMenu.getSubItems();
          
          if(subItems != null) {
            for(ActionMenu item : subItems) {
              if(item.getActionId() != ActionMenu.ID_ACTION_NONE) {
                mEditableMenus.add(menuIf);
                break;
              }
            }
          }
        }
      }
    }

    mDeactivatedItems = new ArrayList<ContextMenuIf>(ContextMenuManager.getDisabledContextMenuIfs());
  }

  public void saveSettings() {
    Object[] o = mList.getItems();

    ArrayList<String> pluginIDsList = new ArrayList<String>();
    String[] orderIDs = new String[o.length];
    for (int i = 0; i < o.length; i++) {
      ContextMenuIf menuIf = (ContextMenuIf) o[i];
      orderIDs[i] = menuIf.getId();
      if (menuIf instanceof PluginProxy) {
        pluginIDsList.add(menuIf.getId());
      }
    }
    
    final ArrayList<String> listDisabledSubMenus = new ArrayList<>();
    
    if(!mDisabledSubMenusMap.isEmpty()) {
      final Set<ContextMenuIf> keys = mDisabledSubMenusMap.keySet();
      
      for(ContextMenuIf key : keys) {
        final HashSet<Integer> set = mDisabledSubMenusMap.get(key);
        
        if(set != null && !set.isEmpty()) {
          final StringBuilder disabledSubMenus = new StringBuilder(key.getId());
          disabledSubMenus.append(SEPARATOR_SUB_MENUS_DISABLED);
          
          for(Integer value : set) {
            disabledSubMenus.append(value).append(SEPARATOR_SUB_MENUS_DISABLED_IDS);
          }
          
          disabledSubMenus.delete(disabledSubMenus.length()-SEPARATOR_SUB_MENUS_DISABLED_IDS.length(), disabledSubMenus.length());
          
          listDisabledSubMenus.add(disabledSubMenus.toString());
        }
      }
    }
    
    Settings.propContextMenuDisabledSubItems.setStringArray(listDisabledSubMenus.toArray(new String[listDisabledSubMenus.size()]));
    
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
    return LOCALIZER.msg("title", "context menu");
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
    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
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

        mItemLabel.setIcon(TVBrowserIcons.fullScreen(TVBrowserIcons.SIZE_SMALL));
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