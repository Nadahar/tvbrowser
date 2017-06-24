/*
 * RememberMe Plugin
 * Copyright (C) 2013 René Mach (rene@tvbrowser.org)
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
 */
package rememberme;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import javax.swing.AbstractAction;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ChangeListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import compat.BordersCompat;
import compat.PersonaCompat;
import compat.PersonaCompatListener;
import compat.UiCompat;
import compat.VersionCompat;
import devplugin.ActionMenu;
import devplugin.Channel;
import devplugin.Date;
import devplugin.Marker;
import devplugin.Plugin;
import devplugin.PluginAccess;
import devplugin.Program;
import devplugin.ProgramFieldType;
import devplugin.ProgramReceiveTarget;
import devplugin.TabListener;
import tvbrowser.extras.searchplugin.SearchPluginProxy;
import util.ui.TVBrowserIcons;
import util.ui.TabListenerPanel;
import util.ui.menu.MenuUtil;

public class RememberMeManagePanel extends TabListenerPanel implements PersonaCompatListener, TabListener {
  private static final int TYPE_ACTION_REMOVE = 1;
  private static final int TYPE_ACTION_UPDATE = 2;
  
  private RememberedProgramsList<RememberedProgram> mPrograms;
  private JList mList;
  private DefaultListModel mModel;
  private JComboBox mDayFilter;
  private JComboBox mTagFilter;
  private DayFilter mCurrentDayFilter;
  private TagFilter mCurrentTagFilter;
  private JLabel mFilterLabel;
  private JLabel mTagLabel;
  private JButton mUndo;
  private RememberedProgramsList<RememberedProgram> mUndoPrograms;
  private DefaultComboBoxModel mFilterModel;
  
  public RememberMeManagePanel(RememberedProgramsList<RememberedProgram> programs, final RememberMe rMe, final JButton close) {
    mPrograms = programs;
    mUndoPrograms = new RememberedProgramsList<RememberedProgram>();
    mModel = new DefaultListModel();
    
    mCurrentDayFilter = new DayFilter(RememberMe.mLocalizer.msg("all", "All available days"), 0, 0);
    mCurrentTagFilter = new TagFilter(null);
    
    mFilterModel = new DefaultComboBoxModel();
    
    mTagFilter = new JComboBox(rMe.getProgramReceiveTargets());
    mTagFilter.addItemListener(new ItemListener() {
      @Override
      public void itemStateChanged(ItemEvent e) {
        if(e.getStateChange() == ItemEvent.SELECTED) {
          ProgramReceiveTarget target = (ProgramReceiveTarget)e.getItem();
          
          if(mTagFilter.getSelectedIndex() == 0) {
            mCurrentTagFilter = new TagFilter(null);
          }
          else {
            mCurrentTagFilter = new TagFilter(target.getTargetName());
          }
          
          updatePanel(rMe);
        }
      }
    });
    mDayFilter = new JComboBox(mFilterModel);
    mDayFilter.addItemListener(new ItemListener() {
      @Override
      public void itemStateChanged(ItemEvent e) {
        if(e.getStateChange() == ItemEvent.SELECTED) {
          mCurrentDayFilter = (DayFilter)e.getItem();
          updatePanel(rMe);
        }
      }
    });
    
    updateFilters(rMe.getDayCount());
    
    mList = new JList(mModel);
    setDefaultFocusOwner(mList);
    UiCompat.addKeyRotation(mList);
    
    mList.addKeyListener(new KeyAdapter() {
      @Override
      public void keyPressed(KeyEvent e) {
        if(e.getKeyCode() == KeyEvent.VK_CONTEXT_MENU && e.getModifiersEx() == 0
            || e.getKeyCode() == KeyEvent.VK_R && e.getModifiersEx() == 0) {
          Point p = mList.indexToLocation(mList.getSelectedIndex());
          Rectangle r = mList.getCellBounds(mList.getSelectedIndex(), mList.getSelectedIndex());
          p.x += (int)(r.width * 0.2f);
          p.y += (int)(r.height * 2/3f);
          showContextMenu(e.getComponent(), p, rMe);
          e.consume();
        }
      }
    });
    
    mList.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseReleased(MouseEvent e) {
        showContextMenu(e, rMe);
      }
      
      @Override
      public void mousePressed(MouseEvent e) {
        if(SwingUtilities.isRightMouseButton(e)) {
          int index = mList.locationToIndex(e.getPoint());
          
          int[] values = mList.getSelectedIndices();
          
          boolean found = false;
          
          for(int value : values) {
            if(value == index) {
              found = true;
              break;
            }
          }
          
          if(!found) {
            mList.setSelectedIndex(index);
          }
        }
        
        showContextMenu(e, rMe);
      }
    });
    
    JPanel filterPanel = new JPanel(new FormLayout("default,3dlu,default:grow","default,3dlu,default"));
    filterPanel.setOpaque(false);
    
    mFilterLabel = new JLabel(RememberMe.mLocalizer.msg("filterDays", "Filter days:"));
    mTagLabel = new JLabel(RememberMe.mLocalizer.msg("filterTags", "Filter tags:"));
    
    filterPanel.add(mFilterLabel, CC.xy(1, 1));
    filterPanel.add(mTagLabel, CC.xy(1, 3));
    filterPanel.add(mDayFilter, CC.xy(3, 1));
    filterPanel.add(mTagFilter, CC.xy(3, 3));
    
    setLayout(new BorderLayout(0,5));
    add(filterPanel, BorderLayout.NORTH);
    add(new JScrollPane(mList), BorderLayout.CENTER);
    setBorder(BordersCompat.getDialogBorder());
    setOpaque(false);
    
    JButton remove = new JButton(TVBrowserIcons.delete(TVBrowserIcons.SIZE_SMALL));
    remove.setToolTipText(RememberMe.mLocalizer.msg("remove", "Remove from list"));
    remove.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        handleAction(TYPE_ACTION_REMOVE,rMe);
      }
    });
    
    mUndo = new JButton(rMe.createImageIcon("actions", "edit-undo", 16));
    mUndo.setToolTipText(RememberMe.mLocalizer.msg("undo", "Undo delete"));
    mUndo.setEnabled(false);
    mUndo.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        for(RememberedProgram prog : mUndoPrograms) {
          if(prog.isValid(rMe.getDayCount()) && !mPrograms.contains(prog)) {
            mPrograms.add(prog);
            
            prog.mark(rMe);
          }
        }
        
        synchronized (mPrograms) {
          Collections.sort(mPrograms);
        }
        
        updatePanel(rMe);
        
        mUndoPrograms.clear();
        mUndo.setEnabled(false);
      }
    });
    
    JPanel buttons = new JPanel(new FormLayout("default,5dlu,default,5dlu:grow,default","default"));
    buttons.setOpaque(false);
    buttons.add(remove, CC.xy(1, 1));
    buttons.add(mUndo, CC.xy(3, 1));
    
    if(close != null) {
      buttons.add(close, CC.xy(5, 1));
    }
    
    add(buttons, BorderLayout.SOUTH);
    
    updatePanel(rMe);
    
    if(VersionCompat.isCenterPanelSupported()) {
      updatePersona();
    }
    
    
  }
  
  private void showContextMenu(final Component c, final Point p, final RememberMe rMe) {
    final JPopupMenu popupMenu = new JPopupMenu();
    
    JMenuItem remove = new JMenuItem(RememberMe.mLocalizer.msg("remove", "Remove from list (cannot be undone)"),TVBrowserIcons.delete(TVBrowserIcons.SIZE_SMALL));
    remove.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        handleAction(TYPE_ACTION_REMOVE,rMe);
      }
    });
    
    popupMenu.add(remove);
    
    if(mList.getSelectedIndices().length == 1) {
      popupMenu.addSeparator();
      
      JMenuItem copy = new JMenuItem(RememberMe.mLocalizer.msg("copyTitle", "Copy title to clipboard"),TVBrowserIcons.copy(TVBrowserIcons.SIZE_SMALL));
      copy.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
          Toolkit toolkit = Toolkit.getDefaultToolkit();
          Clipboard clipboard = toolkit.getSystemClipboard();
          StringSelection strSel = new StringSelection(((RememberedProgram)mList.getSelectedValue()).getTitle());
          clipboard.setContents(strSel, null);
        }
      });
      
      popupMenu.add(copy);
      
      final String episodeTitle = ((RememberedProgram)mList.getSelectedValue()).getEpisodeTitle();
      
      if(episodeTitle != null) {
        JMenuItem copyEpisode = new JMenuItem(RememberMe.mLocalizer.msg("copyEpisodeTitle", "Copy episode title to clipboard"),TVBrowserIcons.copy(TVBrowserIcons.SIZE_SMALL));
        copyEpisode.addActionListener(new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent e) {
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            Clipboard clipboard = toolkit.getSystemClipboard();
            StringSelection strSel = new StringSelection(episodeTitle);
            clipboard.setContents(strSel, null);
          }
        });
        
        popupMenu.add(copyEpisode);
      }
      
      DummyProgram dummy = new DummyProgram((RememberedProgram)mList.getSelectedValue());
      
      ActionMenu search = SearchPluginProxy.getInstance().getContextMenuActions(dummy);
      
      if(search != null) {
        popupMenu.add(MenuUtil.createMenuItem(search));
      }
      
      PluginAccess access = Plugin.getPluginManager().getActivatedPluginForId("java.webplugin.WebPlugin");
      
      if(access != null) {
        ActionMenu iSearch = access.getContextMenuActions(dummy);
        
        if(iSearch != null) {
          popupMenu.add(MenuUtil.createMenuItem(iSearch));
        }
      }
    }
    
    final KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
    final KeyStroke stroke1 = KeyStroke.getKeyStroke(KeyEvent.VK_R, 0);
    final KeyStroke stroke2 = KeyStroke.getKeyStroke(KeyEvent.VK_CONTEXT_MENU, 0);
    
    popupMenu.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(stroke, "CLOSE_ON_ESCAPE_POPUP");
    popupMenu.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(stroke1, "CLOSE_ON_ESCAPE_POPUP");
    popupMenu.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(stroke2, "CLOSE_ON_ESCAPE_POPUP");
    popupMenu.getActionMap().put("CLOSE_ON_ESCAPE_POPUP", new AbstractAction() {
      @Override
      public void actionPerformed(ActionEvent e) {
        if(popupMenu != null && popupMenu.isVisible()) {
          popupMenu.setVisible(false);
        }
      }
    });
    
    popupMenu.addPopupMenuListener(new PopupMenuListener() {
      @Override
      public void popupMenuWillBecomeVisible(PopupMenuEvent e) {}
      
      @Override
      public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
        popupMenu.getInputMap().remove(stroke);
        popupMenu.getInputMap().remove(stroke1);
        popupMenu.getInputMap().remove(stroke2);
        popupMenu.getActionMap().remove("CLOSE_ON_ESCAPE_POPUP");
      }
      
      @Override
      public void popupMenuCanceled(PopupMenuEvent e) {}
    });
    
    popupMenu.show(c, p.x, p.y);
  }
  
  public void showContextMenu(MouseEvent e, final RememberMe rMe) {
    if(e.isPopupTrigger()) {
      showContextMenu(e.getComponent(), e.getPoint(), rMe);
    }
  }
  
  private class DummyProgram implements Program {
    private RememberedProgram mProgram;

    public DummyProgram(RememberedProgram program) {
      mProgram = program;
    }
    
    @Override
    public void addChangeListener(ChangeListener listener) {
      // TODO Auto-generated method stub
      
    }

    @Override
    public void removeChangeListener(ChangeListener listener) {
      // TODO Auto-generated method stub
      
    }

    @Override
    public String getID() {
      // TODO Auto-generated method stub
      return "dummy";
    }

    @Override
    public String getUniqueID() {
      // TODO Auto-generated method stub
      return mProgram.getUniqueID();
    }

    @Override
    public String getTitle() {
      // TODO Auto-generated method stub
      return mProgram.getTitle();
    }

    @Override
    public String getShortInfo() {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public String getDescription() {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public int getStartTime() {
      // TODO Auto-generated method stub
      return 0;
    }

    @Override
    public int getHours() {
      // TODO Auto-generated method stub
      return 0;
    }

    @Override
    public int getMinutes() {
      // TODO Auto-generated method stub
      return 0;
    }

    @Override
    public int getLength() {
      // TODO Auto-generated method stub
      return 0;
    }

    @Override
    public int getInfo() {
      // TODO Auto-generated method stub
      return 0;
    }

    @Override
    public String getTimeString() {
      // TODO Auto-generated method stub
      return "dummy";
    }

    @Override
    public String getDateString() {
      // TODO Auto-generated method stub
      return mProgram.getDate().toString();
    }

    @Override
    public String getEndTimeString() {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public Channel getChannel() {
      // TODO Auto-generated method stub
      return RememberMe.getPluginManager().getExampleProgram().getChannel();
    }

    @Override
    public Date getDate() {
      // TODO Auto-generated method stub
      return mProgram.getDate();
    }

    @Override
    public byte[] getBinaryField(ProgramFieldType type) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public String getTextField(ProgramFieldType type) {
      // TODO Auto-generated method stub
      if(type == ProgramFieldType.TITLE_TYPE) {
        return getTitle();
      }
      else if(type == ProgramFieldType.EPISODE_TYPE) {
        return mProgram.getEpisodeTitle();
      }
        
      return null;
    }

    @Override
    public int getIntField(ProgramFieldType type) {
      // TODO Auto-generated method stub
      return 0;
    }

    @Override
    public String getIntFieldAsString(ProgramFieldType type) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public int getTimeField(ProgramFieldType type) {
      // TODO Auto-generated method stub
      return 0;
    }

    @Override
    public String getTimeFieldAsString(ProgramFieldType type) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public int getFieldCount() {
      // TODO Auto-generated method stub
      return 0;
    }

    @Override
    public Iterator<ProgramFieldType> getFieldIterator() {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public void mark(Plugin javaPlugin) {
      // TODO Auto-generated method stub
      
    }

    @Override
    public void unmark(Plugin javaPlugin) {
      // TODO Auto-generated method stub
      
    }

    @Override
    public void mark(Marker plugin) {
      // TODO Auto-generated method stub
      
    }

    @Override
    public void unmark(Marker plugin) {
      // TODO Auto-generated method stub
      
    }

    @Override
    public boolean isOnAir() {
      // TODO Auto-generated method stub
      return mProgram.isOnAir();
    }

    @Override
    public Marker[] getMarkerArr() {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public boolean isExpired() {
      // TODO Auto-generated method stub
      return mProgram.isExpired();
    }

    @Override
    public int getProgramState() {
      // TODO Auto-generated method stub
      return 0;
    }

    @Override
    public void validateMarking() {
      // TODO Auto-generated method stub
      
    }

    @Override
    public int getMarkPriority() {
      // TODO Auto-generated method stub
      return 0;
    }

    @Override
    public boolean hasFieldValue(ProgramFieldType type) {
      return (type == ProgramFieldType.TITLE_TYPE || (type == ProgramFieldType.EPISODE_TYPE && mProgram.getEpisodeTitle() != null));
    }
  }
    
  private synchronized void removeSelectionInternal(RememberMe rMe) {
    synchronized (mPrograms) {
      mUndoPrograms.clear();
      Object[] remove = mList.getSelectedValues();
      
      if(remove.length == 0) {
        remove = new RememberedProgram[mList.getModel().getSize()];
        
        for(int i = 0; i < mList.getModel().getSize(); i++) {
          remove[i] = mList.getModel().getElementAt(i);
        }
      }
      
      for(int i = remove.length-1; i >= 0; i--) {
        RememberedProgram prog = (RememberedProgram)remove[i];
        mUndoPrograms.add(prog);
        mPrograms.remove(prog,rMe);
      }
      
      mUndo.setEnabled(!mUndoPrograms.isEmpty());
    }
    
    updatePanelInternal(rMe);
  }
  
  public synchronized void updatePanel(RememberMe rMe) {
    handleAction(TYPE_ACTION_UPDATE, rMe);
  }
  
  private synchronized void handleAction(final int actionType, final  RememberMe rMe) {
    switch(actionType) {
      case TYPE_ACTION_REMOVE: removeSelectionInternal(rMe);break;
      case TYPE_ACTION_UPDATE: updatePanelInternal(rMe);break;
    }
  }
  
  private synchronized void updatePanelInternal(RememberMe rMe) {
    synchronized (mPrograms) {
      final Object[] selectedValues = (mList != null ? mList.getSelectedValues() : new Object[0]);
      
      mModel.clear();

      ArrayList<RememberedProgram> toRemove = new ArrayList<RememberedProgram>();
      
      int index = 0;
      int pos = 0;
      
      int[] selectedIndices = new int[selectedValues.length];
      
      for(RememberedProgram prog : mPrograms) {
        if(prog.isExpired() && prog.isValid(rMe.getDayCount())) {
          if(!containsProgram(prog)) {
            if(mCurrentDayFilter.accept(prog) && mCurrentTagFilter.accept(prog)) {
              mModel.addElement(prog);
              
              for(Object selected : selectedValues) {
                if(selected.equals(prog)) {
                  selectedIndices[pos++] = index;
                }
              }
              
              index++;
            }
          }
          else if(!prog.hasProgram()){
            toRemove.add(prog);
          }
        }
        else if(!prog.isValid(rMe.getDayCount())) {
          toRemove.add(prog);
        }
      }
      
      for(RememberedProgram prog : toRemove) {
        mPrograms.remove(prog,rMe);
      }
      
      if(pos < selectedIndices.length) {
        int[] indiciesCopy = new int[pos];
        System.arraycopy(selectedIndices, 0, indiciesCopy, 0, pos);
        selectedIndices = indiciesCopy;
      }
      
      if(pos > 0 && mList != null) {
        mList.setSelectedIndices(selectedIndices);
      }
    }
  }
  
  private boolean containsProgram(RememberedProgram toCheck) {
    synchronized (mModel) {
      for(int i = 0; i < mModel.size(); i++) {
        RememberedProgram prog = (RememberedProgram)mModel.get(i);
        
        if(toCheck.getDate().equals(prog.getDate()) && toCheck.getTitle().equals(prog.getTitle())) {
          return (toCheck.getTag().equals(prog.getTag()) && (toCheck.getEpisodeTitle() == null && prog.getEpisodeTitle() == null) || (toCheck.getEpisodeTitle() != null && prog.getEpisodeTitle() != null && toCheck.getEpisodeTitle().equals(prog.getEpisodeTitle()))); 
        }
      }
    }
    
    return false;
  }
  
  private static class DayFilter {
    private String mLabel;
    private int mStartDay;
    private int mEndDay;
    
    public DayFilter(String label, int startDay, int endDay) {
      mLabel = label;
      mStartDay = startDay;
      mEndDay = endDay;
    }
    
    public String toString() {
      return mLabel;
    }
    
    public boolean accept(RememberedProgram prog) {
      boolean value = false;
      
      if(prog != null) {
        value = (mStartDay == mEndDay);
        
        Date startDate = Date.getCurrentDate().addDays(mStartDay);
        Date endDate = Date.getCurrentDate().addDays(mEndDay);
        
        value = value || (prog.getDate().compareTo(startDate) <= 0 && prog.getDate().compareTo(endDate) >= 0);
      }
      
      return value;
    }
  }
  
  private static class TagFilter {
    private String mTag;
    
    public TagFilter(String tag) {
      mTag = tag;
    }
    
    public boolean accept(RememberedProgram prog) {
      return mTag == null || (prog != null && prog.getTag().equals(mTag));
    }
  }

  @Override
  public void updatePersona() {
    if(mFilterLabel != null) {
      if(PersonaCompat.getInstance().getHeaderImage() != null) {
        mFilterLabel.setForeground(PersonaCompat.getInstance().getTextColor());
        mTagLabel.setForeground(PersonaCompat.getInstance().getTextColor());
      }
      else {
        mFilterLabel.setForeground(UIManager.getDefaults().getColor("Label.foreground"));
        mTagLabel.setForeground(UIManager.getDefaults().getColor("Label.foreground"));
      }
    }
  }
  
  public void updateFilters(int dayCount) {
    int selection = mDayFilter.getSelectedIndex();
    
    if(selection < 0) {
      selection = 0;
    }
    
    mFilterModel.removeAllElements();
    
    try {
      mFilterModel.addElement(mCurrentDayFilter);
    }catch(Exception e) {/* Catch stupid exception */}
    
    try {
    mFilterModel.addElement(new DayFilter(RememberMe.mLocalizer.msg("last3", "Last 3 days"),0,-3));
    mFilterModel.addElement(new DayFilter(RememberMe.mLocalizer.msg("last7", "Last 7 days"),0,-7));
    
    if(dayCount > 8) {
      mFilterModel.addElement(new DayFilter(RememberMe.mLocalizer.msg("last8to14", "Last 8-14 days"),-8,-14));
    }
    
    if(dayCount > 14) {
      mFilterModel.addElement(new DayFilter(RememberMe.mLocalizer.msg("last15to21", "Last 15-21 days"),-15,-21));
    }
    
    if(dayCount > 21) {
      mFilterModel.addElement(new DayFilter(RememberMe.mLocalizer.msg("last22to28", "Last 22-28 days"),-22,-28));
    }
    
    if(mFilterModel.getSize() > selection) {
      mDayFilter.setSelectedIndex(selection);
    }
    else {
      mDayFilter.setSelectedIndex(mFilterModel.getSize()-1);
    }}catch(Exception e) {e.printStackTrace();}
  }
}
