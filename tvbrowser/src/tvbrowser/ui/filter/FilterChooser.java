/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (darras@users.sourceforge.net)
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
 
package tvbrowser.ui.filter;

import javax.swing.*;

import devplugin.ProgramFilter;

import java.awt.event.*;
import java.awt.BorderLayout;

//import tvbrowser.ui.programtable.*;
import tvbrowser.core.filters.FilterList;
import tvbrowser.ui.filter.dlgs.SelectFilterDlg;
import tvbrowser.ui.programtable.ProgramTableModel;

public class FilterChooser extends JPanel {

  private static final util.ui.Localizer mLocalizer
         = util.ui.Localizer.getLocalizerFor(FilterChooser.class);
     
    
    
  private final JComboBox mBox;
  private ProgramTableModel mModel;  
  private FilterList mFilterList;
    
  public FilterChooser(final JFrame parent, final ProgramTableModel model) {
     
    super(new BorderLayout());
    
    mFilterList = new FilterList();
    mFilterList.create();
    
    
    mBox=new JComboBox();
    mModel=model;
    updateList();
    
    model.setProgramFilter((ProgramFilter)mBox.getSelectedItem());
    mBox.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        Object o=mBox.getSelectedItem();
        if (o instanceof ProgramFilter) {
          model.setProgramFilter((ProgramFilter)o);   
        }else if (o instanceof String){
          SelectFilterDlg dlg=new SelectFilterDlg(parent, mFilterList);
          util.ui.UiUtilities.centerAndShow(dlg);
          //mFilterList = dlg.getFilterList();
          updateList();
          ProgramFilter f=(ProgramFilter)mBox.getItemAt(0);
          mBox.setSelectedIndex(0);
          model.setProgramFilter(f);  
        }
      }            
    });        
    add(mBox,BorderLayout.CENTER);    
    }
    
    
    
  private void updateList() {
        
    mBox.removeAllItems();
    
    //Object[] o=FilterListModel.getInstance().toArray();
    ProgramFilter[] f = mFilterList.getFilterArr();
    for (int i=0;i<f.length;i++) {
      mBox.addItem(f[i]);
    }
    
    /*
    AbstractFilter[] f=FilterList.getFilterList();
    for (int i=0;i<f.length;i++) {
      mBox.addItem(f[i]);
    }
        */
    mBox.addItem(mLocalizer.msg("EditFilters","edit filters..."));
    }
}



