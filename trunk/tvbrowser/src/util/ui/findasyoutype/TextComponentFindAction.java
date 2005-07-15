package util.ui.findasyoutype;

import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.JComponent;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;

public class TextComponentFindAction extends FindAction implements FocusListener {

  public TextComponentFindAction(JTextComponent comp) {
    super(comp);
  }
  
  public TextComponentFindAction(JTextComponent comp, boolean startAtKeyType) {
    super(comp, startAtKeyType);
  }
  
  // 1. inits searchField with selected text
  // 2. adds focus listener so that textselection gets painted
  // even if the textcomponent has no focus
  protected void initSearch(ActionEvent ae) {
    super.initSearch(ae);
    getSearchField().setText("");
    getSearchField().removeFocusListener(this);
    getSearchField().addFocusListener(this);
  }

  protected boolean changed(JComponent comp, String str, Position.Bias bias) {
    JTextComponent textComp = (JTextComponent) comp;
    int offset = bias == Position.Bias.Forward ? textComp.getCaretPosition() : textComp.getCaret().getMark() - 1;

    int index = getNextMatch(textComp, str, offset, bias);
    if (index != -1) {
      textComp.select(index, index + str.length());
      return true;
    } else {
      offset = bias == null || bias == Position.Bias.Forward ? 0 : textComp.getDocument().getLength();
      index = getNextMatch(textComp, str, offset, bias);
      if (index != -1) {
        textComp.select(index, index + str.length());
        return true;
      } else
        return false;
    }
  }

  protected int getNextMatch(JTextComponent textComp, String str, int startingOffset, Position.Bias bias) {
    String text;
    try {
      text = textComp.getDocument().getText(0, textComp.getDocument().getLength());

      if (ignoreCase) {
        str = str.toUpperCase();
        text = text.toUpperCase();
      }

      return bias == null || bias == Position.Bias.Forward ? text.indexOf(str, startingOffset) : text.lastIndexOf(str,
          startingOffset);
    } catch (BadLocationException e) {
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
    caret.setVisible(true);
    caret.setSelectionVisible(true);
  }

  public void focusLost(FocusEvent e) {
  }
  
  public JTextComponent getTextComponent() {
    return (JTextComponent) getComponent();
  }
}