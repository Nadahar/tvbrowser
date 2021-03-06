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

package tvbrowser.extras.favoritesplugin.core;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import devplugin.Channel;
import devplugin.Date;
import devplugin.Plugin;
import devplugin.PluginManager;
import devplugin.Program;
import devplugin.ProgramFilter;
import tvbrowser.extras.favoritesplugin.FavoriteConfigurator;
import tvbrowser.extras.favoritesplugin.FavoritesPlugin;
import tvbrowser.ui.filter.dlgs.SelectFilterDlg;
import tvbrowser.ui.mainframe.MainFrame;
import util.exc.ErrorHandler;
import util.exc.TvBrowserException;
import util.ui.SearchForm;
import util.ui.SearchFormSettings;
import util.ui.UiUtilities;
import util.ui.WrapperFilter;

public class AdvancedFavorite extends Favorite implements PendingFilterLoader {

  private static final util.ui.Localizer mLocalizer
        = util.ui.Localizer.getLocalizerFor(AdvancedFavorite.class);

  public static final String TYPE_ID = "advanced";

  private ProgramFilter mFilter;
  private String mFilterName;

  private String mPendingFilterName = null;

  private AdvancedFavorite() {
    super();
  }

  public AdvancedFavorite(ObjectInputStream in) throws IOException, ClassNotFoundException {
    super(in);
    int version = in.readInt();   // version
    mSearchFormSettings = new SearchFormSettings(in);
    if (version > 1) {
      boolean useFilter = in.readBoolean();

      if (useFilter) {
        mPendingFilterName = (String)in.readObject();
        FavoritesPlugin.getInstance().addPendingFavorite(this);
      }
    }
  }

  public AdvancedFavorite(String searchText) {
    super();
    mSearchFormSettings = new SearchFormSettings(searchText);
    mSearchFormSettings.setUserDefaultFieldTypes(FavoritesPlugin.getInstance().getDefaultProgramFieldTypeSelection());
    if (searchText.contains(" OR ") || searchText.contains(" AND ")
        || searchText.contains(" NOT ")) {
      mSearchFormSettings.setSearcherType(PluginManager.TYPE_SEARCHER_BOOLEAN);
      mSearchFormSettings.setSearchIn(SearchFormSettings.SEARCH_IN_ALL);
    }
  }

  @Override
  public String getTypeID() {
    return TYPE_ID;
  }

  @Override
  public String getName() {
    if (super.getName() != null) {
      return super.getName();
    }
    return mSearchFormSettings.getSearchText();
  }

  @Override
  public FavoriteConfigurator createConfigurator() {
    return new Configurator();
  }

  @Override
  protected void internalWriteData(ObjectOutputStream out) throws IOException {
    out.writeInt(2); // version
    mSearchFormSettings.writeData(out);
    out.writeBoolean(mFilter != null || mPendingFilterName != null);

    if (mFilter != null) {
      out.writeObject(mFilter.getName());
    }
    else if (mPendingFilterName != null) {
      out.writeObject(mPendingFilterName);
    }
  }

  /**
   * Loads an old version favorite.
   * <p>
   * @param in The stream to read from.
   * @return The loaded AdvancedFavorite.
   * @throws IOException Thrown if something went wrong.
   * @throws ClassNotFoundException Thrown if somethinge went wrong.
   */
  public static AdvancedFavorite loadOldFavorite(ObjectInputStream in) throws IOException, ClassNotFoundException {
    AdvancedFavorite favorite = new AdvancedFavorite();
    favorite.readOldFavorite(in);

    return favorite;
  }

  private void readOldFavorite(ObjectInputStream in) throws IOException, ClassNotFoundException {
    int version = in.readInt();

    if (version <= 2) {
      String term = (String) in.readObject();
      in.readBoolean(); // searchInTitle
      boolean searchInText = in.readBoolean();
      int searchMode = in.readInt();

      mSearchFormSettings.setSearchText(term);
      if (searchInText) {
        mSearchFormSettings.setSearchIn(SearchFormSettings.SEARCH_IN_ALL);
      } else {
        mSearchFormSettings.setSearchIn(SearchFormSettings.SEARCH_IN_TITLE);
      }

      switch (searchMode) {
        case 1: mSearchFormSettings.setSearcherType(PluginManager.TYPE_SEARCHER_EXACTLY); break;
        case 2: mSearchFormSettings.setSearcherType(PluginManager.TYPE_SEARCHER_KEYWORD); break;
        case 3: mSearchFormSettings.setSearcherType(PluginManager.TYPE_SEARCHER_REGULAR_EXPRESSION); break;
        default: mSearchFormSettings.setSearcherType(PluginManager.TYPE_SEARCHER_EXACTLY); break;
      }
    } else {
      mSearchFormSettings = new SearchFormSettings(in);
    }

    if (version >= 5) {
      super.setName((String) in.readObject());
    }
    else {
      super.setName(mSearchFormSettings.getSearchText());
    }

    boolean useCertainChannel = in.readBoolean();

    if (version < 6) {
      String certainChannelServiceClassName = (String) in.readObject();
      String certainChannelId;
      if (version == 1) {
        certainChannelId = Integer.toString(in.readInt());
      } else {
        certainChannelId = (String) in.readObject();
      }
      Channel ch = Channel.getChannel(certainChannelServiceClassName, null, null, certainChannelId);
      if (ch != null) {
        getLimitationConfiguration().setChannels(new Channel[]{ch});
      }
    }
    else {
      if (useCertainChannel) {
        int cnt = in.readInt();
        ArrayList<Channel> list = new ArrayList<Channel>();
        for (int i = 0; i < cnt; i++) {
          String certainChannelServiceClassName = (String) in.readObject();
          String certainChannelId;
          if (version == 1) {
            certainChannelId = Integer.toString(in.readInt());
          }else{
            certainChannelId = (String) in.readObject();
          }

          Channel channel = Channel.getChannel(certainChannelServiceClassName, null, null, certainChannelId);
          if (channel != null) {
            list.add(channel);
          }
        }

        getLimitationConfiguration().setChannels(list.toArray(new Channel[list.size()]));
      }
    }



    boolean useCertainTimeOfDay = in.readBoolean();
    int certainFromTime = in.readInt();
    int certainToTime = in.readInt();

    if (useCertainTimeOfDay) {
      getLimitationConfiguration().setTime(certainFromTime, certainToTime);
    }


    // Don't save the programs but only their date and id
    int size = in.readInt();
    ArrayList<Program> programList = new ArrayList<Program>(size);
    for (int i = 0; i < size; i++) {
      Date date = Date.readData(in);
      String progID = (String) in.readObject();
      Program program = Plugin.getPluginManager().getProgram(date, progID);
      if (program != null) {
        programList.add(program);
      }
    }

    Program[] mProgramArr = new Program[programList.size()];
    programList.toArray(mProgramArr);

    if (version >= 4) {
      boolean useFilter = in.readBoolean();  // useFilter
      mFilterName = mPendingFilterName = (String) in.readObject();

      if (useFilter) {
        FavoritesPlugin.getInstance().addPendingFavorite(this);
      } else {
        mPendingFilterName = null;
      }
    }
    else {
      mFilter = null;
      mFilterName = null;
    }

    if (version >= 7) {
      size = in.readInt();
      for (int i = 0; i < size; i++) {
        /* For compatibility reasons we read the programs here.
           Later we perform an complete refresh.
         */
        Date.readData(in); // program date
        in.readObject(); // program id
      }
    }

    getReminderConfiguration().setReminderServices(new String[] {});

    try {
      this.updatePrograms();
    }
    catch (final TvBrowserException exc) {
      ErrorHandler.handle("Could not update favorites.", exc);

    }

  }


  /**
   * Returns a specific Filter.
   * @param name Name of the Filter
   * @return The Filter if found, otherwise null
   */
  private static ProgramFilter getFilterByName(String name ){
    ProgramFilter[] flist = Plugin.getPluginManager().getFilterManager().getAvailableFilters();

    for (ProgramFilter filter : flist) {
      if (filter != null && filter.getName().equals(name)) {
        return filter;
      }
    }

    return null;
  }

  @Override
  protected Program[] internalSearchForPrograms() throws TvBrowserException {
    Program[] progArr = super.internalSearchForPrograms();

    if (mFilter != null) {
      ArrayList<Program> list = new ArrayList<Program>();
      for (Program element : progArr) {
        if (mFilter.accept(element)) {
          list.add(element);
        }
      }
      return list.toArray(new Program[list.size()]);
    }
    else {
      return progArr;
    }
  }

  @Override
  public boolean matches(Program p) throws TvBrowserException {
    return super.matches(p) && (mFilter == null || mFilter.accept(p));
  }

  class Configurator implements FavoriteConfigurator {

    private SearchForm mSearchForm;
    private JCheckBox mFilterCheckbox;
    private JComboBox<WrapperFilter> mFilterCombo;
    private JButton mEditFilter;

    public JPanel createConfigurationPanel() {
      mSearchForm = new SearchForm(true, false, false, SearchForm.LAYOUT_HORIZONTAL, true);
      mSearchForm.setSearchFormSettings(mSearchFormSettings);

      CellConstraints cc = new CellConstraints();
      PanelBuilder panelBuilder = new PanelBuilder(new FormLayout("pref:grow, 3dlu, pref:grow, 3dlu, default", "pref, 5dlu, pref"));

      panelBuilder.add(mSearchForm, cc.xyw(1, 1, 5));
      panelBuilder.add(mFilterCheckbox = new JCheckBox(mLocalizer.msg("useFilter","Use filter:")), cc.xy(1, 3));
      panelBuilder.add(mFilterCombo = new JComboBox<>(), cc.xy(3, 3));
      
      ProgramFilter[] availableFilter = Plugin.getPluginManager().getFilterManager().getAvailableFilters();
      
      for(ProgramFilter filter : availableFilter) {
        if(FilterFavorite.filterIsAcceptable(filter)) {
          ((DefaultComboBoxModel<WrapperFilter>)mFilterCombo.getModel()).addElement(new WrapperFilter(filter));
        }
      }
      
      mEditFilter = new JButton(SelectFilterDlg.mLocalizer.msg("title", "Edit Filters"));
      mEditFilter.addActionListener(e -> {
        SelectFilterDlg filterDlg = SelectFilterDlg.create(UiUtilities.getLastModalChildOf(MainFrame.getInstance()));
        filterDlg.setVisible(true);
        
        WrapperFilter selected = (WrapperFilter)mFilterCombo.getSelectedItem();
        
        ((DefaultComboBoxModel<WrapperFilter>)mFilterCombo.getModel()).removeAllElements();
        
        ProgramFilter[] availableFilter1 = Plugin.getPluginManager().getFilterManager().getAvailableFilters();
        
        int selectedIndex = -1;
        int index = 0;
        
        for(ProgramFilter filter : availableFilter1) {
          if(FilterFavorite.filterIsAcceptable(filter)) {
            if(selected.getFilter().equals(filter) || selected.getFilter().getName().equals(filter.getName())) {
              selectedIndex = index;
            }
            
            ((DefaultComboBoxModel<WrapperFilter>)mFilterCombo.getModel()).addElement(new WrapperFilter(filter));
            index++;
          }
        }

        if(selectedIndex != -1) {
          mFilterCombo.setSelectedIndex(selectedIndex);
        }
      });
      
      panelBuilder.add(mEditFilter, cc.xy(5, 3));
      
      if (mFilter != null) {
        mFilterCheckbox.setSelected(true);
        mFilterCombo.setSelectedItem(new WrapperFilter(mFilter));
      }
      else {
        mFilterCombo.setEnabled(false);
        mEditFilter.setEnabled(false);
      }

      mFilterCheckbox.addActionListener(e -> {
        mFilterCombo.setEnabled(mFilterCheckbox.isSelected());
        mEditFilter.setEnabled(mFilterCheckbox.isSelected());
      });

      return panelBuilder.getPanel();
    }

    public void save() {
      SearchFormSettings settings = mSearchForm.getSearchFormSettings();
      mSearchFormSettings.setCaseSensitive(settings.getCaseSensitive());
      mSearchFormSettings.setNrDays(settings.getNrDays());
      mSearchFormSettings.setSearcherType(settings.getSearcherType());
      mSearchFormSettings.setSearchIn(settings.getSearchIn());
      mSearchFormSettings.setSearchText(settings.getSearchText());
      mSearchFormSettings.setUserDefinedFieldTypes(settings.getUserDefinedFieldTypes());
      FavoritesPlugin.getInstance().setDefaultProgramFieldTypeSelection(settings.getUserDefaultFieldTypes());
      
      if (mFilterCheckbox.isSelected()) {
        mFilter = ((WrapperFilter)mFilterCombo.getSelectedItem()).getFilter();
        mFilterName = mFilter.getName();
        
        if(!FilterFavorite.filterIsAcceptable(mFilter)) {
          mFilter = null;
          mFilterName = null;
        }
      }
      else {
        mFilter = null;
        mFilterName = null;
      }
    }

    public boolean check() {
      if (mSearchForm.getSearchFormSettings().getSearchText().equals("")) {
        JOptionPane.showMessageDialog(mSearchForm,
            mLocalizer.msg("missingSearchText.message", "Please specify a search text for the favorite!"),
            mLocalizer.msg("missingSearchText.title", "Invalid search options"),
            JOptionPane.WARNING_MESSAGE);
        return false;
      }
      return true;
    }
  }

  /**
   * Loads the filters after TV-Browser start was finished.
   *
   * @since 2.5.1
   */
  public void loadPendingFilter() {
    if(mPendingFilterName != null) {
      try {
        mFilter = getFilterByName(mPendingFilterName);

        if(mFilter != null) {
          mPendingFilterName = null;
          
          if(!FilterFavorite.filterIsAcceptable(mFilter)) {
            mFilter = null;
          }
        }
        else {
          FavoritesPlugin.mLog.severe("Error on loading pending filter '" + mPendingFilterName + "' for Favorite: '" + getName() + "'. Filter was not found.");
        }
      }catch(Exception e) {
        ErrorHandler.handle("Error on loading pending filter '" + mPendingFilterName + "' for Favorite: '" + getName() + "'",e);
      }
    }
  }

  @Override
  public boolean isValidSearch() {
    return true;
  }
  
  public boolean updateFilter(ProgramFilter filter) {
    boolean result = (filter != null) && ((mFilter != null && mFilter.equals(filter)) || (mFilterName != null && mFilterName.equals(filter.getName()))); 
    
    if(result) {
      mFilterName = filter.getName();
      
      if(FilterFavorite.filterIsAcceptable(filter)) {
        mFilter = filter;
      }
      else {
        mFilter = null;
      }
    }
    
    result = updateFilterExclusion(filter,false) || result;
    
    if(result) {
      try {
        updatePrograms();
      } catch (TvBrowserException e) {}
    }
    
    return result;
  }
  
  public boolean deleteFilter(ProgramFilter filter) {
    boolean result = (filter != null) && ((mFilter != null && mFilter.equals(filter)) || (mFilterName != null && mFilterName.equals(filter.getName()))); 
    
    if(result) {
      mFilter = null;
      mFilterName = null;
    }
    
    result = deleteFilterExclusion(filter,false) || result;
    
    if(result) {
      try {
        updatePrograms();
      } catch (TvBrowserException e) {}
    }
    
    return result;
  }
}
