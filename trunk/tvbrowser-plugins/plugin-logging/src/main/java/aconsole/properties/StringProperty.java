package aconsole.properties;
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
import java.util.Properties;

public class StringProperty extends BaseProperty<String>{
	Properties props;
	public StringProperty(Properties props,String key,String defaultval){
		super(key,defaultval);
		this.props=props;
	}
	@Override
	public String get() {
		try{
			String result=(String)props.get(key);
			if (result==null) return defaultvalue;
			return result;
		}catch (Exception e){
			return defaultvalue;
		}
	}

	@Override
	public void set(String data) {
		try{
			props.put(key, data);
			fireChanged();
		}catch (Exception e){
			foundABug(e);
		}
		
	}

}