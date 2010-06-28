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
package aconsole.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;

import util.ui.ImageUtilities;
import aconsole.AConsole;
import aconsole.data.Console;
import aconsole.properties.DimensionProperty;

/**
 * this is contains the JFrame for the console
 * @author Tomas
 *
 */
public class ConsoleFrame extends JFrame implements Console.Listener{
	private static final long serialVersionUID = -9092987468990448974L;
	ConsolePanel consolepanel;
	boolean init=false;
	Console console;
	DimensionProperty dimprop,posprop;
	public ConsoleFrame(){
		super();
		console=Console.getConsole();
		//init frame
		setTitle("TVBConsole");
		this.setIconImage(ImageUtilities.createImageFromJar("aconsole/log16.gif", AConsole.class));
		setResizable(true);
		setSize(640,450);
		this.getContentPane().setLayout(new BorderLayout());
		dimprop=new DimensionProperty(AConsole.getProperties(),"consolesize",-Integer.MIN_VALUE,-Integer.MIN_VALUE);
		posprop=new DimensionProperty(AConsole.getProperties(),"consolepos",-Integer.MIN_VALUE,-Integer.MIN_VALUE);
		this.addWindowListener(new WindowAdapter(){
			public void windowClosing(WindowEvent e) {
				consolepanel.shutdownConsole() ;
				if (console!=null){
					console.removeListener(ConsoleFrame.this);
					console=null;
				}
				if (ConsoleFrame.this.getExtendedState()!=NORMAL){
					ConsoleFrame.this.setExtendedState(NORMAL);
				}
				dimprop.set(getSize());
				posprop.set(ConsoleFrame.this.getLocation());
				ConsoleFrame.this.setVisible(false);
				ConsoleFrame.this.dispose();
			}
			public void windowClosed(WindowEvent e) {
			}
		});
		//init console-panel
		consolepanel=new ConsolePanel(this,console,new java.awt.Font("Monospaced", 0, 12),Color.black,Color.gray);
		this.getContentPane().add(consolepanel, BorderLayout.CENTER);
		//done
		init=true;
		if (dimprop.getWidth()>-Integer.MIN_VALUE && dimprop.getHeight()>-Integer.MIN_VALUE){
			this.setSize(dimprop.get());
		}
		if (posprop.getWidth()>-Integer.MIN_VALUE && posprop.getHeight()>-Integer.MIN_VALUE){
			this.setLocation(posprop.getPoint());
		}
		setVisible(true);
	}
	public void clear() {
		consolepanel.clear();
	}
	public boolean save(String filename){
		return consolepanel.save(filename);
	}
	/**
	 * @see tvbconsole.Console.Listener#shutdownConsole()
	 */
	public void shutdownConsole() {
		this.setVisible(false);
		this.dispose();
		this.console.removeListener(this);
		this.console=null;
	}
	/**
	 * @see tvbconsole.Console.Listener#addText(tvbconsole.Console.LoggerConsoleEvent)
	 */
	public void addText(Console.LoggerConsoleEvent ce) {
	}
}
