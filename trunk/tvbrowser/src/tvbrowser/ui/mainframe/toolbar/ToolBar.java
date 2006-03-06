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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragSource;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.border.CompoundBorder;

import tvbrowser.core.Settings;
import tvbrowser.core.plugin.PluginProxyManager;
import tvbrowser.ui.mainframe.MainFrame;
import tvbrowser.ui.settings.SettingsDialog;
import tvbrowser.ui.settings.ToolBarDragAndDropSettings;
import util.ui.UiUtilities;
import devplugin.Plugin;

public class ToolBar extends JToolBar {

  private static final util.ui.Localizer mLocalizer = util.ui.Localizer
      .getLocalizerFor(ToolBar.class);

  public static final String ACTION_VALUE = "ActionValue";
  public static final String ACTION_TYPE_KEY = "ActionType";
  public static final String ACTION_ID_KEY = "ActionId";
  public static final String ACTION_IS_SELECTED = "ActionIsSelected";

  public static final int BUTTON_ACTION = 0;
  public static final int TOOGLE_BUTTON_ACTION = 1;
  public static final int SEPARATOR = 2;

  public static final int STYLE_TEXT = 1, STYLE_ICON = 2;
  private static final int ICON_BIG = 1, ICON_SMALL = 2;

  private static Insets NULL_INSETS = new Insets(0, 0, 0, 0);
  private static Font TEXT_FONT = new Font("Dialog", Font.PLAIN, 10);

  private ToolBarModel mModel;
  private ContextMenu mContextMenu;
  private int mStyle;
  private int mIconSize;
  private String mLocation;
  private boolean disabled = false;

  public ToolBar(ToolBarModel model) {
    super();
    mModel = model;
    loadSettings();
    mContextMenu = new ContextMenu(this);
    setFloatable(false);
    update();
    addMouseListener(new MouseAdapter() {

      public void mousePressed(MouseEvent e) {
        if (e.isPopupTrigger()) {
          mContextMenu.show(e.getX(), e.getY());
        }
      }

      public void mouseReleased(MouseEvent e) {
        if (e.isPopupTrigger()) {
          mContextMenu.show(e.getX(), e.getY());
        }
      }
      
    });
  }

  public void updateTimeButtons() {
    ((DefaultToolBarModel) mModel).updateTimeButtons();
    update();
  }

  public void update() {
    super.removeAll();
    Action[] actions = mModel.getActions();
    for (int i = 0; i < actions.length; i++) {
      Action action = actions[i];
      Integer typeInteger = (Integer) action.getValue(ACTION_TYPE_KEY);
      int type = -1;
      if (typeInteger != null) {
        type = typeInteger.intValue();
      }

      if (type == TOOGLE_BUTTON_ACTION) {
        addToggleButton(action);
      } else if (type == SEPARATOR) {
        addSeparator();
      } else {
        addButton(action);
      }

    }

    updateUI();
    disabled = false;
  }

  /**
   * Set up the ToolBar for Drag'n'Drop.
   * 
   * @param s
   *          The Drag'n'Drop Class.
   * @param west
   *          The toolbar is shown in the west.
   */
  public void disableForDragAndDrop(ToolBarDragAndDropSettings s, boolean west) {
    disabled = true;
    /*
     * If the ToolBar is empty set the size for better dopping the first
     * ActionButton
     */
    if (this.getComponentCount() == 0)
      if (!west)
        this.setPreferredSize(new Dimension(this.getWidth(), 15));
      else
        this.setPreferredSize(new Dimension(15, this.getHeight()));

    this.updateUI();

    int x = this.getComponentCount();

    /* Prepare all ToolBar buttons for Drag'n'Drop */
    for (int i = 0; i < x; i++) {
      this.getComponent(i).addMouseMotionListener(s);
      (new DragSource()).createDefaultDragGestureRecognizer(this
          .getComponent(i), DnDConstants.ACTION_MOVE, s);
      if (this.getComponent(i) instanceof AbstractButton) {
        AbstractButton b = (AbstractButton) this.getComponent(i);
        b.setDisabledIcon(b.getIcon());

        if (west)
          b.setBorder(new CompoundBorder(BorderFactory.createEmptyBorder(1, 0,
              0, 0), BorderFactory.createEmptyBorder(1, 1, 1, 1)));
        else
          b.setBorder(new CompoundBorder(BorderFactory.createEmptyBorder(0, 1,
              0, 0), BorderFactory.createEmptyBorder(1, 1, 1, 1)));

        b.setToolTipText("");
        b.setEnabled(false);
      }
      this.getComponent(i).addMouseListener(s);
    }
  }

  private void addToggleButton(Action action) {
    final JToggleButton button = new JToggleButton(action);
    action.putValue(ACTION_VALUE, button);
    addButtonProperties(button, action);
    Boolean isSelected = (Boolean) action.getValue(ACTION_IS_SELECTED);
    if (isSelected != null) {
      button.setSelected(isSelected.booleanValue());
    }
    button.setBorderPainted(isSelected != null && isSelected.booleanValue());
    button.addMouseListener(new MouseAdapter() {
      public void mouseEntered(MouseEvent e) {
        if (!button.isSelected()) {
          button.setBorderPainted(true);
        }
      }

      public void mouseExited(MouseEvent e) {
        if (!button.isSelected()) {
          button.setBorderPainted(false);
        }
      }
    });

    add(button);
  }

  private void addButton(final Action action) {
    final JButton button = new JButton();
    addButtonProperties(button, action);
    button.setBorderPainted(false);

    button.addMouseListener(new MouseAdapter() {
      public void mouseEntered(MouseEvent e) {
        button.setBorderPainted(true);
      }

      public void mouseExited(MouseEvent e) {
        button.setBorderPainted(false);
      }

      public void mousePressed(MouseEvent e) {
        if (e.isPopupTrigger() && !disabled)
          showPopupMenu(e);
      }

      public void mouseReleased(MouseEvent e) {
        if (e.isPopupTrigger() && !disabled)
          showPopupMenu(e);
      }
    });

    add(button);
  }

  private void showPopupMenu(MouseEvent e) {
    JPopupMenu menu = new JPopupMenu();
    String label = mLocalizer.msg("configure", "Configure");
    String name = null;
    boolean showall = false;

    if (e.getSource() instanceof JButton) {
      name = ((JButton) e.getSource()).getName();

      if (name.startsWith("#scrollTo")) {
        showall = true;
        label = mLocalizer.msg("configureTime", "Configure time buttons");
      }
      if(name.startsWith(SettingsDialog.TAB_ID_REMINDER)) {
        showall = true;
        label = mLocalizer.msg("configureReminder", "Configure Reminder");
      }
      if(name.startsWith(SettingsDialog.TAB_ID_FAVORITE)) {
        showall = true;
        label = mLocalizer.msg("configureFavorite", "Configure Favorites");
      }
      
      if ((PluginProxyManager.getInstance().getPluginForId(name) != null))
        showall = true;
    } else
      return;

    if (showall) {
      JMenuItem item = new JMenuItem(label);
      item.setActionCommand(name);

      item.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          if (e.getActionCommand().startsWith("#scrollTo"))
            MainFrame.getInstance().showSettingsDialog("#timebuttons");
          else
            MainFrame.getInstance().showSettingsDialog(e.getActionCommand());
        }
      });
      menu.add(item);
      menu.addSeparator();
    }

    menu.add(ContextMenu.getSubMenu());

    menu.show(e.getComponent(), e.getX(), e.getY());
  }

  private void addButtonProperties(final AbstractButton button,
      final Action action) {
    String tooltip = (String) action.getValue(Action.SHORT_DESCRIPTION);
    Icon icon = getIcon(action);
    String title = getTitle(action);

    button.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        action.actionPerformed(e);
      }
    });

    button.setText(title);
    button.setIcon(icon);
    button.setName(action.getValue(ToolBar.ACTION_ID_KEY).toString());
    button.setVerticalTextPosition(SwingConstants.BOTTOM);
    button.setHorizontalTextPosition(SwingConstants.CENTER);
    button.setFont(TEXT_FONT);
    button.setMargin(NULL_INSETS);
    button.setFocusPainted(false);
    button.setToolTipText(tooltip);
  }

  private String getTitle(Action action) {
    if ((mStyle & STYLE_TEXT) == STYLE_TEXT) {
      return (String) action.getValue(Action.NAME);
    }
    return null;
  }

  private Icon getIcon(Action action) {
    if ((mStyle & STYLE_ICON) == STYLE_ICON) {
      Icon icon;
      if (mIconSize == ICON_BIG) {
        icon = (Icon) action.getValue(Plugin.BIG_ICON);

        if ((icon != null)
            && ((icon.getIconHeight() != 22) || (icon.getIconWidth() != 22))) {
          icon = UiUtilities.scaleIcon(icon, 22, 22);
        }

      } else {
        icon = (Icon) action.getValue(Action.SMALL_ICON);

        if ((icon != null)
            && ((icon.getIconHeight() != 16) || (icon.getIconWidth() != 16))) {
          icon = UiUtilities.scaleIcon(icon, 16, 16);
        }

      }
      return icon;
    }
    return null;
  }

  public void setStyle(int style) {
    mStyle = style;
  }

  public int getStyle() {
    return mStyle;
  }

  private void loadSettings() {

    String styleStr = Settings.propToolbarButtonStyle.getString();
    if ("text".equals(styleStr)) {
      mStyle = STYLE_TEXT;
    } else if ("icon".equals(styleStr)) {
      mStyle = STYLE_ICON;
    } else {
      mStyle = STYLE_ICON | STYLE_TEXT;
    }

    setUseBigIcons(Settings.propToolbarUseBigIcons.getBoolean());

    String locationStr = Settings.propToolbarLocation.getString();
    mLocation = null;
    if ("west".equals(locationStr)) {
      mLocation = BorderLayout.WEST;
    } else {
      mLocation = BorderLayout.NORTH;
    }

    if (mLocation == BorderLayout.EAST || mLocation == BorderLayout.WEST) {
      setOrientation(JToolBar.VERTICAL);
    } else {
      setOrientation(JToolBar.HORIZONTAL);
    }

  }

  public void storeSettings() {

    if (mStyle == STYLE_TEXT) {
      Settings.propToolbarButtonStyle.setString("text");
    } else if (mStyle == STYLE_ICON) {
      Settings.propToolbarButtonStyle.setString("icon");
    } else {
      Settings.propToolbarButtonStyle.setString("text&icon");
    }

    Settings.propToolbarUseBigIcons.setBoolean(mIconSize == ICON_BIG);

    if (mLocation == null) {
      Settings.propToolbarLocation.setString("hidden");
    } else if (mLocation == BorderLayout.WEST) {
      Settings.propToolbarLocation.setString("west");
    } else {
      Settings.propToolbarLocation.setString("north");
    }

  }

  public void setToolbarLocation(String location) {
    mLocation = location;
  }

  public String getToolbarLocation() {
    return mLocation;
  }

  public void setUseBigIcons(boolean arg) {
    if (arg) {
      mIconSize = ICON_BIG;
    } else {
      mIconSize = ICON_SMALL;
    }
  }

  public boolean useBigIcons() {
    return mIconSize == ICON_BIG;
  }

}
