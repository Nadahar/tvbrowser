package util.ui.findasyoutype;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Position;

import util.ui.TVBrowserIcons;

/**
 * This TextComponentFindAction is based on the Implementation of Santhosh
 * 
 * For Details look here:
 * http://jroller.com/page/santhosh/20050707#incremental_search_the_framework
 * 
 * @author Santhosh
 * 
 * Changed for support of a search bar instead of a search popup by René Mach:
 *   - added automatically closing Thread
 *   - removed Popup because the search field will be in a search bar
 *   - removed up and down key for finding matches because of the buttons in the search bar
 *   - added message label
 *   - added methods for the search bar
 *   - removed redundant constructor
 */
public abstract class FindAction extends AbstractAction implements DocumentListener, KeyListener, Runnable {

  private static final util.ui.Localizer mLocalizer = util.ui.Localizer.getLocalizerFor(FindAction.class);

  private JTextField mSearchField = new JTextField();

  private JComponent mComponent;

  private boolean mIgnoreCase;
  
  // New variables for the search bar starts here
  
  private boolean mBlockAutoClosing = false;
  private int mWaitTime = 5000;
  private Thread mThread;
  private JLabel mMessage;
  private Color mBg, mFg;
  
  public FindAction(JComponent comp, boolean startAtKeytype) {
    super(mLocalizer.msg("incrementalSearch", "Incremental Search")); // NOI18N

    mBg = mSearchField.getBackground();
    mFg = mSearchField.getForeground();
    
    mComponent = comp;
    mMessage = new JLabel(mLocalizer.msg("notFound", "Phrase not found"));
    mMessage.setIcon(TVBrowserIcons.warning(TVBrowserIcons.SIZE_SMALL));
    mMessage.setVisible(false);
    
    install(comp);

    if (startAtKeytype) {
      installKeyListener(comp);
    }
  }
  
  
  
  //Start of added methods for search bar
  
  protected void setBlockAutoClosing(boolean value) {
    mBlockAutoClosing = value;
  }
  
  protected boolean isBlockAutoClosing() {
    return mBlockAutoClosing;
  }
  
  protected Thread getThread() {
    return mThread;
  }
  
  protected int getWaitTime() {
    return mWaitTime;
  }
  
  protected void setWaitTime(int value) {
    mWaitTime = value;
  }

  protected JLabel getMessageLabel() {
    return mMessage;
  }
  
  //reset the find bar to initial state
  protected void reset() {
    mSearchField.setBackground(mBg);
    mSearchField.setForeground(mFg);
    mMessage.setVisible(false);
  }
  
  /**
   * Go to the previous match
   */
  public void prev() {
    mWaitTime = 5000;
    changed(Position.Bias.Backward);
  }

  /**
   * Go to the next match
   */
  public void next() {
    mWaitTime = 5000;
    changed(Position.Bias.Forward);
  }
  
  /**
   * Starts the closing Thread.
   */
  private void start() {
    if((mThread == null || !mThread.isAlive()) && !mBlockAutoClosing) {
      mThread = new Thread(this);
      mThread.setPriority(Thread.MIN_PRIORITY);
      mThread.start();
    }
  }
  
  //End of added methods
  
  
  
  
  public boolean isIgnoreCase() {
    return mIgnoreCase;
  }
  
  public void setIgnoreCase(boolean ignoreCase) {
    mIgnoreCase = ignoreCase;
  }
  
  /*-------------------------------------------------[ ActionListener ]---------------------------------------------------*/

  public void actionPerformed(ActionEvent ae) {
      setIgnoreCase((ae.getModifiers() & ActionEvent.SHIFT_MASK) == 0);

      mSearchField.removeActionListener(this);
      mSearchField.removeKeyListener(this);
      mSearchField.getDocument().removeDocumentListener(this);
      initSearch(ae);
      mSearchField.addActionListener(this);
      mSearchField.addKeyListener(this);
      mSearchField.getDocument().addDocumentListener(this);
  }

  // can be overridden by subclasses to change initial search text etc.
  protected void initSearch(ActionEvent ae) {
    mSearchField.setText(""); // NOI18N
    reset();
  }

  private void changed(Position.Bias bias) {
    mSearchField.requestFocusInWindow();
    if(changed(mComponent, mSearchField.getText(), bias)) {
      reset();
    }
    else {
      mSearchField.setBackground(new Color(255,102,102));
      mSearchField.setForeground(Color.white);
      mMessage.setVisible(true);
    }
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

  protected boolean metaDown = false;
  
  public void keyPressed(KeyEvent ke) {
    mWaitTime = 5000;
    shiftDown = ke.isShiftDown();
    controlDown = ke.isControlDown();
    metaDown = ke.isMetaDown();
    
    switch (ke.getKeyCode()) {
    case KeyEvent.VK_F3:
      next();
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
    comp.registerKeyboardAction(this, KeyStroke.getKeyStroke('I', InputEvent.CTRL_MASK), JComponent.WHEN_FOCUSED);
    comp.registerKeyboardAction(this, KeyStroke.getKeyStroke('I', InputEvent.CTRL_MASK | InputEvent.SHIFT_MASK),
        JComponent.WHEN_FOCUSED);
  }
  
  public void installKeyListener(Component comp) {
    comp.addKeyListener(new KeyAdapter() {

      public void keyTyped(KeyEvent e) {
        if (Character.isLetterOrDigit(e.getKeyChar()) &&
            !(e.isControlDown()||e.isMetaDown())) {
          actionPerformed(new ActionEvent(this, 0, "show"));
          mSearchField.setText(Character.toString(e.getKeyChar()));
          
          start();
        }
      }

      @Override
      public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_F && e.isControlDown()) {
          setBlockAutoClosing(true);
          actionPerformed(new ActionEvent(this, 0, "show"));
        }
      }
      
    });
  }
}