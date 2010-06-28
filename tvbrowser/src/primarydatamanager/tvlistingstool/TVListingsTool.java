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
 *     $Date$
 *   $Author$
 * $Revision$
 */

package primarydatamanager.tvlistingstool;

import java.io.File;

import primarydatamanager.mirrorupdater.data.DataSource;
import primarydatamanager.mirrorupdater.data.DataTarget;
import primarydatamanager.mirrorupdater.data.FileDataTarget;
import primarydatamanager.mirrorupdater.data.HttpDataSource;


public class TVListingsTool {
    
  private static void usage() {
    System.out.println("usage: TVListingsViewer [-url mirrorUrl] [-groups groupName:groupName...]");
  }
 
  public static void main(String[] args) throws Exception {
    
    if (args.length==0) {
      usage();
      System.exit(0);
    }
    
    String groupNameList=null;
    String mirrorUrlName=null;
    
    for (int i = 0; i < args.length; i++) {
      if (args[i].equalsIgnoreCase("-url")) {
        if ((i + 1) >= args.length) {
          System.out.println("option '-url' without mirror url");
          System.exit(1);
        } else {
          mirrorUrlName = args[i+1];
        }
      }
      else if (args[i].equalsIgnoreCase("-groups")) {
        if ((i + 1) >= args.length) {
          System.out.println("option '-groups' without group name");
          System.exit(1);
        } else {
          groupNameList = args[i+1];
        }
      }
    }
    
    if (mirrorUrlName==null) {
      System.out.println("no mirror url given");
      System.exit(1);
    }
    if (groupNameList==null) {
      System.out.println("no group name given");
      System.exit(1);
    }
    
    String[] groupNames = groupNameList.split(":");
    
    DataSource source = new HttpDataSource("http://tvbrowser.dyndns.tv");
    DataTarget target = new FileDataTarget(new File("."));
    
    MirrorVisualizer visualizer = new HtmlMirrorVisualizer(source, target, groupNames);
    visualizer.visualize();
    
  }


  
}