/*
 * IDontWant2See - Plugin for TV-Browser
 * Copyright (C) 2008 René Mach
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
package idontwant2see;

import java.awt.AWTEvent;
import java.awt.Dialog.ModalityType;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.AWTEventListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutput;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPOutputStream;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRootPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

import org.apache.commons.codec.binary.Base64;
//import org.apache.commons.lang3.StringUtils;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import compat.MenuCompat;
import compat.VersionCompat;
import devplugin.ActionMenu;
import devplugin.ContextMenuAction;
import devplugin.Date;
import devplugin.ImportanceValue;
import devplugin.Plugin;
import devplugin.PluginInfo;
import devplugin.PluginsFilterComponent;
import devplugin.PluginsProgramFilter;
import devplugin.Program;
import devplugin.ProgramReceiveTarget;
import devplugin.SettingsTab;
import devplugin.Version;
import util.io.IOUtilities;
import util.ui.Localizer;
import util.ui.TVBrowserIcons;
import util.ui.UiUtilities;
import util.ui.WindowClosingIf;

/**
 * A very simple filter plugin to easily get rid of stupid programs in the
 * program table.
 *
 * @author René Mach
 */
public final class IDontWant2See extends Plugin implements AWTEventListener {
  private static final String DONT_WANT_TO_SEE_SYNC_ADDRESS = "http://android.tvbrowser.org/data/scripts/syncUp.php?type=dontWantToSee";
  private static final String DONT_WANT_TO_SEE_IMPORT_SYNC_ADDRESS = "http://android.tvbrowser.org/data/scripts/syncDown.php?type=dontWantToSee";
  
  private static final boolean PLUGIN_IS_STABLE = true;
  private static final Version PLUGIN_VERSION = new Version(0, 16, 1, PLUGIN_IS_STABLE);

  private static final String RECEIVE_TARGET_EXCLUDE_EXACT = "target_exclude_exact";

  static final Localizer mLocalizer = Localizer
      .getLocalizerFor(IDontWant2See.class);

  private static Date mCurrentDate = Date.getCurrentDate();
  private PluginsProgramFilter mFilter;
  private static IDontWant2See mInstance;
  private boolean mDateWasSet;
  private IDontWant2SeeSettings mSettings;

  private boolean mCtrlPressed;

  private HashMap<Program, Boolean> mMatchCache = new HashMap<Program, Boolean>(
			1000);

  private static final Pattern PATTERN_TITLE_PART = Pattern.compile(
      "(?i)" // case insensitive matching
      + "(.*)" // ignore everything at the beginning
      + "((" // have two alternatives: optional and mandatory brackets
      + "\\(?(" // optional brackets begin
      + "(Teil \\d+)" + "|"
      + "(Teil \\d+/\\d+)" + "|"
      + "(Teil \\d+ von \\d+)" + "|"
      + "(Folge \\d+)" + "|"
      + "(Folge \\d+/\\d+)" + "|"
      + "(Folge \\d+ von \\d+)" + "|"
      + "(Best of)" + "|"
      + "(\\d+/\\d+)" + "|"
      + "(\\sI)" + "|" // 1
      + "(\\sII)" + "|" // 2
      + "(\\sIII)" + "|" // 3
      + "(\\sIV)" + "|" // 4
      + "(\\sV)" // 5
      + ")\\)?" // optional brackets end
      + ")|(" // 2. alternative: mandatory brackets
      + "\\((" // mandatory brackets begin
      + "(Fortsetzung)" + "|"
      + "(\\d+)"
      + ")\\)" // mandatory brackets end
      + "))"
      + "$"); // at the end only

  public static Version getVersion() {
    return PLUGIN_VERSION;
  }

  /**
   * Creates an instance of this plugin.
   */
  public IDontWant2See() {
    mInstance = this;
    mSettings = new IDontWant2SeeSettings();
    mDateWasSet = false;
  }

  static IDontWant2See getInstance() {
    return mInstance;
  }

  public void handleTvDataUpdateFinished() {
    clearCache();
    setCurrentDate();
    mDateWasSet = false;

    for(IDontWant2SeeListEntry entry : mSettings.getSearchList()) {
      entry.resetDateWasSetFlag();
    }
  }

  void clearCache() {
    synchronized (mMatchCache) {
      mMatchCache = new HashMap<Program, Boolean>(1000);  
    }
  }

  private static void setCurrentDate() {
    mCurrentDate = Date.getCurrentDate();
  }
  
  public ImportanceValue getImportanceValueForProgram(Program p) {
    if(!acceptInternal(p)) {
      return new ImportanceValue((byte)1,mSettings.getProgramImportance());
    }
    
    return new ImportanceValue((byte)1,Program.DEFAULT_PROGRAM_IMPORTANCE);
  }
  
  boolean acceptInternal(final Program program) {
    if(program == null) {
      return false;
    }
    else {
      if(!mDateWasSet) {
        mSettings.setLastUsedDate(getCurrentDate());
        mDateWasSet = true;
      }
      
      Boolean result = null;
      
      synchronized (mMatchCache) {
        result = mMatchCache.get(program);;
      }
      
  		if (result != null) {
  			return result;
  		}
  
      // calculate lower case title only once, not for each entry again
      final String title = program.getTitle();
      final String lowerCaseTitle = title.toLowerCase();
      for(IDontWant2SeeListEntry entry : mSettings.getSearchList()) {
        if (entry.matchesProgramTitle(title, lowerCaseTitle)) {
          return putCache(program, false);
        }
      }
  
      return putCache(program, true);
    }
  }

  private boolean putCache(final Program program, final boolean matches) {
    synchronized (mMatchCache) {
      mMatchCache.put(program, matches);
    }
		
		return matches;
	}

	public PluginInfo getInfo() {
		return new PluginInfo(
        IDontWant2See.class,
        mLocalizer.msg("name", "I don't want to see!"),
        mLocalizer
            .msg(
                "desc",
                "Removes all programs with an entered search text in the title from the program table."),
        "Ren\u00e9 Mach", "GPL");
  }

  int getSearchTextIndexForProgram(final Program program) {
    if (program != null) {
      // calculate lower case title only once, not for each entry again
      final String title = program.getTitle();
      final String lowerCaseTitle = title.toLowerCase();
      for(int i = 0; i < mSettings.getSearchList().size(); i++) {
        if (mSettings.getSearchList().get(i).matchesProgramTitle(title, lowerCaseTitle)) {
          return i;
        }
      }
    }

    return -1;
  }

  public ActionMenu getButtonAction() {
    

    final ContextMenuAction openExclusionList = new ContextMenuAction(
        mLocalizer.msg("editExclusionList", "Edit exclusion list"),
        createImageIcon("apps", "idontwant2see", 16));
    openExclusionList.putValue(Plugin.BIG_ICON, createImageIcon("apps","idontwant2see",22));
    openExclusionList.setActionListener(new ActionListener() {
      public void actionPerformed(final ActionEvent e) {
        final Window w = UiUtilities.getLastModalChildOf(getParentFrame());

        JDialog temDlg = null;

        if(w instanceof JDialog) {
          temDlg = new JDialog((JDialog)w, ModalityType.DOCUMENT_MODAL);
        }
        else {
          temDlg = new JDialog((JFrame)w, ModalityType.DOCUMENT_MODAL);
        }

        final JDialog exclusionListDlg = temDlg;
        exclusionListDlg.setTitle(mLocalizer
            .msg("name", "I don't want to see!")
            + " - "
            + mLocalizer.msg("editExclusionList", "Edit exclusion list"));

        UiUtilities.registerForClosing(new WindowClosingIf() {
          public void close() {
            exclusionListDlg.dispose();
          }

          public JRootPane getRootPane() {
            return exclusionListDlg.getRootPane();
          }
        });

        final ExclusionTablePanel exclusionPanel = new ExclusionTablePanel(mSettings);

        final JButton ok = new JButton(Localizer
            .getLocalization(Localizer.I18N_OK));
        ok.addActionListener(new ActionListener() {
          public void actionPerformed(final ActionEvent e) {
            try {
            exclusionPanel.saveSettings(mSettings);
            exclusionListDlg.dispose();
            }catch(Throwable t) {
              t.printStackTrace();
            }
          }
        });

        final JButton cancel = new JButton(Localizer
            .getLocalization(Localizer.I18N_CANCEL));
        cancel.addActionListener(new ActionListener() {
          public void actionPerformed(final ActionEvent e) {
            exclusionListDlg.dispose();
          }
        });

        final FormLayout layout = new FormLayout(
            "0dlu:grow,default,3dlu,default",
            "fill:350px:grow,2dlu,default,5dlu,default");
        layout.setColumnGroups(new int[][] {{2,4}});

        final CellConstraints cc = new CellConstraints();
        final PanelBuilder pb = new PanelBuilder(layout,
            (JPanel) exclusionListDlg
            .getContentPane());
        pb.setDefaultDialogBorder();

        pb.add(exclusionPanel, cc.xyw(1,1,4));
        pb.addSeparator("", cc.xyw(1,3,4));
        pb.add(ok, cc.xy(2,5));
        pb.add(cancel, cc.xy(4,5));

        layoutWindow("exclusionListDlg", exclusionListDlg, new Dimension(600,
            450));
        exclusionListDlg.setVisible(true);
      }
    });

    final ContextMenuAction undo = new ContextMenuAction(mLocalizer.msg(
        "undoLastExclusion", "Undo last exclusion"), createImageIcon("actions",
        "edit-undo", 16));
    undo.putValue(Plugin.BIG_ICON, createImageIcon("actions","edit-undo",22));
    undo.setActionListener(new ActionListener() {
      public void actionPerformed(final ActionEvent e) {
        String lastEnteredExclusionString = mSettings.getLastEnteredExclusionString();
        if(lastEnteredExclusionString.length() > 0) {
          for(int i = mSettings.getSearchList().size()-1; i >= 0; i--) {
            if(mSettings.getSearchList().get(i).getSearchText().equals(lastEnteredExclusionString)) {
              mSettings.getSearchList().remove(i);
            }
          }

          mSettings.setLastEnteredExclusionString("");

          updateFilter(true);
        }
      }
    });
    
    ContextMenuAction export = new ContextMenuAction(mLocalizer.msg("menu.export","Export 'I don't want to see!' exclusion list to TV-Browser server"),createImageIcon("apps", "idontwant2see-android", TVBrowserIcons.SIZE_SMALL));
    export.putValue(Plugin.BIG_ICON, createImageIcon("apps", "idontwant2see-android", TVBrowserIcons.SIZE_LARGE));
    export.setActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if(mSettings.getUserName().trim().length() == 0 || mSettings.getPassword().trim().length() == 0) {
          JPanel message = new JPanel(new FormLayout("5dlu,default,3dlu,150dlu","default,5dlu,default,2dlu,default"));
          
          JTextField userName = new JTextField(mSettings.getUserName());
          JPasswordField userPassword = new JPasswordField(mSettings.getPassword());
          
          message.add(new JLabel(mLocalizer.msg("menu.export.message","You need to enter your AndroidSync user name and password:")), CC.xyw(1, 1, 4));
          message.add(new JLabel(mLocalizer.msg("settings.userName"," User name:")), CC.xy(2, 3));
          message.add(userName, CC.xy(4, 3));
          message.add(new JLabel(mLocalizer.msg("settings.passWord","Password:")), CC.xy(2, 5));
          message.add(userPassword, CC.xy(4, 5));
          
          if(JOptionPane.showConfirmDialog(getParentFrame(), message, mLocalizer.msg("settings.synchronization", "Android synchronization"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null) == JOptionPane.OK_OPTION) {
            mSettings.setUserName(userName.getText().trim());
            mSettings.setPassword(new String(userPassword.getPassword()).trim());
            
            exportAndroid();
          }
        }
        else {
          exportAndroid();
        }
      }
    });
    
    ContextMenuAction importExclusions = new ContextMenuAction(mLocalizer.msg("menu.import","Add exclusions from the TV-Browser server to the exclusion list of 'I don't want to see!'"),createImageIcon("apps", "idontwant2see-android", TVBrowserIcons.SIZE_SMALL));
    importExclusions.putValue(Plugin.BIG_ICON, createImageIcon("apps", "idontwant2see-android", TVBrowserIcons.SIZE_LARGE));
    importExclusions.setActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if(mSettings.getUserName().trim().length() == 0 || mSettings.getPassword().trim().length() == 0) {
          JPanel message = new JPanel(new FormLayout("5dlu,default,3dlu,150dlu","default,5dlu,default,2dlu,default"));
          
          JTextField userName = new JTextField(mSettings.getUserName());
          JPasswordField userPassword = new JPasswordField(mSettings.getPassword());
          
          message.add(new JLabel(mLocalizer.msg("menu.export.message","You need to enter your AndroidSync user name and password:")), CC.xyw(1, 1, 4));
          message.add(new JLabel(mLocalizer.msg("settings.userName"," User name:")), CC.xy(2, 3));
          message.add(userName, CC.xy(4, 3));
          message.add(new JLabel(mLocalizer.msg("settings.passWord","Password:")), CC.xy(2, 5));
          message.add(userPassword, CC.xy(4, 5));
          
          if(JOptionPane.showConfirmDialog(getParentFrame(), message, mLocalizer.msg("settings.synchronization", "Android synchronization"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null) == JOptionPane.OK_OPTION) {
            mSettings.setUserName(userName.getText().trim());
            mSettings.setPassword(new String(userPassword.getPassword()).trim());
            
            importAndroid();
          }
        }
        else {
          importAndroid();
        }
      }
    });

    return new ActionMenu(mLocalizer.msg(
        "name", "I don't want to see!"), createImageIcon("apps",
        "idontwant2see", 16),new Action[] {openExclusionList,undo,export,importExclusions});
  }
  
  private void importAndroid() {
    String car = mSettings.getUserName();
    String bicycle = mSettings.getPassword();
        
    if(car != null && bicycle != null) {
      URLConnection conn = null;
      BufferedReader read = null;

      try {
        URL url = new URL(DONT_WANT_TO_SEE_IMPORT_SYNC_ADDRESS);
        System.out.println("url:" + url);
        conn = url.openConnection();
        
        String getmethere = car.trim() + ":" + bicycle.trim();
        
        conn.setRequestProperty  ("Authorization", "Basic " + new String(Base64.encodeBase64(getmethere.getBytes())));
        
        read = new BufferedReader(new InputStreamReader(IOUtilities.openSaveGZipInputStream(conn.getInputStream()),"UTF-8"));
        
        /*String dateValue = read.readLine();
        
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        java.util.Date syncDate = dateFormat.parse(dateValue.trim());
        System.out.println(syncDate);
        if(syncDate.getTime() > System.currentTimeMillis()) {*/
          String line = null;
                      
          ArrayList<String> importExclusions = new ArrayList<String>();
          
          while((line = read.readLine()) != null) {
            if(line.contains(";;")) {
              importExclusions.add(line);
            }
          }
          
          if(!importExclusions.isEmpty()) {
            updateExclusions(importExclusions.toArray(new String[importExclusions.size()]));
          }
       // }
      }catch(Exception e) {e.printStackTrace();}
    }
  }
  
  private void exportAndroid() {
    upload(getExclusions());
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
  
  private void upload(String value) {
    String car = mSettings.getUserName();
    String bicycle = mSettings.getPassword();
    String CrLf = "\r\n";
    
    if(car != null && bicycle != null) {
      URLConnection conn = null;
      OutputStream os = null;
      InputStream is = null;

      try {
          URL url = new URL(DONT_WANT_TO_SEE_SYNC_ADDRESS);
          System.out.println("url:" + url);
          conn = url.openConnection();
          
          String getmethere = car.trim() + ":" + bicycle.trim();
          
          conn.setRequestProperty  ("Authorization", "Basic " + new String(Base64.encodeBase64(getmethere.getBytes())));
          
          conn.setDoOutput(true);

          String postData = "";
          
          byte[] xmlData = getCompressedData(value.getBytes("UTF-8"));
          
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
          
          JOptionPane.showMessageDialog(getParentFrame(), mLocalizer.msg("success", "The data were send successfully."), mLocalizer.msg("successTitle", "Success"), JOptionPane.INFORMATION_MESSAGE);
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
        
        switch (response) {
          case 404: JOptionPane.showMessageDialog(getParentFrame(), mLocalizer.msg("userError", "Username or password were not accepted. Please check them."), mLocalizer.msg("serverError", "Error in server connection"), JOptionPane.ERROR_MESSAGE);break;
          case 415: JOptionPane.showMessageDialog(getParentFrame(), mLocalizer.msg("wrongFileError", "Server didn't accepted upload data. This should not happen. Please contact TV-Browser team."), mLocalizer.msg("serverError", "Error in server connection"), JOptionPane.ERROR_MESSAGE);break;
          case 500: JOptionPane.showMessageDialog(getParentFrame(), mLocalizer.msg("serverFileError", "Server could not store data. Please try again, if this continues please contact TV-Browser team."), mLocalizer.msg("serverError", "Error in server connection"), JOptionPane.ERROR_MESSAGE);break;
          
          default: JOptionPane.showMessageDialog(getParentFrame(), mLocalizer.msg("unknowError", "Something went wrong with the connection to the server. Reason unknown."), mLocalizer.msg("serverError", "Error in server connection"), JOptionPane.ERROR_MESSAGE);break;
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

  public ActionMenu getContextMenuActions(final Program p) {
    if (p == null) {
      return null;
    }
    // check if this program is already hidden
    final int index = getSearchTextIndexForProgram(p);

    // return menu to hide the program
    if (index == -1 || p.equals(getPluginManager().getExampleProgram())) {
      AbstractAction actionDontWant = getActionDontWantToSee(p);
      
      if (mSettings.isSimpleMenu() && !VersionCompat.isAtLeastTvBrowser4() && !mCtrlPressed && !p.equals(getPluginManager().getExampleProgram())) {
        final Matcher matcher = PATTERN_TITLE_PART.matcher(p.getTitle());
        if (matcher.matches()) {
          actionDontWant = getActionInputTitle(p, matcher.group(2));
        }
        actionDontWant.putValue(Action.NAME,mLocalizer.msg("name","I don't want to see!"));
        actionDontWant.putValue(Action.SMALL_ICON,createImageIcon("apps","idontwant2see",16));
  
        return MenuCompat.createActionMenu(1, actionDontWant);
      }
      else {
        actionDontWant.putValue(Action.SMALL_ICON,createImageIcon("apps","idontwant2see",16));
        
        final AbstractAction actionInput = getActionInputTitle(p, null);
        actionInput.putValue(Action.SMALL_ICON,createImageIcon("apps","idontwant2see",16));

        final ActionMenu action1 = MenuCompat.createActionMenu(1, actionDontWant);
        final ActionMenu action2 = MenuCompat.createActionMenu(2, actionInput);
        
        return MenuCompat.createActionMenu(MenuCompat.ID_ACTION_NONE, mLocalizer
            .msg("name", "I don't want to see!"), createImageIcon("apps","idontwant2see",16),
            new ActionMenu[] {action1,action2}, mSettings.isSimpleMenu());
      }
    }

    // return menu to show the program
    return MenuCompat.createActionMenu(1, getActionShowAgain(p));
  }

  private ContextMenuAction getActionShowAgain(final Program p) {
    return new ContextMenuAction(mLocalizer
        .msg("menu.reshow", "I want to see!"), createImageIcon("actions",
        "edit-paste", 16)) {
      public void actionPerformed(final ActionEvent e) {
      	mSettings.showAgain(p);
        updateFilter(!mSettings.isSwitchToMyFilter());
      }
    };
  }

  private AbstractAction getActionInputTitle(final Program p, final String part) {
    return new AbstractAction(mLocalizer.msg("menu.userEntered",
        "User entered value")) {
      public void actionPerformed(final ActionEvent e) {
        final JCheckBox caseSensitive = new JCheckBox(mLocalizer.msg(
            "caseSensitive",
            "case sensitive"), mSettings.isDefaultCaseSensitive());
        String title = p.getTitle();
        ArrayList<String> items = new ArrayList<String>();
        if (part != null && !part.isEmpty()) {
          String shortTitle = title.trim().substring(0, title.length() - part.length()).trim();
          shortTitle = removeEnd(shortTitle, "-").trim();
          shortTitle = removeEnd(shortTitle, "(").trim();
          items.add(shortTitle + "*");
        }
        int index = title.indexOf(" - ");
        if (index > 0) {
          items.add(title.substring(0, index).trim()+"*");
        }
        items.add(title);
        index = title.lastIndexOf(':');
        if (index > 0) {
          items.add(title.substring(0, index).trim()+"*");
        }
        final JComboBox input = new JComboBox(items.toArray(new String[items.size()]));
        input.setEditable(true);
        
        input.addAncestorListener(new AncestorListener() {
          public void ancestorAdded(final AncestorEvent event) {
            event.getComponent().requestFocusInWindow();
          }

          public void ancestorMoved(final AncestorEvent event) {
          }

          public void ancestorRemoved(final AncestorEvent event) {
          }
        });
        
        JOptionPane pane = new JOptionPane(new Object[] {
            mLocalizer.msg("exclusionText",
                "What should be excluded? (You can use the wildcard *)"),
            input, caseSensitive }, JOptionPane.QUESTION_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
        JDialog d = pane.createDialog(UiUtilities.getLastModalChildOf(getParentFrame()), mLocalizer.msg("exclusionTitle",
            "Exclusion value entering"));
        d.setModalityType(ModalityType.DOCUMENT_MODAL);
        d.setVisible(true);
        
        if (pane.getValue() != null && (pane.getValue() instanceof Integer && ((Integer)pane.getValue()).intValue() == JOptionPane.OK_OPTION)) {
          String test = "";

          String result = (String) input.getSelectedItem();
          if (result != null) {
            test = result.replaceAll("\\*+", "\\*").trim();

            if (test.length() >= 0 && !test.equals("*")) {
              mSettings.getSearchList().add(new IDontWant2SeeListEntry(result,
                  caseSensitive.isSelected()));
              mSettings.setLastEnteredExclusionString(result);
              updateFilter(!mSettings.isSwitchToMyFilter());
            }
          }

          if (test.trim().length() <= 1) {
            JOptionPane.showMessageDialog(UiUtilities
                .getLastModalChildOf(getParentFrame()), mLocalizer.msg(
                "notValid", "The entered text is not valid."), Localizer
                .getLocalization(Localizer.I18N_ERROR),
                JOptionPane.ERROR_MESSAGE);
          }
        }
      }
    };
  }

  private AbstractAction getActionDontWantToSee(final Program p) {
    return new AbstractAction(mLocalizer.msg("menu.completeCaseSensitive",
        "Complete title case-sensitive")) {
      public void actionPerformed(final ActionEvent e) {
        mSettings.getSearchList().add(new IDontWant2SeeListEntry(p.getTitle(), mSettings.isDefaultCaseSensitive()));
        mSettings.setLastEnteredExclusionString(p.getTitle());
        updateFilter(!mSettings.isSwitchToMyFilter());
      }
    };
  }

  void updateFilter(final boolean update) {
    clearCache();

    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        if(!update) {
          getPluginManager().getFilterManager().setCurrentFilter(getFilter());
        }
        else {
          getPluginManager().getFilterManager().setCurrentFilter(getPluginManager().getFilterManager().getCurrentFilter());
        }
      }
    });

    saveMe();
  }

  public PluginsProgramFilter[] getAvailableFilter() {
    return new PluginsProgramFilter[] {getFilter()};
  }

  private PluginsProgramFilter getFilter() {
    if (mFilter == null) {
      mFilter = new PluginsProgramFilter(mInstance) {
        public String getSubName() {
          return "";
        }

        public boolean accept(final Program prog) {
          return acceptInternal(prog);
        }
      };
    }
    return mFilter;
  }

  public void readData(final ObjectInputStream in) throws IOException,
      ClassNotFoundException {
    final int version = in.readInt(); // read Version

    int n = in.readInt();

    mSettings.getSearchList().clear();
    
    if(version <= 2) {
      for(int i = 0; i < n; i++) {
        final StringBuilder value = new StringBuilder("*");
        value.append(in.readUTF()).append("*");

        mSettings.getSearchList().add(new IDontWant2SeeListEntry(value.toString(),false));
      }

      if(version == 2) {
        n = in.readInt();

        for(int i = 0; i < n; i++) {
          mSettings.getSearchList().add(new IDontWant2SeeListEntry(in.readUTF(),true));
        }

        mSettings.setSimpleMenu(false);
      }
    }
    else {
      for(int i = 0; i < n; i++) {
        mSettings.getSearchList().add(new IDontWant2SeeListEntry(in, version));
      }

      mSettings.setSimpleMenu(in.readBoolean());

      if(version >= 4) {
        mSettings.setSwitchToMyFilter(in.readBoolean());
      }
      if(version >= 5) {
        mSettings.setLastEnteredExclusionString(in.readUTF());
      }
      if(version >= 6) {
        mSettings.setLastUsedDate(Date.readData(in));
      }
      if(version >= 7) {
        mSettings.setProgramImportance(in.readByte());
      }
      else {
        mSettings.setProgramImportance(Program.DEFAULT_PROGRAM_IMPORTANCE);
      }
      if(version >= 8) {
        mSettings.setUserName(in.readUTF());
        mSettings.setPassword(in.readUTF());
      }
      if(version >= 9) {
        mSettings.setDefaultCaseSensitive(in.readBoolean());
      }
    }
  }

  public void writeData(final ObjectOutputStream out) throws IOException {
    // sort the list so that often used entries are in the front (for faster lookup)
    Collections.sort(mSettings.getSearchList(), new Comparator<IDontWant2SeeListEntry>() {

      public int compare(IDontWant2SeeListEntry o1, IDontWant2SeeListEntry o2) {
        return o2.getLastMatchedDate().compareTo(o1.getLastMatchedDate());
      }
    });

    out.writeInt(9); //version
    out.writeInt(mSettings.getSearchList().size());

    for(IDontWant2SeeListEntry entry : mSettings.getSearchList()) {
      entry.writeData(out);
    }

    out.writeBoolean(mSettings.isSimpleMenu());
    out.writeBoolean(mSettings.isSwitchToMyFilter());

    out.writeUTF(mSettings.getLastEnteredExclusionString());

    mSettings.getLastUsedDate().writeData((DataOutput)out);

    out.writeByte(mSettings.getProgramImportance());
    
    out.writeUTF(mSettings.getUserName());
    out.writeUTF(mSettings.getPassword());
    
    out.writeBoolean(mSettings.isDefaultCaseSensitive());
  }

  public SettingsTab getSettingsTab() {
    return new IDontWant2SeeSettingsTab(mSettings);
  }

  static Date getCurrentDate() {
    return mCurrentDate;
  }

  @SuppressWarnings("unchecked")
  public Class<? extends PluginsFilterComponent>[] getAvailableFilterComponentClasses() {
    return (Class<? extends PluginsFilterComponent>[]) new Class[] {IDontWant2SeeFilterComponent.class};
  }

  @Override
  public boolean canReceiveProgramsWithTarget() {
    return true;
  }

  @Override
  public ProgramReceiveTarget[] getProgramReceiveTargets() {
    return new ProgramReceiveTarget[] { new ProgramReceiveTarget(this,
        mLocalizer.msg("programTarget", "Exclude programs"),
        RECEIVE_TARGET_EXCLUDE_EXACT) };
  }

  @Override
  public boolean receivePrograms(final Program[] programArr,
      final ProgramReceiveTarget receiveTarget) {
    if (receiveTarget.getTargetId().equals(RECEIVE_TARGET_EXCLUDE_EXACT)) {
      if (programArr.length > 0) {
        for (Program program : programArr) {
          if (getSearchTextIndexForProgram(program) == -1) {
            mSettings.getSearchList()
                .add(new IDontWant2SeeListEntry(program.getTitle(), true));
            mSettings.setLastEnteredExclusionString(program.getTitle());
          }
        }
        updateFilter(!mSettings.isSwitchToMyFilter());
      }
      return true;
    }
    return false;
  }

  @Override
  public void onActivation() {
    mFilter = new PluginsProgramFilter(this) {
      public String getSubName() {
        return "";
      }

      public boolean accept(final Program prog) {
        return acceptInternal(prog);
      }
    };

    Toolkit.getDefaultToolkit().addAWTEventListener(this,
        AWTEvent.KEY_EVENT_MASK);
  }

  @Override
  public void onDeactivation() {
    Toolkit.getDefaultToolkit().removeAWTEventListener(this);
  }

  public void eventDispatched(final AWTEvent event) {
    if (event instanceof KeyEvent) {
      final KeyEvent keyEvent = (KeyEvent) event;
      mCtrlPressed = keyEvent.isControlDown();
      // System.out.println("Ctrl " + mCtrlPressed);
    }
  }
  
  public String getPluginCategory() {
    //Plugin.OTHER_CATEGORY
    return "misc";
  }
  
  private void updateExclusions(String[] exclusions) {
    ArrayList<IDontWant2SeeListEntry> entryList = mSettings.getSearchList();
    
    boolean changed = false;;
    
    for(String exclusion : exclusions) {
      String[] parts = exclusion.split(";;");
      
      IDontWant2SeeListEntry entry = new IDontWant2SeeListEntry(parts[0], parts[1].equals("1"));
      
      if(!entryList.contains(entry)) {
        changed = true;
        entryList.add(entry);
      }
    }
    
    if(changed) {
      updateFilter(!mSettings.isSwitchToMyFilter());
      exportAndroid();
    }
  }
  
  private String getExclusions() {
    StringBuilder value = new StringBuilder();
    
    ArrayList<IDontWant2SeeListEntry> entryList = mSettings.getSearchList();
    
    for(IDontWant2SeeListEntry entry : entryList) {
      value.append(entry.getSearchText()).append(";;").append(entry.isCaseSensitive() ? "1" : "0").append("\n");
    }
    
    return value.toString();
  }
  
  static String removeEnd(String haystack, final String needle) {
    if(haystack != null && needle != null && needle.length() > 0 && haystack.endsWith(needle)) {
      haystack = haystack.substring(0, haystack.length()-needle.length());
    }
    
    return haystack;
  }
}
