/*
 * TV-Pearl by Reinhard Lehrbaum
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package tvpearlplugin;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.text.DateFormat;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import util.browserlauncher.Launch;
import util.ui.Localizer;
import util.ui.UiUtilities;
import util.ui.WindowClosingIf;
import util.ui.html.ExtendedHTMLDocument;
import util.ui.html.ExtendedHTMLEditorKit;
import util.ui.html.HorizontalLine;
import devplugin.Plugin;

public final class PearlInfoDialog extends JDialog implements WindowClosingIf
{
	static final util.ui.Localizer mLocalizer = util.ui.Localizer
      .getLocalizerFor(PearlInfoDialog.class);

	private static final long serialVersionUID = 1L;

	private JScrollPane mScrollPane;
	private JEditorPane mInfoPane;
	private JButton mCloseBn;
	transient private TVPProgram mProgram;

	public PearlInfoDialog(final Frame parent, final TVPProgram program)
	{
		super(parent, true);

		mProgram = program;
		createGUI();
	}

	public PearlInfoDialog(final Dialog parent, final TVPProgram program)
	{
		super(parent, true);

		mProgram = program;
		createGUI();
	}

	private void createGUI()
	{
		setTitle(mLocalizer.msg("title", "TV Pearl Info"));
		UiUtilities.registerForClosing(this);

		final JPanel main = new JPanel(new BorderLayout());
		main.setBorder(UiUtilities.DIALOG_BORDER);
		main.setPreferredSize(new Dimension(500, 350));
		setContentPane(main);

		mInfoPane = new JEditorPane();
		mInfoPane.setEditorKit(new ExtendedHTMLEditorKit());
		mInfoPane.setEditable(false);
		mInfoPane.addHyperlinkListener(new HyperlinkListener()
		{
			public void hyperlinkUpdate(final HyperlinkEvent evt)
			{
				if (evt.getEventType() == HyperlinkEvent.EventType.ACTIVATED)
				{
				  final URL url = evt.getURL();
					if (url != null)
					{
						Launch.openURL(url.toString());
					}
				}
			}
		});
		mScrollPane = new JScrollPane(mInfoPane);
		main.add(mScrollPane, BorderLayout.CENTER);

		final JPanel buttonPn = new JPanel(new FlowLayout(FlowLayout.TRAILING));
		main.add(buttonPn, BorderLayout.SOUTH);

		if (mProgram != null)
		{
		  final JButton GotoBn = new JButton(mLocalizer.msg("goto", "Goto"));
			GotoBn.setVisible(mProgram.getProgramID().length() != 0);

			GotoBn.addActionListener(new ActionListener()
			{
				public void actionPerformed(final ActionEvent evt)
				{
					Plugin.getPluginManager().scrollToProgram(mProgram.getProgram());
				}
			});
			buttonPn.add(GotoBn);
		}

		mCloseBn = new JButton(Localizer.getLocalization(Localizer.I18N_CLOSE));
		mCloseBn.addActionListener(new ActionListener()
		{
			public void actionPerformed(final ActionEvent evt)
			{
				dispose();
			}
		});
		buttonPn.add(mCloseBn);
		getRootPane().setDefaultButton(mCloseBn);

		pack();

		if (mProgram != null)
		{
		  final ExtendedHTMLDocument doc = (ExtendedHTMLDocument) mInfoPane
          .getDocument();
			mInfoPane.setText(createHtmlText(doc, mProgram));
			mScrollPane.getVerticalScrollBar().setValue(0);
		}
		toFront();
	}

	public void close()
	{
		dispose();
	}

	private String createHtmlText(final ExtendedHTMLDocument doc,
      final TVPProgram program)
	{
	  final Font tFont = new Font("Verdana", Font.BOLD, 18);
    final Font bFont = new Font("Verdana", Font.PLAIN, 11);
    final String titleFont = tFont.getFamily();
    final String titleSize = bFont.getFamily();
    final String bodyFont = String.valueOf(tFont.getSize());
    final String bodyFontSize = String.valueOf(bFont.getSize());

	  final StringBuffer buffer = new StringBuffer(1024);
		buffer.append("<html><body>");
		buffer.append("<table width=\"100%\" style=\"font-family:");
		buffer.append(bodyFont);
		buffer.append(";\"><tr>");
		buffer.append("<td width=\"60\">");
		buffer.append("<p \"align=center\">");
		buffer.append("</p></td><td>");
		buffer.append("<div style=\"color:#ff0000; font-size:");
		buffer.append(bodyFontSize).append(";\"><b>");
		buffer.append(TVPearlPlugin.getDayName(program.getStart(), false)).append(
        ", ");
		buffer.append(DateFormat.getDateInstance().format(program.getStart().getTime()));
		buffer.append(" · ");
		buffer.append(DateFormat.getTimeInstance(DateFormat.SHORT).format(program.getStart().getTime()));
		buffer.append(" · ");
		buffer.append(program.getChannel());
		buffer.append("</b></div><div style=\"color:#003366; font-size:");
		buffer.append(titleSize);
		buffer.append("; line-height:2.5em; font-family:");
		buffer.append(titleFont).append("\"><b>");
		buffer.append(program.getTitle());
		buffer.append("</b></div>");
		buffer.append("</td></tr>");

		addSeparator(doc, buffer);

		buffer.append("<tr><td valign=\"top\" style=\"color:#808080; font-size:");
		buffer.append(bodyFontSize).append("\"><b>");
		buffer.append(mLocalizer.msg("author", "Author"));
		buffer.append("</b></td><td valign=\"middle\" style=\"font-size:font-size:");
		buffer.append(bodyFontSize).append("\">");
		buffer.append(program.getAuthor());
		buffer.append("</td></tr>");

		addSeparator(doc, buffer);

		buffer.append("<tr><td valign=\"top\" style=\"color:#808080; font-size:");
		buffer.append(bodyFontSize).append("\"><b>");
		buffer.append(mLocalizer.msg("info", "Info"));
		buffer.append("</b></td><td valign=\"middle\" style=\"font-size:font-size:");
		buffer.append(bodyFontSize).append("\">");
		buffer.append(program.getInfo().replaceAll("\n", "<br>"));
		buffer.append("</td></tr>");

		addSeparator(doc, buffer);

		buffer.append("<tr><td colspan=\"2\" valign=\"top\" align=\"center\" style=\"color:#808080; font-size:");
		buffer.append(bodyFontSize).append("\">");
		buffer.append("<a href=\"");
		buffer.append(program.getContentUrl()).append("\">");
		buffer.append(mLocalizer.msg("forum", "Forum entry"));
		buffer.append("</a>");
		buffer.append("</td></tr>");

		buffer.append("</table>");

		buffer.append("</body></html>");

		return buffer.toString();
	}

	private void addSeparator(final ExtendedHTMLDocument doc,
      final StringBuffer buffer)
	{
		buffer.append("<tr><td colspan=\"2\">");
		buffer.append("<div style=\"font-size:0;\">").append(doc.createCompTag(new HorizontalLine())).append("</div></td></tr>");
	}
}
