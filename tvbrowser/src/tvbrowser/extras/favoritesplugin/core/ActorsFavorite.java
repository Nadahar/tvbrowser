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
import tvbrowser.extras.favoritesplugin.FavoriteConfigurator;
import devplugin.ProgramFieldType;
import devplugin.PluginManager;

import javax.swing.*;

public class ActorsFavorite extends Favorite {

  public static final util.ui.Localizer mLocalizer
       = util.ui.Localizer.getLocalizerFor(ActorsFavorite.class);


  public static final String TYPE_ID = "actors";

  public static final String[] SEPERATOR_ARR = new String[]{",",";"};

  private SearchFormSettings mSearchFormSettings;

  private String mActors;

  public ActorsFavorite(String actors) {
    super();
    mActors = actors;
    mSearchFormSettings = createSearchFormSettings();
  }

  public ActorsFavorite(ObjectInputStream in) throws IOException, ClassNotFoundException {
    super(in);
    in.readInt(); // version
    mActors = (String)in.readObject();
    mSearchFormSettings = createSearchFormSettings();
  }


  private void createSearchStringForActor(String actor, StringBuffer buf) {
    String[] names = actor.split(" ");
    for (int j=0; j<names.length-1; j++) {
      buf.append(names[j]).append(" AND ");
    }
    if (names.length > 0) {
      buf.append(names[names.length-1]);
    }
  }

  private String createSearchString() {
    String[] actorsArr = createActorsArr(mActors);
    StringBuffer buf = new StringBuffer();
    for (int i=0; i<actorsArr.length-1; i++) {
      buf.append("(");
      createSearchStringForActor(actorsArr[i], buf);
      buf.append(") AND");
    }
    if (actorsArr.length > 0) {
      createSearchStringForActor(actorsArr[actorsArr.length-1], buf);
    }


    return buf.toString();
  }

  private SearchFormSettings createSearchFormSettings() {
    SearchFormSettings formSettings = new SearchFormSettings(createSearchString());
    formSettings.setSearchIn(SearchFormSettings.SEARCH_IN_USER_DEFINED);
    formSettings.setSearcherType(PluginManager.SEARCHER_TYPE_BOOLEAN);
    formSettings.setUserDefinedFieldTypes(new ProgramFieldType[]{ProgramFieldType.ACTOR_LIST_TYPE, ProgramFieldType.DESCRIPTION_TYPE, ProgramFieldType.SHORT_DESCRIPTION_TYPE});
    return formSettings;
  }

  private String[] createActorsArr(String actors) {
    String s = actors;
    for (int i=1; i< SEPERATOR_ARR.length; i++) {
      s = actors.replaceAll(SEPERATOR_ARR[i], SEPERATOR_ARR[0]);
    }

    String[] actorsArr = s.split(SEPERATOR_ARR[0]);
    String[] result = new String[actorsArr.length];
    for (int i=0; i<result.length; i++) {
      actorsArr[i] = actorsArr[i].trim();
    }
    return actorsArr;
  }

  public String getTypeID() {
    return TYPE_ID;
  }

  public SearchFormSettings getSearchFormSettings() {
    return mSearchFormSettings;
  }


  protected void _writeData(ObjectOutputStream out) throws IOException {
    out.writeInt(1); // version
    out.writeObject(mActors);
  }


  public FavoriteConfigurator createConfigurator() {
    return new Configurator();
  }

  class Configurator implements FavoriteConfigurator {

    private JTextField mSearchTextTf;

    public Configurator() {
      mSearchTextTf = new JTextField(mActors);
    }

    public JPanel createConfigurationPanel() {
      JPanel panel = new JPanel(new GridLayout(-1, 1));
      panel.add(new JLabel(mLocalizer.msg("actors-favorite.term","Any program containing all of theses actors will be marked as a favorite:")));
      panel.add(mSearchTextTf);
      return panel;
    }

    public void save() {
      String searchText = mSearchTextTf.getText();
      getSearchFormSettings().setSearchText(searchText);
    }
  }

}
