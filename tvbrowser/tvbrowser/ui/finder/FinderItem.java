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
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */


 /**
  * TV-Browser
  * @author Martin Oberhauser
  */


package tvbrowser.ui.finder;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import tvbrowser.core.*;

/**
 * A FinderItem object represents an item of the Finder.
 */

public class FinderItem extends JLabel implements MouseListener {

  private devplugin.Date date;
  private FinderListener listener;
  private boolean isMarked;

  /**
   * Constructs a new FinderItem containing the specified date (in days since 1900-01-01).
   * The FinderListener is called, if the FinderItem is selected.
   */
  public FinderItem(FinderListener listener,devplugin.Date d) {
    super();
    this.listener=listener;
    date=d;
    if (new devplugin.Date().equals(d)) {
      setText("today");
    }else {
      setText(d.toString());
    }

    addMouseListener(this);
    update();
    isMarked=false;
  }


  /**
   * Returns the date.
   */
  public devplugin.Date getDate() {
    return date;
  }

  /**
   * Enables (or disables) the FinderItem if program information is available (or unavailable).
   */
  public void update() {
    this.setEnabled(DataService.dataAvailable(date));
    setMark(isMarked());
  }


  /**
   * Returns true, if the FinderItem is marked.
   */
  public boolean isMarked() {
    return isMarked;
  }



  /**
   * Marks the FinderItem. FinderItems are marked, if the ProgramTablePanel shows
   * its program.
   */
  public void setMark(boolean mark) {
    isMarked=mark;

    if (isMarked) {
      setOpaque(true);
      setBackground(Color.darkGray);
    }
    else {
      setOpaque(false);
      updateUI();
    }
  }

  /**
   * Implementation of the MouseListener interface.
   */
  public void mouseClicked(MouseEvent e) {

    if (this.isEnabled() || DataService.getInstance().isOnlineMode()) {
      if (listener!=null) {
        listener.finderItemStatusChanged(this);
      }
    }
  }

  /**
   * Implementation of the MouseListener interface.
   */
  public void mouseEntered(MouseEvent e) {

    if (isEnabled()) {
      setOpaque(true);
      setBackground(Color.yellow);
      this.updateUI();

    }
  }


  /**
   * Implementation of the MouseListener interface.
   */
  public void mouseExited(MouseEvent e) {
    if (isMarked) {
      setOpaque(true);
      setBackground(Color.darkGray);
    }
    else {
      setOpaque(false);
    }
    updateUI();

  }

  /**
   * Implementation of the MouseListener interface.
   */
  public void mousePressed(MouseEvent e) { }

  /**
   * Implementation of the MouseListener interface.
   */
  public void mouseReleased(MouseEvent e) { }




}