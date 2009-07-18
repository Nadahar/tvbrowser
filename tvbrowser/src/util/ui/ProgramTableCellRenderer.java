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
package util.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Insets;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableCellRenderer;

import util.settings.PluginPictureSettings;
import util.settings.ProgramPanelSettings;
import devplugin.Date;
import devplugin.Program;

/**
 * CellRenderer for Program in Table<p>
 * <i>Keep in mind:</i> This Renderer internally uses "static" data for 
 * each displayed program. If program data changes the container 
 * using this renderer should be repainted to display the changed
 * data.
 * 
 * @since 2.2
 */
public class ProgramTableCellRenderer extends DefaultTableCellRenderer {

    /** Container of Program-Panel */
    private JPanel mMainPanel;

    /** Header for Program-Panel */
    private JLabel mHeaderLb;

    /** ProgramPanel */
    private ProgramPanel mProgramPanel;

    /**
     * Creates the Renderer
     */
    public ProgramTableCellRenderer() {
      this(new ProgramPanelSettings(ProgramPanelSettings.SHOW_PICTURES_NEVER, -1, -1, false, true, 10));
    }
    
    /**
     * Creates the Renderer
     * 
     * @param settings The settings to be used for this Renderer
     * @since 2.6
     */
    public ProgramTableCellRenderer(PluginPictureSettings settings) {
      this(new ProgramPanelSettings(new PluginPictureSettings(PluginPictureSettings.ALL_PLUGINS_SETTINGS_TYPE),false));      
    }
    
    /**
     * Creates the Renderer
     * 
     * @param settings The settings for the program panel.
     * @since 2.2.2
     */
    public ProgramTableCellRenderer(ProgramPanelSettings settings) {
        mMainPanel = new JPanel(new BorderLayout());
        mMainPanel.setOpaque(true);

        mHeaderLb = new JLabel();
        mMainPanel.add(mHeaderLb, BorderLayout.NORTH);

        mProgramPanel = new ProgramPanel(settings);
        mMainPanel.add(mProgramPanel, BorderLayout.CENTER);
    }

    public Component getTableCellRendererComponent(final JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        
        if (value instanceof Program) {
            Program program = (Program) value;
            Insets borderInsets = label.getBorder().getBorderInsets(label);
            mProgramPanel.setWidth(table.getCellRect(row, column, false).width - borderInsets.left - borderInsets.right);
            mProgramPanel.setProgram(program);
            
            program.addChangeListener(new ChangeListener() {
              public void stateChanged(ChangeEvent e) {
                table.repaint();
              }
            });
            
            mProgramPanel.setPaintExpiredProgramsPale(!isSelected);
            mProgramPanel.setTextColor(label.getForeground());
            
            String dateString;
            
            if(program.getDate().equals(Date.getCurrentDate().addDays(-1))) {
              dateString = Localizer.getLocalization(Localizer.I18N_YESTERDAY);
            }
            else if(program.getDate().equals(Date.getCurrentDate())) {
              dateString = Localizer.getLocalization(Localizer.I18N_TODAY);
            }
            else if(program.getDate().equals(Date.getCurrentDate().addDays(1))) {
              dateString = Localizer.getLocalization(Localizer.I18N_TOMORROW);
            }
            else {
              dateString = program.getDate().toString();
            }
            
            mHeaderLb.setText(dateString + " - " + program.getChannel().getName());
            
            if(program.isExpired() && !isSelected) {
              mHeaderLb.setForeground(Color.gray);
            }
            else {
              mHeaderLb.setForeground(label.getForeground());
            }

            mMainPanel.setBackground(label.getBackground());
            mMainPanel.setForeground(label.getForeground());
            mMainPanel.setEnabled(label.isEnabled());
            mMainPanel.setBorder(label.getBorder());

            if (table.getRowHeight(row) != mMainPanel.getPreferredSize().height)
                table.setRowHeight(row, mMainPanel.getPreferredSize().height);
            
            return mMainPanel;
        }

        return label;
    }

}