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
 *     $Date: 2010-06-28 19:33:48 +0200 (Mo, 28 Jun 2010) $
 *   $Author: bananeweizen $
 * $Revision: 6662 $
 */
package captureplugin.drivers.defaultdriver;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.UIManager;

import util.ui.Localizer;
import util.ui.UiUtilities;
import util.ui.WindowClosingIf;
import captureplugin.CapturePlugin;


/**
 * This Dialog shows the Result
 */
public class ResultDialog extends JDialog implements WindowClosingIf {
  /**
   * Translator
   */
  private static final Localizer mLocalizer = Localizer.getLocalizerFor(ResultDialog.class);

  /**
   * Create the Dialog
   *
   * @param parent Parent
   * @param input  Input
   * @param output Output
   * @param error  True if Error
   */
  public ResultDialog(Window parent, String input, String output, boolean error) {
    super(parent);
    setModal(true);
    createGui(input, output, error);
  }

  public void createGui(String input, String output, boolean error) {
    setTitle(mLocalizer.msg("Title", "Capture-Plugin"));

    JPanel content = (JPanel) getContentPane();

    content.setLayout(new BorderLayout());

    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

    JButton ok = new JButton(Localizer.getLocalization(Localizer.I18N_OK));

    ok.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        close();
      }
    });

    buttonPanel.add(ok);

    content.add(buttonPanel, BorderLayout.SOUTH);

    content.add(createResultPanel(input, output, error), BorderLayout.CENTER);

    UiUtilities.registerForClosing(this);
    getRootPane().setDefaultButton(ok);
    
    CapturePlugin.getInstance().layoutWindow("resultDialog",this,new Dimension(400,250));
  }

  /**
   * Creates the Result-Panel
   *
   * @param input  Input
   * @param output Output
   * @param error  True, if error
   * @return JPanel
   */
  private JPanel createResultPanel(String input, String output, boolean error) {

    JPanel panel = new JPanel();
    panel.setLayout(new GridBagLayout());


    GridBagConstraints ic = new GridBagConstraints();
    ic.gridheight = 4;
    ic.weightx = 0;
    ic.weighty = 1.0;
    ic.anchor = GridBagConstraints.NORTHWEST;
    ic.insets = new Insets(10, 5, 2, 2);
    JLabel iconLabel = new JLabel();

    if (error) {
      iconLabel.setIcon(UIManager.getIcon("OptionPane.errorIcon"));
    } else {
      iconLabel.setIcon(UIManager.getIcon("OptionPane.informationIcon"));
    }

    panel.add(iconLabel, ic);

    GridBagConstraints lc = new GridBagConstraints();
    lc.fill = GridBagConstraints.HORIZONTAL;
    lc.weightx = 1.0;
    lc.gridwidth = GridBagConstraints.REMAINDER;
    lc.insets = new Insets(2, 5, 2, 5);

    GridBagConstraints tc = new GridBagConstraints();
    tc.fill = GridBagConstraints.BOTH;
    tc.weightx = 1.0;
    tc.weighty = 0.5;
    tc.gridwidth = GridBagConstraints.REMAINDER;
    tc.insets = new Insets(2, 5, 2, 5);

    panel.add(new JLabel(mLocalizer.msg("SendParams", "Send Parameters:")), lc);

    JTextArea send = new JTextArea(input);
    send.setEditable(false);
    send.setLineWrap(true);
    send.setWrapStyleWord(true);

    panel.add(new JScrollPane(send), tc);

    panel.add(new JLabel(mLocalizer.msg("Result", "Result:")), lc);

    JTextArea received = new JTextArea(output);
    received.setEditable(false);
    received.setLineWrap(true);
    received.setWrapStyleWord(true);

    panel.add(new JScrollPane(received), tc);

    return panel;
  }

  @Override
  public void close() {
    setVisible(false);
  }
}