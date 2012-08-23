/*
 * SoundReminder - Plugin for TV-Browser
 * Copyright (C) 2009 Ren� Mach
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
 * SVN information:
 *     $Date: 2009-03-01 09:56:39 +0100 (So, 01 Mrz 2009) $
 *   $Author: ds10 $
 * $Revision: 5521 $
 */
package soundreminder;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Locale;

import javax.sound.midi.Sequencer;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.LineEvent.Type;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import util.ui.ExtensionFileFilter;
import util.ui.Localizer;
import util.ui.UiUtilities;
import util.ui.WindowClosingIf;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.Sizes;

import devplugin.ActionMenu;
import devplugin.ContextMenuAction;
import devplugin.Plugin;
import devplugin.PluginInfo;
import devplugin.Program;
import devplugin.ProgramReceiveTarget;
import devplugin.SettingsTab;
import devplugin.ThemeIcon;
import devplugin.Version;

/**
 * A plugin to remind with different sound per title.
 * <p>
 * @author Ren� Mach
 */
public class SoundReminder extends Plugin {
  protected static final Localizer mLocalizer = Localizer
      .getLocalizerFor(SoundReminder.class);
  private static final String PLAY_TARGET = "####PLAY####MUSIC#####";
  
  private static SoundReminder mInstance;
  private ArrayList<SoundEntry> mSoundEntryList;
  
  /** Creates an instance of this class. */
  public SoundReminder() {
    mInstance = this;
    mSoundEntryList = new ArrayList<SoundEntry>(0);
  }
  
  public static Version getVersion() {
    return new Version(0,5,true);
  }
  
  public PluginInfo getInfo() {
    return new PluginInfo(SoundReminder.class, mLocalizer.msg("title",
        "Sound reminder"), mLocalizer.msg("description",
        "Playes a sound for title of programs"), "Ren\u00e9 Mach", "GPL 3");
  }
  
  public ThemeIcon getMarkIconFromTheme() {
    return new ThemeIcon("status","audio-volume-high",16);
  }
  
  public void readData(final ObjectInputStream in) throws IOException,
      ClassNotFoundException {
    final int version = in.readInt(); // read version
    final int n = in.readInt(); // read number of entries
    
    for(int i = 0; i < n; i++) {
      mSoundEntryList.add(new SoundEntry(in, version));
    }
  }
  
  public void writeData(final ObjectOutputStream out) throws IOException {
    out.writeInt(1); // write version
    out.writeInt(mSoundEntryList.size());
    
    for(SoundEntry entry : mSoundEntryList) {
      entry.writeData(out);
    }
  }
  
  public ActionMenu getButtonAction() {
    final ContextMenuAction openDialog = new ContextMenuAction(mLocalizer.msg(
        "editSoundList", "Edit sound list"), createImageIcon("status",
        "audio-volume-high", 16));
    openDialog.putValue(Plugin.BIG_ICON,createImageIcon("status","audio-volume-high",22));
    openDialog.setActionListener(new ActionListener() {
      public void actionPerformed(final ActionEvent e) {
        final Window w = UiUtilities.getLastModalChildOf(getParentFrame());
        
        JDialog temDlg = null;
        
        if(w instanceof JDialog) {
          temDlg = new JDialog((JDialog)w,true);
        }
        else {
          temDlg = new JDialog((JFrame)w,true);
        }
        
        final JDialog exclusionListDlg = temDlg;
        exclusionListDlg.setTitle(mLocalizer
            .msg("title", "Sound reminder")
            + " - "
            + mLocalizer.msg("editSoundList", "Edit sound list"));
        
        UiUtilities.registerForClosing(new WindowClosingIf() {
          public void close() {
            exclusionListDlg.dispose();
          }

          public JRootPane getRootPane() {
            return exclusionListDlg.getRootPane();
          }
        });
        
        final SoundTablePanel soundPanel = new SoundTablePanel();
        
        final JButton ok = new JButton(Localizer
            .getLocalization(Localizer.I18N_OK));
        ok.addActionListener(new ActionListener() {
          public void actionPerformed(final ActionEvent e) {
            soundPanel.saveSettings();
            exclusionListDlg.dispose();
          }
        });
        
        final JButton cancel = new JButton(Localizer
            .getLocalization(Localizer.I18N_CANCEL));
        cancel.addActionListener(new ActionListener() {
          public void actionPerformed(final ActionEvent e) {
            exclusionListDlg.dispose();
          }
        });
        
        final FormLayout layout = new FormLayout(
            "0dlu:grow,default,3dlu,default",
            "fill:default:grow,2dlu,default,5dlu,default");
        layout.setColumnGroups(new int[][] {{2,4}});
        
        final CellConstraints cc = new CellConstraints();
        final PanelBuilder pb = new PanelBuilder(layout,
            (JPanel) exclusionListDlg
            .getContentPane());
        pb.setDefaultDialogBorder();
        
        pb.add(soundPanel, cc.xyw(1,1,4));
        pb.addSeparator("", cc.xyw(1,3,4));
        pb.add(ok, cc.xy(2,5));
        pb.add(cancel, cc.xy(4,5));
        
        layoutWindow("soundListDlg", exclusionListDlg, new Dimension(600,
            450));
        exclusionListDlg.setVisible(true);
      }
    });
    
    return new ActionMenu(openDialog);
  }
  
  public SettingsTab getSettingsTab() {
    return new SettingsTab() {
      private SoundTablePanel mSoundTablePanel;
      
      public JPanel createSettingsPanel() {
        mSoundTablePanel = new SoundTablePanel();
        mSoundTablePanel.setBorder(Borders.createEmptyBorder("5dlu,10dlu,0dlu,0dlu"));
                
        return mSoundTablePanel;
      }

      public Icon getIcon() {
        return null;
      }

      public String getTitle() {
        return null;
      }

      public void saveSettings() {
        mSoundTablePanel.saveSettings();
      }
    };
  }
  
  protected static SoundReminder getInstance() {
    return mInstance;
  }
  
  public ProgramReceiveTarget[] getProgramReceiveTargets() {
    return new ProgramReceiveTarget[]{new ProgramReceiveTarget(this,mLocalizer.msg("title","Sound reminder"),PLAY_TARGET)};
  }
  
  public boolean canReceiveProgramsWithTarget() {
    return !mSoundEntryList.isEmpty();
  }
  
  public boolean receivePrograms(final Program[] programArr,
      final ProgramReceiveTarget receiveTarget) {
    if(programArr != null && !mSoundEntryList.isEmpty()) {
      for(Program p : programArr) {
        boolean soundPlayed = false;
        
        for(SoundEntry entry : mSoundEntryList) {
          if(entry.matches(p)) {
            entry.playSound();
            soundPlayed = true;
            break;
          }
        }
        
        if(!soundPlayed) {
          mSoundEntryList.get(0).playSound();
        }
      }
      
      return true;
    }
    
    return false;
  }
  
  protected Frame getSuperFrame() {
    return getParentFrame();
  }
  
  private class SoundTablePanel extends JPanel {
    private JTable mTable;
    private SoundReminderSettingsTableModel mTableModel;
    private Object mTestSound;
    
    protected SoundTablePanel() {
      mTableModel = new SoundReminderSettingsTableModel(mSoundEntryList);
      
      final SoundReminderSettingsTableRenderer renderer = new SoundReminderSettingsTableRenderer();        
      mTable = new JTable(mTableModel);
      mTableModel.setTable(mTable);
      mTable.setRowHeight(25);
      mTable.setPreferredScrollableViewportSize(new Dimension(200,150));
      mTable.getColumnModel().getColumn(0).setCellRenderer(renderer);
      mTable.getColumnModel().getColumn(1).setCellRenderer(renderer);
      mTable.getColumnModel().getColumn(1).setMaxWidth(Locale.getDefault().getLanguage().equals("de") ? Sizes.dialogUnitXAsPixel(80,mTable) : Sizes.dialogUnitXAsPixel(55,mTable));
      mTable.getColumnModel().getColumn(1).setMinWidth(mTable.getColumnModel().getColumn(1).getMaxWidth());
      mTable.getColumnModel().getColumn(2).setCellRenderer(renderer);
      mTable.getTableHeader().setReorderingAllowed(false);
      mTable.getTableHeader().setResizingAllowed(false);
      
      final JScrollPane scrollPane = new JScrollPane(mTable);
      
      mTable.addMouseListener(new MouseAdapter() {
        public void mouseClicked(final MouseEvent e) {
          final int column = mTable.columnAtPoint(e.getPoint());
          
          if(column == 1) {
            final int row = mTable.rowAtPoint(e.getPoint());
            
            mTable.getModel().setValueAt(!((Boolean)mTable.getValueAt(row,column)),row,1);
            mTable.repaint();
          }
          else if(column == 2 && e.getClickCount() == 2) {
            selectAudioFile(mTable.rowAtPoint(e.getPoint()));
          }
        }
      });
      
      mTable.addKeyListener(new KeyAdapter() {
        public void keyPressed(final KeyEvent e) {
          if(e.getKeyCode() == KeyEvent.VK_DELETE) {
            deleteSelectedRows();
            e.consume();
          }
          else if(e.getKeyCode() == KeyEvent.VK_F2 || e.getKeyCode() == KeyEvent.VK_SPACE) {
            final int row = mTable.getSelectedRow();
            
            if(mTable.getSelectedColumn() == 1) {
              mTable.getModel().setValueAt(!((Boolean)mTable.getValueAt(mTable.getSelectedRow(),1)),
                  row,1);
              mTable.repaint();
            }
            else if(mTable.getSelectedColumn() == 2){
              selectAudioFile(row);
            }
          }
        }
      });
      
      final JButton add = new JButton(mLocalizer.msg("settings.add",
          "Add entry"),
          createImageIcon("actions","document-new",16));
      add.addActionListener(new ActionListener() {
        public void actionPerformed(final ActionEvent e) {
          mTableModel.addRow();
          mTable.scrollRectToVisible(mTable.getCellRect(mTableModel.getRowCount()-1,0,true));
        }
      });
      
      final JButton delete = new JButton(mLocalizer.msg("settings.delete",
          "Delete selected entries"),createImageIcon("actions","edit-delete",16));
      delete.setEnabled(false);
      delete.addActionListener(new ActionListener() {
        public void actionPerformed(final ActionEvent e) {
          deleteSelectedRows();
        }
      });
      
      final JButton play = new JButton(mLocalizer.msg("settings.test","Test"),createImageIcon("status","audio-volume-high",16));
      play.setEnabled(false);
      play.addActionListener(new ActionListener() {
        public void actionPerformed(final ActionEvent e) {
          playSelectedRow(e);
        }
      });
      
      mTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
        public void valueChanged(final ListSelectionEvent e) {
          if(!e.getValueIsAdjusting()) {
            delete.setEnabled(e.getFirstIndex() >= 0);
            
            final String value = (String) mTableModel.getValueAt(mTable
                    .getSelectedRow(), 2);
            play.setEnabled(value != null && new File(value).isFile() && mTable.getSelectedRows().length == 1); 
          }
        }
      });
      
      final FormLayout layout = new FormLayout("default,0dlu:grow,default",
      "fill:default:grow,1dlu,default,4dlu,default,5dlu,pref");
      final PanelBuilder pb = new PanelBuilder(layout, this);
      final CellConstraints cc = new CellConstraints();
      
      int y = 1;
      
      pb.add(scrollPane, cc.xyw(1,y++,3));
      
      pb.add(play, cc.xy(3,++y));
      
      y++;
      pb.add(add, cc.xy(1,++y));
      pb.add(delete, cc.xy(3,y++));
      pb.add(UiUtilities.createHelpTextArea(mLocalizer.msg("settings.help",
      "To edit a value double click a cell. You can use wildcard * to search for any text.")), cc.xyw(1,++y,3));
    }
    
    private void selectAudioFile(final int row) {
      final String[] extArr = { ".wav", ".aif", ".rmf", ".au", ".mid" };
      final String soundFName = (String) mTable.getValueAt(row, 2);
      final String msg = mLocalizer.msg("settings.soundFileFilter",
          "Sound file ({0})",
          "*.wav, *.aif, *.rmf, *.au, *.mid");
      
      final JFileChooser fileChooser = new JFileChooser();
      fileChooser.setFileFilter(new ExtensionFileFilter(extArr, msg));
      fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
      fileChooser.setMultiSelectionEnabled(false);
      
      if(soundFName != null && new File(soundFName).isFile()) {
        fileChooser.setSelectedFile(new File(soundFName));
      }
      
      if(fileChooser.showOpenDialog(UiUtilities.getLastModalChildOf(getParentFrame())) == JFileChooser.APPROVE_OPTION) {
        mTableModel.setValueAt(fileChooser.getSelectedFile().getAbsolutePath(),row,2);
      }
    }
    
    private void playSelectedRow(final ActionEvent e) {
      if(e.getActionCommand().compareTo(mLocalizer.msg("settings.test", "Test")) == 0) {
        mTestSound = SoundEntry.playSound((String)mTable.getValueAt(mTable.getSelectedRow(),2));
        if(mTestSound != null) {
          ((JButton)e.getSource()).setText(mLocalizer.msg("settings.stop", "Stop"));
        }
        if(mTestSound != null) {
          if(mTestSound instanceof SourceDataLine) {
            ((SourceDataLine)mTestSound).addLineListener(new LineListener() {
              public void update(final LineEvent event) {
                if(event.getType() == Type.CLOSE || event.getType() == Type.STOP) {
                  ((JButton)e.getSource()).setText(mLocalizer.msg("settings.test", "Test"));
                }
              }
            });
          }
          else if(mTestSound instanceof Sequencer) {
            new Thread("Test MIDI sound") {
              public void run() {
                setPriority(Thread.MIN_PRIORITY);
                while(((Sequencer)mTestSound).isRunning()) {
                  try {
                    Thread.sleep(100);
                  }catch(Exception ee) {}
                }
                
                ((JButton)e.getSource()).setText(mLocalizer.msg("settings.test", "Test"));
              }
            }.start();
          }
        }
      }
      else if(mTestSound != null) {
        if(mTestSound instanceof SourceDataLine && ((SourceDataLine)mTestSound).isRunning()) {
          ((SourceDataLine)mTestSound).stop();
        } else if(mTestSound instanceof Sequencer && ((Sequencer)mTestSound).isRunning()) {
          ((Sequencer)mTestSound).stop();
        }
      }
    }
    
    private void deleteSelectedRows() {
      final int selectedIndex = mTable.getSelectedRow();
      final int[] selection = mTable.getSelectedRows();
      
      for(int i = selection.length-1; i >= 0; i--) {
        mTableModel.deleteRow(selection[i]);
      }
      
      if ((selectedIndex > 0) && (selectedIndex<mTable.getRowCount())) {
        mTable.setRowSelectionInterval(selectedIndex,selectedIndex);
      }
      else if(mTable.getRowCount() > 0) {
        if(mTable.getRowCount() - selectedIndex > 0) {
          mTable.setRowSelectionInterval(0,0);
        }
        else {
          mTable.setRowSelectionInterval(mTable.getRowCount()-1,mTable.getRowCount()-1);
        }
      }
    }
    
    protected void saveSettings() {
      if(mTable.isEditing()) {
        mTable.getCellEditor().stopCellEditing();
      }
      
      mSoundEntryList = mTableModel.getChangedList();
    }
  }
  
  public String getPluginCategory() {
    //Plugin.OTHER
    return "misc";
  }
}
