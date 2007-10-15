/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (martin_oat@yahoo.de)
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

import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import util.settings.ProgramPanelSettings;

import devplugin.Program;

/**
 * A list cell renderer that renders Programs.
 *
 * @author  Til Schneider, www.murfman.de
 */
public class ProgramListCellRenderer extends DefaultListCellRenderer {

  private static final Color SECOND_ROW_COLOR = new Color(220, 220, 220, 150);
  private static final Color SECOND_ROW_COLOR_EXPIRED = new Color(220, 220, 220, 55);
  
  private JPanel mMainPanel;
  private JLabel mHeaderLb;
  private ProgramPanel mProgramPanel;
  
  /**
   * Creates a new instance of ProgramListCellRenderer
   */
  public ProgramListCellRenderer() {
    this(new ProgramPanelSettings(ProgramPanelSettings.SHOW_PICTURES_NEVER, -1, -1, false, true, 10)); 
  }
  
  /**
   * Creates a new instance of ProgramListCellRenderer
   * @param showOnlyDateAndTitle If the programs should only show date and title.
   * 
   * @since 2.2.1
   * @deprecated Since 2.2.2 User {@link #ProgramListCellRenderer(ProgramPanelSettings)} instead.
   */
  public ProgramListCellRenderer(boolean showOnlyDateAndTitle) {
    this(new ProgramPanelSettings(ProgramPanelSettings.SHOW_PICTURES_NEVER, -1, -1, showOnlyDateAndTitle, true, 10));
  }
  
  /**
   * Creates a new instance of ProgramListCellRenderer
   * @param settings The settings for the program panel. 
   * 
   * @since 2.2.2
   */
  public ProgramListCellRenderer(ProgramPanelSettings settings) {
	  this(settings, ProgramPanel.Y_AXIS);
  }

  
  public ProgramListCellRenderer(ProgramPanelSettings settings, int axis) {
	    mMainPanel = new JPanel(new BorderLayout());
	    mMainPanel.setOpaque(true);
	    
	    mHeaderLb = new JLabel();
	    mMainPanel.add(mHeaderLb, BorderLayout.NORTH);
	    
	    if(settings == null)
	      settings = new ProgramPanelSettings(ProgramPanelSettings.SHOW_PICTURES_NEVER, 1080, 1380, false, true, 90);
	    
	    mProgramPanel = new ProgramPanel(settings, axis);
	    mMainPanel.add(mProgramPanel, BorderLayout.CENTER);
}

/**
   * Return a component that has been configured to display the specified
   * value. That component's <code>paint</code> method is then called to
   * "render" the cell.  If it is necessary to compute the dimensions
   * of a list because the list cells do not have a fixed size, this method
   * is called to generate a component on which <code>getPreferredSize</code>
   * can be invoked.
   *
   * @param list The JList we're painting.
   * @param value The value returned by list.getModel().getElementAt(index).
   * @param index The cells index.
   * @param isSelected True if the specified cell was selected.
   * @param cellHasFocus True if the specified cell has the focus.
   * @return A component whose paint() method will render the specified value.
   *
   * @see JList
   * @see ListSelectionModel
   * @see ListModel
   */
  public Component getListCellRendererComponent(final JList list, Object value,
    int index, boolean isSelected, boolean cellHasFocus)
  {
    JLabel label = (JLabel) super.getListCellRendererComponent(list, value,
      index, isSelected, cellHasFocus);

    if (value instanceof Program) {
      Program program = (Program) value;
      
      mProgramPanel.setProgram(program);
      mProgramPanel.setTextColor(label.getForeground());
            
      program.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent e) {
          if(list != null) {
            list.repaint();
          }
        }
      });
      mHeaderLb.setText(program.getDate() + " - " + program.getChannel().getName());
      
      if(program.isExpired() && !isSelected)
        mHeaderLb.setForeground(Color.gray);
      else
        mHeaderLb.setForeground(label.getForeground());
      
      mMainPanel.setBackground(label.getBackground());
      
      if(isSelected)
        mMainPanel.setForeground(label.getForeground());

      mMainPanel.setEnabled(label.isEnabled());
      mMainPanel.setBorder(label.getBorder());

      if ((index % 2 == 1) && (! isSelected) && program.getMarkPriority() < Program.MIN_MARK_PRIORITY) {
        mMainPanel.setBackground(program.isExpired() ? SECOND_ROW_COLOR_EXPIRED : SECOND_ROW_COLOR); 
      }

      return mMainPanel;
    }
    
    return label;
  }
  
}
