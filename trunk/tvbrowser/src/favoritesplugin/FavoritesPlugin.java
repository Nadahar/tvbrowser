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

package favoritesplugin;

import java.io.*;
import java.util.Properties;
import java.util.ArrayList;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import util.ui.UiUtilities;
import util.exc.*;

import devplugin.*;
import javax.swing.*;


/**
 * Plugin for managing the favorite programs.
 *
 * @author Til Schneider, www.murfman.de
 */
public class FavoritesPlugin extends Plugin {

  /** The localizer for this class. */  
  private static final util.ui.Localizer mLocalizer
    = util.ui.Localizer.getLocalizerFor(FavoritesPlugin.class);

  private static FavoritesPlugin mInstance;  
  private Favorite[] mFavoriteArr;
  
  /** The IDs of the plugins that should receive the favorites. */
  private String[] mClientPluginIdArr;
  
  private Properties mSettings;
  

  
  
  /**
   * Creates a new instance of FavoritesPlugin.
   */
  public FavoritesPlugin() {
    mFavoriteArr = new Favorite[0];
    mClientPluginIdArr = new String[0];
    
    mInstance = this;
  }




  public static FavoritesPlugin getInstance() {
    return mInstance;
  }

  ImageIcon getImageIcon(String fileName) {
    return createImageIcon(fileName);
  }

  
  public void readData(ObjectInputStream in)
    throws IOException, ClassNotFoundException
  {
    int version = in.readInt();

    // get the favorites
    int size = in.readInt();
    Favorite[] newFavoriteArr = new Favorite[size];
    for (int i = 0; i < size; i++) {
      newFavoriteArr[i] = new Favorite(in);
    }
    mFavoriteArr = newFavoriteArr;
    
    // mark all the favorites
    for (int i = 0; i < mFavoriteArr.length; i++) {
      Program[] programArr = mFavoriteArr[i].getPrograms();
      for (int j = 0; j < programArr.length; j++) {
        programArr[j].mark(this);        
      }
    }
    
    updateTree();

    // Get the client plugins
    size = in.readInt();
    mClientPluginIdArr = new String[size];
    for (int i = 0; i < size; i++) {
      if (version == 1) {
        // In older versions of TV-Browser, not the plugin ID was saved,
        // but its class name.
        // -> We have to translate the class name into an ID. 
        String className = (String) in.readObject();
        mClientPluginIdArr[i] = "java." + className;
      } else {
        String pluginId = (String) in.readObject();
        mClientPluginIdArr[i] = pluginId;
      }
    }
  }

  private void deleteFavorite(Favorite favorite) {
    favorite.unmarkPrograms();
    ArrayList list = new ArrayList();
    for (int i=0; i<mFavoriteArr.length; i++) {
      if (!mFavoriteArr[i].equals(favorite)) {
        list.add(mFavoriteArr[i]);
        try {
          mFavoriteArr[i].updatePrograms();     // due to overlapping favorites we refresh all of our favorites
        } catch (TvBrowserException e) {
          ErrorHandler.handle(e);
        }
      }
    }
    mFavoriteArr = new Favorite[list.size()];
    list.toArray(mFavoriteArr);
    updateTree();
  }

 public void updateTree() {
    PluginTreeNode node = getRootNode();
    node.removeAllActions();
    node.addAction(new CreateFavoriteAction());
    node.removeAllChildren();
    for (int i=0; i<mFavoriteArr.length; i++) {
      PluginTreeNode curNode = node.addNode(mFavoriteArr[i].getTitle());
      curNode.addAction(new EditFavoriteAction(mFavoriteArr[i]));
      curNode.addAction(new DeleteFavoriteAction(mFavoriteArr[i]));
      curNode.addAction(null);
      curNode.addAction(new RenameFavoriteAction(mFavoriteArr[i]));

      Program[] progs = mFavoriteArr[i].getPrograms();
      for (int j=0; j<progs.length; j++) {
        curNode.addProgram(progs[j]);
      }
    }
    node.update();
  }

  public void writeData(ObjectOutputStream out) throws IOException {
    out.writeInt(2); // version

    out.writeInt(mFavoriteArr.length);
    for (int i = 0; i < mFavoriteArr.length; i++) {
      mFavoriteArr[i].writeData(out);
    }
    
    out.writeInt(mClientPluginIdArr.length);
    for (int i = 0; i < mClientPluginIdArr.length; i++) {
      out.writeObject(mClientPluginIdArr[i]);
    }
  }
  
  /**
    * Called by the host-application during start-up. Implements this method to
    * load your plugins settings from the file system.
    */
   public void loadSettings(Properties settings) {
     mSettings=settings;     
     if (settings==null) {
       throw new IllegalArgumentException("settings is null"); 
     }
   }
  
  
  
   /**
    * Called by the host-application during shut-down. Implements this method to
    * store your plugins settings to the file system.
    */
   public Properties storeSettings() {
     return mSettings;
   }
   
   
   private int getIntegerSetting(Properties prop, String key, int defaultValue) {
     int res=defaultValue;
     try {
       res = Integer.parseInt(prop.getProperty(key,""+defaultValue));
     }catch (NumberFormatException e) {
       // ignore
     }
      return res;
   }

    public ActionMenu getButtonAction() {
      ButtonAction action = new ButtonAction();
      action.setActionListener(new ActionListener(){
        public void actionPerformed(ActionEvent e) {
          showManageFavoritesDialog();
        }
      });

      action.setBigIcon(createImageIcon("favoritesplugin/ThumbUp16.gif"));
      action.setSmallIcon(createImageIcon("favoritesplugin/ThumbUp16.gif"));
      action.setShortDescription(mLocalizer.msg("favoritesManager", "Manage favorite programs"));
      action.setText(mLocalizer.msg( "buttonText", "Manage Favorites" ));



      return new ActionMenu(action);
    }




   public ActionMenu getContextMenuActions(final Program program) {
     ContextMenuAction action = new ContextMenuAction();
     action.setText(mLocalizer.msg("contextMenuText", "Add to favorite programs"));
     action.setSmallIcon(createImageIcon("favoritesplugin/ThumbUp16.gif"));
     action.setActionListener(new ActionListener(){
        public void actionPerformed(ActionEvent event) {
          showEditFavoriteDialog(program);
        }
      });
      return new ActionMenu(action);
   }


  private void showManageFavoritesDialog() {
    int splitPanePosition=getIntegerSetting(mSettings,"splitpanePosition",200);
    int width=getIntegerSetting(mSettings,"width",500);
    int height=getIntegerSetting(mSettings,"height",300);
    ManageFavoritesDialog dlg = new ManageFavoritesDialog(this, getParentFrame(), mFavoriteArr,splitPanePosition);
    dlg.setSize(new Dimension(width,height));
    UiUtilities.centerAndShow(dlg);
    
    if (dlg.getOkWasPressed()) {
      mFavoriteArr = dlg.getFavorites();
      updateTree();
    }
    splitPanePosition=dlg.getSplitpanePosition();
    mSettings.setProperty("splitpanePosition",""+splitPanePosition);
    mSettings.setProperty("width",""+dlg.getWidth());
    mSettings.setProperty("height",""+dlg.getHeight());
  }



  private void addFavorite(Favorite fav) {
    Favorite[] newFavoritesArr = new Favorite[mFavoriteArr.length + 1];
    System.arraycopy(mFavoriteArr, 0, newFavoritesArr, 0, mFavoriteArr.length);
    newFavoritesArr[mFavoriteArr.length] = fav;
    mFavoriteArr = newFavoritesArr;
  }

  public void showEditFavoriteDialog(Program program) {
    Favorite favorite = new Favorite(program.getTitle());

    EditFavoriteDialog dlg = new EditFavoriteDialog(getParentFrame(), favorite);
    dlg.centerAndShow();

    if (dlg.getOkWasPressed()) {
      addFavorite(favorite);
    /*  Favorite[] newFavoritesArr = new Favorite[mFavoriteArr.length + 1];
      System.arraycopy(mFavoriteArr, 0, newFavoritesArr, 0, mFavoriteArr.length);
      newFavoritesArr[mFavoriteArr.length] = favorite;
      mFavoriteArr = newFavoritesArr;   */
      updateTree();
    }
  }
  

  
  public PluginInfo getInfo() {
    String name = mLocalizer.msg("favoritesManager", "Manage favorite programs");
    String desc = mLocalizer.msg("description",
      "Automatically marks your favorite programs and passes them to other Plugins." );
    String author = "Til Schneider, www.murfman.de" ;
    
    return new PluginInfo(name, desc, author, new Version(1, 9));
  }



  public String getMarkIconName() {
    return "favoritesplugin/ThumbUp16.gif";
  }
  

  void unmark(Program[] programArr) {
    // unmark all programs with this plugin
    for (int i = 0; i < programArr.length; i++) {
      programArr[i].unmark(this);    
    }
  }

  
  void mark(Program[] programArr) {
    // mark all programs with this plugin
    for (int i = 0; i < programArr.length; i++) {
      programArr[i].mark(this);    
    }

    // Pass the program list to all client plugins
    for (int i = 0; i < mClientPluginIdArr.length; i++) {
      PluginAccess plugin = getPluginManager().getActivatedPluginForId(mClientPluginIdArr[i]);
      if (plugin != null) {
        plugin.receivePrograms(programArr);
      }
    }
  }



  public void handleTvDataUpdateFinished() {
    // Update all favorites
    for (int i = 0; i < mFavoriteArr.length; i++) {
      try {
        mFavoriteArr[i].updatePrograms();
      }
      catch (TvBrowserException exc) {
        ErrorHandler.handle(exc);
      }
    }
    updateTree();    
  }
  
  
  
  public String[] getClientPluginIds() {
    return mClientPluginIdArr;
  }
  
  
  
  public void setClientPluginIds(String[] clientPluginArr) {
    mClientPluginIdArr = clientPluginArr;
  }

  
  
  /**
   * Returns a new SettingsTab object, which is added to the settings-window.
   */
  public SettingsTab getSettingsTab() {
    return new FavoritesSettingTab();
  }
  
  public boolean canUseProgramTree() {
    return true;
  }


  class RenameFavoriteAction extends ButtonAction {
    private Favorite mFavorite;

    public RenameFavoriteAction(Favorite favorite) {
      mFavorite = favorite;
      //super.setSmallIcon(createImageIcon("favoritesplugin/Edit16.gif"));
      super.setText(mLocalizer.msg("rename","rename"));
    }

    public void actionPerformed(ActionEvent e) {
      String newName = (String)JOptionPane.showInputDialog(getParentFrame(), "Title:","Rename favorite", JOptionPane.PLAIN_MESSAGE, null, null, mFavorite.getTitle());
      if (newName != null) {
        mFavorite.setTitle(newName);
        updateTree();
      }
    }
  }

  class EditFavoriteAction extends ButtonAction {

    private Favorite mFavorite;

    public EditFavoriteAction(Favorite favorite) {
      mFavorite = favorite;
      //super.setSmallIcon(createImageIcon("favoritesplugin/Edit16.gif"));
      super.setText(mLocalizer.msg("edit","edit"));
    }

    public void actionPerformed(ActionEvent e) {
      EditFavoriteDialog dlg = new EditFavoriteDialog(getParentFrame(), mFavorite);
      dlg.centerAndShow();
      if (dlg.getOkWasPressed()) {
        updateTree();
      }
    }
  }

  class DeleteFavoriteAction extends ButtonAction {

    private Favorite mFavorite;

    public DeleteFavoriteAction(Favorite favorite) {
      mFavorite = favorite;
      //super.setSmallIcon(createImageIcon("favoritesplugin/Delete16.gif"));
      super.setText(mLocalizer.msg("delete","delete"));
    }

    public void actionPerformed(ActionEvent e) {
      deleteFavorite(mFavorite);
    }
  }

  class CreateFavoriteAction extends ButtonAction {
    
    public CreateFavoriteAction() {
      super.setSmallIcon(createImageIcon("favoritesplugin/New16.gif"));
      super.setText(mLocalizer.msg("new","Create new favorite"));
    }

    public void actionPerformed(ActionEvent e) {
      Favorite favorite = new Favorite();
      EditFavoriteDialog dlg = new EditFavoriteDialog(getParentFrame(), favorite);
      dlg.centerAndShow();
      if (dlg.getOkWasPressed()) {
        addFavorite(favorite);
        updateTree();
      }
    }
  }

}
