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
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import util.ui.Localizer;
import util.ui.UiUtilities;
import util.ui.WindowClosingIf;

import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import devplugin.Plugin;

/**
 * This Class checks a Param-String and shows the Result using a example Program
 * 
 * @author bodum
 */
public class ParamCheckDialog extends JDialog implements WindowClosingIf {
  /** Translator */
  private static final Localizer mLocalizer = Localizer.getLocalizerFor(ParamCheckDialog.class);
  /** Use this ParamLib */
	private ParamLibrary mParamLib;
  /** The String to check */
	private String mCheckString;

  /**
   * Create the Dialog with a default ParamLibrary
   * 
   * @param parent
   *          Parent window
   * @param check
   *          Check this String
   * @since 3.0
   */
  public ParamCheckDialog(Window parent, String check) {
    super(parent);
    setModal(true);
    mCheckString = check;
    mParamLib = new ParamLibrary();
    createGui();
    setLocationRelativeTo(parent);
  }

  /**
   * Create the Dialog with a default ParamLibrary
   * 
   * @param dialog
   *          Parent
   * @param check
   *          Check this String
   * @deprecated since 3.0
   */
	@Deprecated
  public ParamCheckDialog(JDialog dialog, String check) {
	  this((Window) dialog, check);
	}

  /**
   * Create the Dialog with a specific ParamLibrary
   * 
   * @param parent
   *          Parent window
   * @param lib
   *          ParamLibrary to use
   * @param check
   *          Check this String
   * @since 3.0
   */
  public ParamCheckDialog(Window parent, ParamLibrary lib, String check) {
    super(parent);
    setModal(true);
    mParamLib = lib;
    mCheckString = check;
    createGui();
    setLocationRelativeTo(parent);
  }

  /**
   * Create the Dialog with a specific ParamLibrary
   * 
   * @param dialog
   *          Parent
   * @param lib
   *          ParamLibrary to use
   * @param check
   *          Check this String
   * @deprecated since 3.0
   */
  @Deprecated
  public ParamCheckDialog(JDialog dialog, ParamLibrary lib, String check) {
    this((Window) dialog, lib, check);
	}

  /**
   * Create the Dialog with a default ParamLibrary
   * 
   * @param frame
   *          Parent
   * @param check
   *          Check this String
   * @deprecated since 3.0
   */
	@Deprecated
  public ParamCheckDialog(JFrame frame, String check) {
	  this((Window) frame, check);
	}

  /**
   * Create the Dialog with a specific ParamLibrary
   * 
   * @param frame
   *          Parent
   * @param lib
   *          ParamLibrary to use
   * @param check
   *          Check this String
   * @deprecated since 3.0
   */
	@Deprecated
  public ParamCheckDialog(JFrame frame, ParamLibrary lib, String check) {
	  this((Window) frame, lib, check);
	}
	
  /**
   * Creates the GUI
   */
	private void createGui() {
		setTitle(mLocalizer.msg("Result", "Result"));
		JPanel panel = (JPanel)getContentPane();

    UiUtilities.registerForClosing(this);
    
		CellConstraints cc = new CellConstraints();
		FormLayout layout = new FormLayout("fill:default:grow, 3dlu, default", "default, 3dlu, fill:default:grow, 3dlu, default");
		panel.setLayout(layout);
		panel.setBorder(Borders.DLU4_BORDER);
		
    panel.add(new JLabel(mLocalizer.msg("Result", "Result") + ":" ), cc.xyw(1,1,3));
    
		JTextArea area = new JTextArea();
		area.setWrapStyleWord(true);
		area.setEditable(false);
		 
    area.setText(analyseString());
    
    final JScrollPane spane = new JScrollPane(area);
		panel.add(spane, cc.xyw(1,3, 3));
		
    SwingUtilities.invokeLater(new Runnable() {

      public void run() {
        spane.getVerticalScrollBar().setValue(0);
      }

    });
    
		JButton ok = new JButton(Localizer.getLocalization(Localizer.I18N_OK));
		
		ok.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {
        setVisible(false);
			}
			
		});
		
		getRootPane().setDefaultButton(ok);
		
		panel.add(ok, cc.xy(3,5));
		
		setSize(500, 400);
	}

  /**
   * Analyse the String and returns a Result or the Error-Description
   * @return Result or Error-Description
   */
  private String analyseString() {
    ParamParser parse = new ParamParser(mParamLib);
    String ret = parse.analyse(mCheckString, Plugin.getPluginManager().getExampleProgram());
    
    if (ret == null) {
      return "Error:\n" + parse.getErrorString();
    }

    return ret;
  }

  public void close() {
    setVisible(false);
  }
  
}