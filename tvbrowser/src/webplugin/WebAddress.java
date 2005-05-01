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
package webplugin;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import util.ui.ImageUtilities;

/**
 * A Web-Address
 */
public class WebAddress implements Cloneable {
  /** Default-Icon */
  private final static ImageIcon DEFAULTICON = ImageUtilities.createImageIconFromJar("webplugin/Search16.gif", WebAddress.class);

  /** URL */
  private String mUrl;

  /** Icon */
  private String mIconFile;

  /** Name */
  private String mName;

  /** Entered by User ? */
  private boolean mUserEntry = false;

  /** Active ? */
  private boolean mActive = true;

  /**
   * Create the Address
   *
   * @param name Name
   * @param url Url ( {0} as placeholder )
   * @param iconFile Icon-File
   * @param encoding Which encoding (UTF-8, ...)
   * @param userEntry Is this Entry editable?
   * @param active Is this Entry active?
   */
  public WebAddress(String name, String url, String iconFile, boolean userEntry, boolean active) {
    mName = name;
    mIconFile = iconFile;
    mUrl = url;
    mUserEntry = userEntry;
    mActive = active;
  }

  /**
   * Create a Copy of a WebAddress
   *
   * @param address Copy Settings from this WebAddress
   */
  public WebAddress(WebAddress address) {
    mName = address.getName();
    mIconFile = address.getIconFile();
    mUrl = address.getUrl();
    mUserEntry = address.isUserEntry();
    mActive = address.isActive();
  }

  /**
   * Create a WebAddress with a Stream
   * @param in Input-Stream
   */
  public WebAddress(ObjectInputStream in) throws IOException, ClassNotFoundException {
    readData(in);
  }

  public void setName(String name) {
    mName = name;
  }

  public String getName() {
    return mName;
  }

  public void setUrl(String url) {
    mUrl = url;
  }

  public String getUrl() {
    return mUrl;
  }

  public void setIconFile(String iconFile) {
    mIconFile = iconFile;
  }

  public String getIconFile() {
    return mIconFile;
  }

  public Icon getIcon() {

    if (mIconFile == null || mIconFile.length() == 0) { return DEFAULTICON; }

    try {
      Icon icon = ImageUtilities.createImageIconFromJar(mIconFile, this.getClass());
      if ((icon != null) && (icon.getIconWidth() > 0)){
        return icon;
      }
    } catch (Exception e) {
    }

    return DEFAULTICON;

  }

  public boolean isActive() {

    if ((mUrl == null) || (mUrl.trim().length() == 0)) { return false; }

    if (mName.trim().length() == 0) { return false; }

    return mActive;
  }

  public void setActive(boolean active) {
    mActive = active;
  }

  public boolean isUserEntry() {
    return mUserEntry;
  }

  public void setUserEntry(boolean user) {
    mUserEntry = user;
  }

  public String toString() {
    return mName;
  }

  public Object clone() {
    return new WebAddress(this);
  }

  public void readData(ObjectInputStream in) throws IOException, ClassNotFoundException {
    int version = in.readInt();

    mName = (String) in.readObject();
    mIconFile = (String) in.readObject();
    mUrl = (String) in.readObject();
    
    if (version == 1) {
      String encoding = (String) in.readObject();
      mUrl = mUrl.replaceAll("\\{0\\}", "{urlencode(title, \""+encoding+"\")}");
    }
    mUserEntry = in.readBoolean();
    mActive = in.readBoolean();
  }

  public void writeData(ObjectOutputStream out) throws IOException {
    out.writeInt(2);

    out.writeObject(mName);
    out.writeObject(mIconFile);
    out.writeObject(mUrl);
    out.writeBoolean(mUserEntry);
    out.writeBoolean(mActive);
  }
}