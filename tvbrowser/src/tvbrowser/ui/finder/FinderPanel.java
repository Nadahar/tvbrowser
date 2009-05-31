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

/**
 * TV-Browser
 * @author Martin Oberhauser
 */

package tvbrowser.ui.finder;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;

import tvbrowser.core.DateListener;
import tvbrowser.core.TvDataBase;
import tvbrowser.ui.mainframe.MainFrame;
import devplugin.Date;

class FinderItemRenderer extends DefaultListCellRenderer {

  private FinderItem mCurSelectedItem;

  public FinderItemRenderer() {
  }

  public void setSelectedItem(FinderItem item) {
    mCurSelectedItem = item;
  }

  public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
      boolean cellHasFocus) {

    FinderItem comp = (FinderItem) value;

    if (cellHasFocus) {
      comp.setBorder(BorderFactory.createLineBorder(Color.black));
    } else {
      comp.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
    }

    if (value == mCurSelectedItem) {
      comp.setChoosen();
    } else if (isSelected) {
      comp.setSelected();
    } else {
      comp.setOpaque(false);
    }

    comp.setEnabled(TvDataBase.getInstance().dataAvailable(comp.getDate()));
    return comp;
  }

}

public class FinderPanel extends AbstractDateSelector implements DateSelector,
    MouseMotionListener, KeyListener {

  private static final util.ui.Localizer mLocalizer
  = util.ui.Localizer.getLocalizerFor(FinderPanel.class);  
  
  private DateListener mDateChangedListener;

  private JList mList;

  private DefaultListModel mModel;

  private FinderItemRenderer mRenderer;

  private int mCurMouseItemInx = -1;

  private JScrollPane mScrollPane;

  public FinderPanel() {

    mScrollPane = new JScrollPane();
    setLayout(new BorderLayout());
    add(mScrollPane, BorderLayout.CENTER);
    mScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    mModel = new DefaultListModel();
    mList = new JList(mModel);
    mList.setOpaque(false);

    mRenderer = new FinderItemRenderer();
    mList.setCellRenderer(mRenderer);

    mScrollPane.setViewportView(mList);
    updateContent();
    mList.addMouseMotionListener(this);
    mList.addMouseListener(this);
    mList.addKeyListener(this);

    markDate(mToday);

  }

  public JComponent getComponent() {
    return this;
  }

  /**
   * Refresh the content. This method should be called after midnight to refresh
   * the 'today' label.
   */
  public void updateContent() {
    Date date = Date.getCurrentDate();
    if (date.equals(mToday)) {
      return;
    }

    mToday = date;
    mModel.removeAllElements();
    Date maxDate = TvDataBase.getInstance().getMaxSupportedDate();
    for (int i = -1;; i++) {
      Date d = mToday.addDays(i);
      if (d.getNumberOfDaysSince(maxDate) > 0) {
        break;
      }
      FinderItem fi = new FinderItem(mList, d, mToday);
      mModel.addElement(fi);
      if (d.equals(getSelectedDate())) {
        mRenderer.setSelectedItem(fi);
        mList.setSelectedValue(fi, false);
      }
    }
    mList.repaint();
  }

  public void setDateListener(DateListener dateChangedListener) {
    mDateChangedListener = dateChangedListener;
  }

  public devplugin.ProgressMonitor getProgressMonitorForDate(Date date) {
    Object[] objects = mModel.toArray();
    for (Object object : objects) {
      FinderItem item = (FinderItem) object;
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
   * Updates the items after a data update.
   */
  public void updateItems() {
    Object[] o = mModel.toArray();
    
    for (int i = 0; i < o.length; i++) {
      FinderItem item = (FinderItem) o[i];
      
      if(!item.isEnabled()) {
        if (isValidDate(item.getDate())) {
          item.setEnabled(true);
        }
      }
    }
    
    mList.repaint();
  }

  /**
   * This is a non blocking method
   * 
   * @param d
   * @param callback
   */
  public void markDate(final Date d, Runnable callback) {

    if (d.equals(getSelectedDate())) {
      if (callback != null) {
        callback.run();
      }
      return;
    }
    
    Object[] objects = mModel.toArray();
    for (Object object : objects) {
      final FinderItem item = (FinderItem) object;
      if (item.getDate().equals(d)) {
        if (item.isEnabled()) {
          setCurrentDate(d);
          mRenderer.setSelectedItem(item);
          mList.setSelectedValue(item, true);
          item.startProgress(mDateChangedListener, callback);
        } else {
          MainFrame.getInstance().askForDataUpdate(
              mLocalizer.msg("noDataFor", "No TV data available for {0}.", d
                  .toString()), d.getNumberOfDaysSince(Date.getCurrentDate()));
        }
        return;
      }
    }
  }

  public void mouseExited(MouseEvent arg0) {
    mList.clearSelection();
    mCurMouseItemInx = -1;
  }

  public void mouseReleased(MouseEvent e) {
    if (e.isPopupTrigger()) {
      showPopup(e);
    }
    else if (SwingUtilities.isLeftMouseButton(e)) {
      int index = mList.locationToIndex(e.getPoint());
      markDate(((FinderItem) mModel.getElementAt(index)).getDate());
      MainFrame.getInstance().addKeyboardAction();
    }
  }


  public void mouseDragged(MouseEvent arg0) {
  }

  public void mouseMoved(MouseEvent event) {

    int index = mList.locationToIndex(event.getPoint());
    if (index != mCurMouseItemInx) {
      mCurMouseItemInx = index;
      mList.setSelectedIndex(index);
    }

  }

  public void keyPressed(KeyEvent event) {
    if (event.getKeyCode() == KeyEvent.VK_SPACE) {
      FinderItem item = (FinderItem) mList.getSelectedValue();
      if (item != null) {
        markDate(item.getDate());
      }
    }
  }

  public void keyReleased(KeyEvent arg0) {
  }

  public void keyTyped(KeyEvent arg0) {
  }
}
