/*
 * Created on 01.11.2004
 */
package util.ui.beanshell;

import java.awt.Dimension;

import javax.swing.JEditorPane;
import javax.swing.JViewport;
import javax.swing.plaf.TextUI;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;
import javax.swing.text.StyledEditorKit;

/**
 * A TextArea for BeanShell-Skripts. Including Syntax-Highlighting
 * 
 * @author bodum
 */
public class BeanShellEditor extends JEditorPane {

  /**
   * Create the TextArea
   */
  public BeanShellEditor() {
    setDocument(new SyntaxDocument());

    EditorKit editorKit = new StyledEditorKit() {
      public Document createDefaultDocument() {
        return new SyntaxDocument();
      }
    };

    setEditorKitForContentType("text/beanshell", editorKit);
    setContentType("text/beanshell");
    
    
  }

  /**
   * Override to get no Line-Wraps
   */
  public boolean getScrollableTracksViewportWidth() {
    if (getParent() instanceof JViewport) {
      JViewport port = (JViewport) getParent();
      TextUI ui = getUI();
      int w = port.getWidth();
      Dimension min = ui.getMinimumSize(this);
      Dimension max = ui.getMaximumSize(this);
      Dimension pref = ui.getPreferredSize(this);
      
      if ((w >= pref.width)) {
        return true;
      }
    }
    return false;
  }

}
