/*
 * TimeListPlugin by Michael Keppler
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
 * VCS information:
 *     $Date: 2007-09-20 17:59:16 +0200 (Do, 20 Sep 2007) $
 *   $Author: bananeweizen $
 * $Revision: 3885 $
 */
package timelistplugin;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import util.settings.ProgramPanelSettings;
import util.ui.ProgramPanel;

import devplugin.Program;

public class TimeListCellRenderer extends DefaultListCellRenderer {

  private static final Color SECOND_ROW_COLOR = new Color(230, 230, 230, 150);

  private static final Color SECOND_ROW_COLOR_EXPIRED = new Color(230, 230, 230, 55);

  private ProgramPanel mProgramPanel;

  private JPanel mMainPanel;

  protected TimeListCellRenderer() {
    super();
    ProgramPanelSettings settings = new ProgramPanelSettings(ProgramPanelSettings.SHOW_PICTURES_NEVER, 0, 0, !TimeListPlugin.getInstance().isShowDescriptions(), false, 1000, ProgramPanelSettings.X_AXIS);
    mProgramPanel = new ProgramPanel(settings);

    mMainPanel = new JPanel(new BorderLayout());
    mMainPanel.setOpaque(true);

    mMainPanel.add(mProgramPanel, BorderLayout.CENTER);
  }

  @Override
  public Component getListCellRendererComponent(final JList list, Object value,
      int index, boolean isSelected, boolean cellHasFocus) {
    JLabel label = (JLabel) super.getListCellRendererComponent(list, value,
        index, isSelected, cellHasFocus);

    if (value instanceof Program) {
      Program program = (Program) value;

      mProgramPanel.setProgram(program);
      mProgramPanel.setTextColor(label.getForeground());

      program.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent e) {
          if (list != null) {
            list.repaint();
          }
        }
      });
      mMainPanel.setBackground(label.getBackground());

      if (isSelected)
        mMainPanel.setForeground(label.getForeground());

      mMainPanel.setEnabled(label.isEnabled());
      mMainPanel.setBorder(label.getBorder());

      if (((index & 1) == 1) && (!isSelected)
          && program.getMarkPriority() < Program.MIN_MARK_PRIORITY) {
        mMainPanel.setBackground(program.isExpired() ? SECOND_ROW_COLOR_EXPIRED
            : SECOND_ROW_COLOR );
      }

      return mMainPanel;
    }

    return label;
  }

}
