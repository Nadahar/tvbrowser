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

package printplugin;

import java.awt.Color;
import java.awt.Component;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.Icon;

import devplugin.Program;


public class ProgramTableIcon implements Icon {

   private ProgramItem[][] mProgramItems;
   private int mWidth, mColumnHeight;
  
   private int mStartHour, mEndHour;
   private static final int HEADER_SPACE=22; 
   private double mZoom;
   private String[] mColHeaders;
   
   private ProgramIconSettings mProgramIconSettings;
    
   
    
   public ProgramTableIcon(ColumnModel[] cols, int width, int height, int maxColsPerPage, int startHour, int endHour, ProgramIconSettings programIconSettings) {
     
     mProgramIconSettings = programIconSettings;
     
     mProgramItems = createProgramItems(cols);
    
     mWidth = width;
     mColumnHeight = height-HEADER_SPACE;
     mZoom = (double)mWidth/(double)(ChannelPageRenderer.COLUMN_WIDTH * maxColsPerPage);
    
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
         items[i][j] = new ProgramItem(cols[i].getProgramAt(j), mProgramIconSettings);
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
     int curHour = -1;
    
     for (int col = 0; col < mProgramItems.length; col++) {  
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
      
       int numberOfPrograms = mProgramItems[col].length;  
       if (numberOfPrograms>1) {
         ProgramItem item = mProgramItems[col][numberOfPrograms-1];
         y = (int)((mColumnHeight-item.getHeight())/mZoom);
         item.setPos(x,y);
       }
      
      
      
       for (int i=1;i<mProgramItems[col].length/*-1*/; i++) {
         ProgramItem curItem = mProgramItems[col][i];
         ProgramItem prevItem = mProgramItems[col][i-1];
        
         double minY = prevItem.getY() + prevItem.getHeight();
         if (minY > curItem.getY()) {
           curItem.setPos(curItem.getX(), minY);
         }
       }
      
       ProgramItem lastItem = mProgramItems[col][numberOfPrograms-1];
      
       if (lastItem.getY()*mZoom+lastItem.getHeight()>mColumnHeight*1.1) {
         lastItem.setPos(lastItem.getX(), mColumnHeight/mZoom - lastItem.getHeight());
         for (int i=numberOfPrograms-2;i>0;i--) {
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
    
      
       for (int i=0;i<mProgramItems[col].length-1; i++) {
         ProgramItem curItem = mProgramItems[col][i];
         ProgramItem nextItem = mProgramItems[col][i+1];
      
         curItem.setMaximumHeight((int)(nextItem.getY()-curItem.getY()));
      
       }      
      
       x+=ChannelPageRenderer.COLUMN_WIDTH;
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
      
     
     
     
     g.setFont(ChannelPageRenderer.COL_HEADER_FONT);
     for (int i=0;i<mColHeaders.length;i++) {
       g.setColor(Color.lightGray);
       g.fillRect(ChannelPageRenderer.COLUMN_WIDTH*i,-HEADER_SPACE,ChannelPageRenderer.COLUMN_WIDTH,HEADER_SPACE);  
       g.setColor(Color.black);
       g.drawLine(ChannelPageRenderer.COLUMN_WIDTH*i,-HEADER_SPACE,ChannelPageRenderer.COLUMN_WIDTH*i,0);
       FontMetrics metrics = g.getFontMetrics(ChannelPageRenderer.COL_HEADER_FONT);
       int w = metrics.stringWidth(mColHeaders[i]);
       int cw = ChannelPageRenderer.COLUMN_WIDTH;
       g.drawString(mColHeaders[i],cw*i + (cw-w)/2,-(HEADER_SPACE-ChannelPageRenderer.COL_HEADER_FONT.getSize())/2);
     }
        
     g.drawRect(0,-HEADER_SPACE,(int)(mWidth/mZoom),(int)(mColumnHeight/mZoom)+HEADER_SPACE);    
     g.drawLine(0,0,(int)(mWidth/mZoom),0);    
     
     g.setColor(Color.lightGray);
     
     for (int i=1;i<mProgramItems.length;i++) {
       g.drawLine(ChannelPageRenderer.COLUMN_WIDTH*i,0,ChannelPageRenderer.COLUMN_WIDTH*i,(int)(mColumnHeight/mZoom));   
     }
      
     /*
     for (int i=0;i<mColHeaders.length;i++) {
             g.drawString(mColHeaders[i],ChannelPageRenderer.COLUMN_WIDTH*i, HEADER_SPACE);
           }*/
      
         
     for (int i=0;i<mProgramItems.length;i++) {
       for (int j=0;j<mProgramItems[i].length;j++) {
         mProgramItems[i][j].paint(g);
       }
     }
      
     g.translate(-x/mZoom,-(y+HEADER_SPACE)/mZoom);
      
      
      
      
      
     g.scale(1/mZoom,1/mZoom);
      
      
      
   }
    
    
    
 }