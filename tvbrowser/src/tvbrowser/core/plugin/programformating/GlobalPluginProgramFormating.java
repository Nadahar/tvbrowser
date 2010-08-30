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
 *     $Date: 2007-01-09 18:37:06 +0100 (Di, 09 Jan 2007) $
 *   $Author: bananeweizen $
 * $Revision: 2997 $
 */
package tvbrowser.core.plugin.programformating;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import util.misc.HashCodeUtilities;
import util.program.AbstractPluginProgramFormating;

/**
 * A class for global program configuration settings.
 *
 * @author René Mach
 * @since 2.5.1
 */
public class GlobalPluginProgramFormating extends AbstractPluginProgramFormating {
  /**
   * Creates an empty instance of this class.
   * Don't remove!
   * This is really needed for loading GlobalPluginProgramFormating
   * from ObjectInputStream!
   */
  public GlobalPluginProgramFormating() {
    super(null,null,null,null,null);
  }
  
  protected GlobalPluginProgramFormating(String id, String name, String titleValue, String contentValue, String encodingValue) {
    super(id, name, titleValue, contentValue, encodingValue);
  }

  protected GlobalPluginProgramFormating(String name, String titleValue, String contentValue, String encodingValue) {
    this("#id_" + System.currentTimeMillis(), name, titleValue, contentValue, encodingValue);
  }

  public String getContentValue() {
    GlobalPluginProgramFormating config = GlobalPluginProgramFormatingManager.getInstance().getConfigurationForId(getId());

    if(config != null) {
      return config.mContentValue;
    } else {
      return mContentValue;
    }
  }

  public String getEncodingValue() {
    GlobalPluginProgramFormating config = GlobalPluginProgramFormatingManager.getInstance().getConfigurationForId(getId());

    if(config != null) {
      return config.mEncodingValue;
    } else {
      return mEncodingValue;
    }
  }

  public String getName() {
    GlobalPluginProgramFormating config = GlobalPluginProgramFormatingManager.getInstance().getConfigurationForId(getId());

    if(config != null) {
      return config.mName;
    } else {
      return mName;
    }
  }

  public String getTitleValue() {
    GlobalPluginProgramFormating config = GlobalPluginProgramFormatingManager.getInstance().getConfigurationForId(getId());

    if(config != null) {
      return config.mTitleValue;
    } else {
      return mTitleValue;
    }
  }

  protected static GlobalPluginProgramFormating load(ObjectInputStream in) throws IOException, ClassNotFoundException {
    in.readInt(); //read version

    return new GlobalPluginProgramFormating((String)in.readObject(), (String)in.readObject(), (String)in.readObject(), (String)in.readObject(), (String)in.readObject());
  }

  protected void store(ObjectOutputStream out) throws IOException {
    out.writeInt(1); //write version
    out.writeObject(mId);
    out.writeObject(mName);
    out.writeObject(mTitleValue);
    out.writeObject(mContentValue);
    out.writeObject(mEncodingValue);
  }

  protected void loadData(ObjectInputStream in) throws IOException, ClassNotFoundException {
    in.readInt(); //read version
    setId((String)in.readObject());
  }

  protected void storeData(ObjectOutputStream out) throws IOException {
    out.writeInt(1); //write version
    out.writeObject(getId());
  }

  /**
   * Checks if this global program configuration has the given id.
   *
   * @param id The id to check.
   * @return <code>true</code> if the ids are the same, <code>false</code> otherwise;
   */
  public boolean hasId(String id) {
    if(id != null) {
      return id.equals(getId());
    }

    return false;
  }

  public boolean equals(Object o) {
    if(o != null && o instanceof GlobalPluginProgramFormating) {
      return ((GlobalPluginProgramFormating)o).getId().equals(getId());
    }

    return false;
  }

  @Override
  public int hashCode() {
    return HashCodeUtilities.hash(getId());
  }

  @Override
  public boolean isValid() {
    return GlobalPluginProgramFormatingManager.getInstance().getConfigurationForId(getId()) != null;
  }
}
