/*
 * ColorMe plugin for TV-Browser
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
package colorme;

import java.awt.Cursor;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashSet;
import java.util.Iterator;

import util.ui.Localizer;
import util.ui.TVBrowserIcons;

import devplugin.Plugin;
import devplugin.PluginInfo;
import devplugin.PluginsProgramFilter;
import devplugin.Program;
import devplugin.ProgramReceiveTarget;
import devplugin.ThemeIcon;
import devplugin.Version;

/**
 * A plugin that receives programs to color them with the received priority.
 * 
 * @author René Mach
 */
public class ColorMe extends Plugin {
  private static final Version VERSION = new Version(0,11,3,false);
  private static final Localizer LOCALIZER = Localizer.getLocalizerFor(ColorMe.class);
  
  private HashSet<Program> mLowestPrograms;
  private HashSet<Program> mLowerMediumPrograms;
  private HashSet<Program> mMediumPrograms;
  private HashSet<Program> mHigherMediumPrograms;
  private HashSet<Program> mHighestPrograms;
  
  private ProgramReceiveTarget[] mReceiveTargets;
  private PluginsProgramFilter[] mAvailableFilter;
  
  private ThemeIcon mIcon;
  
  public ColorMe() {
    mLowestPrograms = new HashSet<Program>();
    mLowerMediumPrograms = new HashSet<Program>();
    mMediumPrograms = new HashSet<Program>();
    mHigherMediumPrograms = new HashSet<Program>();
    mHighestPrograms = new HashSet<Program>();
    
    mReceiveTargets = new ProgramReceiveTarget[10];
    mAvailableFilter = new PluginsProgramFilter[5];
    
    mIcon = new ThemeIcon("apps", "colorme", TVBrowserIcons.SIZE_SMALL);
    
    for(int i = 0; i < 5; i++) {
      String priority = getValueForPriority(i);
      
      mReceiveTargets[i] = new ProgramReceiveTarget(this, LOCALIZER.msg("add", "Add: Priority") + " - " + priority, String.valueOf(i+1));
      mReceiveTargets[i+5] = new ProgramReceiveTarget(this, LOCALIZER.msg("remove", "Remove: Priority:") + " - " + priority, String.valueOf(-(i+1)));
      
      final HashSet<Program> toFilter = getSetForPriority(i);
      final String subName = LOCALIZER.msg("filter", "Priority") + " - " + priority;
      
      mAvailableFilter[i] = new PluginsProgramFilter(this) {
        @Override
        public boolean accept(Program program) {
          return toFilter.contains(program);
        }
        
        @Override
        public String getSubName() {
          return subName;
        }
      };
    }
  }
  
  public static Version getVersion() {
    return VERSION;
  }
  
  @Override
  public PluginInfo getInfo() {
    return new PluginInfo(ColorMe.class, LOCALIZER.msg("name", "ColorMe"), LOCALIZER.msg("description", "Colors programs with priorities"), "René Mach", "GPL");
  }
  
  @Override
  public ThemeIcon getMarkIconFromTheme() {
    return mIcon;
  }
    
  private HashSet<Program> getSetForPriority(int priority) {
    switch (priority) {
      case Program.MIN_MARK_PRIORITY: return mLowestPrograms;
      case Program.LOWER_MEDIUM_MARK_PRIORITY: return mLowerMediumPrograms;
      case Program.MEDIUM_MARK_PRIORITY: return mMediumPrograms;
      case Program.HIGHER_MEDIUM_MARK_PRIORITY: return mHigherMediumPrograms;
      case Program.MAX_MARK_PRIORITY: return mHighestPrograms;
    }
    
    return null;
  }
  
  private String getValueForPriority(int priority) {
    switch(priority) {
      case Program.MIN_MARK_PRIORITY: return LOCALIZER.msg("priorities.minimum","Minimum");
      case Program.LOWER_MEDIUM_MARK_PRIORITY: return LOCALIZER.msg("priorities.lowerMedium","Lower medium");
      case Program.MEDIUM_MARK_PRIORITY: return LOCALIZER.msg("priorities.medium","Medium");
      case Program.HIGHER_MEDIUM_MARK_PRIORITY: return LOCALIZER.msg("priorities.higherMedium","Hiher medium");
      case Program.MAX_MARK_PRIORITY: return LOCALIZER.msg("priorities.maximum","Maximum");
    }
    
    return LOCALIZER.msg("priorities.unknown","Unknown");
  }
    
  @Override
  public boolean canReceiveProgramsWithTarget() {
    return true;
  }
    
  @Override
  public ProgramReceiveTarget[] getProgramReceiveTargets() {
    return mReceiveTargets;
  }
  
  @Override
  public int getMarkPriorityForProgram(Program p) {
    if(mHighestPrograms.contains(p)) {
      return Program.MAX_MARK_PRIORITY;
    }
    else if(mHigherMediumPrograms.contains(p)) {
      return Program.HIGHER_MEDIUM_MARK_PRIORITY;
    }
    else if(mMediumPrograms.contains(p)) {
      return Program.MEDIUM_MARK_PRIORITY;
    }
    else if(mLowerMediumPrograms.contains(p)) {
      return Program.LOWER_MEDIUM_MARK_PRIORITY;
    }
    else if(mLowestPrograms.contains(p)) {
      return Program.MIN_MARK_PRIORITY;
    }
    
    return Program.NO_MARK_PRIORITY;
  }
  
  private boolean programIsDoubleMarked(HashSet<Program> remove, Program p) {
    return (!remove.equals(mLowestPrograms) && mLowestPrograms.contains(p)) || (!remove.equals(mLowerMediumPrograms) && mLowerMediumPrograms.contains(p)) ||
        (!remove.equals(mMediumPrograms) && mMediumPrograms.contains(p)) || (!remove.equals(mHigherMediumPrograms) && mHigherMediumPrograms.contains(p)) ||
        (!remove.equals(mHighestPrograms) && mHighestPrograms.contains(p));
  }
  
  @Override
  public boolean receivePrograms(final Program[] programArr, ProgramReceiveTarget receiveTarget) {
    boolean returnValue = false;
    
    HashSet<Program> toUse = null;
    boolean add = false;
    
    if(receiveTarget.getReceiveIfId().equals(getId())) {
      int id = Integer.parseInt(receiveTarget.getTargetId());
      
      add = id>=0;
      toUse = getSetForPriority(Math.abs(id)-1);
    }
    
    if(toUse != null) {
      Cursor old = getParentFrame().getCursor();
      
      getParentFrame().setCursor(new Cursor(Cursor.WAIT_CURSOR));
      
      for(Program p : programArr) {
        if(p != null) {
          if(add) {
            if(toUse.add(p)) {
              if(!programIsDoubleMarked(toUse,p)) {
                p.mark(ColorMe.this);
              }
              else {
                p.validateMarking();
              }
            }
          }
          else {
            if(toUse.remove(p)) {
              if(!programIsDoubleMarked(toUse,p)) {
                p.unmark(ColorMe.this);
              }
              else {
                p.validateMarking();
              }
            }
          }
        }
      }
      
      getParentFrame().setCursor(old);
      
      returnValue = true;
      
      saveMe();
    }
    
    return returnValue;
  }
  
  @Override
  public void readData(ObjectInputStream in) throws IOException, ClassNotFoundException {
    in.readInt(); // read version
    readSet(in, mLowestPrograms);
    readSet(in, mLowerMediumPrograms);
    readSet(in, mMediumPrograms);
    readSet(in, mHigherMediumPrograms);
    readSet(in, mHighestPrograms);
  }
  
  private void readSet(ObjectInputStream in, HashSet<Program> set) throws IOException {
    int size = in.readInt();
    //TODO defer loading of programs to after
    for(int i = 0; i < size; i++) {
      String progID = in.readUTF();
      
      Program[] progs = getPluginManager().getPrograms(progID);
      
      if(progs != null) {
        for(Program p : progs) {
          set.add(p);
          p.mark(ColorMe.this);
        }
      }
    }
  }
  
  @Override
  public void writeData(ObjectOutputStream out) throws IOException {
    out.writeInt(1); // version;
    writeSet(out, mLowestPrograms);
    writeSet(out, mLowerMediumPrograms);
    writeSet(out, mMediumPrograms);
    writeSet(out, mHigherMediumPrograms);
    writeSet(out, mHighestPrograms);
  }
  
  private void writeSet(ObjectOutputStream out, HashSet<Program> set) throws IOException {
    out.writeInt(set.size());
    
    for(Iterator<Program> it = set.iterator(); it.hasNext();) {
      out.writeUTF(it.next().getUniqueID());
    }
  }
    
  @Override
  public PluginsProgramFilter[] getAvailableFilter() {
    return mAvailableFilter;
  }
    
  public boolean isAllowingArtificialPluginTree() {
    return false;
  }
}
