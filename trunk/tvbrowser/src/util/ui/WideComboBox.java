package util.ui;

import javax.swing.*; 
import java.awt.*; 
import java.util.Vector; 
 
// Taken from http://www.jroller.com/santhosh/entry/make_jcombobox_popup_wide_enough
//
// got this workaround from the following bug: 
//      http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4618607 
public class WideComboBox<E> extends JComboBox<E>{ 
 
    public WideComboBox() { 
    } 
 
    public WideComboBox(final E items[]){ 
        super(items); 
    } 
 
    public WideComboBox(Vector<E> items) { 
        super(items); 
    } 
 
    public WideComboBox(ComboBoxModel<E> aModel) { 
        super(aModel); 
    } 
 
    private boolean layingOut = false; 
 
    public void doLayout(){ 
        try{ 
            layingOut = true; 
            super.doLayout(); 
        }finally{ 
            layingOut = false; 
        } 
    } 
 
    public Dimension getSize(){ 
        Dimension dim = super.getSize(); 
        if(!layingOut) 
            dim.width = Math.max(dim.width, getPreferredSize().width); 
        return dim; 
    }
    
    public boolean contains(E test) {
      boolean result = false;
      
      final ComboBoxModel<E> model = getModel();
      
      for(int i = 0; i < model.getSize(); i++) {
        if(test.equals(model.getElementAt(i))) {
          result = true;
          break;
        }
      }
      
      return result;
    }
}