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
package taggingplugin;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import util.program.ProgramUtilities;
import util.settings.PluginPictureSettings;
import util.settings.ProgramPanelSettings;
import util.ui.Localizer;
import util.ui.ProgramList;
import util.ui.UiUtilities;
import util.ui.WindowClosingIf;

import com.jgoodies.forms.builder.ButtonBarBuilder2;

import devplugin.Program;

/**
 * @author bananeweizen
 *
 */
final class ProgramListDialog extends JDialog implements WindowClosingIf {

  /**
   * vector of programs shown in the dialog
   */
  private Program[] mPrograms;

  private ProgramList programList;

  private JScrollPane scrollPane;

	private String mTitle;

	private JComponent mAdditionalButton;

  /**
   * Creates the Dialog
   *
   * @param parent
   *          parent frame
   */
  public ProgramListDialog(final Window parent, final Program[] programs, final String title, final JComponent additionalButton) {
    super(parent);
    setModalityType(ModalityType.APPLICATION_MODAL);
    mPrograms = programs.clone();
    Arrays.sort(mPrograms, ProgramUtilities.getProgramComparator());
    mTitle = title;
    mAdditionalButton = additionalButton;
    createGUI();
  }

  public ProgramListDialog(final Window parent, final Program[] programs, final
			String title) {
  	this(parent, programs, title, null);
	}

	/**
   * create the dialog UI
   */
  private void createGUI() {
    setTitle(mTitle);
    UiUtilities.registerForClosing(this);

    final JPanel content = (JPanel) this.getContentPane();
    content.setLayout(new BorderLayout());

    int scrollIndex = -1;
    for (int i = 0; i < mPrograms.length; i++) {
      if (!mPrograms[i].isExpired()) {
        scrollIndex = i;
        break;
      }
    }

    final ProgramPanelSettings settings = new ProgramPanelSettings(
        new PluginPictureSettings(PluginPictureSettings.NO_PICTURE_TYPE), true);
    programList = new ProgramList(mPrograms, settings);

    programList.addMouseListeners(null);

    scrollPane = new JScrollPane(programList);
    scrollPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 0, 5));

    content.add(scrollPane, BorderLayout.CENTER);

    ButtonBarBuilder2 buttonBar = new ButtonBarBuilder2();
    buttonBar.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

    final JButton closeButton = new JButton(Localizer
        .getLocalization(Localizer.I18N_CLOSE));
    closeButton.addActionListener(new ActionListener() {

      public void actionPerformed(final ActionEvent evt) {
        dispose();
      }
    });
    if (mAdditionalButton != null) {
    	buttonBar.addFixed(mAdditionalButton);
    	buttonBar.addUnrelatedGap();
    }
    buttonBar.addGlue();
    buttonBar.addFixed(closeButton);
    content.add(buttonBar.getPanel(), BorderLayout.SOUTH);

    getRootPane().setDefaultButton(closeButton);
    pack();
    setMinimumSize(new Dimension(300, 400));
    scrollToIndex(scrollIndex);
  }

  public void close() {
    dispose();
  }

  /**
   * scroll to the program with the given index
   * @param index index of the program
   */
  private void scrollToIndex(final int index) {
    if (index < 0) {
      return;
    }

    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        scrollPane.getVerticalScrollBar().setValue(0);
        scrollPane.getHorizontalScrollBar().setValue(0);

        final Rectangle cellBounds = programList.getCellBounds(index, index);
        if (cellBounds != null) {
          cellBounds.setLocation(cellBounds.x, cellBounds.y
              + scrollPane.getHeight() - cellBounds.height - 5);
          programList.scrollRectToVisible(cellBounds);
        }
      }
    });
  }

}