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
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragSource;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.border.Border;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import tvbrowser.core.Settings;
import tvbrowser.core.plugin.PluginProxyManager;
import tvbrowser.ui.filter.dlgs.FilterButtons;
import tvbrowser.ui.mainframe.MainFrame;
import tvbrowser.ui.settings.ToolBarDragAndDropSettings;
import util.ui.ChannelContextMenu;
import util.ui.UiUtilities;
import devplugin.Date;
import devplugin.Plugin;
import devplugin.ProgressMonitor;
import devplugin.SettingsItem;

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
  public static final int SPACE = 3;
  public static final int GLUE = 4;

  public static final int STYLE_TEXT = 1, STYLE_ICON = 2;
  private static final int ICON_BIG = 1, ICON_SMALL = 2;

  private static final Insets NULL_INSETS = new Insets(0, 0, 0, 0);
  protected static final Font TEXT_FONT = new Font("Dialog", Font.PLAIN, 10);

  private ToolBarModel mModel;
  private ContextMenu mContextMenu;
  private int mStyle;
  private int mIconSize;
  private String mLocation;
  private boolean disabled = false;

  private JButton mUpdateButton = null;
  
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

  public void updatePluginButtons() {
    ((DefaultToolBarModel) mModel).updatePluginButtons();
    update();
  }
  
  public void updateTimeButtons() {
    ((DefaultToolBarModel) mModel).updateTimeButtons();
    update();
  }
  
  public void updateUpdateButton(boolean showStopButton) {
    if(showStopButton) {
      ((DefaultToolBarModel)mModel).showStopButton();
    } else {
      ((DefaultToolBarModel)mModel).showUpdateButton();
    }
    
    if(mUpdateButton != null) {
      mUpdateButton.removeActionListener(mUpdateButton.getActionListeners()[0]);
      addButtonProperties(mUpdateButton, ((DefaultToolBarModel)mModel).getUpdateAction());
    }
  }
  
  public void update() {
    super.removeAll();
    mUpdateButton = null;
    
    Action[] actions = mModel.getActions();
    for (Action action : actions) {
      Integer typeInteger = (Integer) action.getValue(ACTION_TYPE_KEY);
      int type = -1;
      if (typeInteger != null) {
        type = typeInteger.intValue();
      }
      
      if (type == TOOGLE_BUTTON_ACTION) {
        addToggleButton(action);
      } else if (type == SEPARATOR) {
        JPanel separatorPanel = new JPanel(new FormLayout("0dlu:grow,default,0dlu:grow","fill:default:grow"));
        
        if(mLocation.equals(BorderLayout.WEST))
          separatorPanel.setLayout(new FormLayout("default:grow","0dlu:grow,default,0dlu:grow"));
          
        separatorPanel.setOpaque(false);
        separatorPanel.setBorder(BorderFactory.createEmptyBorder());
        separatorPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        if(mLocation.equals(BorderLayout.NORTH))
          separatorPanel.add(new JSeparator(JSeparator.VERTICAL), new CellConstraints().xy(2,1));
        else
          separatorPanel.add(new JSeparator(JSeparator.HORIZONTAL), new CellConstraints().xy(1,2));

        int height = (int)((mLocation.equals(BorderLayout.NORTH) ? getPreferredSize().height : 20) * 0.85);
        int width = mLocation.equals(BorderLayout.NORTH) ? 10 : 18;
        
        separatorPanel.setPreferredSize(new Dimension(width,height));
        separatorPanel.setMaximumSize(new Dimension(width,height));
        separatorPanel.setMinimumSize(new Dimension(width,height));        
        
        add(separatorPanel);
      } else if (type == GLUE) {
        JPanel gluePanel = new JPanel();
        gluePanel.setOpaque(false);
        gluePanel.setBorder(BorderFactory.createEmptyBorder());
        gluePanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(gluePanel);
      } else if (type == SPACE) {
        JPanel spacePanel = new JPanel();
        spacePanel.setOpaque(false);
        spacePanel.setBorder(BorderFactory.createEmptyBorder());
        spacePanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        int height = (mLocation.equals(BorderLayout.NORTH) ? getPreferredSize().height : 20);
        
        spacePanel.setPreferredSize(new Dimension(20,height));
        spacePanel.setMaximumSize(new Dimension(20,height));
        spacePanel.setMinimumSize(new Dimension(20,height));
          
        add(spacePanel);
      } else {
        addButton(action);
      }
    }

    updateUI();
    disabled = false;
  }
  
  public void updateUI() {
    super.updateUI();
  }
  
  public void setBorder(Border b) {
    super.setBorder(BorderFactory.createEmptyBorder());
  }
  
  public void setLayout(LayoutManager manager) {
    if(mLocation != null) {
      if(mLocation.equals(BorderLayout.NORTH)) {
        super.setLayout(new BoxLayout(this,BoxLayout.X_AXIS));
      } else {
        super.setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
      }
    }
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
    if (this.getComponentCount() == 0) {
      if (!west) {
        this.setPreferredSize(new Dimension(this.getWidth(), 15));
      } else {
        this.setPreferredSize(new Dimension(15, this.getHeight()));
      }
    }

    this.repaint();
    setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    
    int x = this.getComponentCount();

    /* Prepare all ToolBar buttons for Drag'n'Drop */
    for (int i = 0; i < x; i++) {
      (new DragSource()).createDefaultDragGestureRecognizer(this
          .getComponent(i), DnDConstants.ACTION_MOVE, s);
      if (this.getComponent(i) instanceof AbstractButton) {
        AbstractButton b = (AbstractButton) this.getComponent(i);
        b.setDisabledIcon(b.getIcon());
        b.setBorder(BorderFactory.createEmptyBorder(b.getInsets().top,b.getInsets().left,b.getInsets().bottom,b.getInsets().right));
        b.setEnabled(false);
      }
      else if(this.getComponent(i) instanceof JToolBar.Separator) {
        ((JToolBar.Separator)this.getComponent(i)).setBorder(BorderFactory.createLineBorder(getBackground().darker().darker().darker()));
      }
      else if(this.getComponent(i) instanceof JPanel) {
        JPanel filler = ((JPanel)this.getComponent(i));
        
        filler.setSize(filler.getWidth(),10);
        filler.setVisible(true);
        filler.setOpaque(true);
        filler.setBackground(filler.getBackground().brighter());
        
        filler.setBorder(BorderFactory.createLineBorder(getBackground().darker().darker().darker()));
      }
      
      this.getComponent(i).addMouseListener(s);
    }
  }

  private void addToggleButton(final Action action) {
    final JToggleButton button = new JToggleButton();
    button.setAlignmentX(Component.CENTER_ALIGNMENT);
    
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

      public void mousePressed(MouseEvent e) {
        if (e.isPopupTrigger() && !disabled) {
          showPopupMenu(e);
        }
      }

      public void mouseReleased(MouseEvent e) {
        if (e.isPopupTrigger() && !disabled) {
          showPopupMenu(e);
        }
      }
    });

    add(button);
  }

  private void addButton(final Action action) {
    final JButton button = new JButton();
    addButtonProperties(button, action);
    button.setBorderPainted(false);
    button.setAlignmentX(Component.CENTER_ALIGNMENT);

    if(action.equals(((DefaultToolBarModel)mModel).getUpdateAction())) {
      mUpdateButton = button;
    }
    
    button.addMouseListener(new MouseAdapter() {
      public void mouseEntered(MouseEvent e) {
        button.setBorderPainted(true);
      }

      public void mouseExited(MouseEvent e) {
        button.setBorderPainted(false);
      }

      public void mousePressed(MouseEvent e) {
        if (e.isPopupTrigger() && !disabled) {
          showPopupMenu(e);
        }
      }

      public void mouseReleased(MouseEvent e) {
        if (e.isPopupTrigger() && !disabled) {
          showPopupMenu(e);
        }
      }
    });

    add(button);
  }

  private void showPopupMenu(MouseEvent e) {
    JPopupMenu menu = new JPopupMenu();
    String label = mLocalizer.msg("configure", "Configure");
    String name = null;
    boolean showall = false;
    
    if (e.getSource() instanceof AbstractButton) {
      name = ((AbstractButton) e.getSource()).getName();

      if (name.startsWith("#scrollTo") && name.indexOf("Channel") == -1) {
        showall = true;
        label = mLocalizer.msg("configureTime", "Configure time buttons");
      }
      else if(name.startsWith(SettingsItem.SEARCH)) {
        showall = true;
        label = mLocalizer.msg("configureSearch", "Configure Search");
      }
      else if(name.startsWith(SettingsItem.REMINDER)) {
        showall = true;
        label = mLocalizer.msg("configureReminder", "Configure Reminder");
      }
      else if(name.startsWith(SettingsItem.FAVORITE)) {
        showall = true;
        label = mLocalizer.msg("configureFavorite", "Configure Favorites");
      }
      else if(name.startsWith("#filter")) {
        showall = true;
        label = FilterButtons.mLocalizer.msg("createFilter", "Create filter...");
      }
      else if(name.startsWith("#scrollToChannel")) {
        showall = true;
        label = ChannelContextMenu.mLocalizer.msg("addChannels", "Add/Remove channels");
      }
      else if (PluginProxyManager.getInstance().getActivatedPluginForId(name) != null && 
          PluginProxyManager.getInstance().getActivatedPluginForId(name).getSettingsTab() != null) {
        showall = true;
      }
    } else {
      return;
    }

    if (showall) {
      JMenuItem item = new JMenuItem(label);
      item.setActionCommand(name);

      item.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          if (e.getActionCommand().startsWith("#scrollTo") && e.getActionCommand().indexOf("Channel") == -1) {
            MainFrame.getInstance().showSettingsDialog("#timebuttons");
          } else if (e.getActionCommand().startsWith("#filter")) {
            MainFrame.getInstance().showFilterDialog();
          } else if (e.getActionCommand().startsWith("#scrollToChannel")) {
            MainFrame.getInstance().showSettingsDialog(SettingsItem.CHANNELS);
          } else {
            MainFrame.getInstance().showSettingsDialog(e.getActionCommand());
          }
        }
      });
      menu.add(item);
      menu.addSeparator();
    }

    JMenuItem item = new JMenuItem(mLocalizer.msg("removeButton", "Remove button"));
    final String buttonName = name;
    item.addActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent e) {
        String[] ids = Settings.propToolbarButtons.getStringArray();
        ArrayList<String> list = new ArrayList<String>();
        for (String buttonId : ids) {
          if (buttonId.compareTo(buttonName) != 0) {
            list.add(buttonId);
          }
        }
        ids = new String[list.size()];
        list.toArray(ids);
        DefaultToolBarModel.getInstance().setButtonIds(ids);
        MainFrame.getInstance().updateToolbar();
        Settings.propToolbarButtons.setStringArray(ids);
      }});
    menu.add(item);
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
        action.actionPerformed(new ActionEvent(action,ActionEvent.ACTION_PERFORMED,""));
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

    if (mLocation.equals(BorderLayout.EAST) || mLocation.equals(BorderLayout.WEST)) {
      setOrientation(SwingConstants.VERTICAL);
    } else {        
      setOrientation(SwingConstants.HORIZONTAL);
    }
    
    setLayout(null);
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
    } else if (mLocation.equals(BorderLayout.WEST)) {
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

public void dateChanged(Date date, ProgressMonitor monitor, Runnable callback) {
	mModel.dateChanged(date, monitor, callback);
}
}
