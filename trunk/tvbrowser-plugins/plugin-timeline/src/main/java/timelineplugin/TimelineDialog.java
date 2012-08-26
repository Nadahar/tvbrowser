/*
 * Timeline by Reinhard Lehrbaum
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
package timelineplugin;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JRootPane;

import util.ui.Localizer;
import util.ui.UiUtilities;
import util.ui.WindowClosingIf;

import devplugin.Plugin;

public final class TimelineDialog extends JDialog implements WindowClosingIf {
	private static final long serialVersionUID = 1L;

	static final util.ui.Localizer mLocalizer = util.ui.Localizer
			.getLocalizerFor(TimelinePlugin.class);
	
	private TimelinePanel mTimelinePanel;
	
	public TimelineDialog(final Frame frame, final boolean startWithNow) {
		super(frame, true);
		mTimelinePanel =  new TimelinePanel(startWithNow);
		setTitle(mLocalizer.msg("name", "Timeline"));
		
		UiUtilities.registerForClosing(this);
		
		createGUI();
	}
	
  private void createGUI() {
    final JPanel content = (JPanel) this.getContentPane();
    content.setLayout(new BorderLayout());
    content.setBorder(UiUtilities.DIALOG_BORDER);
    
    content.add(mTimelinePanel, BorderLayout.CENTER);
    content.add(getFooterPanel(), BorderLayout.SOUTH);

    addKeyboardAction(rootPane);
  }


  private JPanel getFooterPanel() {
    final JPanel fp = new JPanel(new BorderLayout());
    fp.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));

    final JButton settingsBtn = new JButton(TimelinePlugin.getInstance()
        .createImageIcon("categories", "preferences-system", 16));
    settingsBtn.setToolTipText(mLocalizer.msg("settings", "Open settings"));
    settingsBtn.addActionListener(new ActionListener() {
      public void actionPerformed(final ActionEvent e) {
        close();
        Plugin.getPluginManager().showSettings(TimelinePlugin.getInstance());
      }
    });
    fp.add(settingsBtn, BorderLayout.WEST);

    final JButton closeBtn = new JButton(
        Localizer.getLocalization(Localizer.I18N_CLOSE));
    closeBtn.addActionListener(new ActionListener() {
      public void actionPerformed(final ActionEvent evt) {
        dispose();
      }
    });
    fp.add(closeBtn, BorderLayout.EAST);
    rootPane.setDefaultButton(closeBtn);

    return fp;
  }
  

  public void addKeyboardAction(JRootPane rootPane) {
    mTimelinePanel.addKeyboardAction(rootPane);
    this.setRootPane(rootPane);
  }

  
  public void resize() {
    mTimelinePanel.resize();
  }
  
  public void close() {
    dispose();
  }
}
