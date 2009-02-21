/*
 * Copyright Michael Keppler
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
 */
package mediathekplugin;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import util.ui.Localizer;
import util.ui.UiUtilities;
import util.ui.WindowClosingIf;

public final class ProgramsDialog extends JDialog implements WindowClosingIf {

  /** The localizer used by this class. */
  private static final Localizer mLocalizer = Localizer
      .getLocalizerFor(ProgramsDialog.class);

  public ProgramsDialog(final Frame frame) {
    super(frame, true);
    setTitle(mLocalizer.msg("title", "Programs in the Mediathek"));
    createGUI();
    UiUtilities.registerForClosing(this);
  }

  private void createGUI() {
    final JPanel content = (JPanel) this.getContentPane();
    content.setLayout(new BorderLayout());
    content.setBorder(UiUtilities.DIALOG_BORDER);

    final JList programList = new JList(MediathekPlugin.getInstance()
        .getSortedPrograms());
    programList.setCellRenderer(new ItemListCellRenderer());
    content.add(new JScrollPane(programList), BorderLayout.CENTER);

    final JPanel buttonPn = new JPanel(new BorderLayout());
    buttonPn.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
    content.add(buttonPn, BorderLayout.SOUTH);

    final JButton closeButton = new JButton(Localizer
        .getLocalization(Localizer.I18N_CLOSE));
    closeButton.addActionListener(new ActionListener() {

      public void actionPerformed(final ActionEvent evt) {
        close();
      }
    });

    buttonPn.add(closeButton, BorderLayout.EAST);
    getRootPane().setDefaultButton(closeButton);
  }

  public void close() {
    dispose();
  }

}