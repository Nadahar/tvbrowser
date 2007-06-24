/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (darras@users.sourceforge.net)
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

package tvbrowser.ui.filter.dlgs;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.ButtonGroup;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;

import tvbrowser.core.filters.FilterList;
import tvbrowser.core.filters.SeparatorFilter;
import tvbrowser.core.filters.ShowAllFilter;
import tvbrowser.ui.mainframe.MainFrame;
import tvbrowser.ui.programtable.ProgramTableModel;
import util.ui.ProgramPanel;
import util.ui.SendToPluginDialog;
import devplugin.Program;
import devplugin.ProgramFilter;

/**
 * Creates the Buttons for the Filters
 */
public class FilterButtons implements ActionListener {
    /** The localizer for this class. */
    public static final util.ui.Localizer mLocalizer
      = util.ui.Localizer.getLocalizerFor(FilterButtons.class);
    
    /** Mainframe */
    private MainFrame mMainFrame;

    /** Menu-Items */
    private JMenuItem mCreateFilterMI, mSendFilterMI;

    /**
     * Constructor
     * @param mainFrame MainFrame
     */
    public FilterButtons(MainFrame mainFrame) {
        mMainFrame = mainFrame;
    }

    /**
     * Returns the Menus for Filtering
     * @return Menus for Filtering
     */
    public JMenuItem[] createFilterMenuItems() {
        ButtonGroup group = new ButtonGroup();
        FilterList filterList = FilterList.getInstance();
        ProgramFilter[] filterArr = filterList.getFilterArr();
        ProgramFilter curFilter = mMainFrame.getProgramFilter();
        
        int size = filterArr.length + 2;
        
        if ((curFilter != null) && !(curFilter instanceof ShowAllFilter)) {
            size++;
        }
        
        JMenuItem[] result = new JMenuItem[size];
        
        
        for (int i = 0; i < filterArr.length; i++) {
          
            if (filterArr[i] != null) {
              final ProgramFilter filter = filterArr[i];
              
              if (filter instanceof SeparatorFilter) {
                result[i] = null;
              } else {
                final JRadioButtonMenuItem item = new JRadioButtonMenuItem(filter.toString());
                
                result[i] = item;
                
                group.add(item);
                result[i].addActionListener(new ActionListener() {

                    public void actionPerformed(ActionEvent event) {
                        mMainFrame.setProgramFilter(filter);
                        item.setSelected(true);
                    }
                });

                if (curFilter != null && filter != null && (curFilter.getName().equals(filter.getName()))) {
                    result[i].setSelected(true);
                } else if ((curFilter == null) && (filter instanceof ShowAllFilter)) {
                    result[i].setSelected(true);
                }
              }
              
            } else {
              result[i] = null;
            }
        }

        mCreateFilterMI = new JMenuItem(mLocalizer.msg("createFilter", "Create filter..."));
        mCreateFilterMI.addActionListener(this);

        result[filterArr.length] = null;

        if ((curFilter != null) && !(curFilter instanceof ShowAllFilter)){
            mSendFilterMI = new JMenuItem(mLocalizer.msg("sendPrograms", "Send visible Programs to another Plugin"));
            mSendFilterMI.addActionListener(this);
            result[filterArr.length + 1] = mSendFilterMI;
        }
        
        result[size - 1] = mCreateFilterMI;
        
        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == mCreateFilterMI) {
            mMainFrame.showFilterDialog();
        } else if (e.getSource() == mSendFilterMI) {
            sendPrograms();
        }
    }

    /**
     * Sends the visible Programs to another Plugin
     */
    private void sendPrograms() {
        
        Program[] prgs = collectPrograms();
        
        SendToPluginDialog sendTo = new SendToPluginDialog(null, mMainFrame, prgs);
        sendTo.setVisible(true);
    }

    /**
     * Collects all visible Programs
     * @return visible Programs
     */
    private Program[] collectPrograms() {
        ArrayList<Program> array = new ArrayList<Program>();
        
        ProgramTableModel model = mMainFrame.getProgramTableModel();
        
        int columnCount = model.getColumnCount();
		for (int col = 0; col < columnCount; col++) {
            int rowCount = model.getRowCount(col);
			for (int row = 0; row < rowCount; row++) {
                // Get the program
                ProgramPanel panel = model.getProgramPanel(col, row);
                
                array.add(panel.getProgram());
            }
        }
        
        Program[] prg = new Program[array.size()];
        
        for (int i = 0; i < array.size(); i++) {
            prg[i] = array.get(i);
        }
        
        return prg;
    }
}