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
 *     $Date: 2007-09-21 21:48:38 +0200 (Fr, 21 Sep 2007) $
 *   $Author: ds10 $
 * $Revision: 3905 $
 */

package printplugin.settings;

import java.awt.Font;

import devplugin.ProgramFieldType;

/**
 * The print settings for a mutable program.
 */
public class MutableProgramIconSettings implements ProgramIconSettings {

  private Font mTitleFont, mTextFont, mTimeFont;
  private int mTimeFieldWidth;
  private ProgramFieldType[] mProgramInfoFields;
  private String[] mProgramTableIconPlugins;
  private boolean mPaintExpiredProgramsPale, mPaintProgramOnAir, mPaintPluginMarks;

  /**
   * Creates an instance of this class with the given settings.
   * 
   * @param settings The settings for this class.
   */
  public MutableProgramIconSettings(ProgramIconSettings settings) {
    mTitleFont = settings.getTitleFont();
    mTextFont = settings.getTextFont();
    mTimeFont = settings.getTimeFont();
    mTimeFieldWidth = settings.getTimeFieldWidth();
    mProgramInfoFields = settings.getProgramInfoFields();
    mProgramTableIconPlugins = settings.getProgramTableIconPlugins();
    mPaintExpiredProgramsPale = settings.getPaintExpiredProgramsPale();
    mPaintProgramOnAir = settings.getPaintProgramOnAir();
    mPaintPluginMarks = settings.getPaintPluginMarks();
  }
  
  public void setTitleFont(Font titleFont) {
    mTitleFont = titleFont;
  }

  public void setTextFont(Font textFont) {
    mTextFont = textFont;
  }

  public void setTimeFont(Font timeFont) {
    mTimeFont = timeFont;
  }

  public void setTimeFieldWidth(int timeFieldWidth) {
    mTimeFieldWidth = timeFieldWidth;
  }

  public void setProgramInfoFields(ProgramFieldType[] programInfoFields) {
    mProgramInfoFields = programInfoFields;
  }

  public void setProgramTableIconPlugins(String[] programTableIconPlugins) {
    mProgramTableIconPlugins = programTableIconPlugins;
  }

  public void setPaintExpiredProgramsPale(boolean paintExpiredProgramsPale) {
    mPaintExpiredProgramsPale = paintExpiredProgramsPale;
  }

  public void setPaintProgramOnAir(boolean paintProgramOnAir) {
    mPaintProgramOnAir = paintProgramOnAir;
  }

  public void setPaintPluginMarks(boolean paintPluginMarks) {
    mPaintPluginMarks = paintPluginMarks;
  }

  public Font getTitleFont() {
    return mTitleFont;
  }

  public Font getTextFont() {
    return mTextFont;
  }

  public Font getTimeFont() {
    return mTimeFont;
  }

  public int getTimeFieldWidth() {
    return mTimeFieldWidth;
  }

  public ProgramFieldType[] getProgramInfoFields() {
    return mProgramInfoFields;
  }

  public String[] getProgramTableIconPlugins() {
    return mProgramTableIconPlugins;
  }

  public boolean getPaintExpiredProgramsPale() {
    return mPaintExpiredProgramsPale;
  }

  public boolean getPaintProgramOnAir() {
    return mPaintProgramOnAir;
  }

  public boolean getPaintPluginMarks() {
    return mPaintPluginMarks;
  }

}
