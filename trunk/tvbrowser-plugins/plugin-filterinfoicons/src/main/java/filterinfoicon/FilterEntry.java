/*
 * Filter Info Icon plugin for TV-Browser
 * Copyright (C) 2014 René Mach (rene@tvbrowser.org)
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
 */
package filterinfoicon;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;

import compat.IOCompat;
import devplugin.Plugin;
import devplugin.PluginManager;
import devplugin.Program;
import devplugin.ProgramFilter;
import devplugin.ProgramReceiveTarget;
import devplugin.ProgramSearcher;
import tvdataservice.MutableChannelDayProgram;
import util.exc.TvBrowserException;
import util.ui.SearchFormSettings;
import util.ui.UiUtilities;

/**
 * A filter info icon.
 * 
 * @author René Mach
 */
public class FilterEntry implements Comparable<FilterEntry> {
  private String mFilterName;
  private String mIconFilePath;
  private ProgramReceiveTarget[] mReceiveTargets;
  
  private ProgramFilter mFilterInstance;
  private Icon mIcon;
  
  private boolean mIsValid;
  private boolean mFilterLoad = false;
  private boolean mUpdateDaily;
  
  private HashSet<Program> mUpdateSet;
  
  public FilterEntry() {
    mFilterName = "";
    mIsValid = false;
    mFilterLoad = true;
    mUpdateDaily = false;
  }
  
  public FilterEntry(ObjectInputStream in) throws IOException, ClassNotFoundException {
    int version = in.readInt(); // read version
    
    mFilterName = in.readUTF();
    
    if(in.readBoolean()) {
      setIconFilePath(IOCompat.translateRelativePath(in.readUTF()));
    }
    
    if(in.readBoolean()) {
      mReceiveTargets = new ProgramReceiveTarget[in.readInt()];
      
      for(int i = 0; i < mReceiveTargets.length; i++) {
        mReceiveTargets[i] = new ProgramReceiveTarget(in);
      }
    }
    
    mIsValid = true;
    mFilterLoad = false;
    
    if(version >= 2) {
      mUpdateDaily = in.readBoolean();
    }
    else {
      mUpdateDaily = false;
    }
  }
  
  public void setProgramReceiveTargets(ProgramReceiveTarget[] receiveTargets) {
    mReceiveTargets = receiveTargets;
    findForReceiveTargets();
  }
  
  public boolean getUpdateDaily() {
    return mUpdateDaily;
  }
  
  public void setUpdateDaily(boolean value) {
    mUpdateDaily = value;
  }
  
  public void handleTvDataUpdateStarted() {
    if(mReceiveTargets != null) {
      mUpdateSet = new HashSet<Program>();
    }
  }
  
  public void handleTvDataAdded(MutableChannelDayProgram touched) {
    if(mUpdateSet != null && isValidFilter()) {
      ProgramFilter test = getFilter();
      
      if(test != null) {
        for(Iterator<Program> it = touched.getPrograms(); it.hasNext();) {
          Program prog = it.next();
          
          if(prog != null && test.accept(prog) && !mUpdateSet.contains(prog)) {
            mUpdateSet.add(prog);
          }
        }
      }
    }
  }
  
  void findForReceiveTargets() {
    if(isValidFilter()) {
      ProgramFilter test = getFilter();
      
      if(test != null) {
        ArrayList<Program> matched = new ArrayList<Program>();
        
        SearchFormSettings formSettings = new SearchFormSettings(".*"); // We match all programs to check them later
        formSettings.setSearchIn(SearchFormSettings.SEARCH_IN_TITLE);
        formSettings.setSearcherType(PluginManager.SEARCHER_TYPE_REGULAR_EXPRESSION);
        
        try {
          ProgramSearcher searcher = formSettings.createSearcher();
          Program[] all = searcher.search(formSettings.getFieldTypes(),new devplugin.Date().addDays(-1),1000,Plugin.getPluginManager().getSubscribedChannels(),false);
          
          for(Program prog : all) {
            if(test.accept(prog)) {
              matched.add(prog);
              prog.validateMarking();
            }
          }
        } catch (TvBrowserException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
        
        if(!matched.isEmpty()) {
          sendReceiveTargets(matched.toArray(new Program[matched.size()]));
        }
        
        matched.clear();
        matched = null;
      }
    }
  }
  
  private void sendReceiveTargets(Program[] toSend) {
    if(mReceiveTargets != null) {
      for(ProgramReceiveTarget target : mReceiveTargets) {
        target.getReceifeIfForIdOfTarget().receivePrograms(toSend, target);
      }
    }
  }
  
  public void handleTvDataUpdateFinished() {
    if(mUpdateSet != null) {
      if(!mUpdateSet.isEmpty() && mReceiveTargets != null) {
        sendReceiveTargets(mUpdateSet.toArray(new Program[mUpdateSet.size()]));
      }
      
      mUpdateSet.clear();
      mUpdateSet = null;
    }
  }
 
  @Override
  public String toString() {
    getFilter();
    
    return mFilterName;
  }
  
  public synchronized ProgramFilter getFilter() {
    if(!mFilterLoad && mFilterInstance == null && mFilterName != null) {
      ProgramFilter[] filters = Plugin.getPluginManager().getFilterManager().getAvailableFilters();
      boolean found = false;
      
      for(ProgramFilter filter : filters) {
        if(filter.getName().equals(mFilterName)) {
          mFilterInstance = filter;
          found = true;
          break;
        }
      }
      
      mIsValid = found;
    }
    
    mFilterLoad = true;
    
    return mFilterInstance;
  }
  
  public boolean isValidFilter() {
    return mIsValid;
  }
  
  public void updateFilter(ProgramFilter filter) {
    if(filter != null) {
      mFilterName = filter.getName();
      mFilterInstance = filter;
      mIsValid = true;
      mFilterLoad = true;
      
      findForReceiveTargets();
    }
  }
  
  public void setIconFilePath(String filePath) {
    mIconFilePath = filePath;
    mIcon = null;
    
    if(mIconFilePath != null) {
      File test = new File(mIconFilePath);
      
      if(test.isFile()) {
        try {
          mIcon = new ImageIcon(ImageIO.read(test));
          mIcon = UiUtilities.scaleIcon(mIcon, 13);
        } catch (IOException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }
    }
  }
  
  public Icon getIcon() {
    return mIcon;
  }
  
  public boolean accepts(Program p) {
    boolean returnValue = false;
    
    if(isValidFilter()) {
      ProgramFilter test = getFilter();
      
      if(test != null) {
        returnValue = test.accept(p);
      }
    }
    
    return returnValue;
  }
  
  public void writeData(ObjectOutputStream out) throws IOException {
    out.writeInt(2); // version
    
    if(mFilterName == null) {
      mFilterName = "";
    }
    
    out.writeUTF(mFilterName);
    
    out.writeBoolean(mIconFilePath != null);
    
    if(mIconFilePath != null) {
      out.writeUTF(IOCompat.checkForRelativePath(mIconFilePath));
    }
    
    out.writeBoolean(mReceiveTargets != null);
    
    if(mReceiveTargets != null) {
      out.writeInt(mReceiveTargets.length);
      
      for(ProgramReceiveTarget target : mReceiveTargets) {
        target.writeData(out);
      }
    }
    
    out.writeBoolean(mUpdateDaily);
  }
  
  public void checkFilter(ProgramFilter filter) {
    ProgramFilter test = getFilter();
    
    if(test != null && test.equals(filter)) {
      mFilterName = filter.getName();
      findForReceiveTargets();
    }
  }
  
  public void deleteFilter(ProgramFilter filter) {
    ProgramFilter test = getFilter();
    
    if((test != null && test.equals(filter)) || mFilterName.equals(filter.getName())) {
      mIsValid = false;
      mFilterLoad = true;
      mFilterInstance = null;
    }
  }
  
  public void checkFilterAdded(ProgramFilter filter) {
    if(mFilterLoad && mFilterInstance == null && mFilterName.equals(filter.getName())) {
      mFilterLoad = false;
      mIsValid = true;
      
      findForReceiveTargets();
    }
  }

  @Override
  public int compareTo(FilterEntry o) {
    return mFilterName.compareToIgnoreCase(o.mFilterName);
  }
  
  public ProgramReceiveTarget[] getReceiveTargets() {
    return mReceiveTargets;
  }
  
  public File getIconFilePath() {
    if(mIconFilePath != null) {
      File test = new File(mIconFilePath);
      
      if(test.isFile()) {
        return test;
      }
    }
    
    return null;
  }
}
