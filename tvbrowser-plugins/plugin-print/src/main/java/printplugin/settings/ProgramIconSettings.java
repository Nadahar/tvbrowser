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
 * The interface for the program painting settings.
 */
public interface ProgramIconSettings {
  
  /**
   * Gets the Font for the program title.
   * 
   * @return The Font for the program title.
   */
  public Font getTitleFont();
  
  /**
   * Gets the Font for the program text.
   * 
   * @return The Font for the program text.
   */
  public Font getTextFont();
  
  /**
   * Gets the Font for the time text.
   * 
   * @return The Font for the time text.
   */
  public Font getTimeFont();
  
  /**
   * Gets the width of the time field.
   * 
   * @return The width of the time field.
   */
  public int getTimeFieldWidth();
  
  /**
   * Gets the program field types to paint.
   * 
   * @return The program field types to paint.
   */
  public ProgramFieldType[] getProgramInfoFields();
  
  public String[] getProgramTableIconPlugins();
  
  public boolean getPaintExpiredProgramsPale();
  
  public boolean getPaintProgramOnAir();
  public boolean getPaintPluginMarks();
  
}