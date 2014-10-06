 /* FilterFilterComponent
 * Copyright (C) 2014 René Mach (rene@tvbrowser.org)
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
 */
package filterfiltercomponent;

import java.awt.BorderLayout;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import devplugin.Plugin;
import devplugin.PluginsFilterComponent;
import devplugin.Program;
import devplugin.ProgramFilter;

/**
 * FilterComponent class to use existing filter in.
 * 
 * @author René Mach
 */
public class FilterFilterComp extends PluginsFilterComponent  {
  private ProgramFilter mFilter;
  private String mName;
  private JComboBox mFilterSelection;
  
  @Override
  public int getVersion() {
    return 1;
  }
  
  private void loadFilter() {
    if(mFilter == null) {
      ProgramFilter[] available = Plugin.getPluginManager().getFilterManager().getAvailableFilters();
      
      for(ProgramFilter filter : available) {
        if(filter.getName().equals(mName)) {
          mFilter = filter;
          break;
        }
      }
      
      if(mFilter == null) {
        mFilter = Plugin.getPluginManager().getFilterManager().getAllFilter();
      }
    }
  }

  @Override
  public boolean accept(Program program) {
    loadFilter();
    
    return mFilter.accept(program);
  }

  @Override
  public void read(ObjectInputStream in, int version) throws IOException,
      ClassNotFoundException {
    mName = in.readUTF();
  }

  @Override
  public void write(ObjectOutputStream out) throws IOException {
    out.writeUTF(mName);
  }

  @Override
  public String getUserPresentableClassName() {
    return FilterFilterComponent.LOCALIZER.msg("compName", "Filter");
  }
  
  @Override
  public JPanel getSettingsPanel() {
    loadFilter();
    
    JPanel global = new JPanel(new BorderLayout());
    
    JPanel settings = new JPanel(new BorderLayout(5,5));
    
    JLabel info = new JLabel(FilterFilterComponent.LOCALIZER.msg("settings.info", "Select filter to use for this component."));
    info.setAlignmentX(JLabel.LEFT_ALIGNMENT);
    
    mFilterSelection = new JComboBox(Plugin.getPluginManager().getFilterManager().getAvailableFilters());
    mFilterSelection.setSelectedItem(mFilter);
    mFilterSelection.setAlignmentX(JLabel.LEFT_ALIGNMENT);
    
    settings.add(info, BorderLayout.NORTH);
    
    JPanel center = new JPanel(new BorderLayout());
    center.add(mFilterSelection, BorderLayout.NORTH);
    
    settings.add(center, BorderLayout.CENTER);
    
    global.add(settings, BorderLayout.WEST);
    
    return global;
  }
  
  @Override
  public void saveSettings() {
    if(mFilterSelection != null) {
      mFilter = (ProgramFilter)mFilterSelection.getSelectedItem();
      mName = mFilter.getName();
    }
  }
}
