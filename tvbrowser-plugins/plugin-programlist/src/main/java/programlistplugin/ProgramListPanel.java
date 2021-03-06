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

package programlistplugin;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.ListCellRenderer;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import compat.FilterCompat;
import compat.PersonaCompat;
import compat.PersonaCompatListener;
import compat.ProgramListCompat;
import devplugin.Channel;
import devplugin.Date;
import devplugin.Plugin;
import devplugin.Program;
import devplugin.ProgramFilter;
import util.program.ProgramUtilities;
import util.settings.PluginPictureSettings;
import util.settings.ProgramPanelSettings;
import util.ui.Localizer;
import util.ui.ProgramList;
import util.ui.SendToPluginDialog;
import util.ui.TVBrowserIcons;
import util.ui.TabListenerPanel;
import util.ui.UiUtilities;

/**
 * The panel with the program list.
 * 
 * @author René Mach
 */
public class ProgramListPanel extends TabListenerPanel implements PersonaCompatListener, FilterCompat.FilterChangeListener {
  private static final Localizer mLocalizer = ProgramListPlugin.mLocalizer;
  private static final String DATE_SEPARATOR = "DATE_SEPRATOR";
  private static final int ANCHESTOR_UPDATE_TIMEOUT = 250;
  
  /**
   * maximum number of programs to be shown in the list. If the filter has more results, only the first results are shown.
   */
  private int mMaxListSize = 5000;
  private int mUpdateCounter = 5;
  
  private JComboBox mChannelBox;
  private ProgramFilter mFilter;
  private ArrayList<Program> mPrograms = new ArrayList<Program>();
  private DefaultListModel mModel;
  private ProgramList mList;
  private ProgramPanelSettings mProgramPanelSettings;
  private JCheckBox mShowDescription;
  private JComboBox mFilterBox;
  private JComboBox mDateBox;
  private JLabel mChannelLabel, mFilterLabel, mDateLabel;
  private Thread mListThread;
  private Thread mUpdateThread;

  private JButton mSendBtn;
  private JButton mRefreshBtn;
  
  private JButton mNextDay;
  private JButton mPreviousDay;
  
  private boolean mUpdateList;
  
  private AtomicBoolean mKeepListing;
  
  private long mLastAncestorRemoved;
  
  private static Class<?> mWrapperFilter;
  
  private Component mFocusOwner;
  
  static {
    try {
      mWrapperFilter = Class.forName("util.ui.WrapperFilter");
    } catch (ClassNotFoundException e) {
      mWrapperFilter = null;
    }
  }
  
  public ProgramListPanel(final Channel selectedChannel, boolean showClose, int maxListSize) {
    mKeepListing = new AtomicBoolean(false);
    mMaxListSize = maxListSize;
    createGui(selectedChannel,showClose);
  }
  
  private void createGui(final Channel selectedChannel, boolean showClose) {
    final ProgramListSettings mSettings = ProgramListPlugin.getInstance().getSettings();
    
    setLayout(new BorderLayout(0,10));
    setBorder(Borders.DIALOG_BORDER);
    setOpaque(false);
    
    
    mModel = new DefaultListModel();
    boolean showDescription = mSettings.getBooleanValue(ProgramListSettings.KEY_SHOW_DESCRIPTION);
    mProgramPanelSettings = new ProgramPanelSettings(new PluginPictureSettings(
        PluginPictureSettings.ALL_PLUGINS_SETTINGS_TYPE), !showDescription, ProgramPanelSettings.X_AXIS);
    mList = new ProgramList(mModel, mProgramPanelSettings);
    
    setDefaultFocusOwner(mList);
    
    final ListCellRenderer backend = mList.getCellRenderer();
    
    mList.setCellRenderer(new DefaultListCellRenderer() {
      public Component getListCellRendererComponent(final JList list, Object value, final int index, boolean isSelected,
          boolean cellHasFocus) {
        Component c = backend.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        
        if(value instanceof String) {
          JPanel separator = new JPanel(new FormLayout("0dlu:grow,default,0dlu:grow","5dlu,default,5dlu"));
          separator.setBorder(BorderFactory.createMatteBorder(2, 0, 2, 0, UIManager.getColor("Label.foreground")));
          
          if(list.getModel().getSize() > index + 1) {
            JLabel date = new JLabel(((Program)list.getModel().getElementAt(index + 1)).getDateString());
            date.setFont(date.getFont().deriveFont(date.getFont().getSize2D() + 4).deriveFont(Font.BOLD));
            
            separator.add(date, new CellConstraints().xy(2, 2));
            
            return separator;
          }
          
        }
        
        return c;
      }
    });
    
    mList.addMouseListeners(null);

    Channel[] subscribedChannels = Plugin.getPluginManager().getSubscribedChannels();
    mChannelBox = new JComboBox(subscribedChannels);
    mChannelBox.insertItemAt(mLocalizer.msg("allChannels", "All channels"), 0);
    mChannelBox.setRenderer(new ChannelListCellRenderer());
    if (mSettings.getIndex() < mChannelBox.getItemCount()) {
      mChannelBox.setSelectedIndex(mSettings.getIndex());
    }
    
    if (selectedChannel != null) {
      mChannelBox.setSelectedItem(selectedChannel);
    }
    
    JButton resetChannelBox = new JButton(ProgramListPlugin.getInstance().createImageIcon("actions", "edit-undo", TVBrowserIcons.SIZE_SMALL));
    resetChannelBox.setToolTipText(mLocalizer.msg("reset", "Reset"));
    resetChannelBox.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        SwingUtilities.invokeLater(new Runnable() {
          @Override
          public void run() {
            mChannelBox.setSelectedIndex(0);
          }
        });
      }
    });

    mFilterBox = new JComboBox();

    if (mSettings.getFilterName().isEmpty()) {
      mSettings.setFilterName(Plugin.getPluginManager().getFilterManager().getAllFilter().getName());
    }

    fillFilterBox();
    
    JButton resetFilterBox = new JButton(ProgramListPlugin.getInstance().createImageIcon("actions", "edit-undo", TVBrowserIcons.SIZE_SMALL));
    resetFilterBox.setToolTipText(mLocalizer.msg("reset", "Reset"));
    resetFilterBox.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        updateFilter(Plugin.getPluginManager().getFilterManager().getDefaultFilter());
      }
    });
    
    mDateBox = new JComboBox() {
      @Override
      public void setSelectedItem(Object anObject) {
        if(!(anObject instanceof JSeparator)) {
          super.setSelectedItem(anObject);
        }
      }
    };
    
    mDateBox.setRenderer(new DefaultListCellRenderer() {
      @Override
      public Component getListCellRendererComponent(JList list, Object value,
          int index, boolean isSelected, boolean cellHasFocus) {
        JLabel c = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected,
            cellHasFocus);
        
        if(value instanceof Integer) {
          c.setText(mLocalizer.msg("dateFromNow", "From now"));
        }
        else if(value instanceof Date) {
          Date date = (Date)value;
          
          String dateString = date.getLongDateString();
          
          if(date.addDays(1).equals(Date.getCurrentDate())) {
            dateString = Localizer.getLocalization(Localizer.I18N_YESTERDAY) + ", " + dateString;
          }
          else if(date.equals(Date.getCurrentDate())) {
            dateString = Localizer.getLocalization(Localizer.I18N_TODAY) + ", " + dateString;
          }
          else if(date.addDays(-1).equals(Date.getCurrentDate())) {
            dateString = Localizer.getLocalization(Localizer.I18N_TOMORROW) + ", " + dateString;
          }
          
          c.setText(dateString);
        }
        else if(value instanceof JSeparator) {
          JSeparator sep = new JSeparator();
          return sep;
        }
        
        c.setBorder(BorderFactory.createEmptyBorder(3, 0, 3, 0));
        
        return c;
      }
    });
    
    fillDateBox();

    JButton resetDateBox = new JButton(ProgramListPlugin.getInstance().createImageIcon("actions", "edit-undo", TVBrowserIcons.SIZE_SMALL));
    resetDateBox.setToolTipText(mLocalizer.msg("reset", "Reset"));
    resetDateBox.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        SwingUtilities.invokeLater(new Runnable() {
          @Override
          public void run() {
            mDateBox.setSelectedIndex(0);
          }
        });
      }
    });
    
    mFilterBox.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        if(e == null || e.getStateChange() == ItemEvent.SELECTED) {
          ProgramFilter[] filterArr = ProgramListPlugin.getPluginManager().getFilterManager().getAvailableFilters();
          
          boolean found = false;
          
          for(ProgramFilter filter : filterArr) {
            if(mFilterBox.getSelectedItem().equals(filter)) {
              mFilter = filter;
              
              if (mFilter != ProgramListPlugin.getInstance().getReceiveFilter()) {
                mSettings.setFilterName(mFilter.getName());
                found = true;
                break;
              }
            }
          }
          
          if(!found) {
            fillFilterBox();
          }
          
          mChannelBox.getItemListeners()[0].itemStateChanged(null);
        }
      }
    });

    mChannelBox.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        if(e == null || e.getStateChange() == ItemEvent.SELECTED) {          
          fillProgramList();
        }
        
        mSettings.setIndex(mChannelBox.getSelectedIndex());
      }
    });
    
    mDateBox.addItemListener(new ItemListener() {
      @Override
      public void itemStateChanged(ItemEvent e) {
        if(e == null || e.getStateChange() == ItemEvent.SELECTED) {
          fillProgramList();
        }
      }
    });

    CellConstraints cc = new CellConstraints();

    JPanel panel = new JPanel(new FormLayout("1dlu,default,3dlu,default:grow,3dlu,default", "default,2dlu,default,2dlu,default,2dlu"));
    panel.setOpaque(false);
    panel.add(mDateLabel = new JLabel(mLocalizer.msg("date", "Date:")), cc.xy(2, 1));
    panel.add(mDateBox, cc.xy(4, 1));
    panel.add(resetDateBox, cc.xy(6, 1));
    panel.add(mChannelLabel = new JLabel(Localizer.getLocalization(Localizer.I18N_CHANNELS) + ":"), cc.xy(2, 3));
    panel.add(mChannelBox, cc.xy(4, 3));
    panel.add(resetChannelBox, cc.xy(6, 3));
    panel.add(mFilterLabel = new JLabel(mLocalizer.msg("filter", "Filter:")), cc.xy(2, 5));
    panel.add(mFilterBox, cc.xy(4, 5));
    panel.add(resetFilterBox, cc.xy(6, 5));

    mPreviousDay = new JButton(TVBrowserIcons.left(TVBrowserIcons.SIZE_SMALL));
    mPreviousDay.setToolTipText(mLocalizer.msg("prevDay", "Scrolls to previous day from current view position (if there is previous day in the list)"));
    mPreviousDay.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        int index = mList.locationToIndex(mList.getVisibleRect().getLocation())-1;
        
        if(index > 0) {
          Object o = mList.getModel().getElementAt(index);
          
          if(o instanceof String) {
            o = mList.getModel().getElementAt(index-1);
            index--;
          }
          
          if(index > 0) {
            Date current = ((Program)o).getDate();
            
            for(int i = index-1; i >= 0; i--) {
              Object test = mList.getModel().getElementAt(i);
              
              if(test instanceof Program && current.compareTo(((Program)test).getDate()) > 0) {
                mList.ensureIndexIsVisible(i+1);
                return;
              }
            }
          }
        }
        
        if(mList.getModel().getSize() > 0) {
          mList.ensureIndexIsVisible(0);
        }
      }
    });
    
    mNextDay = new JButton(TVBrowserIcons.right(TVBrowserIcons.SIZE_SMALL));
    mNextDay.setToolTipText(mLocalizer.msg("nextDay", "Scrolls to next day from current view position (if there is next day in the list)"));
    mNextDay.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        int index = mList.locationToIndex(mList.getVisibleRect().getLocation());
        
        if(index < mList.getModel().getSize() - 1) {
          Object o = mList.getModel().getElementAt(index);
          
          if(o instanceof String) {
            o = mList.getModel().getElementAt(index+1);
            index++;
          }
          
          if(index < mList.getModel().getSize() - 1) {
            Date current = ((Program)o).getDate();
            
            for(int i = index + 1; i < mList.getModel().getSize(); i++) {
              Object test = mList.getModel().getElementAt(i);
              
              if(test instanceof Program && current.compareTo(((Program)test).getDate()) < 0) {
                Point p = mList.indexToLocation(i-(ProgramListPlugin.getInstance().getSettings().getBooleanValue(ProgramListSettings.KEY_SHOW_DATE_SEPARATOR) ? 1 : 0));
                mList.scrollRectToVisible(new Rectangle(p.x,p.y,1,mList.getVisibleRect().height));
                return;
              }
            }            
          }
        }
      }
    });
    
    mSendBtn = new JButton(TVBrowserIcons.copy(TVBrowserIcons.SIZE_SMALL));
    mSendBtn.setToolTipText(mLocalizer.msg("send", "Send to other Plugins"));
    mSendBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {try {
        Object[] objects = mList.getSelectedValues();
        ArrayList<Program> programs = new ArrayList<Program>();
        
        for(Object o : objects) {
          if(o instanceof Program) {
            programs.add((Program)o);
          }
        }
        
        if (programs.size() == 0) {
          programs = mPrograms;
        }
        if (programs.size() > 0) {
          SendToPluginDialog sendDialog = new SendToPluginDialog(ProgramListPlugin.getInstance(), ProgramListPlugin.getInstance().getDialog() != null && ProgramListPlugin.getInstance().getDialog().isVisible() ? ProgramListPlugin.getInstance().getDialog() : ProgramListPlugin.getInstance().getSuperFrame(),
              programs.toArray(new Program[programs.size()]));
          sendDialog.setVisible(true);
        }
      }catch(Exception e1) {e1.printStackTrace();}
      }
    });
    
    mRefreshBtn = new JButton(TVBrowserIcons.refresh(TVBrowserIcons.SIZE_SMALL));
    mRefreshBtn.setToolTipText(mLocalizer.msg("refreshList", "Refresh list"));
    mRefreshBtn.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        new Thread("refresh program list panel") {
          public void run() {
            fillProgramList();
            mChannelBox.getItemListeners()[0].itemStateChanged(null);            
          }
        }.start();
      }
    });

    mShowDescription = new JCheckBox(mLocalizer.msg("showProgramDescription", "Show program description"),
        showDescription);
    mShowDescription.setOpaque(false);
    mShowDescription.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        int topRow = mList.getFirstVisibleIndex();
        mProgramPanelSettings.setShowOnlyDateAndTitle(e.getStateChange() == ItemEvent.DESELECTED);
        
        if(mMaxListSize != ProgramListPlugin.MAX_PANEL_LIST_SIZE) {
          ProgramListPlugin.getInstance().updateDescriptionSelection(e.getStateChange() == ItemEvent.SELECTED);
        }
        
        mSettings.setBooleanValue(ProgramListSettings.KEY_SHOW_DESCRIPTION,e.getStateChange() == ItemEvent.SELECTED);
        mList.updateUI();
        if (topRow != -1) {
          mList.ensureIndexIsVisible(topRow);
        }
      }
    });

    JButton close = new JButton(Localizer.getLocalization(Localizer.I18N_CLOSE));
    close.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        ProgramListPlugin.getInstance().closeDialog();
      }
    });

    JPanel southPanel = new JPanel(new FormLayout("default,5dlu,default,10dlu,default,5dlu,default,5dlu,default,0dlu:grow,default", "default"));
    southPanel.setOpaque(false);
    
    southPanel.add(mPreviousDay, cc.xy(1, 1));
    southPanel.add(mNextDay, cc.xy(3, 1));
    southPanel.add(mSendBtn, cc.xy(5, 1));
    southPanel.add(mRefreshBtn, cc.xy(7, 1));
    southPanel.add(mShowDescription, cc.xy(9, 1));
    
    if(showClose) {
      southPanel.add(close, cc.xy(11, 1));
    }

    final JScrollPane pane = new JScrollPane(mList);
    
    add(panel, BorderLayout.NORTH);
    add(pane, BorderLayout.CENTER);
    add(southPanel, BorderLayout.SOUTH);
    
    mUpdateList = false;
    mLastAncestorRemoved = -1;
    
    addAncestorListener(new AncestorListener() {
      @Override
      public void ancestorRemoved(AncestorEvent event) {
        mLastAncestorRemoved = System.currentTimeMillis();
        mUpdateList = false;
      }
      
      @Override
      public void ancestorMoved(AncestorEvent event) {}
      
      @Override
      public void ancestorAdded(AncestorEvent event) {
        mUpdateList = true;
        
        if(mLastAncestorRemoved == -1) {
          fillProgramList();
        }
        else {
          if(System.currentTimeMillis() - mLastAncestorRemoved > ANCHESTOR_UPDATE_TIMEOUT) {
            mCurrentSelection = mList.getSelectedValue();
            mCurrentVisible = mList.getVisibleRect();
            mCurrentCount = mModel.getSize();
            
            mChannelBox.getItemListeners()[0].itemStateChanged(null);
          }
        }
      }
    });
  }
  
  void fillFilterBox() {
    mCurrentSelection = mList.getSelectedValue();
    
    // initialize filter as allFilter because we may no longer find the filter
    // with the stored name
    ProgramFilter receiveFilter = ProgramListPlugin.getInstance().getReceiveFilter();
    
    if (mFilter == null) {
      mFilter = Plugin.getPluginManager().getFilterManager().getAllFilter();
    }
    ArrayList<ProgramFilter> filters = new ArrayList<ProgramFilter>();
    for (ProgramFilter filter : Plugin.getPluginManager().getFilterManager().getAvailableFilters()) {
      filters.add(filter);
    }
    if (receiveFilter != null) {
      filters.add(receiveFilter);
    }

    for (ProgramFilter filter : filters) {
      boolean found = false;

      for (int i = 0; i < mFilterBox.getItemCount(); i++) {
        if (filter != null && mFilterBox.getItemAt(i).equals(filter)) {
          found = true;
          break;
        }
      }

      if (!found) {
        final Object item = getItemForFilter(filter);
        
        mFilterBox.addItem(item);

        if ((receiveFilter == null && filter.getName().equals(ProgramListPlugin.getInstance().getSettings().getFilterName()))
            || (receiveFilter != null && filter.getName().equals(receiveFilter.getName()))) {
          mFilter = filter;
          mFilterBox.setSelectedItem(item);
        }
      }
    }

    for (int i = mFilterBox.getItemCount() - 1; i >= 0; i--) {
      boolean found = false;

      for (ProgramFilter filter : filters) {
        if (mFilterBox.getItemAt(i).equals(filter)) {
          found = true;
          break;
        }
      }

      if (!found) {
        mFilterBox.removeItemAt(i);
      }
    }
  }
  
  void fillDateBox() {
    Object currentDate = mDateBox.getSelectedItem();
    JSeparator sep = new JSeparator();
    
    mDateBox.removeAllItems();
    mDateBox.addItem(Integer.valueOf(-1));
    mDateBox.addItem(sep);
    
    for(Date d = Date.getCurrentDate().addDays(-1); Plugin.getPluginManager().isDataAvailable(d); d = d.addDays(1)) {
      if(d.isFirstDayOfWeek() && mDateBox.getItemCount() > 2) {
        mDateBox.addItem(sep);
      }
      
      mDateBox.addItem(d);
    }
    
    if(currentDate != null) {
      mDateBox.setSelectedItem(currentDate);
    }
  }
  
  synchronized void fillProgramList() {
    mKeepListing.set(false);
    
    if(mListThread != null && mListThread.isAlive()) {
      try {
        mListThread.join();
      } catch (InterruptedException e1) {
        // Ignore
      }
    }
    
    if(mUpdateListThread != null && mUpdateListThread.isAlive()) {
      try {
        mUpdateListThread.join();
      } catch(InterruptedException e1) {
        //Ignore
      }
    }
    
    mKeepListing.set(true);
    fillProgramListDo();
    
  }
  
  private Thread mUpdateListThread;
  
  private void fillProgramListDo() {
    if(mUpdateList && (mListThread == null || !mListThread.isAlive())) {
      mListThread = new Thread() {
        public void run() {
          if(mUpdateThread != null && mUpdateThread.isAlive()) {
            try {
              mUpdateThread.join();
            } catch (InterruptedException e) {}
          }
          
          mUpdateListThread = new Thread() {
            @Override
            public void run() {
              synchronized (mPrograms) {
                final DefaultListModel model = new DefaultListModel();
                
                try {
                  setPriority(MIN_PRIORITY);
                  setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                  mModel.clear();
                  mPrograms.clear();
                  
                  Channel[] channels = mChannelBox.getSelectedItem() instanceof String ? Plugin.getPluginManager()
                      .getSubscribedChannels() : new Channel[] { (Channel) mChannelBox.getSelectedItem() };
                  
                  boolean dateSelected = mDateBox.getSelectedItem() instanceof Date;
                      
                  Date date = dateSelected ? (Date)mDateBox.getSelectedItem() : Date.getCurrentDate();
        
                  int startTime = Plugin.getPluginManager().getTvBrowserSettings().getProgramTableStartOfDay();
                  int endTime = Plugin.getPluginManager().getTvBrowserSettings().getProgramTableEndOfDay();
        
                  int maxDays = dateSelected ? 2 : 28;
                  
                  //boolean showExpired = date.compareTo(Date.getCurrentDate()) != 0;
                  
                  for (int d = 0; d < maxDays; d++) {
                    if(!mKeepListing.get()) {
                      break;
                    }
                    
                    if (Plugin.getPluginManager().isDataAvailable(date)) {
                      for (Channel channel : channels) {
                        if(!mKeepListing.get()) {
                          break;
                        }
                        
                        for (Iterator<Program> it = Plugin.getPluginManager().getChannelDayProgram(date, channel); it.hasNext();) {
                          if(!mKeepListing.get()) {
                            break;
                          }
                          
                          Program program = it.next();
                          if ((dateSelected || !program.isExpired()) && mFilter.accept(program)) {
                            if (dateSelected) {
                              if ((d == 0 && program.getStartTime() >= startTime)
                                  || (d == 1 && program.getStartTime() <= endTime)) {
                                mPrograms.add(program);
                              }
                            } else {
                              mPrograms.add(program);
                            }
                          }
                        }
                      }
                    }
                    date = date.addDays(1);
                  }
        
                  if (channels.length > 1) {
                    Collections.sort(mPrograms, ProgramUtilities.getProgramComparator());
                  }
        
                  int index = -1;
                  
                  Program lastProgram = null;
                  
                  int currentSelectionNewIndex = -1;
                  
                  for (Program program : mPrograms) {
                    if(!mKeepListing.get()) {
                      break;
                    }
                    
                    if (model.size() < mMaxListSize) {
                      if(ProgramListPlugin.getInstance().getSettings().getBooleanValue(ProgramListSettings.KEY_SHOW_DATE_SEPARATOR) && 
                          (lastProgram == null || program.getDate().compareTo(lastProgram.getDate()) > 0)) {
                        model.addElement(DATE_SEPARATOR);
                      }
                      
                      model.addElement(program);
                      
                      if(mCurrentSelection != null && mCurrentSelection.equals(program)) {
                        currentSelectionNewIndex = model.getSize()-1;
                      }
                      
                      if (!program.isExpired() && index == -1) {
                        index = model.getSize() - (ProgramListPlugin.getInstance().getSettings().getBooleanValue(ProgramListSettings.KEY_SHOW_DATE_SEPARATOR) ? 2 : 1);
                      }
                      
                      lastProgram = program;
                    }
                  }
                  
                  mCurrentSelection = null;
                  
                  if(currentSelectionNewIndex != -1) {
                    index = currentSelectionNewIndex;
                  }
                  
                  if(index == -1 && dateSelected) {
                    index = 0;
                  }
                  
                  if(mKeepListing.get()) {
                    updateList(model, index, currentSelectionNewIndex != -1);
                  }
                } catch (Exception e) {
                  e.printStackTrace();
                }
              }
            }
          };
          
          SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
              if(mUpdateListThread != null && !mUpdateListThread.isAlive()) {
                mUpdateListThread.start();
              }
            }
          });
        }
      };
      mListThread.setDaemon(true);
      mListThread.start();
    }
  }
  
  
  private void updateList(final DefaultListModel model, final int index, final boolean select) {
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {try {
        mModel = model;
        mList.setModel(model);
        
        final Rectangle rect = mList.getCellBounds(index, index);
        final Rectangle visibleRect = mList.getVisibleRect();
        
        if(rect != null && visibleRect != null) {
          rect.height = visibleRect.height;
        }
        
        if(rect != null) {
          mList.scrollRectToVisible(rect);
        }
        
        if(index >= 0 && index < mModel.getSize()) {
          if(select) {
            mList.setSelectedIndex(index);
          }
          else if(mCurrentVisible != null && mList.contains(mCurrentVisible.getLocation()) && mModel.getSize() == mCurrentCount) {
            mList.scrollRectToVisible(mCurrentVisible);
          }
        }
        
        mCurrentVisible = null;
        mCurrentCount = 0;
        
        setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));   
        mList.repaint();//.updateUI();
      }catch(Throwable t) {t.printStackTrace();}
      }
    });
  }

  private static class ChannelListCellRenderer extends DefaultListCellRenderer {
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
        boolean cellHasFocus) {
      Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

      if (c instanceof JLabel && value instanceof Channel) {
        ((JLabel) c).setIcon(UiUtilities.createChannelIcon(((Channel) value).getIcon()));
      }

      return c;
    }
  }


  @Override
  public void updatePersona() {
    if(PersonaCompat.getInstance().getHeaderImage() != null) {
      mShowDescription.setForeground(PersonaCompat.getInstance().getTextColor());    }
    else {
      mShowDescription.setForeground(UIManager.getColor("Label.foreground"));
    }
    
    mChannelLabel.setForeground(mShowDescription.getForeground());
    mFilterLabel.setForeground(mShowDescription.getForeground());
    mDateLabel.setForeground(mShowDescription.getForeground());
  }
  
  private Object mCurrentSelection = null;
  private Rectangle mCurrentVisible = null;
  private int mCurrentCount = 0;
  
  synchronized void updateFilter(ProgramFilter filter) {
    if(mListThread == null || !mListThread.isAlive()) {
      final Object selected = mFilterBox.getSelectedItem();
      
      if(selected != null && selected.equals(filter)) {
        mCurrentVisible = mList.getVisibleRect();
        mCurrentCount = mModel.getSize();
      }
      
      fillFilterBox();
      mFilterBox.setSelectedItem(getItemForFilter(filter));
    }
  }
  
  void updateDescriptionSelection(boolean value) {
    mShowDescription.setSelected(value);
  }
  
  synchronized void timeEvent() {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        mUpdateCounter--;
        
        if(mUpdateCounter < 0) {
          mUpdateCounter = 5;
          
          if(mDateBox.getSelectedIndex() == 0 && (mListThread == null || !mListThread.isAlive()) && (mUpdateThread == null || !mUpdateThread.isAlive())) {
            mUpdateThread = new Thread() {
              public void run() {
                final DefaultListModel model = new DefaultListModel();
                final AtomicInteger first = new AtomicInteger(mList.getFirstVisibleIndex());
                final AtomicInteger selected = new AtomicInteger(mList.getSelectedIndex());
                final int firstVisible = first.get();
                final int selectedIndex = selected.get();
                
                synchronized (mModel) {
                  for(int i = 0; i < mModel.getSize(); i++) {
                    Object value = mModel.get(i);
                    
                    if(!(value instanceof Program && ((Program)value).isExpired())) {
                      model.addElement(value);
                    }
                    else {
                      if(firstVisible > i) {
                        first.decrementAndGet();
                      }
                      if(selectedIndex > i) {
                        selected.decrementAndGet();
                      }
                    }
                  }
                }
                
                if(model.getSize() != mModel.getSize()) {
                  DefaultListModel old = mModel;
                  mModel = model;
                  mList.setModel(mModel);
                  old.clear();
                  
                  if(selected.get() > 0 && selected.get() < mModel.getSize()) {
                    SwingUtilities.invokeLater(new Runnable() {
                      @Override
                      public void run() {
                        mList.setSelectedIndex(selected.get());
                      }
                    });
                  }
                  
                  if(first.get() > 0 && first.get() < mModel.getSize()) {
                    SwingUtilities.invokeLater(new Runnable() {
                      @Override
                      public void run() {
                        mList.ensureIndexIsVisible(first.get());
                      }
                    });
                    
                  }
                }
              }
            };
            mUpdateThread.start();
          }
        }        
      }
    });
  }

  @Override
  public void filterAdded(ProgramFilter filter) {
    mFilterBox.addItem(getItemForFilter(filter));
  }

  @Override
  public void filterDefaultChanged(ProgramFilter filter) {}

  @Override
  public synchronized void filterRemoved(ProgramFilter filter) {
    final Object selectedFilter = mFilterBox.getSelectedItem();
    
    Object o = getItemForFilter(filter);
    
    if(o != null) {
      mFilterBox.removeItem(o);
    }
    
    if(selectedFilter != null && selectedFilter.equals(filter)) {
      fillProgramList();
    }
  }

  @Override
  public synchronized void filterTouched(ProgramFilter filter) {
    final Object selected = mFilterBox.getSelectedItem();
    
    if(selected != null && selected.equals(filter)) {
      mCurrentSelection = mList.getSelectedValue();
      mCurrentVisible = mList.getVisibleRect();
      mCurrentCount = mModel.getSize();
      
      fillProgramList();
    }
  }
  
  void dateSelected(Date date) {
    if(mUpdateList && (mDateBox.getSelectedItem() == null || !mDateBox.getSelectedItem().equals(date))) {
      mDateBox.setSelectedItem(date);
    }
  }
  
  private Thread mTimeScrollWaitingThread;
  private static final int TIME_SCROLL_WAITING_TIME = 250;
  
  void nowSelected() {
    if(mTimeScrollWaitingThread != null && mTimeScrollWaitingThread.isAlive()) {
      mTimeScrollWaitingThread.interrupt();
    }
    
    if(mDateBox.getSelectedIndex() != 0) {
      mDateBox.setSelectedIndex(0);
    }
    else if(mUpdateList) {
      mList.scrollRectToVisible(new Rectangle(0,0));
    }
  }
  
  void scrollToTime(final int time) {
    if(mUpdateList) {
      if(mTimeScrollWaitingThread != null && mTimeScrollWaitingThread.isAlive()) {
        mTimeScrollWaitingThread.interrupt();
      }
      
      mTimeScrollWaitingThread = new Thread("PROGRAM LIST SCROLL TO TIME THREAD") {
        @Override
        public void run() {
          try {
            sleep(TIME_SCROLL_WAITING_TIME);
            
            SwingUtilities.invokeLater(new Runnable() {
              @Override
              public void run() {
                if(ProgramListPlugin.getInstance().getSettings().getBooleanValue(ProgramListSettings.KEY_TAB_TIME_SCROLL_AROUND)) {
                  ProgramListCompat.scrollToTimeFromCurrentViewIfAvailable(mList, time);
                }
                else {
                  ProgramListCompat.scrollToFirstOccurrenceOfTimeFromCurrentViewOnwardIfAvailable(mList, time);
                }
              }
            });
          } catch (InterruptedException e1) {
            // Ignore
          }
        };
      };
      mTimeScrollWaitingThread.start();
      
    }
  }
  
  void selectChannel(Channel ch) {
    if(mUpdateList && (mChannelBox.getSelectedItem() == null || !mChannelBox.getSelectedItem().equals(ch))) {
      mChannelBox.setSelectedItem(ch);
    }
  }
  
  private Object getItemForFilter(ProgramFilter filter) {
    Object result = filter;
    
    if(mWrapperFilter != null) {
      try {
        Constructor<?> constructor = mWrapperFilter.getConstructor(ProgramFilter.class);
        result = constructor.newInstance(filter);
        
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    
    return result;
  }
  

  public void tabShown() {
    if(mFocusOwner == null) {
      mList.requestFocusInWindow();
    }
    else {
      mFocusOwner.requestFocusInWindow();
    }
  }
  
  public void tabHidden(Component mostRecent) {
    mFocusOwner = null;
    
    for(int i = 0; i < getComponentCount(); i++) {
      if(getComponent(i).equals(mostRecent)) {
        mFocusOwner = mostRecent;
      }
    }
  }
}
