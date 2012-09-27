/*
 * CapturePlugin by Andreas Hessel (Vidrec@gmx.de), Bodo Tasche
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
 *     $Date: 2009-09-04 11:15:55 +0200 (Fr, 04 Sep 2009) $
 *   $Author: bananeweizen $
 * $Revision: 5953 $
 */
package captureplugin.drivers.defaultdriver.configpanels;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;

import util.ui.ChannelTableCellRenderer;
import util.ui.ExtensionFileFilter;
import util.ui.Localizer;
import captureplugin.drivers.defaultdriver.DeviceConfig;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import devplugin.Channel;

/**
 * This Panel makes it possible to assign external names to channels
 */
public class ChannelPanel extends JPanel {

    /** Translator */
    private static final Localizer mLocalizer = Localizer.getLocalizerFor(ChannelPanel.class);

    private JTable mChannelTable = new JTable();
    private ChannelTableModel mTableModel;

    /** Settings */
    private DeviceConfig mData;

    /**
     * Creates the Panel
     * @param data Data to use
     */
    public ChannelPanel(DeviceConfig data) {
        mData = data;
        createPanel();
    }
    
    /**
     * creates a JPanel for managing the channels
     */
    private void createPanel() {
      CellConstraints cc = new CellConstraints();
      PanelBuilder pb = new PanelBuilder(new FormLayout("5dlu,default,pref:grow,5dlu","pref,5dlu,fill:default:grow,5dlu,default"),this);
      pb.setDefaultDialogBorder();

      mTableModel = new ChannelTableModel(mData);
      mChannelTable.setModel(mTableModel);
      mChannelTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      mChannelTable.getTableHeader().setReorderingAllowed(false);
      mChannelTable.getColumnModel().getColumn(0).setCellRenderer(new ChannelTableCellRenderer());
      mChannelTable.getColumnModel().getColumn(1).setCellRenderer(new DefaultTableCellRenderer() {
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
          JPanel background = new JPanel(new FormLayout("default:grow","fill:default:grow"));
          JLabel label = new JLabel(value.toString());
          label.setOpaque(false);
          
          if(isSelected) {
            background.setBackground(table.getSelectionBackground());
            label.setForeground(table.getSelectionForeground());
          }
          else {
            background.setBackground(table.getBackground());
          }
          
          background.add(label, new CellConstraints().xy(1,1));
          
          return background;
        }
      });
      
      JScrollPane sp = new JScrollPane(mChannelTable);

      addAncestorListener(new AncestorListener() {
        public void ancestorAdded(AncestorEvent event) {}

        public void ancestorMoved(AncestorEvent event) {}

        public void ancestorRemoved(AncestorEvent event) {
          if (mChannelTable.isEditing()) {
            TableCellEditor editor = mChannelTable.getCellEditor();
            if (editor != null) {
              editor.stopCellEditing();
            }
          }
        }
      });
      
      JButton tryMapping = new JButton(mLocalizer.msg("tryMapping", "Try mapping of channels from text file with external names"));
      tryMapping.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
          tryMapping();
        }
      });
        
      pb.addSeparator(mLocalizer.msg("ChannelNames", "Channel Names"), cc.xyw(1,1,4));
      pb.add(sp, cc.xyw(2,3,2));
      pb.add(tryMapping, cc.xy(2,5));
    }
    
    /**
     * @since 3.1.1
     */
    private void tryMapping() {
      JFileChooser chooser = new JFileChooser();
      chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
      chooser.setDialogTitle(mLocalizer.msg("tryMappingTitle", "Load external channel names from text file"));
      
      ExtensionFileFilter filter = new ExtensionFileFilter("txt", mLocalizer.msg("fileType", "Text files (*.txt [UTF-8])"));

      chooser.setFileFilter(filter);
      
      if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
        ArrayList<String> externalChannelList = new ArrayList<String>(0);

        try {
          BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(chooser.getSelectedFile()),"UTF-8"));
          
          String line = null;
          
          // seems to be needed to prevent additional char in first line
          in.read();
          
          while((line = in.readLine()) != null) {
            externalChannelList.add(line.trim());
          }
          
          in.close();
        } catch (IOException e) {e.printStackTrace();}
        
        if(!externalChannelList.isEmpty()) {
          guessChannels(externalChannelList.toArray(new String[externalChannelList.size()]));
        }
      }
    }
    
    /**
     * Taken from WtvcgScheduler2 an changed for CapturePlugin
     * <p>
     * @param externalChannels The names of the external channels.
     * @since 3.1.1
     */
    private void guessChannels(String[] externalChannels) {
      for(int i = 0; i < mTableModel.getRowCount(); i++) {
        Channel ch = (Channel)mTableModel.getValueAt(i,0);
        
        boolean found = false;
        int foundLength = -1;
        String foundValue = null;
        
        for(int j = 0; j < externalChannels.length; j++) {
          String externalChannel = externalChannels[j].replaceAll("\\p{Punct}|\\s+","").toLowerCase();
          String internalChannel = ch.getName().replaceAll("\\p{Punct}|\\s+","").toLowerCase();
          
          if(!found && internalChannel.indexOf(externalChannel) != -1) {
            if(externalChannel.length() > foundLength) {
              foundLength = externalChannel.length();
              foundValue = externalChannels[j];
            }
          }
          else if(!found && externalChannel.indexOf(internalChannel) != -1) {
            if(internalChannel.length() > foundLength) {
              foundLength = internalChannel.length();
              foundValue = externalChannels[j];
            }
          }
          
          if(internalChannel.equals(externalChannel)){
            mTableModel.setValueAt(externalChannels[j],i,1);
            found = true;
          }
        }
        
        if(!found && foundValue != null) {
          mTableModel.setValueAt(foundValue,i,1);
        }
      }
      
      mChannelTable.repaint();
    }
}