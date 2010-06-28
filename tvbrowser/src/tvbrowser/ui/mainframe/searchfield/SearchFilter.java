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
package tvbrowser.ui.mainframe.searchfield;

import util.exc.TvBrowserException;
import util.ui.SearchFormSettings;
import devplugin.Program;
import devplugin.ProgramFieldType;
import devplugin.ProgramFilter;
import devplugin.ProgramSearcher;

/**
 * This Class implements a Filter for the Programtable.
 * 
 * The Filter only accepts Programs that match the Search-Criteria
 * 
 * @author bodum
 */
public class SearchFilter implements ProgramFilter {
  /** The localizer of this class. */
  private static final util.ui.Localizer mLocalizer
    = util.ui.Localizer.getLocalizerFor(SearchFilter.class);
  /** Instance */
  private static SearchFilter mInstance;
  /** Settings of the Search */
  private SearchFormSettings mSearcherForm;
  /** The Searcher */
  private ProgramSearcher mSearch;
  /** Fields to search in */
  private ProgramFieldType[] mFieldTypes;

  /**
   * Private Constructor
   */
  private SearchFilter() {}

  /**
   * Set the Search-Settings
   * @param searchFormSettings Search-Settings
   * @throws TvBrowserException
   */
  public void setSearch(SearchFormSettings searchFormSettings) throws TvBrowserException {
    mSearcherForm = searchFormSettings;
    mSearch = mSearcherForm.createSearcher();
    mFieldTypes = mSearcherForm.getFieldTypes();
  }

  /**
   * Deactivate the Search-Filter
   */
  void deactivateSearch() {
    mSearcherForm = null;
  }

  /**
   * @return true, if the Filter is active
   */
  public boolean isActive() {
    return mSearcherForm != null;
  }

  public boolean accept(Program prog) {
    if (mSearch == null) {
      return true;
    }
    return mSearch.matches(prog, mFieldTypes);
  }

  public String getName() {
    return mLocalizer.msg("searchFor", "Search for {0}", mSearcherForm.getSearchText());
  }
  
  /**
   * Return the Name
   */
  public String toString() {
    return getName();
  }

  /**
   * @return Instance of this Filter
   */
  public static SearchFilter getInstance() {
    if (mInstance == null) {
      mInstance = new SearchFilter();
    }
    
    return mInstance;
  }

}