package mediacenterplugin;

import java.awt.Font;
import java.awt.Graphics2D;
import java.io.StringReader;

import util.misc.TextLineBreakerFontWidth;
import devplugin.Program;
import devplugin.ProgramFieldType;

public class MediaProgramPanel {

  Program mProgram;
  String[] mSplittedText;
  int mLineStart = 0;
  
  public void setProgram(Program program, Font font, int width) {
    if ((mProgram == null) || (!mProgram.equals(program))) {
      mProgram = program;
      
      TextLineBreakerFontWidth breaker = new TextLineBreakerFontWidth(font);
      
      try {
        mSplittedText = breaker.breakLines(new StringReader(generateStringFromProgram(program)), width, Integer.MAX_VALUE); 
      } catch (Exception e) {
        mSplittedText = new String[0];
      }
      
    }
    
  }
  
  private String generateStringFromProgram(Program program) {
    StringBuffer line = new StringBuffer();
    
    line.append(program.getShortInfo());
    line.append("\n\n");
    line.append(program.getDescription());
    line.append("\n\n");
    line.append(program.getTextField(ProgramFieldType.EPISODE_TYPE));
    line.append("\n\n");
    line.append(program.getTextField(ProgramFieldType.ACTOR_LIST_TYPE));
    
    
    return line.toString();
  }

  public void nextLine() {
    if ((mSplittedText != null) && (mLineStart < mSplittedText.length)) {
      mLineStart++;
    }
  }
  
  public void lastLine() {
    if (mLineStart > 0) {
      mLineStart--;
    }
  }
  
  public void paintPanel(Graphics2D g2d, Font textFont, int x, int y) {
    if (mProgram == null) 
      return;
 
    Font titleFont = textFont.deriveFont(textFont.getSize2D() + (int) (textFont.getSize()*0.2));
    DrawToolBox.drawFontWithShadow(g2d, mProgram.getTimeString() + " " + mProgram.getTitle(), x, y, 1, titleFont);
    y += titleFont.getSize() + (int) (textFont.getSize()*0.2);
    
    int lineheight = textFont.getSize() + (int) (textFont.getSize()*0.2);

    g2d.setFont(textFont);
    
    int maxlines = (200 / lineheight) - 1; 

    if (maxlines > mSplittedText.length) {
      maxlines = mSplittedText.length;
    }  
      
    if (mLineStart+maxlines > mSplittedText.length) {
      mLineStart = mSplittedText.length-maxlines;
    }
    
    for (int i = 0; i < maxlines;i++) {
      DrawToolBox.drawFontWithShadow(g2d, mSplittedText[mLineStart+i], x, y, 1, textFont);
      y += lineheight;
    }
  }
  
}