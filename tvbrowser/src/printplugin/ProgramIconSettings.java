package printplugin;

import java.awt.*;

import javax.swing.Icon;

import devplugin.Program;
import devplugin.ProgramFieldType;

public interface ProgramIconSettings {
  
  public Font getTitleFont();
  public Font getTextFont();
  public Font getTimeFont();
  
  public int getTimeFieldWidth();
  
  
  public ProgramFieldType[] getProgramInfoFields();
  public Icon[] getPluginIcons(Program prog);
  
  public Color getColorOnAir_dark();
  public Color getColorOnAir_light();
  public Color getColorMarked();
  
  public boolean getPaintExpiredProgramsPale();
  
  public boolean getPaintProgramOnAir();
  public boolean getPaintMarkedPrograms();
  
}