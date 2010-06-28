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

import java.util.Iterator;
import java.util.Vector;

import aconsole.AConsole;


/**
 * util to notify listeners asynchronous from the GUI thread
 * usage (if Listener is the interface and changed the notify method):
 * 		new AbstractNotifyTask<Listener>(listener){
 *			public void notify(Listener l){
 *				l.changed(Rule.this);
 *			}
 *		};
 * 
 * @author Tomas
 * @param <LISTENER>
 */
abstract public class AbstractNotifyTask<LISTENER> extends AbstractGuiTask {
	private Vector<LISTENER> listener;
	public AbstractNotifyTask(Vector<LISTENER> listener){
		this.listener=new Vector<LISTENER>(listener);
		invokeLater();
	}
	public AbstractNotifyTask(LISTENER listener){
		this.listener=new Vector<LISTENER>();
		this.listener.add(listener);
		invokeLater();
	}
	final public void runGui(){
		Iterator<LISTENER> it=listener.iterator();
		while (it.hasNext()){
			try{
				notify(it.next());
			}catch(Exception e){
				AConsole.foundABug(e);
			}
		}
	}
	abstract public void notify(LISTENER l);
	

}
