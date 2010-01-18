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
 *     $Date$
 *   $Author$
 * $Revision$
 */
package tvbrowser.extras.favoritesplugin.dlgs;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.swing.tree.DefaultMutableTreeNode;

import tvbrowser.extras.favoritesplugin.core.ActorsFavorite;
import tvbrowser.extras.favoritesplugin.core.AdvancedFavorite;
import tvbrowser.extras.favoritesplugin.core.Favorite;
import tvbrowser.extras.favoritesplugin.core.TitleFavorite;
import tvbrowser.extras.favoritesplugin.core.TopicFavorite;
import devplugin.Program;

/**
 * A node for the favorite tree.
 * 
 * @author René Mach
 * @since 2.6
 */
public class FavoriteNode extends DefaultMutableTreeNode implements Comparable<FavoriteNode> {
  private boolean mWasExpanded;
  
  /**
   * Creates an instance of this class with
   * the given Object as userObject of this node.
   * 
   * @param userObject The user object for this node.
   */
  public FavoriteNode(Object userObject) {
    if(userObject instanceof Favorite)
      setAllowsChildren(false);
    
    this.userObject = userObject;
  }
  
  /**
   * Reads the node from an ObjectInputStream.
   * 
   * @param in The ObjectInputStream to read from.
   * @param version The version of the data file.
   * @throws IOException Thrown if something went wrong.
   * @throws ClassNotFoundException Thrown if something went wrong.
   */
  protected FavoriteNode(ObjectInputStream in, int version) throws IOException, ClassNotFoundException {
    if(in.readBoolean()) {
      int n = in.readInt();
      setUserObject(in.readObject());
      
      mWasExpanded = in.readBoolean();      
      for(int i = 0; i < n; i++) {
        FavoriteNode node = new FavoriteNode(in, version);
        add(node);
      }
    }
    else {
      setAllowsChildren(false);
      if (version <= 2) {
        userObject = AdvancedFavorite.loadOldFavorite(in);
      }
      else {
        String typeID = (String)in.readObject();
        if (TopicFavorite.TYPE_ID.equals(typeID)) {
          userObject = new TopicFavorite(in);
        }
        else if (TitleFavorite.TYPE_ID.equals(typeID)) {
          userObject = new TitleFavorite(in);
        }
        else if (ActorsFavorite.TYPE_ID.equals(typeID)) {
          userObject = new ActorsFavorite(in);
        }
        else if (AdvancedFavorite.TYPE_ID.equals(typeID)) {
          userObject = new AdvancedFavorite(in);
        }
      }
    }
  }
  
  public String toString() {
    if(userObject instanceof String)
      return userObject.toString();
    else if(userObject != null)
      return ((Favorite)userObject).getName();
    else
      return "NULL";
  }
  
  /**
   * Gets if this node contains a favorite.
   * 
   * @return <code>True</code> if this node contains a favorite, <code>false</code> otherwise.
   */
  public boolean containsFavorite() {
    return userObject instanceof Favorite;
  }

  /**
   * Gets if this node is a directory node.
   * 
   * @return <code>True</code> if this node is a directory node, <code>false</code> otherwise.
   */
  public boolean isDirectoryNode() {
    return userObject instanceof String;
  }
  
  /**
   * Gets the favorite contained by this node if there is one.
   * 
   * @return The favorite contained by this node or <code>null</code> if
   * there is no favorite.
   */
  public Favorite getFavorite() {
    if(containsFavorite())
      return (Favorite)userObject;
    else
      return null;
  }
  
  protected boolean wasExpanded() {
    return mWasExpanded || isRoot();
  }
  
  protected void setWasExpanded(boolean expanded) {
    mWasExpanded = expanded;
  }

  /**
   * Add a favorite to this node if this is a directory node.
   * 
   * @param fav
   *          The favorite to add.
   * @return the newly created sub node or <code>null</code>
   */
  public FavoriteNode add(Favorite fav) {
    if(allowsChildren) {
      final FavoriteNode newChild = new FavoriteNode(fav);
      super.add(newChild);
      return newChild;
    }
    return null;
  }
  
  protected void store(ObjectOutputStream out) throws IOException {
    out.writeBoolean(isDirectoryNode());
    
    if(isDirectoryNode()) {
      out.writeInt(getChildCount());
      out.writeObject(userObject);
      out.writeBoolean(mWasExpanded);
    
      for(int i = 0; i < getChildCount(); i++) {
        ((FavoriteNode)getChildAt(i)).store(out);
      }
    }
    else if(containsFavorite()) {
      out.writeObject(getFavorite().getTypeID());
      getFavorite().writeData(out);
    }
  }
  
  public boolean equals(Object o) {
    if(o instanceof Favorite)
      return containsFavorite() && userObject == o;
    else
      return this == o;
  }

  public int compareTo(FavoriteNode other) {
    return toString().compareToIgnoreCase(other.toString());
  }
  
  /**
   * Gets all programs contained in this node and all children of it.
   * 
   * @param onlyNotExpiredPrograms <code>true</code> if only not expired
   * programs should be returned, <code>false</code> otherwise.
   * @return All programs contained in this node and all children of it.
   */
  public Program[] getAllPrograms(boolean onlyNotExpiredPrograms) {
    try{
    if(isDirectoryNode()) {
      Program[] progs = new Program[0];
      
      for(int i = 0; i < getChildCount(); i++) {
        Program[] p = ((FavoriteNode)getChildAt(i)).getAllPrograms(onlyNotExpiredPrograms);
        
        if(p != null && p.length > 0 ) {
          Program[] newArr = new Program[progs.length + p.length];
          
          System.arraycopy(progs,0,newArr,0,progs.length);
          System.arraycopy(p,0,newArr,progs.length,p.length);
          
          progs = newArr;
        }
      }
      
      return progs;
    }
    else {
      return getFavorite().getWhiteListPrograms(onlyNotExpiredPrograms);
    }
    }catch(Exception e) {e.printStackTrace();}
    return null;
  }
}
