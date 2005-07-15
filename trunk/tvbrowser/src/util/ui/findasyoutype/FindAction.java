package util.ui.findasyoutype;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Position;

/**
 * This TextComponentFindAction is based on the Implementation of Santosh
 * 
 * For Details look here:
 * http://jroller.com/page/santhosh/20050707#incremental_search_the_framework
 * 
 * @author Santosh
 */
public abstract class FindAction extends AbstractAction implements DocumentListener, KeyListener {

  private static final util.ui.Localizer mLocalizer = util.ui.Localizer.getLocalizerFor(FindAction.class);

  private JPanel mSearchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));

  private JTextField mSearchField = new JTextField();

  private JPopupMenu mPopup = new JPopupMenu();

  private JComponent mComponent;

  private boolean mIgnoreCase;

  public FindAction(JComponent comp) {
    this(comp, false);
  }

  public FindAction(JComponent comp, boolean startAtKeytype) {
    super(mLocalizer.msg("incrementalSearch", "Incremental Search")); // NOI18N

    mComponent = comp;
    mSearchPanel.setBackground(UIManager.getColor("ToolTip.background"));
    mSearchField.setOpaque(false);
    JLabel label = new JLabel(mLocalizer.msg("searchFor", "Search for") + ":");
    label.setFont(label.getFont().deriveFont(Font.BOLD)); // for readability
    mSearchPanel.add(label);
    mSearchPanel.add(mSearchField);
    mSearchField.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
    mSearchPanel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
    mPopup.setBorder(BorderFactory.createLineBorder(Color.black));
    mPopup.add(mSearchPanel);
    mSearchField.setFont(label.getFont().deriveFont(Font.PLAIN)); // for
    // readability

    // when the window containing the "comp" has registered Esc key
    // then on pressing Esc instead of search popup getting closed
    // the event is sent to the window. to overcome this we
    // register an action for Esc.
    mSearchField.registerKeyboardAction(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        mPopup.setVisible(false);
      }
    }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_FOCUSED);

    install(comp);

    if (startAtKeytype) {
      installKeyListener(comp);
    }

  }

  public boolean isIgnoreCase() {
    return mIgnoreCase;
  }
  
  public void setIgnoreCase(boolean ignoreCase) {
    mIgnoreCase = ignoreCase;
  }
  
  /*-------------------------------------------------[ ActionListener ]---------------------------------------------------*/

  public void actionPerformed(ActionEvent ae) {
    if (ae.getSource() == mSearchField)
      mPopup.setVisible(false);
    else {
      setIgnoreCase((ae.getModifiers() & ActionEvent.SHIFT_MASK) == 0);

      mSearchField.removeActionListener(this);
      mSearchField.removeKeyListener(this);
      mSearchField.getDocument().removeDocumentListener(this);
      initSearch(ae);
      mSearchField.addActionListener(this);
      mSearchField.addKeyListener(this);
      mSearchField.getDocument().addDocumentListener(this);

      Rectangle rect = mComponent.getVisibleRect();
      mPopup.show(mComponent, rect.x, rect.y + rect.height - (mPopup.getPreferredSize().height / 2));
      mSearchField.requestFocus();
    }
  }

  // can be overridden by subclasses to change initial search text etc.
  protected void initSearch(ActionEvent ae) {
    mSearchField.setText(""); // NOI18N
    mSearchField.setForeground(Color.black);
  }

  private void changed(Position.Bias bias) {
    // note: popup.pack() doesn't work for first character insert
    mPopup.setVisible(false);
    mPopup.setVisible(true);

    mSearchField.requestFocus();
    mSearchField.setForeground(changed(mComponent, mSearchField.getText(), bias) ? Color.black : Color.red);
  }

  // should search for given text and select item and
  // return true if search is successfull
  protected abstract boolean changed(JComponent comp, String text, Position.Bias bias);

  /*-------------------------------------------------[ DocumentListener ]---------------------------------------------------*/

  public void insertUpdate(DocumentEvent e) {
    changed(null);
  }

  public void removeUpdate(DocumentEvent e) {
    changed(null);
  }

  public void changedUpdate(DocumentEvent e) {
  }

  /*-------------------------------------------------[ KeyListener ]---------------------------------------------------*/

  protected boolean shiftDown = false;

  protected boolean controlDown = false;

  public void keyPressed(KeyEvent ke) {
    shiftDown = ke.isShiftDown();
    controlDown = ke.isControlDown();

    switch (ke.getKeyCode()) {
    case KeyEvent.VK_UP:
      changed(Position.Bias.Backward);
      break;
    case KeyEvent.VK_DOWN:
    case KeyEvent.VK_F3:
      changed(Position.Bias.Forward);
      break;
    }
  }

  public void keyTyped(KeyEvent e) {
  }

  public void keyReleased(KeyEvent e) {
  }

  protected JTextField getSearchField() {
    return mSearchField;
  }

  public JComponent getComponent() {
    return mComponent;
  }

  /*-------------------------------------------------[ Installation ]---------------------------------------------------*/
  private void install(JComponent comp) {
    comp.registerKeyboardAction(this, KeyStroke.getKeyStroke('I', KeyEvent.CTRL_MASK), JComponent.WHEN_FOCUSED);
    comp.registerKeyboardAction(this, KeyStroke.getKeyStroke('I', KeyEvent.CTRL_MASK | KeyEvent.SHIFT_MASK),
        JComponent.WHEN_FOCUSED);
  }

  private void installKeyListener(JComponent comp) {
    comp.addKeyListener(new KeyAdapter() {

      public void keyTyped(KeyEvent e) {
        if (Character.isLetterOrDigit(e.getKeyChar())) {
          actionPerformed(new ActionEvent(this, 0, "show"));
          mSearchField.setText("" + e.getKeyChar());
        }
      }

    });

  }
}