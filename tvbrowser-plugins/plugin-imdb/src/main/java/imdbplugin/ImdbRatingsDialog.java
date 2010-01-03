package imdbplugin;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Paint;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.general.DatasetUtilities;

import util.ui.Localizer;
import util.ui.UiUtilities;
import util.ui.WindowClosingIf;
import util.ui.html.ExtendedHTMLDocument;
import util.ui.html.ExtendedHTMLEditorKit;

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
      editor.setBorder(BorderFactory.createEmptyBorder());
			pane.addTab(mLocalizer.msg("rating", "Rating"), editor);
			editor = createEditor(episodeRating);
      editor.setBorder(BorderFactory.createEmptyBorder());
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
  }

	private JComponent createEditor(final ImdbRating rating) {
		JEditorPane editor = new JEditorPane();
    editor.setEditorKit(new ExtendedHTMLEditorKit());
    editor.setText(createText((ExtendedHTMLDocument)editor.getDocument(), rating));
    editor.setEditable(false);
    editor.setBackground(Color.WHITE);
    editor.setOpaque(true);
    editor.setBorder(BorderFactory.createEmptyBorder());

    final JScrollPane scroll = new JScrollPane(editor);
    // Scroll to the beginning
    Runnable runnable = new Runnable() {
      public void run() {
        scroll.getVerticalScrollBar().setValue(0);
      }
    };
    SwingUtilities.invokeLater(runnable);
		return scroll;
	}

  private String createText(final ExtendedHTMLDocument doc, final ImdbRating rating) {
    StringBuilder builder = new StringBuilder();
    builder.append("<p style='margin-left:10px'>");
    builder.append(makeTextForRating(doc, rating));
    builder.append("</p>");
    return builder.toString();
  }

  private String makeTextForRating(final ExtendedHTMLDocument doc, final ImdbRating rating) {
    final StringBuilder builder = new StringBuilder();
    final ImdbMovie movie = ImdbPlugin.getInstance().getDatabase().getMovieForId(rating.getMovieId());

    builder.append("<b>").append(movie.getTitle());
    final String episode = movie.getEpisode();
		if (episode != null && episode.length() > 0) {
      builder.append(" - <i>").append(episode).append("</i>");
    }
    if (movie.getYear() > 0) {
      builder.append(" (").append(movie.getYear()).append(")");
    }
    builder.append("</b><br>");

    builder.append(mLocalizer.msg("rating", "Rating")).append(": <b>").append(rating.getRatingText()).append("</b> ").append(mLocalizer.msg("ofTen", "of 10")).append("<br>");
    builder.append(mLocalizer.msg("votes", "votes")).append(": <b>").append(rating.getVotes()).append("</b><br><br>");

    builder.append(doc.createCompTag(createChartForRating(rating))).append("<br>");

    ImdbAka[] akas = movie.getAkas();
    if (akas.length > 0) {
      builder.append("<br>").append(mLocalizer.msg("alternativeTitle", "Alternative title")).append(": ");
      if (akas.length == 1) {
        appendAkaTitle(builder, akas[0]);
      }
      else {
      	builder.append("<br><ul>");
	      for (final ImdbAka aka : akas) {
        	builder.append("<li>");
	        appendAkaTitle(builder, aka);
        	builder.append("</li>");
	      }
        builder.append("</ul>");
      }
    }

    return builder.toString();
  }

	private void appendAkaTitle(final StringBuilder builder, final ImdbAka aka) {
		builder.append(aka.getTitle());
		if (aka.getEpisode() != null && aka.getEpisode().length() > 0) {
		  builder.append(" - ").append(aka.getEpisode());
		}
		builder.append(" (").append(aka.getYear()).append(")");
	}

  private ChartPanel createChartForRating(final ImdbRating rating) {
    final JFreeChart chart = ChartFactory.createBarChart(
        null,         // chart title
        mLocalizer.msg("rating", "Rating"),                 // domain axis label
        mLocalizer.msg("votesInPercent","Votes in %"),                // range axis label
        createDataSet(rating),      // data
        PlotOrientation.VERTICAL, // orientation
        false,                       // include legend
        true,
        false
    );

    chart.setBackgroundPaint(Color.WHITE);

    final CategoryPlot plot = chart.getCategoryPlot();
    plot.setRangeAxisLocation(AxisLocation.BOTTOM_OR_LEFT);

    BarRenderer renderer = new BarRenderer(){
      @Override
      public Paint getItemPaint(int row, int column) {
        return UIManager.getColor("TextPane.selectionBackground");
      }
    };
    renderer.setDrawBarOutline(false);
    plot.setRenderer(renderer);

    final NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
    rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());

    ChartPanel chartPanel = new ChartPanel(chart);
    chartPanel.setPreferredSize(new Dimension(300, 200));
		return chartPanel;
  }

  private CategoryDataset createDataSet(final ImdbRating rating) {
    final double[][] data = new double[1][10];

    String dist = rating.getDistribution();
    for (int i=0;i<10;i++){
      char character = dist.charAt(i);
			if (character == '.') {
        data[0][i] = 0;
      } else if (character == '*') {
        data[0][i] = 100;
      } else {
        data[0][i] = (character - '0') * 10 + 5;
      }
    }

    String[] keys = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10"};

    return DatasetUtilities.createCategoryDataset(new String[]{"hallo"}, keys, data);
  }

  public void close() {
    setVisible(false);
  }
}