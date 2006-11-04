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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package util.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;

import javax.swing.GrayFilter;
import javax.swing.Icon;
import javax.swing.ImageIcon;

import devplugin.Program;
import devplugin.ProgramFieldType;

/**
 * A class for painting a picture with copyright and description info.
 * 
 * @author Ren� Mach
 * @since 2.2.2
 */
public class PictureAreaIcon implements Icon {
  
  private TextAreaIcon mDescriptionText;
  private TextAreaIcon mCopyrightText;  
  private ImageIcon mScaledIcon;
  private Program mProgram;
  private boolean mIsExpired;
  private boolean mIsGrayFilter;
  private boolean mShowDescription;
  
  /**
   * Constructor for programs with no picture or if pictures for 
   * a program should not be shown.
   */
  public PictureAreaIcon() {}
  
  /**
   * Constructor for programs with picture.
   * 
   * @param p The program with the picture.
   * @param f The font for the description.
   * @param width The width of this area.
   * @param showDescription If description should be shown.
   * @param grayFilter If the image should be filtered to gray if the program is expired.
   * @param zoom If the piture should be zoomed to width.
   */
  public PictureAreaIcon(Program p, Font f, int width, boolean showDescription, boolean grayFilter, boolean zoom) {
    mProgram = p;
    mIsExpired = false;
    mIsGrayFilter = grayFilter;
    mShowDescription = showDescription;
    
    byte[] picture = p.getBinaryField(ProgramFieldType.PICTURE_TYPE);    
    
    if(picture != null) {
      ImageIcon imic = new ImageIcon(picture);
      
      if(width == -1)        
        width = imic.getIconWidth()+6;
      
      if(imic.getIconWidth() > width-6 || (zoom && imic.getIconWidth() != width))
        mScaledIcon = (ImageIcon)UiUtilities.scaleIcon(imic, width - 6);
      else
        mScaledIcon = imic;
    }
    
    mCopyrightText = new TextAreaIcon(p.getTextField(ProgramFieldType.PICTURE_COPYRIGHT_TYPE),f.deriveFont((float)(f.getSize() * 0.9)),width-6);
    mDescriptionText = new TextAreaIcon(showDescription ? p.getTextField(ProgramFieldType.PICTURE_DESCRIPTION_TYPE) : "",f,width-6);
  }
  
  public int getIconHeight() {
    if(mScaledIcon == null)
      return 0;
    else
      return mScaledIcon.getIconHeight() + mCopyrightText.getIconHeight() + (mShowDescription ? mDescriptionText.getIconHeight() : 0) + 10;
  }

  public int getIconWidth() {
    return mCopyrightText.getIconWidth() + 6;
  }

  public void paintIcon(Component c, Graphics g, int x, int y) {
    if(mScaledIcon == null)
      return;
    
    y += 2;    
    
    Color color = g.getColor();
    
    g.setColor(c.getBackground());
    g.fillRect(x,y,getIconWidth(),getIconHeight()-2);
    
    g.setColor(color);
    g.drawRect(x,y,getIconWidth()-1,getIconHeight()-3);

    y += 3;
    x += 3;
    
    if(mIsGrayFilter && !mIsExpired && mProgram.isExpired()) {
      ImageFilter filter = new GrayFilter(true, 60);
      mScaledIcon.setImage(c.createImage(new FilteredImageSource(mScaledIcon.getImage().getSource(),filter)));
      mIsExpired = true;
    }
    
    mScaledIcon.paintIcon(c,g,x,y);
    
    if(!mProgram.isExpired())
      g.setColor(Color.black);
    else
      g.setColor(Color.gray);
    
    mCopyrightText.paintIcon(c,g,x,y + mScaledIcon.getIconHeight());
    mDescriptionText.paintIcon(c,g,x,y + mScaledIcon.getIconHeight() + mCopyrightText.getIconHeight() + 1);
    g.setColor(color);
  }
}
