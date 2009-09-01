/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package wirschauenplugin;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;

/**
 * This class is a DocumentListener and a ItemListener. it is intended to control
 * ((de)activate) an ok-button. the triggers are a JComboBox (ItemListener) and a
 * document/input field (DocumentListener). the button is activated if the index
 * of the selected item (combo box) is not 0 and the length of the text in the
 * input field is >0.
 *
 * @author uzi
 * @date 30.08.2009
 */
public class DocumentAndItemButtonController
implements DocumentListener, ItemListener
{
  /**
   * the button to control
   */
  private JButton button;

  /**
   * the document's (input field's) vote to activate the button
   */
  private boolean docOk = false;

  /**
   * the dropdown's vote to activate the button
   */
  private boolean dropdownOk = false;



  /**
   * @param button the button to control
   */
  public DocumentAndItemButtonController(JButton button)
  {
    this.button = button;
  }


  /**
   * called whenever one of the listeners fires. if both listeners
   * vote to activate the button, the button is activated.
   */
  private void updateButton()
  {
    button.setEnabled(docOk && dropdownOk);
  }


  /**
   * votes for the activation of the button if the selected item in the combo box
   * has not the index 0.
   *
   * @see java.awt.event.ItemListener#itemStateChanged(java.awt.event.ItemEvent)
   */
  public void itemStateChanged(ItemEvent e)
  {
    if (e.getStateChange() == ItemEvent.SELECTED)
    {
      dropdownOk = ((JComboBox) e.getItemSelectable()).getSelectedIndex() != 0;
      updateButton();
    }
  }


  /**
   * called whenever the document listener fires. votes to activate the button
   * if the length of the document is > 0.
   *
   * @param e
   */
  private void docUpdated(DocumentEvent e)
  {
    try
    {
      docOk = !(e.getDocument().getText(0, e.getDocument().getLength()).trim().length() == 0);
      updateButton();
    }
    catch (BadLocationException ble)
    {
      ble.printStackTrace();
    }
  }

  /**
   * @see javax.swing.event.DocumentListener#changedUpdate(javax.swing.event.DocumentEvent)
   */
  public void changedUpdate(DocumentEvent e)
  {
    docUpdated(e);
  }

  /**
   * @see javax.swing.event.DocumentListener#insertUpdate(javax.swing.event.DocumentEvent)
   */
  public void insertUpdate(DocumentEvent e)
  {
    docUpdated(e);
  }

  /**
   * @see javax.swing.event.DocumentListener#removeUpdate(javax.swing.event.DocumentEvent)
   */
  public void removeUpdate(DocumentEvent e)
  {
    docUpdated(e);
  }
}
