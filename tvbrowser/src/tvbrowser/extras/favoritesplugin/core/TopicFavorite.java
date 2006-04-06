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

import devplugin.Program;

import java.io.ObjectOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

import util.exc.TvBrowserException;
import util.ui.SearchFormSettings;

public class TopicFavorite extends Favorite {

  public static final String TYPE_ID = "topic";


  private String mTopic;
  private SearchFormSettings mSearchFormSettings;

  public TopicFavorite(ObjectInputStream in) throws IOException, ClassNotFoundException {
    super(in);
    in.readInt();  // version
    mTopic = (String)in.readObject();
    mSearchFormSettings = new SearchFormSettings(mTopic);
    mSearchFormSettings.setSearchIn(SearchFormSettings.SEARCH_IN_ALL);
  }

  public TopicFavorite(String topic) {
    super();
    mTopic = topic;
    setName(mTopic);
    mSearchFormSettings = new SearchFormSettings(mTopic);
    mSearchFormSettings.setSearchIn(SearchFormSettings.SEARCH_IN_ALL);
  }


  public String getTypeID() {
    return TYPE_ID;
  }


  public SearchFormSettings getSearchFormSettings() {
    return mSearchFormSettings;
  }

  public void _writeData(ObjectOutputStream out) throws IOException {
    out.writeInt(1);  // version
    out.writeObject(mTopic);
  }

  public void _handleContainingPrograms(Program[] progs) {
    //To change body of implemented methods use File | Settings | File Templates.
  }

  public void _unmarkPrograms() {
    //To change body of implemented methods use File | Settings | File Templates.
  }


  public boolean _contains(Program prog) {
    return false;  //To change body of implemented methods use File | Settings | File Templates.
  }

  public void _updatePrograms() throws TvBrowserException {
    //To change body of implemented methods use File | Settings | File Templates.
  }
}
