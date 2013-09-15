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
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeListener;

import tvbrowser.extras.searchplugin.SearchPluginProxy;
import util.ui.TVBrowserIcons;
import util.ui.menu.MenuUtil;

import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.Sizes;

import devplugin.ActionMenu;
import devplugin.Channel;
import devplugin.Date;
import devplugin.Marker;
import devplugin.Plugin;
import devplugin.PluginAccess;
import devplugin.Program;
import devplugin.ProgramFieldType;

public class RememberMeManagePanel extends JPanel {
  private ArrayList<RememberedProgram> mPrograms;
  private JList mList;
  private DefaultListModel mModel;
  private JComboBox mDayFilter;
  private DayFilter mCurrentFilter;
  
  public RememberMeManagePanel(ArrayList<RememberedProgram> programs, final RememberMe rMe) {
    mPrograms = programs;
    mModel = new DefaultListModel();
    
    mCurrentFilter = new DayFilter(RememberMe.mLocalizer.msg("all", "All available days"), 0, 0);
    
    DayFilter[] filterValues = {mCurrentFilter,
                                new DayFilter(RememberMe.mLocalizer.msg("last3", "Last 3 days"),0,-3),
                                new DayFilter(RememberMe.mLocalizer.msg("last7", "Last 7 days"),0,-7),
                                new DayFilter(RememberMe.mLocalizer.msg("last8to14", "Last 8-14 days"),-8,-14)
                               };
    
    mDayFilter = new JComboBox(filterValues);
    mDayFilter.addItemListener(new ItemListener() {
      @Override
      public void itemStateChanged(ItemEvent e) {
        if(e.getStateChange() == ItemEvent.SELECTED) {
          mCurrentFilter = (DayFilter)e.getItem();
          updatePanel(rMe);
        }
      }
    });
    
    mList = new JList(mModel);
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
    
    JPanel filterPanel = new JPanel();
    filterPanel.setLayout(new BoxLayout(filterPanel, BoxLayout.X_AXIS));
    filterPanel.setOpaque(false);
    
    filterPanel.add(new JLabel(RememberMe.mLocalizer.msg("filterDays", "Filter:")));
    filterPanel.add(Box.createRigidArea(new Dimension(Sizes.dialogUnitXAsPixel(5, filterPanel),0)));
    filterPanel.add(mDayFilter);
    
    setLayout(new BorderLayout(0,5));
    add(filterPanel, BorderLayout.NORTH);
    add(new JScrollPane(mList), BorderLayout.CENTER);
    setBorder(Borders.DIALOG);
    setOpaque(false);
    
    JButton remove = new JButton(TVBrowserIcons.delete(TVBrowserIcons.SIZE_SMALL));
    remove.setToolTipText(RememberMe.mLocalizer.msg("remove", "Remove from list (cannot be undone)"));
    remove.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        removeSelection(rMe);
      }
    });
    
    JPanel buttons = new JPanel(new BorderLayout());
    buttons.setOpaque(false);
    buttons.add(remove, BorderLayout.WEST);
    
    add(buttons, BorderLayout.SOUTH);
    
    updatePanel(rMe);
  }
  
  public void showContextMenu(MouseEvent e, final RememberMe rMe) {
    if(e.isPopupTrigger()) {
      JPopupMenu popup = new JPopupMenu();
      
      JMenuItem remove = new JMenuItem(RememberMe.mLocalizer.msg("remove", "Remove from list (cannot be undone)"),TVBrowserIcons.delete(TVBrowserIcons.SIZE_SMALL));
      remove.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
          removeSelection(rMe);
        }
      });
      
      popup.add(remove);
      
      if(mList.getSelectedIndices().length == 1) {
        popup.addSeparator();
        
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
        
        popup.add(copy);
        
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
          
          popup.add(copyEpisode);
        }
        
        DummyProgram dummy = new DummyProgram((RememberedProgram)mList.getSelectedValue());
        
        ActionMenu search = SearchPluginProxy.getInstance().getContextMenuActions(dummy);
        
        if(search != null) {
          popup.add(MenuUtil.createMenuItem(search));
        }
        
        PluginAccess access = Plugin.getPluginManager().getActivatedPluginForId("java.webplugin.WebPlugin");
        
        if(access != null) {
          ActionMenu iSearch = access.getContextMenuActions(dummy);
          
          if(iSearch != null) {
            popup.add(MenuUtil.createMenuItem(iSearch));
          }
        }
      }
      
      popup.show(e.getComponent(), e.getX(), e.getY());
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
  
  private void removeSelection(RememberMe rMe) {
    int[] remove = mList.getSelectedIndices();
    
    if(remove.length == 0) {
      remove = new int[mList.getModel().getSize()];
      
      for(int i = 0; i < mList.getModel().getSize(); i++) {
        remove[i] = i;
      }
    }
    
    for(int i = remove.length-1; i >= 0; i--) {
      RememberedProgram prog = mPrograms.remove(remove[i]);
      prog.unmark(rMe);
    }
    
    updatePanel(rMe);
  }

  
  public synchronized void updatePanel(RememberMe rMe) {
    mModel.clear();
    
    ArrayList<RememberedProgram> toRemove = new ArrayList<RememberedProgram>();
    
    for(RememberedProgram prog : mPrograms) {
      if(prog.isExpired() && prog.isValid()) {
        if(!containsProgram(prog)) {
          if(mCurrentFilter.accept(prog)) {
            mModel.addElement(prog);
          }
        }
        else if(!prog.hasProgram()){
          toRemove.add(prog);
        }
      }
      else if(!prog.isValid()) {
        toRemove.add(prog);
      }
    }
    
    for(RememberedProgram prog : toRemove) {
      mPrograms.remove(prog);
      prog.unmark(rMe);
    }
  }
  
  private boolean containsProgram(RememberedProgram toCheck) {
    for(int i = 0; i < mModel.size(); i++) {
      RememberedProgram prog = (RememberedProgram)mModel.get(i);
      
      if(toCheck.getDate().equals(prog.getDate()) && toCheck.getTitle().equals(prog.getTitle())) {
        return (toCheck.getTag().equals(prog.getTag()) && (toCheck.getEpisodeTitle() == null && prog.getEpisodeTitle() == null) || (toCheck.getEpisodeTitle() != null && prog.getEpisodeTitle() != null && toCheck.getEpisodeTitle().equals(prog.getEpisodeTitle()))); 
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
}
