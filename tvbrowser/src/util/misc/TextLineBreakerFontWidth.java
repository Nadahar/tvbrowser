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
 *
 * CVS information:
 *  $RCSfile$
 *   $Source$
 *     $Date$
 *   $Author$
 * $Revision$
 */
package util.misc;

import java.awt.Font;
import java.awt.FontMetrics;
import java.util.HashMap;

import javax.swing.JLabel;

/**
 * Breaks a text into lines using Font-Metrics.
 *
 * @author Til Schneider, www.murfman.de
 */
public class TextLineBreakerFontWidth extends TextLineBreakerStringWidth{

  private static final HashMap<Font, FontMetrics> FONT_METRICS_CACHE = new HashMap<Font, FontMetrics>();

  /** The helper label. */
  private static final JLabel HELPER_LABEL = new JLabel();
  /** Font-Metrics of current Font */
  private FontMetrics mFontMetrics;

  /**
   * Create the LineBreaker
   * @param font Font to use for Width-Calculation
   */
  public TextLineBreakerFontWidth(Font font) {
    super();
    setFont(font);
  }

  /**
   * Set the Font to use for Width-Calculation
   * @param font Font to use
   */
  public void setFont(Font font) {
    mFontMetrics = FONT_METRICS_CACHE.get(font);
    if (mFontMetrics == null) {
      mFontMetrics = HELPER_LABEL.getFontMetrics(font);
      FONT_METRICS_CACHE.put(font, mFontMetrics);
    }
    setSpaceWidth(mFontMetrics.charWidth(' '));
    setMinusWidth(mFontMetrics.charWidth('-'));
  }

  /**
   * Get the width of a String
   * @param str Calculate Width of this String
   * @return Width of String
   */
  @Override
  public int getStringWidth(String str) {
    return mFontMetrics.stringWidth(str);
  }

}