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
import java.util.Enumeration;


import java.util.Vector;

import devplugin.Date;
import tvbrowser.core.*;

public class FinderPanel extends JComponent implements FinderListener {
  private FinderItem curSelectedFinderItem=null;
  private DateListener dataChangedListener=null;
  private Vector itemList;
  
  private static FinderPanel mSingleton;

  /**
   * Constructs a new FinderPanel.
   */
  private FinderPanel() {

    setLayout(new BorderLayout());
    
    JPanel labelList=new JPanel();
    labelList.setLayout(new GridLayout(0,1));
    
    
    JScrollPane scrollPane=new JScrollPane(labelList,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    
    FinderItem item;
    itemList=new Vector();

    devplugin.Date curDate=new Date();
    devplugin.Date today=new Date(curDate);
   
    
    
    curDate=curDate.addDays(-3);
    
    for (int i=0;i<60;i++) {
        item=new FinderItem(this, curDate);
        labelList.add(item);
        itemList.add(item);
        if (today.equals(curDate)) {
          curSelectedFinderItem=item;
          item.setMark(true);
        }
        curDate=curDate.addDays(1);
    }


    add(scrollPane,BorderLayout.CENTER);
    updateUI();
  }


  public static FinderPanel getInstance() {
    if (mSingleton==null) {
      mSingleton=new FinderPanel();
    }
    return mSingleton;
  }
  
  public void setDateListener(DateListener dataChangedListener) {
    this.dataChangedListener=dataChangedListener;
  }


  /**
   * Calls the update() method for each FinderItem
   */
  public void update() {
    Enumeration enum=itemList.elements();
    while (enum.hasMoreElements()) {
      ((FinderItem)enum.nextElement()).update();
    }

  }

  /**
   * Returns the currently selected date
   */

  public Date getSelectedDate() {
    return curSelectedFinderItem.getDate();
  }

  /**
   * Marks the FinderItem containing the given date
   */
  public void markDate(devplugin.Date date) {
    if (itemList==null) {
      throw new RuntimeException("itemList is null");
    }
    java.util.Enumeration enum=itemList.elements();
    FinderItem item;
    while (enum.hasMoreElements()) {
      item=(FinderItem)enum.nextElement();
      if (item.getDate().equals(date)) {
        finderItemStatusChanged(item);
        return;
      }
    }
  }



  /**
   * Implementation of the interface "FinderListener". Called by the FinderItem.
   * Marks the FinderItem and calls the dataChangedListener to update the program table.
   */
  public void finderItemStatusChanged(FinderItem item) {
   if (item!=curSelectedFinderItem) {
      if (curSelectedFinderItem!=null) {
        curSelectedFinderItem.setMark(false);
      }
      if (dataChangedListener!=null) dataChangedListener.dateChanged(item.getDate());
      item.setMark(true);
      curSelectedFinderItem=item;
    }
  }

}