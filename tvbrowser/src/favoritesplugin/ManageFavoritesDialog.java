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

package favoritesplugin;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

import util.ui.UiUtilities;

import devplugin.*;

/**
 * A dialog for managing the favorite programs.
 *
 * @author Til Schneider, www.murfman.de
 */
public class ManageFavoritesDialog extends JDialog {

  /** The localizer for this class. */  
  private static final util.ui.Localizer mLocalizer
    = util.ui.Localizer.getLocalizerFor(ManageFavoritesDialog.class);

  private Channel[] mSubsribedChannelArr;
  
  private DefaultListModel mFavoritesListModel;
  private JList mFavoritesList;
  private JButton mNewBt, mEditBt, mDeleteBt, mOkBt, mCancelBt;
  
  private boolean mOkWasPressed = false;
  
  
  
  /**
   * Creates a new instance of ManageFavoritesDialog.
   */
  public ManageFavoritesDialog(Frame parent, Favorite[] favoriteArr) {
    super(parent, true);

    mSubsribedChannelArr = Plugin.getPluginManager().getSubscribedChannels();

    setTitle(mLocalizer.msg("title", "Manage favorite programs"));
    
    JPanel main = new JPanel(new BorderLayout());
    main.setBorder(UiUtilities.DIALOG_BORDER);
    setContentPane(main);
    
    JPanel toolbarPn = new JPanel();
    main.add(toolbarPn, BorderLayout.NORTH);
    
    mNewBt = new JButton(mLocalizer.msg("new", "New..."));
    mNewBt.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        newFavorite();
      }
    });
    toolbarPn.add(mNewBt);
    
    mEditBt = new JButton(mLocalizer.msg("edit", "Edit..."));
    mEditBt.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        editSelectedFavorite();
      }
    });
    toolbarPn.add(mEditBt);
    
    mDeleteBt = new JButton(mLocalizer.msg("delete", "Delete"));
    mDeleteBt.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        deleteSelectedFavorite();
      }
    });
    toolbarPn.add(mDeleteBt);
    
    mFavoritesListModel = new DefaultListModel();
    mFavoritesListModel.ensureCapacity(favoriteArr.length);
    for (int i = 0; i < favoriteArr.length; i++) {
      mFavoritesListModel.addElement(favoriteArr[i]);
    }
    mFavoritesList = new JList(mFavoritesListModel);
    mFavoritesList.setCellRenderer(new FavoriteListCellRenderer());
    ListSelectionModel selModel = mFavoritesList.getSelectionModel();
    selModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    selModel.addListSelectionListener(new ListSelectionListener() {
      public void valueChanged(ListSelectionEvent evt) {
        updateEnabled();
      }
    });
    main.add(new JScrollPane(mFavoritesList), BorderLayout.CENTER);
    
    JPanel buttonPn = new JPanel();
    main.add(buttonPn, BorderLayout.SOUTH);
    
    mOkBt = new JButton(mLocalizer.msg("ok", "OK"));
    mOkBt.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        mOkWasPressed = true;
        dispose();
      }
    });
    buttonPn.add(mOkBt);
    getRootPane().setDefaultButton(mOkBt);

    mCancelBt = new JButton(mLocalizer.msg("cancel", "Cancel"));
    mCancelBt.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        dispose();
      }
    });
    buttonPn.add(mCancelBt);
    
    updateEnabled();
    pack();
  }
  
  
  
  protected void updateEnabled() {
    boolean enabled = (mFavoritesList.getSelectedIndex() != -1);
    mEditBt.setEnabled(enabled);
    mDeleteBt.setEnabled(enabled);
  }

  
  
  protected void newFavorite() {
    Favorite fav = new Favorite();
    
    EditFavoriteDialog dlg = new EditFavoriteDialog(this, fav);
    dlg.centerAndShow();
    
    if (dlg.getOkWasPressed()) {
      mFavoritesListModel.addElement(fav);
      int idx = mFavoritesListModel.size() - 1;
      mFavoritesList.setSelectedIndex(idx);
      mFavoritesList.ensureIndexIsVisible(idx);
    }
  }

  
  
  protected void editSelectedFavorite() {
    Favorite fav = (Favorite) mFavoritesList.getSelectedValue();
    if (fav != null) {
      EditFavoriteDialog dlg = new EditFavoriteDialog(this, fav);
      dlg.centerAndShow();

      if (dlg.getOkWasPressed()) {
        mFavoritesList.repaint();
      }
    }
  }

  
  
  protected void deleteSelectedFavorite() {
    int selection = mFavoritesList.getSelectedIndex();
    if (selection != -1) {
      Favorite fav = (Favorite) mFavoritesListModel.get(selection);
      fav.unmarkPrograms();
      
      mFavoritesListModel.remove(selection);
    }
  }

  

  public boolean getOkWasPressed() {
    return mOkWasPressed;
  }
  
  
  
  public Favorite[] getFavorites() {
    Favorite[] favoriteArr = new Favorite[mFavoritesListModel.size()];
    mFavoritesListModel.copyInto(favoriteArr);
    return favoriteArr;
  }
  
  
  // inner class FavoriteListCellRenderer
  
  
  class FavoriteListCellRenderer extends DefaultListCellRenderer {
    
    public Component getListCellRendererComponent(JList list, Object value,
      int index, boolean isSelected, boolean cellHasFocus)
    {
      if (value instanceof Favorite) {
        Favorite fav = (Favorite)value;
        String info = fav.getTerm();
        if (fav.getUseCertainChannel() || fav.getUseCertainTimeOfDay()) {
          info += " (";
          if (fav.getUseCertainChannel()) {
            Channel channel = fav.getCertainChannel();
            if (channel != null) {
              info += channel.getName();
            }
          }
          if (fav.getUseCertainTimeOfDay()) {
            if (fav.getUseCertainChannel()) {
              info += ", ";
            }
            info += toTimeString(fav.getCertainFromTime());
            info += " - ";
            info += toTimeString(fav.getCertainToTime());
          }
          
          info += ")";
        }
        value = info;
      }
      
      return super.getListCellRendererComponent(list, value, index, isSelected,
        cellHasFocus);
    }

    

    private String toTimeString(int time) {
      int hours = time / 60;
      int minutes = time % 60;
      
      return "" + hours + ":" + ((minutes < 10) ? ("0" + minutes) : "" + minutes);
    }
    
  }
  
}
