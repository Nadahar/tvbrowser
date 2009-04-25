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

package tvbrowser.ui.settings.channel;

import java.util.ArrayList;
import java.util.Locale;

import javax.swing.table.AbstractTableModel;

import tvbrowser.core.ChannelList;
import devplugin.Channel;
import devplugin.ChannelGroup;

/**
 * Created by: Martin Oberhauser (martin@tvbrowser.org)
 * Date: 27.01.2005
 * Time: 21:30:42
 */
public class AvailableChannelsTableModel extends AbstractTableModel {

      private String[] columnNames = { "Channel",
                         "Provider",
                         "Country",
                         "Timezone"};

     private ArrayList<Channel> mChannelList;
     //private HashSet mSubscribedChannels;

     public AvailableChannelsTableModel() {
       mChannelList = new ArrayList<Channel>();
       Channel[] ch = ChannelList.getAvailableChannels();
       for (int i=0; i<ch.length; i++) {
         if (!ChannelList.isSubscribedChannel(ch[i])) {
           mChannelList.add(ch[i]);
         }
       }
     }


     public int getColumnCount() {
       return 4;
     }



     public String getColumnName(int col) {
       return columnNames[col];
     }

     public int getRowCount() {
       return mChannelList.size();
     }

     public Class<?> getColumnClass(int columnIndex) {
       if (columnIndex == 0) {
         return Channel.class;
       }
       return String.class;
     }

     public void removeChannel(Channel ch) {
       mChannelList.remove(ch);
       fireTableDataChanged();
     }

     public void addRow(Channel ch) {
       mChannelList.add(ch);
       fireTableDataChanged();
     }

     public Object getValueAt(int rowIndex, int columnIndex) {
       Channel ch = mChannelList.get(rowIndex);
       ChannelGroup group = ch.getGroup();
       switch (columnIndex) {
         case 0 : return ch;
         case 1 : if (group != null) {
                    try {
                      return group.getProviderName();
                    }catch(NoSuchMethodError e) {
                      return "-";
                    }
                  }
                  else {
                    return "-";
                  }



         case 2 : String country = ch.getCountry();
                  Locale locale = new Locale(Locale.getDefault().getLanguage(), country);
                  return locale.getDisplayCountry();
         case 3 : return ch.getTimeZone().getDisplayName();
         default: return ch;
       }
     }


   }


