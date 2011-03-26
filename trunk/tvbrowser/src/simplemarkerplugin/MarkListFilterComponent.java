/*
 * SimpleMarkerPlugin by René Mach
 * 
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * SVN information:
 *     $Date$
 *   $Author$
 * $Revision$
 */
package simplemarkerplugin;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import util.ui.Localizer;
import util.ui.UiUtilities;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import devplugin.Plugin;
import devplugin.PluginsFilterComponent;
import devplugin.Program;

/**
 * The filter component for the SimpleMarkerPlugin.
 * 
 * @author René Mach
 */
public class MarkListFilterComponent extends PluginsFilterComponent {
  private String mListId = null;
  private JComboBox mLists;
  
  @Override
  public String getUserPresentableClassName() {
    return SimpleMarkerPlugin.getLocalizer().msg("filterText","Contained on a list of SimpleMarkerPlugin");
  }

  public boolean accept(Program program) {
    try {
      return SimpleMarkerPlugin.getInstance().getMarkListForId(mListId).contains(program);
    } catch(Exception e) {
      if(SimpleMarkerPlugin.getInstance().getSuperFrame() != null) {
        JOptionPane.showMessageDialog(UiUtilities.getLastModalChildOf(SimpleMarkerPlugin.getInstance().getSuperFrame()),SimpleMarkerPlugin.getLocalizer().msg("filterErrorMessage","The mark list that is used for the filter component '{0}' was deleted.\nPlease correct your filter configuration.",getName()),Localizer.getLocalization(Localizer.I18N_ERROR) + ": " +SimpleMarkerPlugin.getInstance().getInfo().getName(),JOptionPane.ERROR_MESSAGE);
        Plugin.getPluginManager().getFilterManager().setCurrentFilter(Plugin.getPluginManager().getFilterManager().getDefaultFilter());
      }
    }
    
    return false;
  }

  public JPanel getSettingsPanel() {
    CellConstraints cc = new CellConstraints();
    PanelBuilder pb = new PanelBuilder(new FormLayout("pref","pref"));
    
    String[] lists = SimpleMarkerPlugin.getInstance().getMarkListNames();
    
    mLists = new JComboBox(lists);
    
    if(mListId != null){
      MarkList selected = SimpleMarkerPlugin.getInstance().getMarkListForId(mListId);
      
      if(selected != null) {
        mLists.setSelectedItem(selected.getName());
      }
    }
    
    pb.add(mLists, cc.xy(1,1));
    
    return pb.getPanel();
  }

  public int getVersion() {
    return 0;
  }

  public void saveSettings() {
    if(mLists != null) {
      mListId = SimpleMarkerPlugin.getInstance().getMarkListForName((String)mLists.getSelectedItem()).getId();
    }
  }

  public void read(ObjectInputStream in, int version) throws IOException, ClassNotFoundException {
    mListId = in.readUTF();
  }

  public void write(ObjectOutputStream out) throws IOException {
    out.writeUTF(mListId);
  }

}
