/*
 * CapturePlugin by Andreas Hessel (Vidrec@gmx.de), Bodo Tasche
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
 *
 * CVS information:
 *  $RCSfile$
 *   $Source$
 *     $Date$
 *   $Author$
 * $Revision$
 */
package util.paramhandler;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;

import util.ui.Localizer;
import util.ui.UiUtilities;
import util.ui.WindowClosingIf;

import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * This Dialog shows a Help-Dialog for a ProgramLibrary.
 * It fetches all Params/Functions from the Library and shows the Descriptions in a HTML-Pane
 * 
 * @author bodum
 */
public class ParamHelpDialog extends JDialog implements WindowClosingIf {
	/** The ParamLibrary to use */
  private ParamLibrary mParamLib;

  /**
   * Creates the Help-Dialog
   * 
   * @param parent
   *          Parent window
   * @since 3.0
   */
  public ParamHelpDialog(Window parent) {
    super(parent);
    setModal(true);
    mParamLib = new ParamLibrary();
    createGui();
    setLocationRelativeTo(parent);
  }

  /**
   * Creates the Help-Dialog
   * 
   * @param dialog
   *          Parent
   * @deprecated since 3.0
   */
	public ParamHelpDialog(JDialog dialog) {
	  this((Window) dialog);
	}

  /**
   * Creates the Help-Dialog
   * 
   * @param parent
   *          Parent window
   * @param lib
   *          Library to use
   * @since 3.0
   */
  public ParamHelpDialog(Window parent, ParamLibrary lib) {
    super(parent);
    setModal(true);
    mParamLib = lib;
    createGui();
    setLocationRelativeTo(parent);
  }

  /**
   * Creates the Help-Dialog
   * 
   * @param dialog
   *          Parent
   * @param lib
   *          Library to use
   * @deprecated since 3.0
   */
	public ParamHelpDialog(JDialog dialog, ParamLibrary lib) {
	  this((Window) dialog, lib);
	}

  /**
   * Creates the Help-Dialog
   * 
   * @param frame
   *          Parent
   * @deprecated since 3.0
   */
	public ParamHelpDialog(JFrame frame) {
	  this((Window) frame);
	}

  /**
   * Creates the Help-Dialog
   * 
   * @param frame
   *          Parent
   * @param lib
   *          Library to use
   * @deprecated since 3.0
   */
	public ParamHelpDialog(JFrame frame, ParamLibrary lib) {
	  this((Window) frame, lib);
	}

  /**
   * Creates the GUI
   */
	private void createGui() {
		setTitle(Localizer.getLocalization(Localizer.I18N_HELP));
		JPanel panel = (JPanel)getContentPane();
    
    UiUtilities.registerForClosing(this);

		CellConstraints cc = new CellConstraints();
		FormLayout layout = new FormLayout("fill:default:grow, 3dlu, default", "fill:default:grow, 3dlu, default");
		panel.setLayout(layout);
		panel.setBorder(Borders.DLU4_BORDER);
		
		panel.add(new ParamDescriptionPanel(mParamLib), cc.xyw(1,1, 3));
		
		JButton ok = new JButton(Localizer.getLocalization(Localizer.I18N_OK));
		
		ok.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {
        setVisible(false);
			}
			
		});
		
		getRootPane().setDefaultButton(ok);
		
		panel.add(ok, cc.xy(3,3));
		
		setSize(500, 400);
	}

  public void close() {
    setVisible(false);
  }

}