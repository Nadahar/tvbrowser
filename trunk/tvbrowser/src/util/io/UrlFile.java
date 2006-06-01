/*
 *
 * $Id: UrlFile.java 39 2006-04-15 14:44:16Z arnep $
 *
 */
package util.io;

import java.io.*;
import java.net.*;

/**
 * <p>
 * UrlFile is able to handle Windows Shortcut files (*.url).
 * It can be used to create or read <i>url</i> files which are used by 
 * Microsoft for Shortcuts and Favorites.
 * </p>  
 *
 * <p>
 * see <a href="http://www.cyanwerks.com/file-format-url.html">windows shortcut url file format</a>
 * for details of the file format.* 
 * </p>
 * 
 * <p>
 * <pre>
 *  UrlFile sf = new UrlFile( 
 *		new File( 
 *			Win32.getSpecialDirectory( Win32.SPECIALDIRECTORY_PERSONAL_DESKTOP)
 *			, "Go to ROXES Technologies.url"
 *		)
 *	); 
 *	sf.setUrl( new URL( "http://www.roxes.com"));
 *	sf.setIconFile( "C:\\winnt\\explorer.exe");
 *	sf.setIconIndex( 1);
 *
 *	    // cerate desktop shortcut
 *	sf.save();
 *	
 *		// dump created shortcut to console
 *	System.out.println( sf);
 *	
 *		// create favorite
 *		// save shortcut in internet explorer favorites directory 
 *	sf.save( new File( Win32.getSpecialDirectory( Win32.SPECIALDIRECTORY_PERSONAL_FAVORITES), "Go to ROXES Technologies.url"));
 * </pre>
 * </p> 
 * 
 * @version 	1.0
 * @since 		03.11.2003
 * @author		lars.gersmann@roxes.com
 *
 * (c) Copyright 2003 ROXES Technologies (www.roxes.com).
 * 
 * 
 * Changed Win32Exception to IOException for TV-Browser.
 */
public class UrlFile
{
	public static final String SIGNATURE = "[InternetShortcut]";
	public static final String LINE_BREAK = "\r\n";
	
	public static final int SHOWCOMMAND_NORMAL = -1,
							SHOWCOMMAND_MAXIMIZED = 3,
							SHOWCOMMAND_MINIMIZED = 7;
	
	String workingDirectory = null;
	String iconFile = null;
	int iconIndex = -1;
	URL url = null;
	int showCommand = -1;
	int hotKey = -1;
	String modified = null;
	
	File file = null;
	
	public UrlFile( File file) throws IOException
	{
		this.file = file;
		
		if( !file.exists() || (file.length() == 0))
			return;
			
		LineNumberReader reader = new LineNumberReader( new FileReader( file));
		
		String line = reader.readLine();
		if( !line.equals( SIGNATURE))
			throw new IOException( "Shortcut Signature \"" + SIGNATURE + "\"" + " not found in file " + file.getAbsolutePath());
		
		while( (line = reader.readLine())!=null)
		{
			int i = line.indexOf( '=');
			if( i==-1)
				throw new IOException( "Invalid Shortcut file format (line " + reader.getLineNumber() + "): \"=\" expected in \"" + line + "\"");
			
			String key = line.substring( 0, i);
			String value = line.substring( i+1);
			
			if( key.equals( "URL"))
			{
				url = new URL( value);
			}
			else if( key.equals( "WorkingDirectory"))
			{
				workingDirectory = value;
			}
			else if( key.equals( "IconFile"))
			{
				iconFile = value;
			}
			else if( key.equals( "IconIndex"))
			{
				iconIndex = Integer.parseInt( value);
			}
			else if( key.equals( "ShowCommand"))
			{
				showCommand = Integer.parseInt( value);
			}
			else if( key.equals( "Modified"))
			{
				modified = value;
			}
			else if( key.equals( "HotKey"))
			{
				hotKey = Integer.parseInt( value);
			}
			else
				throw new IOException( "Invalid Shortcut file format (line " + reader.getLineNumber() + "): dont know key " + key);

		}
		
		reader.close();
	}

	public String toString()
	{
		StringBuffer sb = new StringBuffer();
		sb.append( SIGNATURE).append( LINE_BREAK);
		
		if( url!=null)
			sb.append("URL=").append( url).append( LINE_BREAK);

		if( workingDirectory!=null)
			sb.append("WorkingDirectory=").append( workingDirectory).append( LINE_BREAK);
			
		if( iconFile!=null)
			sb.append("IconFile=").append( iconFile).append( LINE_BREAK);
		
		if( iconIndex!=-1)	
			sb.append("IconIndex=").append( iconIndex).append( LINE_BREAK);
			
		if( modified!=null)
			sb.append("Modified=").append( modified).append( LINE_BREAK);		
		
		if( hotKey!=-1)
			sb.append("HotKey=").append( hotKey).append( LINE_BREAK);
			
		return sb.toString();
	}

	/**
	 * @return iconIndex
	 */
	public int getIconIndex()
	{
		return iconIndex;
	}

	/**
	 * @return modified
	 */
	public String getModified()
	{
		return modified;
	}

	/**
	 * @return showCommand
	 * (see the SHOWCOMMAND_* constants)
	 */
	public int getShowCommand()
	{
		return showCommand;
	}

	/**
	 * @return url to open
	 */
	public URL getUrl()
	{
		return url;
	}

	/**
	 * @return working directory
	 */
	public String getWorkingDirectory()
	{
		return workingDirectory;
	}

	/**
	 * @param iconIndex (starts with 0)
	 */
	public void setIconIndex(int iconIndex)
	{
		this.iconIndex = iconIndex;
	}

	/**
	 * @param string modification date
	 */
	public void setModified(String string)
	{
		modified = string;
	}

	/**
	 * @param i the show command to use
	 * (see the SHOWCOMMAND_* constants)
	 */
	public void setShowCommand(int i)
	{
		showCommand = i;
	}

	/**
	 * @param url url to open
	 */
	public void setUrl(URL url)
	{
		this.url = url;
	}

	/**
	 * @param string the working directory
	 */
	public void setWorkingDirectory(String string)
	{
		workingDirectory = string;
	}
	
	public void save() throws IOException
	{
		if( !file.getName().endsWith( ".url"))
			throw new IOException( "Shortcut/Favorite files must have prefix \".url\".");
		
		if( !file.getParentFile().exists())
			file.getParentFile().mkdirs();
						
		PrintStream out = new PrintStream( new FileOutputStream( file));
		out.print( toString());
		out.close();
	}
	
	public void save( File file) throws IOException
	{
		this.file = file;
		save();
	}
	
	/**
	 * @return the hotkey
	 */
	public int getHotKey()
	{
		return hotKey;
	}

	/**
	 * @return the icon file
	 */
	public String getIconFile()
	{
		return iconFile;
	}

	/**
	 * @param i the hotkey code
	 */
	public void setHotKey(int i)
	{
		hotKey = i;
	}

	/**
	 * @param string the icon file to use (valid file types are exe, dll and ico)
	 */
	public void setIconFile(String string)
	{
		iconFile = string;
	}
}
