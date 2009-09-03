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
import java.util.Arrays;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
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
 * the dialog to enter data for wirschauen. after the ok-button was pressed
 * the data can be obtained via getWirSchauenInput. if the dialog was cancelled
 * (button, esc, close window) isCancelled will return true.
 *
 * @author uzi
 */
@SuppressWarnings("serial")
public class CreateWirSchauenDataDialog extends JDialog implements WindowClosingIf
{
  /**
   * max input length of the abstract in characters.
   */
  private static final short MAX_CHARS_IN_ABSTRACT = 200;


  /**
   * Localizer.
   */
  private static final Localizer LOCALIZER = Localizer.getLocalizerFor(CreateWirSchauenDataDialog.class);



  /**
   * true, if one of the different cancel-methods was used. every user initiated closing of
   * the dialog means that cancelled is true.
   */
  private boolean mCancelled;

  /**
   * the form-input from the user.
   */
  private WirSchauenEvent mWirSchauenInput;





  /**
   * creates an application modal dialog to input the wirschauen data.
   *
   * @param parent the parent window of this dialog
   * @param program the program, which the data is for
   * @param wirSchauenEvent the (old) wirschauen data loaded from the server
   */
  public CreateWirSchauenDataDialog(final Window parent, final Program program, final WirSchauenEvent wirSchauenEvent)
  {
    //create the window
    super(parent, LOCALIZER.msg("DialogTitle", "Description for WirSchauen.de"), ModalityType.APPLICATION_MODAL);
    UiUtilities.registerForClosing(this);
    JPanel contentPane = (JPanel) getContentPane();
    contentPane.setBorder(Borders.DLU4_BORDER);
    contentPane.setLayout(new FormLayout("right:pref, 3dlu, pref:grow", "pref, 3dlu, pref, 3dlu, pref, 3dlu, pref, 3dlu, fill:50dlu:grow, 3dlu, pref, pref, pref, 3dlu, pref"));
    CellConstraints cellConstraints = new CellConstraints();
    addWindowListener(new WindowAdapter()
    {
      @Override
      public void windowClosing(final WindowEvent e)
      {
        close();
      }
    });

    //labels for movie and episode titles
    contentPane.add(DialogUtil.createBoldLabel(WirSchauenPlugin.mLocalizer.msg("PropertyLabels.Title", "Title")), cellConstraints.xy(1, 1));
    contentPane.add(DialogUtil.createReadOnlySelectAllTextField(program.getTitle()), cellConstraints.xy(3, 1));
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
      contentPane.add(DialogUtil.createReadOnlySelectAllTextField(episodeTitle), cellConstraints.xy(3, 3));
    }

    //dropdown category (movie or series)
    contentPane.add(DialogUtil.createBoldLabel(WirSchauenPlugin.mLocalizer.msg("PropertyLabels.Category", "Category")), cellConstraints.xy(1, 5));
    final JComboBox categoryDropdown = DialogUtil.createUneditableDropdown(new String[] {WirSchauenPlugin.mLocalizer.msg("Category.NotSet", "not yet set"), WirSchauenPlugin.mLocalizer.msg("Category.Movie", "Movie"), WirSchauenPlugin.mLocalizer.msg("Category.Series", "Series"), WirSchauenPlugin.mLocalizer.msg("Category.Other", "Other")});
    contentPane.add(categoryDropdown, cellConstraints.xy(3, 5));

    //dropdown genre
    contentPane.add(DialogUtil.createBoldLabel(WirSchauenPlugin.mLocalizer.msg("PropertyLabels.Genre", "Genre")), cellConstraints.xy(1, 7));
    final JComboBox genreComboBox = DialogUtil.createEditableDropdown(loadGenres());
    contentPane.add(genreComboBox, cellConstraints.xy(3, 7));

    //description/abstract
    contentPane.add(DialogUtil.createBoldLabel(WirSchauenPlugin.mLocalizer.msg("PropertyLabels.Description", "Description")), cellConstraints.xy(1, 9, CellConstraints.DEFAULT, CellConstraints.TOP));
    final DescriptionInputField descriptionInputField = new DescriptionInputField(CreateWirSchauenDataDialog.MAX_CHARS_IN_ABSTRACT, "", WirSchauenPlugin.mLocalizer.msg("RemainingChars", "%s characters remaining"));
    contentPane.add(descriptionInputField, cellConstraints.xy(3, 9));

    // format information
    contentPane.add(DialogUtil.createBoldLabel(WirSchauenPlugin.mLocalizer.msg("PropertyLabels.Format", "Format")), cellConstraints.xy(1, 11));
    final JCheckBox subtitleCheckBox = new JCheckBox(WirSchauenPlugin.mLocalizer.msg("PropertyLabels.Subtitles", "Subtitles"));
    final JCheckBox owsCheckBox = new JCheckBox(WirSchauenPlugin.mLocalizer.msg("PropertyLabels.OwS", "Original with subtitles"));
    final JCheckBox premiereCheckBox = new JCheckBox(WirSchauenPlugin.mLocalizer.msg("PropertyLabels.Premiere", "TV Premiere"));
    contentPane.add(subtitleCheckBox, cellConstraints.xy(3, 11));
    contentPane.add(owsCheckBox, cellConstraints.xy(3, 12));
    contentPane.add(premiereCheckBox, cellConstraints.xy(3, 13));

    //buttons (ok, cancel)
    JButton okButton = new JButton(Localizer.getLocalization(Localizer.I18N_OK));
    okButton.setEnabled(false);
    //the ok-button is controlled by the abstract-input (i.e. deactivated if no input) and by the
    //category-dropdown (i.e. deactivated if no category selected)
    DocumentAndItemButtonController buttonController = new DocumentAndItemButtonController(okButton);
    categoryDropdown.addItemListener(buttonController);
    descriptionInputField.addDocumentListener(buttonController);
    okButton.addActionListener(new ActionListener()
    {
      public void actionPerformed(final ActionEvent e)
      {
        //provide the input and close the dialog
        setVisible(false);
        mWirSchauenInput = new WirSchauenEvent();
        if (categoryDropdown.getSelectedIndex() == 1)
        {
          mWirSchauenInput.setCategory(WirSchauenEvent.CATEGORY_MOVIE);
        }
        else if (categoryDropdown.getSelectedIndex() == 2)
        {
          mWirSchauenInput.setCategory(WirSchauenEvent.CATEGORY_SERIES);
        }
        else
        {
          mWirSchauenInput.setCategory(WirSchauenEvent.CATEGORY_OTHER);
        }
        mWirSchauenInput.setDesc(descriptionInputField.getText());
        mWirSchauenInput.setGenre((String) genreComboBox.getSelectedItem());
        mWirSchauenInput.setOmu(owsCheckBox.isSelected());
        mWirSchauenInput.setPremiere(premiereCheckBox.isSelected());
        mWirSchauenInput.setSubtitles(subtitleCheckBox.isSelected());
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

    contentPane.add(builder.getPanel(), cellConstraints.xyw(1, 15, 3));

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
    mCancelled = true;
    setVisible(false);
    dispose();
  }



  /**
   * @return true, if one of the cancel methods have been used (esc key, cancel button, window control)
   */
  public boolean isCancelled()
  {
    return mCancelled;
  }



  /**
   * the used properties of the WirSchauenEvent are:<br>
   * <ul>
   * <li>Category</li>
   * <li>Desc</li>
   * <li>Genre</li>
   * <li>Omu</li>
   * <li>Premiere</li>
   * <li>Subtitles</li>
   * </ul>
   *
   * @return the wirSchauenInput (user input)
   */
  public WirSchauenEvent getWirSchauenInput()
  {
    return mWirSchauenInput;
  }


  /**
   * @return the localized and sorted default genres for the genre dropdown
   */
  private static String[] loadGenres()
  {
    String[] genres = new String[10];
    genres[0] = WirSchauenPlugin.mLocalizer.msg("Genre.Action", "Action");
    genres[1] = WirSchauenPlugin.mLocalizer.msg("Genre.Adventure", "Adventure");
    genres[2] = WirSchauenPlugin.mLocalizer.msg("Genre.Animation", "Animation");
    genres[3] = WirSchauenPlugin.mLocalizer.msg("Genre.Comedy", "Comedy");
    genres[4] = WirSchauenPlugin.mLocalizer.msg("Genre.Documentation", "Documentation");
    genres[5] = WirSchauenPlugin.mLocalizer.msg("Genre.Drama", "Drama");
    genres[6] = WirSchauenPlugin.mLocalizer.msg("Genre.Series", "Series");
    genres[7] = WirSchauenPlugin.mLocalizer.msg("Genre.Thriller", "Thriller");
    genres[8] = WirSchauenPlugin.mLocalizer.msg("Genre.Crime", "Crime");
    genres[9] = WirSchauenPlugin.mLocalizer.msg("Genre.Movie", "Movie");
    Arrays.sort(genres);
    return genres;
  }
}
