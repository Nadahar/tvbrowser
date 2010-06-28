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


package tvbrowser.ui.finder;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JProgressBar;
import javax.swing.UIManager;

import tvbrowser.core.DateListener;
import util.ui.Localizer;
import devplugin.Date;
import devplugin.ProgressMonitor;


class FinderItem extends JComponent implements ProgressMonitor {
  
  private devplugin.Date mDate;
  private JProgressBar mProgressBar;
  private JLabel mLabel;
  private JList mList;
  
  private static Color mColorSelected= new Color(236,236,212);
  
  public FinderItem(JList list, Date date, Date today) {
    mDate=date;
    mList=list;
    
    mProgressBar=new JProgressBar();
    mProgressBar.setForeground(UIManager.getColor("List.selectionBackground"));
    mProgressBar.setBorder(null);
    
    mLabel=new JLabel();
    
    if(date.equals(today.addDays(-1))) {
      mLabel.setText(Localizer.getLocalization(Localizer.I18N_YESTERDAY));
    } else if (date.equals(today)) {
      mLabel.setText(Localizer.getLocalization(Localizer.I18N_TODAY));
    }
    else if(date.equals(today.addDays(1))) {
      mLabel.setText(Localizer.getLocalization(Localizer.I18N_TOMORROW));
    }
    else {
      mLabel.setText(date.getShortDayLongMonthString());
    }
   
    setLayout(new BorderLayout());
    add(mLabel,BorderLayout.CENTER);
  }
  
  public Date getDate() {
    return mDate;
  }
  
  /**
   * Sets the item as choosen. A choosen item contains the currently viewed date.
   *
   */
  public void setChoosen() {
    mLabel.setOpaque(true);
    mLabel.setBackground(UIManager.getColor("List.selectionBackground"));
    mLabel.setForeground(UIManager.getColor("List.selectionForeground"));
  }
  
  public void setSelected() {
     mLabel.setOpaque(true);
     mLabel.setBackground(mColorSelected);
  }
    
  public void setOpaque(boolean b) {
    super.setOpaque(b);
    mLabel.setOpaque(b);
    mLabel.setForeground(UIManager.getColor("List.foreground"));
  }

  public void setEnabled(boolean b) {
    super.setEnabled(b);
    mLabel.setEnabled(b);
  }
  
  public void startProgress(final DateListener listener, final Runnable callback) {
    
    if (listener==null) {
      return;
    }
    
    remove(mLabel);
    add(mProgressBar,BorderLayout.CENTER);
    final ProgressMonitor monitor=this;
    Thread thread=new Thread("Finder"){
      public void run() {
        listener.dateChanged(mDate, monitor, callback);
        stopProgress();
      }
    };
    thread.start();
  }
  
  public void stopProgress() {
    remove(mProgressBar);
    add(mLabel,BorderLayout.CENTER);
    mList.repaint();
  }
  
  public void setMaximum(int maximum) {
    mProgressBar.setMaximum(maximum);
  }
  
  public void setValue(int value) {
    mProgressBar.setValue(value);
    mList.repaint();
  }

  
  public void setMessage(String msg) {
  }
  
  
  
}