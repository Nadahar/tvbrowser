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

import java.awt.Point;

import javax.swing.JScrollPane;

import devplugin.Channel;

/**
 *
 * @author Til Schneider, www.murfman.de
 */
public class ProgramTableScrollPane extends JScrollPane
  implements ProgramTableModelListener
{
  
  private ProgramTable mProgramTable;
  private ChannelPanel mChannelPanel;
  private ChannelChooser mChannelChooser;
  
  
  
  /**
   * Creates a new instance of ProgramTableScrollPane.
   */
  public ProgramTableScrollPane(ProgramTableModel model) {
    mProgramTable = new ProgramTable(model);
    model.addProgramTableModelListener(this);
    
    setViewportView(mProgramTable);

    setWheelScrollingEnabled(true);
    getHorizontalScrollBar().setUnitIncrement(30);
    getVerticalScrollBar().setUnitIncrement(30);
    
    mChannelPanel = new ChannelPanel(mProgramTable.getColumnWidth(),
      model.getShownChannels());
    setColumnHeaderView(mChannelPanel);
    
    mChannelChooser = new ChannelChooser(this);
    setCorner(JScrollPane.UPPER_RIGHT_CORNER, mChannelChooser);
  }
  
  
  
  public ProgramTable getProgramTable() {
    return mProgramTable;
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
          scrollPos.x = col * mProgramTable.getColumnWidth();
          getViewport().setViewPosition(scrollPos);
        }
      }
    }
  }



  public void scrollToTime(int minutesAfterMidnight) {
    Point scrollPos = getViewport().getViewPosition();
    
    scrollPos.y = mProgramTable.getTimeY(minutesAfterMidnight)
      - (getViewport().getHeight() / 2);

    getViewport().setViewPosition(scrollPos);
  }
  

  // implements ProgramTableModelListener


  public void tableDataChanged() {
    mChannelPanel.setShownChannels(mProgramTable.getModel().getShownChannels());
  }



  public void tableCellUpdated(int col, int row) {
  }

}
