/*
 * TV-Pearl improvement by René Mach
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
package tvpearlplugin;

import java.util.ArrayList;

import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableModel;

import util.program.AbstractPluginProgramFormating;
import util.program.ProgramUtilities;
import util.ui.Localizer;


public class PearlCreationTableModel extends DefaultTableModel {
  private ArrayList<TVPearlCreation> mTVPearlCreationList;
  
  public PearlCreationTableModel() {
    mTVPearlCreationList = new ArrayList<TVPearlCreation>();
  }
  
  @Override
  public int getRowCount() {
    return mTVPearlCreationList == null ? 0 : mTVPearlCreationList.size();
  }
  
  @Override
  public int getColumnCount() {
    return 2;
  }
  
  @Override
  public boolean isCellEditable(int row, int column) {
    return column == 1;
  }
  
  @Override
  public String getColumnName(int column) {
    String value = null;
    
    switch(column){
      case 0: value = Localizer.getLocalization(Localizer.I18N_PROGRAMS);break;
      case 1: value = PearlCreationJPanel.mLocalizer.msg("formating", "Formating");break;
    }
    
    return value;
  }

  @Override
  public Object getValueAt(int row, int column) {
    if (row < 0 || row > mTVPearlCreationList.size()) {
      return null;
    }
    
    switch(column) {
      case 0: return mTVPearlCreationList.get(row).getProgram();
      case 1: return mTVPearlCreationList.get(row).getFormating();
    }

    return null;
  }

  @Override
  public void setValueAt(Object aValue, int row, int column) {
    switch(column) {
      case 1: mTVPearlCreationList.get(row).setFormating((AbstractPluginProgramFormating)aValue);break;
    }

    fireTableChanged(new TableModelEvent(this));
  }
  
  /**
   * Adds a row to the table.
   * <p>
   * @param item The item to add.
   */
  public void addRowSorted(TVPearlCreation item) {try {
    int insertIndex = mTVPearlCreationList.size();
    
    for(int row = 0; row < mTVPearlCreationList.size(); row++) {
      int compare = ProgramUtilities.getProgramComparator().compare(mTVPearlCreationList.get(row).getProgram(), item.getProgram());
      
      if(compare == 0 && mTVPearlCreationList.get(row).getProgram().getStartTime() > item.getProgram().getStartTime() || compare > 0) {
        insertIndex = row;
        break;
      }
    }
    
    mTVPearlCreationList.add(insertIndex,item);
    
    fireTableRowsInserted(insertIndex, insertIndex);}catch(Throwable t) {t.printStackTrace();}
  }

  @Override
  public void removeRow(int row) {
    mTVPearlCreationList.remove(row);
    fireTableRowsDeleted(row, row);
  }
  
  public void clear() {
    int maxIndex = mTVPearlCreationList.size() - 1;
    
    if(maxIndex >= 0) {
      mTVPearlCreationList.clear();
      fireTableRowsDeleted(0, maxIndex);
    }
  }
}
