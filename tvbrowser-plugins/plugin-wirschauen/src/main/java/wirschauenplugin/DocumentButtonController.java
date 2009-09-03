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
 */
public class DocumentButtonController implements DocumentListener
{
  /**
   * the button to control.
   */
  private JButton mButton;

  /**
   * the old description.
   */
  private String mOldDescription;



  /**
   * the button is activated if the ne description != old descrption and
   * the new description is not the empty string.
   *
   * @param button the button to control
   * @param oldDescription the old description to change
   */
  public DocumentButtonController(final JButton button, final String oldDescription)
  {
    this.mButton = button;
    this.mOldDescription = oldDescription;
  }


  /**
   * called whenever the document listener fires. this method will
   * (de)activate the button.
   *
   * @param e the event fired
   */
  private void docUpdated(final DocumentEvent e)
  {
    try
    {
      mButton.setEnabled(!(e.getDocument().getText(0, e.getDocument().getLength()).trim().length() == 0)
          && !e.getDocument().getText(0, e.getDocument().getLength()).equals(mOldDescription));
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
