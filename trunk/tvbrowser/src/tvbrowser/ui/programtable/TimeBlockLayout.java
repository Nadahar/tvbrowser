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

import tvbrowser.core.Settings;
import util.ui.ProgramPanel;

/**
 * A class for layout the program table in time blocks.
 * 
 * @author René Mach
 * @since 2.7
 */
public class TimeBlockLayout extends AbstractProgramTableLayout {
  protected boolean mCompactLayout = false;
  
  public void updateLayout(ProgramTableModel model) {
    int columnCount = model.getColumnCount();
    
    int[] columnStartArr = new int[columnCount];
    Arrays.fill(columnStartArr,0);
    
    int[] lastRow = new int[columnCount];
    Arrays.fill(lastRow,0);
    
    LastLayoutComponent[] lastLayoutComponentList = new LastLayoutComponent[columnCount];
    
    ArrayList[] blockProgramList = new ArrayList[columnCount];
    
    for(int i = 0; i < blockProgramList.length; i++) {
      blockProgramList[i] = new ArrayList();
    }
    
    int currentHeight = 0;
    int lastHeight = 0;
    
    int blockSize = Settings.propTimeBlockSize.getInt() * 60;
    int blockCount = ((Settings.propProgramTableEndOfDay.getInt() 
        - Settings.propProgramTableStartOfDay.getInt() + 1440) / blockSize) +1;
    
    for(int block = 0; block < blockCount; block++) {
      int maxHeight = 0;
      int maxIndex = 0;
      
      for(int column = 0; column < columnCount; column++) {
        blockProgramList[column].clear();
        int height = 0;
        for(int row = lastRow[column]; row < model.getRowCount(column); row++) {
          lastRow[column] = row;
          
          ProgramPanel panel = model.getProgramPanel(column,row);
          
          int startTime = panel.getProgram().getStartTime();
          
          if(panel.getProgram().getDate().addDays(-1).equals(model.getDate())) {
            startTime += 1440;
          }         
          
          if((startTime >= block * blockSize) && (startTime < (block+1) * blockSize)) {
            blockProgramList[column].add(panel);
            height += mCompactLayout ? panel.getMinimumHeight() : panel.getPreferredHeight();
            
            if(height > maxHeight) {
              maxIndex = column;
            }
            
            maxHeight = Math.max(maxHeight,height);
          }
          else {
            break;
          }
        }
      }
      
    
      ArrayList maxList = blockProgramList[maxIndex];
      
      int blockStart = currentHeight;
      
      if(!maxList.isEmpty()) {
        if(maxList.get(0).equals(model.getProgramPanel(maxIndex,0))) {
          columnStartArr[maxIndex] = blockStart;
        }
        
        if(lastLayoutComponentList[maxIndex] != null) {
          lastLayoutComponentList[maxIndex].getPanel().setHeight(blockStart - lastLayoutComponentList[maxIndex].getPrePosition());
        }

        
        for(int i = 0; i < maxList.size(); i++) {
          ProgramPanel panel = (ProgramPanel)maxList.get(i);
          
          panel.setHeight(mCompactLayout ? panel.getMinimumHeight() : panel.getPreferredHeight());
          
          lastHeight = currentHeight;
          currentHeight += panel.getHeight();
        }
        
        lastLayoutComponentList[maxIndex] = new LastLayoutComponent((ProgramPanel)maxList.get(maxList.size()-1),lastHeight);
      }
      
      for(int col = 0; col < columnCount; col++) {
        if(col != maxIndex) {
          ArrayList list = blockProgramList[col];
          
          if(!list.isEmpty()) {
            if(list.get(0).equals(model.getProgramPanel(col,0))) {
              columnStartArr[col] = blockStart;
            }
            
            if(lastLayoutComponentList[col] != null) {
              lastLayoutComponentList[col].getPanel().setHeight(blockStart - lastLayoutComponentList[col].getPrePosition());
            }
            
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
    }  
    
    for(int col = 0; col < columnCount; col++) {
      if(lastLayoutComponentList[col] != null) {
        lastLayoutComponentList[col].getPanel().setHeight(currentHeight - lastLayoutComponentList[col].getPrePosition());
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
