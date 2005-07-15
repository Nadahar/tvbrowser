package util.ui.findasyoutype;

import java.awt.BorderLayout;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;


public class FindAsYouTypeTest extends JFrame {

  /**
   * @param args
   */
  public static void main(String[] args) {

 /*   JTextArea textArea = new JTextArea(10, 50);
    textArea.setEditable(false);
    try {
      new HTMLEditorKit().read(new , textArea.getDocument(), 0);
    } catch (Exception e) {
      e.printStackTrace();
    }
   */
    
    JEditorPane textArea;
    try {
      textArea = new JEditorPane("http://www.heise.de");
      textArea.setEditable (false);
      textArea.setContentType ("text/html");
      
      
      FindAction findAction = new TextComponentFindAction(textArea, true);
      
      JScrollPane scroll = new JScrollPane(textArea);
      scroll.setBorder(BorderFactory.createCompoundBorder(
              BorderFactory.createEmptyBorder(50, 10, 10, 10),
              scroll.getBorder()
      ));
      
      JFrame frame = new JFrame();
      
      frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      JPanel panel = (JPanel)frame.getContentPane();
      
      panel.setLayout(new BorderLayout());
      panel.add(scroll, BorderLayout.CENTER);
      frame.setSize(500, 500);
      
      frame.setVisible(true);
      
      //JOptionPane.showMessageDialog(null, new JScrollPane(scroll));
    
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    
    
    
  }

}
