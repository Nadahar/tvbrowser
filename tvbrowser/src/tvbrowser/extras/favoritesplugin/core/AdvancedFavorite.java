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

package tvbrowser.extras.favoritesplugin.core;

import tvbrowser.extras.favoritesplugin.ClassicFavorite;
import tvbrowser.extras.favoritesplugin.FavoriteConfigurator;
import devplugin.Program;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import util.ui.SearchFormSettings;
import util.ui.SearchForm;
import javax.swing.*;

public class AdvancedFavorite extends Favorite {

  private ClassicFavorite mClassicFavorite;

  public static final String TYPE_ID = "advanced";


  public AdvancedFavorite(ObjectInputStream in) throws IOException, ClassNotFoundException {
    super(in);
    mClassicFavorite = new ClassicFavorite(in);
    setName(mClassicFavorite.getTitle());
  }

  public AdvancedFavorite(ClassicFavorite fav) {
    super();
    mClassicFavorite = fav;
    setName(mClassicFavorite.getTitle());
  }

  public AdvancedFavorite(String searchText) {
    super();
    mClassicFavorite = new ClassicFavorite(searchText);
  }

 
  public String getTypeID() {
    return TYPE_ID;
  }


  public ClassicFavorite getClassicFavorite() {
    return mClassicFavorite;
  }

  public SearchFormSettings getSearchFormSettings() {
    return mClassicFavorite.getSearchFormSettings();
  }

  public String getName() {
    return mClassicFavorite.getTitle();
  }

  public Program[] getPrograms() {
    return mClassicFavorite.getPrograms();
  }


  public FavoriteConfigurator createConfigurator() {
    return new Configurator();
  }

  protected void _writeData(ObjectOutputStream out) throws IOException {
    mClassicFavorite.writeData(out);
  }



  class Configurator implements FavoriteConfigurator {

    private SearchForm mSearchForm;

    public Configurator() {

    }

    public JPanel createConfigurationPanel() {
      mSearchForm = new SearchForm(true, false, false, SearchForm.LAYOUT_HORIZONTAL);
      mSearchForm.setSearchFormSettings(getSearchFormSettings());
      return mSearchForm;
    }

    public void save() {
      SearchFormSettings settings = mSearchForm.getSearchFormSettings();
      getSearchFormSettings().setCaseSensitive(settings.getCaseSensitive());
      getSearchFormSettings().setNrDays(settings.getNrDays());
      getSearchFormSettings().setSearcherType(settings.getSearcherType());
      getSearchFormSettings().setSearchIn(settings.getSearchIn());
      getSearchFormSettings().setSearchText(settings.getSearchText());
      getSearchFormSettings().setUserDefinedFieldTypes(settings.getUserDefinedFieldTypes());

    }
  }


}
