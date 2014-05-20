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
 *     $Date: 2014-05-20 10:08:41 +0200 (Di, 20 Mai 2014) $
 *   $Author: ds10 $
 * $Revision: 8078 $
 */
package util.ui;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.DefaultListModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import tvbrowser.core.filters.FilterManagerImpl;
import util.exc.TvBrowserException;
import util.settings.ProgramPanelSettings;
import util.ui.persona.Persona;
import util.ui.persona.PersonaListener;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

import devplugin.FilterChangeListener;
import devplugin.Program;
import devplugin.ProgramFilter;

/**
 * A JPanel with a program list with filter selection.
 * 
 * @author René Mach
 * @since 3.3.4
 */
public class FilterableProgramListPanel extends JPanel implements FilterChangeListener, PersonaListener {
  private static final Localizer LOCALIZER = Localizer.getLocalizerFor(FilterableProgramListPanel.class);
  
  private ProgramList mProgramList;
  private JScrollPane mProgramListScrollPane;
  
  private JLabel mFilterLabel;
  private JComboBox mFilterBox;
  private DefaultListModel mListModel;
  private JLabel mNumberLabel;
  
  private Program[] mAllPrograms;
  
  private static final Program[] EMPTY_PROGRAM_ARR = new Program[0];
  
  private boolean mShowDateSeparators;
  
  public FilterableProgramListPanel(Program[] programs, boolean showNumberOfPrograms, boolean showDateSeparators, ProgramPanelSettings progPanelSettings) {
    mListModel = new DefaultListModel();
    mProgramList = new ProgramList(mListModel, progPanelSettings);
    mShowDateSeparators = showDateSeparators;
    
    FilterManagerImpl.getInstance().registerFilterChangeListener(this);
    createGUI(showNumberOfPrograms);
    setPrograms(programs);
  }
  
  public void setShowDateSeparators(boolean showDateSeparators) {
    mShowDateSeparators = showDateSeparators;
    
    setPrograms(mAllPrograms);
  }
  
  public void setPrograms(Program[] programs) {
    if(programs == null) {
      programs = EMPTY_PROGRAM_ARR;
    }
    
    mAllPrograms = programs;
    
    filterPrograms((ProgramFilter)mFilterBox.getSelectedItem());
  }
  
  public void setListModel(DefaultListModel model) {
    mAllPrograms = new Program[model.size()];
    
    ProgramFilter filter = (ProgramFilter)mFilterBox.getSelectedItem();
    
    DefaultListModel newModel = new DefaultListModel();
    
    for(int i = 0; i < model.size(); i++) {
      mAllPrograms[i] = (Program)model.getElementAt(i);
    }
    
    for(Program p : mAllPrograms) {
      if(filter.accept(p)) {
        newModel.addElement(p);
      }
    }
    
    mListModel = newModel;
    mProgramList.setModel(newModel);
          
    if(mNumberLabel != null) {
      mNumberLabel.setText(LOCALIZER.msg("numberOfPrograms", "Number of shown programs: {0}", mListModel.size()));
    }
    
    if(mShowDateSeparators) {
      try {
        mProgramList.addDateSeparators();
      } catch (TvBrowserException e) {
        // ignore
      }
    }
  
    mProgramList.repaint();
  }
  
  public ProgramList getProgramList() {
    return mProgramList;
  }
  
  private void createGUI(boolean showNumberOfPrograms) {
    FormLayout layout = new FormLayout("default,3dlu,default:grow","default,3dlu,fill:default:grow");
    
    setLayout(layout);
    
    mFilterBox = new JComboBox();
    mFilterBox.addItemListener(new ItemListener() {
      @Override
      public void itemStateChanged(ItemEvent e) {
        if(e.getStateChange() == ItemEvent.SELECTED) {
          filterPrograms((ProgramFilter)mFilterBox.getSelectedItem());
          scrollToFirstNotExpiredIndex(false);
        }
      }
    });
    
    fillFilterBox();
    
    mFilterLabel = new JLabel(LOCALIZER.msg("filter", "Filter:"));
    
    int y = 1;
    
    add(mFilterLabel, CC.xy(1, y));
    add(mFilterBox, CC.xy(3, y++));
    
    y++;
    
    if(showNumberOfPrograms) {
      layout.insertRow(y, RowSpec.decode("default"));
      layout.insertRow(y+1, RowSpec.decode("3dlu"));
      
      mNumberLabel = new JLabel(LOCALIZER.msg("numberOfPrograms", "Number of shown programs: {0}", 0));
      
      add(mNumberLabel, CC.xyw(1, y++, 3));
      
      y++;
    }
    
    mProgramListScrollPane = new JScrollPane(mProgramList);
    mProgramListScrollPane.setBorder(null);
    
    add(mProgramListScrollPane, CC.xyw(1, y, 3));
    
    Persona.getInstance().registerPersonaListener(this);
  }
  
  private void fillFilterBox() {
    ProgramFilter[] filters = FilterManagerImpl.getInstance().getAvailableFilters();
    
    for(ProgramFilter filter : filters) {
      mFilterBox.addItem(filter);
    }
    
    mFilterBox.setSelectedItem(FilterManagerImpl.getInstance().getAllFilter());
  }

  @Override
  public void filterAdded(ProgramFilter filter) {
    mFilterBox.addItem(filter);
  }

  @Override
  public void filterRemoved(ProgramFilter filter) {
    if(mFilterBox.getSelectedItem().equals(filter)) {
      mFilterBox.setSelectedItem(FilterManagerImpl.getInstance().getAllFilter());
    }
    
    mFilterBox.removeItem(filter);
  }

  @Override
  public void filterTouched(ProgramFilter filter) {
    if(mFilterBox.getSelectedItem().equals(filter)) {
      filterPrograms(filter);
    }
    
    mFilterBox.updateUI();
  }
  
  private void filterPrograms(ProgramFilter filter) {
    if(mAllPrograms != null) {
      mListModel.clear();

      DefaultListModel model = new DefaultListModel();
      
      if(FilterManagerImpl.getInstance().getAllFilter().equals(filter)) {
        for(Program p : mAllPrograms) {
          model.addElement(p);
        }
      }
      else {
        for(Program p : mAllPrograms) {
          if(filter.accept(p)) {
            model.addElement(p);
          }
        }
      }
      
      mListModel = model;
      mProgramList.setModel(model);
            
      if(mNumberLabel != null) {
        mNumberLabel.setText(LOCALIZER.msg("numberOfPrograms", "Number of shown programs: {0}", mListModel.size()));
      }
      
      if(mShowDateSeparators) {
        try {
          mProgramList.addDateSeparators();
        } catch (TvBrowserException e) {
          // ignore
        }
      }
    }
    
    mProgramList.repaint();
  }
  
  public void clearPrograms() {
    setPrograms(EMPTY_PROGRAM_ARR);
  }
  
  /**
   * @param check If scrolling should only be done if the program in not visible.
   */
  public void scrollToFirstNotExpiredIndex(boolean check) {
    synchronized (mListModel) {
      if(check) {
        int firstVisibleIndex = mProgramList.locationToIndex(mProgramList.getVisibleRect().getLocation());
        int lastVisibleIndex = mProgramList.locationToIndex(new Point(0,mProgramList.getVisibleRect().y + mProgramList.getVisibleRect().height));
        
        for(int i = firstVisibleIndex; i < lastVisibleIndex; i++) {
          Object test = mListModel.getElementAt(i);
          
          if(test instanceof Program && !((Program)test).isExpired()) {
            return;
          }
        }
      }
      
      for(int i = 0; i < mListModel.getSize(); i++) {try {
        Object test = mListModel.getElementAt(i);
        
        if(test instanceof Program && !((Program)test).isExpired()) {
          scrollToIndex(i);
          break;
        }
      }catch(Throwable t) {t.printStackTrace();}
      }
    };
  }
  
  /**
   * Scrolls to the given index.
   * 
   * @param index The index to scroll to.
   */
  public void scrollToIndex(final int index) {
    if (index < 0) {
      return;
    }
    
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        mProgramListScrollPane.getVerticalScrollBar().setValue(0);
        mProgramListScrollPane.getHorizontalScrollBar().setValue(0);
        
        Rectangle cellBounds = mProgramList.getCellBounds(index,index);
        if (cellBounds != null) {
          cellBounds.setLocation(cellBounds.x, cellBounds.y + mProgramListScrollPane.getHeight() - cellBounds.height);
          mProgramList.scrollRectToVisible(cellBounds);
        }
      }
    });
  }
  
  /**
   * Scrolls to the given index.
   * ATTENTION: Date separators are not counted, so don't include them in the index.
   * 
   * @param index The index to scroll to (date separators excluded).
   */
  public void scrollToIndexWithoutDateSeparators(final int index) {
    scrollToIndex(mProgramList.getNewIndexForOldIndex(index));
  }

  @Override
  public void updatePersona() {
    if(Persona.getInstance().getHeaderImage() != null) {
      mFilterLabel.setForeground(Persona.getInstance().getTextColor());
      mNumberLabel.setForeground(Persona.getInstance().getTextColor());
    }
    else {
      mFilterLabel.setForeground(UIManager.getColor("Label.foreground"));
      mNumberLabel.setForeground(UIManager.getColor("Label.foreground"));
    }
  }
}
