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
package aconsole.properties;

import java.util.Vector;

import aconsole.AConsole;
import aconsole.gui.AbstractNotifyTask;


abstract public class BaseProperty <DATA> implements Property <DATA>{
	String key;
	DATA mCachedValue,defaultvalue;
	private Vector<Listener> listeners=new Vector<Listener>();
	public BaseProperty(String key,DATA defaultvalue) {
		this.key=key;
		this.defaultvalue=defaultvalue;
		this.mCachedValue=null;
	}
	final public String getKey(){return key;}
	abstract public DATA get();
	abstract public void set(DATA data);
	final public DATA getDefault(){
		return defaultvalue;
	}
	public void foundABug(Exception e){
		AConsole.foundABug(e);
	}
	public void addListener(Listener l){
		listeners.add(l);
	}
	public void removeListener(Listener l){
		listeners.remove(l);	
	}
	protected void fireChanged(){
		new AbstractNotifyTask<Listener>(listeners){
			@Override
			public void notify(aconsole.properties.Property.Listener l) {
				l.changedProperty(BaseProperty.this);
			}
		};
	}
}
