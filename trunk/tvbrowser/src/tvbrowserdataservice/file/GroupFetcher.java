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
package tvbrowserdataservice.file;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Properties;

import tvbrowserdataservice.ChannelGroup;
import tvbrowserdataservice.TvBrowserDataService;
import util.io.IOUtilities;


/**
 * Fetches the Default-Groups from the Server
 * 
 * @author bodum
 */
public class GroupFetcher {

    /** URL to load */
//    private static String PROVIDERURL="http://www.tvbrowser.org/listings/provider.txt";
    private static String PROVIDERURL="http://www.wannawork.de/provider.txt";
    
    /**
     * Loads the Default-Groups
     * @return ArrayList with Default-Groups
     */
    public ArrayList getChannelGroups(Properties settings) {
        
        ArrayList groups = new ArrayList();
        
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(IOUtilities.getStream(new URL(PROVIDERURL))));
            String str;
            while ((str = reader.readLine()) != null) {
                if (str.length() > 0) {
                    String[] ex = str.split(";");
                    System.out.println(str + "---" + ex.length);
                    
                    ChannelGroup group = new ChannelGroup(TvBrowserDataService.getInstance(), ex[0], new String[] {ex[1]}, settings);

                    if (ex.length > 2)
                        group.setProviderName(ex[2]);
                    if (ex.length > 3) 
                        group.setProviderId(ex[3]);
                    if (ex.length > 4) 
                        group.setProviderWebPage(ex[4]);

                    groups.add(group);
                }
            }
        } catch (Exception e) {
            // TODO: Show Error-Message
            e.printStackTrace();
            return null;
        }
        
        return groups;
    }
}