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

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JRootPane;
import javax.swing.WindowConstants;

import util.ui.Localizer;
import util.ui.UiUtilities;
import util.ui.WindowClosingIf;

/**
 * The dialog to manage Markers.
 * <p>
 * @author René Mach
 * @since 3.2
 */
public class ManageDialog extends JDialog {
  private ManagePanel mPanel;
  
  public ManageDialog(MarkListsVector markListVector) {
    super(SimpleMarkerPlugin.getInstance().getSuperFrame());
    setModal(true);
    setTitle(SimpleMarkerPlugin.getLocalizer().msg("name","Marker plugin"));
    setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

    JButton close = new JButton(Localizer.getLocalization(Localizer.I18N_CLOSE));
    close.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        closeDialog();
      }
    });
    
    mPanel = new ManagePanel(markListVector,close);
    
    UiUtilities.registerForClosing(new WindowClosingIf() {
      public void close() {
        closeDialog();
      }

      public JRootPane getRootPane() {
        return ManageDialog.this.getRootPane();
      }
    });

    addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        closeDialog();
      }
    });
    
    getRootPane().setDefaultButton(close);
    
    setLayout(new BorderLayout());
    add(mPanel, BorderLayout.CENTER);
  }
  
  void selectPrograms(boolean scroll) {
    mPanel.selectPrograms(scroll);
  }
  
  private void closeDialog() {
    dispose();
    mPanel.saveSettings();
  }
}
