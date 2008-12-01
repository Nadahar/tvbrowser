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
 *     $Date: 2007-10-02 10:19:08 +0200 (Di, 02 Okt 2007) $
 *   $Author: Bananeweizen $
 * $Revision: 3966 $
 */
package movieawardplugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import devplugin.Program;
import devplugin.ProgramFieldType;

public class Movie {
  private String mId;
  private int mYear;
  private String mDirector;
  private HashMap<String, String> mTitle = new HashMap<String, String>(4);
  private HashMap<String, ArrayList<String>> mAlternativeTitle = new HashMap<String, ArrayList<String>>(
      4);
  private String mOriginalTitle;

  public Movie(String id) {
    mId = id;
  }
  public String getId() {
    return mId;
  }

  public void addTitle(String lang, String title, boolean original) {
    if (original) {
      mOriginalTitle = title;
    }
    mTitle.put(lang, title);
  }

  public void setProductionYear(int year) {
    mYear = year;
  }

  public int getProductionYear() {
    return mYear;
  }

  public void setYear(int year) {
    mYear = year;
  }

  public String getDirector() {
    return mDirector;
  }

  public void setDirector(String director) {
    mDirector = director;
  }

  public void addAlternativeTitle(String lang, String title) {
    ArrayList<String> list = mAlternativeTitle.get(lang);
    
    if (list == null) {
      list = new ArrayList<String>();
      mAlternativeTitle.put(lang, list);
    }
    list.add(title.toLowerCase());
  }

  public boolean matchesProgram(Program program) {
    // avoid String comparison by filtering for year first
    int year = program.getIntField(ProgramFieldType.PRODUCTION_YEAR_TYPE);
    if (year > 0) {
      if (!(year >= mYear - 1 && year <= mYear + 1)) {
        return false;
      }
    }
    // store all multiple used variables to avoid re-getting
    final String country = program.getChannel().getCountry();
    final String localizedTitle = mTitle.get(country);
    final String programTitle = program.getTitle();
    if (programTitle.equalsIgnoreCase(localizedTitle)
        || (mOriginalTitle != null && programTitle.equalsIgnoreCase(
            mOriginalTitle))) {
      return true;
    }
    // do not use toLowerCase on each program repeatedly
    final List<String> alternativeTitles = mAlternativeTitle.get(country);
    if (alternativeTitles != null) {
      for (String alternateTitle : alternativeTitles) {
        if (programTitle.equalsIgnoreCase(alternateTitle)) {
          return true;
        }
      }
    }

    return false;
  }

}
