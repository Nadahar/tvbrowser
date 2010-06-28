package util.ui.findasyoutype;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ToolTipManager;
import javax.swing.text.Caret;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;

import tvbrowser.core.icontheme.IconLoader;
import util.ui.TVBrowserIcons;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * This TextComponentFindAction is based on the Implementation of Santosh
 * 
 * For Details look here:
 * http://jroller.com/page/santhosh/20050707#incremental_search_jtextcomponent
 * 
 * @author Santhosh
 * 
 * Changed for support of a search bar instead of a search popup by René Mach:
 *   - added the needed components for the search bar
 *   - added ComponentListener to initialize the search when the
 *     search bar becomes visible
 *   - added Runnable to support closing the search bar automatically if the
 *     search was started with a key type.
 *   - added Localizer
 *   - added MouseAdapter for the search bar buttons
 */
public class TextComponentFindAction extends FindAction implements
    FocusListener, ComponentListener {

  private static final util.ui.Localizer mLocalizer = util.ui.Localizer
      .getLocalizerFor(TextComponentFindAction.class);

  private JPanel mSearchBar;
  private JButton mFindNext, mFindPrev, mSearchCloseBtn;

  public TextComponentFindAction(JTextComponent comp) {
    super(comp, false);
    ini();
  }

  public TextComponentFindAction(JTextComponent comp, boolean startAtKeyType) {
    super(comp, startAtKeyType);
    ini();
  }

  // Added method for building up the search bar
  private void ini() {
    PanelBuilder b = new PanelBuilder(new FormLayout(
        "2dlu,pref,5dlu,pref,5dlu,100dlu,5dlu,pref,5dlu,pref,15dlu,pref",
        "pref,3dlu"));
    CellConstraints cc = new CellConstraints();

    mSearchBar = b.getPanel();
    mSearchBar.addComponentListener(this);
    mSearchBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, mSearchBar.getBackground().darker()));

    mSearchCloseBtn = new JButton(IconLoader.getInstance()
        .getIconFromTheme("actions", "process-stop", 16));
    mSearchCloseBtn.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
    mSearchCloseBtn.setPressedIcon(IconLoader.getInstance().getIconFromTheme(
        "actions", "close-pressed", 16));
    mSearchCloseBtn.setToolTipText(mLocalizer.msg("closeToolTip",
        "Close Find bar"));
    mSearchCloseBtn.setContentAreaFilled(false);
    mSearchCloseBtn.setFocusable(false);
    
    MouseListener[] ml = mSearchCloseBtn.getMouseListeners();
    
    for(int i = 0; i < ml.length; i++) {
      if(!(ml[i] instanceof ToolTipManager)) {
        mSearchCloseBtn.removeMouseListener(ml[i]);
      }
    }

    final JTextField searchField = getSearchField();

    mFindNext = new JButton(mLocalizer.msg("next", "Find Next"));
    mFindNext.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
    mFindNext.setIcon(TVBrowserIcons.down(TVBrowserIcons.SIZE_SMALL));
    mFindNext.setContentAreaFilled(false);
    mFindNext.setFocusable(false);
    mFindNext.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        next();
      }
    });

    mFindPrev = new JButton(mLocalizer.msg("prev", "Find Previous"));
    mFindPrev.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
    mFindPrev.setIcon(TVBrowserIcons.up(TVBrowserIcons.SIZE_SMALL));
    mFindPrev.setContentAreaFilled(false);
    mFindPrev.setFocusable(false);
    mFindPrev.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        prev();
      }
    });

    addMouseAdapter(mFindNext);
    addMouseAdapter(mFindPrev);
    addMouseAdapter(mSearchCloseBtn);

    searchField.addKeyListener(new KeyAdapter() {
      public void keyReleased(KeyEvent e) {
        mFindNext.setEnabled(searchField.getText().length() > 0);
        mFindPrev.setEnabled(searchField.getText().length() > 0);
      }
    });

    b.add(mSearchCloseBtn, cc.xy(2, 1));
    b.addLabel(mLocalizer.msg("find", "Find:"), cc.xy(4, 1));
    b.add(searchField, cc.xy(6, 1));
    b.add(mFindNext, cc.xy(8, 1));
    b.add(mFindPrev, cc.xy(10, 1));
    b.add(getMessageLabel(), cc.xy(12, 1));

    /*
     * Close action for the SearchPanel.
     */
    Action close = new AbstractAction() {
      private static final long serialVersionUID = 1L;

      public void actionPerformed(ActionEvent e) {
        mSearchBar.setVisible(false);
        setBlockAutoClosing(false);
        interrupt();
      }
    };

    KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
    searchField.getInputMap(JComponent.WHEN_FOCUSED).put(stroke, "CLOSE_SEARCH");
    searchField.getActionMap().put("CLOSE_SEARCH", close);

    mFindNext.getInputMap(JComponent.WHEN_FOCUSED).put(stroke, "CLOSE_SEARCH");
    mFindNext.getActionMap().put("CLOSE_SEARCH", close);

    mFindPrev.getInputMap(JComponent.WHEN_FOCUSED).put(stroke, "CLOSE_SEARCH");
    mFindPrev.getActionMap().put("CLOSE_SEARCH", close);

    mSearchCloseBtn.getInputMap(JComponent.WHEN_FOCUSED).put(stroke,
        "CLOSE_SEARCH");
    mSearchCloseBtn.getActionMap().put("CLOSE_SEARCH", close);

    mSearchBar.setVisible(false);
    mSearchCloseBtn.setVisible(false);
  }

  /**
   * MouseAdapter for painting borders for a JButton if the mouse is over it
   * and handling actions of the closeButton.
   * 
   * @param button
   *          The button to add the MouseAdapter
   */
  private void addMouseAdapter(JButton button) {
    button.addMouseListener(new MouseAdapter() {
      boolean mOver;
      boolean mPressed;

      public void mouseEntered(MouseEvent e) {
        mOver = true;
        JButton b = (JButton) e.getSource();
        if (b.isEnabled()) {
          if (b.equals(mFindPrev) || b.equals(mFindNext)) {
            setBorder(b, mPressed);
          } else if (mPressed) {
            b.setIcon(IconLoader.getInstance().getIconFromTheme("actions",
                "close-pressed", 16));
          } else {
            b.setIcon(IconLoader.getInstance().getIconFromTheme("status",
                "close-over", 16));
          }
        }
      }

      public void mouseExited(MouseEvent e) {
        mOver = false;
        JButton b = (JButton) e.getSource();
        if (b.isEnabled()) {
          if (b.equals(mFindPrev) || b.equals(mFindNext)) {
            b.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
          } else {
            b.setIcon(IconLoader.getInstance().getIconFromTheme("actions",
                "process-stop", 16));
          }
        }
      }

      public void mousePressed(MouseEvent e) {
        mPressed = true;
        JButton b = (JButton) e.getSource();
        if (b.isEnabled()) {
          if (b.equals(mFindPrev) || b.equals(mFindNext)) {
            setBorder(b, true);
          } else {
            b.setIcon(IconLoader.getInstance().getIconFromTheme("actions",
                "close-pressed", 16));
          }
        }
      }

      public void mouseReleased(MouseEvent e) {
        mPressed = false;
        JButton b = (JButton) e.getSource();
        if (b.isEnabled()) {
          if (b.equals(mFindPrev) || b.equals(mFindNext)) {
            if (mOver) {
              setBorder(b, false);
            } else {
              b.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
            }
          } else {
            b.setIcon(IconLoader.getInstance().getIconFromTheme("actions",
                "process-stop", 16));

            if (mOver) {
              mSearchBar.setVisible(false);
              mSearchCloseBtn.setVisible(false);
              setBlockAutoClosing(false);
              interrupt();
            }
          }
        }
      }

    });
  }

  private void setBorder(JButton b, boolean pressed) {
    b.setBorder(BorderFactory.createCompoundBorder(BorderFactory
        .createCompoundBorder(BorderFactory.createMatteBorder(1, 1, 0, 0,
            pressed ? mSearchBar.getBackground().darker() : mSearchBar
                .getBackground().brighter()), BorderFactory.createEmptyBorder(
            1, 1, 0, 0)), BorderFactory.createCompoundBorder(BorderFactory
        .createMatteBorder(0, 0, 1, 1, pressed ? mSearchBar.getBackground()
            .brighter() : mSearchBar.getBackground().darker()), BorderFactory
        .createEmptyBorder(0, 0, 1, 1))));
  }

  // 1. inits searchField with selected text
  // 2. adds focus listener so that textselection gets painted
  // even if the textcomponent has no focus
  protected void initSearch(ActionEvent ae) {
    mSearchBar.removeComponentListener(this);
    
    mSearchBar.setVisible(true);
    mSearchCloseBtn.setVisible(true);
    mSearchBar.addComponentListener(this);

    super.initSearch(ae);
    getSearchField().setText("");
    getSearchField().removeFocusListener(this);
    getSearchField().addFocusListener(this);

  }

  protected boolean changed(JComponent comp, String str, Position.Bias bias) {
    JTextComponent textComp = (JTextComponent) comp;
    int offset = bias == Position.Bias.Forward ? textComp.getCaretPosition()
        : textComp.getCaret().getMark() - 1;

    int index = getNextMatch(textComp, str, offset, bias);

    if (index != -1) {
      textComp.setSelectionStart(index);
      textComp.setSelectionEnd(index + str.length());
      return true;
    } else {
      offset = bias == null || bias == Position.Bias.Forward ? 0 : textComp
          .getDocument().getLength();
      index = getNextMatch(textComp, str, offset, bias);
      if (index != -1) {
        textComp.select(index, index + str.length());
        return true;
      } else {
        return false;
      }
    }
  }

  protected int getNextMatch(JTextComponent textComp, String str,
      int startingOffset, Position.Bias bias) {
    String text;
    try {
      text = textComp.getDocument().getText(0,
          textComp.getDocument().getLength());
      if (isIgnoreCase()) {
        str = str.toLowerCase();
        text = text.toLowerCase();
      }

      return bias == null || bias == Position.Bias.Forward ? text.indexOf(str,
          startingOffset) : text.lastIndexOf(str, startingOffset);
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    return -1;
  }

  /*-------------------------------------------------[ FocusListener ]---------------------------------------------------*/

  // ensures that the selection is visible
  // because textcomponent doesn't show selection
  // when they don't have focus
  public void focusGained(FocusEvent e) {
    Caret caret = getTextComponent().getCaret();
    caret.setVisible(false);
    caret.setSelectionVisible(true);
  }

  public void focusLost(FocusEvent e) {}

  public JTextComponent getTextComponent() {
    return (JTextComponent) getComponent();
  }

  /*-------------------------------------------------[ ComponentListener ]---------------------------------------------------*/

  public void componentHidden(ComponentEvent e) {
    mSearchCloseBtn.setVisible(false);
  }

  public void componentMoved(ComponentEvent e) {}

  public void componentResized(ComponentEvent e) {}

  public void componentShown(ComponentEvent e) {
    interrupt();
    actionPerformed(new ActionEvent(this, 0, "show"));
  }

  /*-------------------------------------------------[ Runnable ]---------------------------------------------------*/

  /**
   * Waits for closing of the panel.
   */
  public void run() {
    try {
      while (getWaitTime() > 0) {
        Thread.sleep(100);
        setWaitTime(getWaitTime() - 100);
      }
      mSearchBar.setVisible(false);
      mSearchCloseBtn.setVisible(false);
      setWaitTime(5000);
    } catch (Exception e) {}
  }

  // Start of added methods

  /**
   * Interrupts the closing Thread.
   */
  public void interrupt() {
    if (getThread() != null && getThread().isAlive()) {
      getThread().interrupt();
    }
    setWaitTime(5000);
  }

  /**
   * @return The search bar
   */
  public JPanel getSearchBar() {
    return mSearchBar;
  }

  /**
   * Shows the search bar
   */
  public void showSearchBar() {
    if (!mSearchBar.isVisible()) {
      mFindPrev.setEnabled(false);
      mFindNext.setEnabled(false);
      reset();
    }
    setBlockAutoClosing(true);
    // Stop the automatically closing
    interrupt();
    mSearchBar.setVisible(true);
    mSearchCloseBtn.setVisible(true);
    getSearchField().requestFocusInWindow();
  }

  /**
   * @return If the search bar isVisible and will not be closed automatically.
   */
  public boolean isAlwaysVisible() {
    return mSearchBar.isVisible() && isBlockAutoClosing();
  }
  
  /**
   * @return The close button of the search bar.
   */
  public JButton getCloseButton() {
    return mSearchCloseBtn;
  }
}