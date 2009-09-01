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

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * this class is a jpanel with a text area in it. the text area is limited in length.
 * underneath the text area is a label which shows how many characters are still
 * remaining.
 *
 * @author uzi
 * @date 30.08.2009
 */
@SuppressWarnings("serial")
public class DescriptionInputField
extends JPanel
implements DocumentListener
{
  /**
   * limits the length of the text area.
   */
  private int maxChars;

  /**
   * shows the remaining chars.
   */
  private JLabel counterLabel;

  /**
   * the string for the counterLabel.
   */
  private String label;

  /**
   * the text area.
   */
  private JTextArea textArea;


  /**
   * builds a lengthwise limited text area with a remaing-char-counter beneath it.
   *
   * @param maxChars the max legth of the text area
   * @param value the default value of the text area
   * @param label the remaing characters label. it must include a %s as placeholder for the counter.
   */
  public DescriptionInputField(int maxChars, String value, String label)
  {
    this.maxChars = maxChars;
    this.label = label;

    setLayout(new FormLayout("pref:grow", "fill:50dlu:grow, 3dlu, pref"));
    CellConstraints cellConstraints = new CellConstraints();

    textArea = DialogUtil.createTextArea(maxChars); //the text area ensures the length limitation
    textArea.getDocument().addDocumentListener(this); //to update the label

    add(new JScrollPane(textArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER), cellConstraints.xy(1, 1));

    counterLabel = new JLabel(String.format(label, maxChars - textArea.getDocument().getLength()));
    add(counterLabel, cellConstraints.xy(1, 3, CellConstraints.RIGHT, CellConstraints.CENTER));

    //this fires a event to the document listeners (i.e. this). so be sure, everything is initialized (e.g. counter label)
    textArea.insert(value, 0);
  }


  /**
   * @return the user input
   */
  public String getText()
  {
    return textArea.getText();
  }


  /**
   * adds a document listener to the underlying text area.
   *
   * @param listener the listener to add
   */
  public void addDocumentListener(DocumentListener listener)
  {
    textArea.getDocument().addDocumentListener(listener);
  }




  /**
   * called everytime the document listener fires. updates the label
   * for the remaining characters.
   *
   * @param event
   */
  private void update(DocumentEvent event)
  {
    counterLabel.setText(String.format(label, maxChars - event.getDocument().getLength()));
  }



  /**
   * @see javax.swing.event.DocumentListener#changedUpdate(javax.swing.event.DocumentEvent)
   */
  public void changedUpdate(DocumentEvent event)
  {
    update(event);
  }

  /**
   * @see javax.swing.event.DocumentListener#insertUpdate(javax.swing.event.DocumentEvent)
   */
  public void insertUpdate(DocumentEvent event)
  {
    update(event);
  }

  /**
   * @see javax.swing.event.DocumentListener#removeUpdate(javax.swing.event.DocumentEvent)
   */
  public void removeUpdate(DocumentEvent event)
  {
    update(event);
  }
}
