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

  /**
   * Constructs a new FinderPanel.
   */
  public FinderPanel(DateListener dataChangedListener) {

    setLayout(new BorderLayout());
    this.dataChangedListener=dataChangedListener;
    
    JPanel labelList=new JPanel();
    labelList.setLayout(new GridLayout(0,1));
    
    
    JScrollPane scrollPane=new JScrollPane(labelList);
    
    FinderItem item;
    itemList=new Vector();

    devplugin.Date curDate=new Date();
    int cur=curDate.getDaysSince1970();
    
    int lifespan=Settings.getTVDataLifespan();
    int from;
    if (lifespan<0) {
    	from=cur-4;
    }
    else {
    	from=cur-lifespan;
    }
    
    for (int i=from;i<cur+56;i++) {
        Date d=new Date(i);
        item=new FinderItem(this, d);
        labelList.add(item);
        itemList.add(item);
        if (curDate.equals(d)) {
            curSelectedFinderItem=item;
            item.setMark(true);
        }
    }


    add(scrollPane,BorderLayout.CENTER);
    updateUI();
    dataChangedListener.dateChanged(new devplugin.Date());
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
      dataChangedListener.dateChanged(item.getDate());
      item.setMark(true);
      curSelectedFinderItem=item;
    }
  }

}