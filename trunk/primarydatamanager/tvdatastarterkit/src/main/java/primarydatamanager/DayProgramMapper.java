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
 *     $Date: 2007-08-19 20:59:54 +0200 (So, 19 Aug 2007) $
 *   $Author: Bananeweizen $
 * $Revision: 3618 $
 */
package primarydatamanager;

import java.util.ArrayList;

import tvbrowserdataservice.file.DayProgramFile;
import tvbrowserdataservice.file.ProgramField;
import tvbrowserdataservice.file.ProgramFrame;

/**
 * 
 * 
 * @author Til Schneider, www.murfman.de
 */
public class DayProgramMapper {
  
  
  
  public DayProgramMapper() {
  }
  


  public void map(DayProgramFile rawFile, DayProgramFile lastCompleteFile)
    throws PreparationException
  {
    // NOTE: The mapping process works in two steps:
    //       1. At first all the exact matching programs are matched.
    //          In this step the most programs should be mapped.
    //       2. The remaining programs are matched to their best counterpart.
    //          This step is relatively slowly, which is the reason for step 1.
    //
    //       Warning: After this process there may still be programs that are
    //                not mapped! These programs must get a new ID.

    // First reset all IDs
    resetIDs(rawFile);
    
    // Create a list with all Programs that have not yet been assigned
    ArrayList<ProgramFrame> progList = new ArrayList<ProgramFrame>();
    for (int i = 0; i < lastCompleteFile.getProgramFrameCount(); i++) {
      progList.add(lastCompleteFile.getProgramFrameAt(i));
    }

    // Step 1:
    // Now check whether there is an exactly matching program in start time
    // and title
    mapExactMatches(rawFile, progList);
    
    // Put the unmapped programs in a list
    ArrayList<ProgramFrame> unmappedList = createUnmappedList(rawFile);
    
    if (unmappedList.isEmpty()) {
      // Nothing to do any more
      return;
    }
      
    // Step 2:
    // Match the most similar programs together
    mapSimilarMatches(progList, unmappedList);
  }
  
  
  
  private ArrayList<ProgramFrame> createUnmappedList(DayProgramFile file) {
    ArrayList<ProgramFrame> unmappedList = new ArrayList<ProgramFrame>();
    for (int i = 0; i < file.getProgramFrameCount(); i++) {
      ProgramFrame frame = file.getProgramFrameAt(i);
      if (frame.getId() == -1) {
        unmappedList.add(frame);
      }
    }
    return unmappedList;
  }



  private void resetIDs(DayProgramFile file) {
    for (int i = 0; i < file.getProgramFrameCount(); i++) {
      file.getProgramFrameAt(i).setId(-1);
    }
  }



  private void mapExactMatches(DayProgramFile rawFile, ArrayList<ProgramFrame> progList) throws PreparationException {
    for (int frameNr = 0; frameNr < rawFile.getProgramFrameCount(); frameNr++) {
      ProgramFrame rawFrame = rawFile.getProgramFrameAt(frameNr);
      int rawStarttime = PrimaryDataUtilities.getProgramStartTime(rawFrame);
      String rawTitle = PrimaryDataUtilities.getProgramTitle(rawFrame);
      
      for (int i = 0; i < progList.size(); i++) {
        ProgramFrame prepFrame = progList.get(i);
        int prepStarttime = PrimaryDataUtilities.getProgramStartTime(prepFrame);
        String prepTitle = PrimaryDataUtilities.getProgramTitle(prepFrame);
        
        if ((rawStarttime == prepStarttime) && rawTitle.equals(prepTitle)) {
          // This is a match -> map the program
          rawFrame.setId(prepFrame.getId());
          progList.remove(i);
          break;
        }
      }
    }
  }



  private void mapSimilarMatches(ArrayList<ProgramFrame> progList, ArrayList<ProgramFrame> unmappedList) {
    // NOTE: We work with a similarity matrix to find the best matches.
    //       This matrix works as follows: It contains for each raw program
    //       and each prepared program their similarity.
    // Example:
    //       Assumed we have four prepared programs and three raw programs
    //       the similarity matrix may look as follows:
    //          1 3 4 0
    //          2 9 1 3
    //          1 2 5 8
    //       Meaning that prepared program #2 and raw program #0 have a
    //       similarity of 4.
    //
    //       Now the best match is chosen. In this case [1,1] with a similarity
    //       of 9. These programs are mapped together. To avoid a second match
    //       the similarities of these programs is set to 0. After this the
    //       matrix looks like this:
    //            |
    //          1 0 4 0
    //        --0 0 0 0--
    //          1 0 5 8
    //            |
    //       This is made until the whole matrix is 0. In this case the
    //       following programs are mapped:
    //          [1,1] - similarity 9
    //          [3,2] - similarity 8  (The 5 is overwritten in this step)
    //          [2,0] - similarity 4.
    //       The raw program #0 does not get a match.
    
    // Compare each raw program with each prepared program and build a
    // similarity matrix
    double[][] similarity = new double[progList.size()][unmappedList.size()];
    for (int i = 0; i < progList.size(); i++) {
      ProgramFrame prepFrame = progList.get(i);
      for (int j = 0; j < unmappedList.size(); j++) {
        ProgramFrame rawFrame = unmappedList.get(j);
        similarity[i][j] = getSimilarity(prepFrame, rawFrame);
      }
    }
    
    // Use the similarity matrix to map all programs that are similar
    double maxSimilarity;
    do {
      // Find the programs that have the maximum similarity
      maxSimilarity = 0;
      int maxI = 0;
      int maxJ = 0;
      
      for (int i = 0; i < progList.size(); i++) {
        for (int j = 0; j < unmappedList.size(); j++) {
          if (similarity[i][j] > maxSimilarity) {
            // We have a new maximum
            maxSimilarity = similarity[i][j];
            maxI = i;
            maxJ = j;
          }
        }
      }
      
      // map the programs
      if (maxSimilarity > 0) {
        ProgramFrame prepFrame = progList.get(maxI);
        ProgramFrame rawFrame = unmappedList.get(maxJ);
        rawFrame.setId(prepFrame.getId());
        
        // Set the similarities of these programs to 0 to avoid a second match
        for (int i = 0; i < progList.size(); i++) {
          similarity[i][maxJ] = 0;
        }
        for (int j = 0; j < unmappedList.size(); j++) {
          similarity[maxI][j] = 0;
        }
      }
    } while (maxSimilarity != 0);
  }



  private double getSimilarity(ProgramFrame frame1, ProgramFrame frame2) {
    int comparedFieldCount = 0;
    int equalFieldCount = 0;
    
    for (int i = 0; i < frame1.getProgramFieldCount(); i++) {
      ProgramField field1 = frame1.getProgramFieldAt(i);
      // Get the corresponding field of frame2
      int field2Idx = frame2.getProgramFieldIndexForTypeId(field1.getTypeId());
      if (field2Idx != -1) {
        ProgramField field2 = frame2.getProgramFieldAt(field2Idx);
        
        // Compare the fields
        if (field1.equals(field2)) {
          equalFieldCount++;
        }
        comparedFieldCount++;
      }
    }
    
    if (comparedFieldCount == 0) {
      // Avoid division by zero
      return 0;
    } else {
      return (double) equalFieldCount / (double) comparedFieldCount;
    }
  }

}
