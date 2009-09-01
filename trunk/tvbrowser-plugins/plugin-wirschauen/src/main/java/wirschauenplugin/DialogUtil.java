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
 */package wirschauenplugin;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.NumberFormat;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

import util.browserlauncher.Launch;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * just a helper class to create form components.
 *
 * @author uzi
 * @date 30.08.2009
 */
public class DialogUtil
{
  /**
   * creates a JTextField. it allows only numeric input.
   *
   * @param tooltip the tooltip is shown when the mouse cursor is over the input field
   * @return the JTextField
   */
  @SuppressWarnings("serial")
  public static JTextField createNumericInput(String tooltip)
  {
    NumberFormat integerFormat = NumberFormat.getIntegerInstance();
    integerFormat.setGroupingUsed(false);
    integerFormat.setParseIntegerOnly(true);
    final JTextField urlInput = new JTextField();
    urlInput.setToolTipText(tooltip);
    urlInput.setDocument(new PlainDocument()
    {
      @Override
      public void insertString(final int offset, final String input, final AttributeSet a)
      throws BadLocationException
      {
        final String filtered = input.replaceAll("\\D", "");
        if (!filtered.equals(input))
        {
          Toolkit.getDefaultToolkit().beep();
        }
        super.insertString(offset, filtered, a);
      }
    });
    return urlInput;
  }



  /**
   * creates a JTextArea with a limited length. tabs will be replaced
   * by the empty string.
   *
   * @param maxCharCount the max input length of the text area
   * @return the JTextArea
   */
  @SuppressWarnings("serial")
  public static JTextArea createTextArea(final int maxCharCount)
  {
    final JTextArea textArea = new JTextArea();
    textArea.setDocument(new PlainDocument()
    {
      @Override
      public void insertString(final int offs, String str, AttributeSet a)
      throws BadLocationException
      {
        if (str == null)
        {
          return;
        }
        str = str.replaceAll("\t", "");
        if (getLength() + str.length() > maxCharCount)
        {
          Toolkit.getDefaultToolkit().beep();
        }
        else
        {
          super.insertString(offs, str, a);
        }
      }
    });
    textArea.setLineWrap(true);
    textArea.setWrapStyleWord(true);
    return textArea;
  }


  /**
   * creates a button which will open a url in the browser.
   *
   * @param url the url to open
   * @param toolTip the tooltip of the button
   * @return the button
   */
  public static JButton createUrlButton(final String url, String toolTip)
  {
    JButton urlButton = new JButton(WirSchauenPlugin.getInstance().createImageIcon("apps", "internet-web-browser", 16));
    urlButton.setToolTipText(toolTip);
    urlButton.addActionListener(new ActionListener()
    {
      public void actionPerformed(final ActionEvent e)
      {
        Launch.openURL(url);
      }
    });
    return urlButton;
  }


  /**
   * creates a bold label.
   *
   * @param labelString the label label ;)
   * @return the label
   */
  public static JLabel createBoldLabel(String labelString)
  {
    JLabel label = new JLabel(labelString);
    label.setFont(label.getFont().deriveFont(Font.BOLD));
    return label;
  }



  /**
   * creates a JTextField which is not editable. if the user clicks in
   * it the whole value of the text field will get marked.
   *
   * @param value the value of the text field
   * @return
   */
  public static JTextField createReadOnlySelectAllTextField(String value)
  {
    final JTextField textField = new JTextField(value);
    textField.setEditable(false);
    textField.addMouseListener(new MouseAdapter()
    {
      @Override
      public void mouseClicked(final MouseEvent e)
      {
        textField.selectAll();
      }
    });
    return textField;
  }



  /**
   * creates a JCombobox which is not editable. the first options background is red.
   *
   * @param options the options of the dropdown.
   * @param selected the option which is selected
   * @return the JComboBox
   */
  @SuppressWarnings("serial")
  public static JComboBox createUneditableDropdown(final String[] options, int selected)
  {
    JComboBox dropdown = new JComboBox(options);
    dropdown.setEditable(false);
    dropdown.setMaximumRowCount(8);
    dropdown.setRenderer(new DefaultListCellRenderer()
    {
      @Override
      public Component getListCellRendererComponent(final JList list, final Object value, final int index, final boolean isSelected, final boolean cellHasFocus)
      {
        Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        final JPanel colorPanel = new JPanel(new FormLayout("default:grow", "fill:default:grow"));
        ((JLabel) c).setOpaque(true);
        if ((index == -1 && list.getSelectedIndex() == 0) || index == 0)
        {
          c.setForeground(Color.white);
          c.setBackground(Color.red);
        }
        colorPanel.setOpaque(false);
        colorPanel.add(c, new CellConstraints().xy(1, 1));
        c = colorPanel;
        return c;
      }
    });
    dropdown.setSelectedIndex(selected);
    return dropdown;
  }



  /**
   * creates a JCombobox which is not editable. the first options background is red
   * and it is selected.
   *
   * @param options the options of the dropdown.
   * @return the JComboBox
   */
  public static JComboBox createUneditableDropdown(final String[] options)
  {
    return createUneditableDropdown(options, 0);
  }



  /**
   * creates a dropdown which is editable. an AutoCompletion is used.
   *
   * @param options the options of the drowdown.
   * @return the JComboBox
   */
  public static JComboBox createEditableDropdown(final String[] options)
  {
    JComboBox dropdown = new JComboBox(options);
    dropdown.setEditable(true);
    dropdown.setMaximumRowCount(8);
    dropdown.setSelectedIndex(-1);
    new util.ui.AutoCompletion(dropdown, true);
    return dropdown;
  }
}
