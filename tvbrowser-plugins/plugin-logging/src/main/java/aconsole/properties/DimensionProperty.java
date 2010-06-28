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


import java.awt.Dimension;
import java.awt.Point;
import java.util.Properties;
/**
 * tmarkplugin.config.Property for a Dimension
 * @author Tomas Schackert
 *
 */
public class DimensionProperty extends BaseProperty<Dimension>{
	String key;
	int width=0,height=0;
	//int defaultwidth,defaultheight=0;
	boolean loaded=false;
	Properties props;
//	public void setFrom(Property<Dimension> p) throws ClassCastException{
//		this.setDimension(p.getValue());
//	}
	protected Property<Dimension> createBuffer(){
		return new DimensionProperty(null,this);
	}
//	public boolean isEqualProperty(Property<Dimension> p) throws ClassCastException{
//		Dimension pd=p.getValue();
//		return this.width==pd.width && this.height==pd.height;
//	}
	public DimensionProperty(Properties props,DimensionProperty src){
		super(src.key,src.get());
		this.props=props;
		set(src);
	}
	
	public DimensionProperty(Properties props,String key,Dimension defaultvalue){
		super(key,defaultvalue);
		this.props=props;
		this.key=key;
//		defaultwidth=defaultvalue.width;
//		defaultheight=defaultvalue.height;
	}
	public DimensionProperty(Properties props,String key,int defaultwidth,int defaultheight){
		super(key,new Dimension(defaultwidth,defaultheight));
		this.props=props;
		this.key=key;
//		this.defaultwidth=defaultwidth;
//		this.defaultheight=defaultheight;
	}
	private void load(){
		String src=(String)props.get(key);
		if (src==null){
			width=defaultvalue.width;
			height=defaultvalue.height;
			return;
		}
		int index=src.indexOf(",");
		if (index>=0){
			try{
				String wstr=src.substring(0,index);
				width=Integer.parseInt(wstr);
				String hstr=src.substring(index+1);
				height=Integer.parseInt(hstr);
			}catch(Exception e){
				foundABug(e);
			}
		}
		loaded=true;
	}
	public void set(DimensionProperty src){
		src.get();
		this.key=src.key;
		defaultvalue=src.defaultvalue;
		set(src.width,src.height);
	}
	public Dimension get(){
		if (!loaded) {
      load();
    }
		return new Dimension(width,height);
	}
	public int getWidth(){
		if (!loaded) {
      load();
    }
		return width;
	}
	public int getHeight(){
		if (!loaded) {
      load();
    }
		return height;
	}
	public Point getPoint(){
		if (!loaded) {
      load();
    }
		return new Point(width,height);
	}
	public void set(Dimension v){
		set(v.width,v.height);
	}
	public void set(Point point) {
		set(point.x,point.y);
	}
	public void set(int w,int h){
		width=w;
		height=h;
		props.put(key,w+","+h);
		loaded=true;
		fireChanged();
	}
	public void reset() {
		set(defaultvalue);
	}
	public boolean isDefault(){
		if (!loaded) {
      load();
    }
		return width==defaultvalue.width && height==defaultvalue.height;
	}
	public Dimension getDefaultValue(){
		return new Dimension(defaultvalue);
	}
	public void setDefaultValue(Dimension v){
		defaultvalue.width=v.width;
		defaultvalue.height=v.height;
	}
}
