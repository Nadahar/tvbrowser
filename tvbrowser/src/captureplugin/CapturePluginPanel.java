/*
 * CapturePlugin by Andreas Hessel (Vidrec@gmx.de), Bodo Tasche
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

package captureplugin;

import captureplugin.tabs.DevicePanel;
import captureplugin.tabs.ProgramListPanel;
import util.ui.Localizer;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import java.awt.BorderLayout;
import java.awt.Window;

/**
 * This is a Panel for changing the Settings in the Plugin
 */
public class CapturePluginPanel extends JPanel {

    /** Translator */
    private static final Localizer mLocalizer = Localizer.getLocalizerFor(CapturePluginPanel.class);

    /** Tab for Programlist */
    public static final int TAB_PROGRAMLIST = 0;
    /** Tab for DeviceList */
    public static final int TAB_DEVICELIST = 1;

    /** GUI */
    private JTabbedPane mTabPane;

    /**
     * Creates the Panel
     * 
     * @param data Data to use
     */
    public CapturePluginPanel(Window owner, CapturePluginData data) {
        this.setLayout(new BorderLayout());

        mTabPane = new JTabbedPane();

        mTabPane.addTab(mLocalizer.msg("ProgramList", "Programlist"), new ProgramListPanel(owner, data));
        
        mTabPane.addTab(mLocalizer.msg("Devices", "Devices"), new DevicePanel(owner, data));
        
        // Tabbed - Pane
        //	this.add(tabPane,BorderLayout.CENTER);
        this.add(mTabPane, BorderLayout.CENTER);
    }


    /**
     * Sets the selected Tab
     * @param num Number of Tab (use TAB_* )
     */
    public void setSelectedTab(int num) {
        mTabPane.setSelectedIndex(num);
    }
    
}