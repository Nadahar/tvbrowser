/*
 * TV-Browser
 * Copyright (C) 2012 TV-Browser team (dev@tvbrowser.org)
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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Hashtable;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JEditorPane;
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
import javax.swing.UIManager;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;

import com.jgoodies.forms.factories.Borders;

import devplugin.Channel;
import devplugin.Date;
import devplugin.Program;
import devplugin.ProgramFilter;
import devplugin.SettingsItem;
import tvbrowser.core.icontheme.IconLoader;
import tvbrowser.extras.favoritesplugin.FavoritesPlugin;
import tvbrowser.extras.favoritesplugin.core.AdvancedFavorite;
import tvbrowser.extras.favoritesplugin.core.Favorite;
import tvbrowser.extras.favoritesplugin.core.FilterFavorite;
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
import util.ui.FilterableProgramListPanel;
import util.ui.ListDragAndDropHandler;
import util.ui.ListDropAction;
import util.ui.Localizer;
import util.ui.ProgramList;
import util.ui.SendToPluginDialog;
import util.ui.TVBrowserIcons;
import util.ui.TabListenerPanel;
import util.ui.UiUtilities;
import util.ui.persona.Persona;
import util.ui.persona.PersonaListener;

/**
 * A panel for managing of the TV-Browser favorites.
 * 
 * @author René Mach
 */
public class ManageFavoritesPanel extends TabListenerPanel implements ListDropAction<Favorite>, TreeSelectionListener, PersonaListener {
  public static final int FILTER_START_LAST_TYPE = -1;
  
  private static final int MAX_SHOWN_PROGRAMS = 6000;
  private static final Localizer mLocalizer = ManageFavoritesDialog.mLocalizer;
  private DefaultListModel<Favorite> mFavoritesListModel;
  private JList<Favorite> mFavoritesList;
  private FavoriteTree mFavoriteTree;
  private ProgramList mProgramList;
  private JSplitPane mSplitPane;
  private JButton mNewBt, mEditBt, mDeleteBt, mUpBt, mDownBt, mSortAlphaBt, mSortCountBt, mImportBt, mSendBt;
  private JButton mCloseBt;

  private boolean mShowNew = false;
  private JCheckBox mBlackListChb;
  
  private FilterableProgramListPanel mProgramListPanel;
  
  private JButton mScrollToPreviousDay, mScrollToNextDay, mScrollToFirstNotExpired;
  
  public ManageFavoritesPanel(Favorite[] favoriteArr,
      int splitPanePosition, boolean showNew, Favorite initialSelection, boolean border) {
    init(favoriteArr, splitPanePosition, showNew, initialSelection,border);
  }
  
  private void init(Favorite[] favoriteArr, int splitPanePosition, boolean showNew, Favorite initialSelection, boolean border) {try {
    mShowNew = showNew;

    String msg;
    Icon icon;
    
    setLayout(new BorderLayout(5, 5));
    
    if(border) {
      setBorder(Borders.DLU4);
    }
    
    setOpaque(false);

    JToolBar toolbarPn = new JToolBar() {
	  protected void paintComponent(Graphics g) {
	    if(!UiUtilities.isGTKLookAndFeel() || Persona.getInstance().getHeaderImage() == null) {
	      super.paintComponent(g);
	    }
	  }
    };
    toolbarPn.setFloatable(false);
    toolbarPn.setOpaque(false);
    toolbarPn.setBorder(BorderFactory.createEmptyBorder());
    
    if(mShowNew) {
      JEditorPane info =  UiUtilities.createHtmlHelpTextArea(FavoritesPlugin.mLocalizer.msg("newPrograms.description","After updating TV listings, programs matching your favorites were found.\nSelect a favorite to view the new programs."));
      
      JPanel northPanel = new JPanel(new BorderLayout(0,5));
      northPanel.add(info, BorderLayout.NORTH);
      northPanel.add(toolbarPn, BorderLayout.SOUTH);
      
      add(northPanel, BorderLayout.NORTH);
    }
    else {
      add(toolbarPn, BorderLayout.NORTH);
    }

    if(!mShowNew) {
      if(favoriteArr == null) {
        JButton newFolder = UiUtilities.createToolBarButton(mLocalizer.msg("newFolder", "New folder"),
            IconLoader.getInstance().getIconFromTheme("actions", "folder-new", 22));
        newFolder.setOpaque(false);
        newFolder.addActionListener(e -> {
          TreePath path = mFavoriteTree.getSelectionPath();
          
          if(path != null) {
            FavoritesPlugin.getInstance().newFolder((FavoriteNode)path.getLastPathComponent());
          } else {
            FavoritesPlugin.getInstance().newFolder(mFavoriteTree.getRoot());
          }
        });

        toolbarPn.add(newFolder);
      }

      addToolbarSeperator(toolbarPn);

      msg = mLocalizer.ellipsisMsg("new", "Create a new favorite");
      icon = TVBrowserIcons.newIcon(TVBrowserIcons.SIZE_LARGE);
      mNewBt = UiUtilities.createToolBarButton(msg, icon);
      mNewBt.setOpaque(false);
      mNewBt.addActionListener(e -> {
        TreePath path = mFavoriteTree.getSelectionPath();

        if(path == null) {
          newFavorite(mFavoriteTree.getRoot());
        } else {
          FavoriteNode node = (FavoriteNode)path.getLastPathComponent();

          newFavorite(node.isDirectoryNode() ? node : (FavoriteNode)node.getParent());
        }
      });

      toolbarPn.add(mNewBt);
    }

    msg = mLocalizer.ellipsisMsg("edit", "Edit the selected favorite");
    icon = TVBrowserIcons.edit(TVBrowserIcons.SIZE_LARGE);
    mEditBt = UiUtilities.createToolBarButton(msg, icon);
    mEditBt.setOpaque(false);
    mEditBt.addActionListener(e -> {
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
    });
    toolbarPn.add(mEditBt);

    msg = mLocalizer.ellipsisMsg("delete", "Delete selected favorite");
    icon = TVBrowserIcons.delete(TVBrowserIcons.SIZE_LARGE);
    mDeleteBt = UiUtilities.createToolBarButton(msg, icon);
    mDeleteBt.setOpaque(false);
    mDeleteBt.addActionListener(e -> {
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
    });
    toolbarPn.add(mDeleteBt);

    msg = mLocalizer.msg("up", "Move the selected favorite up");
    icon = TVBrowserIcons.up(TVBrowserIcons.SIZE_LARGE);
    mUpBt = UiUtilities.createToolBarButton(msg, icon);
    mUpBt.setOpaque(false);
    mUpBt.addActionListener(e -> {
      mFavoriteTree.moveSelectedFavorite(-1);
    });

    if(!mShowNew) {
      addToolbarSeperator(toolbarPn);
      toolbarPn.add(mUpBt);
    }

    msg = mLocalizer.msg("down", "Move the selected favorite down");
    icon = TVBrowserIcons.down(TVBrowserIcons.SIZE_LARGE);
    mDownBt = UiUtilities.createToolBarButton(msg, icon);
    mDownBt.setOpaque(false);
    mDownBt.addActionListener(e -> {
      mFavoriteTree.moveSelectedFavorite(1);
    });

    if(!mShowNew) {
      toolbarPn.add(mDownBt);
    }

    msg = mLocalizer.msg("sort", "Sort favorites alphabetically");
    icon = FavoritesPlugin.getIconFromTheme("actions", "sort-list", 22);
    final String titleAlpha = msg;
    mSortAlphaBt = UiUtilities.createToolBarButton(msg, icon);
    mSortAlphaBt.setOpaque(false);
    mSortAlphaBt.addActionListener(e -> {
      sortFavorites(FavoriteNodeComparator.getInstance(), titleAlpha);
    });

    msg = mLocalizer.msg("sortCount", "Sort favorites by number of programs");
    icon = FavoritesPlugin.getIconFromTheme("actions", "sort-list-numerical", 22);
    final String titleCount = msg;
    mSortCountBt = UiUtilities.createToolBarButton(msg, icon);
    mSortCountBt.setOpaque(false);
    mSortCountBt.addActionListener(e -> {
      sortFavorites(FavoriteNodeCountComparator.getInstance(), titleCount);
    });

    if(!mShowNew) {
      toolbarPn.add(mSortAlphaBt);
      toolbarPn.add(mSortCountBt);
    }

    msg = mLocalizer.msg("send", "Send Programs to another Plugin");
    icon = TVBrowserIcons.copy(TVBrowserIcons.SIZE_LARGE);
    mSendBt = UiUtilities.createToolBarButton(msg, icon);
    mSendBt.setOpaque(false);
    mSendBt.addActionListener(e -> {
       showSendDialog();
    });

    addToolbarSeperator(toolbarPn);
    toolbarPn.add(mSendBt);

    msg = mLocalizer.msg("import", "Import favorites from TVgenial");
    icon = FavoritesPlugin.getIconFromTheme("actions", "document-open", 22);
    mImportBt = UiUtilities.createToolBarButton(msg, icon);
    mImportBt.setOpaque(false);
    mImportBt.addActionListener(e -> {
      importFavorites();
    });

    if(!mShowNew) {
      toolbarPn.add(mImportBt);
    }

    msg = mLocalizer.msg("settings","Open settings");
    icon = TVBrowserIcons.preferences(TVBrowserIcons.SIZE_LARGE);
    
    if(ManageFavoritesDialog.getInstance() != null) {
      JButton settings = UiUtilities.createToolBarButton(msg, icon);
  
      settings.addActionListener(e -> {
          if(ManageFavoritesDialog.getInstance() != null && ManageFavoritesDialog.getInstance().isVisible()) {
            ManageFavoritesDialog.getInstance().close();
          }

          MainFrame.getInstance().showSettingsDialog(SettingsItem.FAVORITE);
        });
  
      addToolbarSeperator(toolbarPn);
      toolbarPn.add(settings);
    }
    
    toolbarPn.add(Box.createGlue());
    
    mScrollToFirstNotExpired = UiUtilities.createToolBarButton(mLocalizer.msg("scrollToFirstNotExpired", "Scroll to first not expired program."),TVBrowserIcons.scrollToNow(TVBrowserIcons.SIZE_LARGE));
    mScrollToFirstNotExpired.setOpaque(false);
    toolbarPn.add(mScrollToFirstNotExpired);
    mScrollToFirstNotExpired.addActionListener(e -> {
      scrollToFirstNotExpiredIndex(false);
    });
    
    toolbarPn.add(Box.createRigidArea(new Dimension(15,0)));
    
    mScrollToPreviousDay = UiUtilities.createToolBarButton(ProgramList.getPreviousActionTooltip(),TVBrowserIcons.left(TVBrowserIcons.SIZE_LARGE));
    mScrollToPreviousDay.setOpaque(false);
    toolbarPn.add(mScrollToPreviousDay);
    mScrollToPreviousDay.addActionListener(e -> {
      mProgramList.scrollToPreviousDayIfAvailable();
    });

    mScrollToNextDay = UiUtilities.createToolBarButton(ProgramList.getNextActionTooltip(),TVBrowserIcons.right(TVBrowserIcons.SIZE_LARGE));
    mScrollToNextDay.setOpaque(false);
    toolbarPn.add(mScrollToNextDay);
    mScrollToNextDay.addActionListener(e -> {
      mProgramList.scrollToNextDayIfAvailable();
    });   
    
    mSplitPane = new JSplitPane();
    
    for(int i = 0; i < mSplitPane.getComponentCount(); i++) {
      (mSplitPane.getComponent(i)).setBackground(new Color(0,0,0,0));
    }
    
    mSplitPane.setBorder(BorderFactory.createEmptyBorder());
    mSplitPane.setDividerLocation(splitPanePosition);
    mSplitPane.setContinuousLayout(true);
    mSplitPane.setOpaque(false);

    add(mSplitPane, BorderLayout.CENTER);

    JScrollPane scrollPane;

    if(favoriteArr != null) {
      mFavoritesListModel = new DefaultListModel<>();
      mFavoritesListModel.ensureCapacity(favoriteArr.length);
      for (Favorite element : favoriteArr) {
        if(element.getNewPrograms().length >= 0) {
          mFavoritesListModel.addElement(element);
        }
      }

      mFavoritesList = new JList<>(mFavoritesListModel);
      mFavoritesList.setCellRenderer(new FavoriteListCellRenderer());
      ListSelectionModel selModel = mFavoritesList.getSelectionModel();
      selModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      selModel.addListSelectionListener(evt -> {
        if(!evt.getValueIsAdjusting()) {
          favoriteSelectionChanged(true);
        }
      });

      if(!mShowNew) {
        ListDragAndDropHandler dnDHandler = new ListDragAndDropHandler(mFavoritesList,mFavoritesList,this);
        new DragAndDropMouseListener<Favorite>(mFavoritesList,mFavoritesList,this,dnDHandler);
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
    scrollPane.setMinimumSize(new Dimension(200,100));
    mSplitPane.setLeftComponent(scrollPane);
    
    if(FavoritesPlugin.getInstance().getFilterStartType() == FILTER_START_LAST_TYPE) {
      mProgramListPanel = new FilterableProgramListPanel(true, new Program[0], true, FavoritesPlugin.getInstance().showDateSeparators(), new ProgramPanelSettings(new PluginPictureSettings(PluginPictureSettings.ALL_PLUGINS_SETTINGS_TYPE), false, ProgramPanelSettings.X_AXIS),FavoritesPlugin.getInstance().getLastSelectedProgramFilter());
    }
    else {
      mProgramListPanel = new FilterableProgramListPanel(FilterableProgramListPanel.TYPE_NAME_AND_PROGRAM_FILTER, new Program[0], true, FavoritesPlugin.getInstance().showDateSeparators(), new ProgramPanelSettings(new PluginPictureSettings(PluginPictureSettings.ALL_PLUGINS_SETTINGS_TYPE), false, ProgramPanelSettings.X_AXIS),FavoritesPlugin.getInstance().getFilterStartType());
    }
    
    mProgramListPanel.setBorder(Borders.DLU2);
    
    mProgramList = mProgramListPanel.getProgramList();
    setDefaultFocusOwner(mProgramList);
    mProgramList.addMouseAndKeyListeners(null);

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
    
    mSplitPane.setRightComponent(mProgramListPanel);

    msg = mLocalizer.msg("showBlack", "Show single removed programs");
    mBlackListChb = new JCheckBox(msg);
    mBlackListChb.setOpaque(false);
    mBlackListChb.setSelected(FavoritesPlugin.getInstance().isShowingBlackListEntries());
    mBlackListChb.setOpaque(false);
    mBlackListChb.addActionListener(e -> {
      FavoritesPlugin.getInstance().setIsShowingBlackListEntries(mBlackListChb.isSelected());
      favoriteSelectionChanged();
    });

    JPanel buttonPn = new JPanel(new BorderLayout());
    buttonPn.setOpaque(false);

    if(!mShowNew) {
      buttonPn.add(mBlackListChb, BorderLayout.WEST);
    }

    add(buttonPn, BorderLayout.SOUTH);

    mCloseBt = new JButton(Localizer.getLocalization(Localizer.I18N_CLOSE));
    
    if(ManageFavoritesDialog.getInstance() != null) {
      mCloseBt.addActionListener(e -> {
        if(ManageFavoritesDialog.getInstance() != null && ManageFavoritesDialog.getInstance().isVisible()) {
          ManageFavoritesDialog.getInstance().close();
        }
      });
      buttonPn.add(mCloseBt, BorderLayout.EAST);
      ManageFavoritesDialog.getInstance().getRootPane().setDefaultButton(mCloseBt);
    }

    if(mFavoriteTree != null) {
      FavoriteNode initialNode = mFavoriteTree.getRoot();

      if (initialSelection != null) {
        initialNode = mFavoriteTree.findFavorite(initialSelection);
      }
      TreePath treePath = new TreePath(initialNode.getPath());
      mFavoriteTree.setSelectionPath(treePath);
      mFavoriteTree.scrollPathToVisible(treePath);
    }

    favoriteSelectionChanged(true);}catch(Throwable t) {t.printStackTrace();}
  }

  private void addToolbarSeperator(JToolBar toolbarPn) {
    JPanel p = new JPanel();
    p.setOpaque(false);
    p.setSize(10,10);
    p.setMaximumSize(new Dimension(10,10));
    toolbarPn.add(p);
    toolbarPn.addSeparator();

    p = new JPanel();
    p.setOpaque(false);
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
      JMenuItem createNew = new JMenuItem(mLocalizer.ellipsisMsg("new", "Create a new favorite"),
          TVBrowserIcons.newIcon(TVBrowserIcons.SIZE_SMALL));

      createNew.addActionListener(e -> {
        newFavorite(mFavoriteTree.getRoot());
      });

      menu.add(createNew);
      menu.addSeparator();
    }

    JMenuItem edit = new JMenuItem(mLocalizer.ellipsisMsg("edit", "Edit the selected favorite"),
        TVBrowserIcons.edit(TVBrowserIcons.SIZE_SMALL));

    edit.addActionListener(e -> {
      editSelectedFavorite();
    });

    menu.add(edit);

    JMenuItem delete = new JMenuItem(mLocalizer.ellipsisMsg("delete", "Delete selected favorite"),
        TVBrowserIcons.delete(TVBrowserIcons.SIZE_SMALL));

    delete.addActionListener(e -> {
      deleteSelectedFavorite();
    });

    menu.add(delete);
    menu.addSeparator();

    JMenuItem sendPrograms = new JMenuItem(mLocalizer.msg("send", "Send Programs to another Plugin"),
        TVBrowserIcons.copy(TVBrowserIcons.SIZE_SMALL));

    sendPrograms.addActionListener(e -> {
      showSendDialog();
    });
    
    sendPrograms.setEnabled(mProgramList.getModel().getSize() > 0);

    menu.add(sendPrograms);

    menu.show(mFavoritesList, x, y);
  }


  public int getSplitpanePosition() {
    return mSplitPane.getDividerLocation();
  }

  public void setSplitpanePosition(int val) {
    mSplitPane.setDividerLocation(val);
  }

  /**
   * Refresh the program list.
   * @param scrollToFirst <code>true</code> if it should be scrolled to first index, <code>false</code> if not.
   */
  public synchronized void favoriteSelectionChanged(final boolean scrollToFirst) {
    if(mFavoritesList != null) {
      int selection = mFavoritesList.getSelectedIndex();
      int size = mFavoritesListModel.getSize();

      mEditBt.setEnabled(selection != -1);
      mDeleteBt.setEnabled(selection != -1);

      mEditBt.setToolTipText(mLocalizer.ellipsisMsg("edit", "Edit the selected favorite"));
      mDeleteBt.setToolTipText(mLocalizer.ellipsisMsg("delete", "Delete selected favorite"));

      mUpBt.setEnabled(selection > 0);
      mDownBt.setEnabled((selection != -1) && (selection < (size - 1)));

      mSortAlphaBt.setEnabled(size >= 2);
      mSortCountBt.setEnabled(mSortAlphaBt.isEnabled());

      if (selection == -1) {
        mProgramListPanel.clearPrograms();
        mSendBt.setEnabled(false);
      } else {
        changeProgramList((Favorite)mFavoritesList.getSelectedValue(),scrollToFirst);
      }
      
      Rectangle rect = mFavoritesList.getCellBounds(selection, selection);
      
      if(rect != null) {
        mFavoritesList.paintImmediately(rect);
      }
    }
    else {
      if(mFavoriteTree != null && mFavoriteTree.getSelectionPath() != null) {
        Favorite fav = ((FavoriteNode)mFavoriteTree.getSelectionPath().getLastPathComponent()).getFavorite();

        if(fav != null) {
          enableButtons(true);
          changeProgramList(fav,scrollToFirst);
          mDeleteBt.setEnabled(true);
          mEditBt.setToolTipText(mLocalizer.ellipsisMsg("edit", "Edit the selected favorite"));
          mDeleteBt.setToolTipText(mLocalizer.ellipsisMsg("delete", "Delete selected favorite"));
        }
        else {
          Program[] p = ((FavoriteNode)mFavoriteTree.getSelectionPath().getLastPathComponent()).getAllPrograms(false);

          int firstNotExpiredIndex = -1;
          
          if(p != null && p.length > 0) {
            enableButtons(true);

            Arrays.sort(p,ProgramUtilities.getProgramComparator());

            int maxSize = Math.min(p.length,MAX_SHOWN_PROGRAMS);
            
            ArrayList<Program> programs = new ArrayList<Program>(maxSize);
            
            Hashtable<Channel,ArrayList<Program>> test = new Hashtable<Channel,ArrayList<Program>>();
            
            int i = 0;
            
            while (i < p.length && programs.size() < MAX_SHOWN_PROGRAMS) {
              // don't list programs twice, if they are marked by different favorites
              ArrayList<Program> testList = test.get(p[i].getChannel());
              if(testList == null) {
                testList = new ArrayList<Program>();
                test.put(p[i].getChannel(), testList);
              }
              
              if (!testList.contains(p[i])) {
                testList.add(p[i]);
                programs.add(p[i]);

                if(firstNotExpiredIndex == -1 && !p[i].isExpired()) {
                  firstNotExpiredIndex = programs.size()-1;
                }
              }
              
              i++;
            }
            
            mProgramListPanel.setPrograms(programs.toArray(new Program[programs.size()]));
            
            if (scrollToFirst) {
              scrollInProgramListToIndex(firstNotExpiredIndex);
            }

            mSendBt.setEnabled(true);
            mScrollToPreviousDay.setEnabled(true);
            mScrollToNextDay.setEnabled(true);
            mDeleteBt.setEnabled(false);
          }
          else {
            mProgramListPanel.clearPrograms();

            FavoriteNode node = (FavoriteNode)mFavoriteTree.getSelectionPath().getLastPathComponent();

            enableButtons(node.isDirectoryNode());

            mDeleteBt.setEnabled(node.isDirectoryNode() && node.getChildCount() < 1);
            mSendBt.setEnabled(false);
            mScrollToPreviousDay.setEnabled(false);
            mScrollToNextDay.setEnabled(false);
          }
          mEditBt.setToolTipText(mLocalizer.ellipsisMsg("renameFolder", "Rename selected folder"));
          mDeleteBt.setToolTipText(mLocalizer.msg("deleteFolder", "Delete selected folder"));
        }
      }
      else {
        if(mProgramListPanel != null) {
          mProgramListPanel.clearPrograms();
          mDeleteBt.setEnabled(false);
          mSendBt.setEnabled(false);
          enableButtons(false);
        }
      }
    }
  }

  public void scrollToFirstNotExpiredIndex(boolean check) {
    mProgramListPanel.scrollToFirstNotExpiredIndex(check);
  }
  
  public void scrollInProgramListToIndex(final int index) {
    mProgramListPanel.scrollToIndexWithoutDateSeparators(index);
  }

  private void enableButtons(boolean enabled) {
    TreePath path = mFavoriteTree.getSelectionPath();

    mEditBt.setEnabled(enabled && path != null && !path.getLastPathComponent().equals(mFavoriteTree.getRoot()));

    mUpBt.setEnabled((enabled || (path != null && ((FavoriteNode)path.getLastPathComponent()).isDirectoryNode())) && !path.getLastPathComponent().equals(mFavoriteTree.getRoot()) && mFavoriteTree.getRowForPath(mFavoriteTree.getSelectionPath()) > 0 );
    mDownBt.setEnabled((enabled || (path != null && ((FavoriteNode)path.getLastPathComponent()).isDirectoryNode())) && !path.getLastPathComponent().equals(mFavoriteTree.getRoot()) && mFavoriteTree.getRowForPath(mFavoriteTree.getSelectionPath()) < mFavoriteTree.getRowCount() -1);

    if(path != null && !((FavoriteNode)path.getLastPathComponent()).isDirectoryNode() && path.getParentPath().getLastPathComponent().equals(mFavoriteTree.getRoot())) {
      path = path.getParentPath();
    }

    mSortAlphaBt.setEnabled(path == null || (enabled && (path != null && ((FavoriteNode)path.getLastPathComponent()).isDirectoryNode() && ((FavoriteNode)path.getLastPathComponent()).getChildCount() > 1 || path.getLastPathComponent().equals(mFavoriteTree.getRoot()))));
    mSortCountBt.setEnabled(mSortAlphaBt.isEnabled());
  }

  private void changeProgramList(Favorite fav, boolean scrollToFirstIndex) {
    Program[] programArr = mShowNew ? fav.getNewPrograms() : fav.getWhiteListPrograms();
    Program[] blackListPrograms = fav.getBlackListPrograms();

    Program[] programs = programArr;
    
    if(!mShowNew && mBlackListChb.isSelected()) {
      programs = new Program[programArr.length + blackListPrograms.length];
      
      System.arraycopy(programArr, 0, programs, 0, programArr.length);
      System.arraycopy(blackListPrograms, 0, programs, programArr.length, blackListPrograms.length);
    }
    
    mProgramListPanel.setPrograms(programs);
    
    mSendBt.setEnabled(mProgramList.getModel().getSize() > 0);
    mScrollToPreviousDay.setEnabled(mSendBt.isEnabled());
    mScrollToNextDay.setEnabled(mSendBt.isEnabled());
    
    if(scrollToFirstIndex) {
      scrollToFirstNotExpiredIndex(false);
    }
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

    SendToPluginDialog send = new SendToPluginDialog(null, (ManageFavoritesDialog.getInstance() != null && ManageFavoritesDialog.getInstance().isVisible()) ? (Window)ManageFavoritesDialog.getInstance() : MainFrame.getInstance(), programs);

    send.setVisible(true);
  }


  public void newFavorite(FavoriteNode parent) {
    Favorite favorite;
    if (FavoritesPlugin.getInstance().isUsingExpertMode()) {
      if(FavoritesPlugin.getInstance().showTypeSelection() && JOptionPane.showConfirmDialog(UiUtilities.getLastModalChildOf(MainFrame.getInstance()), FavoritesPlugin.mLocalizer.msg("askType.message", "Create a filter favorite?"), FavoritesPlugin.mLocalizer.msg("askType.title", "Type selection"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
        favorite = new FilterFavorite();
      }
      else {
        favorite = new AdvancedFavorite("");
      }
      
      EditFavoriteDialog dlg = new EditFavoriteDialog((ManageFavoritesDialog.getInstance() != null && ManageFavoritesDialog.getInstance().isVisible()) ? (Window)ManageFavoritesDialog.getInstance() : MainFrame.getInstance(), favorite);
      UiUtilities.centerAndShow(dlg);
      
      if (!dlg.getOkWasPressed()) {
        favorite = null;
      }

    } else {
      WizardHandler handler = new WizardHandler((ManageFavoritesDialog.getInstance() != null && ManageFavoritesDialog.getInstance().isVisible()) ? (Window)ManageFavoritesDialog.getInstance() : MainFrame.getInstance(), new TypeWizardStep(null,parent));
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
    if(mFavoritesListModel != null) {
      mFavoritesListModel.addElement(fav);
    }
  }
  
  public void reload() {
    reload(false);
  }
  
  public void reload(boolean keepPath) {
    if(mFavoriteTree != null) {
      final TreePath path = mFavoriteTree.getSelectionPath();
      mFavoriteTree.reload(mFavoriteTree.getRoot());
      
      if(path != null && keepPath) {
        mFavoriteTree.setSelectionPath(path);
      }
    }
  }
  
  public int getSelectedProgramIndex() {
    return mProgramList.getSelectedIndex();
  }

  public void editSelectedFavorite() {
    Favorite fav = null;
    FavoriteNode node = null;
    
    int index = mProgramList.getSelectedIndex();
    
    if(mFavoritesList != null) {
      fav = (Favorite) mFavoritesList.getSelectedValue();
      index = mFavoritesList.getSelectedIndex();
    }
    else {
      if (mFavoriteTree.getSelectionCount() > 0) {
        node = (FavoriteNode)mFavoriteTree.getSelectionPath().getLastPathComponent();
        fav = node.getFavorite();
      }
    }

    if (fav != null) {
        EditFavoriteDialog dlg = new EditFavoriteDialog((ManageFavoritesDialog.getInstance() != null && ManageFavoritesDialog.getInstance().isVisible()) ? (Window)ManageFavoritesDialog.getInstance() : MainFrame.getInstance(), fav);
        UiUtilities.centerAndShow(dlg);
        if (dlg.getOkWasPressed()) {
          if(mFavoritesList != null) {
            mFavoritesList.repaint();
          }
          //favoriteSelectionChanged();
          FavoritesPlugin.getInstance().updateRootNode(true);
        }

        if (node != null) {
          mFavoriteTree.reload(node);
          mFavoriteTree.repaint();
        }
        
        scrollInProgramListToIndex(index);
    }
  }

  public void deleteSelectedFavorite() {
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
      FavoriteTreeModel.getInstance().sort((FavoriteNode)path.getLastPathComponent(), comp, title);
      mFavoriteTree.reload((FavoriteNode)path.getLastPathComponent());
      FavoritesPlugin.getInstance().store();
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
        BufferedReader lineReader = null;
        try {
          reader = new FileReader(file);
          lineReader = new BufferedReader(reader);
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

    public Component getListCellRendererComponent(JList<?> list, Object value,
                                                  int index, boolean isSelected, boolean cellHasFocus)
    {
      Component c = super.getListCellRendererComponent(list, value, index, isSelected,
          cellHasFocus);
      
      if (value instanceof Favorite && c instanceof JLabel) {
        Favorite fav = (Favorite)value;
        ((JLabel)c).setText(fav.getName() + " (" + (mShowNew ? fav.getNewPrograms().length : fav.getWhiteListPrograms().length) + ")");
        
        if(!fav.isValidSearch()) {
          c.setForeground(Color.orange);
          ((JLabel)c).setText("<html><strike>"+((JLabel)c).getText()+"</strike></html>");
        }
        else if(mShowNew && fav.getNewPrograms().length > 0 && !isSelected) {
          c.setForeground(Color.red);
        }
      }
      return c;
    }
  }
  
  @Override
  public void drop(JList<Favorite> source, JList<Favorite> target, int rows, boolean move) {
    UiUtilities.moveSelectedItems(target,rows,true);
    FavoritesPlugin.getInstance().updateRootNode(true);
  }
  
  @Override
  public void valueChanged(TreeSelectionEvent e) {
    favoriteSelectionChanged(true);
  }
  
  public void close() {
    if (mFavoriteTree != null) {
      mFavoriteTree.removeTreeSelectionListener(this);
    }
  }
  
  public boolean programListIsEmpty() {
    return mProgramList.getModel().getSize() < 1;
  }

  /**
   * Gets if this dialog shows the new found programs after data update.
   * @return <code>True</code> if this dialog shows the new found programs after data update.
   */
  public boolean isShowingNewFoundPrograms() {
    return mShowNew;
  }

  public void favoriteSelectionChanged() {
    favoriteSelectionChanged(false);
  }
  
  public void handleFavoriteEvent() {
    SwingUtilities.invokeLater(() -> {
      mFavoriteTree.updateUI();
      favoriteSelectionChanged();
    });
  }
  
  public void newFolder(FavoriteNode parent, Window partenWindow) {
    mFavoriteTree.newFolder(parent,partenWindow);
  }

  @Override
  public void updatePersona() {
    if(Persona.getInstance().getHeaderImage() != null) {
      mBlackListChb.setForeground(Persona.getInstance().getTextColor());
    }
    else {
      mBlackListChb.setForeground(UIManager.getColor("Label.foreground"));
    }
  }
  
  public void setShowDateSeparators(boolean showDateSeparators) {
    mProgramListPanel.setShowDateSeparators(showDateSeparators);
  }
  
  public void registerPersonaListener() {
    Persona.getInstance().registerPersonaListener(mProgramListPanel);
    mProgramListPanel.updatePersona();
  }
  
  public void removePersonaListener() {
    Persona.getInstance().removePersonaListener(mProgramListPanel);
    mProgramListPanel.updatePersona();
  }
  
  public void scrollToDate(Date date) {
    mProgramList.scrollToNextDateIfAvailable(date);
  }
  
  public void scrollToNow() {
    mProgramListPanel.scrollToFirstNotExpiredIndex(false);
  }
  
  public void scrollToTime(int time, boolean scrollToNext) {
    if(scrollToNext) {
      mProgramList.scrollToFirstOccurrenceOfTimeFromCurrentViewOnwardIfAvailable(time);
    }
    else {
      mProgramList.scrollToTimeFromCurrentViewIfAvailable(time);
    }
  }
  
  public void selectFilter(ProgramFilter filter) {
    mProgramListPanel.selectFilter(filter);
  }
  
  public String getSelectedProgramFilterName() {
    return mProgramListPanel.getSelectedProgramFilterName();
  }
}
