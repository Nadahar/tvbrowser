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
 
package tvbrowser.ui.filter;

import java.util.*;
import java.io.*;
import tvbrowser.core.Settings;

public class FilterList {
    
    public static final String FILTER_DIRECTORY=Settings.getUserDirectoryName()+"/filters";
    private static HashSet mFilterList;
    
    public static void load() {
        
        mFilterList=new HashSet();
        File[] list=new File(FILTER_DIRECTORY).listFiles(new FileFilter() {
            public boolean accept(File f) {
                return f.getAbsolutePath().endsWith(".filter");
            }            
        });
        if (list!=null) {
            for (int i=0;i<list.length;i++) {
                Filter f=new Filter(list[i]);
                mFilterList.add(f);
            }
        }        
    }
    
    public static void store() {
        Iterator it=mFilterList.iterator();
        File directory=new File(FILTER_DIRECTORY);
        if (!directory.exists()) {
            directory.mkdir();
        }
        while (it.hasNext()) {
            ((Filter)it.next()).store(directory);
        }
    }
    
    public static Filter[] getFilterList() {
        if (mFilterList==null) {
            load();
        }
        Iterator it=mFilterList.iterator();
        int size=mFilterList.size();
        Filter[] result=new Filter[size];
        int i=0;
        while (it.hasNext()) {
            result[i++]=(Filter)it.next();
        }
        return result;
    }
    
    public static void add(Filter filter) {
        if (mFilterList.contains(filter)) {
            
        }
        mFilterList.add(filter);
    }
    
    public static void remove(Filter filter) {
        filter.delete();
        mFilterList.remove(filter);
    }
    
}