/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (martin_oat@yahoo.de)
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

package programinfo;

import java.util.Iterator;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import util.ui.*;

import devplugin.Program;
import devplugin.Plugin;

/**
 * TV-Browser
 *
 * @author Martin Oberhauser
 */
public class ProgramInfoDialog extends JDialog {

  private static final util.ui.Localizer mLocalizer
    = util.ui.Localizer.getLocalizerFor(ProgramInfoDialog.class);
  
  private static final Font LABEL_FONT = new JLabel().getFont();
  private static final Font TITLE_FONT
    = new Font(LABEL_FONT.getName(), Font.BOLD, LABEL_FONT.getSize() + 4);
  private static final Font INFO_FONT
    = new Font(LABEL_FONT.getName(), Font.ITALIC, LABEL_FONT.getSize() - 2);

  
  
  public ProgramInfoDialog(Frame parent, devplugin.Program program) {
    super(parent);
    
    String msg;
    JTextArea ta;
    JLabel lb;
    
    setTitle(mLocalizer.msg("title", "Program information"));
    
    JPanel main = new JPanel(new BorderLayout());
    main.setPreferredSize(new Dimension(450, 250));
    setContentPane(main);
    
    JPanel infoPn = new JPanel(new BorderLayout(10, 10));
    infoPn.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    final JScrollPane scrollPane = new JScrollPane(infoPn);
    main.add(scrollPane, BorderLayout.CENTER);

    // the header (title and time)
    JPanel headerPn = new JPanel(new BorderLayout(10, 10));
    infoPn.add(headerPn, BorderLayout.NORTH);
    
    // add the channel and title
    msg = program.getChannel().getName() + ": " + program.getTitle();
    ta = createTextArea(msg);
    ta.setFont(TITLE_FONT);
    headerPn.add(ta, BorderLayout.CENTER);
    
    // Add date, time and length
    JPanel timePn = new JPanel(new TabLayout(1));
    headerPn.add(timePn, BorderLayout.EAST);
    timePn.add(new JLabel(program.getDateString()));
    timePn.add(new JLabel(program.getTimeString()));
    if (program.getLength()>0) {
    	msg = mLocalizer.msg("min", "{0} min", new Integer(program.getLength()));
    	timePn.add(new JLabel(msg));
    }
    
    // add all the other information
    JPanel bodyPn = new JPanel(new TabLayout(1));
    infoPn.add(bodyPn, BorderLayout.CENTER);

    // info flags
    msg = programInfoToString(program.getInfo());
    lb = new JLabel(msg);
    lb.setFont(INFO_FONT);
    bodyPn.add(lb);

    // description
    String desc = program.getDescription();
    bodyPn.add(createTextArea(desc));

    // Create the shortInfoArea only if the short info differs from the description
    String shortInfo = program.getShortInfo();
    int shortInfoLength = shortInfo.length();
    if (shortInfo.endsWith("...")) {
      shortInfoLength -= 3;
    }
    String shortInfoSub = shortInfo.substring(0, shortInfoLength);
    String descSub = desc.substring(0, shortInfoLength);
    if (! shortInfoSub.equals(descSub)) {
      bodyPn.add(createTextArea(shortInfo));
    }

    // actors
    bodyPn.add(createTextArea(program.getActors()));
    
    // plugins
    Iterator pluginIter = program.getMarkedByIterator();
    if (pluginIter.hasNext()) {
      JPanel pluginPn = new JPanel(new FlowLayout(FlowLayout.LEADING));
      bodyPn.add(pluginPn);
      while (pluginIter.hasNext()) {
        Plugin plugin = (Plugin) pluginIter.next();
        pluginPn.add(new JLabel(plugin.getMarkIcon()));
      }
    }
    
    // buttons
    JPanel buttonPn = new JPanel(new BorderLayout());
    buttonPn.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    main.add(buttonPn, BorderLayout.SOUTH);

    JButton closeBtn = new JButton(mLocalizer.msg("close", "Close"));
    closeBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        dispose();
      }
    }
    );
    buttonPn.add(closeBtn, BorderLayout.EAST);
    getRootPane().setDefaultButton(closeBtn);
    
    // Scroll to the beginning
    Runnable runnable = new Runnable() {
      public void run() {
        scrollPane.getVerticalScrollBar().setValue(0);
      }
    };
    SwingUtilities.invokeLater(runnable);
  }
  
  
  
  private JTextArea createTextArea(String text) {
    JTextArea ta = new JTextArea(text);
    ta.setEditable(false);
    ta.setOpaque(false);
    ta.setLineWrap(true);
    ta.setWrapStyleWord(true);
    return ta;
  }

  
  
  private String programInfoToString(int info) {
    StringBuffer buf = new StringBuffer();

    // TODO: This is TVgenial specific!!
    if (bitSet(info, Program.INFO_VISION_BLACK_AND_WHITE)) {
      buf.append(mLocalizer.msg("blackAndWhite", "Black and white") + "  ");
    }
    if (bitSet(info, Program.INFO_VISION_4_TO_3)) {
      buf.append(mLocalizer.msg("4to3", "4:3") + "  ");
    }
    if (bitSet(info, Program.INFO_VISION_16_TO_9)) {
      buf.append(mLocalizer.msg("16to9", "16:9") + "  ");
    }
    if (bitSet(info, Program.INFO_AUDIO_MONO)) {
      buf.append(mLocalizer.msg("mono", "Mono") + "  ");
    }
    if (bitSet(info, Program.INFO_AUDIO_STEREO)) {
      buf.append(mLocalizer.msg("stereo", "Stereo") + "  ");
    }
    if (bitSet(info, Program.INFO_AUDIO_DOLBY_SURROUND)) {
      buf.append(mLocalizer.msg("dolbySurround", "Dolby surround") + "  ");
    }
    if (bitSet(info, Program.INFO_AUDIO_DOLBY_DIGITAL_5_1)) {
      buf.append(mLocalizer.msg("dolbyDigital5.1", "Dolby digital 5.1") + "  ");
    }
    if (bitSet(info, Program.INFO_AUDIO_TWO_CHANNEL_TONE)) {
      buf.append(mLocalizer.msg("twoChannelTone", "Two channel tone") + "  ");
    }
    if (bitSet(info, Program.INFO_SUBTITLE)) {
      buf.append(mLocalizer.msg("subtitle", "Subtitle") + "  ");
    }
    if (bitSet(info, Program.INFO_LIVE)) {
      buf.append(mLocalizer.msg("live", "Live") + "  ");
    }
    
    return buf.toString();
  }
  
  
  
  /**
   * Returns whether a bit (or combination of bits) is set in the specified
   * number.
   */
  private boolean bitSet(int num, int pattern) {
    return (num & pattern) == pattern;
  }
  
}