/*
 * TV-Browser
 * Copyright (C) 2012 TV-Browser team (dev@tvbrowser.org)
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
 *     $Date$
 *   $Author$
 * $Revision$
 */
package devplugin;

import tvbrowser.core.filters.FilterComponentList;
import tvbrowser.core.filters.filtercomponents.ChannelFilterComponent;
import util.exc.TvBrowserException;

/**
 * A filter that channels for filters using a ChannelFilterComponent.
 * NOTE: The ChannelFilterComponent cannot be added itself, only the name of
 * the of the ChannelFilterComponent can be provided to load it internal.
 * <p>
 * @author René Mach
 * @since 3.2.1
 */
public final class ChannelFilter implements ProgramFilter {
  private ChannelFilterComponent mChannelFilterComponent;
  
  /**
   * Creates a channel filter with the name of the
   * component to create the filter from.
   * <p>
   * @param name The name of the filter component to create
   * the channel filter from.
   * @throws ClassCastException Thrown if the found component is not a ChannelFilterComponent.
   * @throws TvBrowserException Thrown if the ChannelFilterComponent with the given name was not found.
   */
  private ChannelFilter(String name) throws ClassCastException, TvBrowserException {
    mChannelFilterComponent = (ChannelFilterComponent)FilterComponentList.getInstance().getFilterComponentByName(name);
    
    if(mChannelFilterComponent == null) {
      throw new TvBrowserException(ChannelFilter.class, "filterComponentNotFound", "Filter component not found");
    }
  }
  
  /**
   * Creates a channel filter with the name of the
   * component to create the filter from.
   * <p>
   * @param name The name of the filter component to create
   * the channel filter from.
   * @return The created channel filter.
   * @throws ClassCastException Thrown if the found component is not a ChannelFilterComponent.
   * @throws TvBrowserException Thrown if the ChannelFilterComponent with the given name was not found.
   */
  public static ChannelFilter createChannelFilterForName(String name) throws ClassCastException, TvBrowserException {
    return new ChannelFilter(name);
  }

  @Override
  public boolean accept(Program program) {
    return mChannelFilterComponent.accept(program);
  }

  @Override
  public String getName() {
    return mChannelFilterComponent.getName();
  }
  
  @Override
  public String toString() {
    if(mChannelFilterComponent.isEmpty()) {
      return "<html><span style=\"color:orange;\"><s>"+getName()+"</s></span></html>";
    }
    else if(mChannelFilterComponent.isBroken()) {
      return "<html><span style=\"color:orange;\"><u>"+getName()+"</u></span></html>";
    }

    return getName();
  }
  
  /**
   * Gets all the channels of this channel filter.
   * <p>
   * @return All channels of this channel filter.
   */
  public Channel[] getChannels() {
    return mChannelFilterComponent.getChannels();
  }
}
