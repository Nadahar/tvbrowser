/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (darras@users.sourceforge.net)
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
package primarydatamanager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

import devplugin.*;
import devplugin.Program;

import tvbrowserdataservice.file.*;

/**
 * 
 * 
 * @author Til Schneider, www.murfman.de
 */
public class DayProgramFileTranslator {
  
  public static void translateAllDayPrograms(File dir)
    throws IOException, FileFormatException
  {
    if (! dir.exists()) {
      throw new IOException("Directory does not exist: " + dir.getAbsolutePath());
    }
    
    if (! dir.isDirectory()) {
      throw new IOException("File is not an directory: " + dir.getAbsolutePath());
    }
    
    // Delete the old translations
    File[] fileArr = dir.listFiles();
    for (int i = 0; i < fileArr.length; i++) {
      if (fileArr[i].getName().endsWith(".prog.txt")) {
        // This is an old translation -> delete it
        if (! fileArr[i].delete()) {
          throw new IOException("Can't delete old translation: "
            + fileArr[i].getAbsolutePath());
        } 
      }
    }

    // Go through all files and translate
    for (int i = 0; i < fileArr.length; i++) {
      if (fileArr[i].getName().endsWith(".prog.gz")) {
        translateDayProgram(fileArr[i]);
      }
      if (fileArr[i].isDirectory()) {
        translateAllDayPrograms(fileArr[i]);
      }
    }
  }



  public static void translateDayProgram(File file)
    throws IOException, FileFormatException
  {
    DayProgramFile prog = new DayProgramFile();
    prog.readFromFile(file);

    String progFileName = file.getAbsolutePath();
    // -8 for .prog.gz
    String transFileName = progFileName.substring(0, progFileName.length() - 8)
      + ".prog.txt";

    FileOutputStream stream = null;
    try {
      stream = new FileOutputStream(transFileName);
      PrintWriter writer = new PrintWriter(stream);
      
      writer.println("Version: " + prog.getVersion());
      for (int frameIdx = 0; frameIdx < prog.getProgramFrameCount(); frameIdx++) {
        ProgramFrame frame = prog.getProgramFrameAt(frameIdx);
        writer.println();
        writer.println("Program ID: " + frame.getId());
        for (int fieldIdx = 0; fieldIdx < frame.getProgramFieldCount(); fieldIdx++) {
          ProgramField field = frame.getProgramFieldAt(fieldIdx);
          ProgramFieldType type = field.getType();
          writer.print("  " + type.getName() + ": ");
          
          if (type == ProgramFieldType.INFO_TYPE) {
            int info = field.getIntData();
            writer.println(programInfoToString(info));
          } else {
            if (field.getBinaryData() == null) {
              writer.println("(delete)");
            } else {
              switch (type.getFormat()) {
                case ProgramFieldType.TEXT_FORMAT:
                  writer.println(field.getTextData());
                  break;
                case ProgramFieldType.INT_FORMAT:
                  writer.println(field.getIntData());
                  break;
                case ProgramFieldType.TIME_FORMAT:
                  int time = field.getTimeData();
                  int hours = time / 60;
                  int minutes = time % 60;
                  writer.println(hours + ":" + ((minutes < 10) ? "0" : "") + minutes);
                  break;
                default:
                  writer.println("(binary data)");
                  break;
              }
            }
          }
        }
      }
      
      writer.close();
    }
    finally {
      if (stream != null) {
        try { stream.close(); } catch (IOException exc) {}
      }
    }    
  }
  
  
  
  private static String programInfoToString(int info) {
    StringBuffer buf = new StringBuffer();

    if (bitSet(info, Program.INFO_VISION_BLACK_AND_WHITE)) {
      buf.append("Black and white  ");
    }
    if (bitSet(info, Program.INFO_VISION_4_TO_3)) {
      buf.append("4:3  ");
    }
    if (bitSet(info, Program.INFO_VISION_16_TO_9)) {
      buf.append("16:9  ");
    }
    if (bitSet(info, Program.INFO_AUDIO_MONO)) {
      buf.append("Mono  ");
    }
    if (bitSet(info, Program.INFO_AUDIO_STEREO)) {
      buf.append("Stereo  ");
    }
    if (bitSet(info, Program.INFO_AUDIO_DOLBY_SURROUND)) {
      buf.append("Dolby surround  ");
    }
    if (bitSet(info, Program.INFO_AUDIO_DOLBY_DIGITAL_5_1)) {
      buf.append("Dolby digital 5.1  ");
    }
    if (bitSet(info, Program.INFO_AUDIO_TWO_CHANNEL_TONE)) {
      buf.append("Two channel tone  ");
    }
    if (bitSet(info, Program.INFO_SUBTITLE)) {
      buf.append("Subtitle  ");
    }
    if (bitSet(info, Program.INFO_LIVE)) {
      buf.append("Live  ");
    }
    
    return buf.toString();
  }
  


  /**
   * Returns whether a bit (or combination of bits) is set in the specified
   * number.
   */
  private static boolean bitSet(int num, int pattern) {
    return (num & pattern) == pattern;
  }

  
  
  public static void main(String[] args) {
    if (args.length == 0) {
      System.out.println("Please specify at least one file or directory");
    } else {
      try {
        for (int i = 0; i < args.length; i++) {
          File file = new File(args[i]);
          if (file.isDirectory()) {
            translateAllDayPrograms(file);
          } else {
            translateDayProgram(file);
          }
        }
      }
      catch (Exception exc) {
        exc.printStackTrace();
      }
    }
  }

}
