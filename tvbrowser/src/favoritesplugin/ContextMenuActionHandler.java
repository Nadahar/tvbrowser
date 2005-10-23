package favoritesplugin;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;

import devplugin.Program;

public class ContextMenuActionHandler extends AbstractAction {
  private Program mProgram;
  private String mAction;
  private Favorite mFavorite;
  
  public ContextMenuActionHandler(Program program, String action, Favorite fav, String title) {
    mProgram = program;
    mAction = action;
    mFavorite = fav;
    
    putValue(Action.NAME,title);
  }

  public void actionPerformed(ActionEvent e) {
    if(mAction.equals("add"))
      FavoritesPlugin.getInstance().addToBlackList(mProgram,mFavorite);
    if(mAction.equals("addtoall"))
      FavoritesPlugin.getInstance().addToAllBlackLists(mProgram);
    if(mAction.equals("remove"))
      FavoritesPlugin.getInstance().removeFromBlackList(mProgram,mFavorite);
    if(mAction.equals("removefromall"))
      FavoritesPlugin.getInstance().removeFromAllBlackLists(mProgram);
  }

}
