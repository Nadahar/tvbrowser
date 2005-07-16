/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (martin@tvbrowser.org)
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

package printplugin.settings;

import java.awt.Color;
import java.awt.Font;

import devplugin.ProgramFieldType;


public class PrinterProgramIconSettings implements ProgramIconSettings {
  
  private static final Font PROGRAMTITLEFONT=new Font("Dialog",Font.BOLD,12);
  private static final Font PROGRAMTEXTFONT=new Font("Dialog",Font.PLAIN,10);
  private static final Font PROGRAMTIMEFONT=new Font("Dialog",Font.BOLD,12);
  
  private static final Color COLOR_ON_AIR_DARK  = new Color(128, 128, 255, 80);
  private static final Color COLOR_ON_AIR_LIGHT = new Color(128, 128, 255, 40);
  private static final Color COLOR_MARKED       = new Color(255, 0, 0, 40);

  private ProgramFieldType[] mProgramInfoFields;
  private boolean mShowPluginMark;
  
  protected PrinterProgramIconSettings() {
    mProgramInfoFields = new ProgramFieldType[]{
      ProgramFieldType.SHORT_DESCRIPTION_TYPE,
      ProgramFieldType.ACTOR_LIST_TYPE,
      ProgramFieldType.DESCRIPTION_TYPE
    };
    
    mShowPluginMark=false;
  }
  
  public static ProgramIconSettings create(ProgramFieldType[] programInfoFields, boolean showPluginMark) {
    PrinterProgramIconSettings settings = new PrinterProgramIconSettings();
    settings.mProgramInfoFields = programInfoFields;
    settings.mShowPluginMark = showPluginMark;
    return settings;
    
  }
  
  public static ProgramIconSettings create() {
    return new PrinterProgramIconSettings();
  }
	
	public Font getTitleFont() {
		return PROGRAMTITLEFONT;
	}

	
	public Font getTextFont() {
		return PROGRAMTEXTFONT;
	}

	
	public Font getTimeFont() {
		return PROGRAMTIMEFONT;
	}

	
	public int getTimeFieldWidth() {
		return 35;
	}





  
	public ProgramFieldType[] getProgramInfoFields() {
    return mProgramInfoFields;
	}

  public String[] getProgramTableIconPlugins() {
    return new String[]{"java.programinfo.ProgramInfo"};
  }

	
	public Color getColorOnAir_dark() {
		return COLOR_ON_AIR_DARK;
	}

	
	public Color getColorOnAir_light() {
		return COLOR_ON_AIR_LIGHT;
	}

	public Color getColorMarked() {
		return COLOR_MARKED;
	}

	
	public boolean getPaintExpiredProgramsPale() {
		return false;
	}

	
	public boolean getPaintProgramOnAir() {
		return false;
	}

	
	public boolean getPaintPluginMarks() {
		return mShowPluginMark;
	}
  
}