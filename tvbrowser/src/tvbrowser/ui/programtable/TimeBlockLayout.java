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
 *     $Date$
 *   $Author$
 * $Revision$
 */
package tvbrowser.ui.programtable;

import java.util.ArrayList;
import java.util.Arrays;

import devplugin.Date;

import tvbrowser.core.Settings;
import util.ui.ProgramPanel;

/**
 * Define blocks of <i>n</i> hours. Each of these blocks starts at the same height in all columns
 * (so the borders of these time blocks are time synchronous). 
 * The height of a certain block is calculated by the channel with the most programs in that block.
 * 
 * @author René Mach
 * @since 2.7
 */
public class TimeBlockLayout extends AbstractProgramTableLayout {
  protected boolean mCompactLayout = false;
  
  public void updateLayout(ProgramTableModel model) {
    int columnCount = model.getColumnCount();
    
    // nothing to show at all?
    if (columnCount == 0) {
      return;
    }
    
    int[] columnStartArr = new int[columnCount];
    Arrays.fill(columnStartArr,0);
    
    int[] lastRow = new int[columnCount];
    Arrays.fill(lastRow,0);
    
    LastLayoutComponent[] lastLayoutComponentList = new LastLayoutComponent[columnCount];
    
    ArrayList<ProgramPanel>[] blockProgramList = new ArrayList[columnCount];
    
    for(int i = 0; i < blockProgramList.length; i++) {
      blockProgramList[i] = new ArrayList<ProgramPanel>();
    }
    
    int blockEnd = 0;

    // we need to check all the time from midnight to end of day because the filters may include
    // programs before the beginOfDay time
    int blockSize = Settings.propTimeBlockSize.getInt() * 60;
    int blockCount = ((Settings.propProgramTableEndOfDay.getInt() + 24 * 60) / blockSize) +1;
    int blockOffset = 0;
    Date nextProgramTableDate = model.getDate().addDays(1);

    // calculate the hight of each block independently
    for(int block = blockOffset; block < blockOffset + blockCount; block++) {
      int maxHeight = 0;
      
      // search for the largest column in this block
      for(int column = 0; column < columnCount; column++) {
        blockProgramList[column].clear();
        int height = 0;
        int rowCount = model.getRowCount(column);
        for(int row = lastRow[column]; row < rowCount; row++) {
          lastRow[column] = row;
          
          ProgramPanel panel = model.getProgramPanel(column,row);
          
          int startTime = panel.getProgram().getStartTime();
          if(panel.getProgram().getDate().equals(nextProgramTableDate)) {
            startTime += 24 * 60;
          }         
          
          if((startTime >= block * blockSize) && (startTime < (block+1) * blockSize)) {
            blockProgramList[column].add(panel);
            // reset the preferred hight of the panel
            panel.setHeight(-1);
            height += mCompactLayout ? panel.getMinimumHeight() : panel.getPreferredHeight();
            
            if(height > maxHeight) {
              maxHeight = height;
            }
          }
          else {
            break;
          }
        }
      }

      // increase overall height by block height
      int blockStart = blockEnd;
      blockEnd += maxHeight;
      
      for(int col = 0; col < columnCount; col++) {
        // first correct the last panel of this column to make it full size
        if(lastLayoutComponentList[col] != null) {
          lastLayoutComponentList[col].getPanel().setHeight(blockStart - lastLayoutComponentList[col].getPrePosition());
        }

        // set all panels in this block to preferred size
        ArrayList<ProgramPanel> list = blockProgramList[col];
        if(!list.isEmpty()) {
          if(list.get(0).equals(model.getProgramPanel(col,0))) {
            columnStartArr[col] = blockStart;
          }

/*        
          // calculate wanted full size of this block column
          int sumLength = 0;
          int sumMinLength = 0;
          for (int i = 0; i < list.size(); i++) {
            ProgramPanel panel = (ProgramPanel)list.get(i);
            sumMinLength += (mCompactLayout ? panel.getMinimumHeight() : panel.getPreferredHeight());
            panel.setMaximumHeight();
            sumLength += panel.getHeight();
          }
          
          double factor = 1;
          // now distribute programs evenly in available space (excluding the last panel)
          // but do not stretch programs longer than they want to be
          if (sumLength > maxHeight) {
            factor = ((double)(maxHeight - sumMinLength))/(double)(sumLength - sumMinLength);
          }
          
          sumLength = 0;
          for (int i = 0; i < list.size() - 1; i++) {
            ProgramPanel panel = (ProgramPanel)list.get(i);
            int height = (int) Math.floor(factor * panel.getHeight());
            height = Math.max(height, panel.getMinimumHeight());
            panel.setHeight(height);
            sumLength += height;
          }
          
          // set last panel to consume remaining space
          ProgramPanel panel = (ProgramPanel) list.get(list.size()-1);
          int lastHeight = maxHeight-sumLength;
          panel.setHeight(lastHeight);
          lastLayoutComponentList[col] = new LastLayoutComponent((ProgramPanel)list.get(list.size()-1), blockEnd - lastHeight);
*/          
          
          int internHeight = blockStart;
          int internLastHeight = internHeight;

          for(int i = 0; i < list.size(); i++) {
            ProgramPanel panel = (ProgramPanel)list.get(i);
            
            panel.setHeight(mCompactLayout ? panel.getMinimumHeight() : panel.getPreferredHeight());
            
            internLastHeight = internHeight;
            internHeight += panel.getHeight();
          }
          
          lastLayoutComponentList[col] = new LastLayoutComponent((ProgramPanel)list.get(list.size()-1),internLastHeight);

        }
      }
    }
    
    // set all last panels in all columns to eat all available space
    for(int col = 0; col < columnCount; col++) {
      if(lastLayoutComponentList[col] != null) {
        lastLayoutComponentList[col].getPanel().setHeight(blockEnd - lastLayoutComponentList[col].getPrePosition());
      }
    }

    setColumnStarts(columnStartArr);
  }
  

  private static class LastLayoutComponent {
    private ProgramPanel mLastPanel;
    private int mPrePosition;
    
    protected LastLayoutComponent(ProgramPanel lastPanel, int prePosition) {
      mLastPanel = lastPanel;
      mPrePosition = prePosition;
    }
    
    protected ProgramPanel getPanel() {
      return mLastPanel;
    }
    
    protected int getPrePosition() {
      return mPrePosition;
    }
  }

}