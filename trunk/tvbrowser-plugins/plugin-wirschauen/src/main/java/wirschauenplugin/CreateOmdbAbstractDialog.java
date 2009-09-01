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

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

import util.ui.Localizer;
import util.ui.UiUtilities;
import util.ui.WindowClosingIf;

import com.jgoodies.forms.builder.ButtonBarBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import devplugin.Program;
import devplugin.ProgramFieldType;

/**
 * the dialog to enter the abstract for omdb. after the ok-button was pressed
 * the data can be obtained via the getOmdbAbstractInput-method. if the dialog
 * was cancelled (button, esc, close window) isCancelled will return true. in
 * that case getOmdbAbstractInput returns a undefined value.
 *
 * @author uzi
 * @date 30.08.2009
 */
@SuppressWarnings("serial")
public class CreateOmdbAbstractDialog
extends JDialog
implements WindowClosingIf
{
  /**
   * true, if one of the different cancel-methods was used. every user initiated closing of
   * the dialog means that cancelled is true.
   */
  private boolean cancelled = false;


  /**
   * the abstract from the input field of the form.
   */
  private String omdbAbstractInput = "";


  /**
   * max input length of the abstact in characters.
   */
  private static short MAX_CHARS_IN_ABSTRACT = 400;


  /**
   * Localizer
   */
  private static final Localizer mLocalizer = Localizer.getLocalizerFor(CreateOmdbAbstractDialog.class);




  /**
   * creates an application modal dialog to input an abstract.
   *
   * @param parent the parent window of this dialog
   * @param program the program, which the abstract is for (to display some data, e.g. the name of the program)
   * @param wirSchauenEvent the WirSchauenEvent corresponding to the program (to display the omdb-link and film/series)
   * @param oldOmdbAbstract the old abstract for the program (init value for the input field)
   */
  public CreateOmdbAbstractDialog(Window parent, Program program, WirSchauenEvent wirSchauenEvent, String oldOmdbAbstract)
  {
    //create the window
    super(parent, mLocalizer.msg("DialogTitle", "Description for OMDB.org"), ModalityType.APPLICATION_MODAL);
    UiUtilities.registerForClosing(this); //register esc-button
    JPanel contentPane = (JPanel) getContentPane();
    contentPane.setBorder(Borders.DLU4_BORDER);
    contentPane.setLayout(new FormLayout("right:pref, 3dlu, pref:grow, pref", "pref, 3dlu, pref, 3dlu, pref, 3dlu, pref, 3dlu, fill:50dlu:grow, 3dlu, pref"));
    CellConstraints cellConstraints = new CellConstraints();
    addWindowListener(new WindowAdapter()
    {
      @Override
      public void windowClosing(WindowEvent e)
      {
        close();
      }
    });

    //labels for movie and episode titles (not changeable)
    contentPane.add(DialogUtil.createBoldLabel(WirSchauenPlugin.mLocalizer.msg("PropertyLabels.Title", "Title")), cellConstraints.xy(1, 1));
    contentPane.add(DialogUtil.createReadOnlySelectAllTextField(program.getTitle()), cellConstraints.xyw(3, 1, 2));
    //if there is no episode title, take the episode number
    String episodeTitle = program.getTextField(ProgramFieldType.EPISODE_TYPE);
    if (episodeTitle == null || episodeTitle.length() == 0)
    {
      episodeTitle = program.getIntFieldAsString(ProgramFieldType.EPISODE_NUMBER_TYPE);
    }
    //if any episode id (title or number) was found, display it
    if (episodeTitle != null && episodeTitle.length() > 0)
    {
      contentPane.add(DialogUtil.createBoldLabel(WirSchauenPlugin.mLocalizer.msg("PropertyLabels.Episode", "Episode")), cellConstraints.xy(1, 3));
      contentPane.add(DialogUtil.createReadOnlySelectAllTextField(episodeTitle), cellConstraints.xyw(3, 3, 2));
    }

    //omdb-link (not changeable)
    contentPane.add(DialogUtil.createBoldLabel(WirSchauenPlugin.mLocalizer.msg("PropertyLabels.OmdbUrl", "URL")), cellConstraints.xy(1, 5));
    contentPane.add(DialogUtil.createReadOnlySelectAllTextField(wirSchauenEvent.getOmdbUrl()), cellConstraints.xy(3, 5));
    //this buttons opens the omdb-page for this program
    contentPane.add(DialogUtil.createUrlButton(wirSchauenEvent.getOmdbUrl(), WirSchauenPlugin.mLocalizer.msg("OmdbButton", "Open OMDB in Browser")), cellConstraints.xy(4, 5));

    //category (movie or series, not changeable)
    contentPane.add(DialogUtil.createBoldLabel(WirSchauenPlugin.mLocalizer.msg("PropertyLabels.Category", "Category")), cellConstraints.xy(1, 7));
    contentPane.add(DialogUtil.createReadOnlySelectAllTextField(mapCategoryToString(wirSchauenEvent.getCategory())), cellConstraints.xyw(3, 7, 2));

    //description/abstract (changeable)
    contentPane.add(DialogUtil.createBoldLabel(WirSchauenPlugin.mLocalizer.msg("PropertyLabels.Description", "Description")), cellConstraints.xy(1, 9, CellConstraints.DEFAULT, CellConstraints.TOP));
    final DescriptionInputField descriptionInputField = new DescriptionInputField(CreateOmdbAbstractDialog.MAX_CHARS_IN_ABSTRACT, oldOmdbAbstract, WirSchauenPlugin.mLocalizer.msg("RemainingChars", "%s Characters remaining"));
    contentPane.add(descriptionInputField, cellConstraints.xyw(3, 9, 2));

    //buttons (ok, cancel)
    JButton okButton = new JButton(Localizer.getLocalization(Localizer.I18N_OK));
    okButton.setEnabled(false);
    //the ok-button is controlled by the input field (eg deactivated when no input). therefore the DocumentButtonController.
    descriptionInputField.addDocumentListener(new DocumentButtonController(okButton, oldOmdbAbstract));
    okButton.addActionListener(new ActionListener()
    {
      public void actionPerformed(final ActionEvent e)
      {
        //provide the input and close the dialog
        omdbAbstractInput = descriptionInputField.getText();
        setVisible(false);
        dispose();
      }
    });
    final JButton cancelButton = new JButton(Localizer.getLocalization(Localizer.I18N_CANCEL));
    cancelButton.addActionListener(new ActionListener()
    {
      public void actionPerformed(final ActionEvent e)
      {
        close();
      }
    });
    final ButtonBarBuilder builder = new ButtonBarBuilder();
    builder.addGlue();
    builder.addGriddedButtons(new JButton[]{okButton, cancelButton});
    contentPane.add(builder.getPanel(), cellConstraints.xyw(1, 11, 4));

    pack();
  }




  /**
   * called when the user closes the dialog by pressing esc, using the
   * window control or pushing the cancel button. closes the dialog and
   * provides the cancel status.
   *
   * @see util.ui.WindowClosingIf#close()
   */
  public void close()
  {
    cancelled = true;
    setVisible(false);
    dispose();
  }




  /**
   * @return true, if one of the cancel methods have been used (esc key, cancel button, window control)
   */
  public boolean isCancelled()
  {
    return cancelled;
  }




  /**
   * @return the input from the user.
   */
  public String getOmdbAbstractInput()
  {
    return omdbAbstractInput;
  }


  /**
   * converts the category to a localized string.
   *
   * @param category the category (see WirSchauenEvent constants)
   * @return a localized string
   */
  private String mapCategoryToString(byte category)
  {
    if (category == WirSchauenEvent.CATEGORY_MOVIE)
    {
      return WirSchauenPlugin.mLocalizer.msg("Category.Movie", "Movie");
    }
    if (category == WirSchauenEvent.CATEGORY_SERIES)
    {
      return WirSchauenPlugin.mLocalizer.msg("Category.Series", "Series");
    }
    if (category == WirSchauenEvent.CATEGORY_OTHER)
    {
      return WirSchauenPlugin.mLocalizer.msg("Category.Other", "Series");
    }
    return null;
  }
}
