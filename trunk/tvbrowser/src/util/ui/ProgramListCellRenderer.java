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

import java.awt.*;

import javax.swing.*;

import devplugin.Program;

/**
 * A list cell renderer that renders Programs.
 *
 * @author  Til Schneider, www.murfman.de
 */
public class ProgramListCellRenderer extends DefaultListCellRenderer {

  private static final Color SECOND_ROW_COLOR = new Color(250, 250, 220);
  
  private JPanel mMainPanel;
  private JLabel mHeaderLb;
  private ProgramPanel mProgramPanel;
  
  
  
  public ProgramListCellRenderer() {
    mMainPanel = new JPanel(new BorderLayout());
    mMainPanel.setOpaque(true);
    
    mHeaderLb = new JLabel();
    mMainPanel.add(mHeaderLb, BorderLayout.NORTH);
    
    mProgramPanel = new ProgramPanel();
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
  public Component getListCellRendererComponent(JList list, Object value,
    int index, boolean isSelected, boolean cellHasFocus)
  {
    JLabel label = (JLabel) super.getListCellRendererComponent(list, value,
      index, isSelected, cellHasFocus);

    if (value instanceof Program) {
      Program program = (Program) value;
      
      mProgramPanel.setProgram(program);
      mHeaderLb.setText(program.getDate() + " - " + program.getChannel().getName());
      
      mMainPanel.setBackground(label.getBackground());
      mMainPanel.setForeground(label.getForeground());
      mMainPanel.setEnabled(label.isEnabled());
      mMainPanel.setBorder(label.getBorder());

      if ((index % 2 == 1) && (! isSelected)) {
        mMainPanel.setBackground(SECOND_ROW_COLOR);
      }

      return mMainPanel;
    }
    
    return label;
  }
  
}
