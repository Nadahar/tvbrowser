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

package tvbrowser.ui;

import java.awt.Graphics;
import java.awt.Image;
import java.io.File;

import javax.swing.ImageIcon;
import javax.swing.JPanel;

import util.ui.ImageUtilities;

/**
 * A SkinPanel is a JPanel with the capability to have a background image
 *
 * @author Martin Oberhauser
 */
public class SkinPanel extends JPanel {
	private Image image;
   private int hImg, wImg;
   private int mode=WALLPAPER;
   private String imgFile;
   private int colDiff=0;
   private int rowDiff=0;
   public static final int NONE=0, WALLPAPER=1, COLUMNS=2, ROWS=3, SINGLE=4;


  public SkinPanel() {
	 super();
   }

   public SkinPanel(String imgFile, int mode) {
	 super();
	 this.mode=mode;
	 this.imgFile=imgFile;
	 if (mode!=NONE) {
	   File f=new File(imgFile);
	   if (f.exists() && f.isFile()) {
		 image=new ImageIcon(imgFile).getImage();
		 setSkin(image);
	   }else {
		 image=null;
	   }
	 }
   }
   
   
   public void setColDiff(int diff) {
	 colDiff=diff;
   }
  
   public void setRowDiff(int diff) {
	 rowDiff=diff;
   }

   public void update(String newImgFile, int newMode) {
	 if (mode!=newMode || !newImgFile.equals(imgFile)) {
	   imgFile=newImgFile;
	   mode=newMode;
	   File f=new File(imgFile);
	   if (f.exists() && f.isFile()) {
		 image = ImageUtilities.createImage(imgFile);
		 setSkin(image);
	   }else {
		 image=null;
	   }
	   repaint();
	 }

   }

   public void setMode(int mode){
	 this.mode=mode;
   }

   public void setSkin(Image img) {
	 image=img;
	 if (image!=null) {
	   hImg=image.getHeight(this);
	   wImg=image.getWidth(this);
	 }
   }

   public void paintComponent(Graphics g) {
	 if (image==null || mode==NONE) {
	   //return;
    super.paintComponent(g);
    return;
	 }
    super.paintComponent(g);
    
	 if (mode==SINGLE) {
		 g.drawImage(image, 0, 0, this);
	 }
	 else {
		 int width = getWidth();
		 int height = getHeight();

		 int stepX=0;
		 int stepY=0;
         
		 if (mode==WALLPAPER) {
			 stepX=wImg;
			 stepY=hImg;
		 }else if (mode==COLUMNS) {
			 stepX=colDiff;
			 stepY=hImg;
		 }else if (mode==ROWS) {
			 stepX=wImg;
			 stepY=rowDiff;
		 }
		 if (stepX==0 || stepY==0) {
		 	throw new RuntimeException();
		 }
    	

		 for (int x = 0; x < width; x += stepX) {
			 for (int y = 0; y < height; y += stepY) {
				 g.drawImage(image, x, y, this);
			 }
		 }
	 }
   }

 }