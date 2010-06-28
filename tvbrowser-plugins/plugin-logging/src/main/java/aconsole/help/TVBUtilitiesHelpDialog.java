/*
 *******************************************************************
 *              TVBConsole plugin for TVBrowser                    *
 *                                                                 *
 * Copyright (C) 2010 Tomas Schackert.                             *
 * Contact koumori@web.de                                          *
 *******************************************************************

 This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, in version 3 of the License.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program, in a file called LICENSE in the top
 directory of the distribution; if not, write to
 the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 Boston, MA  02111-1307  USA

 *******************************************************************/

package aconsole.help;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

import util.ui.UiUtilities;


/**
 * A help dialog which is able to show HTML pages that are located in a jar
 * file.
 *
 * @author Til Schneider, www.murfman.de, Tomas Schackert
 */
public class TVBUtilitiesHelpDialog implements ActionListener{

	/** The localizer for this class. */
	private static final util.ui.Localizer mLocalizer = util.ui.Localizer.getLocalizerFor(TVBUtilitiesHelpDialog.class);

	/** The minimum size of the help dialog. */
	public static final Dimension MIN_HELP_DIALOG_SIZE = new Dimension(350, 400);

	/** The maximum size of the help dialog. */
	public static final Dimension MAX_HELP_DIALOG_SIZE = new Dimension(500, 10000);

	/**
	 * Dialog-Singleton: Es wird immer nur ein Dialog auf einmal angezeigt.
	 * Wenn eine neue Seite angezeigt werden soll, so passiert das im selben
	 * Dialog.
	 */
	private static TVBUtilitiesHelpDialog mDialogSingleton;

	/** The dialog. */
	private JDialog mDialog;

	private TVBUtilitiesHelpPanel panel;
	private JButton mBackButton, mCloseButton;




	/**
	 * Shows the specified site in the help dialog. If there is currently no help
	 * dialog shown, one is created.
	 *
	 * @param parent If there is currently no help dialog shown, this parent is
	 *        used for creating a new one.
	 * @param filename The name of the HTML file to show.
	 * @param clazz A class in the jar where the HTML file is in.
	 */
	public static void showHelpPage(Component parent, URL url,String helptitle) {
		showHelpDialog(parent,helptitle);
		mDialogSingleton.openSite(url);
		mDialogSingleton.show();
	}

//	private void initHelpUi(Component parent) {
//		initHelpUi(parent,null);
//	}
	private void initHelpUi(Component parent,String helpTitle) {
		mDialog = UiUtilities.createDialog(parent, false);
		if (helpTitle==null) {
      helpTitle=mLocalizer.msg("titleHelp", "Help");
    }
		mDialog.setTitle(helpTitle);

		JPanel main = new JPanel(new BorderLayout());
		mDialog.setContentPane(main);
		main.setBorder(UiUtilities.DIALOG_BORDER);

		main.add(panel=new TVBUtilitiesHelpPanel(),BorderLayout.CENTER);

		// buttons
		JPanel buttonPn = new JPanel(new FlowLayout(FlowLayout.TRAILING));
		main.add(buttonPn, BorderLayout.SOUTH);

		String msg = mLocalizer.msg("back", "Back");
		buttonPn.add(mBackButton = new JButton(msg));
		mBackButton.addActionListener(this);
		mBackButton.setEnabled(false);

		msg = mLocalizer.msg("close", "Close");
		buttonPn.add(mCloseButton = new JButton(msg));
		mCloseButton.addActionListener(this);
		mDialog.getRootPane().setDefaultButton(mCloseButton);


		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension hilfeSize = new Dimension((int)(screenSize.width*0.8),(int)(screenSize.height*0.8));
		if (hilfeSize.width < MIN_HELP_DIALOG_SIZE.width) {
			hilfeSize.width = MIN_HELP_DIALOG_SIZE.width;
		}
		if (hilfeSize.height < MIN_HELP_DIALOG_SIZE.height) {
			hilfeSize.height = MIN_HELP_DIALOG_SIZE.height;
		}
		mDialog.setSize(hilfeSize);
		mDialog.setLocation((screenSize.width-hilfeSize.width)/2,(screenSize.height-hilfeSize.height)/2);
	}



	public void show() {
		mDialog.setVisible(true);
	}


	// Hilfsmethoden

	/**
	 * L�dt eine HTML-Seite und gibt deren Inhalt als String zur�ck.
	 * <p>
	 * Die Seite kann auch in der Jar-Datei sein, in dem diese Klasse ist.
	 *
	 * @param clazz A class in the jar where the HTML site is located.
	 * @param filename Der Dateiname der HTML-Seite.
	 */
	protected void openSite(URL url) {
		panel.openSite(url);
	}
	// implements ActionListener


	public void actionPerformed(ActionEvent evt) {
		try{
			try{
				if (evt.getSource() == mBackButton) {
					panel.popFromHistory();
				}

				else if (evt.getSource() == mCloseButton) {
					mDialog.dispose();
				}
			}catch (RuntimeException re){
				re.printStackTrace();
			}
		}catch (RuntimeException re){
			re.printStackTrace();

		}
	}
	synchronized static void showHelpDialog(Component parent,String helpTitle){
		if ((mDialogSingleton== null) || (! mDialogSingleton.mDialog.isShowing())){
		  mDialogSingleton = new TVBUtilitiesHelpDialog();
			mDialogSingleton.initHelpUi(parent,helpTitle);
		}
	}
	static void showHelpDialog(Component parent){
		showHelpDialog(parent,null);
	}




}
