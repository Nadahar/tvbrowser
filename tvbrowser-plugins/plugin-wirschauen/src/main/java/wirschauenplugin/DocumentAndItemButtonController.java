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
 */
public class DocumentAndItemButtonController implements DocumentListener, ItemListener
{
  /**
   * the button to control.
   */
  private JButton mButton;

  /**
   * the document's (input field's) vote to activate the button.
   */
  private boolean mDocOk;

  /**
   * the dropdown's vote to activate the button.
   */
  private boolean mDropdownOk;



  /**
   * @param button the button to control
   */
  public DocumentAndItemButtonController(final JButton button)
  {
    this.mButton = button;
  }


  /**
   * called whenever one of the listeners fires. if both listeners
   * vote to activate the button, the button is activated.
   */
  private void updateButton()
  {
    mButton.setEnabled(mDocOk && mDropdownOk);
  }


  /**
   * votes for the activation of the button if the selected item in the combo box
   * has not the index 0.
   *
   * @param e the event fired
   * @see java.awt.event.ItemListener#itemStateChanged(java.awt.event.ItemEvent)
   */
  public void itemStateChanged(final ItemEvent e)
  {
    if (e.getStateChange() == ItemEvent.SELECTED)
    {
      mDropdownOk = ((JComboBox) e.getItemSelectable()).getSelectedIndex() != 0;
      updateButton();
    }
  }


  /**
   * called whenever the document listener fires. votes to activate the button
   * if the length of the document is > 0.
   *
   * @param e the event fired
   */
  private void docUpdated(final DocumentEvent e)
  {
    try
    {
      mDocOk = !(e.getDocument().getText(0, e.getDocument().getLength()).trim().length() == 0);
      updateButton();
    }
    catch (final BadLocationException ble)
    {
      ble.printStackTrace();
    }
  }

  /**
   * {@inheritDoc}
   */
  public void changedUpdate(final DocumentEvent e)
  {
    docUpdated(e);
  }

  /**
   * {@inheritDoc}
   */
  public void insertUpdate(final DocumentEvent e)
  {
    docUpdated(e);
  }

  /**
   * {@inheritDoc}
   */
  public void removeUpdate(final DocumentEvent e)
  {
    docUpdated(e);
  }
}
