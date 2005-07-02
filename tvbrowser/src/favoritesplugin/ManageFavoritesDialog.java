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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Enumeration;

import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import util.exc.ErrorHandler;
import util.exc.TvBrowserException;
import util.ui.ExtensionFileFilter;
import util.ui.ImageUtilities;
import util.ui.ProgramList;
import util.ui.SendToPluginDialog;
import util.ui.UiUtilities;
import devplugin.Channel;
import devplugin.Plugin;
import devplugin.Program;

/**
 * A dialog for managing the favorite programs.
 *
 * @author Til Schneider, www.murfman.de
 */
public class ManageFavoritesDialog extends JDialog {

  /** The localizer for this class. */  
  public static final util.ui.Localizer mLocalizer
    = util.ui.Localizer.getLocalizerFor(ManageFavoritesDialog.class);

  private DefaultListModel mFavoritesListModel, mProgramListModel;
  private JList mFavoritesList;
  private ProgramList mProgramList;
  private JSplitPane mSplitPane;
  private JButton mNewBt, mEditBt, mDeleteBt, mUpBt, mDownBt, mSortBt, mImportBt, mSendBt;
  private JButton mCloseBt;

  private boolean mOkWasPressed = false;
  
  /** The FavoritesPlugin */
  private Plugin mPlugin;
  
  /**
   * Creates a new instance of ManageFavoritesDialog.
   */
  public ManageFavoritesDialog(Plugin plugin, Frame parent, Favorite[] favoriteArr, int splitPanePosition) {
    super(parent, true);
    mPlugin = plugin;
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

    msg = mLocalizer.msg("send", "Send Programs to another Plugin");
    icon = new ImageIcon("imgs/SendToPlugin24.png");
    mSendBt = UiUtilities.createToolBarButton(msg, icon);
    mSendBt.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
          showSendDialog();
      }
    });
    toolbarPn.add(mSendBt);
    
    msg = mLocalizer.msg("sort", "Sort favorites alphabetically");
    icon = ImageUtilities.createImageIconFromJar("favoritesplugin/Sort24.gif", getClass());
    mSortBt = UiUtilities.createToolBarButton(msg, icon);
    mSortBt.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        sortFavorites();
      }
    });
    toolbarPn.add(mSortBt);

    msg = mLocalizer.msg("import", "Import favorites from TVgenial");
    icon = ImageUtilities.createImageIconFromJar("favoritesplugin/Import24.gif", getClass());
    mImportBt = UiUtilities.createToolBarButton(msg, icon);
    mImportBt.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        importFavorites();
      }
    });
    toolbarPn.add(mImportBt);

    mSplitPane = new JSplitPane();
    mSplitPane.setDividerLocation(splitPanePosition);
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
    mProgramList = new ProgramList(mProgramListModel);
    mProgramList.addMouseListeners(mPlugin);
    scrollPane = new JScrollPane(mProgramList);
    scrollPane.setBorder(null);
    mSplitPane.setRightComponent(scrollPane);
    
    JPanel buttonPn = new JPanel(new FlowLayout(FlowLayout.TRAILING));
    main.add(buttonPn, BorderLayout.SOUTH);
    
    mCloseBt = new JButton(mLocalizer.msg("close", "Close"));
    mCloseBt.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        mOkWasPressed = true;
        dispose();
      }
    });
    buttonPn.add(mCloseBt);
    getRootPane().setDefaultButton(mCloseBt);

    favoriteSelectionChanged();
  }
  
  
  public int getSplitpanePosition() {
    return mSplitPane.getDividerLocation();
  }
  
  public void setSplitpanePosition(int val) {
    mSplitPane.setDividerLocation(val);
  }
  
  protected void favoriteSelectionChanged() {
    int selection = mFavoritesList.getSelectedIndex();
    int size = mFavoritesListModel.getSize();
    
    mEditBt.setEnabled(selection != -1);
    mDeleteBt.setEnabled(selection != -1);
    
    mUpBt.setEnabled(selection > 0);
    mDownBt.setEnabled((selection != -1) && (selection < (size - 1)));
    
    mSortBt.setEnabled(size >= 2);
    
    if (selection == -1) {
      mProgramListModel.clear();
      mSendBt.setEnabled(false);
    } else {
      Favorite fav = (Favorite) mFavoritesListModel.get(selection);
      Program[] programArr = fav.getPrograms();

      mSendBt.setEnabled(programArr.length > 0);
      
      mProgramListModel.clear();
      mProgramListModel.ensureCapacity(programArr.length);
      for (int i = 0; i < programArr.length; i++) {
        mProgramListModel.addElement(programArr[i]);
      }
    }
  }

  public void showSendDialog() {
      int selection = mFavoritesList.getSelectedIndex();

      if(selection == -1) {
          return;
      }
      
      Favorite fav = (Favorite) mFavoritesListModel.get(selection);
      Program[] programArr = fav.getPrograms();

      SendToPluginDialog send = new SendToPluginDialog(mPlugin, this, programArr);

      send.setVisible(true);
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
      
      if (JOptionPane.showConfirmDialog(this, 
      msg, msg = mLocalizer.msg("delete", "Delete selected favorite..."), JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
        Favorite fav = (Favorite) mFavoritesListModel.get(selection);
        fav.unmarkPrograms();
        mFavoritesListModel.remove(selection);
        Object[] o = mFavoritesListModel.toArray();
        for (int i=0; i<o.length; i++) {
          Favorite f = (Favorite)o[i];
          try {
            f.updatePrograms();
          } catch (TvBrowserException e) {
            ErrorHandler.handle(e);
          }
        }
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

  
  protected void sortFavorites() {
    String msg = mLocalizer.msg("reallySort", "Do you really want to sort your " +
        "favorites?\n\nThe current order will get lost.");
    String title = UIManager.getString("OptionPane.titleText");
    
    int result = JOptionPane.showConfirmDialog(this, msg, title, JOptionPane.YES_NO_OPTION);
    if (result == JOptionPane.YES_OPTION) {
      // Create a comparator for Favorites
      Comparator comp = new Comparator() {
        public int compare(Object o1, Object o2) {
          String text1 = ((Favorite) o1).getSearchFormSettings().getSearchText();
          String text2 = ((Favorite) o2).getSearchFormSettings().getSearchText();
          text1 = text1.toLowerCase();
          text2 = text2.toLowerCase();
          return text1.compareTo(text2);
        }
      };
      
      // Sort the list
      Object[] asArr = mFavoritesListModel.toArray();
      Arrays.sort(asArr, comp);
      mFavoritesListModel.removeAllElements();
      for (int i = 0; i < asArr.length; i++) {
        mFavoritesListModel.addElement(asArr[i]);
      }
      
      // Update the buttons
      favoriteSelectionChanged();
    }
  }
  

  protected void importFavorites() {
    JFileChooser fileChooser = new JFileChooser();
    String[] extArr = { ".txt" };
    String msg = mLocalizer.msg("importFile.TVgenial", "Text file (from TVgenial) (.txt)");
    fileChooser.setFileFilter(new ExtensionFileFilter(extArr, msg));
    fileChooser.showOpenDialog(this);
    
    File file = fileChooser.getSelectedFile();
    if (file != null) {
      FileReader reader = null;
      int importedFavoritesCount = 0;
      try {
        reader = new FileReader(file);
        BufferedReader lineReader = new BufferedReader(reader);
        String line;
        while ((line = lineReader.readLine()) != null) {
          line = line.trim();
          if ((line.length() > 0) && (! line.startsWith("***"))) {
            // This is a favorite -> Check whether we already have such a favorite
            Enumeration en = mFavoritesListModel.elements();
            boolean alreadyKnown = false;
            while (en.hasMoreElements()) {
              Favorite fav = (Favorite) en.nextElement();
              String searchText = fav.getSearchFormSettings().getSearchText();
              if (line.equalsIgnoreCase(searchText)) {
                alreadyKnown = true;
                break;
              }
            }
            
            // Import the favorite if it is new
            if (! alreadyKnown) {
              Favorite fav = new Favorite();
              fav.getSearchFormSettings().setSearchText(line);
              fav.updatePrograms();
              
              mFavoritesListModel.addElement(fav);
              importedFavoritesCount++;
            }
          }
        }
      }
      catch (Exception exc) {
        msg = mLocalizer.msg("error.1", "Importing text file failed: {0}.",
                             file.getAbsolutePath());
        ErrorHandler.handle(msg, exc);
      }
      finally {
        if (reader != null) {
          try { reader.close(); } catch (IOException exc) {}
        }
      }

      if (importedFavoritesCount == 0) {
        msg = mLocalizer.msg("error.2", "There are no new favorites in {0}.",
                             file.getAbsolutePath());
        JOptionPane.showMessageDialog(this, msg);
      } else {
        // Scroll to the end
        mFavoritesList.ensureIndexIsVisible(mFavoritesListModel.size() - 1);
        
        // Select the first new fevorite
        int firstNewIdx = mFavoritesListModel.size() - importedFavoritesCount;
        mFavoritesList.setSelectedIndex(firstNewIdx);
        mFavoritesList.ensureIndexIsVisible(firstNewIdx);

        msg = mLocalizer.msg("importDone", "There were {0} new favorites imported.",
            new Integer(importedFavoritesCount));
        JOptionPane.showMessageDialog(this, msg);
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
        String info = fav.getSearchFormSettings().getSearchText();
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
        info += " (" + fav.getPrograms().length + ")";
        
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
