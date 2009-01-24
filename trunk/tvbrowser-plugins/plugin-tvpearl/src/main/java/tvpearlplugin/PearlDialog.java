/*
 * TV-Pearl by Reinhard Lehrbaum
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
 */
package tvpearlplugin;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Calendar;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JToolTip;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import tvbrowser.core.icontheme.IconLoader;
import util.ui.Localizer;
import util.ui.SearchFormSettings;
import util.ui.SearchHelper;
import util.ui.SendToPluginDialog;
import util.ui.UiUtilities;
import util.ui.WindowClosingIf;
import util.ui.menu.MenuUtil;

import com.jgoodies.forms.builder.ButtonBarBuilder;

import devplugin.Plugin;
import devplugin.PluginManager;
import devplugin.Program;

public class PearlDialog extends JDialog implements WindowClosingIf
{
	private static final long serialVersionUID = 1L;

	protected static final util.ui.Localizer mLocalizer = util.ui.Localizer.getLocalizerFor(TVPearlPlugin.class);

	private JScrollPane mScrollPane;
	private JList mDataList;
	private JButton mSendBn;
	private JButton mCloseBn;
	private JButton mUpdateBn;
	private DefaultListModel mProgramList;

	public PearlDialog(Dialog dialog)
	{
		super(dialog, true);

		setTitle(mLocalizer.msg("name", "TV Pearl"));
		createGUI();
		UiUtilities.registerForClosing(this);
	}

	public PearlDialog(Frame frame)
	{
		super(frame, true);

		setTitle(mLocalizer.msg("name", "TV Pearl"));
		createGUI();
		UiUtilities.registerForClosing(this);
	}

	private void createGUI()
	{
		JPanel main = new JPanel(new BorderLayout());
		main.setBorder(UiUtilities.DIALOG_BORDER);

		setContentPane(main);

		mProgramList = new DefaultListModel();
		mDataList = new JList(mProgramList)
		{
			private static final long serialVersionUID = 1L;

			public JToolTip createToolTip()
			{
				return new TVPearlToolTip();
			}

			public String getToolTipText(MouseEvent evt)
			{
				int index = locationToIndex(evt.getPoint());

				Object item = getModel().getElementAt(index);
				if (item instanceof TVPProgram)
				{
					TVPProgram p = (TVPProgram) item;
					HTTPConverter converter = new HTTPConverter();
					final String info = converter.convertToString(p.getInfo());
          if (info.length() == 0) {
            return mLocalizer.msg("noInfo", "(no information)");
          }
          return info;
				}
				return null;
			}
		};
		mDataList.setCellRenderer(new TVPearlListCellRenderer());
		mDataList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		mDataList.addMouseListener(new MouseAdapter()
		{
			public void mouseClicked(MouseEvent e)
			{
				PluginManager mng = Plugin.getPluginManager();
				int index = mDataList.locationToIndex(e.getPoint());

				if (mDataList.getModel().getElementAt(index) instanceof TVPProgram)
				{

					TVPProgram pearl = (TVPProgram) mDataList.getModel().getElementAt(
              index);
          Program prog = pearl.getProgram();
					if (prog != null)
					{
						if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 2)
						{
							mng.handleProgramDoubleClick(prog, TVPearlPlugin.getInstance());
						}
						else if (SwingUtilities.isMiddleMouseButton(e) && (e.getClickCount() == 1))
						{
							mng.handleProgramMiddleClick(prog);
						}
					}
					else {
            if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 2) {
              TVPearlPlugin.getInstance().showPearlInfo(pearl);
            }
          }
					if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 1 && e.isShiftDown())
					{
						TVPearlPlugin.getInstance().showPearlInfo(pearl);
					}
				}
			}

			public void mouseReleased(MouseEvent e)
			{
				checkPopup(e);
			}

			public void mousePressed(MouseEvent e)
			{
				checkPopup(e);
			}
		});
		mDataList.addListSelectionListener(new ListSelectionListener()
		{
			public void valueChanged(ListSelectionEvent e)
			{
				if (!mDataList.getValueIsAdjusting())
				{
					boolean isEnable = false;
					Object selectedValues[] = mDataList.getSelectedValues();
					if (selectedValues.length == 1
              && (selectedValues[0] instanceof TVPProgram)) {
            TVPProgram p = (TVPProgram) selectedValues[0];
            if (p.wasFound()) {
              isEnable = true;
            }
          }
					mSendBn.setEnabled(isEnable);
				}
			}
		});

		mScrollPane = new JScrollPane(mDataList);
		main.add(mScrollPane, BorderLayout.CENTER);

		ButtonBarBuilder builderButton = ButtonBarBuilder.createLeftToRightBuilder();
		builderButton.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));

		JButton configBn = new JButton(IconLoader.getInstance().getIconFromTheme("categories", "preferences-system", 16));
		configBn.setToolTipText(mLocalizer.msg("config", "Configure TV Pearl"));
		configBn.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				close();
				Plugin.getPluginManager().showSettings(TVPearlPlugin.getInstance());
			}
		});
		builderButton.addFixed(configBn);
		builderButton.addRelatedGap();

		mSendBn = new JButton(IconLoader.getInstance().getIconFromTheme("actions", "edit-copy", 16));
		mSendBn.setToolTipText(mLocalizer.msg("send", "Send to other Plugins"));
		mSendBn.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				showSendDialog();
			}
		});
		builderButton.addFixed(mSendBn);
		builderButton.addRelatedGap();

		builderButton.addGlue();

		mUpdateBn = new JButton(mLocalizer.msg("update", "Update"));
		mUpdateBn.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent evt)
			{
				try
				{
					mUpdateBn.setEnabled(false);
					getRootPane().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
					// TVPearlPlugin.getInstance().update();
					TVPearlPlugin.getInstance().run();
				}
				catch (Exception e)
				{
					mUpdateBn.setEnabled(true);
				}
				finally
				{
					getRootPane().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
				}
			}
		});
		mUpdateBn.setVisible(TVPearlPlugin.getSettings().getUpdatePearlsManually());
		builderButton.addFixed(mUpdateBn);
		builderButton.addRelatedGap();

		mCloseBn = new JButton(Localizer.getLocalization(Localizer.I18N_CLOSE));
		mCloseBn.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent evt)
			{
				dispose();
			}
		});
		builderButton.addFixed(mCloseBn);

		main.add(builderButton.getPanel(), BorderLayout.SOUTH);

		getRootPane().setDefaultButton(mCloseBn);

		pack();

		updateProgramList();
	}

	private void checkPopup(final MouseEvent e)
	{
		if (e.isPopupTrigger())
		{
			int index = mDataList.locationToIndex(e.getPoint());

			if (mDataList.getModel().getElementAt(index) instanceof TVPProgram)
			{
				mDataList.setSelectedIndex(index);
        JPopupMenu popup;
        TVPProgram pearlProgram = (TVPProgram) mDataList.getModel()
            .getElementAt(index);
        Program program = pearlProgram.getProgram();
        if (program != null)
				{
				  popup = Plugin.getPluginManager().createPluginContextMenu(program,
              null);
				}
				else
				{
					final TVPProgram popupProgram = pearlProgram;
					popup = new JPopupMenu();
					JMenuItem item = new JMenuItem(mLocalizer.msg("comment", "TV Pearl comment"));
					item.setIcon(TVPearlPlugin.getInstance().getSmallIcon());
					item.setFont(MenuUtil.CONTEXT_MENU_BOLDFONT);
					item.addActionListener(new ActionListener()
					{
						public void actionPerformed(ActionEvent arg0)
						{
							if (popupProgram != null)
							{
								TVPearlPlugin.getInstance().showPearlInfo(popupProgram);
							}
						}
					});
					popup.add(item);
					item = new JMenuItem(mLocalizer.msg("menu.repetitions",
              "Search repetitions"), Plugin.getPluginManager()
              .getIconFromTheme(TVPearlPlugin.getInstance(), "action",
                  "system-search", 16));
          item.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
              final SearchFormSettings searchSettings = new SearchFormSettings(
                  popupProgram.getTitle());
              searchSettings
                  .setSearcherType(PluginManager.SEARCHER_TYPE_EXACTLY);
              SearchHelper.search(PearlDialog.this, searchSettings);
            }
          });
          popup.add(item);
				}

				final JPopupMenu openPopup = popup;
				SwingUtilities.invokeLater(new Runnable()
				{
					public void run()
					{
						openPopup.show(mDataList, e.getX() - 15, e.getY() - 15);
					}
				});
			}
		}
	}

	/**
	 * show send to plugin dialog
	 */
	private void showSendDialog()
	{
		Object selectedValues[] = mDataList.getSelectedValues();
		if (selectedValues.length == 1)
		{
			if (selectedValues[0] instanceof TVPProgram)
			{
				TVPProgram p = (TVPProgram) selectedValues[0];
				if (p.wasFound())
				{
					Program[] programArr = { p.getProgram() };

					SendToPluginDialog send = new SendToPluginDialog(TVPearlPlugin.getInstance(), this, programArr);
					send.setVisible(true);
				}
			}
		}
	}

	public void updateProgramList()
	{
		Calendar now = Calendar.getInstance();
		int index = -1;

		mProgramList.clear();
		for (TVPProgram item : TVPearlPlugin.getInstance().getProgramList())
		{
			mProgramList.addElement(item);
			if (index == -1)
			{
				final Program program = item.getProgram();
				// find next pearl or first still running program
				if ((now.compareTo(item.getStart()) < 0) || (program != null && !program.isExpired()))
				{
					index = mProgramList.size() - 1;
				}
			}
		}
		if (mUpdateBn != null)
		{
			mUpdateBn.setEnabled(true);
		}
		mDataList.revalidate();
		mDataList.repaint();
		if (mProgramList.getSize() > 0)
		{
			mDataList.setSelectedIndex(0);
			if (mProgramList.getSize() > index)
			{
				mDataList.setSelectedIndex(index);
				mDataList.ensureIndexIsVisible(index);
			}
		}
	}

	public void close()
	{
		dispose();
	}

	public void update()
	{
		mDataList.repaint();
	}
}
