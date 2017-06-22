/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (martin_oat@yahoo.de)
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
package util.ui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPopupMenu;
import javax.swing.ListModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import devplugin.ContextMenuIf;
import devplugin.Date;
import devplugin.Plugin;
import devplugin.PluginManager;
import devplugin.Program;
import tvbrowser.core.plugin.PluginProxy;
import tvbrowser.core.plugin.PluginProxyManager;
import tvbrowser.core.plugin.PluginStateListener;
import util.exc.TvBrowserException;
import util.programkeyevent.ProgramKeyAndContextMenuListener;
import util.programkeyevent.ProgramKeyEventHandler;
import util.programmouseevent.ProgramMouseAndContextMenuListener;
import util.programmouseevent.ProgramMouseEventHandler;
import util.settings.PluginPictureSettings;
import util.settings.ProgramPanelSettings;

/**
 * This Class extends a JList for showing Programs
 */
public class ProgramList extends JList<Object> implements ChangeListener,
    ListDataListener, PluginStateListener, ProgramMouseAndContextMenuListener, 
    ProgramKeyAndContextMenuListener {
  private final static Localizer mLocalizer = Localizer.getLocalizerFor(ProgramList.class);
  
  /** Key for separator list entry */
  public final static String DATE_SEPARATOR = "DATE_SEPARATOR";

  private Vector<Program> mPrograms = new Vector<Program>();
  private boolean mSeparatorsCreated = false;
  
  private ProgramMouseEventHandler mMouseEventHandler;
  private ContextMenuIf mCaller;
  private ProgramKeyEventHandler mKeyEventHandler;
  private JPopupMenu mPopupMenu;

  /**
   * Creates the JList and adds the default MouseListeners (PopUpBox)
   * 
   * @param programArr
   *          Array of Programs to show
   */
  public ProgramList(Vector<Program> programArr) {
    this(programArr, new PluginPictureSettings(
        PluginPictureSettings.ALL_PLUGINS_SETTINGS_TYPE));
  }

  /**
   * Creates the JList and adds the default MouseListeners (PopUpBox)
   * 
   * @param programArr
   *          Array of Programs to show
   */
  public ProgramList(Program[] programArr) {
    this(programArr, new PluginPictureSettings(
        PluginPictureSettings.ALL_PLUGINS_SETTINGS_TYPE));
  }

  /**
   * Creates the JList and adds the default MouseListeners (PopUpBox)
   * 
   * @param programs
   *          Model with Programs to show
   */
  public ProgramList(ListModel<Object> programs) {
    this(programs, new PluginPictureSettings(
        PluginPictureSettings.ALL_PLUGINS_SETTINGS_TYPE));
  }
  
  /**
   * Creates the JList and adds the default MouseListeners (PopUpBox)
   * 
   * @param programVector
   *          Array of Programs to show
   * @param settings
   *          The settings for the program panel.
   * 
   * @since 2.2.2
   */
  public ProgramList(Vector<Program> programVector,
      ProgramPanelSettings settings) {
    super(programVector);
    initialize(settings);
  }

  private void initialize(ProgramPanelSettings settings) {
    setCellRenderer(new ProgramListCellRenderer(settings));
    setToolTipText("");
    UiUtilities.addKeyRotation(this);
  }

  /**
   * Creates the JList and adds the default MouseListeners (PopUpBox)
   * 
   * @param programArr
   *          Array of Programs to show
   * @param settings
   *          The settings for the program panel.
   * 
   * @since 2.2.2
   */
  public ProgramList(Program[] programArr, ProgramPanelSettings settings) {
    super(programArr);
    initialize(settings);
  }

  /**
   * Creates the JList and adds the default MouseListeners (PopUpBox)
   * 
   * @param programs
   *          Model with Programs to show
   * @param settings
   *          The settings for the program panel.
   * 
   * @since 2.2.2
   */
  public ProgramList(ListModel<Object> programs, ProgramPanelSettings settings) {
    super(programs);
    programs.addListDataListener(this);
    initialize(settings);
  }

  /**
   * Creates the JList and adds the default MouseListeners (PopUpBox)
   * 
   * @param programVector
   *          Array of Programs to show
   * @param settings
   *          The plugin picture settings for the program panel.
   * 
   * @since 2.6
   */
  public ProgramList(Vector<Program> programVector,
      PluginPictureSettings settings) {
    this(programVector, new ProgramPanelSettings(settings, false));
  }

  /**
   * Creates the JList and adds the default MouseListeners (PopUpBox)
   * 
   * @param programArr
   *          Array of Programs to show
   * @param settings
   *          The plugin picture settings for the program panel.
   * 
   * @since 2.6
   */
  public ProgramList(Program[] programArr, PluginPictureSettings settings) {
    this(programArr, new ProgramPanelSettings(settings, false));
  }
  
  /**
   * Creates the JList and adds the default MouseListeners (PopUpBox)
   * 
   * @param programs
   *          Model with Programs to show
   * @param settings
   *          The plugin picture settings for the program panel.
   * 
   * @since 2.6
   */
  public ProgramList(ListModel<Object> programs, PluginPictureSettings settings) {
    this(programs, new ProgramPanelSettings(settings, false));
  }
  
  /**
   * Add a ChangeListener to all Programs for repainting
   */
  public void addNotify() {
    super.addNotify();
    removeFromPrograms();
    addToPrograms();
    PluginProxyManager.getInstance().addPluginStateListener(this);
  }

  /**
   * Remove a ChangeListener to all Programs
   */
  public void removeNotify() {
    super.removeNotify();
    removeFromPrograms();
    PluginProxyManager.getInstance().removePluginStateListener(this);
  }

  private void removeFromPrograms() {
    removeFromPrograms(0, mPrograms.size() - 1);
  }

  private void removeFromPrograms(int indexFirst, int indexLast) {
    synchronized(mPrograms) {
      while(indexFirst >= mPrograms.size()) {
        indexFirst = mPrograms.size() - 1;
      }
      while(indexLast >= mPrograms.size()) {
        indexLast = mPrograms.size() - 1;
      }
      if(indexFirst >= 0) {
        for (int i = indexLast; i >= indexFirst; i--) {
          mPrograms.remove(i).removeChangeListener(this);
        }
      }
    }
  }

  private void addToPrograms() {
    ListModel<Object> list = getModel();
    
    synchronized (list) {
      addToPrograms(0, list.getSize() - 1, list);
    }
  }

  private void addToPrograms(int indexFirst, int indexLast, ListModel<Object> list) {
    if(list.getSize() > indexLast) {
      for (int i = indexFirst; i <= indexLast; i++) {
        Object element = list.getElementAt(i);
        if (element instanceof Program) {
          Program prg = (Program) element;
          prg.addChangeListener(this);
          mPrograms.add(prg);
        }
      }
    }
  }
  
  /**
   * Add a Mouse-Listener for the Popup-Box
   * 
   * The caller ContextMenuIfs menus are not shown, if you want to have all
   * available menus just use <code>null</code> for caller.
   * 
   * @param caller
   *          The ContextMenuIf that called this.
   * @deprecated since 3.3.1 use {@link #addMouseAndKeyListeners(ContextMenuIf)} instead.
   */
  public void addMouseListeners(final ContextMenuIf caller) {
    addMouseAndKeyListeners(caller);
  }
  
  /**
   * Add a Mouse-Listener for the Popup-Box
   * 
   * The caller ContextMenuIfs menus are not shown, if you want to have all
   * available menus just use <code>null</code> for caller.
   * 
   * @param caller
   *          The ContextMenuIf that called this.
   * @since 3.3.1
   */
  public void addMouseAndKeyListeners(final ContextMenuIf caller) {
    if(mMouseEventHandler == null) {
      mMouseEventHandler = new ProgramMouseEventHandler(this, caller);
      addMouseListener(mMouseEventHandler);
      mCaller = caller;
    }
    if(mKeyEventHandler == null) {
      mCaller = caller;
      mKeyEventHandler = new ProgramKeyEventHandler(this, caller);
      
      addKeyListener(mKeyEventHandler);
    }
  }

  /**
   * Shows the Popup
   * 
   * @param e
   *          MouseEvent for X/Y Coordinates
   * @param caller
   *          The ContextMenuIf that called this
   */
  private void showPopup(Point p, ContextMenuIf caller) {
    if(mPopupMenu != null && mPopupMenu.isVisible()) {
      mPopupMenu.setVisible(false);      
    }
    else {
      PluginManager mng = Plugin.getPluginManager();
  
      int inx = locationToIndex(p);
      setSelectedIndex(inx);
  
      if (getModel().getElementAt(inx) instanceof Program) {
        Program prog = (Program) getModel().getElementAt(inx);
        mPopupMenu = mng.createPluginContextMenu(prog, caller);
        UiUtilities.registerForClosing(mPopupMenu);
        mPopupMenu.show(ProgramList.this, p.x - 15, p.y - 15);
      }
    }
  }

  public void stateChanged(ChangeEvent e) {
    repaint();
  }

  public void contentsChanged(ListDataEvent e) {
    removeFromPrograms();
    addToPrograms();
  }

  public void intervalAdded(ListDataEvent e) {
    ListModel<Object> list = getModel();
    
    synchronized (list) {
      addToPrograms(e.getIndex0(), e.getIndex1(), list);
    }
  }

  public void intervalRemoved(ListDataEvent e) {
    removeFromPrograms(e.getIndex0(), e.getIndex1());
  }

  /**
   * @return The selected programs;
   * @since 2.2
   */
  public Program[] getSelectedPrograms() {
    List<Object> o = getSelectedValuesList();

    if (o == null || o.size() == 0) {
      return null;
    }

    if(mSeparatorsCreated) {
      ArrayList<Program> progs = new ArrayList<Program>(o.size());
      
      for(Object p : o) {
        if(p instanceof Program) {
          progs.add((Program)p);
        }
      }
      
      return progs.toArray(new Program[progs.size()]);
    }
    else {
      Program[] p = new Program[o.size()];
      
      for (int i = 0; i < o.size(); i++) {
        p[i] = (Program) o.get(i);
      }
  
      return p;
    }
  }
  
  public void pluginActivated(PluginProxy plugin) {
    if (plugin.getProgramTableIcons(Plugin.getPluginManager().getExampleProgram()) != null) {
      updatePrograms();
    }
  }

  public void pluginDeactivated(PluginProxy plugin) {
    updatePrograms();
  }

  private void updatePrograms() {
    repaint();
  }

  public void pluginLoaded(PluginProxy plugin) {
    // noop
  }

  public void pluginUnloaded(PluginProxy plugin) {
    // noop
  }

  @Override
  public String getToolTipText(MouseEvent event) {
    final Point point = event.getPoint();
    int index = locationToIndex(point);
    if (index >= 0) {
      Rectangle bounds = getCellBounds(index, index);
      if (bounds != null) {
        int x = point.x - bounds.x;
        int y = point.y - bounds.y;
        Component component = getCellRenderer()
            .getListCellRendererComponent(this, getModel().getElementAt(index),
                index, false, false);
        if (component != null && component instanceof Container) {
          Container container = (Container) component;
          component = container.getComponent(1);
          if (component != null && component instanceof ProgramPanel) {
            ProgramPanel panel = (ProgramPanel) component;
            x -= panel.getX();
            y -= panel.getY();
            return panel.getToolTipText(x, y);
          }
        }
      }
    }
    // mouse is over an empty part of the list
    return null;
  }
  
  
  /**
   * Adds date separators to this list.
   * This needs to be called every time the list elements are changed.
   * <p>
   * @throws TvBrowserException Thrown if used ListModel is not {@link javax.swing.DefaultListModel} or a child class of it.
   * @since 3.2.2
   */
  public void addDateSeparators() throws TvBrowserException {
    if(getModel() instanceof DefaultListModel) {
      mSeparatorsCreated = true;
      
      DefaultListModel<Object> newModel = new DefaultListModel<>();
      
      Program previous = null;
      
      for(int i = 0; i < getModel().getSize(); i++) {
        Object o = getModel().getElementAt(i);
        
        if(o instanceof Program) {
          Program prog = (Program) o;
          
          if(previous == null || prog.getDate().compareTo(previous.getDate()) > 0) {
            newModel.addElement(DATE_SEPARATOR);
          }
          
          newModel.addElement(prog);
          
          previous = prog;
        }
      }
      
      super.setModel(newModel);
    }
    else {
      throw new TvBrowserException(ProgramList.class, "unsupportedListModel", "Used ListModel not supported.");
    }
  }
  
  public void setModel(ListModel<Object> model) {
    mSeparatorsCreated = false;
    super.setModel(model);
  }
  
  /**
   * Scrolls the list to given date (if date is available)
   * <p>
   * @param date The date to scroll to.
   * @since 3.3.4
   */
  public void scrollToNextDateIfAvailable(Date date) {
    for(int i = 0; i < super.getModel().getSize(); i++) {
      Object test = super.getModel().getElementAt(i);
      
      if(test instanceof Program && date.compareTo(((Program)test).getDate()) == 0) {
        Point p = indexToLocation(i-(mSeparatorsCreated ? 1 : 0));
        
        if(getVisibleRect() != null) {
          super.scrollRectToVisible(new Rectangle(p.x,p.y,1,getVisibleRect().height));
          repaint();
        }
        return;
      }
    }
  }
  

  /**
   * Scrolls the list to the first occurrence of the given time from the current view
   * backward if time is smaller than the current views first time, forward if time is
   * bigger than the current views first time. 
   * <p>
   * @param time The time in minutes from midnight to scroll to.
   * @since 3.3.4
   */
  public void scrollToTimeFromCurrentViewIfAvailable(int time) {
    int index = locationToIndex(getVisibleRect().getLocation());
    
    if(index < getModel().getSize() - 1) {
      Object o = super.getModel().getElementAt(index);
      
      if(o instanceof String) {
        o = super.getModel().getElementAt(index+1);
        index++;
      }
      
      if(index < super.getModel().getSize() - 1) {
        Date current = ((Program)o).getDate();
        
        int i = index + 1;
        
        boolean down = time <= ((Program)o).getStartTime();
        
        if(down) {
          i = index - 1;
        }
        
        Point scrollPoint = null;
        
        if(down && ((Program)o).getStartTime() == time) {
          if(i > 0 && (getModel().getElementAt(i) instanceof String) ) {
            scrollPoint = indexToLocation(i);
          }
          else {
            scrollPoint = indexToLocation(i+1);
          }
        }
        
        while(down ? i >= 0 : i < super.getModel().getSize()) {
          Object test = super.getModel().getElementAt(i);
          
          if(test instanceof Program) {
            Program prog = (Program)test;
            int startTime = prog.getStartTime();
            
            if(prog.getDate().compareTo(current) == 0) {
              if((down ? startTime < time : startTime >= time)) {
                if(scrollPoint == null) {
                  if(i > 0 && (getModel().getElementAt(i-1) instanceof String) || startTime > time) {
                    scrollPoint = indexToLocation(i - 1);
                  }
                  else {
                    scrollPoint = indexToLocation(i);
                  }
                }
                break;
              }
              else if(down && startTime == time) {
                if(i > 0 && getModel().getElementAt(i-1) instanceof String) {
                  scrollPoint = indexToLocation(i - 1);
                  break;
                }
                else {
                  scrollPoint = indexToLocation(i);
                }
              }
            }
            else if(scrollPoint == null) {
              if(down && i < getModel().getSize()-1) {
                scrollPoint = indexToLocation(i+1);
              }
              else if(!down && i > 0) {
                scrollPoint = indexToLocation(i-1);
              }
              else {
                scrollPoint = indexToLocation(i);
              }
              
              break;
            }
          }
          else if(test instanceof String && scrollPoint == null) {
            if(down || i == 0) {
              scrollPoint = indexToLocation(i);
            }
            else {
              scrollPoint = indexToLocation(i-1);
            }
            
            break;
          }
          
          if(down) {
            i--;
          }
          else {
            i++;
          }
        }
        
        if(scrollPoint == null) {
          if(down) {
            if(getModel().getSize() > 0) {
              scrollPoint = indexToLocation(0);
            }
          }
          else {
            if(getModel().getSize() > 0) {
              scrollPoint = indexToLocation(getModel().getSize()-1);
            }
          }
        }
        
        if(scrollPoint != null && getVisibleRect() != null) {
          super.scrollRectToVisible(new Rectangle(scrollPoint.x,scrollPoint.y,1,getVisibleRect().height));
          repaint();
        }
      }
    }
  }
  
  /**
   * Scrolls the list to the first occurrence of the given time from the current view onward (if time is available)
   * <p>
   * @param time The time in minutes from midnight.
   * @since 3.3.4
   */
  public void scrollToFirstOccurrenceOfTimeFromCurrentViewOnwardIfAvailable(int time) {
    int index = locationToIndex(getVisibleRect().getLocation());
    
    if(index < getModel().getSize() - 1) {
      Object o = super.getModel().getElementAt(index);
      
      if(o instanceof String) {
        o = super.getModel().getElementAt(index+1);
        index++;
      }
      
      if(index < super.getModel().getSize() - 1 && ((Program)o).getStartTime() != time) {
        Date current = ((Program)o).getDate();
                
        if(((Program)o).getStartTime() > time) {
          time += 1440;
        }
        
        for(int i = index + 1; i < super.getModel().getSize(); i++) {
          Object test = super.getModel().getElementAt(i);
          
          if(test instanceof Program) {
            Program prog = (Program)test;
            int startTime = prog.getStartTime();
                        
            if(prog.getDate().compareTo(current) > 0) {
              startTime += 1440;
            }
            
            if(prog.getDate().compareTo(current) >= 0 && startTime >= time) {
              Point p = indexToLocation(i);
              
              if(i > 0 && (getModel().getElementAt(i-1) instanceof String) || startTime > time) {
                p = indexToLocation(i - 1);
              }
              
              if(getVisibleRect() != null) {
                super.scrollRectToVisible(new Rectangle(p.x,p.y,1,getVisibleRect().height));
                repaint();
              }
              
              return;
            }
          }
        }            
      }
    }
  }
  
  /**
   * Scrolls the list to next day from
   * the current view position (if next
   * day is available)
   * <p>
   * @since 3.2.2
   */
  public void scrollToNextDayIfAvailable() {
    int index = locationToIndex(getVisibleRect().getLocation());
    
    if(index < getModel().getSize() - 1) {
      Object o = super.getModel().getElementAt(index);
      
      if(o instanceof String) {
        o = super.getModel().getElementAt(index+1);
        index++;
      }
      
      if(index < super.getModel().getSize() - 1) {
        Date current = ((Program)o).getDate();
        
        for(int i = index + 1; i < super.getModel().getSize(); i++) {
          Object test = super.getModel().getElementAt(i);
          
          if(test instanceof Program && current.compareTo(((Program)test).getDate()) < 0) {
            Point p = indexToLocation(i-(mSeparatorsCreated ? 1 : 0));
            
            if(getVisibleRect() != null) {
              super.scrollRectToVisible(new Rectangle(p.x,p.y,1,getVisibleRect().height));
            }
            
            return;
          }
        }            
      }
    }
  }
  
  /**
   * Scrolls the list to previous day from
   * the current view position (if previous
   * day is available)
   * <p>
   * @since 3.2.2
   */
  public void scrollToPreviousDayIfAvailable() {
    int index = locationToIndex(getVisibleRect().getLocation())-1;
    
    if(index > 0) {
      Object o = super.getModel().getElementAt(index);
      
      if(o instanceof String) {
        o = super.getModel().getElementAt(index-1);
        index--;
      }
      
      if(index > 0) {
        Date current = ((Program)o).getDate();
        
        for(int i = index-1; i >= 0; i--) {
          Object test = super.getModel().getElementAt(i);
          
          if(test instanceof Program && current.compareTo(((Program)test).getDate()) > 0) {
            super.ensureIndexIsVisible(i+1);
            return;
          }
        }
      }
    }
    
    if(getModel().getSize() > 0) {
      super.ensureIndexIsVisible(0);
    }
  }
  
  /**
   * Gets the new index of a row after adding of date separators.
   * <p>
   * @param index The old index of the row.
   * @return The new index or the given index if no separators were added.
   * @since 3.2.2
   */
  public int getNewIndexForOldIndex(int index) {
    if(mSeparatorsCreated) {
      for(int i = 0; i < Math.min(index,mPrograms.size()); i++) {
        if(getModel().getElementAt(i) instanceof String) {
          index++;
        }
      }
    }
    
    return index;
  }
  
  /**
   * @return The tool tip text for the previous scroll action,
   */
  public static String getPreviousActionTooltip() {
    return mLocalizer.msg("prevTooltip", "Scrolls to previous day from current view position (if there is previous day in the list)");
  }
  
  /**
   * @return The tool tip text for the next scroll action,
   */
  public static String getNextActionTooltip() {
    return mLocalizer.msg("nextTooltip", "Scrolls to next day from current view position (if there is next day in the list)");
  }

  @Override
  public Program getProgramForMouseEvent(MouseEvent e) {
    final int inx = locationToIndex(e.getPoint());
    
    if (inx >= 0) {
      final Object element = ProgramList.this.getModel()
      .getElementAt(inx);
      
      if(element instanceof Program) {
        return (Program) element;
      }
      else if (SwingUtilities.isLeftMouseButton(e) && element instanceof String && getSelectedIndices().length == 1) {
        setSelectedIndex(inx);
      }
    }
    
    return null;
  }

  @Override
  public void mouseEventActionFinished() {}

  @Override
  public void showContextMenu(MouseEvent e) {
    showPopup(e.getPoint(), mCaller);
  }

  @Override
  public Program getProgramForKeyEvent(KeyEvent e) {
    Object program = getSelectedValue();
    
    if(program instanceof Program) {
      return (Program)program;
    }
    
    return null;
  }

  @Override
  public void keyEventActionFinished() {}

  @Override
  public void showContextMenu(Program program) {
    Point p = indexToLocation(getSelectedIndex());
    Rectangle r = getCellBounds(getSelectedIndex(), getSelectedIndex());
    p.x += (int)(r.width*0.2f);
    p.y += (int)(r.height*2/3f);
    showPopup(p, mCaller);
  }
    
  @Override
  public void setSelectedIndex(int row) {
    if(getModel().getSize() > 0) {
      int index = getSelectedIndex();
      
      if(index-1 == row) {
        index = -1;
      }
      else {
        index = 1;
      }
      
      if(row == 0 && getSelectedIndex() == getModel().getSize()-1) {
        index = 1;
      }
      
      if(row < 0) {
        row = getModel().getSize()-1;
      }
      else if(row > getModel().getSize()-1) {
        row = 0;
      }
      
      if(!(getModel().getElementAt(row) instanceof Program)) {
        setSelectedIndex(row + index);
      }
      else {
        super.setSelectedIndex(row);
        ensureIndexIsVisible(row);
      }
    }
  }
}