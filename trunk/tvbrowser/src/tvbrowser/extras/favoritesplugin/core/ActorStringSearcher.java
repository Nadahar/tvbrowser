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
 *     $Date: 2006-05-01 22:44:23 +0200 (Mo, 01 Mai 2006) $
 *   $Author: darras $
 * $Revision: 2318 $
 */
package tvbrowser.extras.favoritesplugin.core;

import java.util.regex.Pattern;

import devplugin.Program;
import devplugin.ProgramFieldType;

/**
 * This Class Searches for Actors in Strings
 * 
 * The following combinations are the same for this searcher:
 * 
 * Max Maria Mustermann
 * Mustermann, Max
 * Mustermann, Max Maria
 * Max Mustermann
 * Max M. Mustermann
 * M. M. Mustermann
 * M. Mustermann 
 *  
 * @author bodum
 */
public class ActorStringSearcher {
  /** LowerCase Actor-String */
  private String mActor;
  /** Compiled Patterns */
  private Pattern[] mPattern;
  
  /**
   * Actor to seach for
   * @param actor Actor
   */
  public ActorStringSearcher(String actor) {
    mPattern = new Pattern[4];
    mActor = actor;
    String[] actorStr = actor.split("\\s");
    
    mPattern[0] = Pattern.compile(actor);
    
    int actorMax = actorStr.length-1;
    
    if (actorStr.length > 1) {
      mPattern[1] = Pattern.compile("\\b" + actorStr[0] + "\\s*\\w*.?\\s*"+ actorStr[actorMax]+"\\b");
      mPattern[2] = Pattern.compile("\\b" + actorStr[actorMax] + "\\s*,\\s*"+ actorStr[0]+"\\b");
      mPattern[3] = Pattern.compile("\\b" + actorStr[0].substring(0,1) + ".\\s*\\w*.\\s*?"+ actorStr[actorMax]+"\\b");
    }
    
  }
  
  /**
   * Find a Actor in a Program. It searches in the short description, long description
   * and in the actor-list.
   * 
   * @param program Program
   * @return True, if Program contains Actor
   */
  public boolean actorInProgram(Program program) {
    if (actorInProgram(program.getShortInfo())) {
      return true;
    }
    if (actorInProgram(program.getDescription())) {
      return true;
    }
    return actorInProgram(program.getTextField(ProgramFieldType.ACTOR_LIST_TYPE));
  }

  /**
   * Find a Actor in a String.
   * 
   * @param textField Text that is checked
   * @return True, if textField contains Actor
   */
  public boolean actorInProgram(String textField) {
    // False if TextField is empty
    if ((textField == null) || (textField.length() == 0)) {
      return false;
    }
    
    // Replace Newline
    textField = textField.replace('\n', ' ');
    
    // Check if Text fits
    if (textField.toLowerCase().indexOf(mActor) >= 0) {
      return true;
    }
    
    // Check all Patterns, if one is true, return true
    for (int i = mPattern.length-1;i>=0;i--) {
      if ((mPattern[i] != null) && (mPattern[i].matcher(textField).find())) {
        return true;
      }
    }
      
    return false;
  }

}