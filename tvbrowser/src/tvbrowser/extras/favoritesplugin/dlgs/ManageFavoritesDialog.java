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
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;

import tvbrowser.core.icontheme.IconLoader;
import tvbrowser.extras.favoritesplugin.FavoritesPlugin;
import tvbrowser.extras.favoritesplugin.core.AdvancedFavorite;
import tvbrowser.extras.favoritesplugin.core.Favorite;
import tvbrowser.extras.favoritesplugin.wizards.TypeWizardStep;
import tvbrowser.extras.favoritesplugin.wizards.WizardHandler;
import tvbrowser.ui.mainframe.MainFrame;
import util.exc.ErrorHandler;
import util.exc.TvBrowserException;
import util.program.ProgramUtilities;
import util.settings.PluginPictureSettings;
import util.settings.ProgramPanelSettings;
import util.ui.DragAndDropMouseListener;
import util.ui.ExtensionFileFilter;
import util.ui.ListDragAndDropHandler;
import util.ui.ListDropAction;
import util.ui.Localizer;
import util.ui.ProgramList;
import util.ui.SendToPluginDialog;
import util.ui.UiUtilities;
import util.ui.WindowClosingIf;

import com.jgoodies.forms.factories.Borders;

import devplugin.Program;
import devplugin.SettingsItem;


/**
 * A dialog for managing the favorite programs.
 *
 * @author Til Schneider, www.murfman.de
 */
public class ManageFavoritesDialog extends JDialog implements ListDropAction, WindowClosingIf,TreeSelectionListener{

  /** The localizer for this class. */
  protected static final util.ui.Localizer mLocalizer
    = util.ui.Localizer.getLocalizerFor(ManageFavoritesDialog.class);

  private DefaultListModel mFavoritesListModel, mProgramListModel;
  private JList mFavoritesList;
  private FavoriteTree mFavoriteTree;
  private ProgramList mProgramList;
  private JSplitPane mSplitPane;
  private JButton mNewBt, mEditBt, mDeleteBt, mUpBt, mDownBt, mSortAlphaBt, mSortCountBt, mImportBt, mSendBt;
  private JButton mCloseBt;
  private JScrollPane mProgramScrollPane;

  private boolean mShowNew = false;
  private static ManageFavoritesDialog mInstance = null;
  private JCheckBox mBlackListChb;

  public ManageFavoritesDialog(Window parent, Favorite[] favoriteArr,
      int splitPanePosition, boolean showNew) {
    super(parent);
    setModal(true);
    init(favoriteArr, splitPanePosition, showNew);
  }

  private void init(Favorite[] favoriteArr, int splitPanePosition, boolean showNew) {
    mInstance = this;

    mShowNew = showNew;

    String msg;
    Icon icon;

    addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(WindowEvent e) {
        mInstance = null;
      }
    });

    UiUtilities.registerForClosing(this);

    if(mShowNew) {
      setTitle(mLocalizer.msg("newTitle", "New programs found"));
    } else {
      setTitle(mLocalizer.msg("title", "Manage favorite programs"));
    }

    JPanel main = new JPanel(new BorderLayout(5, 5));
    main.setBorder(Borders.DLU4_BORDER);
    setContentPane(main);

    JToolBar toolbarPn = new JToolBar();
    toolbarPn.setFloatable(false);
    toolbarPn.setBorder(BorderFactory.createEmptyBorder());
    main.add(toolbarPn, BorderLayout.NORTH);

    if(!mShowNew) {
      if(favoriteArr == null) {
        JButton newFolder = UiUtilities.createToolBarButton(mLocalizer.msg("newFolder", "New folder"),
            IconLoader.getInstance().getIconFromTheme("actions", "folder-new", 22));
        
        newFolder.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            TreePath path = mFavoriteTree.getSelectionPath();
            
            if(path != null) {
              mFavoriteTree.newFolder((FavoriteNode)path.getLastPathComponent());
            } else {
              mFavoriteTree.newFolder(mFavoriteTree.getRoot());
            }
          }          
        });
        
        toolbarPn.add(newFolder);
      }
      
      addToolbarSeperator(toolbarPn);
      
      msg = mLocalizer.msg("new", "Create a new favorite...");
      icon = FavoritesPlugin.getInstance().getIconFromTheme("actions", "document-new", 22);
      mNewBt = UiUtilities.createToolBarButton(msg, icon);
      mNewBt.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent evt) {
          TreePath path = mFavoriteTree.getSelectionPath();
          
          if(path == null) {
            newFavorite(mFavoriteTree.getRoot());
          } else {
            FavoriteNode node = (FavoriteNode)path.getLastPathComponent();
                        
            newFavorite(node.isDirectoryNode() ? node : (FavoriteNode)node.getParent());
          }
        }
      });

      toolbarPn.add(mNewBt);
    }
    
    msg = mLocalizer.msg("edit", "Edit the selected favorite...");
    icon = IconLoader.getInstance().getIconFromTheme("actions", "document-edit", 22);
    mEditBt = UiUtilities.createToolBarButton(msg, icon);
    mEditBt.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        if(mShowNew) {
          editSelectedFavorite();
        } else {
          FavoriteNode node = (FavoriteNode)mFavoriteTree.getSelectionPath().getLastPathComponent();
          
          if(node.isDirectoryNode()) {
            mFavoriteTree.renameFolder(node);
          } else {
            editSelectedFavorite();
          }
        }
      }
    });
    toolbarPn.add(mEditBt);

    msg = mLocalizer.msg("delete", "Delete selected favorite...");
    icon = FavoritesPlugin.getInstance().getIconFromTheme("actions", "edit-delete", 22);
    mDeleteBt = UiUtilities.createToolBarButton(msg, icon);
    mDeleteBt.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        if(mShowNew) {
          deleteSelectedFavorite();
        } else {
          FavoriteNode node = (FavoriteNode)mFavoriteTree.getSelectionPath().getLastPathComponent();
          
          if(node.isDirectoryNode()) {
            mFavoriteTree.delete(node);
          } else {
            deleteSelectedFavorite();
          }
        }
      }
    });
    toolbarPn.add(mDeleteBt);
    
    msg = mLocalizer.msg("up", "Move the selected favorite up");
    icon = FavoritesPlugin.getInstance().getIconFromTheme("actions", "go-up", 22);
    mUpBt = UiUtilities.createToolBarButton(msg, icon);
    mUpBt.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        mFavoriteTree.moveSelectedFavorite(-1);
      }
    });

    if(!mShowNew) {
      addToolbarSeperator(toolbarPn);
      toolbarPn.add(mUpBt);
    }

    msg = mLocalizer.msg("down", "Move the selected favorite down");
    icon = FavoritesPlugin.getInstance().getIconFromTheme("actions", "go-down", 22);
    mDownBt = UiUtilities.createToolBarButton(msg, icon);
    mDownBt.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        mFavoriteTree.moveSelectedFavorite(1);
      }
    });

    if(!mShowNew) {
      toolbarPn.add(mDownBt);
    }

    msg = mLocalizer.msg("sort", "Sort favorites alphabetically");
    icon = FavoritesPlugin.getInstance().getIconFromTheme("actions", "sort-list", 22);
    final String titleAlpha = msg;
    mSortAlphaBt = UiUtilities.createToolBarButton(msg, icon);
    mSortAlphaBt.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        sortFavorites(FavoriteNodeComparator.getInstance(), titleAlpha);
      }
    });

    msg = mLocalizer.msg("sortCount", "Sort favorites by number of programs");
    icon = FavoritesPlugin.getInstance().getIconFromTheme("actions", "sort-list-numerical", 22);
    final String titleCount = msg;
    mSortCountBt = UiUtilities.createToolBarButton(msg, icon);
    mSortCountBt.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        sortFavorites(FavoriteNodeCountComparator.getInstance(), titleCount);
      }
    });

    if(!mShowNew) {
      toolbarPn.add(mSortAlphaBt);
      toolbarPn.add(mSortCountBt);
    }
    
    msg = mLocalizer.msg("send", "Send Programs to another Plugin");
    icon = FavoritesPlugin.getInstance().getIconFromTheme("actions", "edit-copy", 22);
    mSendBt = UiUtilities.createToolBarButton(msg, icon);
    mSendBt.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
         showSendDialog();
      }
    });

    addToolbarSeperator(toolbarPn);
    toolbarPn.add(mSendBt);

    msg = mLocalizer.msg("import", "Import favorites from TVgenial");
    icon = FavoritesPlugin.getInstance().getIconFromTheme("actions", "document-open", 22);
    mImportBt = UiUtilities.createToolBarButton(msg, icon);
    mImportBt.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        importFavorites();
      }
    });

    if(!mShowNew) {
      toolbarPn.add(mImportBt);
    }

    msg = mLocalizer.msg("settings","Open settings");
    icon = FavoritesPlugin.getInstance().getIconFromTheme("categories","preferences-system", 22);
    JButton settings = UiUtilities.createToolBarButton(msg, icon);

    settings.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          close();
          MainFrame.getInstance().showSettingsDialog(SettingsItem.FAVORITE);
        }
      });
    
    addToolbarSeperator(toolbarPn);
    toolbarPn.add(settings);

    mSplitPane = new JSplitPane();
    mSplitPane.setDividerLocation(splitPanePosition);
    mSplitPane.setContinuousLayout(true);
    
    main.add(mSplitPane, BorderLayout.CENTER);
    
    JScrollPane scrollPane;
    
    if(favoriteArr != null) {
      mFavoritesListModel = new DefaultListModel();
      mFavoritesListModel.ensureCapacity(favoriteArr.length);
      for (Favorite element : favoriteArr) {
        mFavoritesListModel.addElement(element);
      }
  
      mFavoritesList = new JList(mFavoritesListModel);
      mFavoritesList.setCellRenderer(new FavoriteListCellRenderer());
      ListSelectionModel selModel = mFavoritesList.getSelectionModel();
      selModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      selModel.addListSelectionListener(new ListSelectionListener() {
        public void valueChanged(ListSelectionEvent evt) {
          if(!evt.getValueIsAdjusting()) {
            favoriteSelectionChanged();
          }
        }
      });
  
      if(!mShowNew) {
        ListDragAndDropHandler dnDHandler = new ListDragAndDropHandler(mFavoritesList,mFavoritesList,this);
        new DragAndDropMouseListener(mFavoritesList,mFavoritesList,this,dnDHandler);
      }
  
      mFavoritesList.addMouseListener(new MouseAdapter() {
        @Override
        public void mousePressed(MouseEvent e) {
          if (e.isPopupTrigger()) {
            showFavoritesPopUp(e.getX(), e.getY());
          }
        }
  
        @Override
        public void mouseReleased(MouseEvent e) {
          if (e.isPopupTrigger()) {
            showFavoritesPopUp(e.getX(), e.getY());
          }
        }
  
        @Override
        public void mouseClicked(MouseEvent e) {
          if(SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 2) {
            editSelectedFavorite();
          }
        }
      });
      
      mFavoritesList.addKeyListener(new KeyAdapter() {
        public void keyPressed(KeyEvent e) {
          if (e.getKeyCode() == KeyEvent.VK_RIGHT && e.isControlDown()) {
            mProgramList.grabFocus();
            if (mProgramList.getSelectedIndex() == -1) {
              mProgramList.setSelectedIndex(0);
            }
          }
        }
      });
      
      scrollPane = new JScrollPane(mFavoritesList);
    }
    else {
      mFavoriteTree = new FavoriteTree();
      mFavoriteTree.addTreeSelectionListener(this);

      mFavoriteTree.addKeyListener(new KeyAdapter() {
        public void keyPressed(KeyEvent e) {
          if (e.getKeyCode() == KeyEvent.VK_RIGHT && e.isControlDown()) {
            mProgramList.grabFocus();
            if (mProgramList.getSelectedIndex() == -1) {
              mProgramList.setSelectedIndex(0);
            }
          }
        }
      });

      scrollPane = new JScrollPane(mFavoriteTree);

      mFavoritesList = null;
    }
      
    scrollPane.setBorder(null);
    mSplitPane.setLeftComponent(scrollPane);

    mProgramListModel = new DefaultListModel();
    mProgramList = new ProgramList(mProgramListModel, new ProgramPanelSettings(new PluginPictureSettings(PluginPictureSettings.ALL_PLUGINS_SETTINGS_TYPE), false, ProgramPanelSettings.X_AXIS));
    mProgramList.addMouseListeners(null);

    mProgramList.addKeyListener(new KeyAdapter() {
        public void keyPressed(KeyEvent e) {
          if (e.getKeyCode() == KeyEvent.VK_LEFT && e.isControlDown()) {
            if (mFavoritesList != null) {
              mFavoritesList.grabFocus();
              if (mFavoritesList.getSelectedIndex() == -1) {
                mFavoritesList.setSelectedIndex(0);
              }
            } else if (mFavoriteTree != null) {
              mFavoriteTree.grabFocus();
              if (mFavoriteTree.getSelectionCount() == 0) {
                mFavoriteTree.setSelectionRow(0);
              }
            }
          }
        }
      });

    mProgramScrollPane = new JScrollPane(mProgramList);
    mProgramScrollPane.setBorder(null);
    mSplitPane.setRightComponent(mProgramScrollPane);
    
    msg = mLocalizer.msg("showBlack", "Show single removed programs");
    mBlackListChb = new JCheckBox(msg);
    mBlackListChb.setSelected(FavoritesPlugin.getInstance().isShowingBlackListEntries());
    mBlackListChb.setOpaque(false);
    mBlackListChb.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        FavoritesPlugin.getInstance().setIsShowingBlackListEntries(mBlackListChb.isSelected());
        favoriteSelectionChanged();
      }
    });

    JPanel buttonPn = new JPanel(new BorderLayout());
    
    if(!mShowNew) {
      buttonPn.add(mBlackListChb, BorderLayout.WEST);
    }
    
    main.add(buttonPn, BorderLayout.SOUTH);

    mCloseBt = new JButton(Localizer.getLocalization(Localizer.I18N_CLOSE));
    mCloseBt.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        close();
      }
    });
    buttonPn.add(mCloseBt, BorderLayout.EAST);
    getRootPane().setDefaultButton(mCloseBt);

    setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
    addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        close();
      }
    });
    
    mFavoriteTree.setSelectionPath(new TreePath(mFavoriteTree.getRoot()));
    favoriteSelectionChanged();
  }
  
  private void addToolbarSeperator(JToolBar toolbarPn) {
    JPanel p = new JPanel();
    p.setSize(10,10);
    p.setMaximumSize(new Dimension(10,10));    
    toolbarPn.add(p);
    toolbarPn.addSeparator();
    
    p = new JPanel();
    p.setSize(4,10);
    p.setMaximumSize(new Dimension(4,10));    
    toolbarPn.add(p);
  }

  /**
   * Show the Popup-Menu
   * @param x X-Position for the popup
   * @param y Y-Position for the popup
   */
  protected void showFavoritesPopUp(int x, int y) {
    JPopupMenu menu = new JPopupMenu();

    mFavoritesList.setSelectedIndex(mFavoritesList.locationToIndex(new Point(x,y)));

    if (!mShowNew) {
      JMenuItem createNew = new JMenuItem(mLocalizer.msg("new", "Create a new favorite..."),
          FavoritesPlugin.getInstance().getIconFromTheme("actions", "document-new", 16));

      createNew.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          newFavorite(mFavoriteTree.getRoot());
        }
      });

      menu.add(createNew);
      menu.addSeparator();
    }

    JMenuItem edit = new JMenuItem(mLocalizer.msg("edit", "Edit the selected favorite..."),
        IconLoader.getInstance().getIconFromTheme("actions", "document-edit", 16));

    edit.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        editSelectedFavorite();
      }
    });

    menu.add(edit);

    JMenuItem delete = new JMenuItem(mLocalizer.msg("delete", "Delete selected favorite..."),
        IconLoader.getInstance().getIconFromTheme("actions", "edit-delete", 16));

    delete.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        deleteSelectedFavorite();
      }
    });

    menu.add(delete);
    menu.addSeparator();

    JMenuItem sendPrograms = new JMenuItem(mLocalizer.msg("send", "Send Programs to another Plugin"),
        FavoritesPlugin.getInstance().getIconFromTheme("actions", "edit-copy", 16));

    sendPrograms.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        showSendDialog();
      }
    });
    sendPrograms.setEnabled(mProgramListModel.size() > 0);

    menu.add(sendPrograms);

    menu.show(mFavoritesList, x, y);
  }

  public static ManageFavoritesDialog getInstance() {
    return mInstance;
  }

  public int getSplitpanePosition() {
    return mSplitPane.getDividerLocation();
  }

  public void setSplitpanePosition(int val) {
    mSplitPane.setDividerLocation(val);
  }
  
  /**
   * Refresh the program list.
   */
  public void favoriteSelectionChanged() {
    if(mFavoritesList != null) {
      int selection = mFavoritesList.getSelectedIndex();
      int size = mFavoritesListModel.getSize();

      mEditBt.setEnabled(selection != -1);
      mDeleteBt.setEnabled(selection != -1);

      mEditBt.setToolTipText(mLocalizer.msg("edit", "Edit the selected favorite..."));
      mDeleteBt.setToolTipText(mLocalizer.msg("delete", "Delete selected favorite..."));
      
      mUpBt.setEnabled(selection > 0);
      mDownBt.setEnabled((selection != -1) && (selection < (size - 1)));

      mSortAlphaBt.setEnabled(size >= 2);
      mSortCountBt.setEnabled(mSortAlphaBt.isEnabled());

      if (selection == -1) {
        mProgramListModel.clear();
        mSendBt.setEnabled(false);
      } else {
        changeProgramList((Favorite)mFavoritesList.getSelectedValue());
      }
    } 
    else {
      if(mFavoriteTree.getSelectionPath() != null) {
        Favorite fav = ((FavoriteNode)mFavoriteTree.getSelectionPath().getLastPathComponent()).getFavorite();
        
        if(fav != null) {
          enableButtons(true);
          changeProgramList(fav);
          mDeleteBt.setEnabled(true);
          mEditBt.setToolTipText(mLocalizer.msg("edit", "Edit the selected favorite..."));
          mDeleteBt.setToolTipText(mLocalizer.msg("delete", "Delete selected favorite..."));
        }
        else {
          Program[] p = ((FavoriteNode)mFavoriteTree.getSelectionPath().getLastPathComponent()).getAllPrograms(false);
          
          int firstNotExpiredIndex = -1;
          
          if(p != null && p.length > 0) {
            enableButtons(true);
            
            Arrays.sort(p,ProgramUtilities.getProgramComparator());
            
            mProgramListModel.clear();
            mProgramListModel.ensureCapacity(p.length);
            
            for (int i = 0; i < p.length; i++) {
              // don't list programs twice, if they are marked by different favorites
              if (! mProgramListModel.contains(p[i])) {
                mProgramListModel.addElement(p[i]);
                
                if(firstNotExpiredIndex == -1 && !p[i].isExpired()) {
                  firstNotExpiredIndex = mProgramListModel.size()-1;
                }
              }
            }
            scrollInProgramListToIndex(firstNotExpiredIndex);
            
            mSendBt.setEnabled(true);
            mDeleteBt.setEnabled(false);
          }
          else {
            mProgramListModel.clear();
          
            FavoriteNode node = (FavoriteNode)mFavoriteTree.getSelectionPath().getLastPathComponent();
          
            enableButtons(node.isDirectoryNode());
          
            mDeleteBt.setEnabled(node.isDirectoryNode() && node.getChildCount() < 1);
            mSendBt.setEnabled(false);
          }
          mEditBt.setToolTipText(mLocalizer.msg("renameFolder", "Rename selected folder..."));
          mDeleteBt.setToolTipText(mLocalizer.msg("deleteFolder", "Delete selected folder"));
        }
      }
      else {
        mProgramListModel.clear();
        mDeleteBt.setEnabled(false);
        mSendBt.setEnabled(false);
        enableButtons(false);          
      }
    }
  }
  
  private void scrollInProgramListToIndex(final int index) {
    if (index < 0) {
      return;
    }
    
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        mProgramScrollPane.getVerticalScrollBar().setValue(0);
        mProgramScrollPane.getHorizontalScrollBar().setValue(0);
        
        Rectangle cellBounds = mProgramList.getCellBounds(index,index);
        if (cellBounds != null) {
          cellBounds.setLocation(cellBounds.x, cellBounds.y + mProgramScrollPane.getHeight() - cellBounds.height);
          mProgramList.scrollRectToVisible(cellBounds);
        }
      }
    });
  }
  
  private void enableButtons(boolean enabled) {
    TreePath path = mFavoriteTree.getSelectionPath();
    
    mEditBt.setEnabled(enabled && path != null && !path.getLastPathComponent().equals(mFavoriteTree.getRoot()));

    mUpBt.setEnabled((enabled || (path != null && ((FavoriteNode)path.getLastPathComponent()).isDirectoryNode())) && !path.getLastPathComponent().equals(mFavoriteTree.getRoot()) && mFavoriteTree.getRowForPath(mFavoriteTree.getSelectionPath()) > 0 );
    mDownBt.setEnabled((enabled || (path != null && ((FavoriteNode)path.getLastPathComponent()).isDirectoryNode())) && !path.getLastPathComponent().equals(mFavoriteTree.getRoot()) && mFavoriteTree.getRowForPath(mFavoriteTree.getSelectionPath()) < mFavoriteTree.getRowCount() -1);

    if(path != null && !((FavoriteNode)path.getLastPathComponent()).isDirectoryNode() && path.getParentPath().getLastPathComponent().equals(mFavoriteTree.getRoot())) {
      path = path.getParentPath();
    }
    
    mSortAlphaBt.setEnabled((enabled && (path != null && ((FavoriteNode)path.getLastPathComponent()).isDirectoryNode() && ((FavoriteNode)path.getLastPathComponent()).getChildCount() > 1 || path.getLastPathComponent().equals(mFavoriteTree.getRoot()))) || path == null);
    mSortCountBt.setEnabled(mSortAlphaBt.isEnabled());
  }

  private void changeProgramList(Favorite fav) {
    Program[] programArr = mShowNew ? fav.getNewPrograms() : fav.getWhiteListPrograms();
    Program[] blackListPrograms = fav.getBlackListPrograms();
    
    mProgramListModel.clear();
    mProgramListModel.ensureCapacity(mShowNew ? programArr.length : programArr.length + blackListPrograms.length);
    
    int firstNotExpiredIndex = -1;
    
    for (int i = 0; i < programArr.length; i++) {
      mProgramListModel.addElement(programArr[i]);
      
      if(firstNotExpiredIndex == -1 && !programArr[i].isExpired()) {
        firstNotExpiredIndex = i;
      }
    }
    
    mSendBt.setEnabled(mProgramListModel.size() > 0);
    
    if(!mShowNew && mBlackListChb.isSelected()) {
      for (int i = 0; i < blackListPrograms.length; i++) {
        mProgramListModel.addElement(blackListPrograms[i]);
        
        if(firstNotExpiredIndex == -1 && !blackListPrograms[i].isExpired()) {
          firstNotExpiredIndex = i;
        }
      }
    }
    
    scrollInProgramListToIndex(firstNotExpiredIndex);    
  }
  
  public void showSendDialog() {
    if(mFavoritesList != null) {
      int selection = mFavoritesList.getSelectedIndex();

      if(selection == -1) {
        return;
      }
    }
    else if(mFavoriteTree.getSelectionPath() == null) {
      return;
    }

    Program[] programs = mProgramList.getSelectedPrograms();

    Favorite fav;
    
    if(mFavoritesList != null) {
      fav = (Favorite) mFavoritesListModel.get(mFavoritesList.getSelectedIndex());
    }
    else if(programs == null) {
      programs = ((FavoriteNode)mFavoriteTree.getSelectionPath().getLastPathComponent()).getAllPrograms(true);
      
      if(programs.length < 1) {
        programs = ((FavoriteNode)mFavoriteTree.getSelectionPath().getLastPathComponent()).getAllPrograms(false);
      }
      
      fav = null;
    }
    else {
      fav = null;
    }
    
    if (fav != null && (programs == null || programs.length == 0)) {
      programs = mShowNew ? fav.getNewPrograms() : fav.getWhiteListPrograms();
    }
    
    SendToPluginDialog send = new SendToPluginDialog(null, this, programs);

    send.setVisible(true);
  }


  protected void newFavorite(FavoriteNode parent) {
    Favorite favorite;
    if (FavoritesPlugin.getInstance().isUsingExpertMode()) {
      favorite = new AdvancedFavorite("");
      EditFavoriteDialog dlg = new EditFavoriteDialog(this, favorite);
      UiUtilities.centerAndShow(dlg);
      
      if (!dlg.getOkWasPressed()) {
        favorite = null;
      }

    } else {
      WizardHandler handler = new WizardHandler(this, new TypeWizardStep(null,parent));
      favorite = (tvbrowser.extras.favoritesplugin.core.Favorite)handler.show();
    }
    
    // in case of AdvancedFavorite: search not necessary (because already done)
    addFavorite(favorite, !(favorite instanceof AdvancedFavorite), parent);
  }

  public void addFavorite(Favorite fav, boolean update, FavoriteNode parent) {
    FavoriteNode newNode = null; 
    if (fav != null) {
      try {
        if (update) {
          fav.updatePrograms();
        }
        if(mFavoritesList != null) {
          mFavoritesListModel.addElement(fav);
          int idx = mFavoritesListModel.size() - 1;
          mFavoritesList.setSelectedIndex(idx);
          mFavoritesList.ensureIndexIsVisible(idx);
        }
        newNode = FavoriteTreeModel.getInstance().addFavorite(fav, parent);
      } catch (TvBrowserException e) {
        ErrorHandler.handle("Creating favorites failed.", e);
      }
    }

    if (newNode != null) {
      if (parent != null) {
        mFavoriteTree.reload(parent);
      }
      TreePath path = new TreePath(newNode.getPath());
      mFavoriteTree.scrollPathToVisible(path);
      mFavoriteTree.setSelectionPath(path);
      favoriteSelectionChanged();
    }
  }

  public void addFavorite(Favorite fav, Object dummy) {
    mFavoritesListModel.addElement(fav);
  }

  protected void editSelectedFavorite() {
    Favorite fav = null;
    FavoriteNode node = null;
    if(mFavoritesList != null) {
      fav = (Favorite) mFavoritesList.getSelectedValue();
    }
    else {
      if (mFavoriteTree.getSelectionCount() > 0) {
        node = (FavoriteNode)mFavoriteTree.getSelectionPath().getLastPathComponent();
        fav = node.getFavorite();
      }
    }

    if (fav != null) {
        EditFavoriteDialog dlg = new EditFavoriteDialog(this, fav);
        UiUtilities.centerAndShow(dlg);
        if (dlg.getOkWasPressed()) {
          if(mFavoritesList != null) {
            mFavoritesList.repaint();
          }
          favoriteSelectionChanged();
          FavoritesPlugin.getInstance().updateRootNode(true);
        }

        if (node != null) {
          mFavoriteTree.reload(node);
          mFavoriteTree.repaint();
        }
    }

  }



  protected void deleteSelectedFavorite() {
    int selection = -1;
    if(mFavoritesList != null) {
      selection = mFavoritesList.getSelectedIndex();
    }
    else {
      if(mFavoriteTree.getSelectionPath() != null && ((FavoriteNode)mFavoriteTree.getSelectionPath().getLastPathComponent()).containsFavorite()) {
        selection = 0;
      }
    }
    if (selection != -1) {
      Favorite fav;
      FavoriteNode parent = null;
      if(mFavoritesList != null) {
        fav = (Favorite) mFavoritesListModel.get(selection);
        mFavoritesListModel.remove(selection);
      }
      else {
        FavoriteNode node = ((FavoriteNode)mFavoriteTree.getSelectionPath().getLastPathComponent());
        fav = node.getFavorite();
        parent = (FavoriteNode) node.getParent();
      }

      if (JOptionPane.showConfirmDialog(this,
              FavoritesPlugin.mLocalizer.msg("reallyDelete", "Really delete favorite '{0}'?", fav.getName()),
              mLocalizer.msg("delete", "Delete selected favorite..."),
              JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {

        FavoriteTreeModel.getInstance().deleteFavorite(fav);

        if (parent != null) {
          mFavoriteTree.setSelectionPath(new TreePath(parent.getPath()));
          mFavoriteTree.reload(parent);
        }

        favoriteSelectionChanged();
      }
    }
  }

  protected void sortFavorites(Comparator<FavoriteNode> comp, String title) {         
    TreePath path = mFavoriteTree.getSelectionPath();

    if(path != null && !((FavoriteNode)path.getLastPathComponent()).isDirectoryNode() && path.getParentPath().getLastPathComponent().equals(mFavoriteTree.getRoot())) {
      path = path.getParentPath();
    }
    
    if(path == null) {
      path = new TreePath(mFavoriteTree.getRoot());
    }
    
    if(((FavoriteNode)path.getLastPathComponent()).isDirectoryNode()) {
      FavoriteTreeModel.getInstance().sort((FavoriteNode)path.getLastPathComponent(), true, comp, title);
      mFavoriteTree.reload((FavoriteNode)path.getLastPathComponent());
    }
  }


  protected void importFavorites() {
    JFileChooser fileChooser = new JFileChooser();
    String[] extArr = { ".txt" };
    String msg = mLocalizer.msg("importFile.TVgenial", "Text file (from TVgenial) (.txt)");
    fileChooser.setFileFilter(new ExtensionFileFilter(extArr, msg));
    if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {

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
              boolean alreadyKnown = false;
              Favorite[] favs = null;
              if (mFavoritesListModel != null) {
                favs = (Favorite[]) mFavoritesListModel.toArray();
              } else if (mFavoriteTree != null) {
                favs = FavoriteTreeModel.getInstance().getFavoriteArr();
              }
              if (favs != null) {
                for (Favorite favorite : favs) {
                  String favName = favorite.getName();
                  if (line.equalsIgnoreCase(favName)) {
                    alreadyKnown = true;
                    break;
                  }
                }
                // Import the favorite if it is new
                if (! alreadyKnown) {
                  line = line.replace(" *", " OR ").replace(" |", " OR ")
                      .replace(" ODER ", " OR ").replace(" +", " AND ")
                      .replace(" &", " AND ").replace(" UND ", " AND ")
                      .replace(" \\", " NOT ").replace(" NICHT ", " NOT ")
                      .replace("_", " ").trim();
                  while (line.indexOf("  ") >= 0) {
                    line = line.replace("  ", " ");
                  }
                  AdvancedFavorite fav = new AdvancedFavorite(line);
                  fav.updatePrograms();
                  if (mFavoritesListModel != null) {
                    mFavoritesListModel.addElement(fav);
                  }
                  else {
                    FavoriteTreeModel.getInstance().addFavorite(fav);
                  }
                  importedFavoritesCount++;
                }
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
            try { reader.close(); } catch (IOException exc) {
              // ignore
            }
          }
        }
  
        if (importedFavoritesCount == 0) {
          msg = mLocalizer.msg("error.2", "There are no new favorites in {0}.",
                               file.getAbsolutePath());
          JOptionPane.showMessageDialog(this, msg);
        } else {
          // Scroll to the end
          if (mFavoritesListModel != null) {
            mFavoritesList.ensureIndexIsVisible(mFavoritesListModel.size() - 1);
            // Select the first new favorite
            int firstNewIdx = mFavoritesListModel.size() - importedFavoritesCount;
            mFavoritesList.setSelectedIndex(firstNewIdx);
            mFavoritesList.ensureIndexIsVisible(firstNewIdx);
          }
          msg = mLocalizer.msg("importDone", "There were {0} new favorites imported.", importedFavoritesCount);
          JOptionPane.showMessageDialog(this, msg);
        }
      }
    }
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
      Component c = super.getListCellRendererComponent(list, value, index, isSelected,
          cellHasFocus);
      
      if (value instanceof Favorite && c instanceof JLabel) {
        Favorite fav = (Favorite)value;
        ((JLabel)c).setText(fav.getName() + " (" + (mShowNew ? fav.getNewPrograms().length : fav.getWhiteListPrograms().length) + ")");
        
        if(mShowNew && fav.getNewPrograms().length > 0 && !isSelected) {
          c.setForeground(Color.red);
        }
      }
      return c;
    } 
  }

  /*
   * (non-Javadoc)
   * @see util.ui.WindowClosingIf#close()
   */
  public void close() {
    mInstance = null;

    if (mFavoriteTree != null) {
      mFavoriteTree.removeTreeSelectionListener(this);
    }

    dispose();
  }

  /*
   * (non-Javadoc)
   * @see util.ui.ListDropAction#drop(javax.swing.JList, javax.swing.JList, int, boolean)
   */
  public void drop(JList source, JList target, int rows, boolean move) {
    UiUtilities.moveSelectedItems(target,rows,true);
  }

  public void valueChanged(TreeSelectionEvent e) {
    favoriteSelectionChanged();
  }
  
  protected boolean programListIsEmpty() {
    return mProgramListModel.isEmpty();
  }
  
  /**
   * Gets if this dialog shows the new found programs after data update.
   * @return <code>True</code> if this dialog shows the new found programs after data update.
   */
  public boolean isShowingNewFoundPrograms() {
    return mShowNew;
  }
}

