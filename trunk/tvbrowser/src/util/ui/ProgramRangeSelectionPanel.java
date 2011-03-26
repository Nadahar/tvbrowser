/*
 * TV-Browser
 * Copyright (C) 2003-2010 TV-Browser-Team (dev@tvbrowser.org)
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
 *     $Date$
 *   $Author$
 * $Revision$
 */
package util.ui;

import java.awt.Dimension;
import java.util.Calendar;
import java.util.Iterator;

import javax.swing.DefaultListModel;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionListener;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import devplugin.Channel;
import devplugin.Date;
import devplugin.Plugin;
import devplugin.Program;

/**
 * A class for selection of a program range.
 * 
 * @author René Mach
 * @since 3.0
 */
public class ProgramRangeSelectionPanel extends JPanel {
  private ProgramList mProgramList;
  
  /**
   * Creates an ProgramRangeSelectionPanel starting from the given program
   * end at the last avaiable program or at the additonal program count.
   * <p>
   * @param program The program to start the selection from.
   * @param additonalProgramCount The number of additional program that should be available for selection.
   * @return The ProgramRangeSelectionPanel.
   */
  public static ProgramRangeSelectionPanel createPanel(Program program, short additonalProgramCount) {
    return new ProgramRangeSelectionPanel(program, additonalProgramCount);
  }
  
  private ProgramRangeSelectionPanel(Program p, short additionalProgramCount) {
    setLayout(new FormLayout("default:grow","fill:default:grow"));
    DefaultListSelectionModel m = new DefaultListSelectionModel() {
      public void setSelectionInterval(int index0, int index1) {
        super.clearSelection();
        super.setSelectionInterval(0,index1);
      }
      
      public void removeSelectionInterval(int index0, int index1) {
        super.removeSelectionInterval(Math.max(1,index0), Math.max(1,getMaxSelectionIndex()));
      }
      
      public void addSelectionInterval(int index0, int index1) {
        super.setSelectionInterval(0,index1);
      }
    };
    
    DefaultListModel model = new DefaultListModel();
    
    mProgramList = new ProgramList(model);
    mProgramList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
    mProgramList.setSelectionModel(m);
    
    Date programDate = p.getDate();
    Channel programChannel = p.getChannel();
    
    Iterator<Program> channelDayProgram = Plugin.getPluginManager().getChannelDayProgram(programDate,programChannel);
    
    while(channelDayProgram.hasNext() && model.size() <= additionalProgramCount) {
      Program prog = channelDayProgram.next();
      
      if((prog.equals(p) || (!model.isEmpty() && model.size() <= additionalProgramCount)) /*&& prog.getLength() > 0*/) {
        model.addElement(prog);
      }
    }
    
    if(model.size() <= additionalProgramCount) {
      programDate = programDate.addDays(1);
      channelDayProgram = Plugin.getPluginManager().getChannelDayProgram(programDate, programChannel);
      
      if(channelDayProgram != null) {
        while(channelDayProgram.hasNext() && model.size() <= additionalProgramCount) {
          Program prog = channelDayProgram.next();
          if(model.size() <= additionalProgramCount && prog.getLength() > 0) {
            model.addElement(prog);
          }
        }
      }
    }
    
    mProgramList.setSelectedIndex(0);
    
    JScrollPane scrollPane = new JScrollPane(mProgramList);
    scrollPane.setPreferredSize(new Dimension(300,350));
    
    add(scrollPane, new CellConstraints().xy(1,1));
  }
  
  /**
   * Adds a ListSelectionListener to the program selection list.
   * <p>
   * @param listener The listener to add.
   */
  public void addListSelectionListener(ListSelectionListener listener) {
    mProgramList.addListSelectionListener(listener);
  }
  
  /**
   * Sets the end date used to determinate which programs should be selected.
   * <p>
   * @param endDate The end date to use.
   */
  public void setEndDate(java.util.Date endDate) {
    int lastSelectedIndex = 0;
    
    for(int i = 1; i < mProgramList.getModel().getSize(); i++) {
      Program prog = (Program)mProgramList.getModel().getElementAt(i);
      
      Calendar progDate = prog.getDate().getCalendar();
      progDate.set(Calendar.HOUR_OF_DAY, prog.getHours());
      progDate.set(Calendar.MINUTE, prog.getMinutes());
      
      if (prog.getLength() <= 0) {
        progDate.add(Calendar.MINUTE, 1);
      } else {
        progDate.add(Calendar.MINUTE, prog.getLength());
      }
      progDate.set(Calendar.SECOND, 0);
      
      if(progDate.getTime().compareTo(endDate) <= 0) {
        lastSelectedIndex = i;
      }
      else {
        break;
      }
    }
    
    mProgramList.setSelectedIndex(lastSelectedIndex);
  }
}
