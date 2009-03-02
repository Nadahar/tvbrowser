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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

import tvbrowserdataservice.file.DayProgramFile;
import tvbrowserdataservice.file.ProgramField;
import tvbrowserdataservice.file.ProgramFrame;
import util.io.FileFormatException;
import devplugin.Program;
import devplugin.ProgramFieldType;

/**
 * 
 * 
 * @author Til Schneider, www.murfman.de
 */
public class DayProgramFileTranslator {
  
  public static void translateAllDayPrograms(File srcDir)
    throws IOException, FileFormatException
  {
    if (! srcDir.exists()) {
      throw new IOException("Directory does not exist: " + srcDir.getAbsolutePath());
    }
    
    if (! srcDir.isDirectory()) {
      throw new IOException("File is not a directory: " + srcDir.getAbsolutePath());
    }
    
    
    File destDir=new File(srcDir,"txt");
    if (!destDir.exists()) {
      if (!destDir.mkdirs()) {
        return;
      }
    }
    
    
    // Delete the old translations
    File[] fileArr = destDir.listFiles();
    if (fileArr != null) {
      for (int i = 0; i < fileArr.length; i++) {
        if (fileArr[i].getName().endsWith(".prog.txt")) {
          // This is an old translation -> delete it
          if (! fileArr[i].delete()) {
            throw new IOException("Can't delete old translation: "
              + fileArr[i].getAbsolutePath());
          } 
        }
      }
    }
 
    // Go through all files and translate
    fileArr = srcDir.listFiles();
    if (fileArr != null) {
      for (int i = 0; i < fileArr.length; i++) {
        if (fileArr[i].getName().endsWith(".prog.gz")) {
          translateDayProgram(fileArr[i],destDir);
        }
      }
    }
  }



  public static void translateDayProgram(File file, File destDir)
    throws IOException, FileFormatException
  {
    
    if (! destDir.exists()) {
      throw new IOException("Directory does not exist: " + destDir.getAbsolutePath());
    }
    
    if (! destDir.isDirectory()) {
      throw new IOException("File is not a directory: " + destDir.getAbsolutePath());
    }
    
    DayProgramFile prog = new DayProgramFile();
    prog.readFromFile(file);

    String progFileName = file.getName();  //file.getAbsolutePath();
    // -8 for .prog.gz
    String transFileName = progFileName.substring(0, progFileName.length() - 8)
      + ".prog.txt";

    String binFileName = progFileName.substring(0, progFileName.length() - 8);
    int binNumber =0;

    FileOutputStream stream = null;
    try {
      
      stream = new FileOutputStream(new File(destDir,transFileName));
      PrintWriter writer = new PrintWriter(stream);
      
      writer.print("Version: ");
      writer.println(prog.getVersion());
      for (int frameIdx = 0; frameIdx < prog.getProgramFrameCount(); frameIdx++) {
        ProgramFrame frame = prog.getProgramFrameAt(frameIdx);
        writer.println();
        writer.print("Program ID: ");
        writer.println(frame.getId());
        for (int fieldIdx = 0; fieldIdx < frame.getProgramFieldCount(); fieldIdx++) {
          ProgramField field = frame.getProgramFieldAt(fieldIdx);
          ProgramFieldType type = field.getType();
          writer.print("  ");
          writer.print(type.getName());
          writer.print(": ");
          
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
                case ProgramFieldType.BINARY_FORMAT:
                    binNumber++;
                    writeBinary(new File(destDir, binFileName + "-" + binNumber + ".bin"), field.getBinaryData());
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

    /**
     * Writes Binary-Data into a File
     *
     * @param file File to write Data into
     * @param binaryData Data to write
     */
    private static void writeBinary(File file, byte[] binaryData) {
        try {
            System.out.println("Writing : " + file.getAbsolutePath());
            BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(file));
            out.write(binaryData);
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String programInfoToString(int info) {
    StringBuffer buf = new StringBuffer(128);

    if (bitSet(info, Program.INFO_VISION_BLACK_AND_WHITE)) {
      buf.append("Black and white  ");
    }
    if (bitSet(info, Program.INFO_VISION_4_TO_3)) {
      buf.append("4:3  ");
    }
    if (bitSet(info, Program.INFO_VISION_16_TO_9)) {
      buf.append("16:9  ");
    }
      if (bitSet(info, Program.INFO_VISION_HD)) {
      buf.append("HD  ");
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
    if (bitSet(info, Program.INFO_AUDIO_DESCRIPTION)) {
      buf.append("Audio Description  ");
    }
    if (bitSet(info, Program.INFO_ORIGINAL_WITH_SUBTITLE)) {
      buf.append("Original with subtitle  ");
    }
    if (bitSet(info, Program.INFO_SUBTITLE_FOR_AURALLY_HANDICAPPED)) {
      buf.append("Subtitle for aurally handicapped  ");
    }
    if (bitSet(info, Program.INFO_LIVE)) {
      buf.append("Live  ");
    }
    if (bitSet(info, Program.INFO_NEW)) {
      buf.append("New  ");
    }
    if (bitSet(info, Program.INFO_CATEGORIE_MOVIE)) {
        buf.append("Movie  ");
      }
      if (bitSet(info, Program.INFO_CATEGORIE_SERIES)) {
        buf.append("Series  ");
      }
      if (bitSet(info, Program.INFO_CATEGORIE_NEWS)) {
        buf.append("News  ");
      }
      if (bitSet(info, Program.INFO_CATEGORIE_MAGAZINE_INFOTAINMENT)) {
        buf.append("Magazine/Infotainment  ");
      }
      if (bitSet(info, Program.INFO_CATEGORIE_SHOW)) {
        buf.append("Show  ");
      }
      if (bitSet(info, Program.INFO_CATEGORIE_ARTS)) {
        buf.append("Arts  ");
      }
      if (bitSet(info, Program.INFO_CATEGORIE_CHILDRENS)) {
        buf.append("Childrens  ");
      }
      if (bitSet(info, Program.INFO_CATEGORIE_DOCUMENTARY)) {
        buf.append("Documentary  ");
      }
      if (bitSet(info, Program.INFO_CATEGORIE_OTHERS)) {
        buf.append("Other Category  ");
      }
      if (bitSet(info, Program.INFO_CATEGORIE_SPORTS)) {
        buf.append("Sports  ");
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
          if (! file.exists()) {
            System.out.println("File does not exist: " + file.getAbsolutePath());
          } else {
            if (file.isDirectory()) {
              translateAllDayPrograms(file);
            } else {
              translateDayProgram(file,new File("."));
            }
          }
        }
      }
      catch (Exception exc) {
        exc.printStackTrace();
      }
    }
  }

}
