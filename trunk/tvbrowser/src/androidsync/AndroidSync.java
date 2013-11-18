package androidsync;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Properties;
import java.util.zip.GZIPOutputStream;

import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import org.apache.commons.codec.binary.Base64;

import tvdataservice.MarkedProgramsList;
import util.browserlauncher.Launch;
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
import devplugin.PluginInfo;
import devplugin.Program;
import devplugin.ProgramFilter;
import devplugin.SettingsTab;
import devplugin.Version;

public class AndroidSync extends Plugin {
  private static final Localizer mLocalizer = Localizer.getLocalizerFor(AndroidSync.class);
  
  //private static final String FAVORITE_SYNC_ADDRESS = "http://android.tvbrowser.org/webtest/android-tvb/data/scripts/syncMyFavorites.php";
  //private static final String CHANNEL_SYNC_ADDRESS = "http://android.tvbrowser.org/webtest/android-tvb/data/scripts/syncMyChannels.php";
  
  private static final String FAVORITE_SYNC_ADDRESS = "http://android.tvbrowser.org/syncMyFavorites.php";
  private static final String CHANNEL_SYNC_ADDRESS = "http://android.tvbrowser.org/syncMyChannels.php";
  
  private static final String LAST_UPLOAD = "LAST_UPLOAD";
  private static final String SELECTED_PLUGINS = "SELECTED_PLUGINS";
  private static final String SELECTED_INTERNAL_PLUGINS = "SELECTED_INTERNAL_PLUGINS";
  private static final String SELECTED_FILTER = "SELECTED_FILTER";
  private static final String TYPE = "TYPE";
  
  private static final String PLUGIN_TYPE = "PLUGIN_TYPE";
  private static final String FILTER_TYPE = "FILTER_TYPE";
  
  private static final Version mVersion = new Version(0, 12, 0, false);
  private final String CrLf = "\r\n";
  private Properties mProperties;
  
  private static final String CAR_KEY = "CAR_KEY";
  private static final String BICYCLE_KEY = "BICYCLE_KEY";
  
  public AndroidSync() {
    mProperties = new Properties();
     /*  
    try {
      int[] test = new int[] {5000,5002,5003,5006,5016};
      int value = IOUtilities.encodeMultipleEpisodeNumersToSingleFieldValue(test);
      System.out.println(Integer.toBinaryString(value));
      System.out.println(IOUtilities.decodeSingleFieldValueToMultipleEpisodeString(value));
      
    } catch (UnsupportedDataTypeException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    */
  }
  /*
  @Override
  public void handleTvDataAdded(MutableChannelDayProgram newProg) {
    Iterator<Program> progs = newProg.getPrograms();
    
    while(progs.hasNext()) {
      MutableProgram prog = (MutableProgram)progs.next();
      
      if(prog.hasFieldValue(ProgramFieldType.EPISODE_NUMBER_TYPE)) {
        int[] test = new int[(int)(Math.random() * 9) + 1];
        
        int first = prog.getIntField(ProgramFieldType.EPISODE_NUMBER_TYPE);
        
        test[0] = first;
        
        for(int i=1; i < test.length; i++) {
          test[i] = test[i-1] + (int)(Math.random() * 4);
        }
        
        try {
          prog.setIntField(ProgramFieldType.EPISODE_NUMBER_TYPE, IOUtilities.encodeMultipleEpisodeNumersToSingleFieldValue(test));
        } catch (UnsupportedDataTypeException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }
    }
    
    super.handleTvDataAdded(newProg);
  }
  */
  public static Version getVersion() {
    return mVersion;
  }
  
  @Override
  public void handleTvBrowserStartFinished() {
    if(mProperties.getProperty(CAR_KEY,"").trim().length() == 0 || mProperties.getProperty(BICYCLE_KEY,"").trim().length() == 0) {
      String[] options = {
          mLocalizer.msg("enterNow", "Enter user data"),
          mLocalizer.msg("createNew", "Create new user data"),
          Localizer.getLocalization(Localizer.I18N_CANCEL)
      };
      
      int selected = JOptionPane.showOptionDialog(UiUtilities.getLastModalChildOf(getParentFrame()), mLocalizer.msg("notSetup", "No user data found for synchronization of TV-Browser for Android.\n\nDo you want to enter them now or do you want to create new user data (Internet access needed)?"), getInfo().getName() + ": " + mLocalizer.msg("notSetupTitle", "No user data found"), JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, createImageIcon("apps", "android_robot", 22), options, options[0]);
      
      if(selected == JOptionPane.YES_OPTION) {
        getPluginManager().showSettings(this);
      }
      else if(selected == JOptionPane.NO_OPTION) {
        new Thread() {
          public void run() {
            try {
              sleep(1000);
            } catch (InterruptedException e) {}
            
            Launch.openURL("http://android.tvbrowser.org");
          }
        }.start();
        
        getPluginManager().showSettings(this);
      }
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
    
    ContextMenuAction channels = new ContextMenuAction(mLocalizer.msg("syncChannels", "Export my subscribed EPGfree channels to TV-Browser server"),createImageIcon("actions","export-channels",16));
    channels.setActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        new Thread() {
          public void run() {
            upload(CHANNEL_SYNC_ADDRESS,true);
          }
        }.start();
      }
    });
    channels.putValue(Plugin.BIG_ICON, createImageIcon("actions","export-channels",22));
    
    ActionMenu menu = new ActionMenu(getInfo().getName(), createImageIcon("apps","android_robot",16), new ContextMenuAction[] {action,channels});
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
    }
  }
  
  @Override
  public SettingsTab getSettingsTab() {
    return new SettingsTab() {
      private JTextField mCar;
      private JPasswordField mBicycle;
      private SelectableItemList mPluginSelection;
      private JRadioButton mPluginType;
      private JRadioButton mFilterType;
      private JComboBox mFilterSelection;
      
      @Override
      public void saveSettings() {
        String oldCar = mProperties.getProperty(CAR_KEY, "");
        String oldBicycle = mProperties.getProperty(BICYCLE_KEY, "");
        
        mProperties.setProperty(CAR_KEY, mCar.getText().trim());
        mProperties.setProperty(BICYCLE_KEY, new String(mBicycle.getPassword()).trim());
        
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
            else {
              if(internal.length() > 0) {
                internal.append(";");
              }
              
              internal.append("reminderplugin.ReminderPlugin");
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
            upload(CHANNEL_SYNC_ADDRESS, true);
          }
        }
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
            "default,3dlu,default,3dlu,default,10dlu,default,5dlu,default,default,3dlu,default,default,fill:10dlu:grow,default"));
        
        pb.border(Borders.createEmptyBorder("5dlu,0dlu,0dlu,0dlu"));
        
        mCar = new JTextField(mProperties.getProperty(CAR_KEY,""));
        mBicycle = new JPasswordField(mProperties.getProperty(BICYCLE_KEY,""));
        
        JEditorPane linkToWeb = UiUtilities.createHtmlHelpTextArea(mLocalizer.msg("createUserData", "<html>Create new user account: <a href=\"http://android.tvbrowser.org\">http://android.tvbrowser.or</a></html>"));
        
        String[] selectionIDs = mProperties.getProperty(SELECTED_PLUGINS, "").split(";");
        
        ArrayList<Object> selectedPlugins = new ArrayList<Object>();
        ArrayList<Object> allPlugins = new ArrayList<Object>();
        
        if(mProperties.getProperty(SELECTED_INTERNAL_PLUGINS, "favoritesplugin.FavoritesPlugin").contains("favoritesplugin.FavoritesPlugin")) {
          selectedPlugins.add(mLocalizer.msg("favorites", "Favorites"));
        }
        if(mProperties.getProperty(SELECTED_INTERNAL_PLUGINS, "favoritesplugin.FavoritesPlugin").contains("reminderplugin.ReminderPlugin")) {
          selectedPlugins.add(mLocalizer.msg("reminders", "Reminders"));
        }
        
        allPlugins.add(mLocalizer.msg("favorites", "Favorites"));
        allPlugins.add(mLocalizer.msg("reminders", "Reminders"));
        
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
        
        pb.addLabel(mLocalizer.msg("car", "User name:"), CC.xyw(2, 1, 3));
        pb.add(mCar, CC.xy(5, 1));
        pb.addLabel(mLocalizer.msg("bicycle", "Password:"), CC.xyw(2, 3, 3));
        pb.add(mBicycle, CC.xy(5, 3));
        pb.add(linkToWeb, CC.xyw(2, 5, 4));
        
        pb.addSeparator(mLocalizer.msg("exportPlugins", "Export programs of"), CC.xyw(1, 7, 5));
        pb.add(mPluginType, CC.xyw(2, 9, 4));
        pb.add(mPluginSelection, CC.xyw(3, 10, 3));
        pb.add(mFilterType, CC.xyw(2, 12, 4));
        pb.add(mFilterSelection, CC.xyw(3, 13, 3));
        
        pb.add(UiUtilities.createHtmlHelpTextArea("The Android robot is reproduced or modified from work created and shared by Google and used according to terms described in the Creative Commons 3.0 Attribution License."), CC.xyw(2, 15, 4));
        
        
        return pb.getPanel();
      }
    };
  }
  
  @Override
  public void loadSettings(Properties settings) {
    mProperties = settings;
  }
  
  @Override
  public Properties storeSettings() {
    return mProperties;
  }
  
  private byte[] getXmlBytes(String address) {
    if(address.equals(FAVORITE_SYNC_ADDRESS)) {
      StringBuilder dat = new StringBuilder();
      
      Program[] programs = MarkedProgramsList.getInstance().getMarkedPrograms();
      
      Date compare = Date.getCurrentDate().addDays(14);
      
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
            
            if(((pluginType && (internal.contains(mark.getId()) || plugins.contains(mark.getId()))) || (!pluginType && filter.accept(prog)))
                && prog.getChannel().getDataServicePackageName().equals("tvbrowserdataservice")) {
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
        System.out.println(prog);
        dat.append(time);
        dat.append(";");
        
        if(prog.getChannel().getDataServicePackageName().equals("tvbrowserdataservice")) {
          dat.append("1");
        }
        else {
          dat.append(prog.getChannel().getDataServicePackageName());
        }
        
        dat.append(":");
        dat.append(prog.getChannel().getGroup().getId());
        dat.append(":");
        dat.append(prog.getChannel().getId());
        dat.append("\n");
      }
            
      return getCompressedData(dat.toString().getBytes());
    }
    else if(address.equals(CHANNEL_SYNC_ADDRESS)) {
      StringBuilder channels = new StringBuilder();
      
      Channel[] subscribed = getPluginManager().getSubscribedChannels();
      
      for(Channel ch : subscribed) {
        if(ch.getDataServicePackageName().equals("tvbrowserdataservice")) {
          channels.append("1:");
          channels.append(ch.getGroup().getId());
          channels.append(":");
          channels.append(ch.getId());
          channels.append("\n");
        }
      }
      
      return getCompressedData(channels.toString().getBytes());
    }
    
    return new byte[0];
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
  
  public void upload(String address, boolean info) {
    String car = mProperties.getProperty(CAR_KEY);
    String bicycle = mProperties.getProperty(BICYCLE_KEY);
    
    if(car != null && bicycle != null) {
      URLConnection conn = null;
      OutputStream os = null;
      InputStream is = null;

      try {
          URL url = new URL(address);
          System.out.println("url:" + url);
          conn = url.openConnection();
          
          String getmethere = car.trim() + ":" + bicycle.trim();
          System.out.println(getmethere);
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
          
          if(info) {
            JOptionPane.showMessageDialog(getParentFrame(), mLocalizer.msg("success", "The data were send successfully."), mLocalizer.msg("successTitle", "Success"), JOptionPane.INFORMATION_MESSAGE);
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
}
