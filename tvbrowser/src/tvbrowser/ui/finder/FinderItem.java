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


package tvbrowser.ui.finder;

import javax.swing.*;

import tvbrowser.core.DateListener;

import java.awt.*;

import devplugin.ProgressMonitor;
import devplugin.Date;


class FinderItem extends JComponent implements ProgressMonitor {
  
  private static final util.ui.Localizer mLocalizer
     = util.ui.Localizer.getLocalizerFor(FinderItem.class);
  
  
  private devplugin.Date mDate;
  private static Date TODAY=Date.getCurrentDate();
  private JProgressBar mProgressBar;
  private JLabel mLabel;
  private JList mList;
  
  private static Color mColorChoosen=new Color(170,154,228);
  private static Color mColorSelected= new Color(236,236,212);
  
  public FinderItem(JList list, Date date) {
    mDate=date;
    mList=list;
    
    mProgressBar=new JProgressBar();
    mProgressBar.setForeground(mColorChoosen);
    mProgressBar.setBorder(null);
    
    mLabel=new JLabel();
    if (date.equals(TODAY)) {
      mLabel.setText(mLocalizer.msg("today","today"));
    }
    else {
      mLabel.setText(date.toString());
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
    mLabel.setBackground(mColorChoosen);   
  }
  
  public void setSelected() {
     mLabel.setOpaque(true);
     mLabel.setBackground(mColorSelected);   
  }
    
  public void setOpaque(boolean b) {
    super.setOpaque(b);
    mLabel.setOpaque(b);    
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
    Thread thread=new Thread(){
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