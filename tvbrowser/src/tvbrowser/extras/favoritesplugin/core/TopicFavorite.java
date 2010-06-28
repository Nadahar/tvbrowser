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

import java.awt.GridLayout;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import tvbrowser.extras.favoritesplugin.FavoriteConfigurator;
import util.ui.SearchFormSettings;

public class TopicFavorite extends Favorite {

  private static final util.ui.Localizer mLocalizer
     = util.ui.Localizer.getLocalizerFor(TopicFavorite.class);

  public static final String TYPE_ID = "topic";

  private String mTopic;

  public TopicFavorite(ObjectInputStream in) throws IOException, ClassNotFoundException {
    super(in);
    in.readInt();  // version
    mTopic = (String)in.readObject();
    mSearchFormSettings = new SearchFormSettings(mTopic);
    mSearchFormSettings.setSearchIn(SearchFormSettings.SEARCH_IN_ALL);
  }

  public TopicFavorite(String topic) {
    super();
    topic = topic.trim();
    mTopic = topic;
    setName(mTopic);
    mSearchFormSettings = new SearchFormSettings(mTopic);
    mSearchFormSettings.setSearchIn(SearchFormSettings.SEARCH_IN_ALL);
  }


  @Override
  public String getTypeID() {
    return TYPE_ID;
  }


  @Override
  protected void internalWriteData(ObjectOutputStream out) throws IOException {
    out.writeInt(1);  // version
    out.writeObject(mTopic);
  }

  @Override
  public FavoriteConfigurator createConfigurator() {
    return new Configurator();
  }


  class Configurator implements FavoriteConfigurator {

    private JTextField mSearchTextTf;

    public Configurator() {
      mSearchTextTf = new JTextField(mSearchFormSettings.getSearchText());
    }

    public JPanel createConfigurationPanel() {
      JPanel panel = new JPanel(new GridLayout(-1, 1));
      panel.add(new JLabel(mLocalizer.msg("topic-favorite.term","Any program containing this term will be marked as a favorite:")));
      panel.add(mSearchTextTf);
      return panel;
    }

    public void save() {
      String searchText = mSearchTextTf.getText();
      mSearchFormSettings.setSearchText(searchText);
    }

    public boolean check() {
      if (mSearchTextTf.getText().trim().equals("")) {
        JOptionPane.showMessageDialog(mSearchTextTf,
            mLocalizer.msg("missingTopic.message", "Please specify a topic for the favorite!"),
            mLocalizer.msg("missingTopic.title", "Invalid options"),
            JOptionPane.WARNING_MESSAGE);
        return false;
      }
      return true;
    }
  }

}
