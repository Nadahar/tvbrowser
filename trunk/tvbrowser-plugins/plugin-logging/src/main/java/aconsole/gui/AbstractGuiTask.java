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

import javax.swing.SwingUtilities;

import aconsole.AConsole;


/**
 * tiny wrapper to invoke runnables in GUI thread.
 * -override runGui with the code to run in GUI thread
 * -implement constructor to check preconditions and call invoke()
 * @author Tomas
 *
 */
public abstract class AbstractGuiTask implements Runnable{
	public void invoke(){
		invokeLater();
//	seems to cause a lot of trouble:
//		try{
//			if (!SwingUtilities.isEventDispatchThread()){
//				SwingUtilities.invokeLater(this);
//			}else{
//				System.out.println("skip invokeLater and run "+this.getClass());
//				this.run();
//			}
//		}catch (Exception e){
//			e.printStackTrace();
//		}
	}
	public void invokeLater(){
		try{
			SwingUtilities.invokeLater(this);
		}catch (Exception e){
			AConsole.foundABug(e);
		}
	}
	abstract public void runGui();
	final public void run(){
		try{
			runGui();
		}catch (Exception e){
			AConsole.foundABug(e);
		}
	}
}