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

public class IntProperty extends BaseProperty<Integer>{
	Properties props;
	public IntProperty(Properties props,String key,int defaultval){
		super(key,defaultval);
		this.props=props;
	}
	@Override
	public Integer get() {
		try{
			Object o=props.get(key);
			if (o==null)return defaultvalue;
			return Integer.parseInt((String)o);
		}catch (Exception e){
			return defaultvalue;
		}
	}

	@Override
	public void set(Integer data) {
		try{
			props.put(key, Integer.toString(data));
			fireChanged();
		}catch (Exception e){
			foundABug(e);
		}
		
	}

}
