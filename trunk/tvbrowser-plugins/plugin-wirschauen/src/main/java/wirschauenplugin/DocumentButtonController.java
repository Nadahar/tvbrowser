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

import javax.swing.JButton;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;

/**
 * This class is a DocumentListener. it is intended to control ((de)activate) an
 * ok-button. the button is activated if the length of the document is >0 and the
 * content of the document is not equal oldDescription.
 *
 * @author uzi
 * @date 30.08.2009
 */
public class DocumentButtonController
implements DocumentListener
{
  /**
   * the button to control.
   */
  private JButton button;

  /**
   * the old description.
   */
  private String oldDescription;



  /**
   * the button is activated if the ne description != old descrption and
   * the new description is not the empty string.
   *
   * @param button the button to control
   * @param oldDescription the old description to change
   */
  public DocumentButtonController(JButton button, String oldDescription)
  {
    this.button = button;
    this.oldDescription = oldDescription;
  }


  /**
   * called whenever the document listener fires. this method will
   * (de)activate the button.
   *
   * @param e
   */
  private void docUpdated(DocumentEvent e)
  {
    try
    {
      button.setEnabled(!(e.getDocument().getText(0, e.getDocument().getLength()).trim().length() == 0) &&
          !e.getDocument().getText(0, e.getDocument().getLength()).equals(oldDescription));
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
