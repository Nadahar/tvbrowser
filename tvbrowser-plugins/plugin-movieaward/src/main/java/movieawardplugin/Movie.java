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

import devplugin.Program;
import devplugin.ProgramFieldType;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;

public class Movie {
  private String mId;
  private int mYear;
  private String mDirector;
  private HashMap<String, String> mTitle = new HashMap<String, String>();
  private HashMap<String, ArrayList<String>> mAlternativeTitle = new HashMap<String, ArrayList<String>>();
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
    List<String> list = getAlternativeTitles(lang);
    list.add(title);
  }

  private List<String> getAlternativeTitles(String lang) {
    ArrayList<String> list = mAlternativeTitle.get(lang);

    if (list == null) {
      list = new ArrayList<String>();
      mAlternativeTitle.put(lang, list);
    }

    return list;
  }

  public boolean matchesProgram(Program program) {
    if (program.getTitle().equals(mTitle.get(program.getChannel().getCountry())) ||
        getAlternativeTitles(program.getChannel().getCountry()).contains(program.getTitle()) ||
        (mOriginalTitle != null && program.getTitle().equals(mOriginalTitle))) {
      int year = program.getIntField(ProgramFieldType.PRODUCTION_YEAR_TYPE);

      if (year != 0) {
        if ((year == mYear)|| (year - 1==mYear) || (year + 1 == mYear)){
          return true;  
        }
      } else {
        // No year present
        return true;
      }
    }

    return false;
  }

}
