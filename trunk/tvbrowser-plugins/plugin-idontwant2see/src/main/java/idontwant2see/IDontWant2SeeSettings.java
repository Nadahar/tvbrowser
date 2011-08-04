/*
 * IDontWant2See - Plugin for TV-Browser
 * Copyright (C) 2008 René Mach
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
package idontwant2see;

import java.util.ArrayList;

import devplugin.Date;
import devplugin.Program;

/**
 * @author Bananeweizen
 *
 */
public class IDontWant2SeeSettings {

  private boolean mSimpleMenu = true;
  private boolean mSwitchToMyFilter = true;
  private String mLastEnteredExclusionString = "";
  private Date mLastUsedDate = Date.getCurrentDate();
  private ArrayList<IDontWant2SeeListEntry> mSearchList = new ArrayList<IDontWant2SeeListEntry>();
  
  private byte mProgramImportance = Program.DEFAULT_PROGRAM_IMPORTANCE;

  public IDontWant2SeeSettings() {
  }

  public void setSimpleMenu(final boolean value) {
    mSimpleMenu = value;
  }

  public boolean isSimpleMenu() {
    return mSimpleMenu;
  }

  public boolean isSwitchToMyFilter() {
    return mSwitchToMyFilter;
  }

  public void setSwitchToMyFilter(final boolean value) {
    mSwitchToMyFilter = value;
  }

  public String getLastEnteredExclusionString() {
    return mLastEnteredExclusionString;
  }

  public void setLastEnteredExclusionString(final String value) {
    mLastEnteredExclusionString = value;
    IDontWant2See.getInstance().clearCache();
  }

  public void setLastUsedDate(final Date value) {
    mLastUsedDate = value;    
  }

  public Date getLastUsedDate() {
    return mLastUsedDate;
  }

  public ArrayList<IDontWant2SeeListEntry> getSearchList() {
    return mSearchList;
  }

  public void setSearchList(final ArrayList<IDontWant2SeeListEntry> value) {
    mSearchList = value;
    IDontWant2See.getInstance().clearCache();
  }

  public byte getProgramImportance() {
    return mProgramImportance;
  }
  
  public void setProgramImportance(final byte programImportance) {
    mProgramImportance = programImportance;
  }

	public void showAgain(Program program) {
    final int index = IDontWant2See.getInstance().getSearchTextIndexForProgram(program);
    if (index >= 0) {
    	getSearchList().remove(index);
    }
	}
}
