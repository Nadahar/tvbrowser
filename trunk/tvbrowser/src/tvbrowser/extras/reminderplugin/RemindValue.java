/*
 * TV-Browser
 * Copyright (C) 2013 TV-Browser Team (dev@tvbrowser.org)
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
 * SVN information:
 *     $Date$
 *   $Author$
 * $Revision$
 */
package tvbrowser.extras.reminderplugin;

public class RemindValue {
  private static final util.ui.Localizer mLocalizer
    = util.ui.Localizer.getLocalizerFor(RemindValue.class);
  
  private int mMinutes;
  private String mTranslation;
  
  public RemindValue(int minutes) {
    mMinutes = minutes;
    mTranslation = mLocalizer.msg("remind."+(minutes > 1440 ? "week" : minutes),minutes < 0 ? "Remind me when the program runs "+minutes+" minutes" : "Remind me "+minutes+" minutes before");
  }
  
  public int getMinutes() {
    return mMinutes;
  }
  
  public String toString() {
    return mTranslation;
  }
}
