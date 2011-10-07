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
 *     $Date: 2010-06-28 19:33:48 +0200 (Mo, 28 Jun 2010) $
 *   $Author: bananeweizen $
 * $Revision: 6662 $
 */

package printplugin.printer.dayprogramprinter;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.Icon;

import printplugin.printer.ColumnModel;
import printplugin.printer.ProgramItem;
import printplugin.settings.ProgramIconSettings;
import devplugin.Program;



/**
 * ProgramTableIcon is a printable version of the programmtable.
 */
public class ProgramTableIcon implements Icon {


   private static final int COLUMN_WIDTH = 180;
   private static final int HEADER_SPACE=22;
   private static final Font COL_HEADER_FONT = new Font("Dialog", Font.BOLD, 18);


   private ProgramItem[][] mProgramItems;
   private int mWidth, mColumnHeight;
  
   private int mStartHour, mEndHour;

   private double mZoom;
   private String[] mColHeaders;
   
   private ProgramIconSettings mProgramIconSettings;
    
    
   public ProgramTableIcon(ColumnModel[] cols, int width, int height, int maxColsPerPage, int startHour, int endHour, ProgramIconSettings programIconSettings) {
     
     mProgramIconSettings = programIconSettings;
     
     mProgramItems = createProgramItems(cols);
    
     mWidth = width;
     mColumnHeight = height-HEADER_SPACE;
     mZoom = (double)mWidth/(double)(COLUMN_WIDTH * maxColsPerPage);


     mStartHour = startHour;
     mEndHour = endHour;



     mColHeaders = new String[cols.length];
     for (int i=0;i<cols.length;i++) {
       mColHeaders[i] = cols[i].getTitle();
     }
    
     doLayout();
    
   }
    
   private ProgramItem[][] createProgramItems(ColumnModel[] cols) {
    
     ProgramItem[][] items = new ProgramItem[cols.length][];
     for (int i=0;i<items.length;i++) {
       items[i] = new ProgramItem[cols[i].getProgramCount()];
       for (int j=0;j<items[i].length;j++) {
         items[i][j] = new ProgramItem(cols[i].getProgramAt(j), mProgramIconSettings, COLUMN_WIDTH, false);
       }
     }
    
     return items;
    
   }
    
    
  
   private int getPreferredPosition(Program prog, boolean nextDay) {
    
     int time = prog.getHours()*60 + prog.getMinutes();
     if (nextDay) {
       time+=24*60;
     }
     time = time - mStartHour*60;
     int timerange = (mEndHour - mStartHour)*60;
     
     return (time * mColumnHeight / timerange);
    
   }


   private void doLayout() {
    
     int x = 0;
     int y = 0;

     for (int col = 0; col < mProgramItems.length; col++) {   // for all columns

       // step 1: Find the ideal position of the program item. This may
       //         end up in overlapping programs.
       y=0;
       boolean nextDay = false;
       for (int i = 0; i < mProgramItems[col].length; i++) {
         Program prog = mProgramItems[col][i].getProgram();
         if (prog.getHours() < mStartHour) {
           nextDay = true;
         }
         y = (int)(getPreferredPosition(prog,nextDay)/mZoom);
         mProgramItems[col][i].setPos(x,y);
       }

       // step 2: The last program in the column is positioned at the bottom
       //         of the page
       int numberOfPrograms = mProgramItems[col].length;
/*
       if (numberOfPrograms>1) {
         ProgramItem item = mProgramItems[col][numberOfPrograms-1];
         y = (int)((mColumnHeight-item.getHeight())/mZoom);
         item.setPos(x,y);
       }
*/

       // step 3: resolve overlaps; start from top
       for (int i=1;i<mProgramItems[col].length; i++) {
         ProgramItem curItem = mProgramItems[col][i];
         ProgramItem prevItem = mProgramItems[col][i-1];
        
         double minY = prevItem.getY() + prevItem.getHeight();
         if (minY > curItem.getY()) {
           curItem.setPos(curItem.getX(), minY);
         }
       }
      
       ProgramItem lastItem = mProgramItems[col][numberOfPrograms-1];

       // step 4: go through programs from bottom and move them up if necessary
       if (lastItem.getY() + lastItem.getHeight() > mColumnHeight/mZoom) {
         lastItem.setPos(lastItem.getX(), mColumnHeight/mZoom - lastItem.getHeight());
         for (int i=numberOfPrograms-2;i>=0;i--) {
           ProgramItem curItem = mProgramItems[col][i];
           ProgramItem nextItem = mProgramItems[col][i+1];
           if (curItem.getY()+curItem.getHeight() > nextItem.getY()) {
             curItem.setPos(curItem.getX(), nextItem.getY()-curItem.getHeight());
           }
           else {
             break;
           }
         }
       }

       // step 5: set the maximum height of each program
       for (int i=0;i<mProgramItems[col].length-1; i++) {
         ProgramItem curItem = mProgramItems[col][i];
         ProgramItem nextItem = mProgramItems[col][i+1];
      
         curItem.setMaximumHeight((int)(nextItem.getY()-curItem.getY()));
       }
       //Set the max height for the last program of the channel.
       lastItem.setMaximumHeight((int)(mColumnHeight/mZoom-lastItem.getY()));

       // step 6: Check, if the first program of the column is within the column space.
       // If not, we remove all programs out of the column space.
       if (mProgramItems[col].length > 0) {
         int cnt = 0;
         boolean done = false;
         do {
           if (mProgramItems[col][cnt].getY()<0) {
             mProgramItems[col][cnt].setMaximumHeight(-1);
           }
           else {
             done = true;
           }
           cnt++;
         }while(!done);
         cnt--;
         ProgramItem[] oldColumn = mProgramItems[col];
         mProgramItems[col]=new ProgramItem[oldColumn.length - cnt];
         System.arraycopy(oldColumn, cnt, mProgramItems[col], 0, mProgramItems[col].length);
       }



       x+=COLUMN_WIDTH;
     }
    
   }
    
   public int getIconHeight() {
     return mColumnHeight+HEADER_SPACE;
   }

    
   public int getIconWidth() {
     return mWidth;
   }

  
   public void paintIcon(Component comp, Graphics graphics, int x, int y) {
      
      
     Graphics2D g = (Graphics2D)graphics;
     g.scale(mZoom, mZoom);
     g.translate(x/mZoom,(y+HEADER_SPACE)/mZoom);

     g.setFont(COL_HEADER_FONT);
     for (int i=0;i<mColHeaders.length;i++) {
       g.setColor(Color.lightGray);
       g.fillRect(COLUMN_WIDTH*i,-HEADER_SPACE,COLUMN_WIDTH,HEADER_SPACE);
       g.setColor(Color.black);
       g.drawLine(COLUMN_WIDTH*i,-HEADER_SPACE,COLUMN_WIDTH*i,0);
       FontMetrics metrics = g.getFontMetrics(COL_HEADER_FONT);
       int w = metrics.stringWidth(mColHeaders[i]);
       int cw = COLUMN_WIDTH;
       g.drawString(mColHeaders[i],cw*i + (cw-w)/2,-(HEADER_SPACE-COL_HEADER_FONT.getSize())/2);
     }
        
     g.drawRect(0,-HEADER_SPACE,(int)(mWidth/mZoom),(int)(mColumnHeight/mZoom)+HEADER_SPACE);
     g.drawLine(0,0,(int)(mWidth/mZoom),0);
     
     g.setColor(Color.lightGray);
     
     for (int i=1;i<mProgramItems.length;i++) {
       g.drawLine(COLUMN_WIDTH*i,0,COLUMN_WIDTH*i,(int)(mColumnHeight/mZoom));
     }

     for (ProgramItem[] mProgramItem : mProgramItems) {
       for (int j=0;j<mProgramItem.length;j++) {
         mProgramItem[j].paint(g);
       }
     }
      
     g.translate(-x/mZoom,-(y+HEADER_SPACE)/mZoom);
     g.scale(1/mZoom,1/mZoom);
      
      
   }
    
    
    
 }