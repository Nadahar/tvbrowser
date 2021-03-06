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
import java.awt.image.RGBImageFilter;

import javax.swing.GrayFilter;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.UIManager;

import org.apache.commons.lang3.StringUtils;

import com.l2fprod.util.ColorFillFilter;

import tvbrowser.core.Settings;
import devplugin.Program;
import devplugin.ProgramFieldType;

/**
 * A class for painting a picture with copyright and description info.
 *
 * @author René Mach
 * @since 2.2.2
 */
public class PictureAreaIcon implements Icon {

  private static final int MAX_COLOR_DIFF = 20;
  /**
   * the description text icon will be null, if the string to be shown is empty
   */
  private TextAreaIcon mDescriptionText;
  private TextAreaIcon mCopyrightText;
  private ImageIcon mScaledIcon;
  private Program mProgram;
  private boolean mIsExpired;
  private boolean mIsGrayFilter;
  private boolean mShowPictureBorder;
  private int mDescriptionLines;

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
   * @param zoom If the picture should be zoomed to width.
   */
  public PictureAreaIcon(Program p, Font f, int width, boolean showDescription, boolean grayFilter, boolean zoom) {
    this(p,f,width,showDescription,grayFilter,zoom,true);
  }
  int mWidth = -1;
  /**
   * Constructor for programs with picture.
   *
   * @param p The program with the picture.
   * @param f The font for the description.
   * @param width The width of this area.
   * @param showDescription If description should be shown.
   * @param grayFilter If the image should be filtered to gray if the program is expired.
   * @param zoom If the picture should be zoomed to width.
   * @param showPictureBorder If the picture border should be shown.
   */
  public PictureAreaIcon(Program p, Font f, int width, boolean showDescription, boolean grayFilter, boolean zoom, boolean showPictureBorder) {
    mProgram = p;
    mIsExpired = false;
    mShowPictureBorder = showPictureBorder;
    mIsGrayFilter = grayFilter;
    if (showDescription) {
      mDescriptionLines = Settings.propPictureDescriptionLines.getInt();
    }
    else {
      mDescriptionLines = 0;
    }

    byte[] picture = p.getBinaryField(ProgramFieldType.PICTURE_TYPE);

    if(picture != null) {
      ImageIcon imic = new ImageIcon(picture);

      if(width == -1) {
        width = imic.getIconWidth()+6;
      }

      if(imic.getIconWidth() > width-6 || (zoom && imic.getIconWidth() != width)) {
        mScaledIcon = (ImageIcon)UiUtilities.scaleIcon(imic, width - 6);
      } else {
        mScaledIcon = imic;
      }
      
      mIconHeight = mScaledIcon.getIconHeight();
    }

    mWidth = width;
    
    String copyright = p.getTextField(ProgramFieldType.PICTURE_COPYRIGHT_TYPE);
    
    if(copyright != null && copyright.toLowerCase().startsWith("(c)")) {
      copyright = "\u00A9" + copyright.substring(3);
    }
    
    mCopyrightText = new TextAreaIcon(copyright,f.deriveFont((float)(f.getSize() * 0.9)),width-9);
    String pictureText = showDescription ? p.getTextField(ProgramFieldType.PICTURE_DESCRIPTION_TYPE) : "";
    if (StringUtils.isNotEmpty(pictureText)) {
      mDescriptionText = new TextAreaIcon(pictureText,f,width-9);
      mDescriptionText.setMaximumLineCount(mDescriptionLines);
      
    }
    else {
      // reset show description as the string is empty
      mDescriptionLines = 0;
    }
    
    mIconHeight = mIconHeight + mCopyrightText.getIconHeight() + (mDescriptionLines > 0 ? mDescriptionText.getIconHeight() : 0) + 10;
  }
  
  
  private int mIconHeight = 0;

  public int getIconHeight() {
    if(mScaledIcon == null) {
      return 0;
    } else {
      return mIconHeight;// + mCopyrightText.getIconHeight() + (mDescriptionLines > 0 ? mDescriptionText.getIconHeight() : 0) + 10;
    }
  }

  public int getIconWidth() {
    return mWidth;
  }

  public void paintIcon(final Component c, Graphics g, int x, int y) {
    if(mScaledIcon == null) {
      return;
    }

    y += 2;

    Color color = g.getColor();

    if(!Settings.propTableBackgroundStyle.getString().equals("uiColor") && !Settings.propTableBackgroundStyle.getString().equals("uiTimeBlock") && !UiUtilities.colorsInEqualRange(c.getBackground(),c.getForeground(),MAX_COLOR_DIFF) && !mProgram.isExpired()) {
      g.setColor(c.getBackground());
      g.fillRect(x,y,getIconWidth(),getIconHeight()-2);
    }
    else if(Settings.propTableBackgroundStyle.getString().equals("uiColor") || Settings.propTableBackgroundStyle.getString().equals("uiTimeBlock") && !mProgram.isExpired()) {
      g.setColor(UIManager.getColor("List.background"));
      g.fillRect(x,y,getIconWidth(),getIconHeight()-2);
    }
    
    g.setColor(color);
    
    if(mShowPictureBorder) {
      g.drawRect(x,y,getIconWidth()-1,getIconHeight()-3);
    }

    y += 3;
    x += 3;

    if(mIsGrayFilter && !mIsExpired && mProgram.isExpired()) {
      ImageFilter filter = UIManager.getDefaults().getColor("List.background").equals(Color.black) ? new ColorFillFilter(new Color(50,50,50)) : new GrayFilter(true, 60);
      mScaledIcon.setImage(c.createImage(new FilteredImageSource(mScaledIcon.getImage().getSource(),filter)));
      mIsExpired = true;
    }

    if(c.getForeground().getAlpha() != 255) {
      ImageFilter filter = new RGBImageFilter() {
        public int filterRGB(int x, int y, int rgb) {
          if ((rgb & 0xff000000) != 0) {
            return (rgb & 0xffffff) | (c.getForeground().getAlpha() << 24);
          }
          return rgb;
        }
      };

      mScaledIcon.setImage(c.createImage(new FilteredImageSource(mScaledIcon.getImage().getSource(),filter)));
    }

    mScaledIcon.paintIcon(c,g,x,y);

    /*
    if(!mProgram.isExpired()) {
      g.setColor(color);
    } else {
      g.setColor(color);
    }
    */
    mCopyrightText.paintIcon(null,g,x,y + mScaledIcon.getIconHeight());
    if (mDescriptionLines > 0 && mDescriptionText != null) {
      mDescriptionText.paintIcon(null,g,x,y + mScaledIcon.getIconHeight() + mCopyrightText.getIconHeight() + 1);
    }
    g.setColor(color);
  }
}
