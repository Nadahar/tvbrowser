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

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import util.settings.PluginPictureSettings;
import util.settings.ProgramPanelSettings;
import util.ui.ProgramList;
import util.ui.SendToPluginDialog;
import util.ui.TVBrowserIcons;
import util.ui.UiUtilities;
import util.ui.persona.Persona;
import util.ui.persona.PersonaListener;

import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.Sizes;

import devplugin.Plugin;
import devplugin.Program;
import devplugin.ProgramFilter;

/**
 * SimpleMarkerPlugin 1.4 Plugin for TV-Browser since version 2.3 to only mark
 * programs and add them to the Plugin tree.
 *
 * (Formerly known as Just_Mark ;-))
 *
 * License: GNU General Public License (GPL)
 *
 * @author René Mach
 */
public class ManagePanel extends JPanel implements PersonaListener {
  private static final long serialVersionUID = 1L;

  private JList mMarkListsList;

  private ProgramList mProgramsList;

  private DefaultListModel mProgramListModel;

  private JScrollPane mMarkListsScrolPane, mProgramsScrollPane;

  private MarkListsVector mMarkListVector;

  private JButton mSend, mDelete, mSettings, mClose, mUndo;

  private JRadioButton mShowPrograms, mShowTitles;

  private Program[] mDeletedPrograms;
  private MarkList mLastDeletingList;

  private JSplitPane mSplitPane;
  
  private JComboBox mFilterSelection;
  private JLabel mFilterLabel;

  /**
   * Creates an instance of this panel.
   *
   * @param markListVector The MarkListsVector.
   */
  public ManagePanel(MarkListsVector markListVector, JButton close) {
    mMarkListVector = markListVector;
    mClose = close;
    
    setOpaque(false);

    mShowPrograms = new JRadioButton(SimpleMarkerPlugin.getLocalizer().msg("list.programs","List programs"),true);
    mShowPrograms.setOpaque(false);
    mShowTitles = new JRadioButton(SimpleMarkerPlugin.getLocalizer().msg("list.titles","List for program titles"));
    mShowTitles.setOpaque(false);

    ButtonGroup bg = new ButtonGroup();
    bg.add(mShowPrograms);
    bg.add(mShowTitles);

    mProgramListModel = new DefaultListModel();

    mMarkListsList = new JList(mMarkListVector/*.getMarkListNames()*/);
    mMarkListsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    mMarkListsList.setCellRenderer(new MarkListCellRenderer());

    mProgramsList = new ProgramList(mProgramListModel,new ProgramPanelSettings(new PluginPictureSettings(PluginPictureSettings.ALL_PLUGINS_SETTINGS_TYPE), false, ProgramPanelSettings.X_AXIS));
    mProgramsList.addMouseAndKeyListeners(null);

    mMarkListsScrolPane = new JScrollPane(mMarkListsList);
    mProgramsScrollPane = new JScrollPane(mProgramsList);
    mMarkListsScrolPane.setBorder(null);
    mProgramsScrollPane.setBorder(null);
    
    mFilterSelection = new JComboBox(SimpleMarkerPlugin.getPluginManager().getFilterManager().getAvailableFilters());
    
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        mFilterSelection.setSelectedItem(SimpleMarkerPlugin.getPluginManager().getFilterManager().getAllFilter());
      }
    });
    
    mFilterSelection.addPopupMenuListener(new PopupMenuListener() {
      @Override
      public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
        fillFilterBox();
      }
      
      @Override
      public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {}
      
      @Override
      public void popupMenuCanceled(PopupMenuEvent e) {}
    });
    mFilterSelection.addItemListener(new ItemListener() {
      @Override
      public void itemStateChanged(ItemEvent e) {
        if(ItemEvent.SELECTED == e.getStateChange()) {
          try {
            selectPrograms(true);
          }catch(Throwable t) {/* Ignore until release of TV-Browser 3.3.3 */}
        }
      }
    });
    
    setBorder(Borders.createEmptyBorder("6dlu,6dlu,5dlu,6dlu"));

    FormLayout layout = new FormLayout("fill:default:grow",
    "pref,pref,4dlu,fill:default:grow,4dlu,pref");

    setLayout(layout);

    CellConstraints cc = new CellConstraints();

    add(mShowPrograms, cc.xy(1,1));
    add(mShowTitles, cc.xy(1,2));
    
    JPanel rightComponent = new JPanel(new FormLayout("default,3dlu,default:grow","default,5dlu,fill:default:grow"));
    rightComponent.setOpaque(false);

    mFilterLabel = new JLabel(SimpleMarkerPlugin.getLocalizer().msg("filter", "Filter:"));
    
    rightComponent.add(mFilterLabel, CC.xy(1,1));
    rightComponent.add(mFilterSelection, CC.xy(3, 1));
    rightComponent.add(mProgramsScrollPane, CC.xyw(1, 3, 3));
    
    if(markListVector.size() > 1) {
      mSplitPane = new JSplitPane();
      
      for(int i = 0; i < mSplitPane.getComponentCount(); i++) {
        (mSplitPane.getComponent(i)).setBackground(new Color(0,0,0,0));
      }
      
      mSplitPane.setLeftComponent(mMarkListsScrolPane);
      mSplitPane.setRightComponent(rightComponent);
      mSplitPane.setOpaque(false);
      mSplitPane.setContinuousLayout(true);
      mSplitPane.setBorder(BorderFactory.createEmptyBorder());
      
      add(mSplitPane, cc.xy(1,4));

      int pos = SimpleMarkerPlugin.getInstance().getSettings()
          .getSplitPosition();

      if (pos >= 0) {
        mSplitPane.setDividerLocation(pos);
      }
    }
    else {
      JPanel innerPanel = new JPanel(new FormLayout("fill:default:grow","fill:default:grow"));
      mProgramsScrollPane.setBorder(new JScrollPane().getBorder());

      innerPanel.add(mProgramsScrollPane, cc.xy(1,1));

      add(innerPanel, cc.xy(1,4));
    }

    add(getButtonPanel(cc), cc.xyw(1,6,1));

    mShowPrograms.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        selectPrograms(true);
        mDelete.setToolTipText(SimpleMarkerPlugin.getLocalizer().msg("tooltip.deletePrograms","Delete selected progams"));
      }
    });

    mShowTitles.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        selectPrograms(true);
        mDelete.setToolTipText(SimpleMarkerPlugin.getLocalizer().msg("tooltip.deleteTitles","Delete programs with selected titles/all programs"));
      }
    });

    mSend.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        send();
      }
    });

    mDelete.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        delete();
      }
    });

    mUndo.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        undo();
      }
    });

    if(mClose != null) {
      mSettings.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          mClose.getActionListeners()[0].actionPerformed(null);
          Plugin.getPluginManager()
              .showSettings(SimpleMarkerPlugin.getInstance());
        }
      });
    }

    mMarkListsList.addListSelectionListener(new ListSelectionListener() {
      public void valueChanged(ListSelectionEvent e) {
        if (!e.getValueIsAdjusting()) {
          selectPrograms(true);
        }
      }
    });

    mProgramsList.addListSelectionListener(new ListSelectionListener() {
      public void valueChanged(ListSelectionEvent arg0) {
        mDelete.setEnabled(true);
      }
    });

    mMarkListsList.setSelectedIndex(0);
    
    selectPrograms(true);
    
    updatePersona();
  }
  
  private synchronized void fillFilterBox() {
    for (ProgramFilter filter : SimpleMarkerPlugin.getPluginManager().getFilterManager().getAvailableFilters()) {
      boolean found = false;

      for (int i = 0; i < mFilterSelection.getItemCount(); i++) {
        if (filter != null && filter.getName().equals(((ProgramFilter)mFilterSelection.getItemAt(i)).getName())) {
          found = true;
          break;
        }
      }

      if (!found) {
        mFilterSelection.addItem(filter);
      }
    }

    for (int i = mFilterSelection.getItemCount() - 1; i >= 0; i--) {
      boolean found = false;

      for (ProgramFilter filter : SimpleMarkerPlugin.getPluginManager().getFilterManager().getAvailableFilters()) {
        if (filter.getName().equals(((ProgramFilter)mFilterSelection.getItemAt(i)).getName())) {
          found = true;
          break;
        }
      }

      if (!found) {
        mFilterSelection.removeItemAt(i);
      }
    }
  }
  
  JButton getDefaultButton() {
    return mClose;
  }

  void saveSettings() {
    if(mSplitPane != null) {
      SimpleMarkerPlugin.getInstance().getSettings().setSplitPosition(
          mSplitPane.getDividerLocation());
    }

    SimpleMarkerPlugin.getInstance().resetManageDialog();
  }

  private void send() {
    Program[] programs = null;

    MarkList list = (MarkList) mMarkListsList.getSelectedValue();

    if(mProgramsList.getSelectedValue() != null && mProgramsList.getSelectedValue() instanceof Program) {
      programs = mProgramsList.getSelectedPrograms();
    } else if(mProgramsList.getSelectedValue() != null) {
      Object[] values = mProgramsList.getSelectedValues();
      ArrayList<Program> progList = new ArrayList<Program>();

      for(Object o : values) {
        Program[] progs = list.getProgramsWithTitle((String)o);

        for(Program p : progs) {
          progList.add(p);
        }
      }

      programs = progList.toArray(new Program[progList.size()]);
    }
    else if(mProgramsList.getSelectedValue() == null) {
      ArrayList<Program> expiredProgs = new ArrayList<Program>();
      ArrayList<Program> notExpiredProgs = new ArrayList<Program>();

      for(int i = 0; i < mProgramListModel.size(); i++) {
        Program prog = (Program)mProgramListModel.get(i);

        if(prog.isExpired()) {
          expiredProgs.add(prog);
        }
        else {
          notExpiredProgs.add(prog);
        }
      }

      if(notExpiredProgs.isEmpty()) {
        programs = expiredProgs.toArray(new Program[expiredProgs.size()]);
      }
      else {
        programs = notExpiredProgs.toArray(new Program[notExpiredProgs.size()]);
      }
    }

    if(programs != null) {
      SendToPluginDialog send = new SendToPluginDialog(SimpleMarkerPlugin
          .getInstance(), list.getReceiveTarget(), UiUtilities.getLastModalChildOf(SimpleMarkerPlugin.getInstance().getSuperFrame()), programs);

      send.setVisible(true);
    }
  }
  
  synchronized void selectPrograms(boolean scroll) {
    mProgramsList.clearSelection();
    mProgramListModel.clear();
    
    mProgramListModel = new DefaultListModel();
    
    MarkList list = (MarkList)mMarkListsList.getSelectedValue();
    int index = -1;
    int added = 0;
    
    if (mShowPrograms.isSelected()) {
      for (int i = 0; i < list.size(); i++) {
        Program prog = list.get(i);

        if(prog != null && ((ProgramFilter)mFilterSelection.getSelectedItem()).accept(prog)) {
          mProgramListModel.addElement(prog);
          added++;
          
          if(!list.get(i).isExpired() && index == -1) {
            index = added;
          }
        }
      }
      
      mProgramsList.setModel(mProgramListModel);
      
      if(!mProgramListModel.isEmpty() && SimpleMarkerPlugin.getInstance().getSettings().isShowingDateSeperators()) {
        try {
          mProgramsList.addDateSeparators();
        } catch (Throwable e) {
          e.printStackTrace();
        }
      }
    } else {
      Hashtable<String, LinkedList<Program>> table = list.getSortedPrograms();
      Enumeration<String> keys = table.keys();

      while (keys.hasMoreElements()) {
        mProgramListModel.addElement(keys.nextElement());
      }
      
      mProgramsList.setModel(mProgramListModel);
    }
    
    if(scroll) {
      scroll(mProgramsList.getNewIndexForOldIndex(index));
    }

    mSend.setEnabled(mProgramListModel.size() > 0);
    mDelete.setEnabled(mProgramListModel.size() > 0);
    mUndo.setEnabled(mLastDeletingList != null && mDeletedPrograms != null && mDeletedPrograms.length > 0);

    if (isVisible()) {
      mMarkListsList.repaint();
    }
  }
  
  private void scroll(final int scrollIndex) {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        mProgramsScrollPane.getVerticalScrollBar().setValue(0);
        mProgramsScrollPane.getHorizontalScrollBar().setValue(0);

        if(scrollIndex != -1) {
          Rectangle cellBounds = mProgramsList.getCellBounds(scrollIndex,scrollIndex);
          cellBounds.setLocation(cellBounds.x, cellBounds.y + mProgramsScrollPane.getHeight() - cellBounds.height);

          mProgramsList.scrollRectToVisible(cellBounds);
        }
      }
    });
  }

  private void delete() {
    if (mProgramsList.getSelectedIndex() == -1) {
      mProgramsList.setSelectionInterval(0, mProgramsList.getModel().getSize()-1);
    }

    mLastDeletingList =(MarkList) mMarkListsList.getSelectedValue();

    if (mShowPrograms.isSelected()) {
      mDeletedPrograms = (mProgramsList).getSelectedPrograms();

      for (Program p : mDeletedPrograms) {
        mLastDeletingList.remove(p);
      }

    } else {
      Object[] programs = mProgramsList.getSelectedValues();

      ArrayList<Program> deletedPrograms = new ArrayList<Program>();

      for (Object o : programs) {
        Program[] deletedArr = mLastDeletingList.removeProgramsWithTitle((String)o);

        for(Program deleted: deletedArr) {
          deletedPrograms.add(deleted);
        }
      }

      mDeletedPrograms = deletedPrograms.toArray(new Program[deletedPrograms.size()]);
    }

    SimpleMarkerPlugin.getInstance().revalidate(mDeletedPrograms);
    mLastDeletingList.updateNode();
    SimpleMarkerPlugin.getInstance().refreshManageDialog(false);
  }

  private void undo() {
    if(mLastDeletingList != null && mDeletedPrograms != null && mDeletedPrograms.length > 0) {
      for(Program p : mDeletedPrograms) {
        mLastDeletingList.addElement(p);
      }
    }

    SimpleMarkerPlugin.getInstance().revalidate(mDeletedPrograms);

    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        mLastDeletingList.updateNode();
      }
    });

    mLastDeletingList = null;
    mDeletedPrograms = null;

    SimpleMarkerPlugin.getInstance().refreshManageDialog(true);
  }

  private JPanel getButtonPanel(CellConstraints cc) {
    JPanel buttons = new JPanel();
    buttons.setOpaque(false);
    buttons.setLayout(new BoxLayout(buttons, BoxLayout.X_AXIS));
    
    int pixel7 = Sizes.dialogUnitXAsPixel(7, buttons);
    int pixel2 = Sizes.dialogUnitXAsPixel(2, buttons);
    
    if(mClose != null) {
      mSettings = new JButton(TVBrowserIcons.preferences(TVBrowserIcons.SIZE_SMALL));
      mSettings.setToolTipText(SimpleMarkerPlugin.getLocalizer().msg("tooltip.settings","Open settings"));
      buttons.add(mSettings);
      buttons.add(Box.createRigidArea(new Dimension(pixel7,0)));
    }
    
    JButton scrollToPreviousDay = new JButton(TVBrowserIcons.left(TVBrowserIcons.SIZE_SMALL));
    scrollToPreviousDay.setToolTipText(ProgramList.getPreviousActionTooltip());
    scrollToPreviousDay.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        mProgramsList.scrollToPreviousDayIfAvailable();
      }
    });
    
    JButton scrollToNextDay = new JButton(TVBrowserIcons.right(TVBrowserIcons.SIZE_SMALL));
    scrollToNextDay.setToolTipText(ProgramList.getNextActionTooltip());
    scrollToNextDay.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        mProgramsList.scrollToNextDayIfAvailable();
      }
    });
    
    JButton scrollToFirstNotExpiredIndex = new JButton(TVBrowserIcons.scrollToNow(TVBrowserIcons.SIZE_SMALL));
    scrollToFirstNotExpiredIndex.setToolTipText(SimpleMarkerPlugin.getLocalizer().msg("scrollToFirstNotExpired","Scroll to first not expired program."));
    scrollToFirstNotExpiredIndex.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        for(int i = 0; i < mProgramListModel.getSize(); i++) {
          if(mProgramListModel.getElementAt(i) instanceof Program && 
              !((Program)mProgramListModel.getElementAt(i)).isExpired()) {
            scroll(mProgramsList.getNewIndexForOldIndex(i));
            break;
          }
        }
      }
    });
    
    mSend = new JButton(TVBrowserIcons.copy(TVBrowserIcons.SIZE_SMALL));
    mSend.setToolTipText(SimpleMarkerPlugin.getLocalizer().msg("tooltip.send","Send  programs to other plugins"));
    
    mDelete = new JButton(TVBrowserIcons.delete(TVBrowserIcons.SIZE_SMALL));
    mDelete.setToolTipText(SimpleMarkerPlugin.getLocalizer().msg("tooltip.deletePrograms","Delete all/selected progams"));
    
    mUndo = new JButton(SimpleMarkerPlugin.getInstance().createImageIcon(
        "actions", "edit-undo", 16));
    mUndo.setToolTipText(SimpleMarkerPlugin.getLocalizer().msg("tooltip.undo","Undo"));
    mUndo.setEnabled(false);
    
    buttons.add(scrollToPreviousDay);
    buttons.add(Box.createRigidArea(new Dimension(pixel2,0)));
    buttons.add(scrollToNextDay);
    buttons.add(Box.createRigidArea(new Dimension(pixel2,0)));
    buttons.add(scrollToFirstNotExpiredIndex);
    buttons.add(Box.createRigidArea(new Dimension(pixel7,0)));
    buttons.add(mSend);
    buttons.add(Box.createRigidArea(new Dimension(pixel2,0)));
    buttons.add(mDelete);
    buttons.add(Box.createRigidArea(new Dimension(pixel7,0)));
    buttons.add(mUndo);
    buttons.add(Box.createHorizontalGlue());
    
    if(mClose != null) {
      mClose.setPreferredSize(new Dimension(mClose.getPreferredSize().width, mUndo.getPreferredSize().height));
      
      buttons.add(mClose);
    }
    
    return buttons;
  }

  private static class MarkListCellRenderer extends DefaultListCellRenderer {
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
      simplemarkerplugin.ManagePanel.MarkListCellRenderer c = (simplemarkerplugin.ManagePanel.MarkListCellRenderer) super.getListCellRendererComponent(list,value,index,isSelected,cellHasFocus);

      if(!((MarkList)value).isEmpty()) {
        c.setText(c.getText() + " [" + ((MarkList)value).size() + "]");
      }

      return c;
    }
  }

  @Override
  public void updatePersona() {
    if(Persona.getInstance().getHeaderImage() != null) {
      mFilterLabel.setForeground(Persona.getInstance().getTextColor());
      mShowPrograms.setForeground(Persona.getInstance().getTextColor());    
    }
    else {
      mShowPrograms.setForeground(UIManager.getColor("Label.foreground"));
      mFilterLabel.setForeground(UIManager.getColor("Label.foreground"));
    }
    
    mShowTitles.setForeground(mShowPrograms.getForeground());
  }
}
