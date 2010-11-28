package imdbplugin;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

import util.ui.Localizer;
import util.ui.UiUtilities;
import util.ui.WindowClosingIf;

import com.jgoodies.forms.builder.ButtonBarBuilder2;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

import devplugin.Program;

public class ImdbRatingsDialog extends JDialog implements WindowClosingIf {
  /**
   * Translator
   */
  private static final Localizer mLocalizer = Localizer.getLocalizerFor(ImdbRatingsDialog.class);

  private Program mProgram;

  public ImdbRatingsDialog(final JDialog parent, final Program program) {
    super(parent);
    initialize(program);
  }

	public ImdbRatingsDialog(final JFrame parent, final Program program) {
    super(parent);
    initialize(program);
	}

	private void initialize(final Program prog) {
		setModal(true);
    mProgram = prog;
    createGui();
    UiUtilities.registerForClosing(this);
	}

  private void createGui() {
    setTitle(mLocalizer.msg("title", "IMDb Rating for {0}", mProgram.getTitle()));

    CellConstraints cc = new CellConstraints();
    final FormLayout layout = new FormLayout("fill:min:grow");
    final PanelBuilder panel = new PanelBuilder(layout, (JPanel) getContentPane());

    panel.setBorder(Borders.DLU4_BORDER);

    ImdbRating rating = ImdbPlugin.getInstance().getProgramRating(mProgram);
    ImdbRating episodeRating = ImdbPlugin.getInstance().getEpisodeRating(mProgram);
    JComponent mainComponent;
		if (episodeRating != null) {
    	JTabbedPane pane = new JTabbedPane();
    	JComponent editor = createEditor(rating);
			pane.addTab(mLocalizer.msg("rating", "Rating"), editor);
			editor = createEditor(episodeRating);
    	pane.addTab(mLocalizer.msg("episodeRating", "Rating of Episode"), editor);
    	mainComponent = pane;
    }
		else {
			mainComponent = createEditor(rating);
		}

    layout.appendRow(RowSpec.decode("fill:min:grow"));
    panel.add(mainComponent, cc.xy(1,panel.getRowCount()));

    layout.appendRow(RowSpec.decode("3dlu"));
    layout.appendRow(RowSpec.decode("pref"));

    JButton okButton = new JButton(Localizer.getLocalization(Localizer.I18N_OK));
    okButton.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        close();
      }
    });

    ButtonBarBuilder2 buttonBuilder = new ButtonBarBuilder2();
    buttonBuilder.addGlue();
    buttonBuilder.addButton(new JButton[] {okButton});

    panel.add(buttonBuilder.getPanel(), cc.xy(1,panel.getRowCount()));

    getRootPane().setDefaultButton(okButton);
    pack();
    UiUtilities.setSize(this, 500, 450);
    okButton.requestFocusInWindow();
  }

	private JComponent createEditor(final ImdbRating rating) {
    return new JScrollPane(new ImdbRatingPanel(ImdbPlugin.getInstance().getDatabase().getMovieForId(rating.getMovieId()), rating));
	}

  public void close() {
    setVisible(false);
  }
}