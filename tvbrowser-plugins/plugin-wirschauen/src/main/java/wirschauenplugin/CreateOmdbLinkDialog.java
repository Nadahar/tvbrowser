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
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

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
 * the dialog to connect the program with omdb via wirschauen, i.e. to input
 * the omdb-id for the program. after the ok-button was pressed the data can
 * be obtained via the various getters. if the dialog was cancelled (button,
 * esc, close window) isCancelled will return true.
 *
 * @author uzi
 */
@SuppressWarnings("serial")
public class CreateOmdbLinkDialog extends JDialog implements WindowClosingIf
{
  /**
   * Localizer.
   */
  private static final Localizer LOCALIZER = Localizer.getLocalizerFor(CreateOmdbLinkDialog.class);



  /**
   * true, if one of the different cancel-methods was used. every user initiated closing of
   * the dialog means that cancelled is true.
   */
  private boolean mCancelled;

  /**
   * the omdb id for the program inserted by the user.
   */
  private int mOmdbId;

  /**
   * movie or series.
   */
  private byte mCategory;





  /**
   * creates an application modal dialog to input the link to omdb an the category.
   *
   * @param parent the parent window of this dialog
   * @param program the program to link with omdb
   */
  public CreateOmdbLinkDialog(final Window parent, final Program program)
  {
    //create the window
    super(parent, LOCALIZER.msg("DialogTitle", "Linking with OMDB.org"), ModalityType.APPLICATION_MODAL);
    UiUtilities.registerForClosing(this);
    JPanel contentPane = (JPanel) getContentPane();
    contentPane.setBorder(Borders.DLU4_BORDER);
    contentPane.setLayout(new FormLayout("pref, 3dlu, pref, fill:10dlu:grow, 3dlu, pref", "pref, 3dlu, pref, 3dlu, pref, 3dlu, pref, fill:3dlu:grow, pref"));
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
    contentPane.add(new JLabel(WirSchauenPlugin.mLocalizer.msg("PropertyLabels.Title", "Title")), cellConstraints.xy(1, 1));
    contentPane.add(DialogUtil.createReadOnlySelectAllTextField(program.getTitle()), cellConstraints.xyw(3, 1, 4));
    //if there is no episode title, take the episode number
    String episodeTitle = program.getTextField(ProgramFieldType.EPISODE_TYPE);
    if (episodeTitle == null || episodeTitle.length() == 0)
    {
      episodeTitle = program.getIntFieldAsString(ProgramFieldType.EPISODE_NUMBER_TYPE);
    }
    //if any episode id (title or number) was found, display it
    if (episodeTitle != null && episodeTitle.length() > 0)
    {
      contentPane.add(new JLabel(WirSchauenPlugin.mLocalizer.msg("PropertyLabels.Episode", "Episode")), cellConstraints.xy(1, 3));
      contentPane.add(DialogUtil.createReadOnlySelectAllTextField(episodeTitle), cellConstraints.xyw(3, 3, 4));
    }

    //omdb-link-input (label + url + input-field for id + button-link to omdb)
    contentPane.add(new JLabel(WirSchauenPlugin.mLocalizer.msg("PropertyLabels.OmdbUrl", "URL")), cellConstraints.xy(1, 5));
    contentPane.add(new JLabel("http://www.omdb.org/movie/"), cellConstraints.xy(3, 5));
    final JTextField omdbIdInput = DialogUtil.createNumericInput(LOCALIZER.msg("IdTooltip", "numeric OMDB-ID of the program"));
    contentPane.add(omdbIdInput, cellConstraints.xy(4, 5));
    contentPane.add(DialogUtil.createUrlButton(generateSearchUrl(program.getTitle(), program.getTextField(ProgramFieldType.EPISODE_TYPE)), LOCALIZER.msg("FindProgram", "Find program in OMDB")), cellConstraints.xy(6, 5));

    //dropdown category (movie or series)
    contentPane.add(new JLabel(WirSchauenPlugin.mLocalizer.msg("PropertyLabels.Category", "Category")), cellConstraints.xy(1, 7));
    final JComboBox categoryDropdown = DialogUtil.createUneditableDropdown(new String[] {WirSchauenPlugin.mLocalizer.msg("Category.NotSet", "not yet set"), WirSchauenPlugin.mLocalizer.msg("Category.Movie", "Movie"), WirSchauenPlugin.mLocalizer.msg("Category.Series", "Series")});
    contentPane.add(categoryDropdown, cellConstraints.xyw(3, 7, 4));

    //buttons (ok, cancel)
    JButton okButton = new JButton(Localizer.getLocalization(Localizer.I18N_OK));
    okButton.setEnabled(false);
    //the ok-button is controlled by the link-input (i.e. deactivated if no input) and by the
    //category-dropdown (i.e. deactivated if no category selected)
    DocumentAndItemButtonController buttonController = new DocumentAndItemButtonController(okButton);
    omdbIdInput.getDocument().addDocumentListener(buttonController);
    categoryDropdown.addItemListener(buttonController);
    okButton.addActionListener(new ActionListener()
    {
      public void actionPerformed(final ActionEvent e)
      {
        //provide the input and close the dialog
        setVisible(false);
        mOmdbId = Integer.parseInt(omdbIdInput.getText());
        mCategory = categoryDropdown.getSelectedIndex() == 1 ? WirSchauenEvent.CATEGORY_MOVIE : WirSchauenEvent.CATEGORY_SERIES;
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
    contentPane.add(builder.getPanel(), cellConstraints.xyw(1, 9, 6));

    pack();

    //set the focus in the id-input. must be done _after_ pack().
    omdbIdInput.requestFocusInWindow();
  }



  /**
   * helper method to create a omdb-search-url.
   *
   * @param programName the name of the program
   * @param episodeName the name of the episode or null
   * @return the search-url
   */
  private String generateSearchUrl(final String programName, final String episodeName)
  {
    try
    {
      StringBuilder urlBuilder = new StringBuilder(OmdbConnection.SEARCH_URL);
      urlBuilder.append(URLEncoder.encode(programName, "UTF-8"));
      if (episodeName != null)
      {
        urlBuilder.append("%20").append(URLEncoder.encode(episodeName, "UTF-8"));
      }
      return urlBuilder.toString();
    }
    catch (final UnsupportedEncodingException uee)
    {
      uee.printStackTrace();
    }
    return null;
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
    setVisible(false);
    mCancelled = true;
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
   * @return the omdbId (user input)
   */
  public int getOmdbId()
  {
    return mOmdbId;
  }


  /**
   * @return WirSchauenEvent.CATEGORY_MOVIE or ~SERIES (user input)
   */
  public byte getCategory()
  {
    return mCategory;
  }
}
