/*
 * VirtualDataService by Reinhard Lehrbaum
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
package virtualdataservice;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import tvdataservice.MutableChannelDayProgram;
import tvdataservice.MutableProgram;
import tvdataservice.SettingsPanel;
import tvdataservice.TvDataUpdateManager;
import util.exc.TvBrowserException;
import util.ui.Localizer;
import util.ui.UiUtilities;
import virtualdataservice.ui.ContextDialog;
import virtualdataservice.virtual.VirtualChannel;
import virtualdataservice.virtual.VirtualChannelManager;
import virtualdataservice.virtual.VirtualProgram;
import devplugin.AbstractTvDataService;
import devplugin.ActionMenu;
import devplugin.Channel;
import devplugin.ChannelGroup;
import devplugin.ContextMenuSeparatorAction;
import devplugin.Date;
import devplugin.Plugin;
import devplugin.PluginInfo;
import devplugin.Program;
import devplugin.ProgressMonitor;
import devplugin.Version;

public class VirtualDataService extends AbstractTvDataService
{
  private static final Localizer mLocalizer = Localizer.getLocalizerFor(VirtualDataService.class);
  private static java.util.logging.Logger mLog = java.util.logging.Logger.getLogger(VirtualDataService.class.getName());

  private static VirtualDataService mInstance = null;

  private static Properties mProperties;

  private ChannelGroup mVirtualChannelGroup = new VirtualChannelGroup("Virtual", "virtual", mLocalizer.msg("desc", "Virtual"), "Virtual");
  private ArrayList<Channel> mChannels = null;
  private File mWorkingDir;
  
//modified by jb:
  final private ImageIcon vcIcon = DummyPlugin.getInstance().createImageIcon("emblems", "emblem-symbolic-link", 16);
  final private ImageIcon copyIcon = DummyPlugin.getInstance().createImageIcon("emblems", "go-next", 16);
  final private ImageIcon delIcon = DummyPlugin.getInstance().createImageIcon("actions", "edit-delete", 16);
  final private ImageIcon editIcon = DummyPlugin.getInstance().createImageIcon("actions", "document-edit", 16);
  final private ImageIcon newIcon = DummyPlugin.getInstance().createImageIcon("actions", "document-new", 16);
//modified by jb //

  public VirtualDataService()
  {
    mInstance = this;
  }

  public static VirtualDataService getInstance()
  {
    return mInstance;
  }

  public ChannelGroup[] checkForAvailableChannelGroups(
      final ProgressMonitor monitor) throws TvBrowserException
  {
    return new ChannelGroup[] { mVirtualChannelGroup };
  }

  public Channel[] checkForAvailableChannels(final ChannelGroup group,
      final ProgressMonitor monitor) throws TvBrowserException
  {
    monitor.setMessage(mLocalizer.msg("loading", "Loading virtual data"));

    return getAvailableChannels(group);
  }

  public Channel[] getAvailableChannels(final ChannelGroup group)
  {
    if (mChannels == null)
    {
      mChannels = new ArrayList<Channel>();
      final VirtualChannelManager manager = new VirtualChannelManager(
          mWorkingDir.getAbsolutePath());
      for (VirtualChannel channel : manager.getChannels())
      {
        final Channel ch = new Channel(this, channel.getName(), "VC"
            + Integer.toString(channel.getID()), TimeZone.getDefault(), "xc",
            "", "", mVirtualChannelGroup);
        mChannels.add(ch);
      }
    }
    return mChannels.toArray(new Channel[mChannels.size()]);
  }

  public ChannelGroup[] getAvailableGroups()
  {
    return new ChannelGroup[] { mVirtualChannelGroup };
  }

  public static Version getVersion()
  {
    return new Version(0, 2, 0, false, "0.0.2.0 beta");
  }

  public PluginInfo getInfo()
  {
    return new PluginInfo(VirtualDataService.class, mLocalizer.msg("name", "Virtual Data"), mLocalizer.msg("desc", "Data"), "Reinhard Lehrbaum");
  }

  public SettingsPanel getSettingsPanel()
  {
    return new VirtualDataServiceSettingsPanel(mWorkingDir.getAbsolutePath());
  }

  public boolean hasSettingsPanel()
  {
    return true;
  }

  public void loadSettings(final Properties settings)
  {
    mLog.info("Loading settings in VirtualDataService");

    mProperties = settings;

    mLog.info("Finished loading settings for VirtualDataService");
  }

  public Properties storeSettings()
  {
    mLog.info("Storing settings for VirtualDataService");

    mLog.info("Finished storing settings for VirtualDataService");

    return mProperties;
  }

  public void setWorkingDirectory(final File dataDir)
  {
    mWorkingDir = dataDir;
  }

  public boolean supportsDynamicChannelGroups()
  {
    return false;
  }

  public boolean supportsDynamicChannelList()
  {
    return true;
  }

  public void updateTvData(final TvDataUpdateManager updateManager,
      final Channel[] channelArr, final Date startDate, final int dateCount,
      final ProgressMonitor monitor) throws TvBrowserException
  {
    final VirtualChannelManager manager = new VirtualChannelManager(mWorkingDir
        .getAbsolutePath());
    for (Channel channel : channelArr)
    {
      try
      {
        final VirtualChannel vCh = manager.getChannel(Integer.parseInt(channel
            .getId().substring(2)));
        final Calendar cal = startDate.getCalendar();
 
// modified by jb:
        int prevEnd = 0;
        int actStart;
        for (int i = 0; i < dateCount; i++)
        {
          final Date d = new Date(cal);
          final MutableChannelDayProgram dayProgram = new MutableChannelDayProgram(
              d, channel);

          for (VirtualProgram program : vCh.getPrograms())
          {
            if (program.isDayProgram(cal))
            {
              actStart = (program.getStart().get(Calendar.HOUR_OF_DAY)* 60)+ program.getStart().get(Calendar.MINUTE);
              if (actStart > prevEnd){
                final int dummyPrevStartHH = prevEnd / 60;
                final MutableProgram dummyPrev = new MutableProgram(channel, d,
                    dummyPrevStartHH, prevEnd - (dummyPrevStartHH * 60), false);
                dummyPrev.setTitle(" ");
                dummyPrev.setLength(actStart-prevEnd);
                dayProgram.addProgram(dummyPrev);
              }
 
              final MutableProgram p = new MutableProgram(channel, d, program
                  .getStart().get(Calendar.HOUR_OF_DAY), program.getStart()
                  .get(Calendar.MINUTE), false);
              p.setTitle(program.getTitle());
              p.setLength(program.getLength());
              dayProgram.addProgram(p);
              prevEnd = actStart + program.getLength();
            }
         }

          if (dayProgram.getProgramCount() == 0){
            final int dummyStartHH = prevEnd / 60;
            final MutableProgram dummy = new MutableProgram(channel, d,
                dummyStartHH, prevEnd - (dummyStartHH * 60), false);
            dummy.setTitle(" ");
            dummy.setLength(1439-prevEnd);
            dayProgram.addProgram(dummy);

            prevEnd = 0;
            }
          else
          {
            if (prevEnd >= 1440){
              prevEnd = prevEnd-1440;
            }
            else{
              final int dummyEndStartHH = prevEnd / 60;
              final MutableProgram dummyEnd = new MutableProgram(channel, d,
                  dummyEndStartHH, prevEnd - (dummyEndStartHH * 60), false);
              dummyEnd.setTitle(" ");
              dummyEnd.setLength(1440 - prevEnd);
              dayProgram.addProgram(dummyEnd);
              prevEnd = 0;
            }
          }
          
          
// modified by jb //
          updateManager.updateDayProgram(dayProgram);
          
          cal.add(Calendar.DAY_OF_MONTH, 1);
        }
      }
      catch (Exception ex)
      {mLog.warning("while updating " + channel.getName() + ": "+ ex.toString());}
    }
  }

  public void clearChannelList()
  {
    if (mChannels != null)
    {
      mChannels.clear();
      mChannels = null;
    }

   }

// added by jb :
  
  public ActionMenu getContextMenuActions(final Program program){

    final VirtualChannelManager vcm = new VirtualChannelManager(mWorkingDir
        .getAbsolutePath());
    final List<VirtualChannel> channels = vcm.getChannels();
    
    Collections.sort(channels);
    final ArrayList<AbstractAction> actionList = new ArrayList<AbstractAction>();
    
    VirtualChannel channel = null;
    boolean isValidVcProgram = false;

    for (final VirtualChannel vcn : channels){
      if (program.getChannel().getId().equals("VC" + vcn.getID())){
        channel = vcn;
        for (VirtualProgram vProg : vcn.getPrograms()){
          final Calendar cal = getProgramStart(program);
          if (vProg.getTitle().equals(program.getTitle()) && vProg.getStart().get(Calendar.HOUR_OF_DAY) == cal.get(Calendar.HOUR_OF_DAY) && vProg.getStart().get(Calendar.MINUTE) == cal.get(Calendar.MINUTE) && vProg.getLength()== program.getLength()) {
            isValidVcProgram = true;
          }
          
        }
      }
    }
    
    if (channel != null){
      
      final VirtualChannel fChannel = channel;

      if (isValidVcProgram) {
        final AbstractAction delAction = new AbstractAction() {
          private static final long serialVersionUID = 1L;

          public void actionPerformed(final ActionEvent evt) {
            delProgram(fChannel, program, false);
          }
        };
        delAction.putValue(Action.NAME, Localizer
            .getLocalization(Localizer.I18N_DELETE));
        delAction.putValue(Action.SMALL_ICON, delIcon);
        actionList.add(delAction);
        final AbstractAction editAction = new AbstractAction() {
          private static final long serialVersionUID = 1L;

          public void actionPerformed(final ActionEvent evt) {
            editProgram(fChannel, program);
          }
        };
        editAction.putValue(Action.NAME, Localizer
            .getLocalization(Localizer.I18N_EDIT));
        editAction.putValue(Action.SMALL_ICON, editIcon);
        actionList.add(editAction);
        
        actionList.add(ContextMenuSeparatorAction.getInstance());
      }

      final AbstractAction addAction = new AbstractAction() {
         private static final long serialVersionUID = 1L;
          public void actionPerformed(final ActionEvent evt) {
            addProgram (fChannel, program);
          }
        };
        addAction.putValue(Action.NAME, mLocalizer.msg("new", "Add Program"));
        addAction.putValue(Action.SMALL_ICON, newIcon);
        actionList.add(addAction);

       if (isValidVcProgram) {
        actionList.add(ContextMenuSeparatorAction.getInstance());
      }
      }

    if (isValidVcProgram || channel == null) {
      for (final VirtualChannel vcn : channels) {
        final AbstractAction copyAction = new AbstractAction() {
          private static final long serialVersionUID = 1L;

          public void actionPerformed(final ActionEvent evt) {
            copyProgram(vcn, program);
          }
        };
        copyAction.putValue(Action.NAME, vcn.getName());
        copyAction.putValue(Action.SMALL_ICON, copyIcon);
        actionList.add(copyAction);
      }
    }
    final Action[] actions = new Action[actionList.size()];
    actionList.toArray(actions);
   
    if (actions.length == 0) {
      return null;
     }
    final Action mainaction = new devplugin.ContextMenuAction();
    mainaction.putValue(Action.NAME, mLocalizer.msg("name", "VirtualDataService"));
    mainaction.putValue(Action.SMALL_ICON, vcIcon);
    return new ActionMenu(mainaction, actions);
    }


  private void delProgram(final VirtualChannel channel, final Program program,
      boolean isConfirmed) {
    {
      final Calendar cal = getProgramStart(program);
      
       {
         final VirtualChannelManager vcm = new VirtualChannelManager(mWorkingDir
            .getAbsolutePath());
        final List<VirtualChannel> vChannels = vcm.getChannels();
         for (VirtualChannel vChannel : vChannels){
           if (vChannel.getID()==channel.getID()){
             for (VirtualProgram vProg : vChannel.getPrograms()) {
              if (vProg.getTitle().equals(program.getTitle()) && vProg.getStart().get(Calendar.HOUR_OF_DAY) == cal.get(Calendar.HOUR_OF_DAY) && vProg.getStart().get(Calendar.MINUTE) == cal.get(Calendar.MINUTE) && vProg.getLength()== program.getLength()) {
                final String[] options = {
                    mLocalizer.msg("delProgram", "Delete Program"),
                    Localizer.getLocalization(Localizer.I18N_CANCEL) };
                final int minutes = cal.get(Calendar.MINUTE);
                 String minutesString;
                 if (minutes<10) {
                  minutesString = ":0" + minutes;
                } else {
                  minutesString = ":" + minutes;
                }
                 if (!isConfirmed) {
                   final String message = mLocalizer.msg("removeProgramWarning",
                      "Are you sure to remove '" + program.getTitle()
                          + "' permanently?", program.getTitle(), cal
                          .get(Calendar.HOUR_OF_DAY)
                          + minutesString, channel.getName());
                  final int confirmation = JOptionPane.showOptionDialog(
                      getParentFrame(), message, mLocalizer.msg("delProgram",
                          "Delete Program"), JOptionPane.YES_NO_OPTION,
                      JOptionPane.ERROR_MESSAGE, null, options, options[1]);
                  if (confirmation == JOptionPane.YES_OPTION) {
                    isConfirmed=true;
                  }
                }
                if (isConfirmed){
                  vChannel.removeProgram(vProg);
                   vcm.save();
                 }
              }
            }
           }
         }
       }
     }
      
  }
  
  private void copyProgram(final VirtualChannel channel, final Program program) {
    handleProgram( channel,  program,  false, false);
  }

  private void editProgram(final VirtualChannel channel, final Program program) {
    handleProgram( channel,  program, true,  false);
  }

  private void addProgram(final VirtualChannel channel, final Program program) {
    handleProgram( channel,  program,  false, true);
  }

  private void handleProgram(final VirtualChannel channel,
      final Program program, final boolean delFlg, final boolean noTitle)
  {
    final ContextDialog editor = ContextDialog
        .getInstance(getParentFrame());
     
    final Calendar calStart = getProgramStart(program);
    final Calendar calEnd = getProgramEnd(program);
     String title;
     if (noTitle){
       title = "";
     }
     else{
       title = getProgramTitle(program, delFlg);
     }
     editor.setFields(title, calStart, calEnd, program.getLength());
 
     editor.setModal(true);
     UiUtilities.centerAndShow(editor);
     final VirtualProgram vProg = editor.getProgram();

     if (vProg != null) {
       final VirtualChannelManager vcm = new VirtualChannelManager(mWorkingDir
          .getAbsolutePath());
      final List<VirtualChannel> vChannels = vcm.getChannels();
        for (VirtualChannel vChannel : vChannels){
          if (vChannel.getID()==channel.getID()){
            vChannel.addProgram(vProg);
            vcm.save();
            if (delFlg) {
              delProgram (channel, program, true);
            }
          }
        }
    }
  }


  private String getProgramTitle(final Program program,
      final boolean noChannelFlg) {
    if (noChannelFlg) {
      return program.getTitle();
    }
    return "("+program.getChannel().getName()+ ") " + program.getTitle();
  }
  
  private Calendar getProgramStart(final Program program) {
    final int hours = program.getStartTime() / 60;
    return getCalendar(program, hours, program.getStartTime() - (hours*60));
  }
  
  
  private Calendar getProgramEnd(final Program program) {
    int endTime = program.getStartTime() + program.getLength();
    final int dayDiff = endTime / 1440;
    endTime = endTime - (dayDiff*1440);
    final int hours = endTime / 60;
    return getCalendar(program, hours, endTime - (hours*60), dayDiff);
  }
  
  private Calendar getCalendar(final Program program, final int hours,
      final int minutes) {
   return getCalendar(program, hours, minutes, 0);
 }

  private Calendar getCalendar(final Program program, final int hours,
      final int minutes, final int dayDiff) {
    final Calendar cal = program.getDate().getCalendar();
    if (dayDiff > 0){
      cal.add(Calendar.DAY_OF_MONTH, dayDiff);
    }
    cal.set(Calendar.HOUR_OF_DAY, hours);
    cal.set(Calendar.MINUTE, minutes);
    cal.set(Calendar.SECOND, 0);
    cal.set(Calendar.MILLISECOND, 0);
    return cal;
 }
  

  }

class DummyPlugin extends Plugin {

  private static Plugin mInstance;

  /** Creates a new instance of dummyPlugin */
  public DummyPlugin() {
    mInstance = this;
  }

  public static Plugin getInstance() {
    if (mInstance == null) {
      mInstance = new DummyPlugin();
    }
    return mInstance;
  }
  
//added by jb //
  
}
