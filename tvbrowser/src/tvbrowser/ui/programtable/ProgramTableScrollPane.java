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
package tvbrowser.ui.programtable;

import java.awt.Color;
import java.awt.Point;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.BorderFactory;
import javax.swing.JScrollPane;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;

import tvbrowser.ui.programtable.background.BackgroundPainter;
import devplugin.Channel;

/**
 *
 * @author Til Schneider, www.murfman.de
 */
public class ProgramTableScrollPane extends JScrollPane
  implements ProgramTableModelListener, FocusListener
{
  
  private ProgramTable mProgramTable;
  private ChannelPanel mChannelPanel;
  
  private boolean mBorderPainted;
  
  private Border mDefaultBorder = BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(1,1,1,1),BorderFactory.createLineBorder(Color.gray));
  
  private Border mFocusBorder = new LineBorder(Color.gray,2);
  
  /**
   * Creates a new instance of ProgramTableScrollPane.
   */
  public ProgramTableScrollPane(ProgramTableModel model) {
    
    setFocusable(true);    
    addFocusListener(this);
    
    
    mProgramTable = new ProgramTable(model);
    mProgramTable.addPropertyChangeListener(new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals("backgroundpainter")) {
          BackgroundPainter painter = (BackgroundPainter) evt.getNewValue();
          handleBackgroundPainterChanged(painter);
        }
      }
    });
    model.addProgramTableModelListener(this);
    
    handleBackgroundPainterChanged(mProgramTable.getBackgroundPainter());
    setViewportView(mProgramTable);

    setWheelScrollingEnabled(true);
    getHorizontalScrollBar().setUnitIncrement(30);
    getVerticalScrollBar().setUnitIncrement(30);
    
    mChannelPanel = new ChannelPanel(mProgramTable.getColumnWidth(),
      model.getShownChannels());
    setColumnHeaderView(mChannelPanel);
     
    setOpaque(false);
    setBorder(mDefaultBorder);
    
    getHorizontalScrollBar().setFocusable(false);
    getVerticalScrollBar().setFocusable(false);
        
  }
  
  
  
  public ProgramTable getProgramTable() {
    return mProgramTable;
  }

 
  public void repaint() {
    super.repaint();
    if (mProgramTable!=null) mProgramTable.repaint();
    if (mChannelPanel!=null) mChannelPanel.repaint(); 
  }

 
  public void updateChannelPanel() {
    mChannelPanel = new ChannelPanel(mProgramTable.getColumnWidth(),
      mProgramTable.getModel().getShownChannels()); 
    setColumnHeaderView(mChannelPanel);
    this.updateUI();
  }
  
  public void setColumnWidth(int columnWidth) {
    mProgramTable.setColumnWidth(columnWidth);
    mChannelPanel.setColumnWidth(columnWidth);
  }
  
  
  
  public void scrollToChannel(Channel channel) {
    Channel[] shownChannelArr = mProgramTable.getModel().getShownChannels();
    for (int col = 0; col < shownChannelArr.length; col++) {
      if (channel.equals(shownChannelArr[col])) {
        Point scrollPos = getViewport().getViewPosition();
        if (scrollPos != null) {
          scrollPos.x = col * mProgramTable.getColumnWidth() -  getViewport().getWidth()/2 + mProgramTable.getColumnWidth()/2;
          if (scrollPos.x<0) {
            scrollPos.x=0;
          }
          int max=mProgramTable.getWidth()-getViewport().getWidth();
          if (scrollPos.x>max) {
            scrollPos.x=max;
          }
          getViewport().setViewPosition(scrollPos);
        }
      }
    }
  }



  public void scrollToTime(int minutesAfterMidnight) {
    Point scrollPos = getViewport().getViewPosition();
    
    scrollPos.y = mProgramTable.getTimeY(minutesAfterMidnight)
      - (getViewport().getHeight() / 4);
      
    if (scrollPos.y<0) {
      scrollPos.y=0;
    }
    
    int max=mProgramTable.getHeight()-getViewport().getHeight();
    if (scrollPos.y>max) {
      scrollPos.y=max;
    }

    getViewport().setViewPosition(scrollPos);
  }


  private void handleBackgroundPainterChanged(BackgroundPainter painter) {
    setRowHeaderView(painter.getTableWest());
  }
  

  // implements ProgramTableModelListener


  public void tableDataChanged() {
    mChannelPanel.setShownChannels(mProgramTable.getModel().getShownChannels());
  }



  public void tableCellUpdated(int col, int row) {
  }

  public void setBorderPainted(boolean borderPainted) {
    if (borderPainted!=mBorderPainted) {
      mBorderPainted=borderPainted;
      if (mBorderPainted) {
        setBorder(mFocusBorder);
      }
      else {
        setBorder(mDefaultBorder);
      }
    }
  }

	
	public void focusGained(FocusEvent arg0) {
    setBorderPainted(true);
	}



	
	public void focusLost(FocusEvent arg0) {
    setBorderPainted(false);
	}



}



//class ProgramTableBorder 