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
 * SVN information:
 *     $Date$
 *   $Author$
 * $Revision$
 */
package util.ui;

import tvbrowser.core.filters.UserFilter;
import devplugin.ProgramFilter;

/**
 * Wrapper for program filter that shows if filter
 * contains broken channel filter components.
 * 
 * @author René Mach
 * @since 3.4.3
 */
public final class WrapperFilter {
  private ProgramFilter mFilter;
  
  public WrapperFilter(ProgramFilter filter) {
    mFilter = filter;
  }
  
  @Override
  public String toString() {
    String result = mFilter.toString();
    
    if(mFilter instanceof UserFilter && ((UserFilter)mFilter).containsBrokenChannelFilterComponent()) {
      result = "<html><span style=\"color:orange;\"><u>"+result+"</u></span></html>";
    }
    
    return result;
  }
  
  public String getName() {
    return mFilter.getName();
  }
  
  public ProgramFilter getFilter() {
    return mFilter;
  }
  
  @Override
  public boolean equals(Object obj) {
    if(obj instanceof WrapperFilter) {
      return mFilter.equals(((WrapperFilter)obj).mFilter);
    }
    else if(obj instanceof ProgramFilter) {
      return mFilter.equals(obj);
    }
    
    return super.equals(obj);
  }
}