/*
* TV-Browser
* Copyright (C) 04-2003 Martin Oberhauser (martin_oat@yahoo.de)
*
* This program is free software; you can redistribute it and/or
* modify it under the terms of the GNU General Public License
* as published by the Free Software Foundation; either version 2
* of the License, or (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */


 /**
  * TV-Browser
  * @author Martin Oberhauser
  */


package tvbrowser.ui;

import javax.swing.*;
import java.awt.*;
import java.io.File;

import tvbrowser.core.Settings;

/**
 * A SkinPanel is a JPanel with the capability to have a background image
 */

public class SkinPanel extends JPanel {
  private Image image;
  private int hImg;
  private int mode=Settings.WALLPAPER;
  private String imgFile;

  public SkinPanel() {
    super();
  }

  public SkinPanel(String imgFile, int mode) {
    super();
    this.mode=mode;
    this.imgFile=imgFile;
    if (mode!=Settings.NONE) {
      File f=new File(imgFile);
      if (f.exists() && f.isFile()) {
        image=new ImageIcon(imgFile).getImage();
        setSkin(image);
      }else {
        image=null;
      }
    }
  }

  public void update(String newImgFile, int newMode) {
    if (mode!=newMode || !newImgFile.equals(imgFile)) {
      imgFile=newImgFile;
      mode=newMode;
      File f=new File(imgFile);
      if (f.exists() && f.isFile()) {
        image=new ImageIcon(imgFile).getImage();
        setSkin(image);
      }else {
        image=null;
      }
      updateUI();
    }

  }

  public void setMode(int mode){
    this.mode=mode;
  }

  public void setSkin(Image img) {
    image=img;
    if (image!=null) {
      hImg=image.getHeight(this);
    }
  }

  public void paintComponent(Graphics g) {
    super.paintComponent(g);

    if (image==null || mode==Settings.NONE) {
      return;
    }

    int width = getWidth();
    int height = getHeight();

    int step=0;

    if (mode==Settings.WALLPAPER) {
      step=image.getWidth(this);
    }else if (mode==Settings.COLUMNS) {
      step=Settings.getColumnWidth();
    }

    for (int x = 0; x < width; x += step) {
      for (int y = 0; y < height; y += hImg) {
        g.drawImage(image, x, y, this);
      }
    }

  }




}