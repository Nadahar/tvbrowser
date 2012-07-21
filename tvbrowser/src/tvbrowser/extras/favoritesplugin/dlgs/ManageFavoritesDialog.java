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
 *  $RCSfile$
 *   $Source$
 *     $Date$
 *   $Author$
 * $Revision$
 */

package tvbrowser.extras.favoritesplugin.dlgs;

import java.awt.BorderLayout;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import tvbrowser.extras.favoritesplugin.core.Favorite;
import util.ui.UiUtilities;
import util.ui.WindowClosingIf;

/**
 * A dialog for managing the favorite programs.
 *
 * @author Til Schneider, www.murfman.de
 */
public class ManageFavoritesDialog extends JDialog implements WindowClosingIf{

  /** The localizer for this class. */
  protected static final util.ui.Localizer mLocalizer
    = util.ui.Localizer.getLocalizerFor(ManageFavoritesDialog.class);

  private static ManageFavoritesDialog mInstance = null;
  
  private ManageFavoritesPanel mManagePanel;

  public ManageFavoritesDialog(Window parent, Favorite[] favoriteArr,
      int splitPanePosition, boolean showNew, Favorite initialSelection) {
    super(parent);
    setModal(true);
    
    init(favoriteArr, splitPanePosition, showNew, initialSelection);
  }

  private void init(Favorite[] favoriteArr, int splitPanePosition, boolean showNew, Favorite initialSelection) {
    mInstance = this;
    
    mManagePanel = new ManageFavoritesPanel(favoriteArr, splitPanePosition, showNew, initialSelection);
    
    ((JPanel)getContentPane()).setLayout(new BorderLayout());
    ((JPanel)getContentPane()).add(mManagePanel,BorderLayout.CENTER);
    
    addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(WindowEvent e) {
        mInstance = null;
      }
    });

    UiUtilities.registerForClosing(this);

    if(showNew) {
      setTitle(mLocalizer.msg("newTitle", "New programs found"));
    } else {
      setTitle(mLocalizer.msg("title", "Manage favorite programs"));
    }
    
    setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
    addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        close();
      }
    });
  }

  public static ManageFavoritesDialog getInstance() {
    return mInstance;
  }

  public void close() {
    mInstance = null;

    mManagePanel.close();

    dispose();
  }

  public void favoriteSelectionChanged() {
    mManagePanel.favoriteSelectionChanged();
  }
  
  public int getSplitpanePosition() {
    return mManagePanel.getSplitpanePosition();
  }

  public void setSplitpanePosition(int val) {
    mManagePanel.setSplitpanePosition(val);
  }

  public void addFavorite(Favorite fav, Object dummy) {
    mManagePanel.addFavorite(fav, dummy);
  }
  
  public void editSelectedFavorite() {
    mManagePanel.editSelectedFavorite();
  }
  
  public void newFavorite(FavoriteNode parent) {
    mManagePanel.newFavorite(parent);
  }
  
  public void showSendDialog() {
    mManagePanel.showSendDialog();
  }
  
  public void deleteSelectedFavorite() {
    mManagePanel.deleteSelectedFavorite();
  }
  
  public boolean programListIsEmpty() {
    return mManagePanel.programListIsEmpty();
  }
  
  public boolean isShowingNewFoundPrograms() {
    return mManagePanel.isShowingNewFoundPrograms();
  }
}

