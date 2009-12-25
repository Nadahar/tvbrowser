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


 /**
  * TV-Browser
  * @author Martin Oberhauser
  */

package devplugin;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * A class for the Version value off
 * TV-Browser and it's plugins.
 */
public final class Version implements Comparable<Version> {
  
  private int mMajor, mMinor, mSubMinor;
  private boolean mIsStable;
  private String mName;

  /**
   * Creates an instance of this class.
   * 
   * @param major The major version (the first number).
   * @param minor The minor version (used for the second and third number).
   */
  public Version(int major, int minor) {
    this(major, minor, 0, true, null);
  }
  
  /**
   * Creates an instance of this class.
   * 
   * @param major The major version (the first number).
   * @param minor The minor version (used for the second and third number).
   * @param isStable If the version is stable.
   */
  public Version(int major, int minor, boolean isStable) {
    this(major, minor, 0, isStable, null);
  }
  
  /**
   * Creates an instance of this class.
   * 
   * @param major The major version (the first number).
   * @param minor The minor version (used for the second and third number).
   * @param isStable If the version is stable.
   * @param name The name String of this verison, use <code>null</code>
   *             to let the name be created from the version numbers.
   */
  public Version(int major, int minor, boolean isStable, String name) {
    this(major, minor, 0, isStable, name);
  }

  /**
   * Creates an instance of this class.
   * 
   * @param major The major version (the first number).
   * @param minor The minor version (used for the second and third number).
   * @param subMinor The sub minor version (the 4th number).
   * 
   * @since 2.2.4/2.6
   */
  public Version(int major, int minor, int subMinor) {
    this(major, minor, subMinor, true, null);
  }

  /**
   * Creates an instance of this class.
   * 
   * @param major The major version (the first number).
   * @param minor The minor version (used for the second and third number).
   * @param subMinor The sub minor version (the 4th number).
   * @param isStable If the version is stable.
   * 
   * @since 2.2.4/2.6
   */
  public Version(int major, int minor, int subMinor, boolean isStable) {
    this(major, minor, subMinor, isStable, null);
  }
  
  /**
   * Creates an instance of this class.
   * 
   * @param major The major version (the first number).
   * @param minor The minor version (used for the second and third number).
   * @param subMinor The sub minor version (the 4th number).
   * @param isStable If the version is stable.
   * @param name The name String of this verison, use <code>null</code>
   *             to let the name be created from the version numbers.
   *             
   * @since 2.2.4/2.6
   */
  public Version(int major, int minor, int subMinor, boolean isStable, String name) {
    mMajor = major;
    mMinor = minor;
    mSubMinor = subMinor;
    mIsStable = isStable;
    mName = name;
  }

  public String toString() {
  	if (mName==null) {
      return mMajor + "." + mMinor/10 + "." + mMinor%10 + "." + mSubMinor + (mIsStable?"":" beta");
  	}
  	return mName;
  }

  /**
   * Gets if this version is stable.
   * 
   * @return <code>True</code> if the version is stable,
   * <code>false</code> otherwise.
   */
  public boolean isStable() {
  	return mIsStable;
  }
  
  /**
   * Gets the major version.
   * 
   * @return The major version (first number).
   */
  public int getMajor() {
    return mMajor;
  }
  
  /**
   * Gets the minor version.
   * 
   * @return The minor version (seconds and third number).
   */
  public int getMinor() {
    return mMinor;
  }
  
  /**
   * Gets the sub minor version
   * 
   * @return The sub minor version (4th number).
   */
  public int getSubMinor() {
    return mSubMinor;
  }
  
  public int compareTo(Version v) throws ClassCastException {
  	if (mMajor>v.mMajor) {
  		return 1;
  	}else if (mMajor<v.mMajor) {
  		return -1;  		
  	}else {  // major is equals
  		if (mMinor>v.mMinor) {
  			return 1;
  		} else if (mMinor<v.mMinor) {
  			return -1;
  		}else {
  		  if (mSubMinor>v.mSubMinor) {
  		    return 1;
  		  }
  		  else if(mSubMinor<v.mSubMinor) {
  		    return -1;
  		  }
  		  else {
  		    // sub minor is equals
    			if (mIsStable && !v.mIsStable) {
    				return 1;
    			}
    			else if (!mIsStable && v.mIsStable){
    				return -1;				
    			}
    			else {
    				return 0;
    			}
  		  }
  		}   		
  	} 	
  }
  
  public boolean equals(Object obj) {
    if (obj instanceof Version) {
      Version ver = (Version) obj;
      return (mMajor == ver.mMajor) && (mMinor == ver.mMinor) && (mSubMinor == ver.mSubMinor) && (mIsStable == ver.mIsStable);
    } else {
      return false;
    }
  }
  
  /**
   * Writes tis object to a stream.
   * <p>
   * @param out The stream to write to.
   * @throws IOException Thrown if something went wrong.
   */
  public void writeData(final DataOutputStream out) throws IOException {
    out.writeByte(1); //version
    out.writeInt(mMajor);
    out.writeInt(mMinor);
    out.writeInt(mSubMinor);
    out.writeBoolean(mIsStable);
    
    out.writeBoolean(mName != null);
    
    if(mName != null) {
      out.writeUTF(mName);
    }
  }

  /**
   * Creates an instance of this class from the given stream
   * <p>
   * @param in The stream to read the version from.
   * @throws IOException Thrown if something went wrong.
   * @throws ClassNotFoundException Thrown if something went wrong.
   */
  public Version(final DataInputStream in) throws IOException, ClassNotFoundException {
    in.readByte(); //version
    mMajor = in.readInt();
    mMinor = in.readInt();
    mSubMinor = in.readInt();
    mIsStable = in.readBoolean();
    
    if(in.readBoolean()) {
      mName = in.readUTF();
    }
  }
}