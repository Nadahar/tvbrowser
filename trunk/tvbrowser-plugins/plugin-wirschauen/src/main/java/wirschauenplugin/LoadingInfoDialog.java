/*
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
package wirschauenplugin;

import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import util.ui.Localizer;
import util.ui.UiUtilities;
import util.ui.WindowClosingIf;

import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * its a little dialog with a progress bar and a cancel button. its used to
 * show 'im busy' whenever a long running task is performed (in this case its
 * loading from the Internet). it can be canceled (which will close the
 * dialog). the dialog is application modal and not resizable.
 *
 * @author uzi
 */
@SuppressWarnings("serial")
public class LoadingInfoDialog extends JDialog implements WindowClosingIf
{
  /**
   * true, if one of the different cancel-methods was used. every user initiated closing of
   * the dialog means that cancelled is true.
   */
  private boolean mCancelled;






  /**
   * creates a modal dialog with a progress bar and a cancel-button.
   *
   * @param parent the parent of the dialog
   * @param webSite name of the website we are loading data from
   */
  public LoadingInfoDialog(final Window parent, final String webSite)
  {
    //create the window
    super(parent, WirSchauenPlugin.LOCALIZER.msg("LoadingInfoDialog.DialogTitle", "Loading data from {0}...", webSite), ModalityType.APPLICATION_MODAL);
    setPreferredSize(new Dimension(300, 80));
    setResizable(false);
    //register esc key
    UiUtilities.registerForClosing(this);
    JPanel contentPane = (JPanel) getContentPane();
    contentPane.setBorder(Borders.DLU4_BORDER);
    contentPane.setLayout(new FormLayout("center:pref:grow", "pref, 10dlu, pref"));
    CellConstraints cellConstraints = new CellConstraints();
    //register window control
    addWindowListener(new WindowAdapter()
    {
      @Override
      public void windowClosing(final WindowEvent e)
      {
        close();
      }
    });

    //add the progress bar
    JProgressBar progressBar = new JProgressBar();
    progressBar.setIndeterminate(true);
    progressBar.setPreferredSize(new Dimension(260, 15));
    contentPane.add(progressBar, cellConstraints.xy(1, 1));

    //add the cancel button
    JButton cancelButton = new JButton(Localizer.getLocalization(Localizer.I18N_CANCEL));
    cancelButton.addActionListener(new ActionListener()
    {
      public void actionPerformed(final ActionEvent e)
      {
        close();
      }
    });
    contentPane.add(cancelButton, cellConstraints.xy(1, 3));

    pack();
  }



  /**
   * will be called for all closing events (esc, cancel-button, window event).
   *
   * @see util.ui.WindowClosingIf#close()
   */
  public void close()
  {
    //close the window and provide the cancelled-status
    setVisible(false);
    mCancelled = true;
    dispose();
  }


  /**
   * @return true if the user closed the dialog, false otherwise.
   */
  public boolean isCancelled()
  {
    return mCancelled;
  }
}
