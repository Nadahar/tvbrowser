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
 *
 * CVS information:
 *  $RCSfile$
 *   $Source$
 *     $Date$
 *   $Author$
 * $Revision$
 */

package searchplugin;

import java.util.ArrayList;
import java.util.Iterator;

import java.util.regex.*;

import devplugin.*;

/**
 *
 * @author  Til Schneider, www.murfman.de
 */
public class SearchPlugin extends Plugin {
  
  /**
   * Creates a new instance of SearchPlugin.
   */
  public SearchPlugin() {
  }

  
  
  /**
   * This method is invoked by the host-application if the user has choosen your
   * plugin from the menu.
   */
  public void execute() {
    SearchDialog dlg = new SearchDialog(super.parent);
    dlg.show();
  }



  /**
   * Returns the name of the file, containing your plugin icon (in the jar-File).
   */
  public String getMarkIcon() {
    return null;
  }

  
  
  public String getButtonText() {
    return "Sendungen suchen";
  }

  
  
  public PluginInfo getInfo() {
    return new PluginInfo("Sendungen suchen",
      "Ermöglicht das Suchen von Sendungen, die einem bestimmten Text enthalten.",
      "Til Schneider, www.murfman.de",
      new Version(1, 0));
  }
  
  
  
  public static Program[] search(String regex, boolean inTitle, boolean inText,
    boolean caseSensitive, Channel[] channels, devplugin.Date startDate,
    int nrDays)
    throws PatternSyntaxException
  {
    int flags = 0;
    if (! caseSensitive) {
      flags &= Pattern.CASE_INSENSITIVE;
    }

    Pattern pattern = Pattern.compile(regex, flags);
    
    if (nrDays < 0) {
      startDate.addDays(nrDays);
      nrDays = 0 - nrDays;
    }
    
    ArrayList hitList = new ArrayList();
    for (int day = 0; day <= nrDays; day++) {
      for (int channelIdx = 0; channelIdx < channels.length; channelIdx++) {
        Channel channel = channels[channelIdx];
        Iterator programIter = getPluginManager().getChannelDayProgram(startDate, channel);
        if (programIter == null) {
          // There is no more data -> stop
          day = nrDays;
        } else {
          while (programIter.hasNext()) {
            Program prog = (Program) programIter.next();
            boolean matches = false;
            
            if (inTitle) {
              Matcher matcher = pattern.matcher(prog.getTitle());
              matches = matcher.matches();
            }
            if ((! matches) && inText) {
              Matcher matcher = pattern.matcher(prog.getDescription());
              matches = matcher.matches();
            }
            
            if (matches) {
              hitList.add(prog);
            }
          }
        }
      }
      
      // The next day
      startDate.addDays(1);
    }
    
    Program[] hitArr = new Program[hitList.size()];
    hitList.toArray(hitArr);
    
    return hitArr;
  }

}