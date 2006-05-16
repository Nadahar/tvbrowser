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
 *  $RCSfile$
 *   $Source$
 *     $Date: 2005-08-20 17:18:20 +0200 (Sa, 20 Aug 2005) $
 *   $Author: darras $
 * $Revision: 1443 $
 */
package tvbrowser.extras.favoritesplugin.core;

import tvbrowser.core.search.AbstractSearcher;

/**
 * This is the actual Searcher for the Actors
 * 
 * @author bodum
 */
public class ActorSearcher extends AbstractSearcher {
  /** Actor String Searcher */
  private ActorStringSearcher mActorStringSearcher;
  
  /**
   * Create Searcher
   * @param actor search for this Actor
   */
  public ActorSearcher(String actor) {
    mActorStringSearcher = new ActorStringSearcher(actor);
  }

  /**
   * @return true, if Actor is in Search-String
   */
  protected boolean matches(String value) {
    return mActorStringSearcher.actorInProgram(value);
  }

}
