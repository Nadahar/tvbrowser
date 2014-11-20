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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
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
import java.util.Properties;
import java.util.zip.GZIPOutputStream;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
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
  
  public NewsEditor() {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (Exception e) {
      e.printStackTrace();
    }
    
    mGroupNewsChannels = new GroupNewsChannel[0];
    
    mCreateNews = new JButton("Create");
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
    
    mEditNews = new JButton("Edit");
    mEditNews.setEnabled(false);
    mEditNews.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
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
    });
    
    mDeleteNews = new JButton("Delete");
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
    
    JButton selectConfig = new JButton("Select config directory");
    selectConfig.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        createNewsInfo();
        
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select config directory of your data");
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
    mNewsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    mNewsList.addListSelectionListener(new ListSelectionListener() {
      @Override
      public void valueChanged(ListSelectionEvent e) {
        updateButtons();        
      }
    });
    
    JButton close = new JButton("Close GroupNews-Editor");
    close.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        mNewsFrame.dispose();
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
    c.gridwidth = 3;
    
    mainPanel.add(selectConfig, c);
    
    c.insets = new Insets(2, 5, 2, 5);
    c.gridy = 1;
    c.gridwidth = 1;
    c.weightx = 0;
    
    mainPanel.add(new JLabel("Group:"), c);
    
    c.gridx = 1;
    c.gridwidth = 2;
    c.weightx = 1;
    
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
    
    c.gridx = 1;
    c.gridy = 5;
    c.gridwidth = 1;
    
    mainPanel.add(close, c);
    
    mNewsFrame = new JFrame("GroupNews-Editor");
    mNewsFrame.setContentPane(mainPanel);
    mNewsFrame.setSize(700, 400);
    mNewsFrame.setLocationRelativeTo(null);
    //mNewsFrame.setLocationByPlatform(true);
    mNewsFrame.addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosed(WindowEvent e) {
        saveGroup((String)mGroupSelection.getSelectedItem());
        createNewsInfo();
        
        System.exit(0);
      }
      
      @Override
      public void windowClosing(WindowEvent e) {
        saveGroup((String)mGroupSelection.getSelectedItem());
        createNewsInfo();
        
        System.exit(0);
      }
    });
    mNewsFrame.setVisible(true);
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
  
  private void createNewsInfo() {
    if(mConfigDirectory != null && mConfigDirectory.isDirectory()) {
      File newsInfoFile = new File(mConfigDirectory,"news_info.gz");
      Properties newsInfo = new Properties();
      
      File[] newsFiles = mConfigDirectory.listFiles(new FileFilter() {
        @Override
        public boolean accept(File f) {
          return f.isFile() && f.getName().endsWith("_news.gz");
        }
      });
      
      for(File newsFile : newsFiles) {
        newsInfo.setProperty(newsFile.getName(), String.valueOf(newsFile.lastModified()));
      }
      
      GZIPOutputStream out = null;
      
      if(newsInfoFile.isFile()) {
        newsInfoFile.delete();
      }
      
      try {
        out = new GZIPOutputStream(new FileOutputStream(newsInfoFile));
        
        newsInfo.store(out, "Group news info");
      } catch (FileNotFoundException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      finally {
        if(out != null) {
          try {
            out.close();
          } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
          }
        }
      }
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
    }
  }
}
