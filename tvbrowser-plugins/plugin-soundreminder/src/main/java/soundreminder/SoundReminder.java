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
import java.awt.Point;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

import javax.sound.midi.Sequencer;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineEvent.Type;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.SourceDataLine;
import javax.swing.DefaultCellEditor;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.BadLocationException;

import soundreminder.SoundEntry.SoundPlay;
import util.ui.ExtensionFileFilter;
import util.ui.Localizer;
import util.ui.UiUtilities;
import util.ui.WindowClosingIf;

import com.jgoodies.forms.builder.ButtonBarBuilder;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.Sizes;

import devplugin.ActionMenu;
import devplugin.ContextMenuAction;
import devplugin.Plugin;
import devplugin.PluginInfo;
import devplugin.Program;
import devplugin.ProgramFilter;
import devplugin.ProgramReceiveTarget;
import devplugin.SettingsTab;
import devplugin.ThemeIcon;
import devplugin.Version;

/**
 * A plugin to remind with different sound per title.
 * <p>
 * @author René Mach
 */
public class SoundReminder extends Plugin {
  private static final Version VERSION = new Version(0,12,2,true);
  
  protected static final Localizer mLocalizer = Localizer
      .getLocalizerFor(SoundReminder.class);
  private static final String PLAY_TARGET = "####PLAY####MUSIC#####";
  
  private static SoundReminder mInstance;
  private ArrayList<SoundEntry> mSoundEntryList;
  private SoundEntry mDefaultEntry;
  private Object mTestSound;
  
  private ArrayList<SoundEntry> mPlayList;
  private Thread mPlayThread;
  private boolean mIsRunning;
  private Object mSoundPlay;
  private int mSecondsCount;
  
  private boolean mStopRunning;
  private int mStopCount;
  
  /** Creates an instance of this class. */
  public SoundReminder() {
    mInstance = this;
    mSoundEntryList = new ArrayList<SoundEntry>(0);
    mPlayList = new ArrayList<SoundEntry>();
    mIsRunning = false;
    mStopCount = 0;
    mStopRunning = true;
    mStopCount = 10; 
  }
  
  public static Version getVersion() {
    return VERSION;
  }
  
  public PluginInfo getInfo() {
    return new PluginInfo(SoundReminder.class, mLocalizer.msg("title",
        "Sound reminder"), mLocalizer.msg("description",
        "Playes a sound for title of programs"), "Ren\u00e9 Mach", "GPLv3");
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
    
    if(version > 1) {
      if(in.readBoolean()) {
        mDefaultEntry = new SoundEntry(in, version);
      }
    }
    
    if(version > 2) {
      mStopRunning = in.readBoolean();
      mStopCount = in.readInt();
    }
  }
  
  public void writeData(final ObjectOutputStream out) throws IOException {
    out.writeInt(5); // write version
    out.writeInt(mSoundEntryList.size());
    
    for(SoundEntry entry : mSoundEntryList) {
      entry.writeData(out);
    }
    
    out.writeBoolean(mDefaultEntry != null);
    
    if(mDefaultEntry != null) {
      mDefaultEntry.writeData(out);
    }
    
    out.writeBoolean(mStopRunning);
    out.writeInt(mStopCount);
  }
  
  public ActionMenu getButtonAction() {
    final ContextMenuAction openDialog = new ContextMenuAction(mLocalizer.msg(
        "editSoundList", "Edit sound list"), createImageIcon("status",
        "audio-volume-high", 16));
    openDialog.putValue(Plugin.BIG_ICON,createImageIcon("status","audio-volume-high",22));
    openDialog.setActionListener(new ActionListener() {
      private SettingsTab mSettings;
      
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
        
        mSettings = getSettingsTab();
        
        /*final SoundTablePanel soundPanel = new SoundTablePanel();
        soundPanel.setMinimumSize(new Dimension(560,420));
        */
        final JButton ok = new JButton(Localizer
            .getLocalization(Localizer.I18N_OK));
        ok.addActionListener(new ActionListener() {
          public void actionPerformed(final ActionEvent e) {
            mSettings.saveSettings();
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
        final PanelBuilder pb = new PanelBuilder(layout);
        pb.border(Borders.DIALOG);
        
        JPanel settingsPanel = mSettings.createSettingsPanel();
        settingsPanel.setMinimumSize(new Dimension(560,420));
        
        pb.add(settingsPanel, cc.xyw(1,1,4));
        pb.addSeparator("", cc.xyw(1,3,4));
        pb.add(ok, cc.xy(2,5));
        pb.add(cancel, cc.xy(4,5));
        
        pb.getPanel().setOpaque(true);
        
        exclusionListDlg.setContentPane(pb.getPanel());
        
        layoutWindow("soundListDlg", exclusionListDlg, new Dimension(600,
            450));
        exclusionListDlg.setVisible(true);
      }
    });
    
    return new ActionMenu(openDialog);
  }
  
  public void onActivation() {
    if(mPlayThread == null || !mPlayThread.isAlive()) {
      mIsRunning = true;
      
      mPlayThread = new Thread() {
        public void run() {
          while(mIsRunning) {
            try {
              sleep(1000);
              
              playSound();
            } catch (InterruptedException e) {
              // ignore
            }
          }
        }
      };
      
      mPlayThread.start();
    }
  }
  
  public void onDeactivation() {
    mIsRunning = false;
  }
  
  public void playSound() {
    boolean isPlaying = (mSoundPlay != null &&
        ((mSoundPlay instanceof SourceDataLine && ((SourceDataLine)mSoundPlay).isRunning()) 
        || (mSoundPlay instanceof Sequencer && ((Sequencer)mSoundPlay).isRunning())));
    
    if(isPlaying) {
      mSecondsCount++;
    }
    
    if(mStopRunning && isPlaying) {
      int size = 0;
      
      synchronized (mPlayList) {
        size = mPlayList.size();
      }
      
      if(mSecondsCount >= mStopCount && size > 0) {
        mSecondsCount = 0;
        
        if(mSoundPlay instanceof SourceDataLine && ((SourceDataLine)mSoundPlay).isRunning()) {
          ((SourceDataLine)mSoundPlay).stop();
        } else if(mSoundPlay instanceof Sequencer && ((Sequencer)mSoundPlay).isRunning()) {
          ((Sequencer)mSoundPlay).stop();
        }
      }
    }
    
    isPlaying = (mSoundPlay != null &&
        ((mSoundPlay instanceof SourceDataLine && ((SourceDataLine)mSoundPlay).isRunning()) 
        || (mSoundPlay instanceof Sequencer && ((Sequencer)mSoundPlay).isRunning())));
    
    if(!isPlaying) {
      SoundEntry play = null;
      
      synchronized (mPlayList) {
        if(mPlayList.size() > 0) {
          play = mPlayList.remove(0);
        }
      }
      
      if(play != null) {
        mSecondsCount = 0;
        mSoundPlay = play.playSound();
      }
    }
  }
  
  public SettingsTab getSettingsTab() {
    return new SettingsTab() {
      private SoundTablePanel mSoundTablePanel;
      private JTextField mDefaultFile;
      private JCheckBox mStopParallel;
      private JSpinner mStopCountSelection;
      
      public JPanel createSettingsPanel() {
      	PanelBuilder pb = new PanelBuilder(new FormLayout("5dlu,min,5dlu,min:grow,3dlu,min,3dlu,min,3dlu,min,5dlu",
      	    "5dlu,default,3dlu,default,10dlu,default,5dlu,fill:default:grow"));
        mSoundTablePanel = new SoundTablePanel();
        
        PanelBuilder values = new PanelBuilder(new FormLayout("10dlu,min,3dlu,min,3dlu,min:grow","default,2dlu,default"));
        
        mStopParallel = new JCheckBox(mLocalizer.msg("settings.stopParallel","For more than one parallel reminder stop"), mStopRunning);
        mStopCountSelection = new JSpinner(new SpinnerNumberModel(mStopCount, 5, 60, 5));
        
        values.add(mStopParallel, CC.xyw(1, 1, 6));
        final JLabel l1 = values.addLabel(mLocalizer.msg("settings.after","after"), CC.xy(2, 3));
        values.add(mStopCountSelection, CC.xy(4, 3));
        final JLabel l2 = values.addLabel(mLocalizer.msg("settings.seconds","seconds"), CC.xy(6, 3));
        
        mStopCountSelection.setEnabled(mStopRunning);        
        l1.setEnabled(mStopCountSelection.isEnabled());
        l2.setEnabled(mStopCountSelection.isEnabled());
        
        mStopParallel.addItemListener(new ItemListener() {
          public void itemStateChanged(ItemEvent e) {
            mStopCountSelection.setEnabled(ItemEvent.SELECTED == e.getStateChange());
            l1.setEnabled(mStopCountSelection.isEnabled());
            l2.setEnabled(mStopCountSelection.isEnabled());
          }
        });
        
        mDefaultFile = new JTextField();
        mDefaultFile.setEditable(false);
        
        if(mDefaultEntry != null) {
          mDefaultFile.setText(mDefaultEntry.getPath());
        }

        final JButton play = new JButton(mLocalizer.msg("settings.test","Test"),createImageIcon("status","audio-volume-high",16));
        play.setEnabled(mDefaultFile.getText() != null && new File(mDefaultFile.getText()).isFile());
        play.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            playSoundFile(e, mDefaultFile.getText());
          }
        });

        final JButton delete = new JButton(createImageIcon("actions", "edit-delete", 16));
        delete.setToolTipText(Localizer.getLocalization(Localizer.I18N_DELETE));
        delete.setEnabled(mDefaultFile.getText() != null && mDefaultFile.getText().trim().length() > 0);
        delete.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            mDefaultFile.setText("");
            play.setEnabled(false);
            delete.setEnabled(false);
          }
        });
        
        JButton select = new JButton(Localizer.getEllipsisLocalization(Localizer.I18N_SELECT));
        select.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {  				  
  				  String newValue = selectAudioFile(mDefaultFile.getText());
  				  
  				  if(newValue != null) {
  				    mDefaultFile.setText(newValue);
  				  }
  				  
  				  play.setEnabled(mDefaultFile.getText() != null && new File(mDefaultFile.getText()).isFile());
  				  delete.setEnabled(mDefaultFile.getText() != null && mDefaultFile.getText().trim().length() > 0);
  			  }
    		});
        
        CellConstraints cc = new CellConstraints();
        
        pb.add(values.getPanel(), cc.xyw(2, 2, 9));
        pb.addLabel(mLocalizer.msg("settings.default", "Default sound file:"), cc.xy(2, 4));
        pb.add(mDefaultFile, cc.xy(4, 4));
        pb.add(select, cc.xy(6, 4));
        pb.add(delete, cc.xy(8, 4));
        pb.add(play, cc.xy(10, 4));
        pb.addSeparator(mLocalizer.msg("settings.search", "Search settings"), cc.xyw(1,6,11));
        pb.add(mSoundTablePanel, cc.xyw(2, 8, 9));
        
        return pb.getPanel();
      }

      public Icon getIcon() {
        return null;
      }

      public String getTitle() {
        return null;
      }

      public void saveSettings() {
        mSoundTablePanel.saveSettings();
        if(mDefaultFile.getText() != null && new File(mDefaultFile.getText()).isFile()) {
          if(mDefaultEntry == null) {
            mDefaultEntry = new SoundEntry("BLABLA", false, mDefaultFile.getText());
          }
          else {
            mDefaultEntry.setValues("BLABLA", false, mDefaultFile.getText());
          }
        }
        else {
          mDefaultEntry = null;
        }
        
        mStopRunning = mStopParallel.isSelected();
        mStopCount = (Integer)mStopCountSelection.getValue();
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
    return !mSoundEntryList.isEmpty() || mDefaultEntry != null;
  }
  
  public boolean receivePrograms(final Program[] programArr,
      final ProgramReceiveTarget receiveTarget) {
    if(programArr != null && !mSoundEntryList.isEmpty()) {
      for(Program p : programArr) {
        boolean soundFound = false;
        
        for(SoundEntry entry : mSoundEntryList) {
          if(entry.matches(p)) {
            synchronized (mPlayList) {
              if(!mPlayList.contains(entry)) {
                mPlayList.add(entry);
              }              
            }
            
            soundFound = true;
            break;
          }
        }
        
        if(!soundFound) {
          synchronized (mPlayList) {
            if(mDefaultEntry != null) {
              if(!mPlayList.contains(mDefaultEntry)) {
                mPlayList.add(mDefaultEntry);
              }
            }
            else {
              SoundEntry entry = mSoundEntryList.get(0);
              
              if(!mPlayList.contains(entry)) {
                mPlayList.add(entry);
              }
            }
          }
        }
      }
      
      return true;
    }
    else if(mDefaultEntry != null && !mPlayList.contains(mDefaultEntry)) {
      mPlayList.add(mDefaultEntry);
      
      return true;
    }
    
    return false;
  }
  
  protected Frame getSuperFrame() {
    return getParentFrame();
  }
  
  private class SoundTablePanel extends JPanel {
    private static final String FILTER_PATTERN = "Filter:";
    private JTable mTable;
    private SoundReminderSettingsTableModel mTableModel;
    
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
      
      final JTextField cellEdit = new JTextField();
      cellEdit.addCaretListener(new CaretListener() {
        
        private int mCurrentLine = 0;
        
        public void caretUpdate(CaretEvent e) {try {
          String text = cellEdit.getText();
          
          if(!text.isEmpty() && cellEdit.isVisible() && text.contains(FILTER_PATTERN)) {
            if(e.getDot() > 0) {
              int lastIndex = text.indexOf(";",e.getDot());
              int firstIndex = 0;
              
              if(lastIndex == -1) {
                lastIndex = e.getDot();
              }
              
              firstIndex = text.lastIndexOf(";", lastIndex-1);
                            
              if(firstIndex == -1) {
                firstIndex = 0;
              }
              
              int testIndex = text.indexOf(FILTER_PATTERN, firstIndex);
              
              if(testIndex > 0 && text.charAt(testIndex-1) != ';') {
                return;
              }
              
              final AtomicInteger filterIndex = new AtomicInteger(((testIndex + FILTER_PATTERN.length()) == e.getDot()) ? e.getDot() : -1);
              final AtomicInteger separatorIndex = new AtomicInteger(text.indexOf(";",e.getDot()));
              
              if(separatorIndex.get() == -1) {
                separatorIndex.set(text.length());
              }
              
              if(filterIndex.get() == e.getDot()) {
                final JDialog dialog = new JDialog(UiUtilities.getLastModalChildOf(getParentFrame()));
                
                final JTextArea area = new JTextArea();
                final JScrollPane pane = new JScrollPane(area);
                
                area.setEditable(false);
                area.setFocusable(true);
                area.setLineWrap(false);
                
                final Runnable setFilter = new Runnable() {
                  public void run() {
                    dialog.dispose();
                    String text = area.getSelectedText().trim();
                    
                    if(text != null && !text.isEmpty()) {
                      String searchText = cellEdit.getText();
                      
                      String part1 = searchText.substring(0, filterIndex.get())+text;
                      String part2 = searchText.substring(separatorIndex.get(), searchText.length());
                      
                      cellEdit.setText(part1+part2);
                      cellEdit.setCaretPosition(part1.length());
                    }
                  };
                };
                
                KeyListener[] listener = area.getKeyListeners();
                
                for(KeyListener l : listener) {
                  area.removeKeyListener(l);
                }
                
                area.addKeyListener(new KeyAdapter() {
                  boolean firstEvent = true;
                  
                  public void keyReleased(KeyEvent e) {
                    if(firstEvent) {
                      firstEvent = false;
                      
                      if(e.getKeyCode() == KeyEvent.VK_LEFT || e.getKeyCode() == KeyEvent.VK_RIGHT || e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
                        return;
                      }
                    }
                    
                    if(e.getKeyCode() == KeyEvent.VK_DOWN) {
                      if(!area.getText().trim().isEmpty()) {
                        mCurrentLine++;
                        
                        if(mCurrentLine >= area.getLineCount()) {
                          mCurrentLine = 0;
                        }
                        
                        try {
                          area.setSelectionStart(area.getLineStartOffset(mCurrentLine));
                          area.setSelectionEnd(area.getLineEndOffset(mCurrentLine));
                          area.scrollRectToVisible(area.modelToView(area.getSelectionStart()));
                        } catch (BadLocationException e1) {}
                      }
                    }
                    else if(e.getKeyCode() == KeyEvent.VK_UP) {
                      if(!area.getText().trim().isEmpty()) {
                        mCurrentLine--;
                        
                        if(mCurrentLine < 0) {
                          mCurrentLine = area.getLineCount()-1;
                        }
                        
                        try {
                          area.setSelectionStart(area.getLineStartOffset(mCurrentLine));
                          area.setSelectionEnd(area.getLineEndOffset(mCurrentLine));
                          area.scrollRectToVisible(area.modelToView(area.getSelectionStart()));
                        } catch (BadLocationException e1) {}
                      }
                    }
                    else if(e.getKeyCode() == KeyEvent.VK_ESCAPE || e.getKeyCode() == KeyEvent.VK_LEFT || e.getKeyCode() == KeyEvent.VK_RIGHT || e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
                      dialog.dispose();
                    }
                    else if(e.getKeyCode() == KeyEvent.VK_ENTER) {
                      setFilter.run();
                    }
                  }
                });
                
                area.addMouseListener(new MouseAdapter() {
                  public void mouseClicked(MouseEvent e) {
                    try {
                      int line = area.getLineOfOffset(area.getCaret().getDot());
                      
                      area.setSelectionStart(area.getLineStartOffset(line));
                      area.setSelectionEnd(area.getLineEndOffset(line));
                      
                      setFilter.run();
                    } catch (BadLocationException e1) {}
                    
                    dialog.dispose();
                  };
                });
                
                ProgramFilter[] filters = Plugin.getPluginManager().getFilterManager().getAvailableFilters();
                
                String currentSelection = filterIndex != separatorIndex ? cellEdit.getText().substring(filterIndex.get(), separatorIndex.get()) : null;
                
                for(int i = 0; i < filters.length; i++) {
                  area.append(filters[i].getName());
    
                  if(i < filters.length-1) {
                    area.append("\n");
                  }
                }
                
                if(currentSelection != null) {
                  try {
                    mCurrentLine = area.getLineOfOffset(area.getText().indexOf(currentSelection));
                  } catch (BadLocationException e1) {}
                }
                
                try {
                  area.setSelectionStart(area.getLineStartOffset(mCurrentLine));
                  area.setSelectionEnd(area.getLineEndOffset(mCurrentLine));
                } catch (BadLocationException e1) {}
                
                area.addFocusListener(new FocusAdapter() {
                  public void focusLost(FocusEvent e) {
                    dialog.dispose();
                  }
                });
    
                dialog.addWindowFocusListener(new WindowAdapter() {
                  @Override
                  public void windowOpened(java.awt.event.WindowEvent e) {
                    try {
                      area.scrollRectToVisible(area.modelToView(area.getSelectionStart()));
                    } catch (BadLocationException e1) {}
                  };
                });
                
                Point location = cellEdit.getLocationOnScreen();
                location.setLocation(location.x, location.y+cellEdit.getHeight());
                
                dialog.setUndecorated(true);
                dialog.setContentPane(pane);
                dialog.setSize(300,150);
                dialog.setAlwaysOnTop(true);
                dialog.setVisible(true);
                dialog.setLocation(location);
                dialog.toFront();
                area.grabFocus();
              }
            }
          }
        }catch(Throwable t) {}
        }
        
      });
      
      mTable.getColumnModel().getColumn(0).setCellEditor(new DefaultCellEditor(cellEdit) {
        @Override
        public boolean stopCellEditing() {try {
          String text = cellEdit.getText().replaceAll("\\s*"+FILTER_PATTERN+"\\s*;", "");
          
          if(text.trim().endsWith(FILTER_PATTERN)) {
            text = text.substring(0, text.lastIndexOf(FILTER_PATTERN));
          }
          
          if(text.trim().endsWith(";")) {
            text = text.substring(0, text.lastIndexOf(";"));
          }
          
          if(text.trim().length() == 0) {
            cancelCellEditing();
            return false;
          }
          
          cellEdit.setText(text);
        }catch(Throwable t) {}
          return super.stopCellEditing();
        }
      });
      
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
            selectAudioFileInternal(mTable.rowAtPoint(e.getPoint()));
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
              selectAudioFileInternal(row);
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
          playSoundFile(e,(String)mTable.getValueAt(mTable.getSelectedRow(),2));
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
      
      final FormLayout layout = new FormLayout("default,min:grow,default",
      "fill:min:grow,2dlu,default,5dlu,default");
      final PanelBuilder pb = new PanelBuilder(layout, this);
      final CellConstraints cc = new CellConstraints();
      
      ButtonBarBuilder buttonPanel = new ButtonBarBuilder();
      buttonPanel.addButton(add);
      buttonPanel.addRelatedGap();
      buttonPanel.addButton(delete);
      buttonPanel.addGlue();
      buttonPanel.addButton(play);
      
      int y = 1;
      
      pb.add(scrollPane, cc.xyw(1,y++,3));      
      pb.add(buttonPanel.getPanel(), cc.xyw(1,++y,3));
      y++;
      pb.add(UiUtilities.createHelpTextArea(mLocalizer.msg("settings.help",
      "To edit a value double click a cell. You can use wildcard * to search for any text.")), cc.xyw(1,++y,3));
    }
    
    private void selectAudioFileInternal(int row) {
      final String soundFName = (String) mTable.getValueAt(row, 2);
      
      String selectedValue = selectAudioFile(soundFName);
      
      if(selectedValue != null) {
        mTableModel.setValueAt(selectedValue,row,2);
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
    
  private String selectAudioFile(String soundFName) {
	  final String[] extArr = { ".wav", ".aif", ".rmf", ".au", ".mid" };
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
	  else if(mDefaultEntry != null && mDefaultEntry.getPath() != null && mDefaultEntry.getPath().trim().length() > 0) {
	    File dir = new File(mDefaultEntry.getPath()).getParentFile();
	        
	    if(dir.isDirectory()) {
	      fileChooser.setCurrentDirectory(dir);
	    }
	  }
	  
	  if(fileChooser.showOpenDialog(UiUtilities.getLastModalChildOf(getParentFrame())) == JFileChooser.APPROVE_OPTION) {
		  return fileChooser.getSelectedFile().getAbsolutePath();
	  }
	  
	  return null;
	}
  private void playSoundFile(final ActionEvent e, String soundFName) {try {
    if(e.getActionCommand().compareTo(mLocalizer.msg("settings.test", "Test")) == 0) {
      mTestSound = SoundEntry.playSound(soundFName);
      
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
        else if(mTestSound instanceof SoundPlay) {
          ((SoundPlay) mTestSound).setChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent ce) {
              ((JButton)e.getSource()).setText(mLocalizer.msg("settings.test", "Test"));
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
      } else if(mTestSound instanceof SoundPlay && ((SoundPlay)mTestSound).isRunning()) {
        ((SoundPlay)mTestSound).stop();
      } else if(mTestSound instanceof Sequencer && ((Sequencer)mTestSound).isRunning()) {
        ((Sequencer)mTestSound).stop();
      }
    }}catch(Throwable t) {t.printStackTrace();}
  }

  
  public String getPluginCategory() {
    //Plugin.OTHER
    return "misc";
  }
}
