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

package util.ui.toolbar;

import java.awt.Component;
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.dnd.InvalidDnDOperationException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.util.HashMap;

import javax.swing.*;

import tvbrowser.ui.PictureButton;

public class ToolBar extends JToolBar implements ToolBarModelListener, MouseListener, DropTargetListener, DragGestureListener, ActionListener {
  
  public static int TEXT = 1, ICON = 2;  
    
  private ToolBarModel mModel;
  private HashMap mButtonsHash;
  private HashMap mVisibleItems;
  protected int mStyle = TEXT|ICON;
  private JLabel mLabel;
  
  public ToolBar(ToolBarModel model) {
    mModel = model;
    model.addToolBarModelListener(this);    
    addMouseListener(this);    
    mButtonsHash = new HashMap();
    mVisibleItems = new HashMap();
    updateAvailableItemsMap();
    updateContent();
    
    new DropTarget(this, DnDConstants.ACTION_MOVE, this, true);
  }
  
  public ToolBarModel getModel() {
    return mModel;
  }
  
  public void setStatusLabel(JLabel label) {
    mLabel = label;
  }

 
  public int getStyle() {
    return mStyle;
  }
  
  public void setStyle(int style) {
    mStyle = style;
    refresh();
  }
  
  public void refresh() {
    mButtonsHash.clear();
    updateContent();    
  }
  
  private void updateAvailableItemsMap() {
    mButtonsHash.clear();    
    ToolBarItem[] items = mModel.getAvailableItems();    
  }
  
  private ToolBarItem getToolBarItem(Component comp) {
    return (ToolBarItem)mVisibleItems.get(comp);
  }
  
  private void updateContent() {
    removeAll();
    mVisibleItems.clear();
    ToolBarItem[] items = mModel.getVisibleItems();
    for (int i=0; i<items.length; i++) {
      ToolBarItem item = items[i];
      if (item != null) {          
        if (item instanceof util.ui.toolbar.Separator) {
          addSeparator();          
        }
        else if (item instanceof ToolBarButton) {
          JButton btn = getButton((ToolBarButton)item);
          add(btn);
        }
        
        Component lastInsertedComponent = getComponentAtIndex(getComponentCount()-1);
        DragSource.getDefaultDragSource().createDefaultDragGestureRecognizer(lastInsertedComponent, DnDConstants.ACTION_MOVE, this);
        mVisibleItems.put(lastInsertedComponent, item);
        lastInsertedComponent.addMouseListener(this);
      }
    }
    updateUI();
  }
  
  public JButton getButton(ToolBarButton item) {
    JButton button = (JButton)mButtonsHash.get(item.getId());
    if (button != null) {
      return button;
    }
    
    String title = item.getName();
    Icon icon = item.getIcon();
    if ((mStyle & TEXT) != TEXT) {
      title = null;
    }
    if ((mStyle & ICON) != ICON) {
      icon = null;
    }
            
    button = new PictureButton(title, icon, item.getDescription(), mLabel);
    button.addActionListener(this);
    mButtonsHash.put(item.getId(), button);
    
    return button;
    
  }
  
  public void availableItemsChanged() {
    updateAvailableItemsMap();
  }

  public void visibleItemsChanged() {
    updateContent();
  }


  public void mouseClicked(MouseEvent event) {
      
    if (SwingUtilities.isRightMouseButton(event)) {
      Component comp = event.getComponent();
      ContextMenu menu = new ContextMenu(this, getToolBarItem(comp));
      menu.show(comp, event.getX()-15, event.getY()-15);
    }
  }

  public void mouseEntered(MouseEvent arg0) {
  }

  public void mouseExited(MouseEvent arg0) {
  }

  public void mousePressed(MouseEvent arg0) {
    
  }

  public void mouseReleased(MouseEvent arg0) {
    
  }


  public void dragGestureRecognized(DragGestureEvent event) {
    ToolBarItem item = getToolBarItem(event.getComponent());
    if (item != null) {
      try {         
        Transferable transferable = new StringTransferable(item.getId());
        event.startDrag(DragSource.DefaultMoveDrop, transferable);
      }catch( InvalidDnDOperationException idoe ) {
        System.err.println( idoe );
      }
    }
  }


  public void dragEnter(DropTargetDragEvent arg0) {
  }

  public void dragOver(DropTargetDragEvent arg0) {
  }

  public void dropActionChanged(DropTargetDragEvent arg0) {
  }

  public void drop(DropTargetDropEvent event) {
    event.acceptDrop(DnDConstants.ACTION_MOVE);   
    Point p = event.getLocation();
    System.out.println(p.getX()+" / "+p.getY());
    JComponent newPlaceComp = (JComponent)getComponentAt(p);
    
    
    
    DataFlavor[] flavors = event.getCurrentDataFlavors();  
    Transferable transferable = event.getTransferable();
    int i=0;
    DataFlavor flavor=null;
    while (i<flavors.length) {
      if (transferable.isDataFlavorSupported(flavors[i])) {
        flavor = flavors[i];
        break;
      }
      i++;       
    }
    if (flavor!=null) {
      int toInx = getComponentIndex(newPlaceComp);
           
      if (getOrientation()==HORIZONTAL) {
        if (p.x>newPlaceComp.getX()+newPlaceComp.getWidth()/2) {
          toInx++;
        }        
      }
      else {
        if (p.y>newPlaceComp.getY()+newPlaceComp.getHeight()/2) {
          toInx++;
        }  
      }
      
      
      String itemId=null;
    try {
        itemId = (String)transferable.getTransferData(flavor);
    } catch (UnsupportedFlavorException e) {
        e.printStackTrace();
    } catch (IOException e) {
        e.printStackTrace();
    }
    ToolBarItem[] items = mModel.getVisibleItems();
      int fromInx = -1;
      for (i=0; i<items.length; i++) {
        if (items[i].getId().equals(itemId)) {
          fromInx = i;
          break;
        }
      }
      if (fromInx>-1) {
        // we move the toolbar item from positon 'fromInx' to 'toInx'; 
        if (toInx == -1) {
          toInx = getComponentCount();
        }  
        ToolBarItem temp = items[fromInx];
        if (fromInx > toInx) {
          for (i=fromInx; i>toInx; i--) {
            items[i]=items[i-1];
          }
          items[toInx]=temp;
        }
        else if (fromInx < toInx) {
          for (i=fromInx; i<toInx-1; i++) {
              items[i]=items[i+1];
          }
          items[toInx-1]=temp;
        }     
        mModel.setVisibleItems(items);  
      }      
    }
     
    
  }


  public void dragExit(DropTargetEvent arg0) {
  }



  public void actionPerformed(ActionEvent event) {
    ToolBarItem item = getToolBarItem((JComponent)event.getSource());
    item.action(event);
  }
  
  
    
}




class StringTransferable implements Transferable {

  public static final DataFlavor localStringFlavor = DataFlavor.stringFlavor;
  public static final DataFlavor[] flavors = { localStringFlavor };
  private String mMsg;
    
  public StringTransferable(String msg) {
   mMsg = msg;
  }
    
  public DataFlavor[] getTransferDataFlavors() {
    return flavors;
  }

   
  public boolean isDataFlavorSupported(DataFlavor flavor) {
    return flavor.equals(localStringFlavor);
  }

  public synchronized Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
    if (isDataFlavorSupported(flavor)) {      
      return mMsg;
    }
    else {
      throw new UnsupportedFlavorException (flavor);
    } 
  }
}

