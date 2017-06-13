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
import java.awt.Window;

import javax.swing.JDialog;
import javax.swing.JPanel;

import util.ui.UiUtilities;
import util.ui.WindowClosingIf;

public final class PearlDialog extends JDialog implements WindowClosingIf
{
  public static final int TYPE_PEARL_DISPLAY = 0;
  public static final int TYPE_PEARL_CREATION = 1;
  
	private static final long serialVersionUID = 1L;

	static final util.ui.Localizer mLocalizer = util.ui.Localizer
      .getLocalizerFor(TVPearlPlugin.class);

	private JPanel mPanel;

	public PearlDialog(final Window w, final int type) {
	  super(w, ModalityType.MODELESS);
	  createGUI(type);
	  UiUtilities.registerForClosing(this);
	}
	
	private void createGUI(int type) {
	  setLayout(new BorderLayout());
	  
	  if(type == TYPE_PEARL_CREATION) {
	    setTitle(TVPearlPluginSettingsTab.mLocalizer.msg("tabPearlCreation", "TV Pearl Creation"));
	    mPanel = new PearlCreationJPanel(TVPearlPlugin.getInstance().getCreationTableModel(), TVPearlPlugin.getSettings());
	  }
	  else if(type == TYPE_PEARL_DISPLAY) {
	    setTitle(TVPearlPluginSettingsTab.mLocalizer.msg("tabPearlDisplay", "TV Pearl Display"));
	    mPanel = new PearlDisplayPanel(this);
	  }
	  
	  add(mPanel, BorderLayout.CENTER);
	  pack();
	}

	public void close() {
	  if(mPanel instanceof PearlDisplayPanel && ((PearlDisplayPanel) mPanel).popupCanceled()) {
	    return;
	  }
	  
		dispose();
	}

	public void updateProgramList() {
	  if(mPanel instanceof PearlDisplayPanel) {
	    ((PearlDisplayPanel)mPanel).updateProgramList();
	  }
	}
	
	public void update() {
	  if(mPanel instanceof PearlDisplayPanel) {
	    ((PearlDisplayPanel)mPanel).update();
	  }
	}
	
	@Override
	public void requestFocus() {
	  mPanel.requestFocus();
	}
}
