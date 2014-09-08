/*
 * Filter Info Icon plugin for TV-Browser
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
package filterinfoicon;

import java.awt.Dialog.ModalityType;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JRootPane;

import tvdataservice.MutableChannelDayProgram;
import util.io.IOUtilities;
import util.ui.Localizer;
import util.ui.TVBrowserIcons;
import util.ui.UiUtilities;
import util.ui.WindowClosingIf;

import com.jgoodies.forms.factories.Borders;

import devplugin.ActionMenu;
import devplugin.ContextMenuAction;
import devplugin.Date;
import devplugin.FilterChangeListenerV2;
import devplugin.Plugin;
import devplugin.PluginInfo;
import devplugin.Program;
import devplugin.ProgramFilter;
import devplugin.Version;

/**
 * A TV-Browser plugin that uses filters to search for programs to
 * add info icons to those programs and send them to other Plugins.
 * 
 * @author René Mach
 */
public class FilterInfoIcon extends Plugin implements FilterChangeListenerV2 {
  static final Localizer LOCALIZER = Localizer.getLocalizerFor(FilterInfoIcon.class);
  private static final Version VERSION = new Version(0,10,5,false);
  private static Icon DEFAULT_ICON;
  private HashSet<FilterEntry> mFilterSet;
  private static String LAST_USED_ICON_PATH;
  
  public FilterInfoIcon() {
    DEFAULT_ICON = UiUtilities.scaleIcon(createImageIcon("status", "view-filter-set", TVBrowserIcons.SIZE_SMALL), 13);
    mFilterSet = new HashSet<FilterEntry>();
    LAST_USED_ICON_PATH = null;
  }
    
  public static Version getVersion() {
    return VERSION;
  }
  
  @Override
  public PluginInfo getInfo() {
    return new PluginInfo(FilterInfoIcon.class,LOCALIZER.msg("name", "Filter Info Icons"),LOCALIZER.msg("description", "Adds info icons to programs of selected filters and sends the programs to other plugins."),"René Mach","GPL");
  }
  
  static Icon getDefaultIcon() {
    return DEFAULT_ICON;
  }
  
  @Override
  public ActionMenu getButtonAction() {
    ContextMenuAction action = new ContextMenuAction(LOCALIZER.msg("edit", "Edit filter info icons"),TVBrowserIcons.filter(TVBrowserIcons.SIZE_SMALL));
    action.putValue(Plugin.BIG_ICON, TVBrowserIcons.filter(TVBrowserIcons.SIZE_LARGE));
    
    action.setActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {try {
        final JDialog edit = new JDialog(UiUtilities.getLastModalChildOf(getParentFrame()),LOCALIZER.msg("edit", "Edit filter info icons"),ModalityType.APPLICATION_MODAL);
        
        JButton ok = new JButton(Localizer.getLocalization(Localizer.I18N_OK));
        ok.addActionListener(new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent e) {
            edit.dispose();
          }
        });
        
        FilterInfoIconManagePanel manage = new FilterInfoIconManagePanel(mFilterSet,edit,ok);
        manage.setBorder(Borders.DIALOG);
        
        edit.setContentPane(manage);
        
        layoutWindow("editFilterIconDialog", edit, new Dimension(600, 200));
        
        UiUtilities.registerForClosing(new WindowClosingIf() {
          @Override
          public JRootPane getRootPane() {
            return edit.getRootPane();
          }
          
          @Override
          public void close() {
            edit.dispose();
          }
        });
        
        edit.setVisible(true);}catch(Throwable t) {t.printStackTrace();}
      }
    });
    
    return new ActionMenu(action);
  }
  
  @Override
  public void handleTvDataUpdateStarted(Date until) {
    for(Iterator<FilterEntry> it = mFilterSet.iterator(); it.hasNext();) {
      it.next().handleTvDataUpdateStarted();
    }
  }
  
  @Override
  public void handleTvDataAdded(MutableChannelDayProgram newProg) {
    for(Iterator<FilterEntry> it = mFilterSet.iterator(); it.hasNext();) {
      it.next().handleTvDataAdded(newProg);
    }
  }
  
  @Override
  public void handleTvDataUpdateFinished() {
    for(Iterator<FilterEntry> it = mFilterSet.iterator(); it.hasNext();) {
      it.next().handleTvDataUpdateFinished();
    }
  }
  
  
  @Override
  public String getProgramTableIconText() {
    return LOCALIZER.msg("name", "Filter Info Icons");
  }
  
  @Override
  public Icon[] getProgramTableIcons(Program program) {
    if(getPluginManager().getExampleProgram().equals(program)) {
      return new Icon[] {DEFAULT_ICON};
    }
    
    ArrayList<Icon> shownEntryIcons = new ArrayList<Icon>();
    Icon useDefault = null;
    
    for(Iterator<FilterEntry> it = mFilterSet.iterator(); it.hasNext();) {
      FilterEntry entry = it.next();
      
      if(entry.accepts(program)) {
        if(entry.getIcon() != null) {
          shownEntryIcons.add(entry.getIcon());
        }
        else {
          useDefault = DEFAULT_ICON;
        }
      }
    }
    
    if(useDefault != null) {
      shownEntryIcons.add(useDefault);
    }
    
    if(!shownEntryIcons.isEmpty()) {
      return shownEntryIcons.toArray(new Icon[shownEntryIcons.size()]);
    }
    
    return null;
  }
  
  
  @Override
  public String getPluginCategory() {
    return Plugin.OTHER_CATEGORY;
  }
  
  @Override
  public void writeData(ObjectOutputStream out) throws IOException {
    out.writeInt(1); // write version
    
    out.writeInt(mFilterSet.size());
    
    for(Iterator<FilterEntry> it = mFilterSet.iterator(); it.hasNext();) {
      it.next().writeData(out);
    }
    
    out.writeBoolean(LAST_USED_ICON_PATH != null);
    
    if(LAST_USED_ICON_PATH != null) {
      out.writeUTF(IOUtilities.checkForRelativePath(LAST_USED_ICON_PATH));
    }
  }
  
  @Override
  public void handleTvBrowserStartFinished() {
    getPluginManager().getFilterManager().registerFilterChangeListener(this);
  }
  
  @Override
  public void onDeactivation() {
    getPluginManager().getFilterManager().unregisterFilterChangeListener(this);
  }
  
  @Override
  public void readData(ObjectInputStream in) throws IOException, ClassNotFoundException {
    in.readInt(); // read version
    
    int n = in.readInt();
    
    for(int i = 0; i < n; i++) {
      mFilterSet.add(new FilterEntry(in));
    }
    
    if(in.readBoolean()) {
      LAST_USED_ICON_PATH = IOUtilities.translateRelativePath(in.readUTF());
    }
  }

  @Override
  public void filterAdded(ProgramFilter filter) {
    for(Iterator<FilterEntry> it = mFilterSet.iterator(); it.hasNext();) {
      it.next().checkFilterAdded(filter);
    }
  }

  @Override
  public void filterRemoved(ProgramFilter filter) {
    for(Iterator<FilterEntry> it = mFilterSet.iterator(); it.hasNext();) {
      it.next().deleteFilter(filter);
    }
  }

  static String getLastIconPath() {
    return LAST_USED_ICON_PATH;
  }
  
  static void setLastIconPath(String path) {
    LAST_USED_ICON_PATH = path;
  }
  
  @Override
  public void filterTouched(ProgramFilter filter) {
    for(Iterator<FilterEntry> it = mFilterSet.iterator(); it.hasNext();) {
      it.next().checkFilter(filter);
    }
  }

  @Override
  public void filterDefaultChanged(ProgramFilter filter) {}
}
