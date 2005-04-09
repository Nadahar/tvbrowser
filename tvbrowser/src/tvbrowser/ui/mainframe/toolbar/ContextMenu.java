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


import tvbrowser.ui.mainframe.MainFrame;
import tvbrowser.ui.settings.SettingsDialog;
import tvbrowser.core.Settings;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.*;
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
    mMenu.add(createButtonSizeMenuItem());
    mMenu.add(createViewMenu());
    mMenu.add(createLocationMenu());
    mMenu.addSeparator();
    mMenu.add(createConfigureItem());
  }


  private JMenu createLocationMenu() {
    JMenu menu = new JMenu(mLocalizer.msg("location", "Location"));
    JRadioButtonMenuItem topMenuItem = new JRadioButtonMenuItem(mLocalizer.msg("top","Top"));
    JRadioButtonMenuItem leftMenuItem = new JRadioButtonMenuItem(mLocalizer.msg("left","Left"));
    menu.add(topMenuItem);
    menu.add(leftMenuItem);
    ButtonGroup group = new ButtonGroup();
    group.add(topMenuItem);
    group.add(leftMenuItem);
    String loc = Settings.propToolbarLocation.getString();
   if ("west".equals(loc)) {
      leftMenuItem.setSelected(true);
    }
    else {
      topMenuItem.setSelected(true);
    }
    topMenuItem.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        mToolBar.setToolbarLocation(BorderLayout.NORTH);
        mToolBar.storeSettings();
        MainFrame.getInstance().updateToolbar();
      }
    });

    leftMenuItem.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        mToolBar.setToolbarLocation(BorderLayout.WEST);
        mToolBar.storeSettings();
        MainFrame.getInstance().updateToolbar();
      }
    });
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
        mToolBar.storeSettings();
        mToolBar.update();
      }
    });

    textonlyMenuItem.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent event) {
        mToolBar.setStyle(ToolBar.STYLE_TEXT);
        mToolBar.storeSettings();
        mToolBar.update();
      }
    });

    icononlyMenuItem.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent event) {
        mToolBar.setStyle(ToolBar.STYLE_ICON);
        mToolBar.storeSettings();
        mToolBar.update();
      }
    });

    return menu;
  }

  private JMenuItem createButtonSizeMenuItem() {
    JCheckBoxMenuItem item = new JCheckBoxMenuItem(mLocalizer.msg("bigIcons","Use big icons"), mToolBar.useBigIcons());
    item.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        mToolBar.setUseBigIcons(!mToolBar.useBigIcons());
        mToolBar.storeSettings();
        mToolBar.update();
      }
    });

    return item;
  }

  private JMenuItem createConfigureItem() {
    JMenuItem item = new JMenuItem(mLocalizer.msg("configure","Configure")+"...");
    item.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        MainFrame.getInstance().showSettingsDialog(SettingsDialog.TAB_ID_TOOLBAR);
      }
    });
    return item;
  }

}