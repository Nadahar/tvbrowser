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
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package tvbrowser;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;

import tvbrowser.core.*;
import tvbrowser.ui.programtable.ProgramTablePanel;
import tvbrowser.ui.finder.FinderPanel;
import tvbrowser.ui.SkinPanel;
import tvbrowser.ui.UpdateDlg;
import tvbrowser.ui.PictureButton;
import tvbrowser.ui.settings.SettingsDlg;

import java.util.*;

/**
 * TV-Browser
 * @author Martin Oberhauser
 */
public class TVBrowser extends JFrame implements ActionListener, DateListener, MainApplication {

  private JButton mNowBt, mEarlyBt, mMorningBt, mMiddayBt, mEveningBt,
    updateBtn, settingsBtn, searchBtn;
  private ProgramTablePanel programTablePanel;
  private JPanel buttonPanel;
  //private JProgressBar progressBar;
  private Thread downloadingThread;
  private JPanel jcontentPane;
  private FinderPanel finderPanel;
  private JMenuItem settingsMenuItem, updateMenuItem;
  private SkinPanel skinPanel;
  private static String curLookAndFeel;
  public static String MAINWINDOW_TITLE="TV-Browser v0.9.0";



  /**
   * Entry point of the application
   */
  public static void main(String[] args) {
    System.out.println("please wait...");

    try {
      ChannelList.readChannelList();
    }catch (IOException e) {
      System.out.println("no channel file found. using default channel settings");
      ChannelList.createDefaultChannelList();
    }


    Settings.loadSettings();
    System.out.print("loading Look&Feel..."); System.out.flush();

    try {
      curLookAndFeel=Settings.getLookAndFeel();
      UIManager.setLookAndFeel(curLookAndFeel);
    } catch (InstantiationException e) { e.printStackTrace();
    } catch (ClassNotFoundException e) { e.printStackTrace();
    } catch (UnsupportedLookAndFeelException e) { e.printStackTrace();
    } catch (IllegalAccessException e) { e.printStackTrace();
    }
    System.out.println("done");
    
    System.out.print("loading data service..."); System.out.flush();
    devplugin.Plugin.setPluginManager(DataService.getInstance());
    System.out.println("done");

    System.out.print("loading plugins..."); System.out.flush();
    PluginManager.initInstalledPlugins();
    System.out.println("done");


    System.out.print("loading selections..."); System.out.flush();
    try {
      String dir=Settings.getUserDirectoryName();
      File f=new File(dir,"selections");
      ObjectInputStream in=new ObjectInputStream(new FileInputStream(new File(dir,"selections")));
      ProgramSelection selection=(ProgramSelection)in.readObject();
      in.close();
      DataService.getInstance().setSelection(selection);
      System.out.println("done");
    }catch(FileNotFoundException e) {
      System.out.println("no selections found");
    }catch(IOException e) {
      e.printStackTrace();
    }catch(ClassNotFoundException e) {
      e.printStackTrace();
    }


    System.out.println("starting up...\n");
    final TVBrowser frame = new TVBrowser();
    frame.pack();
    frame.setSize(700,500);
    frame.setVisible(true);

    // scroll to now
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        frame.scrollToNow();
      }
    });
  }

  
  
  public TVBrowser() {
    super(MAINWINDOW_TITLE);
    
    JMenuBar menuBar=new JMenuBar();
    JMenu mainMenu=new JMenu("TV-Browser");
    JMenu tvDataMenu=new JMenu("TV-Data");
    JMenu helpMenu=new JMenu("Help");
    JMenu pluginsMenu=getPluginMenu();

    settingsMenuItem=new JMenuItem("Settings...");
    JMenuItem quitMenuItem=new JMenuItem("Quit");
    JMenuItem searchMenuItem=new JMenuItem("Search...");
    updateMenuItem=new JMenuItem("Update");

    JMenuItem importMenuItem=new JMenuItem("Import");
    JMenuItem exportMenuItem=new JMenuItem("Export");

    JMenuItem pluginDownloadMenuItem=new JMenuItem("Find plugins on the web");
    JMenuItem helpMenuItem=new JMenuItem("Help");
    JMenuItem infoMenuItem=new JMenuItem("About...");

    settingsMenuItem.addActionListener(this);
    quitMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        quit();
      }
    });
    updateMenuItem.addActionListener(this);

    mainMenu.add(settingsMenuItem);
    mainMenu.addSeparator();
    mainMenu.add(quitMenuItem);

    tvDataMenu.add(searchMenuItem);
    tvDataMenu.add(updateMenuItem);
    tvDataMenu.addSeparator();
    tvDataMenu.add(importMenuItem);
    tvDataMenu.add(exportMenuItem);

    pluginsMenu.addSeparator();
    pluginsMenu.add(pluginDownloadMenuItem);

    helpMenu.add(helpMenuItem);
    helpMenu.addSeparator();
    helpMenu.add(infoMenuItem);

    menuBar.add(mainMenu);
    menuBar.add(tvDataMenu);
    menuBar.add(pluginsMenu);
    menuBar.add(helpMenu);

    setJMenuBar(menuBar);

    jcontentPane=(JPanel)getContentPane();
    jcontentPane.setLayout(new BorderLayout());

    int mode;
    if (Settings.useApplicationSkin()) {
      mode=Settings.WALLPAPER;
    }else {
      mode=Settings.NONE;
    }


    skinPanel=new SkinPanel(Settings.getApplicationSkin(),mode);
    skinPanel.setLayout(new BorderLayout());


    JPanel northPanel=new JPanel(new BorderLayout());
    northPanel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
    northPanel.setOpaque(false);

    JPanel eastPanel=new JPanel(new BorderLayout(0,5));

    eastPanel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));

    buttonPanel = createButtonPanel();
    northPanel.add(buttonPanel,BorderLayout.WEST);

    programTablePanel=new ProgramTablePanel(this);
    programTablePanel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
    finderPanel=new FinderPanel(this);

    String[] items={"Offline Mode","Online Mode"};
    final JComboBox comboBox=new JComboBox(items);

    eastPanel.add(finderPanel,BorderLayout.CENTER);

    JPanel panel1=new JPanel();
    panel1.setOpaque(false);
    panel1.setLayout(new BoxLayout(panel1,BoxLayout.Y_AXIS));
    panel1.add(comboBox);

    panel1.add(DataService.getInstance().getProgressBar());

    eastPanel.setOpaque(false);

    comboBox.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (comboBox.getSelectedIndex()==0) {
          DataService.getInstance().setOnlineMode(false);
        }else if (comboBox.getSelectedIndex()==1) {
          DataService.getInstance().setOnlineMode(true);
        }
      }
    }
    );

    //eastPanel.add(comboBox,BorderLayout.SOUTH);
    eastPanel.add(panel1,BorderLayout.SOUTH);

    skinPanel.add(northPanel,BorderLayout.NORTH);
    skinPanel.add(eastPanel,BorderLayout.EAST);
    skinPanel.add(programTablePanel,BorderLayout.CENTER);
    programTablePanel.setOpaque(false);

    jcontentPane.add(skinPanel,BorderLayout.CENTER);

    addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        quit();
      }
    });
  }

  
  
  private void quit() {
    System.out.println("Storing plugin data");
    PluginManager.finalizeInstalledPlugins();
    System.out.println("done");

    System.out.println("Storing selection data");
    ProgramSelection selection=DataService.getInstance().getSelection();
    if (selection==null) {
      System.out.println("nothing to do.");
    }else{

      try {
        String dir=Settings.getUserDirectoryName();
        File f=new File(dir,"selections");
        ObjectOutputStream out=new ObjectOutputStream(new FileOutputStream(new File(dir,"selections")));
        out.writeObject(selection);
        out.close();
      }catch(IOException e) {
        e.printStackTrace();
      }
    }

    System.out.println("done");

    System.exit(0);
  }

  
  
  private JMenu getPluginMenu() {
    final JMenu pluginMenu=new JMenu("Plug-Ins");

    Object[] plugins=PluginManager.getInstalledPlugins();
    JMenuItem item;
    HashMap map=new HashMap();
    for (int i=0;i<plugins.length;i++) {
      final devplugin.Plugin plugin=(devplugin.Plugin)plugins[i];
      plugin.setParent(this);
      String btnTxt=plugin.getButtonText();
      if (btnTxt!=null) {
        int k=1;
        String txt=btnTxt;
        while (map.get(txt)!=null) {
          txt=btnTxt+"("+k+")";
          k++;
        }
        map.put(txt,btnTxt);

        item=new JMenuItem(btnTxt);
        pluginMenu.add(item);
        item.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent event) {
            System.out.println("plugin-btn pressed");
            plugin.execute();
          }
        }
        );
      }
    }

    return pluginMenu;
  }

  
  
  private void scrollToNow() {
    Calendar cal = Calendar.getInstance();
    int hour = cal.get(Calendar.HOUR_OF_DAY);
    programTablePanel.scrollTo(hour);
  }
  
  
  
  public void actionPerformed(ActionEvent event) {
    if (event.getSource() == mNowBt) {
      scrollToNow();
    }
    else if (event.getSource() == mEarlyBt) {
      programTablePanel.scrollTo(4);
    }
    else if (event.getSource() == mMorningBt) {
      programTablePanel.scrollTo(8);
    }
    else if (event.getSource() == mMiddayBt) {
      programTablePanel.scrollTo(12);
    }
    else if (event.getSource() == mEveningBt) {
      programTablePanel.scrollTo(18);
    }

    else if (event.getSource()==updateBtn || event.getSource()==updateMenuItem) {
      
      if (DataService.getInstance().isDownloading()) {
        DataService.getInstance().setIsDownloading(false);
        onDownloadDone();
      }
      else {
        
        UpdateDlg dlg=new UpdateDlg(this,true);
        dlg.pack();
        dlg.show();
        final int daysToDownload=dlg.getResult();
        if (daysToDownload!=UpdateDlg.CANCEL) {
          final JFrame parent=this;
          downloadingThread=new Thread() {
            public void run() {
              try{
                onDownloadStart();
                DataService.getInstance().startDownload(daysToDownload);
                onDownloadDone();
              }catch (IOException e) {
                e.printStackTrace();
              }
            }
          };
          downloadingThread.start();
        }
      }
      
    } else if (event.getSource()==settingsMenuItem || event.getSource()==settingsBtn) {
      SettingsDlg dlg=new SettingsDlg(this);
      dlg.pack();
      dlg.show();
      dlg.dispose();
      updateLookAndFeel();
      updateApplicationSkin();
      this.updateProgramTableSkin();
    }
  }
  
  
  
  private void onDownloadStart() {
    DataService.getInstance().setIsDownloading(true);
    updateBtn.setText("Stop");
    updateBtn.setIcon(new ImageIcon("imgs/Stop24.gif"));
    updateMenuItem.setText("Stop download");
  }
  
  
  
  private void onDownloadDone() {
    DataService.getInstance().setIsDownloading(false);
    DataService.getInstance().getProgressBar().setValue(0);
    updateBtn.setText("Update");
    updateBtn.setIcon(new ImageIcon("imgs/Import24.gif"));
    updateMenuItem.setText("Update");
    
    try {
      devplugin.Date showingDate = finderPanel.getSelectedDate();
      DayProgram dayProgram = DataService.getInstance().getDayProgram(showingDate);
      programTablePanel.setDayProgram(dayProgram);
    }catch(IOException e) {
      JOptionPane.showMessageDialog(this,e.getMessage());
    }
    if (finderPanel!=null) finderPanel.update();
    
  }

  
  
  private JPanel createButtonPanel() {
    JPanel result = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
    result.setOpaque(false);

    if (Settings.isTimeBtnVisible()) {
      // mNowBt, mEarlyBt, mDayBt, mEveningBt
      mNowBt = new PictureButton("Jetzt", new ImageIcon("imgs/TimeNow24.gif"));
      mNowBt.addActionListener(this);
      result.add(mNowBt);
      
      mEarlyBt = new PictureButton("Früh", new ImageIcon("imgs/TimeEarly24.gif"));
      mEarlyBt.addActionListener(this);
      result.add(mEarlyBt);
      
      mMorningBt = new PictureButton("Vormittag", new ImageIcon("imgs/TimeMorning24.gif"));
      mMorningBt.addActionListener(this);
      result.add(mMorningBt);

      mMiddayBt = new PictureButton("Mittag", new ImageIcon("imgs/TimeMidday24.gif"));
      mMiddayBt.addActionListener(this);
      result.add(mMiddayBt);
      
      mEveningBt = new PictureButton("Abend", new ImageIcon("imgs/TimeEvening24.gif"));
      mEveningBt.addActionListener(this);
      result.add(mEveningBt);
      
      result.add(new JSeparator(JSeparator.VERTICAL));
    }
    
    if (Settings.isPrevNextBtnVisible()) {
      JPanel panel1=new JPanel(new GridLayout(1,2,5,0));
      panel1.setOpaque(false);
      JButton prevBtn=new PictureButton("Back",new ImageIcon("imgs/Back24.gif"));
      JButton nextBtn=new PictureButton("Forward", new ImageIcon("imgs/Forward24.gif"));
      panel1.add(prevBtn);
      panel1.add(nextBtn);
      prevBtn.addActionListener(this);
      nextBtn.addActionListener(this);
      result.add(panel1);
    }

    if (Settings.isUpdateBtnVisible()) {
      updateBtn=new PictureButton("Update",new ImageIcon("imgs/Import24.gif"));
      result.add(updateBtn);
      updateBtn.addActionListener(this);
    }

    if (Settings.isSearchBtnVisible()) {

      searchBtn=new PictureButton("Search",new ImageIcon("imgs/Find24.gif"));
      result.add(searchBtn);
      searchBtn.addActionListener(this);
    }
    if (Settings.isPreferencesBtnVisible()) {
      settingsBtn=new PictureButton("Settings",new ImageIcon("imgs/Preferences24.gif"));
      result.add(settingsBtn);
      settingsBtn.addActionListener(this);
    }


    return result;
  }


  
  private void changeDate(devplugin.Date date) {
    try {
      DayProgram prog = DataService.getInstance().getDayProgram(date);
      programTablePanel.setDayProgram(prog);
      if (finderPanel!=null) finderPanel.update();
    } catch (IOException e) {
      JOptionPane.showMessageDialog(this,e.getMessage());
    }
  }

  
  
  /**
   * Implementation of Interface DateListener
   * 
   */
  public void dateChanged(final devplugin.Date date) {
    if (DataService.getInstance().isOnlineMode()) {     
      downloadingThread=new Thread() {
        public void run() {
          onDownloadStart();
          changeDate(date);
          onDownloadDone();
        }
      };
      downloadingThread.start();
    }
    else {
      changeDate(date);
    }
  }



  public void updateLookAndFeel() {

    if (curLookAndFeel==null || !curLookAndFeel.equals(Settings.getLookAndFeel())) {
      try {
        curLookAndFeel=Settings.getLookAndFeel();
        UIManager.setLookAndFeel(curLookAndFeel);
        SwingUtilities.updateComponentTreeUI(this);
        validate();
      } catch (InstantiationException e) { e.printStackTrace();
      } catch (ClassNotFoundException e) { e.printStackTrace();
      } catch (UnsupportedLookAndFeelException e) { e.printStackTrace();
      } catch (IllegalAccessException e) { e.printStackTrace();
      }
    }

  }

  public void updateApplicationSkin() {

    int mode;
    if (Settings.useApplicationSkin()) {
      mode=Settings.WALLPAPER;
    }else {
      mode=Settings.NONE;
    }

    skinPanel.update(Settings.getApplicationSkin(),mode);

  }

  public void updateProgramTableSkin() {
    programTablePanel.updateBackground();
  }


}