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



import java.io.ObjectOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.awt.*;

import util.ui.SearchFormSettings;
import util.exc.TvBrowserException;
import tvbrowser.extras.favoritesplugin.FavoriteConfigurator;

import javax.swing.*;

import devplugin.Program;
import devplugin.Channel;
import devplugin.ProgramSearcher;

public class TitleFavorite extends Favorite {

  public static final util.ui.Localizer mLocalizer
     = util.ui.Localizer.getLocalizerFor(TitleFavorite.class);

  public static final String TYPE_ID = "title";


  private String mProgramTitle;
  private SearchFormSettings mSearchFormSettings;

  public TitleFavorite(ObjectInputStream in) throws IOException, ClassNotFoundException {
    super(in);
    in.readInt();  // version
    mProgramTitle = (String)in.readObject();
    mSearchFormSettings = new SearchFormSettings(mProgramTitle);
  }

  public TitleFavorite(String programTitle) {
    super();
    setName(programTitle);
    mProgramTitle = programTitle;
    mSearchFormSettings = new SearchFormSettings(mProgramTitle);
  }

  public String getTypeID() {
    return TYPE_ID;
  }

  public String getProgramTitle() {
    return mProgramTitle;
  }


  public FavoriteConfigurator createConfigurator() {
    return new Configurator();
  }




  protected void internalWriteData(ObjectOutputStream out) throws IOException {
    out.writeInt(1);  // version
    out.writeObject(mProgramTitle);
  }


  protected Program[] internalSearchForPrograms(Channel[] channelArr) throws TvBrowserException {

    SearchFormSettings searchForm = mSearchFormSettings;

    ProgramSearcher searcher = searchForm.createSearcher();
    return searcher.search(searchForm.getFieldTypes(),
                                                new devplugin.Date().addDays(-1),
                                                1000,
                                                channelArr,
                                                false
                                                );
  }


  class Configurator implements FavoriteConfigurator {

    private JTextField mSearchTextTf;

    public Configurator() {
      mSearchTextTf = new JTextField(mSearchFormSettings.getSearchText());
    }

    public JPanel createConfigurationPanel() {
      JPanel panel = new JPanel(new GridLayout(-1, 1));
      panel.add(new JLabel(mLocalizer.msg("title-favorite.term","Any program whose title contains this term will be marked as a favorite:")));

      panel.add(mSearchTextTf);
      return panel;
    }

    public void save() {
      String searchText = mSearchTextTf.getText();
      mProgramTitle = searchText;
      mSearchFormSettings.setSearchText(searchText);
    }
  }


}
