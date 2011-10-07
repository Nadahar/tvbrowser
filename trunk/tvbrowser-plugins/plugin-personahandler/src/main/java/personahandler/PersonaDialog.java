package personahandler;

import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JSeparator;

import util.ui.Localizer;
import util.ui.UiUtilities;
import util.ui.WindowClosingIf;

import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

public class PersonaDialog extends JDialog implements WindowClosingIf {
  
  PersonaDialog(Window parent) {
    super(parent);
    setTitle("PersonaHandler");
    setModal(true);
    
    //setContentPane(Persona.createPersonaBackgroundPanel());
    
    ((JPanel)getContentPane()).setBorder(Borders.DIALOG_BORDER);
    
    getContentPane().setLayout(new FormLayout("50dlu:grow,default,50dlu:grow","fill:default:grow,5dlu,default,5dlu,default"));
    
    JButton close = new JButton(Localizer.getLocalization(Localizer.I18N_CLOSE));
    close.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        dispose();
      }
    });
    
    getContentPane().add(new PersonaHandlerPanel(true), CC.xyw(1,1,3));
    getContentPane().add(new JSeparator(), CC.xyw(1,3,3));
    getContentPane().add(close, CC.xy(2,5));
    
    PersonaHandler.getInstance().layoutWindow("personaDialog",this,new Dimension(600,550));
    
    UiUtilities.registerForClosing(this);
  }

  @Override
  public void close() {
    dispose();
  }
  
  public void updatePersona() {
    getContentPane().repaint();
  }
  

}
