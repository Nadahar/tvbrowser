using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Collections;
using System.Data;
using System.Drawing;
using System.Text;
using System.Windows.Forms;
using Microsoft.WindowsCE.Forms;
using System.Threading;
using Microsoft.WindowsMobile.PocketOutlook;


namespace TVBrowserMini
{
    public partial class Mainform : Form
    {
        private TVBrowserControll con;
        private ArrayList broadcasts;
        private bool refresh = true;
        private int currentBroadcast = 0;
        private Reminder reminderForm;
        private ScreenOrientation startOrientation;
        private DateTime dtSeatchAt;
        int scrollTo = 0;
        int comboboxChannelIndex = 0;
        int comboboxDateIndex = 0;
       
        public Mainform()
        {
            Cursor.Current = Cursors.WaitCursor;
            this.con = new TVBrowserControll(this);
            this.con.setRefreshReminders(true);
            this.listViewBroadcasts = new CustomTVBrowserList(new System.Drawing.SizeF(this.Width, this.Height));
            // 
            // listViewBroadcasts
            // 
            InitializeComponent();
            this.listViewBroadcasts.Location = new System.Drawing.Point(0, 61);
            this.listViewBroadcasts.Name = "listViewBroadcasts";
            this.listViewBroadcasts.Size = new System.Drawing.Size(240, 142);
            this.listViewBroadcasts.TabIndex = 5;
            this.listViewBroadcasts.Click += new System.EventHandler(this.listViewBroadcasts_Click);
            this.listViewBroadcasts.KeyDown += new KeyEventHandler(listViewBroadcasts_KeyDown);
            this.listViewBroadcasts.ContextMenu = this.contextMenuBroadcasts;
            
            this.Controls.Add(this.listViewBroadcasts);

            this.disableElements();
            this.Closing += new System.ComponentModel.CancelEventHandler(this.Mainform_Closing);
            this.initVideoMode();
            this.refreshLanguage();
            this.showErrorMessages();
            this.fillChannels();
            if (this.con.getCurrentChannel() != null)
            {
                try
                {
                    this.comboBoxChannel.SelectedItem = this.con.getCurrentChannel();
                    if (this.con.isStartAsCurrent())
                    {
                        this.menuItemCurrent_Click(null, null);
                    }
                }
                catch
                {
                    this.comboBoxChannel.SelectedIndex = 0;
                }
            }
            if (!this.con.hasDB())
            {
                DialogResult dr = MessageBox.Show(this.con.getLanguageElement("Mainform.Directory", "Please specify the path to your tvdata.tvd (Menu - Configuration)"), this.con.getLanguageElement("Mainform.Directory.Warning", "WARNING - No TV-Data"));
            }
            this.reminderForm = new Reminder(this.con);
            timerReminder.Enabled = true;
            timerRefreshView.Enabled = true;
            timerRefreshView.Interval = this.con.getRefrehTime() * 1000;
            this.comboBoxChannel.Focus();
            this.startOrientation = SystemSettings.ScreenOrientation;
            this.dtSeatchAt = new DateTime();
            Cursor.Current = Cursors.Default;
        }


        private void disableElements()
        {
            int osVersion = Int32.Parse(Environment.OSVersion.Version.ToString().Split('.')[0].ToString());
            int osVersionSub = 0;
            try
            {
                osVersionSub = Int32.Parse(Environment.OSVersion.Version.ToString().Split('.')[1].ToString());
            }
            catch
            {
            }
            int h = this.Height;
            int w = this.Width;
            if (w < h)
            {
                int t = w;
                w = h;
                h = t;
            }
            if ((w == 240 && h == 188) || (w == 480 && h == 376))
            {
                this.menuItemRotate.Enabled = false;
            }
            else if (osVersion < 5)
            {
                if (osVersion == 4 && osVersionSub >= 21)
                {
                }
                else
                {
                    this.menuItemRotate.Enabled = false;
                }
            }
        }

        private void initVideoMode()
        {
            this.con.setCurrentScreen(this.CurrentAutoScaleDimensions);
            this.lChannel.Bounds = new Rectangle(0, 0, this.Width, this.lChannel.Height);
            this.lDate.Bounds = new Rectangle(0, this.lChannel.Height, this.Width, this.lDate.Height);
            this.comboBoxChannel.Bounds = new Rectangle(0, this.Height-this.comboBoxChannel.Height, this.Width * 3 / 5, this.comboBoxChannel.Height);
            this.comboBoxDate.Bounds = new Rectangle(this.comboBoxChannel.Width-1, this.Height - this.comboBoxDate.Height, (this.Width * 2 / 5)+1, this.comboBoxDate.Height);
            this.labelQickViewChannel.Width = this.Width * 80 / 100;
            this.labelQuickViewDate.Width = this.Width * 80 / 100;
            this.panelQuickView.Bounds = new Rectangle(this.Width * 10 / 100, this.Height - this.labelQickViewChannel.Height - this.labelQuickViewDate.Height-this.comboBoxChannel.Height-10, this.Width * 80 / 100, this.labelQickViewChannel.Height + this.labelQuickViewDate.Height+5);
            try
            {
                this.listViewBroadcasts.Bounds = new Rectangle(0, this.lDate.Location.Y + this.lDate.Height, this.Width, (this.Height - this.lDate.Height - this.lChannel.Height - this.comboBoxChannel.Height+5));
            }
            catch
            {
                this.listViewBroadcasts.Bounds = new Rectangle(0, this.lDate.Location.Y + this.lDate.Height, this.Width, (this.Height - this.lDate.Height - this.lChannel.Height - this.comboBoxChannel.Height));
            }
        }

        protected override void OnResize(EventArgs e)
        {
            this.initVideoMode();
            if (this.listViewBroadcasts.SelectedIndex > -1)
            {
                this.listViewBroadcasts.EnsureVisible(this.listViewBroadcasts.SelectedIndex);
            }
            else
            {
                this.scrollToCurrent();
            }
            try
            {
                this.listViewBroadcasts.Items.Clear();
                for (int i = 0; i < this.broadcasts.Count; i++)
                {
                    Broadcast temp = (Broadcast)this.broadcasts[i];
                    if (this.con.getLastView() == 5)
                    {
                        this.listViewBroadcasts.Items.Add(this.con.createColoredListItemForSearchRunningAt(this.dtSeatchAt, temp, this.getReducedString(temp.getChannel() + "|" + temp.getStart().ToShortTimeString() + "|" + temp.getTitle(),this.Width)));
                    }
                    else
                    {
                        if (this.con.getLastView() == 2 || this.con.getLastView() == 3)
                        {
                            this.listViewBroadcasts.Items.Add(this.con.createColoredListItem(temp, this.getReducedString(temp.getChannel() + "|" + temp.getTitle() + " (" + temp.getStart().ToShortTimeString() + ")", this.Width)));
                        }
                        else if (this.con.getLastView() == 4 || this.con.getLastView() == 6 || this.con.getLastView() == 7)
                        {
                            this.listViewBroadcasts.Items.Add(this.con.createColoredListItem(temp, this.getReducedString(temp.getChannel() + "|" + temp.getStart().ToShortTimeString() + "|" + temp.getTitle(), this.Width)));
                        }
                        else
                        {
                            this.listViewBroadcasts.Items.Add(this.con.createColoredListItem(temp, this.getReducedString(temp.ToString(), this.Width)));
                        }
                    }
                }
            }
            catch
            {
            }
        }

        private void rotate()
        {
            try
            {
                Cursor.Current = Cursors.WaitCursor;
                if (SystemSettings.ScreenOrientation == ScreenOrientation.Angle0)
                {
                    // change to landscape
                    SystemSettings.ScreenOrientation = ScreenOrientation.Angle270;
                }
                else
                {
                    // change to portrait
                    SystemSettings.ScreenOrientation = ScreenOrientation.Angle0;
                }
                this.initVideoMode();
                this.Invalidate();
                this.Update();
                Cursor.Current = Cursors.Default;
            }
            catch
            {
                MessageBox.Show("was't able so rotate screen", "Mainform.RotateError");
                this.menuItemRotate.Enabled = false;
            }
        }

        private void menuItemRotate_Click(object sender, EventArgs e)
        {
            this.rotate();
        }

        private void comboBoxChannel_SelectedIndexChanged(object sender, EventArgs e)
        {
            if (!this.con.hasDB())
            {
                this.con.checkDBExists();
            }
            if (this.comboBoxChannel.SelectedIndex > 0)
            {
                this.timerQuickView.Enabled = false;
                this.comboboxChannelIndex = this.comboBoxChannel.SelectedIndex;

                this.con.setCurrentChannel((Channel)this.comboBoxChannel.SelectedItem);
                this.lChannel.Text = this.con.getCurrentChannel().ToString();
                this.comboBoxDate.Items.Clear();
                this.listViewBroadcasts.Items.Clear();
                this.fillDates();
                int select = 0;
                if (this.comboBoxDate.Items.Count > 0)
                {
                    for (int i = 0; i < this.comboBoxDate.Items.Count; i++)
                    {
                        DateTime temp = ((TVBrowserDate)this.comboBoxDate.Items[i]).getDateTime();
                        if (temp.ToShortDateString().Equals(this.con.getCurrentDate().ToShortDateString()))
                        {
                            select = i;
                            break;
                        }
                    }
                    this.comboBoxDate.SelectedIndex = select;
                }
                else
                {
                    this.lDate.Text = this.con.getLanguageElement("Mainform.NoDate", "no date");
                }
                this.listViewBroadcasts.Refresh();
            }
        }

        private void comboBoxDate_SelectedIndexChanged(object sender, EventArgs e)
        {
            if (refresh && this.comboBoxChannel.SelectedIndex > 0)
            {
                this.timerQuickView.Enabled = false;
                this.comboboxDateIndex = this.comboBoxDate.SelectedIndex;
                DateTime dt = ((TVBrowserDate)this.comboBoxDate.SelectedItem).getDateTime();
                this.con.setCurrentDate(dt);
                this.lDate.Text = dt.ToLongDateString();
                this.lChannel.Text = ((Channel)this.comboBoxChannel.SelectedItem).getName();
                this.fillListView();
                this.listViewBroadcasts.Refresh();
            }
        }

        public void refreshListView()
        {
            this.listViewBroadcasts.Dispose();
            this.listViewBroadcasts = new CustomTVBrowserList(new System.Drawing.SizeF(this.Width, this.Height));
            this.listViewBroadcasts.ImageList = null;
            this.listViewBroadcasts.Name = "listViewBroadcasts";
            this.listViewBroadcasts.ShowScrollbar = true;
            this.listViewBroadcasts.TabIndex = 0;
            this.listViewBroadcasts.TopIndex = 0;
            this.listViewBroadcasts.WrapText = false;
            this.listViewBroadcasts.Click += new System.EventHandler(this.listViewBroadcasts_Click);
            this.listViewBroadcasts.KeyDown += new KeyEventHandler(listViewBroadcasts_KeyDown);
            this.listViewBroadcasts.ContextMenu = this.contextMenuBroadcasts;
            this.initVideoMode();
        }


        private void fillListView()
        {
            if (this.con.hasDB())
            {
                this.refreshListView();
                scrollTo = 0;
                this.broadcasts = new ArrayList();
                DateTime selectedDate = ((TVBrowserDate)this.comboBoxDate.SelectedItem).getDateTime();
                broadcasts = this.con.getBroadcasts(this.con.getCurrentChannel().getId(), selectedDate);
                DateTime now = DateTime.Now;
                for (int i = 0; i < broadcasts.Count; i++)
                {
                    Broadcast temp = (Broadcast)broadcasts[i];
                    if (temp.getStart() <= now && now <= temp.getEnd())
                    {
                        this.currentBroadcast = i;
                        scrollTo = i;
                    }
                    this.listViewBroadcasts.Items.Add(this.con.createColoredListItem(temp, this.getReducedString(temp.ToString(),this.Width)));
                }
                this.Controls.Add(this.listViewBroadcasts);
                this.scrollToCurrent(); 
             }
        }


      

        private void listViewBroadcasts_Click(object sender, EventArgs e)
        {
            this.timerContextMenu.Enabled = true;
            this.panelContext.Visible = true;
            
            try
            {
                if (this.con.hasDB())
                {
                    Broadcast temp = (Broadcast)this.broadcasts[this.listViewBroadcasts.SelectedIndex];
                    Cursor.Current = Cursors.WaitCursor;
                    Details d = new Details(temp, this.con);
                    Cursor.Current = Cursors.Default;
                    d.Show();
                    d.BringToFront();
                }
                else
                {
                    if (this.con.checkDBExists())
                        listViewBroadcasts_Click(sender, e);
                    else
                        MessageBox.Show(this.con.getLanguageElement("Mainform.Directory", "Please specify the path to your tvdata.tvd (Menu - Configuration)"), this.con.getLanguageElement("Mainform.Directory.Warning", "WARNING - No TV-Data"));
                }
           
            }
            catch
            {
                //Nothing - user just klicked in a free area of the screen
            }
            this.panelContext.Visible = false;
            this.timerContextMenu.Enabled = false;
        }


        public void setComboBoxChannelIndex(int index)
        {
            this.comboBoxChannel.SelectedIndex = index;
        }

        private void menuItemExit_Click(object sender, EventArgs e)
        {
            try
            {
                Cursor.Current = Cursors.WaitCursor;
                this.con.saveLastSettings();
                SystemSettings.ScreenOrientation = this.startOrientation;
                Cursor.Current = Cursors.Default;
                this.con.killThread();
                Application.Exit();
                this.Close();
            }
            catch (Exception ex)
            {
                String dummy = ex.Message;
            }
        }

        public void fillChannels()
        {
            this.comboBoxChannel.Items.Clear();
            if (this.con.hasDB())
            {
               
                this.comboBoxChannel.Items.Add(this.con.getLanguageElement("Mainform.SelectChannel", "-select a channel-"));
                for (int i = 0; i < this.con.getChannels().Count; i++)
                {
                    Channel temp = (Channel)this.con.getChannels()[i];
                    this.comboBoxChannel.Items.Add(temp);
                }
            }
        }

        private void fillDates()
        {
            if (this.con.hasDB())
            {
                ArrayList dates = this.con.getDates(this.con.getCurrentChannel().getId());
                for (int i = 0; i < dates.Count; i++)
                {
                    this.comboBoxDate.Items.Add(dates[i]);
                }
            }
        }

        void comboBoxDate_KeyDown(object sender, KeyEventArgs e)
        {
            this.OnKeyDown(e);
        }

        void comboBoxChannel_KeyDown(object sender, KeyEventArgs e)
        {
            this.OnKeyDown(e);
        }

        void listViewBroadcasts_KeyDown(object sender, KeyEventArgs e)
        {
            if (e.KeyValue == 37)
            {
                this.comboBoxChannel.Focus();
                this.listViewBroadcasts.SelectedIndex = -1;
                e.Handled = true;
            }
            else if (e.KeyValue == 39)
            {
                this.comboBoxChannel.Focus();
                this.listViewBroadcasts.SelectedIndex = -1;
                e.Handled = true;
            }
            else if (e.KeyValue == 13)
            {
                try
                {
                    Broadcast temp = (Broadcast)this.broadcasts[this.listViewBroadcasts.SelectedIndex];
                    Cursor.Current = Cursors.WaitCursor;
                    Details d = new Details(temp, this.con);
                    Cursor.Current = Cursors.Default;
                    d.Show();
                    d.BringToFront();
                    e.Handled = true;
                }
                catch
                {
                }
            }
        }


        protected override void OnKeyDown(KeyEventArgs e)
        {
            if (e.KeyValue == 40) //down
            {
                this.panelQuickView.Visible = true;
                this.timerQuickView.Enabled = false;
                this.timerQuickView.Enabled = true;
                if (comboboxChannelIndex+1 > 0 && this.comboboxChannelIndex+1 < this.comboBoxChannel.Items.Count)
                {
                    this.comboboxChannelIndex++;
                    this.labelQickViewChannel.Text = this.comboBoxChannel.Items[this.comboboxChannelIndex].ToString();
                }
                if (this.comboBoxDate.Items.Count > 0)
                    this.labelQuickViewDate.Text = this.comboBoxDate.Items[this.comboboxDateIndex].ToString();
                else
                    this.labelQuickViewDate.Text = "";
                e.Handled = true;
            }
            else if (e.KeyValue == 38) //up
            {
                this.panelQuickView.Visible = true;
                this.timerQuickView.Enabled = false;
                this.timerQuickView.Enabled = true;
                
                if (comboboxChannelIndex-1 > 0 && this.comboboxChannelIndex-1 < this.comboBoxChannel.Items.Count)
                {
                    this.comboboxChannelIndex--;
                    this.labelQickViewChannel.Text = this.comboBoxChannel.Items[this.comboboxChannelIndex].ToString();
                }
                if (this.comboBoxDate.Items.Count>0)
                    this.labelQuickViewDate.Text = this.comboBoxDate.Items[this.comboboxDateIndex].ToString();
                else
                    this.labelQuickViewDate.Text = "";
                e.Handled = true;
            }
            else if (e.KeyValue == 37) //left
            {

                this.panelQuickView.Visible = true;
                this.timerQuickView.Enabled = false;
                this.timerQuickView.Enabled = true;

                if (comboboxDateIndex - 1 > -1 && this.comboboxDateIndex - 1 < this.comboBoxDate.Items.Count)
                {
                    this.comboboxDateIndex--;
                    this.labelQuickViewDate.Text = this.comboBoxDate.Items[this.comboboxDateIndex].ToString();
                }
                e.Handled = true;
            }
            else if (e.KeyValue == 39) //right
            {
                this.panelQuickView.Visible = true;
                this.timerQuickView.Enabled = false;
                this.timerQuickView.Enabled = true;

                if (comboboxDateIndex + 1 > 0 && this.comboboxDateIndex + 1 < this.comboBoxDate.Items.Count)
                {
                    this.comboboxDateIndex++;
                    this.labelQuickViewDate.Text = this.comboBoxDate.Items[this.comboboxDateIndex].ToString();
                }
                if (this.comboBoxDate.Items.Count > 0)
                    this.labelQickViewChannel.Text = this.comboBoxChannel.Items[this.comboboxChannelIndex].ToString();
                e.Handled = true;
            }
            else if (e.KeyValue == 13)//fire
            {
                //scroll to current broadcast
                try
                {
                    if (((TVBrowserDate)this.comboBoxDate.SelectedItem).ToLongDateString().Equals(DateTime.Now.ToLongDateString()) && this.con.getLastView() == 1)
                    {
                        this.listViewBroadcasts.SelectedIndex = this.currentBroadcast;
                    }
                    else
                    {
                        this.listViewBroadcasts.SelectedIndex = 0;
                        this.listViewBroadcasts.EnsureVisible(this.listViewBroadcasts.SelectedIndex);
                    }
                }
                catch
                {
                    this.comboBoxChannel.Focus();
                }

                this.listViewBroadcasts.Focus();
                e.Handled = true;
            }
            else
            {
                base.OnKeyDown(e);
            }
        }

        private void menuItemAbout_Click(object sender, EventArgs e)
        {
            About about = new About(this.con);
            about.Show();
            about.BringToFront();
        }

        private void menuItemCurrent_Click(object sender, EventArgs e)
        {
            if (this.con.hasDB())
            {
                this.comboBoxChannel.SelectedIndex = 0;
                this.currentBroadcast = 0;
                this.refresh = false;
                this.lChannel.Text = this.con.getLanguageElement("Mainform.CurrentBroadcasts", "Current broadcasts");
                for (int i = 0; i < this.comboBoxDate.Items.Count; i++)
                {
                    DateTime temp = ((TVBrowserDate)this.comboBoxDate.Items[i]).getDateTime();
                    if (temp.ToShortDateString().Equals(DateTime.Now.ToShortDateString()))
                    {
                        this.comboBoxDate.SelectedIndex = i;
                        break;
                    }
                }
                this.lDate.Text = DateTime.Now.ToLongDateString() + " " + DateTime.Now.ToShortTimeString();

                this.refreshListView();

                Cursor.Current = Cursors.WaitCursor;
                this.broadcasts = this.con.getCurrentBroadcasts();
                Cursor.Current = Cursors.Default;
                for (int i = 0; i < broadcasts.Count; i++)
                {
                    Broadcast temp = (Broadcast)broadcasts[i];
                    this.listViewBroadcasts.Items.Add(this.con.createColoredListItem(temp, this.getReducedString(temp.getChannel() + "|" + temp.getTitle() + " (" + temp.getStart().ToShortTimeString() + ")",this.Width)));
                }
                this.Controls.Add(this.listViewBroadcasts);
                this.refresh = true;
            }
            else
            {
                if (this.con.checkDBExists())
                    listViewBroadcasts_Click(sender, e);
                else
                    MessageBox.Show(this.con.getLanguageElement("Mainform.Directory", "Please specify the path to your tvdata.tvd (Menu - Configuration)"), this.con.getLanguageElement("Mainform.Directory.Warning", "WARNING - No TV-Data"));
            }
        }

        private void menuItemNext_Click(object sender, EventArgs e)
        {
            if (this.con.hasDB())
            {
                this.currentBroadcast = 0;
                this.refresh = false;
                this.lChannel.Text = this.con.getLanguageElement("Mainform.NextBroadcasts", "Next broadcasts");
                this.lDate.Text = DateTime.Now.ToLongDateString() + " " + DateTime.Now.ToShortTimeString();
                this.refreshListView();
                Cursor.Current = Cursors.WaitCursor;
                this.broadcasts = this.con.getNextBroadcasts();
                Cursor.Current = Cursors.Default;
                for (int i = 0; i < broadcasts.Count; i++)
                {
                    Broadcast temp = (Broadcast)broadcasts[i];
                    this.listViewBroadcasts.Items.Add(this.con.createColoredListItem(temp, this.getReducedString(temp.getChannel() + "|" + temp.getTitle() + " (" + temp.getStart().ToShortTimeString() + ")",this.Width)));
                }
                this.Controls.Add(this.listViewBroadcasts);
                this.refresh = true;
            }
            else
            {
                if (this.con.checkDBExists())
                    listViewBroadcasts_Click(sender, e);
                else
                    MessageBox.Show(this.con.getLanguageElement("Mainform.Directory", "Please specify the path to your tvdata.tvd (Menu - Configuration)"), this.con.getLanguageElement("Mainform.Directory.Warning", "WARNING - No TV-Data"));
            }
        }

        public void refreshView(String element)
        {
            if (element.Equals("reminder"))
            {
                this.menuItemfilterReminders_Click(null, null);
            }
        }

        private void menuItemFilterFavorites_Click(object sender, EventArgs e)
        {
            if (this.con.hasDB())
            {
                if (this.con.getFilterFavorites())
                {
                    this.con.setFilterFavorites(false);
                    this.menuItemFilterFavorites.Checked = false;
                    this.fillListView();
                }
                else
                {
                    this.con.setFilterFavorites(true);
                    this.menuItemFilterFavorites.Checked = true;
                    this.fillListView();
                }
            }
            else
            {
                if (this.con.checkDBExists())
                    listViewBroadcasts_Click(sender, e);
                else
                    MessageBox.Show(this.con.getLanguageElement("Mainform.Directory", "Please specify the path to your tvdata.tvd (Menu - Configuration)"), this.con.getLanguageElement("Mainform.Directory.Warning", "WARNING - No TV-Data"));
            }
        }

        private void menuItemfilterReminders_Click(object sender, EventArgs e)
        {
            if (this.con.hasDB())
            {
                if (this.con.getFilterReminders())
                {
                    this.con.setFilterReminders(false);
                    this.menuItemfilterReminders.Checked = false;
                    this.fillListView();
                }
                else
                {
                    this.con.setFilterReminders(true);
                    this.menuItemfilterReminders.Checked = true;
                    this.fillListView();
                }
            }
            else
            {
                if (this.con.checkDBExists())
                    listViewBroadcasts_Click(sender, e);
                else
                    MessageBox.Show(this.con.getLanguageElement("Mainform.Directory", "Please specify the path to your tvdata.tvd (Menu - Configuration)"), this.con.getLanguageElement("Mainform.Directory.Warning", "WARNING - No TV-Data"));
            }
        }

        private void menuItemFilterRadio_Click(object sender, EventArgs e)
        {
            if (this.con.hasDB())
            {
                if (this.menuItemFilterRadio.Checked)
                {
                    this.menuItemFilterRadio.Checked = false;
                    this.con.setShowRadio(false);
                }
                else
                {
                    this.menuItemFilterRadio.Checked = true;
                    this.con.setShowRadio(true);
                }
                this.con.readChannels();
                this.fillChannels();
                this.comboBoxChannel.SelectedIndex = 0;
            }
            else
            {
                if (this.con.checkDBExists())
                    listViewBroadcasts_Click(sender, e);
                else
                    MessageBox.Show(this.con.getLanguageElement("Mainform.Directory", "Please specify the path to your tvdata.tvd (Menu - Configuration)"), this.con.getLanguageElement("Mainform.Directory.Warning", "WARNING - No TV-Data"));
            }
        }

        private void menuItemFilterTV_Click(object sender, EventArgs e)
        {
            if (this.con.hasDB())
            {
                if (this.menuItemFilterTV.Checked)
                {
                    this.menuItemFilterTV.Checked = false;
                    this.con.setShowTV(false);
                }
                else
                {
                    this.menuItemFilterTV.Checked = true;
                    this.con.setShowTV(true);
                }
                this.con.readChannels();
                this.fillChannels();
                this.comboBoxChannel.SelectedIndex = 0;
            }
            else
            {
                if (this.con.checkDBExists())
                    listViewBroadcasts_Click(sender, e);
                else
                    MessageBox.Show(this.con.getLanguageElement("Mainform.Directory", "Please specify the path to your tvdata.tvd (Menu - Configuration)"), this.con.getLanguageElement("Mainform.Directory.Warning", "WARNING - No TV-Data"));
            }
        }

        private void menuItemShowBroadcasts_Click(object sender, EventArgs e)
        {
            if (this.con.hasDB())
            {
                this.currentBroadcast = 0;
                SearchStartingAt ssa = new SearchStartingAt(this.con, this);
                DialogResult result = ssa.ShowDialog();
                if (result == DialogResult.OK)
                {
                    this.comboBoxChannel.SelectedIndex = 0;
                    this.refresh = false;
                    this.lChannel.Text = this.con.getLanguageElement("Mainform.BroadcastsRunningAt", "Broadcasts running at");
                    for (int i = 0; i < this.comboBoxDate.Items.Count; i++)
                    {
                        DateTime temp = ((TVBrowserDate)this.comboBoxDate.Items[i]).getDateTime();
                        if (temp.ToShortDateString().Equals(ssa.getDateTime().ToShortDateString()))
                        {
                            this.comboBoxDate.SelectedIndex = i;
                            break;
                        }
                    }
                    this.dtSeatchAt = ssa.getDateTime();
                    this.lDate.Text = ssa.getDateTime().ToLongDateString() + " " + ssa.getDateTime().ToShortTimeString();
                    this.refreshListView();
                    this.broadcasts = ssa.getBroadcasts();
                    for (int i = 0; i < ssa.getBroadcasts().Count; i++)
                    {
                        Broadcast temp = (Broadcast)ssa.getBroadcasts()[i];
                        this.listViewBroadcasts.Items.Add(this.con.createColoredListItemForSearchRunningAt(ssa.getDateTime(), temp, this.getReducedString(temp.getChannel() + "|" + temp.getStart().ToShortTimeString() + "|" + temp.getTitle(),this.Width)));
                    }
                    this.Controls.Add(this.listViewBroadcasts);
                    this.refresh = true;
                }
            }
            else
            {
                if (this.con.checkDBExists())
                    listViewBroadcasts_Click(sender, e);
                else
                    MessageBox.Show(this.con.getLanguageElement("Mainform.Directory", "Please specify the path to your tvdata.tvd (Menu - Configuration)"), this.con.getLanguageElement("Mainform.Directory.Warning", "WARNING - No TV-Data"));
            }
        }

        private void menuItemSearch_Click(object sender, EventArgs e)
        {
            if (this.con.hasDB())
            {
                Search search = new Search(this.con);
                search.Show();
                search.BringToFront();
            }
            else
            {
                if (this.con.checkDBExists())
                    listViewBroadcasts_Click(sender, e);
                else
                    MessageBox.Show(this.con.getLanguageElement("Mainform.Directory", "Please specify the path to your tvdata.tvd (Menu - Configuration)"), this.con.getLanguageElement("Mainform.Directory.Warning", "WARNING - No TV-Data"));
            }
        }

        private void menuItemPrimetime_Click(object sender, EventArgs e)
        {
            try
            {
                if (this.con.hasDB())
                {
                    DateTime dt = DateTime.Now;
                    try
                    {
                        dt = ((TVBrowserDate)this.comboBoxDate.SelectedItem).getDateTime();
                    }
                    catch
                    {
                        this.comboBoxDate.Items.Add(new TVBrowserDate(dt, this.con));
                    }
                    this.lChannel.Text = this.con.getLanguageElement("Mainform.PrimeTime", "PrimeTime - broadcasts for");
                    this.lDate.Text = dt.ToLongDateString();

                    this.refreshListView();

                    this.broadcasts = this.con.getPrimetimeBroadcasts(dt);
                    for (int i = 0; i < broadcasts.Count; i++)
                    {
                        Broadcast temp = (Broadcast)broadcasts[i];
                        this.listViewBroadcasts.Items.Add(this.con.createColoredListItem(temp, this.getReducedString(temp.getChannel() + "|" + temp.ToString(), this.Width)));
                    }
                    this.Controls.Add(this.listViewBroadcasts);
                }
                else
                {
                    if (this.con.checkDBExists())
                        listViewBroadcasts_Click(sender, e);
                    else
                        MessageBox.Show(this.con.getLanguageElement("Mainform.Directory", "Please specify the path to your tvdata.tvd (Menu - Configuration)"), this.con.getLanguageElement("Mainform.Directory.Warning", "WARNING - No TV-Data"));
                }
            }
            catch
            {
            }
        }

        private void menuItemConfiguration_Click(object sender, EventArgs e)
        {
            this.comboBoxChannel.Items.Clear();
            Configuration conf = new Configuration(this.con);
            conf.Show();
            conf.BringToFront();
        }

        private void menuItemShowFavorites_Click(object sender, EventArgs e)
        {
            try
            {
                if (this.con.hasDB())
                {
                    this.comboBoxChannel.SelectedIndex = 0;
                    this.con.setLastView(6);
                    this.refresh = false;
                    this.lChannel.Text = this.con.getLanguageElement("Mainform.TodaysFavorites", "favorites");

                    this.refreshListView();
                    TVBrowserDate date = (TVBrowserDate)this.comboBoxDate.SelectedItem;
                    this.lDate.Text = date.ToLongDateString();
                    this.broadcasts = this.con.getBroadcastsByFilter(true, false, date.getDateTime());
                    for (int i = 0; i < broadcasts.Count; i++)
                    {
                        Broadcast temp = (Broadcast)broadcasts[i];
                        this.listViewBroadcasts.Items.Add(this.con.createColoredListItem(temp, this.getReducedString(temp.getChannel() + "|" + temp.ToString(), this.Width)));
                    }
                    this.Controls.Add(this.listViewBroadcasts);
                    this.refresh = true;
                }
                else
                {
                    if (this.con.checkDBExists())
                        listViewBroadcasts_Click(sender, e);
                    else
                        MessageBox.Show(this.con.getLanguageElement("Mainform.Directory", "Please specify the path to your tvdata.tvd (Menu - Configuration)"), this.con.getLanguageElement("Mainform.Directory.Warning", "WARNING - No TV-Data"));
                }
            }
            catch
            {
            }
        }

        private void menuItemShowReminders_Click(object sender, EventArgs e)
        {
            try
            {
                if (this.con.hasDB())
                {
                    this.comboBoxChannel.SelectedIndex = 0;
                    this.con.setLastView(7);
                    this.refresh = false;
                    this.lChannel.Text = this.con.getLanguageElement("Mainform.TodaysReminders", "Todays Reminders");

                    this.refreshListView();
                    TVBrowserDate date = (TVBrowserDate)this.comboBoxDate.SelectedItem;
                    this.lDate.Text = date.ToLongDateString();
                    this.broadcasts = this.con.getBroadcastsByFilter(false, true, date.getDateTime());
                    for (int i = 0; i < broadcasts.Count; i++)
                    {
                        Broadcast temp = (Broadcast)broadcasts[i];
                        this.listViewBroadcasts.Items.Add(this.con.createColoredListItem(temp, this.getReducedString(temp.getChannel() + "|" + temp.ToString(), this.Width)));
                    }
                    this.Controls.Add(this.listViewBroadcasts);
                    this.refresh = true;
                }
                else
                {
                    if (this.con.checkDBExists())
                        listViewBroadcasts_Click(sender, e);
                    else
                        MessageBox.Show(this.con.getLanguageElement("Mainform.Directory", "Please specify the path to your tvdata.tvd (Menu - Configuration)"), this.con.getLanguageElement("Mainform.Directory.Warning", "WARNING - No TV-Data"));
                }
            }
            catch
            {

            }

        }

        private void menuItemRightSoft_Click(object sender, EventArgs e)
        {
            if (this.con.hasDB())
            {
                int scroll = 0;
                int position = 0;
                if (this.con.getLastView() == 2 || this.con.getLastView() == 3)
                {
                    scroll = this.listViewBroadcasts.VScroll.Value;
                    position = this.listViewBroadcasts.SelectedIndex;
                }
                if (this.con.getLastView() == 2) //If last view was "Current"
                {
                    this.menuItemNext_Click(null, null);
                    if (scroll > 0 || position > 0)
                    {
                        try
                        {
                            this.listViewBroadcasts.VScroll.Value = scroll;
                            this.listViewBroadcasts.SelectedIndex = position;
                            this.listViewBroadcasts.Focus();
                        }
                        catch
                        {
                        }
                    }
                }
                else
                {
                    this.menuItemCurrent_Click(null, null);
                    if (scroll > 0 || position > 0)
                    {
                        try
                        {
                            this.listViewBroadcasts.VScroll.Value = scroll;
                            this.listViewBroadcasts.SelectedIndex = position;
                            this.listViewBroadcasts.Focus();
                        }
                        catch
                        {
                        }
                    }
                }
            }
            else
            {
                if (this.con.checkDBExists())
                    listViewBroadcasts_Click(sender, e);
                else
                    MessageBox.Show(this.con.getLanguageElement("Mainform.Directory", "Please specify the path to your tvdata.tvd (Menu - Configuration)"), this.con.getLanguageElement("Mainform.Directory.Warning", "WARNING - No TV-Data"));
            }
        }

        private void timerRefreshView_Tick(object sender, EventArgs e)
        {
            try
            {
                if (this.con.getLastView() == 0)
                {
                }
                else if (this.con.getLastView() == 1)
                {
                    int scroll = this.listViewBroadcasts.VScroll.Value;
                    this.fillListView();
                    try
                    {
                        this.listViewBroadcasts.VScroll.Value = scroll;
                    }
                    catch
                    {
                    }
                }
                else if (this.con.getLastView() == 2)
                {
                    int scroll = this.listViewBroadcasts.VScroll.Value;
                    this.menuItemCurrent_Click(null, null);
                    try
                    {
                        this.listViewBroadcasts.VScroll.Value = scroll;
                    }
                    catch
                    {
                    }
                }
            }
            catch
            {

            }
        }

        public Reminder getReminderForm()
        {
            return this.reminderForm;
        }

        public ComboBox getComboBoxChannel()
        {
            return this.comboBoxChannel;
        }

        public void setListViewBroadcastsSelectedIndex(int index)
        {
            this.listViewBroadcasts.SelectedIndex = index;
        }

        void Mainform_Closing(object sender, CancelEventArgs e)
        {
            try
            {
                this.con.killThread();
                Cursor.Current = Cursors.WaitCursor;
                this.con.saveLastSettings();
                SystemSettings.ScreenOrientation = this.startOrientation;
                Cursor.Current = Cursors.Default;
                Application.Exit();
            }
            catch
            {
                
            }
        }

        private void showErrorMessages()
        {
            for (int i = 0; i < this.con.getErrorMessages().Count; i++)
            {
                ErrorMessage temp = (ErrorMessage) this.con.getErrorMessages()[i];
                DialogResult result = new DialogResult();
                if (temp.getDialogResult() == DialogResult.OK)
                {
                    result = MessageBox.Show(temp.getMessage(), temp.getHeadline());
                }
                if (temp.getDialogResult() == DialogResult.Cancel)
                {
                    result = MessageBox.Show(temp.getMessage(), temp.getHeadline(), MessageBoxButtons.OKCancel, MessageBoxIcon.Question, MessageBoxDefaultButton.Button1);
                }
                if (result == DialogResult.OK)
                {
                    if (!temp.getRun().Equals(""))
                    {
                        if (temp.getRun().Equals("Application.Exit()"))
                        {
                            this.con.killThread();
                            Application.Exit();
                        }
                        if (temp.getRun().Equals("TVBrowserControll.transferReminders()"))
                        {
                            Cursor.Current = Cursors.WaitCursor;
                            this.con.transferReminders();
                            Cursor.Current = Cursors.Default;
                        }
                    }
                }
            }
        }

        public void scrollToCurrent()
        {
            if (this.con.getLastView() == 1 && this.listViewBroadcasts.Items.Count>1)
            {
                this.listViewBroadcasts.EnsureVisible(0);
                try
                {
                    Graphics g = this.CreateGraphics();
                    SizeF size = g.MeasureString("G", this.Font);
                    int subtrakter = 4;
                    int h = this.Height;
                    int w = this.Width;
                    if (w > h)
                    {
                        h = w;
                        subtrakter = 1;
                    }
                    int steps = (int)(this.listViewBroadcasts.Height / (h / 20));
                    steps -= subtrakter;
                    if (scrollTo + steps >= this.listViewBroadcasts.Items.Count)
                    {
                        this.listViewBroadcasts.EnsureVisible(this.listViewBroadcasts.Items.Count);
                    }
                    else
                    {
                        this.listViewBroadcasts.EnsureVisible(scrollTo + steps);
                    }
                }
                catch
                {
                    this.listViewBroadcasts.EnsureVisible(this.scrollTo);
                }
            }
        }

        public void restart()
        {
            try
            {
                this.listViewBroadcasts.Items.Clear();
                this.listViewBroadcasts.ShowScrollbar = false;
                this.listViewBroadcasts.Refresh();
                this.lChannel.Text = "";
                this.lDate.Text = "";
                this.Refresh();
            }
            catch
            {

            }
            if (this.con.checkDBExists())
            {
                this.con.readChannels();
                this.fillChannels();
            }
        }

        private String getReducedString(String original, int width)
        {
            String result = "";
            Graphics g = this.CreateGraphics();
            SizeF size = g.MeasureString(original, this.Font);
            int subrakter = 15;
            if (width >400)
                subrakter = 30;
            if (size.Width > width-subrakter)
            {
                result = original;
                while (g.MeasureString(result, this.Font).Width >= width-subrakter)
                {
                    result = result.Substring(0, result.Length - 1);
                }
                result = result.Substring(0, result.Length - 3);
                result += "...";
            }
            else
            {
                result = original;
            }
            return result;
        }

        private void refreshLanguage()
        {
            this.menuItemAbout.Text = this.con.getLanguageElement("Mainform.About", "About");
            this.menuItemConfiguration.Text = this.con.getLanguageElement("Mainform.Configuration", "Configuration");
            this.menuItemSearch.Text = this.con.getLanguageElement("Mainform.Search", "Search");
            this.menuItemShow.Text = this.con.getLanguageElement("Mainform.Show", "Show...");
            this.menuItemFilter.Text = this.con.getLanguageElement("Mainform.Filter", "Filter");
            this.menuItemCurrent.Text = this.con.getLanguageElement("Mainform.Show.CurrentBroadcast", "...current broadcasts");
            this.menuItemNext.Text = this.con.getLanguageElement("Mainform.Show.NextBroadcast", "...next broadcasts");
            this.menuItemPrimetime.Text = this.con.getLanguageElement("Mainform.Show.PrimetimeBroadcast", "...PrimeTime broadcasts");
            this.menuItemShowBroadcasts.Text = this.con.getLanguageElement("Mainform.Show.BroadcastsRunningAt", "...broadcasts running at");
            this.menuItemShowFavorites.Text = this.con.getLanguageElement("Mainform.Show.TodaysFavorites", "...todays favorites");
            this.menuItemShowReminders.Text = this.con.getLanguageElement("Mainform.Show.TodaysReminder", "...todays reminders");
            this.menuItemFilterFavorites.Text = this.con.getLanguageElement("Mainform.Filter.Favorites", "favorites");
            this.menuItemfilterReminders.Text = this.con.getLanguageElement("Mainform.Filter.Reminders", "reminders");
            this.menuItemFilterTV.Text = this.con.getLanguageElement("Mainform.Filter.TV", "tv");
            this.menuItemRotate.Text = this.con.getLanguageElement("Mainform.Rotate","Rotate");
            this.menuItemFilterRadio.Text = this.con.getLanguageElement("Mainform.Filter.Radio", "radio");
            this.menuItemExit.Text = this.con.getLanguageElement("Mainform.Exit", "Exit (complete)");
            this.menuItemRightSoft.Text = this.con.getLanguageElement("Mainform.CurrentNext", "Cur/Next");
            this.lChannel.Text = this.con.getLanguageElement("Mainform.NoChannelSelected", "no channel selected");
            this.lDate.Text = this.con.getLanguageElement("Mainform.NoDateSelected", "no date selected");
            this.menuItemMenu.Text = this.con.getLanguageElement("Mainform.Menu", "Menu");
            this.Text = this.con.getLanguageElement("Mainform.Text", "PocketTVBrowser");
        }

        private void timerReminder_Tick(object sender, EventArgs e)
        {
            try
            {
                /*OutlookSession os = new OutlookSession();
                AppointmentCollection ac = os.Appointments.Items;
                ac.Sort("Start", false);
                PropertyDescriptor pd = ac.SortProperty;


                Appointment a = ac[0];*/


                if (!this.con.hasDB())
                    this.con.checkDBExists();

                if (this.con.hasDB())
                {
                    if (this.con.isRefreshReminders())
                    {
                        this.reminderForm.updateReminders();
                        this.con.setRefreshReminders(false);
                    }
                    if (this.reminderForm.getNextAlarm() <= DateTime.Now)
                    {
                        try
                        {
                            if (this.con.isPlayReminderSound())
                            {
                                Sound so = new Sound(this.con.getSoundReminder());
                                so.Play();
                                
                                
                            }
                            if (this.con.isPopupReminder())
                            {
                                /*try
                                {
                                    //System.Diagnostics.Process.Start("PowerOn2003.exe", "");
                                 
                                }
                                catch
                                {
                                    String dummy = "";
                                }*/

                                this.reminderForm.DialogResult = DialogResult.OK;
                                this.reminderForm.Refresh();
                                this.reminderForm.createMessage();
                                DialogResult dr = this.reminderForm.ShowDialog();
                                if (dr == DialogResult.OK)
                                {
                                    this.Focus();
                                }
                            }
                        }
                        catch
                        {
                        }
                    }
                    if (this.reminderForm.getLastUpdateDateTime().AddDays(1) < DateTime.Now)
                    {
                        this.con.setRefreshReminders(true);
                    }
                }
            }
            catch
            {

            }
        }

        protected override void OnClick(EventArgs e)
        {
           // IRRemote remote = new IRRemote();
           // remote.ShowDialog();
            base.OnClick(e);
        }


        private void timerQuickView_Tick(object sender, EventArgs e)
        {
            this.panelQuickView.Visible = false;
            if (this.comboboxDateIndex > -1 && this.comboboxDateIndex < this.comboBoxDate.Items.Count)
            {
                this.comboBoxDate.SelectedIndex = comboboxDateIndex;
            }
            if (this.comboboxChannelIndex > 0 && this.comboboxChannelIndex < this.comboBoxChannel.Items.Count)
            {
                this.comboBoxChannel.SelectedIndex = comboboxChannelIndex;
            }

            this.comboboxChannelIndex = this.comboBoxChannel.SelectedIndex;
            this.comboboxDateIndex = this.comboBoxDate.SelectedIndex;
            //this.Refresh();
        }


        void timerContextMenu_Tick(object sender, System.EventArgs e)
        {
            this.panelContext.Visible = true;
            //throw new System.Exception("The method or operation is not implemented.");
        }
    }
}