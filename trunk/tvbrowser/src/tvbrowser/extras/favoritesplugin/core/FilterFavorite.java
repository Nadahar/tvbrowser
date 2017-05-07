/*
 * TV-Browser
 * Copyright (C) 2014 TV-Browser team (dev@tvbrowser.org)
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
package tvbrowser.extras.favoritesplugin.core;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import devplugin.Plugin;
import devplugin.PluginManager;
import devplugin.Program;
import devplugin.ProgramFilter;
import tvbrowser.core.filters.PluginFilter;
import tvbrowser.core.filters.SeparatorFilter;
import tvbrowser.core.filters.ShowAllFilter;
import tvbrowser.core.filters.UserFilter;
import tvbrowser.extras.favoritesplugin.FavoriteConfigurator;
import tvbrowser.extras.favoritesplugin.FavoritesPlugin;
import tvbrowser.ui.filter.dlgs.SelectFilterDlg;
import tvbrowser.ui.mainframe.MainFrame;
import util.exc.TvBrowserException;
import util.ui.Localizer;
import util.ui.SearchFormSettings;
import util.ui.UiUtilities;

/**
 * A Favorite with usage of filter for program matching.
 * 
 * @author René Mach
 */
public class FilterFavorite extends Favorite implements PendingFilterLoader {
  public static final Localizer LOCALIZER = Localizer.getLocalizerFor(FilterFavorite.class);
  
  public static final String TYPE_ID = "filterFavorite";
  
  private String mFilterName;
  private ProgramFilter mFilterInstance;
  private boolean mFilterIsAcceptable;
  
  public FilterFavorite() {
    mFilterName = "";
    mFilterIsAcceptable = false;
    
    setName(mFilterName);
    
    mSearchFormSettings = createSearchFormSettings();
  }
  
  public FilterFavorite(ProgramFilter filter) {
    mFilterName = filter.getName();
    mFilterIsAcceptable = filterIsAcceptable(filter);
    
    setName(mFilterName);
    
    if(mFilterIsAcceptable) {
      mFilterInstance = filter;
    }
    
    mSearchFormSettings = createSearchFormSettings();
  }
  
  public FilterFavorite(ObjectInputStream in) throws IOException, ClassNotFoundException {
    super(in);
    
    in.readInt(); // read version
    mFilterName = in.readUTF();
    mFilterIsAcceptable = true;
    
    mSearchFormSettings = createSearchFormSettings();
    
    FavoritesPlugin.getInstance().addPendingFavorite(this);
  }
  
  private SearchFormSettings createSearchFormSettings() {
    SearchFormSettings formSettings = new SearchFormSettings(".*"); // We match all programs to check them later
    formSettings.setSearchIn(SearchFormSettings.SEARCH_IN_TITLE);
    formSettings.setSearcherType(PluginManager.TYPE_SEARCHER_REGULAR_EXPRESSION);
    return formSettings;
  }
  
  public String getFilterName() {
    return mFilterName;
  }
  
  private boolean filterIsAcceptable(ProgramFilter filter) {
    return (!(filter instanceof FavoriteFilter) && (!(filter instanceof ShowAllFilter || filter instanceof SeparatorFilter || filter instanceof PluginFilter)) && (!(filter instanceof UserFilter) || ((UserFilter)filter).acceptableForFilterFavorite()));      
  }
  
  public ProgramFilter getProgramFilter() {
    if(mFilterInstance == null) {
      ProgramFilter[] filters = Plugin.getPluginManager().getFilterManager().getAvailableFilters();
      
      for(ProgramFilter filter : filters) {
        if(filter != null && filter.getName().equals(mFilterName)) {
          if(filterIsAcceptable(filter)) {
            mFilterInstance = filter;
          }
          else {
            mFilterIsAcceptable = false;
          }
          
          break;
        }
      }
    }
    
    return mFilterInstance;
  }
  
  @Override
  protected Program[] internalSearchForPrograms() throws TvBrowserException {
    ArrayList<Program> foundPrograms = new ArrayList<Program>();

    ProgramFilter test = getProgramFilter();
    
    if(mFilterIsAcceptable && test != null) {
      Program[] programs = super.internalSearchForPrograms();
      
      for(Program prog : programs) {
        if(test.accept(prog)) {
          foundPrograms.add(prog);
        }
      }
    }
    
    return foundPrograms.toArray(new Program[foundPrograms.size()]);
  }
  
  @Override
  public boolean matches(Program p) {
    ProgramFilter test = getProgramFilter();
    
    if(mFilterIsAcceptable && test != null) {
      return test.accept(p);
    }
    
    return false;
  }
  
  @Override
  public String getTypeID() {
    return TYPE_ID;
  }

  @Override
  public FavoriteConfigurator createConfigurator() {
    return new Configurator();
  }

  @Override
  protected void internalWriteData(ObjectOutputStream out) throws IOException {
    // write version
    out.writeInt(1);
    out.writeUTF(mFilterName);
  }
  
  class Configurator implements FavoriteConfigurator {
    private JComboBox<ProgramFilter> mFilterSelection;
    
    @Override
    public JPanel createConfigurationPanel() {
      PanelBuilder pb = new PanelBuilder(new FormLayout("default:grow,3dlu,default","default,3dlu,default"));
      pb.border(Borders.createEmptyBorder("2dlu,0dlu,2dlu,0dlu"));
      
      ArrayList<ProgramFilter> selectableFilter = new ArrayList<ProgramFilter>();
      
      ProgramFilter[] availableFilters = Plugin.getPluginManager().getFilterManager().getAvailableFilters();
      
      for(ProgramFilter filter : availableFilters) {
        if(filterIsAcceptable(filter)) {
          selectableFilter.add(filter);
        }
      }
      
      mFilterSelection = new JComboBox<>(selectableFilter.toArray(new ProgramFilter[selectableFilter.size()]));
      
      if(mFilterInstance != null) {
        mFilterSelection.setSelectedItem(mFilterInstance);
      }
      
      JButton editFilter = new JButton(SelectFilterDlg.mLocalizer.msg("title", "Edit Filters"));
      editFilter.addActionListener(e -> {
        SelectFilterDlg filterDlg = SelectFilterDlg.create(UiUtilities.getLastModalChildOf(MainFrame.getInstance()));
        filterDlg.setVisible(true);
        
        Object selected = mFilterSelection.getSelectedItem();
        
        ((DefaultComboBoxModel<ProgramFilter>)mFilterSelection.getModel()).removeAllElements();
        
        ProgramFilter[] availableFilter = Plugin.getPluginManager().getFilterManager().getAvailableFilters();
        
        for(ProgramFilter filter : availableFilter) {
          if(!(filter instanceof FavoriteFilter)) {
            ((DefaultComboBoxModel<ProgramFilter>)mFilterSelection.getModel()).addElement(filter);
          }
        }
        
        mFilterSelection.setSelectedItem(selected);
      });
      
      pb.addLabel(LOCALIZER.msg("message", "Programs that are accepted by this filter will be marked as Favorite:"), CC.xyw(1, 1, 3));
      pb.add(mFilterSelection, CC.xy(1,3));
      pb.add(editFilter, CC.xy(3, 3));
      
      return pb.getPanel();
    }

    @Override
    public void save() {
      ProgramFilter filter = (ProgramFilter)mFilterSelection.getSelectedItem();
      
      if(filter != null) {
        mFilterName = filter.getName();
        setName(mFilterName);
        
        mFilterIsAcceptable = filterIsAcceptable(filter);
        
        if(mFilterIsAcceptable) {
          mFilterInstance = filter;        
        }
        else {
          mFilterInstance = null;
        }
      }
    }

    @Override
    public boolean check() {
      if(!(mFilterSelection.getSelectedItem() != null && filterIsAcceptable((ProgramFilter)mFilterSelection.getSelectedItem()))) {
        JOptionPane.showMessageDialog(mFilterSelection,
            LOCALIZER.msg("notAcceptable.message", "The current filter cannot be accepted by this Favorite!"),
            LOCALIZER.msg("notAcceptable.title", "Invalid filter"),
          JOptionPane.WARNING_MESSAGE);
        return false;
      }
      
      return true; 
    }
  }

  @Override
  public void loadPendingFilter() {
    getProgramFilter();
  }

  @Override
  public boolean isValidSearch() {
    return mFilterIsAcceptable;
  }
  
  public void updateFilter(ProgramFilter filter) {
    if((mFilterInstance != null && (mFilterInstance.equals(filter)) || mFilterName.equals(filter.getName()))) {
      mFilterName = filter.getName();
      mFilterIsAcceptable = filterIsAcceptable(filter);
      
      if(mFilterIsAcceptable) {
        mFilterInstance = filter;
      }
      else {
        mFilterInstance = null;
      }
      
      try {
        updatePrograms();
      } catch (TvBrowserException e) {}
    }
  }
  
  public void deleteFilter(ProgramFilter filter) {
    if((mFilterInstance != null && (mFilterInstance.equals(filter)) || mFilterName.equals(filter.getName()))) {
      mFilterIsAcceptable = false;
      mFilterInstance = null;
      
      try {
        updatePrograms();
      } catch (TvBrowserException e) {}
    }
  }
}
