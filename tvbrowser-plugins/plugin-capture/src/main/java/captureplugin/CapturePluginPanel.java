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
 *     $Date: 2011-04-25 09:04:52 +0200 (Mo, 25 Apr 2011) $
 *   $Author: bananeweizen $
 * $Revision: 6996 $
 */

package captureplugin;

import java.awt.BorderLayout;
import java.awt.Window;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import util.ui.DefaultMarkingPrioritySelectionPanel;
import util.ui.Localizer;
import captureplugin.tabs.DevicePanel;
import captureplugin.tabs.ProgramListPanel;

/**
 * This is a Panel for changing the Settings in the Plugin
 */
public class CapturePluginPanel extends JPanel {

    /** Translator */
    private static final Localizer mLocalizer = Localizer.getLocalizerFor(CapturePluginPanel.class);

    /** Tab for Programlist */
    protected static final int TAB_PROGRAMLIST = 0;
    /** Tab for DeviceList */
    protected static final int TAB_DEVICELIST = 1;
    /** Tab for Marking */
    public static final int TAB_MARKING = 2;
    /** Tab for Global settings */
    public static final int TAB_GLOBAL_SETTINGS = 3;

    /** GUI */
    private JTabbedPane mTabPane;
    
    private DefaultMarkingPrioritySelectionPanel mMarkingPriorityPanel;
    
    private JCheckBox mShowAdditionalCommandsOnTop;
    
    /**
     * Creates the Panel
     * 
     * @param owner The parent window.
     * @param data Data to use
     */
    public CapturePluginPanel(Window owner, CapturePluginData data) {
        this.setLayout(new BorderLayout());

        mTabPane = new JTabbedPane();

        ProgramListPanel programListPanel = new ProgramListPanel(owner, data);
        mTabPane.addTab(mLocalizer.msg("ProgramList", "Programlist"), programListPanel);
        mTabPane.addTab(mLocalizer.msg("Devices", "Devices"), new DevicePanel(owner, data, programListPanel));
        
        mMarkingPriorityPanel = DefaultMarkingPrioritySelectionPanel.createPanel(data.getMarkPriority(),false,true);
        mTabPane.addTab(DefaultMarkingPrioritySelectionPanel.getTitle(), mMarkingPriorityPanel);

        mShowAdditionalCommandsOnTop = new JCheckBox(mLocalizer.msg("showOnTop", "Show additional commands (if any) on top of context menu."), data.showAdditionalCommandsOnTop());
        
        PanelBuilder pb = new PanelBuilder(new FormLayout("default","default"));
        pb.border(Borders.DIALOG);
        
        pb.add(mShowAdditionalCommandsOnTop, CC.xy(1, 1));
        
        mTabPane.addTab(mLocalizer.msg("Global", "Global Settings"), pb.getPanel());
        
        this.add(mTabPane, BorderLayout.CENTER);
    }


    /**
     * Sets the selected Tab
     * @param num Number of Tab (use TAB_* )
     */
    public void setSelectedTab(int num) {
        mTabPane.setSelectedIndex(num);
    }
    
    /**
     * Gets the index of the selected tab.
     * 
     * @return The index of the selected tab.
     */
    public int getSelectedTabIndex() {
      return mTabPane.getSelectedIndex();
    }
    
    /**
     * Saves the marking settings for the program
     */
    public void saveMarkingSettings() {
      CapturePlugin.getInstance().getCapturePluginData().setMarkPriority(mMarkingPriorityPanel.getSelectedPriority());
      CapturePlugin.getInstance().getCapturePluginData().setShowAdditionalCommandsOnTop(mShowAdditionalCommandsOnTop.isSelected());
    }
}