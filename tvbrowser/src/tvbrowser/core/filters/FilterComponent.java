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


package tvbrowser.core.filters;
import java.io.*;

import javax.swing.*;


public interface FilterComponent /*implements devplugin.ProgramFilter*/ {
  
  
  public int getVersion();
  public boolean accept(devplugin.Program program);  
  public void read(ObjectInputStream in, int version) throws IOException, ClassNotFoundException;
  public void write(ObjectOutputStream out) throws IOException;
  public JPanel getPanel();
  public void ok();
  public String getName();
  public String getDescription();
  public void setName(String name);
  public void setDescription(String desc);
  
  
 /*
    private int mType;
    protected String mName, mDescription;
    
    protected java.util.Properties mSettings;
    protected JPanel mPanel;
    
    public FilterComponent() {
    }
    
    public FilterComponent(String name, String description) {
        mName=name;
        mDescription=description;
        mSettings=new java.util.Properties();
    }
    
    public FilterComponent(java.io.ObjectInputStream in) {
        
    }
    
    public void setName(String name) {
        mName=name.trim().replace(' ','_');
    }
    
    public void setDescription(String desc) {
        mDescription=desc;
    }
    
    public void setType(int type) {
        mType=type;
    }
    
    public String getName() {
        return mName;
    }
    
    public String getDescription() {
        return mDescription;
    }
    
    public abstract String toString();
    
    public abstract void ok();
    
    public abstract void store(java.io.ObjectOutputStream out);
    
    //public abstract java.util.Properties getSettings();
    
    //public abstract void setSettings(java.util.Properties settings);
    
    public abstract JPanel getPanel();
    
    abstract public boolean accept(devplugin.Program program);  
    */  
}


