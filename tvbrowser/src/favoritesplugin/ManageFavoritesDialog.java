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

import util.ui.*;

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
  
  private DefaultListModel mFavoritesListModel, mProgramListModel;
  private JList mFavoritesList, mProgramList;
  private JSplitPane mSplitPane;
  private JButton mNewBt, mEditBt, mDeleteBt, mUpBt, mDownBt, mOkBt, mCancelBt;
  
  private boolean mOkWasPressed = false;
  
  
  
  /**
   * Creates a new instance of ManageFavoritesDialog.
   */
  public ManageFavoritesDialog(Frame parent, Favorite[] favoriteArr) {
    super(parent, true);
    
    mSubsribedChannelArr = Plugin.getPluginManager().getSubscribedChannels();
    
    String msg;
    Icon icon;

    setTitle(mLocalizer.msg("title", "Manage favorite programs"));
    
    JPanel main = new JPanel(new BorderLayout(5, 5));
    main.setBorder(UiUtilities.DIALOG_BORDER);
    setContentPane(main);
    
    JPanel toolbarPn = new JPanel(new FlowLayout(FlowLayout.LEADING, 0, 0));
    main.add(toolbarPn, BorderLayout.NORTH);
    
    msg = mLocalizer.msg("new", "Create a new favorite...");
    icon = ImageUtilities.createImageIconFromJar("favoritesplugin/New24.gif", getClass());
    mNewBt = UiUtilities.createToolBarButton(msg, icon);
    mNewBt.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        newFavorite();
      }
    });
    toolbarPn.add(mNewBt);

    msg = mLocalizer.msg("edit", "Edit the selected favorite...");
    icon = ImageUtilities.createImageIconFromJar("favoritesplugin/Edit24.gif", getClass());
    mEditBt = UiUtilities.createToolBarButton(msg, icon);
    mEditBt.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        editSelectedFavorite();
      }
    });
    toolbarPn.add(mEditBt);

    msg = mLocalizer.msg("delete", "Delete selected favorite...");
    icon = ImageUtilities.createImageIconFromJar("favoritesplugin/Delete24.gif", getClass());
    mDeleteBt = UiUtilities.createToolBarButton(msg, icon);
    mDeleteBt.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        deleteSelectedFavorite();
      }
    });
    toolbarPn.add(mDeleteBt);

    msg = mLocalizer.msg("up", "Move the selected favorite up");
    icon = ImageUtilities.createImageIconFromJar("favoritesplugin/Up24.gif", getClass());
    mUpBt = UiUtilities.createToolBarButton(msg, icon);
    mUpBt.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        moveSelectedFavorite(-1);
      }
    });
    toolbarPn.add(mUpBt);
    
    msg = mLocalizer.msg("down", "Move the selected favorite down");
    icon = ImageUtilities.createImageIconFromJar("favoritesplugin/Down24.gif", getClass());
    mDownBt = UiUtilities.createToolBarButton(msg, icon);
    mDownBt.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        moveSelectedFavorite(1);
      }
    });
    toolbarPn.add(mDownBt);

    mSplitPane = new JSplitPane();
    main.add(mSplitPane, BorderLayout.CENTER);
    
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
        favoriteSelectionChanged();
      }
    });
    JScrollPane scrollPane = new JScrollPane(mFavoritesList);
    scrollPane.setBorder(null);
    mSplitPane.setLeftComponent(scrollPane);

    mProgramListModel = new DefaultListModel();
    mProgramList = new JList(mProgramListModel);
    mProgramList.setCellRenderer(new ProgramListCellRenderer());
    scrollPane = new JScrollPane(mProgramList);
    scrollPane.setBorder(null);
    mSplitPane.setRightComponent(scrollPane);
    
    JPanel buttonPn = new JPanel(new FlowLayout(FlowLayout.TRAILING));
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
    
    
	
	mProgramList.addMouseListener(new MouseAdapter() {
	  public void mouseClicked(MouseEvent e) {
	    if (SwingUtilities.isRightMouseButton(e)) {
		  int inx=mProgramList.locationToIndex(e.getPoint());
		  Program p=(Program)mProgramListModel.getElementAt(inx);
		  JPopupMenu menu=devplugin.Plugin.getPluginManager().createPluginContextMenu(p,FavoritesPlugin.getInstance());
		  menu.show(mProgramList, e.getX() - 15, e.getY() - 15);
		}
		}
    	});
    
    buttonPn.add(mCancelBt);
    
    favoriteSelectionChanged();
    pack();
    
    
  }
  
  
  
  protected void favoriteSelectionChanged() {
    int selection = mFavoritesList.getSelectedIndex();
    int size = mFavoritesListModel.getSize();
    
    mEditBt.setEnabled(selection != -1);
    mDeleteBt.setEnabled(selection != -1);
    
    mUpBt.setEnabled(selection > 0);
    mDownBt.setEnabled((selection != -1) && (selection < (size - 1)));
    
    if (selection == -1) {
      mProgramListModel.clear();
    } else {
      Favorite fav = (Favorite) mFavoritesListModel.get(selection);
      Program[] programArr = fav.getPrograms();
      
      mProgramListModel.clear();
      mProgramListModel.ensureCapacity(programArr.length);
      for (int i = 0; i < programArr.length; i++) {
        mProgramListModel.addElement(programArr[i]);
      }
    }
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
        favoriteSelectionChanged();
      }
    }
  }

  
  
  protected void deleteSelectedFavorite() {
    int selection = mFavoritesList.getSelectedIndex();
    if (selection != -1) {
      String msg = mLocalizer.msg("reallyDelete", "Really delete favorite?");
      if (JOptionPane.showConfirmDialog(this, msg) == JOptionPane.YES_OPTION) {
        Favorite fav = (Favorite) mFavoritesListModel.get(selection);
        fav.unmarkPrograms();

        mFavoritesListModel.remove(selection);
      }
    }
  }

  
  
  protected void moveSelectedFavorite(int rowCount) {
    int selection = mFavoritesList.getSelectedIndex();
    if (selection != -1) {
      int targetPos = selection + rowCount;
      if ((targetPos >= 0) && (targetPos < mFavoritesListModel.size())) {
        Favorite fav = (Favorite) mFavoritesListModel.remove(selection);
        mFavoritesListModel.add(targetPos, fav);
        mFavoritesList.setSelectedIndex(targetPos);
        mFavoritesList.ensureIndexIsVisible(targetPos);
      }
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
