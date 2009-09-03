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
 */
@SuppressWarnings("serial")
public class DescriptionInputField extends JPanel implements DocumentListener
{
  /**
   * limits the length of the text area.
   */
  private int mMaxChars;

  /**
   * shows the remaining chars.
   */
  private JLabel mCounterLabel;

  /**
   * the string for the counterLabel.
   */
  private String mLabel;

  /**
   * the text area.
   */
  private JTextArea mTextArea;


  /**
   * builds a lengthwise limited text area with a remaining-char-counter beneath it.
   *
   * @param maxChars the max length of the text area
   * @param value the default value of the text area
   * @param label the remaining characters label. it must include a %s as placeholder for the counter.
   */
  public DescriptionInputField(final int maxChars, final String value, final String label)
  {
    this.mMaxChars = maxChars;
    this.mLabel = label;

    setLayout(new FormLayout("pref:grow", "fill:50dlu:grow, 3dlu, pref"));
    CellConstraints cellConstraints = new CellConstraints();

    mTextArea = DialogUtil.createTextArea(maxChars); //the text area ensures the length limitation
    mTextArea.getDocument().addDocumentListener(this); //to update the label

    add(new JScrollPane(mTextArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER), cellConstraints.xy(1, 1));

    mCounterLabel = new JLabel(String.format(label, maxChars - mTextArea.getDocument().getLength()));
    add(mCounterLabel, cellConstraints.xy(1, 3, CellConstraints.RIGHT, CellConstraints.CENTER));

    //this fires a event to the document listeners (i.e. this). so be sure, everything is initialized (e.g. counter label)
    String initialValue = null;
    if (value != null) {
      initialValue = value.trim();
    }
    mTextArea.insert(initialValue, 0);
  }


  /**
   * @return the user input
   */
  public String getText()
  {
    return mTextArea.getText().trim();
  }


  /**
   * adds a document listener to the underlying text area.
   *
   * @param listener the listener to add
   */
  public void addDocumentListener(final DocumentListener listener)
  {
    mTextArea.getDocument().addDocumentListener(listener);
  }




  /**
   * called every time the document listener fires. updates the label
   * for the remaining characters.
   *
   * @param event the document event fired
   */
  private void update(final DocumentEvent event)
  {
    mCounterLabel.setText(String.format(mLabel, mMaxChars - event.getDocument().getLength()));
  }



  /**
   * {@inheritDoc}
   */
  public void changedUpdate(final DocumentEvent event)
  {
    update(event);
  }

  /**
   * {@inheritDoc}
   */
  public void insertUpdate(final DocumentEvent event)
  {
    update(event);
  }

  /**
   * {@inheritDoc}
   */
  public void removeUpdate(final DocumentEvent event)
  {
    update(event);
  }
}
