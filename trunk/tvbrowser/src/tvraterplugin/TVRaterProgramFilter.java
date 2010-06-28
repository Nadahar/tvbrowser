/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (martin_oat@yahoo.de)
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
 */
package tvraterplugin;

import util.ui.Localizer;
import devplugin.Plugin;
import devplugin.PluginsProgramFilter;
import devplugin.Program;

/**
 * This Filter only accepts good shows/movies
 */
public class TVRaterProgramFilter extends PluginsProgramFilter {
    /** Good is defined here */
    private static final int GOOD = 3;
    /** Localizer */
    private static final Localizer mLocalizer = Localizer
            .getLocalizerFor(TVRaterProgramFilter.class);

    /**
     * Creates an instance of PluginsProgramFilter.
     *
     * @param plugin The plugin to create for.
     */
    public TVRaterProgramFilter(Plugin plugin) {
        super(plugin);
    }

    /**
     * @return Name of this Filter
     */
    public String getSubName() {
        return mLocalizer.msg("subname", "Very good shows/movies");
    }

    /**
     * Only accept good shows/movies
     * @param program Test this Program
     * @return true, if program is good
     */
    public boolean accept(Program program) {
        Rating rating = TVRaterPlugin.getInstance().getRating(program);
        return rating != null && rating.getOverallRating() >= GOOD;
    }
}
