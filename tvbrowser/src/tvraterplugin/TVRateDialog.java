/*
 * Created on 02.12.2003
 */
package tvraterplugin;

import java.awt.BorderLayout;
import java.awt.Frame;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

import util.ui.Localizer;
import devplugin.Program;

/**
 * @author bodo
 */
public class TVRateDialog extends JDialog {

	private static final Localizer _mLocalizer = Localizer.getLocalizerFor(TVRaterPlugin.class);

	/**
	 * @param parent
	 * @param program
	 */
	public TVRateDialog(Frame parent, Program program) {
		super(parent, true);
		setTitle(_mLocalizer.msg("contextMenuText", "View Rating"));
		
		JPanel panel = (JPanel) this.getContentPane();

		panel.setLayout(new BorderLayout());
		
		panel.add(new JLabel(program.getTitle()), BorderLayout.NORTH);
		
		pack();
	}

}