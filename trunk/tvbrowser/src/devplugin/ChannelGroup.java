/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (darras@users.sourcceforge.net)
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


package devplugin;

/**
 * Each channel should belong to exactly one channel group. The ChannelGroup interface
 * represents a channel group.
 */
public interface ChannelGroup {

  /**
   * @return the title of this group
   */
  public String getName();

  /**
   *
   * @return unique identifier of this group
   */
  public String getId();

  /**
   *
   * @return short description of this group
   */
  public String getDescription();

  /**
   * @since 1.1
   * @return the name of the provider
   */
  public String getProviderName();
}