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
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragSource;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.logging.Logger;

import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import tvbrowser.core.Settings;
import tvbrowser.core.plugin.PluginProxy;
import tvbrowser.core.plugin.PluginProxyManager;
import tvbrowser.extras.common.InternalPluginProxyList;
import tvbrowser.ui.filter.dlgs.FilterButtons;
import tvbrowser.ui.mainframe.MainFrame;
import tvbrowser.ui.mainframe.actions.TVBrowserAction;
import tvbrowser.ui.settings.ToolBarDragAndDropSettings;
import util.ui.ChannelContextMenu;
import util.ui.PopupButton;
import util.ui.TVBrowserIcons;
import util.ui.UiUtilities;
import util.ui.persona.Persona;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import devplugin.Date;
import devplugin.Plugin;
import devplugin.ProgressMonitor;
import devplugin.SettingsItem;

public class ToolBar extends JToolBar {

  private static final util.ui.Localizer mLocalizer = util.ui.Localizer
      .getLocalizerFor(ToolBar.class);

  private static final Logger mLog = java.util.logging.Logger
  .getLogger(ToolBar.class.getName());

  public static final String ACTION_VALUE = "ActionValue";
  public static final String ACTION_TYPE_KEY = "ActionType";
  public static final String ACTION_ID_KEY = "ActionId";
  public static final String ACTION_IS_SELECTED = "ActionIsSelected";

  /**
   * toolbar button with standard click behavior
   */
  public static final int BUTTON_ACTION = 0;
  /**
   * toolbar button with toggle behavior (i.e. on/off state)
   */
  public static final int TOOGLE_BUTTON_ACTION = 1;
  protected static final int SEPARATOR = 2;
  protected static final int SPACE = 3;
  protected static final int GLUE = 4;

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
  private JLabel mStatusLabel;

  public ToolBar(ToolBarModel model, JLabel statusLabel) {
    super();
    mStatusLabel = statusLabel;
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
        add(new ToolBarSeparatorPanel());
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
    
    if(Persona.getInstance().getHeaderImage() != null) {
      setOpaque(false);
    }
    
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
    final JToggleButton button = new JToggleButton() {
      protected void paintComponent(Graphics g) {
        if(Persona.getInstance().getHeaderImage() != null && Persona.getInstance().getTextColor() != null && Persona.getInstance().getShadowColor() != null) {
          if(UIManager.getLookAndFeel().getClass().getCanonicalName().equals("com.sun.java.swing.plaf.gtk.GTKLookAndFeel")) {
            if(isBorderPainted()) {
              g.setColor(UIManager.getColor("List.selectionBackground"));
              g.fillRect(0, 0, getWidth(), getHeight());
            }
            if(isSelected()) {
              g.draw3DRect(0,0,getWidth(),getHeight(),false);
            }
          }
          
          if(Settings.propToolbarButtonStyle.getString().equals("text&icon")) {
            getIcon().paintIcon(this,g,getWidth()/2-getIcon().getIconWidth()/2,getInsets().top);
          }
          
          if(Settings.propToolbarButtonStyle.getString().contains("text")) {
            FontMetrics metrics = g.getFontMetrics(getFont());
            int textWidth = metrics.stringWidth(getText());
          
            if(!Persona.getInstance().getShadowColor().equals(Persona.getInstance().getTextColor())) {
              g.setColor(Persona.getInstance().getShadowColor());
              
              g.drawString(getText(),getWidth()/2-textWidth/2+1,getHeight()-getInsets().bottom-getInsets().top+1);
              g.drawString(getText(),getWidth()/2-textWidth/2+2,getHeight()-getInsets().bottom-getInsets().top+2);
            }
            
            g.setColor(Persona.getInstance().getTextColor());
            g.drawString(getText(),getWidth()/2-textWidth/2,getHeight()-getInsets().bottom-getInsets().top);
          }
          else {
            super.paintComponent(g);
          }
        }
        else {
          super.paintComponent(g);
        }
      }
    };
    button.setOpaque(false);
    button.setAlignmentX(Component.CENTER_ALIGNMENT);

    action.putValue(ACTION_VALUE, button);
    addButtonProperties(button, action);
    Boolean isSelected = (Boolean) action.getValue(ACTION_IS_SELECTED);
    if (isSelected != null) {
      button.setSelected(isSelected.booleanValue());
    }
    button.setBorderPainted(isSelected != null && isSelected.booleanValue());
    
    button.addChangeListener(new ChangeListener() {      
      @Override
      public void stateChanged(ChangeEvent e) {
        if(!button.isFocusOwner() && button.getToolTipText() != null && button.getToolTipText().equals(mStatusLabel.getText())) {
          mStatusLabel.setText("");
        }
      }
    });
    
    button.addMouseListener(new MouseAdapter() {
      public void mouseEntered(MouseEvent e) {
        if (!button.isSelected()) {
          button.setBorderPainted(true);
        }
        if(button.getToolTipText() != null && button.getToolTipText().trim().length() > 0) {
          mStatusLabel.setText(button.getToolTipText());
        }
      }

      public void mouseExited(MouseEvent e) {
        if (!button.isSelected()) {
          button.setBorderPainted(false);
        }
        if(button.getToolTipText() != null && button.getToolTipText().equals(mStatusLabel.getText())) {
          mStatusLabel.setText("");
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
    final JButton button = new PopupButton() {
      protected void paintComponent(Graphics g) {
        if(Persona.getInstance().getHeaderImage() != null && Persona.getInstance().getTextColor() != null && Persona.getInstance().getShadowColor() != null) {
          if(isBorderPainted() && UIManager.getLookAndFeel().getClass().getCanonicalName().equals("com.sun.java.swing.plaf.gtk.GTKLookAndFeel")) {
            g.setColor(UIManager.getColor("List.selectionBackground"));
            g.fillRect(0, 0, getWidth(), getHeight());
          }
          
          if(Settings.propToolbarButtonStyle.getString().equals("text&icon")) {
            getIcon().paintIcon(this,g,getWidth()/2-getIcon().getIconWidth()/2,getInsets().top);
          }
          
          if(Settings.propToolbarButtonStyle.getString().contains("text")) {
            FontMetrics metrics = g.getFontMetrics(getFont());
            int textWidth = metrics.stringWidth(getText());
          
            if(!Persona.getInstance().getShadowColor().equals(Persona.getInstance().getTextColor())) {
              g.setColor(Persona.getInstance().getShadowColor());
              
              g.drawString(getText(),getWidth()/2-textWidth/2+1,getHeight()-getInsets().bottom-getInsets().top+1);
              g.drawString(getText(),getWidth()/2-textWidth/2+2,getHeight()-getInsets().bottom-getInsets().top+2);
            }
            
            g.setColor(Persona.getInstance().getTextColor());
            g.drawString(getText(),getWidth()/2-textWidth/2,getHeight()-getInsets().bottom-getInsets().top);
          }
          else {
            super.paintComponent(g);
          }
        }
        else {
          super.paintComponent(g);
        }
      }
    };
    
    addButtonProperties(button, action);
    button.setBorderPainted(false);
    button.setAlignmentX(Component.CENTER_ALIGNMENT);
    action.putValue(ACTION_VALUE, button);

    if(action.equals(((DefaultToolBarModel)mModel).getUpdateAction())) {
      mUpdateButton = button;
      mUpdateButton.setOpaque(false);
    }

    button.addChangeListener(new ChangeListener() {      
      @Override
      public void stateChanged(ChangeEvent e) {
        if(!button.isFocusOwner() && button.getToolTipText() != null && button.getToolTipText().equals(mStatusLabel.getText())) {
          mStatusLabel.setText("");
        }
      }
    });
    
    button.addMouseListener(new MouseAdapter() {
      public void mouseEntered(MouseEvent e) {
        button.setBorderPainted(true);
        
        if(button.getToolTipText() != null && button.getToolTipText().trim().length() > 0) {
          mStatusLabel.setText(button.getToolTipText());
        }
      }

      public void mouseExited(MouseEvent e) {
        button.setBorderPainted(false);
        
        if(button.getToolTipText() != null && button.getToolTipText().equals(mStatusLabel.getText())) {
          mStatusLabel.setText("");
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

    action.addPropertyChangeListener(new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals("enabled")) {
          button.setEnabled(action.isEnabled());
        }
      }
    });

    add(button);
  }

  private void showPopupMenu(MouseEvent e) {
    JPopupMenu menu = new JPopupMenu();
    String label = mLocalizer.ellipsisMsg("configure", "Configure");
    String name = null;
    boolean configItemEnabled = false;

    if (e.getSource() instanceof AbstractButton) {
      name = ((AbstractButton) e.getSource()).getName();

      if (name.startsWith("#scrollTo") && name.indexOf("Channel") == -1) {
        configItemEnabled = true;
        label = mLocalizer.ellipsisMsg("configureTime", "Configure time buttons");
      }
      else if(name.startsWith("#filter")) {
        configItemEnabled = true;
        label = FilterButtons.mLocalizer.ellipsisMsg("createFilter", "Create filter");
      }
      else if(name.startsWith("#scrollToChannel")) {
        configItemEnabled = true;
        label = ChannelContextMenu.mLocalizer.ellipsisMsg("addChannels", "Add/Remove channels");
      }
      else if (name.indexOf("##") != -1) {
        PluginProxy plugin = PluginProxyManager.getInstance().getActivatedPluginForId(name.substring(0,name.indexOf("##")));
        configItemEnabled = plugin != null && plugin.getSettingsTab() != null;
      }
      else if (PluginProxyManager.getInstance().getActivatedPluginForId(name) != null) {
        configItemEnabled = PluginProxyManager.getInstance().getActivatedPluginForId(name).getSettingsTab() != null;
      }
      else if (InternalPluginProxyList.getInstance().getProxyForId(name) != null) {
        configItemEnabled = InternalPluginProxyList.getInstance().getProxyForId(name).getSettingsTab() != null;
        name = InternalPluginProxyList.getInstance().getProxyForId(name).getSettingsId();
      }
    } else {
      return;
    }


    JMenuItem item = new JMenuItem(label);
    item.setActionCommand(name);
    item.setEnabled(configItemEnabled);

    item.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().startsWith("#scrollTo") && e.getActionCommand().indexOf("Channel") == -1) {
          MainFrame.getInstance().showSettingsDialog("#timebuttons");
        } else if (e.getActionCommand().startsWith("#filter")) {
          MainFrame.getInstance().showFilterDialog();
        } else if (e.getActionCommand().startsWith("#scrollToChannel")) {
          MainFrame.getInstance().showSettingsDialog(SettingsItem.CHANNELS);
        } else if (e.getActionCommand().indexOf("##") != -1){
          MainFrame.getInstance().showSettingsDialog(e.getActionCommand().substring(0,e.getActionCommand().indexOf("##")));
        } else {
          MainFrame.getInstance().showSettingsDialog(e.getActionCommand());
        }
      }
    });
    menu.add(item);
    menu.addSeparator();

    item = new JMenuItem(mLocalizer.msg("removeButton", "Remove button"));
    final String buttonName = name;
    item.addActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent e) {
        Action[] actions = mModel.getActions();
        ArrayList<String> idList = new ArrayList<String>();
        
        for(Action a : actions) {
          String key = (String)a.getValue(ACTION_ID_KEY);
          String test = key;
          
          if (test.equals("searchplugin.SearchPlugin") && buttonName.equals("#search")) {
            test = "#search";
          } else if (test.equals("reminderplugin.ReminderPlugin") && buttonName.equals("#reminder")) {
            test = "#reminder";
          } else if (test.equals("favoritesplugin.FavoritesPlugin") && buttonName.equals("#favorite")) {
            test = "#favorite";
          }
          
          if (test.compareTo(buttonName) != 0) {
            idList.add(key);
          }
        }

        String[] ids = new String[idList.size()];
        idList.toArray(ids);
        DefaultToolBarModel.getInstance().setButtonIds(ids);
        MainFrame.getInstance().updateToolbar();
        Settings.propToolbarButtons.setStringArray(ids);
      }
    });
    
    menu.add(item);
    menu.add(ContextMenu.getSubMenu());

    menu.show(e.getComponent(), e.getX(), e.getY());
  }

  private void addButtonProperties(final AbstractButton button,
      final Action action) {
    String tooltip = (String) action.getValue(Action.SHORT_DESCRIPTION);
    Icon icon = getIcon(action);
    String title = getTitle(action);
    
    if(Persona.getInstance().getHeaderImage() != null) {
      button.setOpaque(false);
    }
    
    button.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        action.actionPerformed(new ActionEvent(action,ActionEvent.ACTION_PERFORMED,""));
        final AbstractButton btn = (AbstractButton) action.getValue(ToolBar.ACTION_VALUE);

        MainFrame.getInstance().getProgramTableScrollPane().requestFocusInWindow();
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
        if (icon == null) {
          mLog.warning("Big icon missing for action " + action.getValue(Action.NAME));
          icon = (Icon) action.getValue(Action.SMALL_ICON);
        }
        if ((icon != null)
            && ((icon.getIconHeight() != TVBrowserIcons.SIZE_LARGE) || (icon.getIconWidth() != TVBrowserIcons.SIZE_LARGE))) {
          icon = UiUtilities.scaleIcon(icon, TVBrowserIcons.SIZE_LARGE, TVBrowserIcons.SIZE_LARGE);
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

  /**
   * This ToolBarSeparatorPanel tracks component changes
   * to dynamically adjust it's size.
   *
   * @author Torsten Keil
   * @since 2.7
   */
  private class ToolBarSeparatorPanel extends JPanel implements ContainerListener {

    public ToolBarSeparatorPanel() {
      super();
      initUI();
    }

    /**
     * Use this method to easily change access to the tool bar.
     *
     * @return
     */
    private ToolBar getToolBar() {
      return ToolBar.this;
    }

    private void initUI() {
      String toolbarLocation = getToolBar().getToolbarLocation();
      if(BorderLayout.NORTH.equals(toolbarLocation)) {
        setLayout(new FormLayout("0dlu:grow,default,0dlu:grow","fill:default:grow"));
      } else {
        setLayout(new FormLayout("default:grow","0dlu:grow,default,0dlu:grow"));
      }

      setOpaque(false);
      setBorder(BorderFactory.createEmptyBorder());
      setAlignmentX(Component.CENTER_ALIGNMENT);

      if(BorderLayout.NORTH.equals(toolbarLocation)) {
        add(new JSeparator(SwingConstants.VERTICAL), new CellConstraints().xy(2,1));
      } else {
        add(new JSeparator(SwingConstants.HORIZONTAL), new CellConstraints().xy(1,2));
      }

      adjustSize();

      ToolBar.this.addContainerListener(this);
    }

    public void componentRemoved(ContainerEvent e) {
      adjustSize();
    }

    public void componentAdded(ContainerEvent e) {
      adjustSize();
    }

    /**
     * Adjust the size if changed.
     */
    private void adjustSize() {
      ToolBar toolBar = getToolBar();
      String toolbarLocation = toolBar.getToolbarLocation();
      Dimension preferredSize = toolBar.getPreferredSize();
      int height = (int)((BorderLayout.NORTH.equals(toolbarLocation) ? preferredSize.height : 20) * 0.85);
      int width = (int)((BorderLayout.NORTH.equals(toolbarLocation) ? 10 : preferredSize.width) * 0.85);
      Dimension actualSize = getSize();
      Dimension newSize = new Dimension(width,height);
      if (!actualSize.equals(newSize)) {
        setPreferredSize(newSize);
        setMaximumSize(newSize);
        setMinimumSize(newSize);
      }
    }
  }
  
  public void showPopupMenu(TVBrowserAction tvBrowserAction) {
    ((DefaultToolBarModel)mModel).showPopupMenu(tvBrowserAction);
  }
  
  public void updatePersona() {
    if(Persona.getInstance().getHeaderImage() != null) {
      setOpaque(false);
    }
    else {
      setOpaque(true);
    }
    
    update();
  }
  
  protected void paintComponent(Graphics g) {
    if(!UIManager.getLookAndFeel().getClass().getCanonicalName().equals("com.sun.java.swing.plaf.gtk.GTKLookAndFeel") || Persona.getInstance().getHeaderImage() == null) {
      super.paintComponent(g);
    }
  }
}
