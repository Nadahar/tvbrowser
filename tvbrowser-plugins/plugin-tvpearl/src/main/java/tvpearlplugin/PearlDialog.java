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
import java.awt.Cursor;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Calendar;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JToolTip;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import tvbrowser.core.icontheme.IconLoader;
import util.ui.Localizer;
import util.ui.SearchFormSettings;
import util.ui.SearchHelper;
import util.ui.SendToPluginDialog;
import util.ui.UiUtilities;
import util.ui.WindowClosingIf;
import util.ui.menu.MenuUtil;

import com.jgoodies.forms.builder.ButtonBarBuilder;

import devplugin.Plugin;
import devplugin.PluginManager;
import devplugin.Program;

public final class PearlDialog extends JDialog implements WindowClosingIf
{
	private static final long serialVersionUID = 1L;

	static final util.ui.Localizer mLocalizer = util.ui.Localizer
      .getLocalizerFor(TVPearlPlugin.class);

	private PearlPanel mPanel;

	public PearlDialog(final Dialog dialog)
	{
		super(dialog, true);

		setTitle(mLocalizer.msg("name", "TV Pearl"));
		createGUI();
		UiUtilities.registerForClosing(this);
	}

	public PearlDialog(final Frame frame)
	{
		super(frame, true);

		setTitle(mLocalizer.msg("name", "TV Pearl"));
		createGUI();
		UiUtilities.registerForClosing(this);
	}

	private void createGUI() {
	  setLayout(new BorderLayout());
	  mPanel = new PearlPanel(this);
	  add(mPanel, BorderLayout.CENTER);
	  pack();
	}

	public void close()
	{
		dispose();
	}

	public void updateProgramList() {
	  mPanel.updateProgramList();
	}
	
	public void update() {
	  mPanel.update();
	}
}
