/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (martin@tvbrowser.org)
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
 *  $RCSfile$
 *   $Source$
 *     $Date$
 *   $Author$
 * $Revision$
 */

package tvbrowser.ui.mainframe;

import java.awt.BorderLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.commons.net.ftp.parser.MLSxEntryParser;

import tvbrowser.core.ChannelList;
import tvbrowser.core.Settings;
import tvbrowser.ui.programtable.DefaultProgramTableModel;
import util.ui.ChannelContextMenu;
import util.ui.ChannelListCellRenderer;
import util.ui.DragAndDropMouseListener;
import util.ui.ListDragAndDropHandler;
import util.ui.ListDropAction;
import util.ui.UiUtilities;
import devplugin.Channel;
import devplugin.ChannelFilter;

/**
 * @author bodum
 */
public class ChannelChooserPanel extends JPanel implements ListDropAction {

  private DefaultListModel mChannelChooserModel;
  private JList mList;
  private MainFrame mParent;
  private boolean disableSync = false;
  private ChannelFilter mChannelFilter;
  
  /**
   * @param frame
   * @param keyListener The key listener for FAYT.
   */
  public ChannelChooserPanel(MainFrame frame,KeyListener keyListener) {
    mParent = frame;
    mChannelChooserModel = new DefaultListModel();

    mList = new JList(mChannelChooserModel) {
      @Override
      public void setSelectedIndex(int index) {
        if(getModel().getElementAt(index) instanceof String) {
          int test = getSelectedIndex();
          
          if(test < index) {
            index++;
          }
          else {
            index--;
          }
          
          if(index >= 0 && index < getModel().getSize()) {
            setSelectedIndex(index);
          }
        }
        else {
          super.setSelectedIndex(index);
        }
      }
    };
    mList.addKeyListener(keyListener);
    updateChannelChooser();
    setLayout(new BorderLayout());
    JScrollPane scrollPane = new JScrollPane(mList);
    scrollPane.addKeyListener(keyListener);
    scrollPane.getViewport().addKeyListener(keyListener);
    scrollPane.getVerticalScrollBar().addKeyListener(keyListener);
    scrollPane.getHorizontalScrollBar().addKeyListener(keyListener);
    add(scrollPane);

    ListDragAndDropHandler dnDHandler = new ListDragAndDropHandler(mList,
        mList, this);
    new DragAndDropMouseListener(mList, mList, this, dnDHandler);

    mList.addListSelectionListener(new ListSelectionListener() {
      public void valueChanged(ListSelectionEvent e) {
        if (!disableSync) {
          showChannel();
        }
        disableSync  = false;
      }
    });

    mList.addKeyListener(new KeyAdapter() {
      public void keyPressed(KeyEvent e) {
        if(e.getKeyCode() == KeyEvent.VK_SPACE) {
          showChannel();
        }
      }
    });

    mList.addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent e) {
        if(SwingUtilities.isLeftMouseButton(e)) {
          showChannel();
        }
      }

      public void mousePressed(MouseEvent e) {
        if(SwingUtilities.isRightMouseButton(e)) {
          mList.setSelectedIndex(mList.locationToIndex(e.getPoint()));
        }
        showPopupMenu(e);
      }

      public void mouseReleased(MouseEvent e) {
        showPopupMenu(e);      }
    });

    mList.addMouseWheelListener(new MouseWheelListener() {
      public void mouseWheelMoved(MouseWheelEvent e) {
        int selected = mList.getSelectedIndex() + e.getWheelRotation();
        if (selected < 0) {
          selected = 0;
        } else if (selected > mList.getModel().getSize()) {
          selected = mList.getModel().getSize();
        }

        mList.setSelectedIndex(selected);
        mList.ensureIndexIsVisible(selected);
      }
    });
  }

  private void showChannel() {
    Channel selectedChannel = (Channel) mList.getSelectedValue();
    if (selectedChannel != null) {
      mParent.showChannel(selectedChannel);
    }
  }

  private void showPopupMenu(MouseEvent e) {
    if(e.isPopupTrigger()) {
      new ChannelContextMenu(e,(Channel)mList.getModel().getElementAt(mList.locationToIndex(e.getPoint())),this);
    }
  }

  public void updateChannelChooser() {
    mList.setCellRenderer(new ChannelListCellRenderer(Settings.propShowChannelIconsInChannellist.getBoolean(),
        Settings.propShowChannelNamesInChannellist.getBoolean(),false,false,true));
    mChannelChooserModel.removeAllElements();
    Channel[] channelList = tvbrowser.core.ChannelList.getSubscribedChannels();
    
    String[] separatorArr = Settings.propSubscribedChannelsSeparators.getStringArray();
    Channel previousChannel = null;
    int lastSeparatorIndex = 0;
    
    if(channelList.length > 0) {
      mChannelChooserModel.addElement(channelList[0]);
      previousChannel = channelList[0];
    }
    
    for (int i = 1; i < channelList.length; i++) {
      for(int j = lastSeparatorIndex; j < separatorArr.length; j++) {
        String separator = separatorArr[j];
        
        if(separator.endsWith(channelList[i].getUniqueId()) && 
            previousChannel != null && separator.startsWith(previousChannel.getUniqueId()) ) {
          mChannelChooserModel.addElement(Channel.SEPARATOR);
          lastSeparatorIndex = j+1;
        }
      }
      
      previousChannel = channelList[i];
      
      if(channelList[i-1].getJointChannel() == null || 
          !channelList[i-1].getJointChannel().equals(channelList[i])) {
          mChannelChooserModel.addElement(channelList[i]);
      }
    }
  }

  public void drop(JList source, JList target, int rows, boolean move) {
    Channel selected = (Channel)source.getSelectedValue();
    int pos = source.getSelectedIndex();
    UiUtilities.moveSelectedItems(target, rows, true);

    Channel additional = selected.getJointChannel();
    
    Object[] list = ((DefaultListModel) mList.getModel()).toArray();

    // Convert the list into a Channel[] and fill channels
    ArrayList<Channel> tempList = new ArrayList<Channel>();
    
    for (int i = 0; i < list.length; i++) {
      Channel joint = ((Channel)list[i]).getJointChannel();
      
      if(additional != null && joint != null && additional.equals(joint)) {
        joint = null;
      }
      
      if(i == pos) {
        if(additional != null) {
          tempList.add(additional);
        }
        tempList.add((Channel)list[i]);
      }
      else {
        tempList.add((Channel)list[i]);
      }
      
      if(joint != null) {
        tempList.add(joint);
      }
    }
    
    Channel[] channelArr = tempList.toArray(new Channel[tempList.size()]);

    ChannelList.setSubscribeChannels(channelArr);
    Settings.propSubscribedChannels.setChannelArray(channelArr);

    if (!Settings.propTrayUseSpecialChannels.getBoolean()) {
      Channel[] tempArr = new Channel[channelArr.length > 10 ? 10 : channelArr.length];
      System.arraycopy(channelArr, 0, tempArr, 0, tempArr.length);
      Settings.propTraySpecialChannels.setChannelArray(tempArr);
    }

    ChannelList.reload();
    DefaultProgramTableModel model = MainFrame.getInstance().getProgramTableModel();
    model.setChannels(ChannelList.getSubscribedChannels());
    MainFrame.getInstance().updateChannellist();
    setChannelFilter(mChannelFilter);
  }

  public void selectChannel(Channel channel) {
    disableSync = true;
    mList.setSelectedValue(channel,true);
    mList.ensureIndexIsVisible(mList.getSelectedIndex());
  }

  public void setChannelFilter(ChannelFilter channelFilter) {
    mChannelFilter = channelFilter;
    Channel[] channels = null;
    if (channelFilter != null) {
      channels = channelFilter.getChannels();
    }
    ((ChannelListCellRenderer)mList.getCellRenderer()).setChannels(channels);
  }

}
