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

import devplugin.Plugin;
import devplugin.PluginInfo;
import devplugin.Version;
import util.ui.Localizer;
import java.util.ArrayList;

public class MovieAwardPlugin extends Plugin {
  /**
   * Translator
   */
  private static final Localizer mLocalizer = Localizer.getLocalizerFor(MovieAwardPlugin.class);

  private static final Version mVersion = new Version(0, 1);
  private PluginInfo mPluginInfo;
  private ArrayList<MovieAward> mMovieAwards = new ArrayList<MovieAward>();

  public MovieAwardPlugin() {
  }

  @Override
  public PluginInfo getInfo() {
    if (mPluginInfo == null) {
      String name = mLocalizer.msg("pluginName", "Movie Awards");
      String desc = mLocalizer.msg("description", "Shows movie awards");
      String author = "TV-Browser Team";

      mPluginInfo = new PluginInfo(MovieAwardPlugin.class, name, desc, author);
    }

    return mPluginInfo;
  }

  public static Version getVersion() {
    return mVersion;
  }

  private void initDatabase() {
    // might be called multiple times
    if (mMovieAwards == null) {
      mMovieAwards = new ArrayList<MovieAward>();
      mMovieAwards.add(new MovieAward(getClass().getResourceAsStream("./data/oscars.xml")));
    }
  }

  @Override
  public void onActivation() {
    initDatabase();
  }

}