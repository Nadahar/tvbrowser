/*
 * CapturePlugin by Andreas Hessel (Vidrec@gmx.de), Bodo Tasche
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
 *     $Date: 2010-05-15 07:57:51 +0200 (Sa, 15 Mai 2010) $
 *   $Author: bananeweizen $
 * $Revision: 6627 $
 */
package captureplugin.drivers.defaultdriver;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Calendar;

import captureplugin.drivers.utils.ProgramTime;
import devplugin.Program;


/**
 * A List of ProgramTimes
 */
public final class ProgramTimeList implements Cloneable {

    /** List of ProgramTimes */
    private ArrayList<ProgramTime> mPrgTimeList = new ArrayList<ProgramTime>();

    /**
     * Create empty List
     *
     */
    public ProgramTimeList() {
        mPrgTimeList = new ArrayList<ProgramTime>();
    }

    /**
     * Copy another List
     * @param list List to copy
     */
    public ProgramTimeList(ProgramTimeList list) {
        mPrgTimeList = new ArrayList<ProgramTime>();

        ProgramTime[] prgTimes = list.getProgramTimes();

        for (ProgramTime prgTime : prgTimes) {
            mPrgTimeList.add((ProgramTime) prgTime.clone());
        }

    }

    /**
     * Clone
     */
    public Object clone() {
        return new ProgramTimeList(this);
    }

    /**
     * Contains Program in list?
     * @param program Program
     * @return true if Program is in List
     */
    public boolean contains(Program program) {
        return getProgramTimeForProgram(program) != null;
    }

    /**
     * Add ProgramTime to List
     * @param prgTime ProgramTime to add
     */
    public void add(ProgramTime prgTime) {
        mPrgTimeList.add(prgTime);
    }


    /**
     * Get ProgramTime for a specific Program
     * @param program Program
     * @return ProgramTime for Program
     */
    public ProgramTime getProgramTimeForProgram(Program program) {
        for (ProgramTime time : mPrgTimeList) {
          Program[] test = time.getAllPrograms();

          for(Program p : test) {
            if (p == program) {
              return time;
            }
          }
        }

        return null;
    }

    /**
     * Removes a ProgramTime from List
     * @param prgTime ProgramTime
     */
    public void remove(ProgramTime prgTime) {
       mPrgTimeList.remove(prgTime);
    }

    /**
     * Returns an Array of Programs in this List
     * @return Programs in this List
     */
    public Program[] getPrograms() {
        ArrayList<Program> list = new ArrayList<Program>();

        for (int i = 0; i < mPrgTimeList.size(); i++) {
            Program[] progs = mPrgTimeList.get(i).getAllPrograms();

            for(Program p : progs) {
              list.add(p);
            }
        }

        return list.toArray(new Program[list.size()]);
    }

    /**
     * Returns an Array of ProgramTimes in this List
     * @return List of all programtimes
     */
    public ProgramTime[] getProgramTimes() {
        ProgramTime[] prgTime = new ProgramTime[mPrgTimeList.size()];

        for (int i = 0; i < mPrgTimeList.size(); i++) {
            prgTime[i] = mPrgTimeList.get(i);
        }

        return prgTime;
    }


    /**
     * Save data to Stream
     * @param out save to this stream
     * @throws IOException problems during save operation
     */
    public void writeData(ObjectOutputStream out) throws IOException {
        out.writeInt(1);

        out.writeInt(mPrgTimeList.size());

        for (ProgramTime time : mPrgTimeList) {
            time.writeData(out);
        }
    }

    /**
     * Read Data from Stream
     * @param in read from this stream
     * @throws IOException problems during read operation
     * @throws ClassNotFoundException class creation problems
     */
    public void readData(java.io.ObjectInputStream in)throws IOException, ClassNotFoundException {
        in.readInt(); // version not yet used

        int size = in.readInt();

        mPrgTimeList = new ArrayList<ProgramTime>();

        for (int i = 0; i < size; i++) {
            ProgramTime prgTime = new ProgramTime();
            prgTime.readData(in);
            if (prgTime.getProgram() != null) {
                mPrgTimeList.add(prgTime);
            }
        }
    }

    /**
     * Returns the Maximum amount of overlapping Programs
     *
     * @param prgTime Check for Time-Overlappings with this Program
     * @return number of time-overlappings with Program
     */
    public int getMaxProgramsInTime(ProgramTime prgTime) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(prgTime.getStart());

        Calendar end = Calendar.getInstance();
        end.setTime(prgTime.getEnd());

        cal.add(Calendar.MINUTE, 1);
        end.add(Calendar.MINUTE, -1);

        int max = 0;

        while (cal.getTime().before(end.getTime())) {
            int cur = 0;

            for (ProgramTime pt : mPrgTimeList) {
                if (pt.getStart().equals(cal.getTime()) ||
                        pt.getEnd().equals(cal.getTime()) ||
                        (pt.getStart().before(cal.getTime()) && (pt.getEnd().after(cal.getTime())))) {
                    cur++;
                }

            }

            if (cur > max) {
                max = cur;
            }

            cal.add(Calendar.MINUTE, 1);
        }
        return max;
    }



}