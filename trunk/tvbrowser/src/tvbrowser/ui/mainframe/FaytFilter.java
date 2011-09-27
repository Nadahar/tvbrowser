/*
 * TV-Browser
 * Copyright (C) 2011 TV-Browser team (dev@tvbrowser.org)
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
package tvbrowser.ui.mainframe;

import devplugin.Program;
import devplugin.ProgramFilter;

/**
 * The filter used for FindAsYouType.
 * <p>
 * @author René Mach
 * @since 3.0.3
 */
public class FaytFilter implements ProgramFilter {
  private static FaytFilter mInstance;
  private String mText;
  
  private FaytFilter() {
    mInstance = this;
    mText = "";
  }
  
  /**
   * Get the instance of this class.
   * <p>
   * @return The instance of this class.
   */
  public static FaytFilter getInstance() {
    if(mInstance == null) {
      new FaytFilter();
    }
    
    return mInstance;
  }
  
  /**
   * Set the search value for this filter.
   * <p>
   * @param value The search value for this filter.
   */
  public void setSearchString(String value) {
    mText = value;
  }
  
  @Override
  public boolean accept(Program program) {
    return program.getTitle().toLowerCase().contains(mText.toLowerCase());
  }

  @Override
  public String getName() {
    return "\"" + mText + "\"";
  }

}
