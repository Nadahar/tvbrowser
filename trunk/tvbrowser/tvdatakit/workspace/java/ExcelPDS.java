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

import devplugin.Channel;
import devplugin.ProgramFieldType;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import primarydatamanager.primarydataservice.AbstractPrimaryDataService;
import primarydatamanager.primarydataservice.ProgramFrameDispatcher;
import tvbrowserdataservice.file.ProgramField;
import tvbrowserdataservice.file.ProgramFrame;
import util.io.IOUtilities;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;


/**
 * Extracts TV data from a excel file.
 * <p/>
 * The excel file must follow the following rules:
 * TODO
 *
 * @author Til Schneider, www.murfman.de
 */
public class ExcelPDS extends AbstractPrimaryDataService {

    /**
     * Gets the list of the channels that are available by this data service.
     *
     * @return The list of available channels
     */
    public Channel[] getAvailableChannels() {
        HSSFSheet[] sheetArr = loadAndCheckSheets();
        return getAvailableChannels(sheetArr);
    }


    /**
     * Gets the list of the channels that are available by this data service.
     *
     * @param sheetArr The sheets to read the channels from
     * @return The list of available channels
     */
    private Channel[] getAvailableChannels(HSSFSheet[] sheetArr) {
        Channel[] channelArr = new Channel[sheetArr.length];
        for (int i = 0; i < channelArr.length; i++) {
            String id = getCellString(sheetArr[i], 1, 1);
            String country = getCellString(sheetArr[i], 4, 1);
            channelArr[i] = new Channel(id, country);
        }

        return channelArr;
    }


    /**
     * Gets the number of bytes read (= downloaded) by this data service.
     *
     * @return The number of bytes read.
     */
    public int getReadBytesCount() {
        return 0;
    }


    /**
     * Gets the raw TV data and writes it to a directory
     *
     * @param dir The directory to write the raw TV data to.
     */
    protected void execute(String dir) {
        HSSFSheet[] sheetArr = loadAndCheckSheets();
        Channel[] channelArr = getAvailableChannels(sheetArr);

        // Extract the TV data for each channel
        for (int channelNr = 0; channelNr < channelArr.length; channelNr++) {
            try {
                // Create a program dispatcher for this channel
                ProgramFrameDispatcher dispatcher = new ProgramFrameDispatcher(channelArr[channelNr]);

                HSSFSheet sheet = sheetArr[channelNr];
                for (int row = 6; row <= sheet.getLastRowNum(); row++) {
                    // Extract the date
                    devplugin.Date date = getCellDate(sheet, 0, row);
                    if (date != null) {
                        // Extract the program data
                        ProgramFrame frame = extractProgramFrame(sheet, row);

                        // Add the program data
                        dispatcher.dispatchProgramFrame(frame, date);
                    }
                }

                // Tell the program dispatcher to store the extracted TV data
                System.out.println("dir: " + dir);
                dispatcher.store(dir);
            }
            catch (Exception exc) {
                logException(exc);
            }
        }
    }


    /**
     * Extracts the program data of one program from a excel sheet.
     *
     * @param sheet The sheet to read the program from
     * @param row   The row of the program to extract.
     * @return The data of the extracted program.
     */
    private ProgramFrame extractProgramFrame(HSSFSheet sheet, int row) {
        ProgramFrame frame = new ProgramFrame();

        // Read all the fields
        frame.addProgramField(ProgramField.create(ProgramFieldType.START_TIME_TYPE,
                getCellTime(sheet, 1, row)));
        frame.addProgramField(ProgramField.create(ProgramFieldType.TITLE_TYPE,
                getCellString(sheet, 2, row)));
        frame.addProgramField(ProgramField.create(ProgramFieldType.ORIGINAL_TITLE_TYPE,
                getCellString(sheet, 3, row)));
        frame.addProgramField(ProgramField.create(ProgramFieldType.EPISODE_TYPE,
                getCellString(sheet, 4, row)));
        frame.addProgramField(ProgramField.create(ProgramFieldType.ORIGINAL_EPISODE_TYPE,
                getCellString(sheet, 5, row)));
        frame.addProgramField(ProgramField.create(ProgramFieldType.DESCRIPTION_TYPE,
                getCellString(sheet, 6, row)));
        frame.addProgramField(ProgramField.create(ProgramFieldType.ACTOR_LIST_TYPE,
                getCellString(sheet, 7, row)));
        frame.addProgramField(ProgramField.create(ProgramFieldType.DIRECTOR_TYPE,
                getCellString(sheet, 8, row)));
        frame.addProgramField(ProgramField.create(ProgramFieldType.AGE_LIMIT_TYPE,
                getCellString(sheet, 9, row)));
        frame.addProgramField(ProgramField.create(ProgramFieldType.URL_TYPE,
                getCellString(sheet, 10, row)));
        frame.addProgramField(ProgramField.create(ProgramFieldType.GENRE_TYPE,
                getCellString(sheet, 11, row)));
        frame.addProgramField(ProgramField.create(ProgramFieldType.ORIGIN_TYPE,
                getCellString(sheet, 12, row)));
        frame.addProgramField(ProgramField.create(ProgramFieldType.NET_PLAYING_TIME_TYPE,
                getCellString(sheet, 13, row)));
        frame.addProgramField(ProgramField.create(ProgramFieldType.VPS_TYPE,
                getCellString(sheet, 14, row)));
        frame.addProgramField(ProgramField.create(ProgramFieldType.SCRIPT_TYPE,
                getCellString(sheet, 15, row)));
        frame.addProgramField(ProgramField.create(ProgramFieldType.MUSIC_TYPE,
                getCellString(sheet, 16, row)));
        frame.addProgramField(ProgramField.create(ProgramFieldType.MODERATION_TYPE,
                getCellString(sheet, 17, row)));
        frame.addProgramField(ProgramField.create(ProgramFieldType.PRODUCTION_YEAR_TYPE,
                getCellString(sheet, 18, row)));

        String filename = getCellString(sheet, 19, row);
        if (filename != null && (filename.trim().length() > 0)) {

            File file = new File(filename);
            if (file.exists() && file.isFile()) {
                try {
                    ProgramField field = ProgramField.create(ProgramFieldType.PICTURE_TYPE, IOUtilities.getBytesFromFile(file));
                    if (field != null) {
                        frame.addProgramField(field);

                        frame.addProgramField(ProgramField.create(ProgramFieldType.PICTURE_COPYRIGHT_TYPE, getCellString(sheet, 20, row)));
                        frame.addProgramField(ProgramField.create(ProgramFieldType.PICTURE_DESCRIPTION_TYPE, getCellString(sheet, 21, row)));

                        System.out.println("Picture added : " + filename);
                    } else {
                        System.out.println("The Picture " + filename + " was not added");
                    }

                } catch (FileNotFoundException e) {
                    System.out.println("File " + filename + " not found!");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }

        return frame;
    }


    /**
     * Loads excel file and extracts and checks the sheets that have TV data.
     * <p/>
     * Each sheet has the data for one channel.
     *
     * @return The sheets that contain TV data.
     */
    private HSSFSheet[] loadAndCheckSheets() {
        // Check whether the TV data file exists
        File dataFile = new File("TvData.xls");
        if (!dataFile.exists()) {
            logException(new IOException("TV data not found: " + dataFile.getAbsolutePath()));
            return new HSSFSheet[0];
        }

        // Load the excel workbook
        FileInputStream stream = null;
        HSSFWorkbook workbook;
        try {
            stream = new FileInputStream(dataFile);
            workbook = new HSSFWorkbook(stream);
        }
        catch (IOException exc) {
            logException(exc);
            return new HSSFSheet[0];
        }
        finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException exc) {
                    // Nothing to do
                }
            }
        }

        // Check the sheets
        ArrayList<HSSFSheet> validSheetList = new ArrayList<HSSFSheet>();
        for (int sheetNr = 0; sheetNr < workbook.getNumberOfSheets(); sheetNr++) {
            HSSFSheet sheet = workbook.getSheetAt(sheetNr);
            if (checkSheet(sheet, sheetNr)) {
                validSheetList.add(sheet);
            }
        }

        // Make an array from the list
        HSSFSheet[] validSheetArr = new HSSFSheet[validSheetList.size()];
        validSheetList.toArray(validSheetArr);
        return validSheetArr;
    }


    /**
     * Checks whether the given sheet is valid.
     *
     * @param sheet   The sheet to check
     * @param sheetNr The number of the sheet.
     * @return Whether the given sheet is valid.
     */
    private boolean checkSheet(HSSFSheet sheet, int sheetNr) {
        if (!checkCell(sheet, sheetNr, 1, 0, "Channel ID")) return false;
        if (!checkCell(sheet, sheetNr, 4, 0, "Country Code")) return false;
        if (!checkCell(sheet, sheetNr, 0, 5, "Date")) return false;
        if (!checkCell(sheet, sheetNr, 1, 5, "Start Time")) return false;
        if (!checkCell(sheet, sheetNr, 2, 5, "Title")) return false;
        if (!checkCell(sheet, sheetNr, 3, 5, "Original Title")) return false;
        if (!checkCell(sheet, sheetNr, 4, 5, "Episode")) return false;
        if (!checkCell(sheet, sheetNr, 5, 5, "Original Episode")) return false;
        if (!checkCell(sheet, sheetNr, 6, 5, "Description")) return false;
        if (!checkCell(sheet, sheetNr, 7, 5, "Actor List")) return false;
        if (!checkCell(sheet, sheetNr, 8, 5, "Director")) return false;
        if (!checkCell(sheet, sheetNr, 9, 5, "Age limit")) return false;
        if (!checkCell(sheet, sheetNr, 10, 5, "URL")) return false;
        if (!checkCell(sheet, sheetNr, 11, 5, "Genre")) return false;
        if (!checkCell(sheet, sheetNr, 12, 5, "Origin")) return false;
        if (!checkCell(sheet, sheetNr, 13, 5, "Net playing time")) return false;
        if (!checkCell(sheet, sheetNr, 14, 5, "VPS")) return false;
        if (!checkCell(sheet, sheetNr, 15, 5, "Script")) return false;
        if (!checkCell(sheet, sheetNr, 16, 5, "Music")) return false;
        if (!checkCell(sheet, sheetNr, 17, 5, "Moderation")) return false;
        if (!checkCell(sheet, sheetNr, 18, 5, "Production Year")) return false;

        // All tests passed
        return true;
    }


    /**
     * Checks whether a certain cell has a certain value.
     *
     * @param sheet   The sheet where the cell to check is in.
     * @param sheetNr The number of the sheet.
     * @param col     The column of the cell. (Starts with 0).
     * @param row     The row of the cell. (Starts with 0).
     * @param value   The value the cell must have.
     * @return Whether the cell has the expected value.
     */
    private boolean checkCell(HSSFSheet sheet, int sheetNr, int col, int row,
                              String value) {
        String cellValue = getCellString(sheet, col, row);
        if ((cellValue != null) && cellValue.trim().equalsIgnoreCase(value.trim())) {
            return true;
        } else {
            logException(new IOException("Sheet #" + (sheetNr + 1) + " has not '"
                    + value + "' in cell (" + (col + 1) + "," + (row + 1) + "): '"
                    + cellValue + "'"));
            return false;
        }
    }


    /**
     * Gets the String value from a certain cell.
     *
     * @param sheet The sheet to read the value from
     * @param col   The column of the cell. (Starts with 0).
     * @param row   The row of the cell. (Starts with 0).
     * @return The value of the cell.
     */
    private String getCellString(HSSFSheet sheet, int col, int row) {
        HSSFCell xlsCell = getCell(sheet, col, row);
        if (xlsCell == null) {
            return null;
        } else {
            return xlsCell.toString().trim();
        }
    }


    /**
     * Gets the String value from a certain cell.
     *
     * @param sheet The sheet to read the value from
     * @param col   The column of the cell. (Starts with 0).
     * @param row   The row of the cell. (Starts with 0).
     * @return The value of the cell.
     */
    private devplugin.Date getCellDate(HSSFSheet sheet, int col, int row) {
        HSSFCell xlsCell = getCell(sheet, col, row);
        if (xlsCell == null) {
            return null;
        } else {
            java.util.Date date = xlsCell.getDateCellValue();
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);

            int year = cal.get(Calendar.YEAR);
            int month = cal.get(Calendar.MONTH) + 1;
            int day = cal.get(Calendar.DAY_OF_MONTH);

            return new devplugin.Date(year, month, day);
        }
    }


    /**
     * Gets the String value from a certain cell.
     *
     * @param sheet The sheet to read the value from
     * @param col   The column of the cell. (Starts with 0).
     * @param row   The row of the cell. (Starts with 0).
     * @return The value of the cell.
     */
    private int getCellTime(HSSFSheet sheet, int col, int row) {
        HSSFCell xlsCell = getCell(sheet, col, row);
        if (xlsCell == null) {
            return -1;
        } else {
            java.util.Date date = xlsCell.getDateCellValue();
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);

            int hour = cal.get(Calendar.HOUR_OF_DAY);
            int min = cal.get(Calendar.MINUTE);

            return (hour * 60) + min;
        }
    }


    /**
     * Gets a certain cell.
     *
     * @param sheet The sheet to get the cell from
     * @param col   The column of the cell. (Starts with 0).
     * @param row   The row of the cell. (Starts with 0).
     * @return The cell.
     */
    private HSSFCell getCell(HSSFSheet sheet, int col, int row) {
        HSSFRow xlsRow = sheet.getRow(row);
        if (xlsRow == null) {
            return null;
        } else {
            return xlsRow.getCell((short) col);
        }
    }


}
