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
import java.awt.event.*;
import java.awt.BorderLayout;

import tvbrowser.ui.programtable.*;
import tvbrowser.ui.filter.dlgs.SelectFilterDlg;

public class FilterChooser extends JPanel {

  private static final util.ui.Localizer mLocalizer
         = util.ui.Localizer.getLocalizerFor(FilterChooser.class);
     
    
    
  private final JComboBox mBox;
    
  public FilterChooser(final JFrame parent, final ProgramTableModel model) {
     
    super(new BorderLayout());
    mBox=new JComboBox();
    updateList();
    mBox.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        Object o=mBox.getSelectedItem();
        if (o instanceof Filter) {
           model.setProgramFilter((Filter)o);   
        }else if (o instanceof String){
          SelectFilterDlg dlg=new SelectFilterDlg(parent);
          util.ui.UiUtilities.centerAndShow(dlg);
          updateList();
        }
      }            
    });        
    add(mBox,BorderLayout.CENTER);    
    }
    
    
  private void updateList() {
        
    mBox.removeAllItems();
    mBox.addItem(new ShowAllFilter(mLocalizer.msg("ShowAll","Show all")));  // default filter
    Filter[] f=FilterList.getFilterList();
    for (int i=0;i<f.length;i++) {
      mBox.addItem(f[i]);
    }
        
    mBox.addItem(mLocalizer.msg("EditFilters","edit filters..."));
    }
}


class ShowAllFilter extends Filter {
    
  public ShowAllFilter(String title) {
    super(title);
  }
    
  public boolean accept(devplugin.Program prog) {
    return true;
  }
    
}
