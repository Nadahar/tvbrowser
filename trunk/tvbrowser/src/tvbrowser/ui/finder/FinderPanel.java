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


 /**
  * TV-Browser
  * @author Martin Oberhauser
  */

package tvbrowser.ui.finder;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import devplugin.Date;
import tvbrowser.core.*;






class FinderItemRenderer extends DefaultListCellRenderer {
  
  private devplugin.Date mChoosenDate;
  private JProgressBar mProgressBar;
  private FinderItem mCurSelectedItem;
  
  public FinderItemRenderer() {
    mProgressBar=new JProgressBar();
  }
  
  public void chooseDate(devplugin.Date date) {
    mChoosenDate=date;
  }
 
  public void setSelectedItem(FinderItem item) {
    mCurSelectedItem=item;
  }
 
  public Component getListCellRendererComponent(JList list, Object value,
                int index, boolean isSelected, boolean cellHasFocus) {
     
    FinderItem comp = (FinderItem)value;
    
    if (cellHasFocus) {
          comp.setBorder(BorderFactory.createLineBorder(Color.black));
        }else{
          comp.setBorder(BorderFactory.createEmptyBorder(1,1,1,1));
        }
    
    
    if (value==mCurSelectedItem) {
      comp.setChoosen();  
    }    
    else if (isSelected) {
      comp.setSelected();          
    }
    else {
      comp.setOpaque(false);          
    }
    
    comp.setEnabled(TvDataBase.getInstance().dataAvailable(comp.getDate()));
    return comp;    
  }
 

} 

public class FinderPanel extends JScrollPane implements MouseListener, MouseMotionListener, KeyListener {
  
  private static FinderPanel mInstance;
  private DateListener mDateChangedListener;
  
  private JList mList;
  private DefaultListModel mModel;
  private FinderItemRenderer mRenderer;
  
  private int mCurMouseItemInx=-1;
  private Date mCurChoosenDate;
  
  private FinderPanel() {
    
    mModel=new DefaultListModel();
    mList=new JList(mModel);
    mList.setOpaque(false);
    
    mRenderer=new FinderItemRenderer();
    mList.setCellRenderer(mRenderer);
    
    setViewportView(mList);
    Date date=Date.getCurrentDate();
    for (int i=0;i<28;i++) {
      mModel.addElement(new FinderItem(mList,date.addDays(i)));
    }
    
    mList.addMouseMotionListener(this);
    mList.addMouseListener(this); 
    mList.addKeyListener(this);
    
    markDate(Date.getCurrentDate());
   
  }
  
  public static FinderPanel getInstance() {
    if (mInstance==null) {
      mInstance=new FinderPanel();
    } 
    
    return mInstance;
  }
  
  public void setDateListener(DateListener dateChangedListener) {
    mDateChangedListener=dateChangedListener;
  }
  
  public Date getSelectedDate() {
    return mCurChoosenDate;
  }
  
  public devplugin.ProgressMonitor getProgressMonitorForDate(Date date) {
    Object[] o=mModel.toArray();
    for (int i=0;i<o.length;i++) {
      FinderItem item=(FinderItem)o[i];
      mRenderer.setSelectedItem(item);
      
      if (item.getDate().equals(date)) {
        return item;
      }      
    }
    
    return null;
  }
  
  public void markDate(Date d) {
    markDate(d, null);
  }
  
  /**
   * This is a non blocking method
   * @param d
   * @param callback
   */
  public void markDate(final Date d, Runnable callback) {
    
    if (d.equals(mCurChoosenDate)) {
      if (callback!=null) {
        callback.run();
      }
      return;
    }    
    
    Object[] o=mModel.toArray();
    for (int i=0;i<o.length;i++) {
      final FinderItem item=(FinderItem)o[i];
      if (item.getDate().equals(d)) {
        if (item.isEnabled()) {
          mCurChoosenDate=d;       
          mRenderer.setSelectedItem(item);
          item.startProgress(mDateChangedListener, callback);
          return;
        }          
      } 
    }
  }

	
	public void mouseClicked(MouseEvent event) {
    int index=mList.locationToIndex(event.getPoint());    
		markDate(((FinderItem)mModel.getElementAt(index)).getDate());
	}


	public void mouseEntered(MouseEvent arg0) {		
	}

	
	public void mouseExited(MouseEvent arg0) {
		mList.clearSelection();
    mCurMouseItemInx=-1;
	}

	
	public void mousePressed(MouseEvent arg0) {}

	
	public void mouseReleased(MouseEvent arg0) {}


	public void mouseDragged(MouseEvent arg0) {}


	public void mouseMoved(MouseEvent event) {
    
    int index=mList.locationToIndex(event.getPoint());
    if (index != mCurMouseItemInx) {
      mCurMouseItemInx=index;
      mList.setSelectedIndex(index);
    }
		
	}

	
	public void keyPressed(KeyEvent event) {
		if (event.getKeyCode()==KeyEvent.VK_SPACE) {
      markDate(((FinderItem)mList.getSelectedValue()).getDate());
		}		
	}

	
	public void keyReleased(KeyEvent arg0) {}

	
	public void keyTyped(KeyEvent arg0) { }
}

