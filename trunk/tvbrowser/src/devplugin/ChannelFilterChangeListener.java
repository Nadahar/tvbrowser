/*
 * TV-Browser
 * Copyright (C) 2014 TV-Browser team (dev@tvbrowser.org)
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
package devplugin;

/**
 * A listener that tracks changes of the channel filters.
 * <p>
 * @author René Mach
 * @since 3.4.1
 */
public interface ChannelFilterChangeListener {
  
  /**
   * Called when a ChannelFilter is added.
   * <p>
   * @param filter The filter that was added.
   */
  public void channelFilterAdded(ChannelFilter filter);
  
  /**
   * Called when a ChannelFilter was removed.
   * <p>
   * @param filter The filter that was removed.
   */
  public void channelFilterRemoved(ChannelFilter filter);
}
