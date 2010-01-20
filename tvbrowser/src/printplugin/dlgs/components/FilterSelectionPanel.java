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
 */
package printplugin.dlgs.components;

import javax.swing.JComboBox;
import javax.swing.JPanel;

import util.ui.Localizer;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import devplugin.Plugin;
import devplugin.ProgramFilter;

/**
 * A panel with the settings of the
 * program filter for the printing.
 * 
 * @author René Mach
 * @since 2.5
 */
public class FilterSelectionPanel extends JPanel {
  private static final Localizer mLocalizer = Localizer.getLocalizerFor(FilterSelectionPanel.class);
  private JComboBox mFilterSelection;
  
  public FilterSelectionPanel() {
    CellConstraints cc = new CellConstraints();
    
    PanelBuilder pb = new PanelBuilder(new FormLayout("5dlu,pref,10dlu,30dlu:grow",
        "pref,5dlu,pref,2dlu,pref,10dlu"), this);
    pb.addSeparator(mLocalizer.msg("filters","Filters"), cc.xyw(1,1,4));
    pb.addLabel(mLocalizer.msg("toUseFilter","To use program filter:"), cc.xy(2,3));
    
    mFilterSelection = new JComboBox(Plugin.getPluginManager().getFilterManager().getAvailableFilters());
    mFilterSelection.setSelectedItem(Plugin.getPluginManager().getFilterManager().getCurrentFilter());

    pb.add(mFilterSelection, cc.xy(4,3));
  }
  
  public ProgramFilter getSelectedFilter() {
    return (ProgramFilter)mFilterSelection.getSelectedItem();
  }
}
