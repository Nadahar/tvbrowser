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
 *     $Date: 2011-07-24 22:09:15 +0200 (So, 24 Jul 2011) $
 *   $Author: ds10 $
 * $Revision: 7060 $
 */
package tvbrowser.core.filters;

import devplugin.Program;
import devplugin.ProgramFilter;

/**
 * This Filter filters for infobits, depending on the given key name
 */
public class InfoBitFilter implements ProgramFilter {
  
  private int[] mInfoBits;
  private String mName;
  private String mKey;

  public InfoBitFilter(String name) {
	if (name.equals("[SUBTITLE_FILTER]")) {
	  mName = "Subtitled";
	  mKey = name;
	  mInfoBits = new int[] {
		  Program.INFO_SUBTITLE_FOR_AURALLY_HANDICAPPED, Program.INFO_ORIGINAL_WITH_SUBTITLE, Program.INFO_SIGN_LANGUAGE};
	} else if (name.equals("[HD_FILTER]")) {
	  mName = "HD";
	  mKey = name;
	  mInfoBits = new int[] { Program.INFO_VISION_HD };
	} else if (name.equals("[AUDIO_DESCRIPTION_FILTER]")) {
	  mName = "Audiodescription";
	  mKey = name;
	  mInfoBits = new int[] {
		  Program.INFO_AUDIO_DESCRIPTION};
	}
	else { 
	  throw new IllegalArgumentException("Unknown filter: "+name);	  
	}
  }
  
  public InfoBitFilter(String name, String key, int infoBit) {
	this(name, key, new int[] {infoBit});
  }
  
  public InfoBitFilter(String name, String key, int[] infoBits) {
	mName = name;
	mKey = key;
	mInfoBits = infoBits;
  }
  
  /**
   * Localizer
   */
  private static final util.ui.Localizer mLocalizer = util.ui.Localizer.getLocalizerFor(InfoBitFilter.class);

  /**
   * Accept only programs with subtitle or sign language
   *
   * @param prog Program to check
   * @return true if prog is subtitled
   */
  public boolean accept(devplugin.Program prog) {
    int info = prog.getInfo();
    
    if (info < 1) {
      return false;
    }
    boolean accept = false;
    for (int bit: mInfoBits) {
      accept = accept || bitSet(info, bit);
    }
    return accept;

  }

  /**
   * Checks if bits are set
   *
   * @param num     check in here
   * @param pattern this pattern
   * @return Pattern set?
   */
  private boolean bitSet(int num, int pattern) {
    return (num & pattern) == pattern;
  }
  

  public String getName() {
    return toString();
  }
  
  public String getKey() {
    return mKey;
  }


  /**
   * Name of Filter
   */
  public String toString() {
    return mLocalizer.msg(mName, mName);
  }

  public boolean equals(Object o) {
    return o instanceof ProgramFilter && getClass().equals(o.getClass())
        && getName().equals(((ProgramFilter) o).getName());
  }
}