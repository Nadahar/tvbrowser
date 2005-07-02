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
 *     $Date$
 *   $Author$
 * $Revision$
 */
package captureplugin.drivers.defaultdriver;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import devplugin.Program;


/**
 * A List of ProgramTimes
 */
public class ProgramTimeList {

    /** List of ProgramTimes */
    private ArrayList mPrgTimeList = new ArrayList();
    
    /**
     * Create empty List
     *
     */
    public ProgramTimeList() {
        mPrgTimeList = new ArrayList();
    }
    
    /**
     * Copy another List
     * @param list List to copy
     */
    public ProgramTimeList(ProgramTimeList list) {
        
        mPrgTimeList = new ArrayList();
        
        ProgramTime[] prgTimes = list.getProgramTimes(); 
        
        for (int i = 0; i < prgTimes.length; i++) {
            mPrgTimeList.add(prgTimes[i].clone());
        }
        
    }
    
    /**
     * Clone
     */
    public Object clone() {
        return new ProgramTimeList(this);
    }

    /**
     * Contains Progam in list?
     * @param program Program
     * @return true if Program is in List
     */
    public boolean contains(Program program) {

        if (getProgamTimeForProgram(program) != null) {
            return true;
        }

        return false;
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
    public ProgramTime getProgamTimeForProgram(Program program) {
        for (int i = 0; i < mPrgTimeList.size(); i++) {
            
            ProgramTime prgTime = (ProgramTime)mPrgTimeList.get(i);
            
            if (prgTime.getProgram() == program) {
                return prgTime;
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
        Program[] prg = new Program[mPrgTimeList.size()];
        
        for (int i = 0; i < mPrgTimeList.size(); i++) {
            prg[i] = ((ProgramTime)mPrgTimeList.get(i)).getProgram();
        }
        
        return prg;
    }
    
    /**
     * Returns an Array of ProgramTimes in this List
     * @return
     */
    public ProgramTime[] getProgramTimes() {
        ProgramTime[] prgTime = new ProgramTime[mPrgTimeList.size()];
        
        for (int i = 0; i < mPrgTimeList.size(); i++) {
            prgTime[i] = (ProgramTime)mPrgTimeList.get(i);
        }
        
        return prgTime;
    }
    
    
    /**
     * Save data to Stream
     * @param out
     * @throws IOException
     */
    public void writeData(ObjectOutputStream out) throws IOException {
        out.writeInt(1);
        
        out.writeInt(mPrgTimeList.size());
        
        for (int i = 0; i < mPrgTimeList.size(); i++) {
            ((ProgramTime)mPrgTimeList.get(i)).writeData(out);
        }
    }
    
    /**
     * Read Data from Stream
     * @param in
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public void readData(java.io.ObjectInputStream in)throws IOException, ClassNotFoundException {
    
        int version = in.readInt();
        
        int size = in.readInt();
        
        mPrgTimeList = new ArrayList();
        
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

        DateFormat format = DateFormat.getTimeInstance();
        
        Calendar cal = Calendar.getInstance();
        cal.setTime(prgTime.getStart());
        
        
        Calendar end = Calendar.getInstance();
        end.setTime(prgTime.getEnd());
        
        cal.add(Calendar.MINUTE, 1);
        end.add(Calendar.MINUTE, -1);
        
        int max = 0;
        
        while (cal.getTime().before(end.getTime())) {
            int cur = 0;
            
            for (int i = 0; i < mPrgTimeList.size(); i++) {
                ProgramTime pt = (ProgramTime) mPrgTimeList.get(i);
                
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