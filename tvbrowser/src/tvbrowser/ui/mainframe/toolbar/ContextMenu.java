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

package tvbrowser.ui.mainframe.toolbar;

import tvbrowser.ui.mainframe.toolbar.ToolBar;
import tvbrowser.ui.mainframe.toolbar.ToolBar;
import tvbrowser.ui.mainframe.MainFrame;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;


public class ContextMenu {

  public static final util.ui.Localizer mLocalizer
       = util.ui.Localizer.getLocalizerFor(ContextMenu.class);


  private ToolBar mToolBar;
  private JPopupMenu mMenu;

  public ContextMenu(ToolBar toolBar) {
    mToolBar = toolBar;
    mMenu = new JPopupMenu();
  }

  public void show(int x, int y) {
    update();
    mMenu.show(mToolBar, x,y);
  }

  private void update() {
    mMenu.removeAll();
    mMenu.add(createLockMenuItem());
    mMenu.add(createViewMenu());
    mMenu.add(createButtonSizeMenuItem());
    mMenu.add(createSetupItem());
  }

  private JMenuItem createLockMenuItem() {
    final JMenuItem item = new JMenuItem();
    if (mToolBar.isFloatable()) {
      item.setText(mLocalizer.msg("lock","lock toolbar"));
    }
    else {
      item.setText(mLocalizer.msg("unlock","unlock toolbar"));
    }
    item.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent event) {
        mToolBar.setFloatable(!mToolBar.isFloatable());
      }
    });
    return item;
  }

  private JMenu createViewMenu() {
    JMenu menu = new JMenu(mLocalizer.msg("view","view"));
    JRadioButtonMenuItem allMenuItem = new JRadioButtonMenuItem(mLocalizer.msg("text.and.icon","text and icon"));
    JRadioButtonMenuItem textonlyMenuItem = new JRadioButtonMenuItem(mLocalizer.msg("text","text"));
    JRadioButtonMenuItem icononlyMenuItem = new JRadioButtonMenuItem(mLocalizer.msg("icon","icon"));
    menu.add(allMenuItem);
    menu.add(textonlyMenuItem);
    menu.add(icononlyMenuItem);
    ButtonGroup group = new ButtonGroup();
    group.add(allMenuItem);
    group.add(textonlyMenuItem);
    group.add(icononlyMenuItem);
    int style = mToolBar.getStyle();
    if (style == ToolBar.STYLE_TEXT) {
      textonlyMenuItem.setSelected(true);
    }
    else if (style == ToolBar.STYLE_ICON) {
      icononlyMenuItem.setSelected(true);
    }
    else {
      allMenuItem.setSelected(true);
    }
    allMenuItem.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent event) {
        mToolBar.setStyle(ToolBar.STYLE_TEXT | ToolBar.STYLE_ICON);
        mToolBar.update();
      }
    });

    textonlyMenuItem.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent event) {
        mToolBar.setStyle(ToolBar.STYLE_TEXT);
        mToolBar.update();
      }
    });

    icononlyMenuItem.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent event) {
        mToolBar.setStyle(ToolBar.STYLE_ICON);
        mToolBar.update();
      }
    });

    return menu;
  }

  private JMenuItem createButtonSizeMenuItem() {
    JCheckBoxMenuItem item = new JCheckBoxMenuItem("Use big icons", mToolBar.useBigIcons());
    item.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        mToolBar.setUseBigIcons(!mToolBar.useBigIcons());
        mToolBar.update();
      }
    });

    return item;
  }

  private JMenuItem createSetupItem() {
    JMenuItem item = new JMenuItem("Configure...");
    item.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        MainFrame.getInstance().showSettingsDialog("#toolbar");
      }
    });
    return item;
  }



      /*

  public static final util.ui.Localizer mLocalizer
     = util.ui.Localizer.getLocalizerFor(ContextMenu.class);


  private ToolBar mToolBar;
  private ToolBarItem mItem;

  public ContextMenu(ToolBar toolbar, ToolBarItem item) {
    super();
    mToolBar = toolbar;
    mItem = item;
    createEntries();

  }

  private void createEntries() {
    removeAll();
    add(createLockMenuItem());
    add(createViewMenu());
    add(createInsertButtonItem());
    if (mItem!=null) {
      add(createRemoveItem(mItem));
    }
  }

  private JMenuItem createLockMenuItem() {
    final JMenuItem item = new JMenuItem();
    if (mToolBar.isFloatable()) {
      item.setText(mLocalizer.msg("lock","lock toolbar"));
    }
    else {
      item.setText(mLocalizer.msg("unlock","unlock toolbar"));
    }
    item.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent event) {
        mToolBar.setFloatable(!mToolBar.isFloatable());
      }
    });
    return item;
  }




  private JMenu createInsertButtonItem() {
    JMenu menu = new JMenu(mLocalizer.msg("addbutton","add button"));
    JMenuItem sepMenuItem = new JMenuItem(mLocalizer.msg("separator","separator"));
    menu.add(sepMenuItem);
    sepMenuItem.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent event) {
        mToolBar.getModel().addSeparator();
      }
    });

    ToolBarModel model = mToolBar.getModel();
    ToolBarItem[] availableItems = model.getAvailableItems();
    for (int i=0; i<availableItems.length; i++) {
      if (availableItems[i] instanceof ToolBarButton) {
        final ToolBarButton tbItem = (ToolBarButton)availableItems[i];
        JMenuItem menuItem = new JMenuItem(tbItem.getName());
        menu.add(menuItem);
        boolean enabled = !mToolBar.getModel().containsItem(tbItem);
        menuItem.setEnabled(enabled);
        if (enabled) {
          menuItem.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent event) {
              ToolBarItem[] s = mToolBar.getModel().getVisibleItems();
              mToolBar.getModel().addVisibleItem(tbItem);
            }});
        }

      }
    }

  return menu;
  }

  private JMenu createViewMenu() {
    JMenu menu = new JMenu(mLocalizer.msg("view","view"));
    JRadioButtonMenuItem allMenuItem = new JRadioButtonMenuItem(mLocalizer.msg("text.and.icon","text and icon"));
    JRadioButtonMenuItem textonlyMenuItem = new JRadioButtonMenuItem(mLocalizer.msg("text","text"));
    JRadioButtonMenuItem icononlyMenuItem = new JRadioButtonMenuItem(mLocalizer.msg("icon","icon"));
    menu.add(allMenuItem);
    menu.add(textonlyMenuItem);
    menu.add(icononlyMenuItem);
    ButtonGroup group = new ButtonGroup();
    group.add(allMenuItem);
    group.add(textonlyMenuItem);
    group.add(icononlyMenuItem);
    int style = mToolBar.getStyle();
    if (style == ToolBar.TEXT) {
      textonlyMenuItem.setSelected(true);
    }
    else if (style == ToolBar.ICON) {
      icononlyMenuItem.setSelected(true);
    }
    else {
      allMenuItem.setSelected(true);
    }
    allMenuItem.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent event) {
        mToolBar.setStyle(ToolBar.TEXT | ToolBar.ICON);
      }
    });

    textonlyMenuItem.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent event) {
        mToolBar.setStyle(ToolBar.TEXT);
      }
    });

    icononlyMenuItem.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent event) {
        mToolBar.setStyle(ToolBar.ICON);
      }
    });

    return menu;
  }

  private JMenuItem createRemoveItem(final ToolBarItem tbItem) {
    JMenuItem item = new JMenuItem(mLocalizer.msg("remove","remove this item"));
    item.addActionListener(new ActionListener(){
        public void actionPerformed(ActionEvent event) {
          mToolBar.getModel().removeVisibleItem(tbItem);
        }});

    return item;

  }
      */
}