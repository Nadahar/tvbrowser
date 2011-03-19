/**
 * Created on 18.03.2011
 */
package captureplugin.drivers.topfield;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JWindow;
import javax.swing.border.BevelBorder;

import util.ui.Localizer;

/**
 * A window to show a wait message during lengthy operations.
 * 
 * @author Wolfgang
 */
public class TopfieldWaitWindow extends JWindow {

  private static final long serialVersionUID = 1L;
  private static final Localizer localizer = Localizer.getLocalizerFor(TopfieldWaitWindow.class);

  private static final String WAIT_LABEL_TEXT = "waitLabelText"; // @jve:decl-index=0:
  private static final String DEFAULT_WAIT_LABEL_TEXT = "Please wait ..."; // @jve:decl-index=0:

  private final Window myOwner;
  private String waitText = localizer.msg(WAIT_LABEL_TEXT, DEFAULT_WAIT_LABEL_TEXT);
  private JPanel jContentPane = null;
  private JLabel waitTextLabel = null;
  private JProgressBar waitProgressBar = null;

  /**
   * Cosntructor.
   * 
   * @param owner
   *          The parent of this window
   */
  public TopfieldWaitWindow(Window owner) {
    super(owner);
    myOwner = owner;
    initialize();
  }

  /**
   * This method initializes this
   */
  private void initialize() {
    this.setSize(300, 200);
    this.setContentPane(getJContentPane());
    this.pack();
  }

  /**
   * This method initializes jContentPane
   * 
   * @return javax.swing.JPanel
   */
  private JPanel getJContentPane() {
    if (jContentPane == null) {
      GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
      gridBagConstraints1.gridx = 0;
      gridBagConstraints1.insets = new Insets(10, 5, 5, 5);
      gridBagConstraints1.fill = GridBagConstraints.HORIZONTAL;
      gridBagConstraints1.weightx = 1.0;
      gridBagConstraints1.gridy = 1;
      GridBagConstraints gridBagConstraints = new GridBagConstraints();
      gridBagConstraints.gridx = 0;
      gridBagConstraints.gridy = 0;
      waitTextLabel = new JLabel();
      waitTextLabel.setText(waitText);
      jContentPane = new JPanel();
      jContentPane.setLayout(new GridBagLayout());
      jContentPane.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED),
          BorderFactory.createBevelBorder(BevelBorder.LOWERED)));
      jContentPane.add(waitTextLabel, gridBagConstraints);
      jContentPane.add(getWaitProgressBar(), gridBagConstraints1);
    }
    return jContentPane;
  }

  /**
   * @param waitText
   *          the waitText to set
   */
  public void setWaitText(String waitText) {
    this.waitText = waitText;
    waitTextLabel.setText(waitText);
  }

  /**
   * This method initializes waitProgressBar
   * 
   * @return javax.swing.JProgressBar
   */
  private JProgressBar getWaitProgressBar() {
    if (waitProgressBar == null) {
      waitProgressBar = new JProgressBar();
      waitProgressBar.setIndeterminate(true);
    }
    return waitProgressBar;
  }

  @Override
  public void setVisible(boolean visible) {
    setLocationRelativeTo(myOwner);
    super.setVisible(visible);
  }
}
