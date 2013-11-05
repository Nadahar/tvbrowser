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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableModel;

import devplugin.Program;

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
    return 3;
  }
  
  @Override
  public boolean isCellEditable(int row, int column) {
    return column == 1 || column == 2;
  }
  
  @Override
  public String getColumnName(int column) {
    String value = null;
    
    switch(column){
      case 0: value = Localizer.getLocalization(Localizer.I18N_PROGRAMS);break;
      case 1: value = PearlCreationJPanel.mLocalizer.msg("comment", "Comment");break;
      case 2: value = PearlCreationJPanel.mLocalizer.msg("formating", "Formating");break;
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
      case 1: return mTVPearlCreationList.get(row).getComment();
      case 2: return mTVPearlCreationList.get(row).getFormating();
    }

    return null;
  }

  @Override
  public void setValueAt(Object aValue, int row, int column) {
    switch(column) {
      case 1: mTVPearlCreationList.get(row).setComment((String)aValue);break;
      case 2: mTVPearlCreationList.get(row).setFormating((AbstractPluginProgramFormating)aValue);break;
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
    
    item.getProgram().mark(TVPearlPlugin.getInstance());
    
    fireTableRowsInserted(insertIndex, insertIndex);}catch(Throwable t) {t.printStackTrace();}
  }

  @Override
  public void removeRow(int row) {
    TVPearlCreation pearl = mTVPearlCreationList.remove(row);
    
    if(TVPearlPlugin.getInstance().getPearl(pearl.getProgram()) == null) {
      pearl.getProgram().unmark(TVPearlPlugin.getInstance());
    }
    
    fireTableRowsDeleted(row, row);
  }
  
  public void clear() {
    int maxIndex = mTVPearlCreationList.size() - 1;
    
    for(int row = mTVPearlCreationList.size() - 1; row >= 0; row--) {
      TVPearlCreation pearl = mTVPearlCreationList.remove(row);
      
      if(TVPearlPlugin.getInstance().getPearl(pearl.getProgram()) == null) {
        pearl.getProgram().unmark(TVPearlPlugin.getInstance());
      }
    }
    
    if(maxIndex >= 0) {
      fireTableRowsDeleted(0, maxIndex);
    }
  }
  
  public boolean contains(Program program) {
    for(TVPearlCreation pearl : mTVPearlCreationList) {
      if(pearl.equals(program)) {
        return true;
      }
    }
    
    return false;
  }
  
  public void writeData(ObjectOutputStream out) throws IOException {
    out.writeInt(mTVPearlCreationList.size());
    
    for(TVPearlCreation pearl : mTVPearlCreationList) {
      pearl.writeData(out);
    }
  }
  
  public static PearlCreationTableModel readData(ObjectInputStream in, int version) throws IOException, ClassNotFoundException {
    int size = in.readInt();
    
    PearlCreationTableModel model = new PearlCreationTableModel();
    
    for(int i = 0; i < size; i++) {
      TVPearlCreation pearl = TVPearlCreation.readData(in, version);
      
      if(pearl.isValid()) {
        model.addRowSorted(pearl);
      }
    }
    
    return model;
  }
}
