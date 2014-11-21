/*
 * TV-Browser
 * Copyright (C) 2014 TV-Browser team (dev@tvbrowser.org)
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
 *     $Date: 2010-06-28 19:33:48 +0200 (Mo, 28 Jun 2010) $
 *   $Author: bananeweizen $
 * $Revision: 6662 $
 */
package helper.newseditor;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.zip.GZIPOutputStream;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.html.HTMLEditorKit;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;

import tvbrowserdataservice.GroupNews;

public class NewsEditor {
  private JFrame mNewsFrame;
  private JList mNewsList;
  private DefaultListModel mNewsListModel;
  private File mConfigDirectory;
  
  private JButton mCreateNews;
  private JButton mEditNews;
  private JButton mDeleteNews;
  
  private JComboBox mGroupSelection;
  
  private GroupNewsChannel[] mGroupNewsChannels;
  
  final static HashMap<String, String> TRANSLATION_EN;
  final static HashMap<String, String> TRANSLATION_DE;
  
  final static String GROUP_EDITOR_TITLE_TEXT = "GROUP_EDITOR_TITLE_TEXT";
  final static String NEWS_EDITOR_TITLE_TEXT = "NEWS_EDITOR_TITLE_TEXT";
  
  final static String CONFIG_BUTTON_TEXT = "CONFIG_BUTTON_TEXT";
  final static String HELP_BUTTON_TEXT = "HELP_BUTTON_TEXT";
  final static String SELECT_TITLE_TEXT = "SELECT_TITLE_TEXT";
  final static String CLOSE_BUTTON_TEXT = "CLOSE_BUTTON_TEXT";
  final static String CLOSE_HELP_TEXT = "CLOSE_HELP_TEXT";
  final static String GROUP_TEXT = "GROUP_TEXT";
  final static String CREATE_TEXT = "CREATE_TEXT";
  final static String EDIT_TEXT = "EDIT_TEXT";
  final static String DELETE_TEXT = "DELETE_TEXT";
  
  final static String HELP_TEXT = "HELP_TEXT";
  
  final static String EN_TITLE_TEXT = "EN_TITLE_TEXT";
  final static String EN_NEWS_TEXT = "EN_NEWS_TEXT";
  final static String DE_TITLE_TEXT = "DE_TITLE_TEXT";
  final static String DE_NEWS_TEXT = "DE_NEWS_TEXT";
  
  final static String RESTRICT_CHANNELS_TEXT = "RESTRICT_CHANNELS_TEXT";
  final static String SAVE_TEXT = "SAVE_TEXT";
  final static String CANCEL_TEXT = "CANCEL_TEXT";
  
  static {
    TRANSLATION_EN = new HashMap<String, String>();
    
    TRANSLATION_EN.put(GROUP_EDITOR_TITLE_TEXT, "GroupNews Editor");
    TRANSLATION_EN.put(NEWS_EDITOR_TITLE_TEXT, "Edit GroupNews for Group: ");
    TRANSLATION_EN.put(CONFIG_BUTTON_TEXT, "Select config directory");
    TRANSLATION_EN.put(HELP_BUTTON_TEXT, "Close Help");
    TRANSLATION_EN.put(CLOSE_BUTTON_TEXT, "Close GroupNews Editor");
    TRANSLATION_EN.put(GROUP_TEXT, "Channel Group: ");
    TRANSLATION_EN.put(CREATE_TEXT, "Create");
    TRANSLATION_EN.put(EDIT_TEXT, "Edit");
    TRANSLATION_EN.put(DELETE_TEXT, "Delete");
    TRANSLATION_EN.put(EN_TITLE_TEXT, "English title:");
    TRANSLATION_EN.put(EN_NEWS_TEXT, "English news text (HTML format possible):");
    TRANSLATION_EN.put(DE_TITLE_TEXT, "German title:");
    TRANSLATION_EN.put(DE_NEWS_TEXT, "German news text (HTML format possible):");
    TRANSLATION_EN.put(RESTRICT_CHANNELS_TEXT, "Restrict news to channels:");
    TRANSLATION_EN.put(SAVE_TEXT, "Save");
    TRANSLATION_EN.put(CANCEL_TEXT, "Cancel");
    TRANSLATION_EN.put(CLOSE_HELP_TEXT, "Close");
    TRANSLATION_EN.put(SELECT_TITLE_TEXT, "Select config directory of your data");
    TRANSLATION_EN.put(HELP_TEXT, "<html><body style=\"padding:5px;\"><h1>Help</h1><h2>Basics</h2><p>If your data configuration is stored on a server without GUI, you have to download the config directory of your data first.</p>" +
    		"<p>To create/edit/delete news you have to select the config directory of your data. After it is load you will see a channel group in the channel group selection. A news is always created for the selected channel" +
    		" group.</p><p>To create a new news, select the button <b>Create</b> you will see the dialog for news editing.</p><h2>News editing</h2><p>To make a valid news you have to enter a title and a text (at least in one language)." +
    		" If you want the news only shown to users that have specific channels subscribed you can select the channels to show the news for on the right side.</p><h2>Get it to the server</h2><p>After you have closed the <i>GroupNews Editor</i>" +
    		" you will find the file <i>GROUPID</i>_news_info.gz and <i>GROUPID</i>_news.gz in your config directory. These files have to be uploaded to the config directory on the server (if you didn't created the news on the server itself). " +
    		"After the news are in the config directory on the server they will be uploaded to all mirrors on the next run of the data tools.</p></body></html>");
    
    TRANSLATION_DE = new HashMap<String, String>();
    
    TRANSLATION_DE.put(GROUP_EDITOR_TITLE_TEXT, "Gruppen-Nachrichten-Editor");
    TRANSLATION_DE.put(NEWS_EDITOR_TITLE_TEXT, "Gruppen-Nachrichten bearbeiten für Sendergruppe: ");
    TRANSLATION_DE.put(CONFIG_BUTTON_TEXT, "config-Verzeichnis auswählen");
    TRANSLATION_DE.put(HELP_BUTTON_TEXT, "Hilfe");
    TRANSLATION_DE.put(CLOSE_BUTTON_TEXT, "Gruppen-Nachrichten-Editor schließen");
    TRANSLATION_DE.put(GROUP_TEXT, "Sendergruppe: ");
    TRANSLATION_DE.put(CREATE_TEXT, "Erstellen");
    TRANSLATION_DE.put(EDIT_TEXT, "Bearbeiten");
    TRANSLATION_DE.put(DELETE_TEXT, "Löschen");
    TRANSLATION_DE.put(EN_TITLE_TEXT, "Englischer Titel:");
    TRANSLATION_DE.put(EN_NEWS_TEXT, "Englischer Nachrichtentext (HTML-Auszeichnungen möglich):");
    TRANSLATION_DE.put(DE_TITLE_TEXT, "Deutscher Titel:");
    TRANSLATION_DE.put(DE_NEWS_TEXT, "Deutscher Nachrichtentext (HTML-Auszeichnungen möglich):");
    TRANSLATION_DE.put(RESTRICT_CHANNELS_TEXT, "Nachrichten auf Sender beschränken:");
    TRANSLATION_DE.put(SAVE_TEXT, "Speichern");
    TRANSLATION_DE.put(CANCEL_TEXT, "Abbrechen");
    TRANSLATION_DE.put(CLOSE_HELP_TEXT, "Hife schließen");
    TRANSLATION_DE.put(SELECT_TITLE_TEXT, "config-Verzeichnis der Daten auswählen");
    TRANSLATION_DE.put(HELP_TEXT, "<html><body style=\"padding:5px;\"><h1>Hilfe</h1><h2>Grundlegendes</h2><p>Sollte sich die Datenaufbereitung auf einem Server ohne grafische Oberfläche befinden, müssen Sie zuerst das config-Verzeichnis Ihrer Daten auf Ihren Rechner laden.</p>" +
        "<p>Um Nachrichten anzulegen/zu bearbeiten/zu löschen wählen Sie das config-Verzeichnis Ihrer Daten aus. Nachdem dieses geladen wurde sehen Sie in der Sendergruppenauswahl die aktuelle Sendergruppe. Nachrichten werden immer für die ausgewählte Sendergruppe" +
        " erstellt.</p><p>Um neue Nachrichten zu erstellen wählen Sie den Button <b>Erstellen</b>, es öffnet sich der Dialog zum Bearbeiten der Nachrichten.</p><h2>Nachrichten bearbeiten</h2><p>Um eine korrekte Nachricht erstellen zu können müssen ein Titel und ein Nachrichtentext (in wenigstens einer Spache) eingegeben werden." +
        " Möchten Sie die Nachrichten nur bei Nutzern anzeigen, die bestimmte Sender der Gruppe abonniert haben, dann wählen Sie auf der rechten Seite die entsprechenden Sender aus.</p><h2>Auf den Server hochladen</h2><p>Nachdem der <i>Gruppen-Nachrichten-Editor</i>" +
        " geschlossen wurde, finden Sie die Dateien <i>GROUPID</i>_news_info.gz und <i>GROUPID</i>_news.gz im config-Verzeichnis Ihrer Daten. Diese Dateien müssen nun in das config-Verzeichnis auf dem Server hochgeladen werden (falls Sie die News nicht auf dem Server selbst erstellt haben). " +
        "Nachdem sich die Nachrichten im richtigen config-Verzeichnis befinden, werden sie beim nächsten Durchlauf der Datenttools auf den Spiegelservern verteilt.</p></body></html>");

  }
  
  public static String getTextFor(String key) {
    if(Locale.getDefault().getLanguage().equals("de")) {
      return TRANSLATION_DE.get(key);
    }
    
    return TRANSLATION_EN.get(key);
  }
  
  public NewsEditor() {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (Exception e) {
      e.printStackTrace();
    }
    
    mGroupNewsChannels = new GroupNewsChannel[0];
    
    mCreateNews = new JButton(getTextFor(CREATE_TEXT));
    mCreateNews.setEnabled(false);
    mCreateNews.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        GroupNews news = new GroupNews();
        
        GroupNewsEditor editor = new GroupNewsEditor((String)mGroupSelection.getSelectedItem(), news, mNewsFrame, mGroupNewsChannels);
        editor.setVisible(true);
        
        if(editor.wasOkButtonPressed()) {
          mNewsListModel.addElement(news);
        }
      }
    });
    
    mEditNews = new JButton(getTextFor(EDIT_TEXT));
    mEditNews.setEnabled(false);
    mEditNews.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        edit();
      }
    });
    
    mDeleteNews = new JButton(getTextFor(DELETE_TEXT));
    mDeleteNews.setEnabled(false);
    mDeleteNews.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        if(mNewsList.getSelectedIndex() >= 0) {
          mNewsListModel.remove(mNewsList.getSelectedIndex());
        }
      }
    });
    
    mGroupSelection = new JComboBox();
    mGroupSelection.setEnabled(false);
    mGroupSelection.addItemListener(new ItemListener() {
      @Override
      public void itemStateChanged(ItemEvent e) {
        if(e.getStateChange() == ItemEvent.DESELECTED) {
          saveGroup((String)e.getItem());
        }
        else if(e.getStateChange() == ItemEvent.SELECTED) {
          String groupId = (String)e.getItem();
          
          ArrayList<GroupNewsChannel> groupNewsChannelList = new ArrayList<GroupNewsChannel>();
          
          File channels = new File(mConfigDirectory,groupId+"_channellist.txt");
          
          if(channels.isFile()) {
            BufferedReader in = null;
            
            try {
              in = new BufferedReader(new InputStreamReader(new FileInputStream(channels), "ISO-8859-15"));
              
              String line = null;
              
              while((line = in.readLine()) != null) {
                String[] parts = line.split(";");
                
                groupNewsChannelList.add(new GroupNewsChannel(parts[2], parts[3]));
              }
            } catch (UnsupportedEncodingException e1) {
              // TODO Auto-generated catch block
              e1.printStackTrace();
            } catch (FileNotFoundException e1) {
              // TODO Auto-generated catch block
              e1.printStackTrace();
            } catch (IOException e1) {
              // TODO Auto-generated catch block
              e1.printStackTrace();
            }
            finally {
              if(in != null) {
                try {
                  in.close();
                } catch (IOException e1) {
                  // TODO Auto-generated catch block
                  e1.printStackTrace();
                }
              }
            }
          }
          
          mGroupNewsChannels = groupNewsChannelList.toArray(new GroupNewsChannel[groupNewsChannelList.size()]);
          
          mNewsListModel.clear();
          
          loadNewsForGroup(groupId);
        }
      }
    });
    
    JButton selectConfig = new JButton(getTextFor(CONFIG_BUTTON_TEXT));
    selectConfig.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        if(mConfigDirectory != null && mGroupSelection.getItemCount() > 0) {
          saveGroup((String)mGroupSelection.getSelectedItem());
        }
        
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle(getTextFor(SELECT_TITLE_TEXT));
        chooser.setMultiSelectionEnabled(false);
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        
        if(chooser.showOpenDialog(mNewsFrame) == JFileChooser.APPROVE_OPTION) {
          mConfigDirectory = chooser.getSelectedFile();
        }
        
        if(mConfigDirectory != null) {
          File[] groups = mConfigDirectory.listFiles(new FileFilter() {
            @Override
            public boolean accept(File f) {
              return f.isFile() && f.getName().endsWith("_channellist.txt");
            }
          });
          
          if(groups.length == 0) {
            mConfigDirectory = null;
          }
          else {
            mGroupSelection.setEnabled(true);
            
            mGroupSelection.removeAllItems();
            
            for(File group : groups) {
              mGroupSelection.addItem(group.getName().substring(0,group.getName().indexOf("_channellist")));
            }
          }
        }
        
        updateButtons();
      }
    });

    mNewsListModel = new DefaultListModel();
    
    mNewsList = new JList(mNewsListModel);
    mNewsList.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        if(SwingUtilities.isLeftMouseButton(e) && e.getClickCount() >= 2) {
          int index = mNewsList.locationToIndex(e.getPoint());
          
          if(index != -1) {
            mNewsList.setSelectedIndex(index);
            
            edit();
          }
        }
      }
    });
    mNewsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    mNewsList.addListSelectionListener(new ListSelectionListener() {
      @Override
      public void valueChanged(ListSelectionEvent e) {
        updateButtons();        
      }
    });
    
    JButton close = new JButton(getTextFor(CLOSE_BUTTON_TEXT));
    close.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        mNewsFrame.dispose();
      }
    });
    
    JButton help = new JButton(getTextFor(HELP_BUTTON_TEXT));
    help.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        final JEditorPane helpPane = new JEditorPane();
        helpPane.setEditorKit(new HTMLEditorKit());
        helpPane.setEditable(false);
        helpPane.setText(getTextFor(HELP_TEXT));
        
        final JScrollPane scrollPane = new JScrollPane(helpPane);
        
        final JDialog helpDialog = new JDialog(mNewsFrame);
        helpDialog.setTitle(getTextFor(HELP_BUTTON_TEXT));
        helpDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        
        JButton close = new JButton(getTextFor(CLOSE_HELP_TEXT));
        close.addActionListener(new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent e) {
            helpDialog.dispose();
          }
        });
        
        JPanel mainPanel = new JPanel(new BorderLayout(5,5));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(close, BorderLayout.SOUTH);
        
        helpDialog.setContentPane(mainPanel);
        helpDialog.setSize(500, 400);
        helpDialog.setLocationRelativeTo(null);
        helpDialog.setVisible(true);
        
        SwingUtilities.invokeLater(new Runnable() {
          @Override
          public void run() {
            scrollPane.getVerticalScrollBar().setValue(0);
          }
        });
      }
    });
    
    JPanel mainPanel = new JPanel(new GridBagLayout());
    mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
    GridBagConstraints c = new GridBagConstraints();
    
    c.insets = new Insets(2, 0, 2, 0);
    c.fill = GridBagConstraints.HORIZONTAL;
    c.weightx = 1;
    c.gridx = 0;
    c.gridy = 0;
    c.gridwidth = 2;
    
    mainPanel.add(selectConfig, c);
    
    c.insets = new Insets(2, 5, 2, 5);
    c.weightx = 0;    
    c.gridx = 2;
    c.gridwidth = 1;
    
    mainPanel.add(help, c);
    
    c.gridy = 1;
    c.gridx = 0;
        
    mainPanel.add(new JLabel(getTextFor(GROUP_TEXT)), c);
    
    c.gridx = 1;
    c.weightx = 1;
    c.gridwidth = 2;
    
    mainPanel.add(mGroupSelection, c);
    
    c.fill = GridBagConstraints.BOTH;
    c.gridx = 0;
    c.gridy = 2;
    c.gridwidth = 3;
    c.weighty = 1;
    
    JPanel editPanel = new JPanel(new GridBagLayout());
    GridBagConstraints c1 = new GridBagConstraints();
    
    c1.insets = new Insets(2, 2, 2, 2);
    c1.fill = GridBagConstraints.BOTH;
    c1.gridx = 0;
    c1.gridy = 0;
    c1.gridwidth = 1;
    c1.gridheight = 3;
    c1.weightx = 1;
    c1.weighty = 1;
    
    editPanel.add(new JScrollPane(mNewsList), c1);
    
    c1.fill = GridBagConstraints.HORIZONTAL;
    c1.gridx = 1;
    c1.gridheight = 1;
    c1.weighty = 0;
    c1.weightx = 0;
    
    editPanel.add(mCreateNews, c1);
    
    c1.gridy = 1;
    
    editPanel.add(mEditNews, c1);
    
    c1.gridy = 2;
    c1.anchor = GridBagConstraints.SOUTH;
    
    editPanel.add(mDeleteNews, c1);
        
    mainPanel.add(editPanel, c);
    
    c.weighty = 0;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.gridx = 0;
    c.gridy = 3;
    c.gridwidth = 3;
    
    c.insets = new Insets(5, 0, 5, 0);
    
    mainPanel.add(new JSeparator(JSeparator.HORIZONTAL), c);
    
    c.gridx = 0;
    c.gridy = 5;
    c.gridwidth = 3;
    
    mainPanel.add(close, c);
    
    mNewsFrame = new JFrame(getTextFor(GROUP_EDITOR_TITLE_TEXT));
    mNewsFrame.setContentPane(mainPanel);
    mNewsFrame.setSize(700, 400);
    mNewsFrame.setLocationRelativeTo(null);
    //mNewsFrame.setLocationByPlatform(true);
    mNewsFrame.addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosed(WindowEvent e) {
        saveGroup((String)mGroupSelection.getSelectedItem());
        
        System.exit(0);
      }
      
      @Override
      public void windowClosing(WindowEvent e) {
        saveGroup((String)mGroupSelection.getSelectedItem());
        
        System.exit(0);
      }
    });
    mNewsFrame.setVisible(true);
  }
  
  private void edit() {
    int index = mNewsList.getSelectedIndex();
    Object news = mNewsList.getSelectedValue();
    
    if(news != null) {
      GroupNews copy = ((GroupNews)news).copy();
      
      GroupNewsEditor edit = new GroupNewsEditor((String)mGroupSelection.getSelectedItem(), copy, mNewsFrame, mGroupNewsChannels);
      edit.setVisible(true);
      
      if(edit.wasOkButtonPressed() && copy.isValid()) {
        mNewsListModel.remove(index);
        mNewsListModel.add(index, copy);
      }
    }
  }
  
  private void saveGroup(String group) {
    if(group != null) {
      ArrayList<GroupNews> news = new ArrayList<GroupNews>();
      
      for(int i = 0; i < mNewsListModel.size(); i++) {
        news.add((GroupNews)mNewsListModel.elementAt(i));
      }
      
      writeGroupNews(group, news.toArray(new GroupNews[news.size()]));
    }
  }
  
  private void updateButtons() {
    mCreateNews.setEnabled(mConfigDirectory != null && mConfigDirectory.isDirectory());
    mEditNews.setEnabled(mConfigDirectory != null && mConfigDirectory.isDirectory() && mNewsList.getSelectedIndex() >= 0);
    mDeleteNews.setEnabled(mNewsList.getSelectedIndex() >= 0);
  }
  /**
   * @param args
   */
  public static void main(String[] args) {
    new NewsEditor();
  }
    
  private void loadNewsForGroup(String group) {
    mNewsListModel.clear();
    
    GroupNews[] news = GroupNews.loadNews(new File(mConfigDirectory, group + "_news.gz"));
    
    for(GroupNews newsItem : news) {
      if(newsItem.isValid()) {
        mNewsListModel.addElement(newsItem);
      }
    }
  }

  private void writeGroupNews(String group, GroupNews[] news) {
    if(mConfigDirectory.isDirectory()) {
      File target = new File(mConfigDirectory, group + "_news.gz");
      File oldNews = new File(mConfigDirectory, group + "_news.gz_old");
      
      if(news.length == 0) {
        if(target.isFile()) {
          target.delete();
        }
      }
      else {
        if(oldNews.isFile()) {
          oldNews.delete();
        }
        
        if(!oldNews.isFile() && target.isFile()) {
          target.renameTo(oldNews);
        }
        
        XMLOutputFactory factory = XMLOutputFactory.newInstance();
        
        
        GZIPOutputStream out = null;
        
        Arrays.sort(news);
        
        try {
            out = new GZIPOutputStream(new FileOutputStream(target));
            XMLStreamWriter writer = factory.createXMLStreamWriter(out);
            
            writer.writeStartDocument();
              writer.writeStartElement("newslist");
            
              long cuttoff = System.currentTimeMillis()-(3*30*24*60*60000L);
              
              for(GroupNews newsItem : news) {
                if(newsItem.isValid() && newsItem.getDate().getTime() >= cuttoff) {
                  newsItem.writeNews(writer);
                }
              }
            
              writer.writeEndElement();
            writer.writeEndDocument();
            
            if(oldNews.isFile()) {
              oldNews.delete();
            }
        } catch (Exception e) {
          if(target.isFile()) {
            target.delete();
          }
          
         if(oldNews.isFile()) {
          oldNews.renameTo(target);
         }
        }
        finally {
          if(out != null) {
            try {
              out.flush();
              out.close();
            } catch (IOException e) {
              // TODO Auto-generated catch block
              e.printStackTrace();
            }
          }
        }
      }
      
      File newsInfo = new File(mConfigDirectory,group+"_news_info.gz");
      
      if(newsInfo.isFile()) {
        newsInfo.delete();
      }
      
      long newsDate = -1;
      
      if(target.isFile()) {
        newsDate = target.lastModified();
      }
      
      GZIPOutputStream out = null;
      
      try {
        out = new GZIPOutputStream(new FileOutputStream(newsInfo));
        DataOutputStream dataOut = new DataOutputStream(out);
        dataOut.writeLong(newsDate);
      } catch (FileNotFoundException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      if(out != null) {
        try {
          out.flush();
          out.close();
        } catch (IOException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }
    }
  }
}
