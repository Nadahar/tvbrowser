/*
 * AndroidSync - Plugin for TV-Browser
 * Copyright (C) 2014 René Mach
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
 * SVN information:
 *     $Date$
 *   $Author$
 * $Revision$
 */
package androidsync;

import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Properties;
import java.util.TimeZone;
import java.util.zip.GZIPOutputStream;

import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import org.apache.commons.codec.binary.Base64;

import tvbrowser.extras.reminderplugin.ReminderPlugin;
import tvdataservice.MarkedProgramsList;
import util.browserlauncher.Launch;
import util.io.IOUtilities;
import util.ui.ChannelListCellRenderer;
import util.ui.DefaultMarkingPrioritySelectionPanel;
import util.ui.EnhancedPanelBuilder;
import util.ui.Localizer;
import util.ui.UiUtilities;
import util.ui.customizableitems.SelectableItemList;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import devplugin.ActionMenu;
import devplugin.Channel;
import devplugin.ContextMenuAction;
import devplugin.Date;
import devplugin.Marker;
import devplugin.Plugin;
import devplugin.PluginAccess;
import devplugin.PluginCommunication;
import devplugin.PluginInfo;
import devplugin.PluginTreeNode;
import devplugin.PluginsProgramFilter;
import devplugin.Program;
import devplugin.ProgramFilter;
import devplugin.SettingsTab;
import devplugin.ThemeIcon;
import devplugin.Version;

/**
 * Exports and imports channels, markings and reminders to and from Android version of TV-Browser.
 * 
 * @author René Mach
 */
public class AndroidSync extends Plugin {
  private static final Localizer mLocalizer = Localizer.getLocalizerFor(AndroidSync.class);
  
  private static final String BACK_SYNC_ADDRESS = "http://android.tvbrowser.org/data/scripts/syncDown.php?type=favortiesFromApp";
  private static final String FAVORITE_SYNC_ADDRESS = "http://android.tvbrowser.org/data/scripts/syncUp.php?type=favoritesFromDesktop";
  private static final String CHANNEL_UP_SYNC_ADDRESS = "http://android.tvbrowser.org/data/scripts/syncUp.php?type=channelsFromDesktop";
  private static final String CHANNEL_DOWN_SYNC_ADDRESS = "http://android.tvbrowser.org/data/scripts/syncDown.php?type=channelsFromDesktop";
  
  private static final String REMINDER_UP_SYNC_ADDRESS = "http://android.tvbrowser.org/data/scripts/syncUp.php?type=reminderFromDesktop";
  private static final String REMINDER_BACK_SYNC_ADDRESS = "http://android.tvbrowser.org/data/scripts/syncDown.php?type=reminderFromApp";
  
  private static final String LAST_UPLOAD = "LAST_UPLOAD";
  private static final String SELECTED_PLUGINS = "SELECTED_PLUGINS";
  private static final String SELECTED_INTERNAL_PLUGINS = "SELECTED_INTERNAL_PLUGINS";
  private static final String SELECTED_FILTER = "SELECTED_FILTER";
  private static final String SYNCHRONIZE_REMINDER = "SYNCHRONIZE_REMINDER";
  private static final String TYPE = "TYPE";
  
  private static final String PLUGIN_TYPE = "PLUGIN_TYPE";
  private static final String FILTER_TYPE = "FILTER_TYPE";
  
  private static final Version mVersion = new Version(0, 23, 0, true);
  private final String CrLf = "\r\n";
  private Properties mProperties;
  
  private static final String CAR_KEY = "CAR_KEY";
  private static final String BICYCLE_KEY = "BICYCLE_KEY";
  
  private ArrayList<Program> mBackSyncedPrograms;
  private ArrayList<Program> mExportedReminders;
  private ArrayList<Program> mRemovedReminders;
  
  private ThemeIcon mIcon;
  private PluginsProgramFilter[] mFilter;
  
  private int mMarkPriority;
  
  private Channel[] mUsedChannelArr;
  private boolean mShowChannelInfo;
  
  private AndroidSyncCommunication mCommunication;
  
  public AndroidSync() {
    mProperties = new Properties();
    mBackSyncedPrograms = new ArrayList<Program>();
    mExportedReminders = new ArrayList<Program>();
    mRemovedReminders = new ArrayList<Program>();
    
    mIcon = new ThemeIcon("apps", "android_robot", 16);
    mMarkPriority = Program.MIN_MARK_PRIORITY;
    
    PluginsProgramFilter filter = new PluginsProgramFilter(this) {
      @Override
      public boolean accept(Program program) {
        return mBackSyncedPrograms.contains(program);
      }
      
      @Override
      public String getSubName() {
        return null;
      }
    };
    
    mFilter = new PluginsProgramFilter[] { filter };
    
    mUsedChannelArr = new Channel[0];
    mShowChannelInfo = false;
    
    mCommunication = new AndroidSyncCommunication(this);
  }
  
  public static Version getVersion() {
    return mVersion;
  }
  
  @Override
  public void handleTvBrowserStartFinished() {
    if(mProperties.getProperty(CAR_KEY,"").trim().length() == 0 || mProperties.getProperty(BICYCLE_KEY,"").trim().length() == 0) {
      String[] options = {
          mLocalizer.msg("enterNow", "Save user data"),
          Localizer.getLocalization(Localizer.I18N_CANCEL)
      };
      
      if(getParentFrame() != null) {
        Window w = UiUtilities.getLastModalChildOf(getParentFrame());
        
        UserPanel userPanel = new UserPanel(mProperties.getProperty(CAR_KEY,""), mProperties.getProperty(BICYCLE_KEY,""), true);
        
        int selected = JOptionPane.showOptionDialog(w, new Object[] {mLocalizer.msg("notSetup", "No user data found for synchronization of TV-Browser for Android.\n\nDo you want to enter them now or do you want to create new user data (Internet access needed)?\n"), userPanel}, getInfo().getName() + ": " + mLocalizer.msg("notSetupTitle", "No user data found"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, createImageIcon("apps", "android_robot", 22), options, options[0]);
        
        if(selected == JOptionPane.OK_OPTION) {
          mProperties.put(CAR_KEY, userPanel.getCar());
          mProperties.put(BICYCLE_KEY, userPanel.getBicycle());
        }
      }
    }
    else {
      updateChannels();
    }
  }
  
  private void updateChannels() {
    if(!mShowChannelInfo) {
      Channel[] channels = getPluginManager().getSubscribedChannels();
      
      mShowChannelInfo = channels.length != mUsedChannelArr.length;
      
      if(!mShowChannelInfo) {
        for(int i = 0; i < mUsedChannelArr.length; i++) {
          if(!channels[i].equals(mUsedChannelArr[i])) {
            mShowChannelInfo = true;
            break;
          }
        }
      }
    }
    
    if(mShowChannelInfo) {
      //Window w = UiUtilities.getLastModalChildOf(getParentFrame());
      
      int selected = JOptionPane.showConfirmDialog(null, mLocalizer.msg("channelChangeMessage", "The subscribed channel list was changed.\n\nDo you want to upload the new channel list to the TV-Browser server?"), getInfo().getName() + ": " + mLocalizer.msg("channelChangeTitle", "Channels changed"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
      
      if(selected == JOptionPane.YES_OPTION) {
        upload(CHANNEL_UP_SYNC_ADDRESS, true);
      }
      else {
        mUsedChannelArr = getPluginManager().getSubscribedChannels();
        saveMe();
      }
      
      mShowChannelInfo = false;
    }
  }
  
  @Override
  public PluginInfo getInfo() {
    return new PluginInfo(AndroidSync.class, "Android Sync", mLocalizer.msg("description", "Exports highlighted programs of selected Plugins/Filter and subscribed channels to the TV-Browser server to make them available for TV-Browser for Android"), "René Mach");
  }
  @Override
  public ActionMenu getButtonAction() {
    ContextMenuAction action = new ContextMenuAction(mLocalizer.msg("syncAndroid", "Export my highlighted/filtered programs to TV-Browser server"),createImageIcon("actions","export-programs",16));
    action.setActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        new Thread() {
          public void run() {
            upload(FAVORITE_SYNC_ADDRESS,true);
          }
        }.start();
      }
    });
    action.putValue(Plugin.BIG_ICON, createImageIcon("actions","export-programs",22));
    
    ContextMenuAction backSync = new ContextMenuAction(mLocalizer.msg("syncBack", "Get synchronized programs from Android app"),createImageIcon("actions","export-programs",16));
    backSync.setActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        new Thread() {
          public void run() {
            download(BACK_SYNC_ADDRESS,true,true);
          }
        }.start();
      }
    });
    backSync.putValue(Plugin.BIG_ICON, createImageIcon("actions","export-programs",22));
    
    ContextMenuAction channels = new ContextMenuAction(mLocalizer.msg("syncChannels", "Export my subscribed EPGfree/EPGdonate channels to TV-Browser server"),createImageIcon("actions","export-channels",16));
    channels.setActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        new Thread() {
          public void run() {
            upload(CHANNEL_UP_SYNC_ADDRESS,true);
          }
        }.start();
      }
    });
    channels.putValue(Plugin.BIG_ICON, createImageIcon("actions","export-channels",22));
    
    ContextMenuAction remindersUp = new ContextMenuAction(mLocalizer.msg("syncUpReminders", "Export my Reminders to TV-Browser server"),createImageIcon("apps",
          "appointment", 16));
    remindersUp.setActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        new Thread() {
          public void run() {
            upload(REMINDER_UP_SYNC_ADDRESS,true);
          }
        }.start();
      }
    });
    remindersUp.putValue(Plugin.BIG_ICON, createImageIcon("apps","appointment",22));
    
    ContextMenuAction remindersBack = new ContextMenuAction(mLocalizer.msg("syncBackReminders", "Get Reminders from the Android app"),createImageIcon("apps",
        "appointment-new", 16));
    remindersBack.setActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        new Thread() {
          public void run() {
            download(REMINDER_BACK_SYNC_ADDRESS,true,true);
          }
        }.start();
      }
    });
    remindersBack.putValue(Plugin.BIG_ICON, createImageIcon("actions","appointment-new",22));
    
    ActionMenu menu = new ActionMenu(getInfo().getName(), createImageIcon("apps","android_robot",16), new ContextMenuAction[] {action, backSync, channels, remindersUp, remindersBack});
    menu.getAction().putValue(Plugin.BIG_ICON, createImageIcon("apps","android_robot",22));
    
    return menu;
  }
  @Override
  public void handleTvDataUpdateFinished() {
    String date = mProperties.getProperty(LAST_UPLOAD,"1970-01-01");
    
    String[] parts = date.split("-");
    
    Date lastUpload = new Date(Short.parseShort(parts[0]), Short.parseShort(parts[1]), Short.parseShort(parts[2]));
    
    if(lastUpload.compareTo(Date.getCurrentDate()) < 0) {
      upload(FAVORITE_SYNC_ADDRESS,false);
      Date today = Date.getCurrentDate();
      mProperties.setProperty(LAST_UPLOAD, today.getYear() + "-" + today.getMonth() + "-" + today.getDayOfMonth());
      
      download(BACK_SYNC_ADDRESS,false,false);
    }
    
    if(mProperties.getProperty(SYNCHRONIZE_REMINDER, "true").trim().equals("true")) {
      download(REMINDER_BACK_SYNC_ADDRESS, false,false);
      upload(REMINDER_UP_SYNC_ADDRESS, false);
    }
    
    updateChannels();
  }
  
  @Override
  public int getMarkPriorityForProgram(Program p) {
    return mMarkPriority;
  }
  
  @Override
  public SettingsTab getSettingsTab() {
    return new SettingsTab() {
      private SelectableItemList mPluginSelection;
      private JRadioButton mPluginType;
      private JRadioButton mFilterType;
      private JComboBox mFilterSelection;
      private DefaultMarkingPrioritySelectionPanel mMarkingsPanel;
      private JCheckBox mSynchroReminders;
      private UserPanel mUserPanel;
      
      @Override
      public void saveSettings() {
        String oldCar = mProperties.getProperty(CAR_KEY, "");
        String oldBicycle = mProperties.getProperty(BICYCLE_KEY, "");
        
        mProperties.setProperty(CAR_KEY, mUserPanel.getCar());
        mProperties.setProperty(BICYCLE_KEY, mUserPanel.getBicycle());
        
        if(mPluginType.isSelected()) {
          mProperties.setProperty(TYPE, PLUGIN_TYPE);
        }
        else {
          mProperties.setProperty(TYPE, FILTER_TYPE);
        }
        
        Object[] selection = mPluginSelection.getSelection();
        
        StringBuilder internal = new StringBuilder();
        StringBuilder plugins = new StringBuilder();
        
        for(Object selected : selection) {
          if(selected instanceof String) {
            if(selected.equals(mLocalizer.msg("favorites", "Favorites"))) {
              internal.append("favoritesplugin.FavoritesPlugin");
            }
          }
          else {
            String id = ((PluginAccess)selected).getId();
            
            if(plugins.length() > 0) {
              plugins.append(";");
            }
            
            plugins.append(id);
          }
        }
        
        mProperties.setProperty(SELECTED_INTERNAL_PLUGINS, internal.toString());
        mProperties.setProperty(SELECTED_PLUGINS, plugins.toString());
        mProperties.setProperty(SYNCHRONIZE_REMINDER,String.valueOf(mSynchroReminders.isSelected()));
        
        mProperties.setProperty(SELECTED_FILTER, ((ProgramFilter)mFilterSelection.getSelectedItem()).getName());
        
        if(mProperties.getProperty(CAR_KEY,"").trim().length() > 0 && mProperties.getProperty(BICYCLE_KEY,"").trim().length() > 0 &&
            (!oldCar.equals(mProperties.getProperty(CAR_KEY,"")) || !oldBicycle.equals(mProperties.getProperty(BICYCLE_KEY,"")))) {
          String[] options = new String[] {
              mLocalizer.msg("optionExport", "Export channels"),
              mLocalizer.msg("optionNotNow", "Not now")
              };
          
          int selectedOption = JOptionPane.showOptionDialog(UiUtilities.getLastModalChildOf(getParentFrame()), 
              mLocalizer.msg("userPasswordChanged", "You have changed the user name or the password\nDo you wish to update the stored channel data on the server?"),
              mLocalizer.msg("userPasswordChangedTitle", "Synchronize channels?"),
              JOptionPane.YES_NO_OPTION,
              JOptionPane.QUESTION_MESSAGE,null,options,options[0]);
          
          if(selectedOption == JOptionPane.YES_OPTION) {
            upload(CHANNEL_UP_SYNC_ADDRESS, true);
          }
        }
        
        mMarkPriority = mMarkingsPanel.getSelectedPriority();
      }
      
      @Override
      public String getTitle() {
        // TODO Auto-generated method stub
        return null;
      }
      
      @Override
      public Icon getIcon() {
        return createImageIcon("apps","android_robot",16);
      }
      
      @Override
      public JPanel createSettingsPanel() {
        PanelBuilder pb = new PanelBuilder(new FormLayout("5dlu,10dlu,50dlu,3dlu,default:grow",
            "default,10dlu,default,5dlu,default,5dlu,default,default,3dlu,default,default,default,fill:10dlu:grow,default"));
        
        pb.border(Borders.createEmptyBorder("5dlu,0dlu,0dlu,0dlu"));
        
        mMarkingsPanel = DefaultMarkingPrioritySelectionPanel.createPanel(mMarkPriority, true, false);
        
        
        String[] selectionIDs = mProperties.getProperty(SELECTED_PLUGINS, "").split(";");
        
        ArrayList<Object> selectedPlugins = new ArrayList<Object>();
        ArrayList<Object> allPlugins = new ArrayList<Object>();
        
        if(mProperties.getProperty(SELECTED_INTERNAL_PLUGINS, "favoritesplugin.FavoritesPlugin").contains("favoritesplugin.FavoritesPlugin")) {
          selectedPlugins.add(mLocalizer.msg("favorites", "Favorites"));
        }
        
        allPlugins.add(mLocalizer.msg("favorites", "Favorites"));
        //allPlugins.add(mLocalizer.msg("reminders", "Reminders"));
        
        PluginAccess[] activatedPlugins = getPluginManager().getActivatedPlugins();
      
        for(PluginAccess activated : activatedPlugins) {
          allPlugins.add(activated);
          
          for(String selection : selectionIDs) {
            if(activated.getId().equals(selection)) {
              selectedPlugins.add(activated);
            }
          }
        }
        
        mPluginSelection = new SelectableItemList(selectedPlugins.toArray(new Object[selectedPlugins.size()]), allPlugins.toArray(new Object[allPlugins.size()]));
        
        ProgramFilter[] filters = getPluginManager().getFilterManager().getAvailableFilters();
        
        mFilterSelection = new JComboBox(filters);
        
        String typeValue = mProperties.getProperty(TYPE,PLUGIN_TYPE);
        String selectedFilter = mProperties.getProperty(SELECTED_FILTER,getPluginManager().getFilterManager().getAllFilter().getName());
        
        for(ProgramFilter filter : filters) {
          if(selectedFilter.equals(filter.getName())) {
            mFilterSelection.setSelectedItem(filter);
            break;
          }
        }
        
        mSynchroReminders = new JCheckBox(mLocalizer.msg("synchronizeReminder", "Synchronize Reminder automatically"), mProperties.getProperty(SYNCHRONIZE_REMINDER,"true").equals("true"));
        mPluginType = new JRadioButton(mLocalizer.msg("pluginType","Hightlighted programs of Plugins"));
        mFilterType = new JRadioButton(mLocalizer.msg("filterType","Accepted programs of Filter"));
        
        ButtonGroup type = new ButtonGroup();
        type.add(mPluginType);
        type.add(mFilterType);
        
        mPluginType.addItemListener(new ItemListener() {
          @Override
          public void itemStateChanged(ItemEvent e) {
            mPluginSelection.setEnabled(e.getStateChange() == ItemEvent.SELECTED);
            mFilterSelection.setEnabled(e.getStateChange() == ItemEvent.DESELECTED);
          }
        });
        
        mPluginType.setSelected(typeValue.equals(PLUGIN_TYPE));
        mFilterType.setSelected(typeValue.equals(FILTER_TYPE));
        
        mPluginSelection.setEnabled(mPluginType.isSelected());
        mFilterSelection.setEnabled(mFilterType.isSelected());
        
        pb.add(mUserPanel = new UserPanel(mProperties.getProperty(CAR_KEY,""), mProperties.getProperty(BICYCLE_KEY,""), true), CC.xyw(2, 1, 4));
        
        pb.addSeparator(mLocalizer.msg("exportPlugins", "Export programs of"), CC.xyw(1, 3, 5));
        
        pb.add(mSynchroReminders, CC.xyw(2, 5, 4));
        pb.add(mPluginType, CC.xyw(2, 7, 4));
        pb.add(mPluginSelection, CC.xyw(3, 8, 3));
        pb.add(mFilterType, CC.xyw(2, 9, 4));
        pb.add(mFilterSelection, CC.xyw(3, 10, 3));
        
        pb.add(mMarkingsPanel, CC.xyw(2, 11, 4));
        
        pb.add(UiUtilities.createHtmlHelpTextArea("The Android robot is reproduced or modified from work created and shared by Google and used according to terms described in the Creative Commons 3.0 Attribution License."), CC.xyw(2, 14, 4));
        
        
        return pb.getPanel();
      }
    };
  }
  
  @Override
  public void loadSettings(Properties settings) {
    mProperties = settings;
    
    String internal = settings.getProperty(SELECTED_INTERNAL_PLUGINS,null);
    
    if(internal != null) {
      if(internal.contains("favoritesplugin.FavoritesPlugin")) {
        internal = "favoritesplugin.FavoritesPlugin";
      }
      else {
        internal = "";
      }
      
      settings.setProperty(SELECTED_INTERNAL_PLUGINS, internal);
    }
    
    mMarkPriority = Integer.parseInt(mProperties.getProperty(Program.MARK_PRIORITY,String.valueOf(Program.MIN_MARK_PRIORITY)));
  }
  
  @Override
  public Properties storeSettings() {
    mProperties.put(Program.MARK_PRIORITY, String.valueOf(mMarkPriority));
    
    return mProperties;
  }
  
  @Override
  public void readData(ObjectInputStream in) throws IOException, ClassNotFoundException {
    try {
      int version = in.readInt(); // version
      
      int count = in.readInt();
      
      mBackSyncedPrograms.clear();
      
      for(int i = 0; i < count; i++) {
        Program test = getPluginManager().getProgram(in.readUTF());
        
        if(test != null) {
          test.mark(this);
          mBackSyncedPrograms.add(test);
        }
      }
      
      if(version > 1) {
        mExportedReminders.clear();
        
        count = in.readInt();
        
        for(int i = 0; i < count; i++) {
          Program test = getPluginManager().getProgram(in.readUTF());
          
          if(test != null) {
            mExportedReminders.add(test);
          }
        }
        
        mRemovedReminders.clear();
        
        count = in.readInt();
        
        for(int i = 0; i < count; i++) {
          Program test = getPluginManager().getProgram(in.readUTF());
          
          if(test != null) {
            mRemovedReminders.add(test);
          }
        }
      }
      
      updateTree();
      
      if(version >= 3) {
        int n = in.readInt();
        
        ArrayList<Channel> usedChannel = new ArrayList<Channel>();
        
        for(int i = 0; i < n; i++) {
          Channel ch = Channel.readData(in, true);
          
          if(ch != null) {
            usedChannel.add(ch);
          }
          else {
            mShowChannelInfo = true;
          }
        }
        
        mUsedChannelArr = usedChannel.toArray(new Channel[usedChannel.size()]);
      }
      else {
        mUsedChannelArr = getPluginManager().getSubscribedChannels();
      }
    }catch(IOException e) {}
  }
  
  @Override
  public void writeData(ObjectOutputStream out) throws IOException {
    out.writeInt(3); // version
    
    out.writeInt(mBackSyncedPrograms.size());
    
    for(Program prog : mBackSyncedPrograms) {
      out.writeUTF(prog.getUniqueID());
    }
    
    out.writeInt(mExportedReminders.size());
    
    for(Program prog : mExportedReminders) {
      out.writeUTF(prog.getUniqueID());
    }
    
    out.writeInt(mRemovedReminders.size());
    
    for(Program prog : mRemovedReminders) {
      out.writeUTF(prog.getUniqueID());
    }
    
    out.writeInt(mUsedChannelArr.length);
    
    for(Channel channel : mUsedChannelArr) {
      channel.writeData(out);
    }
  }
  
  private Channel[] mNotSynchronizedChannels;
  
  private byte[] getXmlBytes(String address) {
    mNotSynchronizedChannels = null;
    
    if(address.equals(FAVORITE_SYNC_ADDRESS)) {
      StringBuilder dat = new StringBuilder();
      
      Program[] programs = MarkedProgramsList.getInstance().getMarkedPrograms();
      
      Date compare = Date.getCurrentDate().addDays(21);
      
      ArrayList<Program> toExport = new ArrayList<Program>();
      
      String internal = mProperties.getProperty(SELECTED_INTERNAL_PLUGINS,"favoritesplugin.FavoritesPlugin");
      String plugins = mProperties.getProperty(SELECTED_PLUGINS,"");
      
      boolean pluginType = mProperties.getProperty(TYPE, PLUGIN_TYPE).equals(PLUGIN_TYPE);
      ProgramFilter allFilter = getPluginManager().getFilterManager().getAllFilter();
      
      String filterName = mProperties.getProperty(SELECTED_FILTER,allFilter.getName());
      
      ProgramFilter filter = null;
      
      ProgramFilter[] availableFilters = getPluginManager().getFilterManager().getAvailableFilters();
      
      for(ProgramFilter available : availableFilters) {
        if(available.getName().equals(filterName)) {
          filter = available;
          break;
        }
      }
      
      if(filter == null) {
        filter = allFilter;
      }
      
      for(Program prog : programs) {
        Marker[] marker = prog.getMarkerArr();
        
        for(Marker mark : marker) {
          if(compare.compareTo(prog.getDate()) >= 0) {
            System.err.println(prog.getChannel().getDataServicePackageName());
            if(((pluginType && (internal.contains(mark.getId()) || plugins.contains(mark.getId()))) || (!pluginType && filter.accept(prog)))
                && (prog.getChannel().getDataServicePackageName().equals("tvbrowserdataservice") || prog.getChannel().getDataServicePackageName().equals("epgdonatedata"))) {
              toExport.add(prog);
            }
          }
        }
      }
      
      for(Program prog : toExport) {
        Calendar cal = prog.getDate().getCalendar();
        
        cal.set(Calendar.HOUR_OF_DAY, prog.getStartTime() / 60);
        cal.set(Calendar.MINUTE, prog.getStartTime() % 60);
        cal.set(Calendar.SECOND, 30);
        
        long time = cal.getTimeInMillis() / 60000;
        
        dat.append(time);
        dat.append(";");
        
        String id = null;
        
        if(prog.getChannel().getDataServicePackageName().equals("tvbrowserdataservice")) {
          id = "1:" + prog.getChannel().getGroup().getId() + ":";
        }
        else if(prog.getChannel().getDataServicePackageName().equals("epgdonatedata")) {
          id = "2:";
        }
        else {
          id = prog.getChannel().getDataServicePackageName() + ":" + prog.getChannel().getGroup().getId() + ":";
        }
        
        dat.append(id);
        dat.append(prog.getChannel().getId());
        dat.append("\n");
      }
            
      return getCompressedData(dat.toString().getBytes());
    }
    else if(address.equals(CHANNEL_UP_SYNC_ADDRESS)) {
      ArrayList<Channel> notUpdateChannels = new ArrayList<Channel>();
      StringBuilder channels = new StringBuilder();
      
      Channel[] subscribed = getPluginManager().getSubscribedChannels();
      
      for(Channel ch : subscribed) {System.out.println(ch.getDataServicePackageName() + " " + ch.getUniqueId());
        if(ch.getDataServicePackageName().equals("tvbrowserdataservice")) {System.out.println(ch.getGroup().getId());
          channels.append("1:");
          channels.append(ch.getGroup().getId());
          channels.append(":");
          channels.append(ch.getId());
          
          String sortNumber = getSortNumber(ch);
          
          if(sortNumber != null) {
            channels.append(":");
            channels.append(sortNumber);
          }
          
          channels.append("\n");
        }
        else if(ch.getDataServicePackageName().equals("epgdonatedata")) {
          channels.append("2:");
          channels.append(ch.getId());
          
          String sortNumber = getSortNumber(ch);
          
          if(sortNumber != null) {
            channels.append(":");
            channels.append(sortNumber);
          }
          
          channels.append("\n");
        }
        else {
          notUpdateChannels.add(ch);
        }
      }
      
      if(!notUpdateChannels.isEmpty()) {
        mNotSynchronizedChannels = notUpdateChannels.toArray(new Channel[notUpdateChannels.size()]);
      }
      
      return getCompressedData(channels.toString().getBytes());
    }
    else if(address.equals(REMINDER_UP_SYNC_ADDRESS)) {
      Program[] programs = MarkedProgramsList.getInstance().getMarkedPrograms();
      
      Date compare = Date.getCurrentDate().addDays(21);
      
      ArrayList<Program> toExport = new ArrayList<Program>();
      
      for(Program prog : programs) {
        if(prog.getDate().compareTo(compare) <= 0) {
          Marker[] test = prog.getMarkerArr();
          
          for(Marker mark : test) {
            if(mark.getId().equals("reminderplugin.ReminderPlugin")) {
              toExport.add(prog);
            }
          }
        }
      }
      
      for(Program old : mExportedReminders) {
        if(!toExport.contains(old) && !old.isExpired() && !mRemovedReminders.contains(old)) {
          mRemovedReminders.add(old);
        }
      }
      
      mExportedReminders = toExport;
      
      StringBuilder dat = new StringBuilder();
      
      for(Program prog : toExport) {
        Calendar cal = prog.getDate().getCalendar();
        
        cal.set(Calendar.HOUR_OF_DAY, prog.getStartTime() / 60);
        cal.set(Calendar.MINUTE, prog.getStartTime() % 60);
        cal.set(Calendar.SECOND, 30);
        
        long time = cal.getTimeInMillis() / 60000;
        
        dat.append(time);
        dat.append(";");
        
        if(prog.getChannel().getDataServicePackageName().equals("tvbrowserdataservice")) {
          dat.append("1");
        }
        else if(prog.getChannel().getDataServicePackageName().equals("epgdonatedata")) {
          dat.append("2");
        }
        else {
          dat.append(prog.getChannel().getDataServicePackageName());
        }
        
        dat.append(":");
        
        if(!prog.getChannel().getDataServicePackageName().equals("epgdonatedata")) {
          dat.append(prog.getChannel().getGroup().getId());
          dat.append(":");
        }
        
        dat.append(prog.getChannel().getId());
        dat.append("\n");
      }
            
      return getCompressedData(dat.toString().getBytes());
    }
    
    return new byte[0];
  }
  
  private String getSortNumber(Channel ch) {
    String result = null;
    
    try {
      Method getSortNumber = ch.getClass().getMethod("getSortNumber", new Class<?>[0]);
      Object value = getSortNumber.invoke(ch, new Object[0]);
      
      if(value != null && value instanceof String && value.toString().trim().length() > 0) {
        result = (String)value;
      }
    } catch (SecurityException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (NoSuchMethodException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (IllegalArgumentException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (IllegalAccessException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (InvocationTargetException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    
    return result;
  }
  
  private byte[] getCompressedData(byte[] uncompressed) {
    ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
    
    try {
      GZIPOutputStream out = new GZIPOutputStream(bytesOut);
      
      // SEND THE IMAGE
      int index = 0;
      int size = 1024;
      do {
          if ((index + size) > uncompressed.length) {
              size = uncompressed.length - index;
          }
          out.write(uncompressed, index, size);
          index += size;
      } while (index < uncompressed.length);
      
      out.flush();
      out.close();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    
    return bytesOut.toByteArray();
  }
  
  @Override
  public ThemeIcon getMarkIconFromTheme() {
    return mIcon;
  }
  
  @Override
  public boolean canUseProgramTree() {
    return true;
  }
  
  private void updateTree() {
    PluginTreeNode root = getRootNode();
    
    root.removeAllChildren();
    
    for(Program prog : mBackSyncedPrograms) {
      root.addProgram(prog);
    }
    
    root.update();
  }
  
  private String[] download(String address, boolean info, boolean showUserdataInput) {
    String car = mProperties.getProperty(CAR_KEY,"");
    String bicycle = mProperties.getProperty(BICYCLE_KEY,"");
    String[] result = null;
    
    boolean backSync = address.equals(BACK_SYNC_ADDRESS);
    boolean channels = address.equals(CHANNEL_DOWN_SYNC_ADDRESS);
    
    if(showUserdataInput && (car.trim().length() == 0 || bicycle.trim().length() == 0)) {
      final UserPanel userPanel = new UserPanel(car, bicycle, false);
      
      Object[] message = new Object[] {mLocalizer.msg("userDataInfo", "To use the synchronization, you must enter your user data first.\n"),userPanel};
      
      if(JOptionPane.showConfirmDialog(UiUtilities.getLastModalChildOf(getParentFrame()), message, mLocalizer.msg("noUserData", "AndroidSync: User data missing"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE) == JOptionPane.OK_OPTION) {
        car = userPanel.getCar();
        bicycle = userPanel.getBicycle();
        
        mProperties.setProperty(CAR_KEY, car);
        mProperties.setProperty(BICYCLE_KEY, bicycle);
      }
    }
    
    if(car.trim().length() != 0 && bicycle.trim().length() != 0) {
      URLConnection conn = null;
      BufferedReader read = null;

      try {
          URL url = new URL(address);
          System.out.println("url:" + url);
          conn = url.openConnection();
          
          String getmethere = car.trim() + ":" + bicycle.trim();
          
          conn.setRequestProperty  ("Authorization", "Basic " + new String(Base64.encodeBase64(getmethere.getBytes())));
          
          read = new BufferedReader(new InputStreamReader(IOUtilities.openSaveGZipInputStream(conn.getInputStream()),"UTF-8"));
          
          String dateValue = read.readLine();
          
          SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
          java.util.Date syncDate = dateFormat.parse(dateValue.trim());
          System.out.println(syncDate);
          if(syncDate.getTime() > System.currentTimeMillis()) {
            if(backSync) {
              for(int i = mBackSyncedPrograms.size()-1; i >= 0; i--) {
                mBackSyncedPrograms.remove(i).unmark(this);
              }
            }
            
            String line = null;
            
            Channel[] subscribedChannels = getPluginManager().getSubscribedChannels();
            
            HashMap<String, TimeZone> timeZoneMap = new HashMap<String, TimeZone>();
            
            ArrayList<Program> newReminders = new ArrayList<Program>();
            ArrayList<String> channelList = new ArrayList<String>();
            
            while((line = read.readLine()) != null) {
              if(line.trim().length() > 0) {
                if(channels) {
                  String[] parts = line.split(":");
                  
                  if(parts[0].equals("1")) {
                    parts[0] = "tvbrowserdataservice"; 
                  }
                  else if(parts[0].equals("2")) {
                    parts[0] = "epgdonatedata";
                  }
                  
                  String channelValue = "";
                  
                  for(int i = 0; i < parts.length-1; i++) {
                    channelValue += parts[i]+":";
                  }
                  
                  channelValue += parts[parts.length-1];
                  
                  channelList.add(channelValue);
                }
                else {
                  String[] parts = line.split(";");
                                  
                  if(parts[1].startsWith("1:")) {
                    parts[1] = "tvbrowserdataservice.TvBrowserDataService" + parts[1].substring(1);
                  }
                  else if(parts[1].startsWith("2:")) {
                    parts[1] = "epgdonatedata.EPGdonateData_group.epgdonate" + parts[1].substring(1);
                  }
                  
                  String id = parts[1].replace(":", "_");
                  
                  TimeZone timeZone = timeZoneMap.get(id);
                  
                  if(timeZone == null) {
                    for(Channel ch : subscribedChannels) {
                      if(ch.getUniqueId().equals(id)) {
                        timeZone = ch.getTimeZone();
                        
                        if(timeZone.getID().equals("GMT")) {
                          timeZone = TimeZone.getTimeZone("WET");
                        }
                        else if(timeZone.getID().equals("GMT+01:00")) {
                          timeZone = TimeZone.getTimeZone("CET");
                        }
                        
                        timeZoneMap.put(id, timeZone);
                        
                        break;
                      }
                    }
                  }
                  
                  if(timeZone != null) {
                    Calendar cal = Calendar.getInstance(timeZone);
                    
                    if(timeZone.getID().equals("UTC")) {
                      cal = Calendar.getInstance();
                    }
                    
                    cal.setTimeInMillis(Long.parseLong(parts[0]) * 60000);
                    Date date = new Date(cal);
                    
                    id += "_" + cal.get(Calendar.HOUR_OF_DAY) + ":" + cal.get(Calendar.MINUTE) + ":" + cal.getTimeZone().getRawOffset()/60000;
                    
                    Program prog = getPluginManager().getProgram(date,id);
                    
                    if(prog != null) {
                      if(backSync && !mBackSyncedPrograms.contains(prog)) {
                        mBackSyncedPrograms.add(prog);
                        prog.mark(this);
                      }
                      else if(!backSync) {
                        if(!mRemovedReminders.contains(prog)) {
                          Marker[] test = prog.getMarkerArr();
                          
                          boolean found = false;
                          
                          for(Marker mark : test) {
                            if(mark.getId().equals("reminderplugin.ReminderPlugin")) {
                              found = true;
                              break;
                            }
                          }
                          
                          if(!found) {
                            newReminders.add(prog);
                          }
                        }
                      }
                    }
                  }
                }
              }
            }
            
            if(channels) {
              result = channelList.toArray(new String[channelList.size()]);
            }
            
            System.out.println(line);
            
            if(!newReminders.isEmpty()) {
              ReminderPlugin.getInstance().addPrograms(newReminders.toArray(new Program[newReminders.size()]));
              
              if(info) {
                JOptionPane.showMessageDialog(getParentFrame(), mLocalizer.msg("successBackReminder", "Successfully updated Reminders."), mLocalizer.msg("successTitle", "Success"), JOptionPane.INFORMATION_MESSAGE);
              }
            }
            else if(!backSync && info) {
              JOptionPane.showMessageDialog(getParentFrame(), mLocalizer.msg("noBackReminder", " No new Reminders were found."), mLocalizer.msg("successTitle", "Success"), JOptionPane.INFORMATION_MESSAGE);
            }
          }
          
          read.close();
      }catch(Exception e) {
        e.printStackTrace();
      } finally {
        System.out.println("Close connection");
        try {
          read.close();
        } catch (Exception e) { }
      }
    }
    
    updateTree();
        
    return result;
  }
  
  private void upload(String address, boolean info) {
    String car = mProperties.getProperty(CAR_KEY);
    String bicycle = mProperties.getProperty(BICYCLE_KEY);
    
    if(car != null && car.trim().length() > 0 && bicycle != null && bicycle.trim().length() > 0) {
      URLConnection conn = null;
      OutputStream os = null;
      InputStream is = null;

      try {
          URL url = new URL(address);
          System.out.println("url:" + url);
          conn = url.openConnection();
          
          String getmethere = car.trim() + ":" + bicycle.trim();
          
          conn.setRequestProperty  ("Authorization", "Basic " + new String(Base64.encodeBase64(getmethere.getBytes())));
          
          conn.setDoOutput(true);

          String postData = "";
          
          byte[] xmlData = getXmlBytes(address);
          
          String message1 = "";
          message1 += "-----------------------------4664151417711" + CrLf;
          message1 += "Content-Disposition: form-data; name=\"uploadedfile\"; filename=\""+car+".gz\""
                  + CrLf;
          message1 += "Content-Type: text/plain" + CrLf;
          message1 += CrLf;

          // the image is sent between the messages in the multipart message.

          String message2 = "";
          message2 += CrLf + "-----------------------------4664151417711--"
                  + CrLf;

          conn.setRequestProperty("Content-Type",
                  "multipart/form-data; boundary=---------------------------4664151417711");
          // might not need to specify the content-length when sending chunked
          // data.
          conn.setRequestProperty("Content-Length", String.valueOf((message1
                  .length() + message2.length() + xmlData.length)));

          System.out.println("open os");
          os = conn.getOutputStream();

          System.out.println(message1);
          os.write(message1.getBytes());
          
          // SEND THE IMAGE
          int index = 0;
          int size = 1024;
          do {
              System.out.println("write:" + index);
              if ((index + size) > xmlData.length) {
                  size = xmlData.length - index;
              }
              os.write(xmlData, index, size);
              index += size;
          } while (index < xmlData.length);
          
          System.out.println("written:" + index);

          System.out.println(message2);
          os.write(message2.getBytes());
          os.flush();

          System.out.println("open is");
          is = conn.getInputStream();

          char buff = 512;
          int len;
          byte[] data = new byte[buff];
          do {
              System.out.println("READ");
              len = is.read(data);

              if (len > 0) {
                  System.out.println(new String(data, 0, len));
              }
          } while (len > 0);

          System.out.println("DONE");

          Object message = mLocalizer.msg("success", "The data were send successfully.");
          String title = mLocalizer.msg("successTitle", "Success");
          
          if(address != null && address.equals(CHANNEL_UP_SYNC_ADDRESS)) {
            if(mNotSynchronizedChannels != null) {
              title = mLocalizer.msg("partlySuccessTitle", "Not all channels were synchronized");
              message = mLocalizer.msg("partlySuccessMessage", "The following channel could not be\nsynchronized, because the data plugins\nof that channels are not supported\nin the TV-Browser Android app:");
              
              DefaultListModel model =  new DefaultListModel();
              
              StringBuilder plugins = new StringBuilder();
              
              for(Channel ch : mNotSynchronizedChannels) {
                model.addElement(ch);
                
                String dataServiceName = getPluginManager().getDataServiceProxy(ch.getDataServiceId()).getInfo().getName();
                
                if(!plugins.toString().contains(dataServiceName)) {
                  if(plugins.length() > 0) {
                    plugins.append(";");
                  }
                  
                  plugins.append(dataServiceName);
                }
              }
              
              JList notSynchronized = new JList(model);
              notSynchronized.setCellRenderer(new ChannelListCellRenderer(true, true));
              notSynchronized.setPreferredSize(new Dimension(100, 80));

              JScrollPane pane = new JScrollPane(notSynchronized);
              pane.setPreferredSize(new Dimension(100,100));
              
              message = new Object[] {message, pane, mLocalizer.msg("partlySuccessDataService", "These channels are provided by\nthe following data-plugins:") , plugins.toString().split(";")};
            }
            
            mUsedChannelArr = getPluginManager().getSubscribedChannels();
            saveMe();
          }
          
          if(info) {
            JOptionPane.showMessageDialog(getParentFrame(), message, title, JOptionPane.INFORMATION_MESSAGE);
          }
      } catch (Exception e) {
        int response = 0;
        
        if(conn != null) {
          try {
            response = ((HttpURLConnection)conn).getResponseCode();
          } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
          }
        }
        if(info) {
          switch (response) {
            case 404: JOptionPane.showMessageDialog(getParentFrame(), mLocalizer.msg("userError", "Username or password were not accepted. Please check them."), mLocalizer.msg("serverError", "Error in server connection"), JOptionPane.ERROR_MESSAGE);break;
            case 415: JOptionPane.showMessageDialog(getParentFrame(), mLocalizer.msg("wrongFileError", "Server didn't accepted upload data. This should not happen. Please contact TV-Browser team."), mLocalizer.msg("serverError", "Error in server connection"), JOptionPane.ERROR_MESSAGE);break;
            case 500: JOptionPane.showMessageDialog(getParentFrame(), mLocalizer.msg("serverFileError", "Server could not store data. Please try again, if this continues please contact TV-Browser team."), mLocalizer.msg("serverError", "Error in server connection"), JOptionPane.ERROR_MESSAGE);break;
            
            default: JOptionPane.showMessageDialog(getParentFrame(), mLocalizer.msg("unknowError", "Something went wrong with the connection to the server. Reason unknown."), mLocalizer.msg("serverError", "Error in server connection"), JOptionPane.ERROR_MESSAGE);break;
          }
        }
          e.printStackTrace();
      } finally {
          System.out.println("Close connection");
          try {
              os.close();
          } catch (Exception e) {
          }
          try {
              is.close();
          } catch (Exception e) {
          }
          try {

          } catch (Exception e) {
          }
      }
    }
    else {
      JOptionPane.showMessageDialog(getParentFrame(), mLocalizer.msg("setupFirst", "You have to enter user name and password first."), mLocalizer.msg("noUser", "No user name and/or password"), JOptionPane.ERROR_MESSAGE);
    }
  }
  
  @Override
  public PluginsProgramFilter[] getAvailableFilter() {
    return mFilter;
  }
  
  public static String getPluginType() {
    return Plugin.OTHER_CATEGORY;
  }
  
  @Override
  public PluginCommunication getCommunicationClass() {
    return mCommunication;
  }
  
  String[] getStoredChannels() {
    return download(CHANNEL_DOWN_SYNC_ADDRESS, false, true);
  }
  
  private class UserPanel extends JPanel {
    private JTextField mCar;
    private JPasswordField mBicycle;
    
    UserPanel(String car, String bicycle, boolean showCreationLink) {
      mCar = new JTextField(car);
      mBicycle = new JPasswordField(bicycle);
      
      EnhancedPanelBuilder pb = new EnhancedPanelBuilder(new FormLayout("5dlu,default,5dlu,50dlu:grow"),this);
      
      pb.addRow(false);
      
      pb.addLabel(mLocalizer.msg("car", "User name:"), CC.xy(2, pb.getRowCount()));
      pb.add(mCar, CC.xy(4, pb.getRowCount()));
      
      pb.addRow("1dlu", false);
      pb.addRow(false);
      
      pb.addLabel(mLocalizer.msg("bicycle", "Password:"), CC.xy(2, pb.getRowCount()));
      pb.add(mBicycle, CC.xy(4, pb.getRowCount()));
      
      if(showCreationLink) {
        JEditorPane linkToWeb = UiUtilities.createHtmlHelpTextArea(mLocalizer.msg("createUserData", "<html>Create new user account: <a href=\"http://android.tvbrowser.org/index.php?id=createaccount\">http://android.tvbrowser.org/index.php?id=createaccount</a></html>"));
        
        pb.addRow("3dlu",false);
        pb.addRow(false);
        pb.add(linkToWeb, CC.xyw(2, pb.getRowCount(), 3));
      }
      
    }
    
    String getCar() {
      return mCar.getText().trim();
    }
    
    String getBicycle() {
      return new String(mBicycle.getPassword()).trim();
    }
  }
}
