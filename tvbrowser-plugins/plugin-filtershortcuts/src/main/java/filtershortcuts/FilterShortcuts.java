/*
 * FilterShortcuts plugin for TV-Browser
 * Copyright (C) 2017 René Mach (rene@tvbrowser.org)
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
package filtershortcuts;

import java.awt.Component;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashSet;
import java.util.Iterator;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import devplugin.Plugin;
import devplugin.PluginInfo;
import devplugin.ProgramFilter;
import devplugin.SettingsTab;
import devplugin.Version;
import util.ui.Localizer;
import util.ui.UiUtilities;

/**
 * A TV-Browser plugin that allows to add keyboard shortcuts
 * to TV-Browser to quick activate certain filters.
 * 
 * @author René Mach
 */
public class FilterShortcuts extends Plugin {
  private static final Localizer LOCALIZER = Localizer.getLocalizerFor(FilterShortcuts.class);
  private static final Version VERSION = new Version(0, 10, false);
  private HashSet<FilterShortcut> mShortcutSet;
  
  public FilterShortcuts() {
    mShortcutSet = new HashSet<FilterShortcut>();
  }
  
  public static Version getVersion() {
    return VERSION;
  }
  
  @Override
  public PluginInfo getInfo() {
    return new PluginInfo(FilterShortcuts.class, LOCALIZER.msg("name", "Filter shortcuts"), LOCALIZER.msg("desc", "Allows to create shortcuts for filter activation."), "Ren\u00E9 Mach", "GPL v3");
  }
  
  @Override
  public void writeData(ObjectOutputStream out) throws IOException {
    //version
    out.writeInt(1);
    out.writeInt(mShortcutSet.size());
    
    Iterator<FilterShortcut> shortCuts = mShortcutSet.iterator();
    
    while(shortCuts.hasNext()) {
      shortCuts.next().write(out);
    }
  }
  
  @Override
  public void readData(ObjectInputStream in) throws IOException, ClassNotFoundException {
    //version
    in.readInt();
    
    int n = in.readInt();
    
    for(int i = 0; i < n; i++) {
      mShortcutSet.add(new FilterShortcut(in));
    }
  }
  
  @Override
  public void handleTvBrowserStartFinished() {
    removeShortcuts();
    addShortcuts();
  }
  
  @Override
  public void onDeactivation() {
    removeShortcuts();
  }
  
  private void removeShortcuts() {
    final InputMap inputMap = ((JFrame)getParentFrame()).getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).getParent();
    final ActionMap actionMap = ((JFrame)getParentFrame()).getRootPane().getActionMap().getParent();
    
    for(final FilterShortcut s : mShortcutSet) {
      final Object o = inputMap.get(s.getKeyStroke());
      
      inputMap.remove(s.getKeyStroke());
      
      actionMap.remove(o);
    }
  }
  
  private void addShortcuts() {
    final InputMap inputMap = ((JFrame)getParentFrame()).getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).getParent();
    final ActionMap actionMap = ((JFrame)getParentFrame()).getRootPane().getActionMap().getParent();

    for(final FilterShortcut s : mShortcutSet) {
      final String key = FilterShortcuts.class.getCanonicalName()+"_"+s.mFilterName;
      
      inputMap.put(s.getKeyStroke(), key);
      actionMap.put(key, new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
          s.handleAction();
        }
      });
    }
  }
  @Override
  public SettingsTab getSettingsTab() {
    return new SettingsTab() {
      private JList mList;
      
      @Override
      public void saveSettings() {
        removeShortcuts();
        mShortcutSet.clear();
        
        for(int i = 0; i < mList.getModel().getSize(); i++) {
          final FilterShortcut s = (FilterShortcut)mList.getModel().getElementAt(i);
          
          if(s.isValid()) {
            mShortcutSet.add(s);
          }
        }
        
        addShortcuts();
      }
      
      @Override
      public String getTitle() {
        return null;
      }
      
      @Override
      public Icon getIcon() {
        return null;
      }
      
      @Override
      public JPanel createSettingsPanel() {
        mList = new JList();
        
        ProgramFilter[] filters = getPluginManager().getFilterManager().getAvailableFilters();
        
        DefaultListModel m = new DefaultListModel();
        
        for(ProgramFilter f : filters) {
          FilterShortcut toAdd = null;
          
          for(FilterShortcut s : mShortcutSet) {
            if(s.isFilter(f)) {
              toAdd = s.copy();
            }
          }
          
          if(toAdd == null) {
            toAdd = new FilterShortcut(f);
          }
          
          m.addElement(toAdd);
        }
        
        final InputMap iMap = ((JFrame)getParentFrame()).getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        final KeyStroke[] strokes = iMap.keys();
        
        mList.setCellRenderer(new ListCellRenderer() {
          @Override
          public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            final JLabel filter = new JLabel(((FilterShortcut)value).getFilterName());
            final JLabel shortCut = new JLabel(((FilterShortcut)value).getShortcutText());
            
            final JPanel p = new JPanel(new FormLayout("10dlu:grow,4dlu,100dlu","default"));
            p.add(filter, CC.xy(1, 1));
            p.add(shortCut, CC.xy(3, 1));
            
            if(isSelected) {
              p.setOpaque(true);
              p.setBackground(list.getSelectionBackground());
              filter.setForeground(list.getSelectionForeground());
              shortCut.setForeground(list.getSelectionForeground());
            }
            else {
              p.setOpaque(false);
              p.setBackground(list.getBackground());
              filter.setForeground(list.getForeground());
              shortCut.setForeground(list.getForeground());
            }
            
            return p;
          }
        });
        
        mList.setModel(m);
        mList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        mList.addKeyListener(new KeyAdapter() {
          @Override
          public void keyReleased(KeyEvent e) {
            FilterShortcut s = (FilterShortcut)mList.getSelectedValue();
            
            if(e.getKeyCode() == KeyEvent.VK_SHIFT ||
                e.getKeyCode() == KeyEvent.VK_ALT ||
                e.getKeyCode() == KeyEvent.VK_ALT_GRAPH ||
                e.getKeyCode() == KeyEvent.VK_CONTROL ||
                e.getKeyCode() == KeyEvent.VK_META ||
                e.getKeyCode() == KeyEvent.VK_WINDOWS) {
              if(s.mKeyCode == KeyEvent.VK_UNDEFINED) {
                s.mModifiersEx = 0;
                mList.repaint();
              }
            }
            
          }
          
          @Override
          public void keyPressed(KeyEvent e) {
            if(e.getKeyCode() != KeyEvent.VK_UNDEFINED &&
                !((e.getModifiers() & Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()) == Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() &&
                (e.getKeyCode() == KeyEvent.VK_V || e.getKeyCode() == KeyEvent.VK_C ||
                e.getKeyCode() == KeyEvent.VK_X || e.getKeyCode() == KeyEvent.VK_A))) {
              FilterShortcut s = (FilterShortcut)mList.getSelectedValue();
              
              if(e.getKeyCode() == KeyEvent.VK_SHIFT ||
                  e.getKeyCode() == KeyEvent.VK_ALT ||
                  e.getKeyCode() == KeyEvent.VK_ALT_GRAPH ||
                  e.getKeyCode() == KeyEvent.VK_CONTROL ||
                  e.getKeyCode() == KeyEvent.VK_META ||
                  e.getKeyCode() == KeyEvent.VK_WINDOWS) {
                s.mModifiersEx = e.getModifiersEx();
                s.mKeyCode = KeyEvent.VK_UNDEFINED;
              }
              else {
                boolean found = (Character.isLetterOrDigit(e.getKeyChar()) || e.getKeyCode() == KeyEvent.VK_SPACE || e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_DOWN || e.getKeyCode() == KeyEvent.VK_PAGE_UP || e.getKeyCode() == KeyEvent.VK_PAGE_DOWN)
                    && !((e.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) == KeyEvent.CTRL_DOWN_MASK)
                    && !((e.getModifiersEx() & KeyEvent.META_DOWN_MASK) == KeyEvent.META_DOWN_MASK)
                    && !((e.getModifiersEx() & KeyEvent.ALT_DOWN_MASK) == KeyEvent.ALT_DOWN_MASK)
                    && !((e.getModifiersEx() & KeyEvent.ALT_GRAPH_DOWN_MASK) == KeyEvent.ALT_GRAPH_DOWN_MASK);
                
                if(!found) { 
                  KeyStroke keyPressed = KeyStroke.getKeyStroke(e.getKeyCode(), e.getModifiersEx());
                  
                  for(KeyStroke test : strokes) {
                    if(test.getModifiers() == keyPressed.getModifiers()
                        && test.getKeyCode() == keyPressed.getKeyCode()) {
                      found = true;
                      break;
                    }
                  }
                  
                  if(!found) {
                    for(int i = 0; i < mList.getModel().getSize(); i++) {
                      FilterShortcut t = (FilterShortcut)mList.getModel().getElementAt(i);
                      
                      if(!t.equals(s) && t.mModifiersEx == e.getModifiersEx()
                          && t.mKeyCode == e.getKeyCode()) {
                        found = true;
                        break;
                      }
                    }
                  }
                }
                
                if(!found) {
                  s.mModifiersEx = e.getModifiersEx();
                  s.mKeyCode = e.getKeyCode();
                }
              }
              mList.repaint();
            }
          }
        });
        
        JPanel p = new JPanel(new FormLayout("5dlu,100dlu:grow","5dlu,default,2dlu,fill:100dlu:grow"));
        p.add(UiUtilities.createHelpTextArea(LOCALIZER.msg("settings.help", "Select an item in the list and press the keyboard shortcut on the keyboard. (To delete an keyboard shortcut select the item in the list and press the CTRL key on the keyboard.)")), CC.xy(2, 2));
        p.add(new JScrollPane(mList), CC.xy(2, 4));
        
        return p;
      }
    };
  }
  
  private static final class FilterShortcut {
    private ProgramFilter mFilter;
    private String mFilterName;
    private int mModifiersEx;
    private int mKeyCode;
    
    private FilterShortcut(ObjectInputStream in) throws IOException {
      mFilterName = in.readUTF();
      mModifiersEx = in.readInt();
      mKeyCode = in.readInt();
    }
    
    private FilterShortcut(ProgramFilter filter) {
      this(filter,0,KeyEvent.VK_UNDEFINED);
    }
    
    private FilterShortcut(ProgramFilter filter, KeyStroke stroke) {
      this(filter,stroke.getModifiers(),stroke.getKeyCode());
    }
    
    private FilterShortcut(ProgramFilter filter, int modifiersEx, int keyCode) {
      mFilter = filter;
      mFilterName = filter.getName();
      mModifiersEx = modifiersEx;
      mKeyCode = keyCode;
    }
    
    private FilterShortcut(String filterName, int modifiersEx, int keyCode) {
      mFilterName = filterName;
      mModifiersEx = modifiersEx;
      mKeyCode = keyCode;
      getFilter();
    }
    
    private String getFilterName() {
      return mFilterName;
    }
    
    private ProgramFilter getFilter() {
      if(mFilter == null) {
        ProgramFilter[] available = getPluginManager().getFilterManager().getAvailableFilters();
        for(ProgramFilter test : available) {
          if(test.getName().equals(mFilterName)) {
            mFilter = test; 
          }
        }
      }
      
      return mFilter;
    }
    
    private boolean isValid() {
      return getFilter() != null && mKeyCode != KeyEvent.VK_UNDEFINED;
    }
    
    private void handleAction() {
      final ProgramFilter filter = getFilter();
      
      if(filter != null) {
        if(filter.equals(getPluginManager().getFilterManager().getCurrentFilter())) {
          getPluginManager().getFilterManager().setCurrentFilter(getPluginManager().getFilterManager().getDefaultFilter());
        }
        else {
          getPluginManager().getFilterManager().setCurrentFilter(filter);
        }
      }
    }
    
    private String getShortcutText() {
      StringBuilder text = new StringBuilder();
      
      if(mModifiersEx != 0) {
        text.append(KeyEvent.getModifiersExText(mModifiersEx));
      }
      
      if(mKeyCode != KeyEvent.VK_UNDEFINED) {
        if(text.length() > 0) {
          text.append("+");
        }
        
        text.append(KeyEvent.getKeyText(mKeyCode));
      }
      
      return text.toString();
    }
    
    private KeyStroke getKeyStroke() {
      return mKeyCode != KeyEvent.VK_UNDEFINED ? KeyStroke.getKeyStroke(mKeyCode, mModifiersEx) : null;
    }
    
    private void write(ObjectOutputStream out) throws IOException {
      out.writeUTF(mFilterName);
      out.writeInt(mModifiersEx);
      out.writeInt(mKeyCode);
    }
    
    private FilterShortcut copy() {
      return new FilterShortcut(mFilterName, mModifiersEx, mKeyCode);
    }
    
    private boolean isFilter(ProgramFilter filter) {
      return (filter != null && mFilterName.equals(filter.getName()));
    }
  }
  
  public String getPluginCategory() {
    return "misc";
  }
}
