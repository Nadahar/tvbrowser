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
 *     $Date$
 *   $Author$
 * $Revision$
 */

package printplugin.settings;

import java.awt.Color;
import java.awt.Font;

import devplugin.ProgramFieldType;


public class MutableProgramIconSettings implements ProgramIconSettings {

  private Font mTitleFont, mTextFont, mTimeFont;
  private int mTimeFieldWidth;
  private ProgramFieldType[] mProgramInfoFields;
  private String[] mProgramTableIconPlugins;
  private Color mColorOnAir_dark, mColorOnAir_light, mColorMarked;
  private boolean mPaintExpiredProgramsPale, mPaintProgramOnAir, mPaintPluginMarks;

  public MutableProgramIconSettings(ProgramIconSettings settings) {
    mTitleFont = settings.getTitleFont();
    mTextFont = settings.getTextFont();
    mTimeFont = settings.getTimeFont();
    mTimeFieldWidth = settings.getTimeFieldWidth();
    mProgramInfoFields = settings.getProgramInfoFields();
    mProgramTableIconPlugins = settings.getProgramTableIconPlugins();
    mColorOnAir_dark = settings.getColorOnAir_dark();
    mColorOnAir_light = settings.getColorOnAir_light();
    mColorMarked = settings.getColorMarked();
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

  public void setColorOnAir_dark(Color colorOnAir_dark) {
    mColorOnAir_dark = colorOnAir_dark;
  }

  public void setColorOnAir_light(Color colorOnAir_light) {
    mColorOnAir_light = colorOnAir_light;
  }

  public void setColorMarked(Color colorMarked) {
    mColorMarked = colorMarked;
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

  public Color getColorOnAir_dark() {
    return mColorOnAir_dark;
  }

  public Color getColorOnAir_light() {
    return mColorOnAir_light;
  }

  public Color getColorMarked() {
    return mColorMarked;
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
