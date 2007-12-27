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

import tvbrowser.core.search.regexsearch.RegexSearcher;
import util.exc.TvBrowserException;

/**
 * This is the actual Searcher for the Actors
 * 
 * @author bananeweizen
 */
public class ActorSearcher extends RegexSearcher {

  public ActorSearcher(String actor)
      throws TvBrowserException {
    super(getSearchTerm(actor), false);
  }
  
  private static String getSearchTerm(String actor) {
    if (actor == null) {
      return null;
    }
    String[] actorStr = actor.split("\\s");
    
    // first pattern is actor name without changes
    String regEx = ".*\\b((" + actor+")";
    
    // use additional variants, if the name consists of multiple parts
    int actorMax = actorStr.length-1;
    if (actorStr.length > 1) {
      regEx = regEx 
        // _Doe,_Jon_ 
        + "|(" + actorStr[actorMax] + "\\s*,\\s*"+ actorStr[0]+")"
        // _J._Doe_
        // _Jon_M._Doe_
        + "|(" + actorStr[0].substring(0,1) + "(" + ".|" + actorStr[0].substring(1) + ")\\s*\\w*.?\\s*"+ actorStr[actorMax]+")";
    }
    regEx = regEx + ")\\b.*";
    return regEx;
  }

}
