namespace PocketTVBrowserCF2
{
    partial class Mainform
    {
        /// <summary>
        /// Erforderliche Designervariable.
        /// </summary>
        private System.ComponentModel.IContainer components = null;
        private System.Windows.Forms.MainMenu mainMenu;

        /// <summary>
        /// Verwendete Ressourcen bereinigen.
        /// </summary>
        /// <param name="disposing">True, wenn verwaltete Ressourcen gelöscht werden sollen; andernfalls False.</param>
        protected override void Dispose(bool disposing)
        {
            if (disposing && (components != null))
            {
                components.Dispose();
            }
            base.Dispose(disposing);
        }

        #region Vom Windows Form-Designer generierter Code

        /// <summary>
        /// Erforderliche Methode für die Designerunterstützung.
        /// Der Inhalt der Methode darf nicht mit dem Code-Editor geändert werden.
        /// </summary>
        private void InitializeComponent()
        {
            this.mainMenu = new System.Windows.Forms.MainMenu();
            this.menuItemMenu = new System.Windows.Forms.MenuItem();
            this.menuItemAbout = new System.Windows.Forms.MenuItem();
            this.menuItemConfiguration = new System.Windows.Forms.MenuItem();
            this.menuItemSearch = new System.Windows.Forms.MenuItem();
            this.menuItemShow = new System.Windows.Forms.MenuItem();
            this.menuItemCurrent = new System.Windows.Forms.MenuItem();
            this.menuItemNext = new System.Windows.Forms.MenuItem();
            this.menuItemPrimetime = new System.Windows.Forms.MenuItem();
            this.menuItemShowFavorites = new System.Windows.Forms.MenuItem();
            this.menuItemShowReminders = new System.Windows.Forms.MenuItem();
            this.menuItemShowBroadcasts = new System.Windows.Forms.MenuItem();
            this.menuItemFilter = new System.Windows.Forms.MenuItem();
            this.menuItemFilterFavorites = new System.Windows.Forms.MenuItem();
            this.menuItemfilterReminders = new System.Windows.Forms.MenuItem();
            this.menuItemFilterTV = new System.Windows.Forms.MenuItem();
            this.menuItemFilterRadio = new System.Windows.Forms.MenuItem();
            this.menuItemRotate = new System.Windows.Forms.MenuItem();
            this.menuItemExit = new System.Windows.Forms.MenuItem();
            this.menuItemRightSoft = new System.Windows.Forms.MenuItem();
            this.lChannel = new System.Windows.Forms.Label();
            this.lDate = new System.Windows.Forms.Label();
            this.comboBoxChannel = new System.Windows.Forms.ComboBox();
            this.comboBoxDate = new System.Windows.Forms.ComboBox();
            this.timerRefreshView = new System.Windows.Forms.Timer();
            this.timerReminder = new System.Windows.Forms.Timer();
            this.contextMenuBroadcasts = new System.Windows.Forms.ContextMenu();
            this.menuItem1 = new System.Windows.Forms.MenuItem();
            this.menuItem2 = new System.Windows.Forms.MenuItem();
            this.SuspendLayout();
            // 
            // mainMenu
            // 
            this.mainMenu.MenuItems.Add(this.menuItemMenu);
            this.mainMenu.MenuItems.Add(this.menuItemRightSoft);
            // 
            // menuItemMenu
            // 
            this.menuItemMenu.MenuItems.Add(this.menuItemAbout);
            this.menuItemMenu.MenuItems.Add(this.menuItemConfiguration);
            this.menuItemMenu.MenuItems.Add(this.menuItemSearch);
            this.menuItemMenu.MenuItems.Add(this.menuItemShow);
            this.menuItemMenu.MenuItems.Add(this.menuItemFilter);
            this.menuItemMenu.MenuItems.Add(this.menuItemRotate);
            this.menuItemMenu.MenuItems.Add(this.menuItemExit);
            this.menuItemMenu.Text = "Menu";
            // 
            // menuItemAbout
            // 
            this.menuItemAbout.Text = "About";
            this.menuItemAbout.Click += new System.EventHandler(this.menuItemAbout_Click);
            // 
            // menuItemConfiguration
            // 
            this.menuItemConfiguration.Text = "Configuration";
            this.menuItemConfiguration.Click += new System.EventHandler(this.menuItemConfiguration_Click);
            // 
            // menuItemSearch
            // 
            this.menuItemSearch.Text = "Search";
            this.menuItemSearch.Click += new System.EventHandler(this.menuItemSearch_Click);
            // 
            // menuItemShow
            // 
            this.menuItemShow.MenuItems.Add(this.menuItemCurrent);
            this.menuItemShow.MenuItems.Add(this.menuItemNext);
            this.menuItemShow.MenuItems.Add(this.menuItemPrimetime);
            this.menuItemShow.MenuItems.Add(this.menuItemShowFavorites);
            this.menuItemShow.MenuItems.Add(this.menuItemShowReminders);
            this.menuItemShow.MenuItems.Add(this.menuItemShowBroadcasts);
            this.menuItemShow.Text = "Show...";
            // 
            // menuItemCurrent
            // 
            this.menuItemCurrent.Text = "...current broadcasts";
            this.menuItemCurrent.Click += new System.EventHandler(this.menuItemCurrent_Click);
            // 
            // menuItemNext
            // 
            this.menuItemNext.Text = "...next broadcasts";
            this.menuItemNext.Click += new System.EventHandler(this.menuItemNext_Click);
            // 
            // menuItemPrimetime
            // 
            this.menuItemPrimetime.Text = "...Primetime broadcasts";
            this.menuItemPrimetime.Click += new System.EventHandler(this.menuItemPrimetime_Click);
            // 
            // menuItemShowFavorites
            // 
            this.menuItemShowFavorites.Text = "...favorites";
            this.menuItemShowFavorites.Click += new System.EventHandler(this.menuItemShowFavorites_Click);
            // 
            // menuItemShowReminders
            // 
            this.menuItemShowReminders.Text = "...reminder";
            this.menuItemShowReminders.Click += new System.EventHandler(this.menuItemShowReminders_Click);
            // 
            // menuItemShowBroadcasts
            // 
            this.menuItemShowBroadcasts.Text = "...broadcasts running at";
            this.menuItemShowBroadcasts.Click += new System.EventHandler(this.menuItemShowBroadcasts_Click);
            // 
            // menuItemFilter
            // 
            this.menuItemFilter.MenuItems.Add(this.menuItemFilterFavorites);
            this.menuItemFilter.MenuItems.Add(this.menuItemfilterReminders);
            this.menuItemFilter.MenuItems.Add(this.menuItemFilterTV);
            this.menuItemFilter.MenuItems.Add(this.menuItemFilterRadio);
            this.menuItemFilter.Text = "Filter";
            // 
            // menuItemFilterFavorites
            // 
            this.menuItemFilterFavorites.Text = "favorites";
            this.menuItemFilterFavorites.Click += new System.EventHandler(this.menuItemFilterFavorites_Click);
            // 
            // menuItemfilterReminders
            // 
            this.menuItemfilterReminders.Text = "reminders";
            this.menuItemfilterReminders.Click += new System.EventHandler(this.menuItemfilterReminders_Click);
            // 
            // menuItemFilterTV
            // 
            this.menuItemFilterTV.Checked = true;
            this.menuItemFilterTV.Text = "tv";
            this.menuItemFilterTV.Click += new System.EventHandler(this.menuItemFilterTV_Click);
            // 
            // menuItemFilterRadio
            // 
            this.menuItemFilterRadio.Checked = true;
            this.menuItemFilterRadio.Text = "radio";
            this.menuItemFilterRadio.Click += new System.EventHandler(this.menuItemFilterRadio_Click);
            // 
            // menuItemRotate
            // 
            this.menuItemRotate.Text = "Rotate";
            this.menuItemRotate.Click += new System.EventHandler(this.menuItemRotate_Click);
            // 
            // menuItemExit
            // 
            this.menuItemExit.Text = "Exit (complete)";
            this.menuItemExit.Click += new System.EventHandler(this.menuItemExit_Click);
            // 
            // menuItemRightSoft
            // 
            this.menuItemRightSoft.Text = "Cur/Next";
            this.menuItemRightSoft.Click += new System.EventHandler(this.menuItemRightSoft_Click);
            // 
            // lChannel
            // 
            this.lChannel.Font = new System.Drawing.Font("Tahoma", 8F, System.Drawing.FontStyle.Bold);
            this.lChannel.Location = new System.Drawing.Point(51, 8);
            this.lChannel.Name = "lChannel";
            this.lChannel.Size = new System.Drawing.Size(132, 13);
            this.lChannel.Text = "labelChannelqpy";
            this.lChannel.TextAlign = System.Drawing.ContentAlignment.TopCenter;
            // 
            // lDate
            // 
            this.lDate.Font = new System.Drawing.Font("Tahoma", 8F, System.Drawing.FontStyle.Regular);
            this.lDate.Location = new System.Drawing.Point(51, 31);
            this.lDate.Name = "lDate";
            this.lDate.Size = new System.Drawing.Size(132, 13);
            this.lDate.Text = "labelDatepqy";
            this.lDate.TextAlign = System.Drawing.ContentAlignment.TopCenter;
            // 
            // comboBoxChannel
            // 
            this.comboBoxChannel.Location = new System.Drawing.Point(0, 246);
            this.comboBoxChannel.Name = "comboBoxChannel";
            this.comboBoxChannel.Size = new System.Drawing.Size(82, 22);
            this.comboBoxChannel.TabIndex = 3;
            this.comboBoxChannel.SelectedIndexChanged += new System.EventHandler(this.comboBoxChannel_SelectedIndexChanged);
            this.comboBoxChannel.KeyDown += new System.Windows.Forms.KeyEventHandler(this.comboBoxChannel_KeyDown);
            // 
            // comboBoxDate
            // 
            this.comboBoxDate.Location = new System.Drawing.Point(147, 246);
            this.comboBoxDate.Name = "comboBoxDate";
            this.comboBoxDate.Size = new System.Drawing.Size(93, 22);
            this.comboBoxDate.TabIndex = 4;
            this.comboBoxDate.SelectedIndexChanged += new System.EventHandler(this.comboBoxDate_SelectedIndexChanged);
            this.comboBoxDate.KeyDown += new System.Windows.Forms.KeyEventHandler(this.comboBoxDate_KeyDown);
            // 
            // timerRefreshView
            // 
            this.timerRefreshView.Interval = 30000;
            this.timerRefreshView.Tick += new System.EventHandler(this.timerRefreshView_Tick);
            // 
            // timerReminder
            // 
            this.timerReminder.Interval = 20000;
            this.timerReminder.Tick += new System.EventHandler(this.timerReminder_Tick);
            // 
            // contextMenuBroadcasts
            // 
            this.contextMenuBroadcasts.MenuItems.Add(this.menuItem1);
            this.contextMenuBroadcasts.MenuItems.Add(this.menuItem2);
            this.contextMenuBroadcasts.Popup += new System.EventHandler(this.contextMenuBroadcasts_Popup);
            // 
            // menuItem1
            // 
            this.menuItem1.Text = "öffnen";
            // 
            // menuItem2
            // 
            this.menuItem2.Text = "erinnern";
            // 
            // Mainform
            // 
            this.AutoScaleDimensions = new System.Drawing.SizeF(96F, 96F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Dpi;
            this.AutoScroll = true;
            this.ClientSize = new System.Drawing.Size(240, 268);
            this.Controls.Add(this.comboBoxDate);
            this.Controls.Add(this.comboBoxChannel);
            this.Controls.Add(this.lDate);
            this.Controls.Add(this.lChannel);
            this.Menu = this.mainMenu;
            this.Name = "Mainform";
            this.Text = "PocketTVBrowser";
            this.ResumeLayout(false);

        }


        #endregion

        private System.Windows.Forms.Label lChannel;
        private System.Windows.Forms.Label lDate;
        private System.Windows.Forms.ComboBox comboBoxChannel;
        private System.Windows.Forms.ComboBox comboBoxDate;
        private CustomTVBrowserList listViewBroadcasts;
        private System.Windows.Forms.MenuItem menuItemMenu;
        private System.Windows.Forms.MenuItem menuItemRightSoft;
        private System.Windows.Forms.MenuItem menuItemAbout;
        private System.Windows.Forms.MenuItem menuItemConfiguration;
        private System.Windows.Forms.MenuItem menuItemSearch;
        private System.Windows.Forms.MenuItem menuItemShow;
        private System.Windows.Forms.MenuItem menuItemFilter;
        private System.Windows.Forms.MenuItem menuItemExit;
        private System.Windows.Forms.MenuItem menuItemCurrent;
        private System.Windows.Forms.MenuItem menuItemNext;
        private System.Windows.Forms.MenuItem menuItemPrimetime;
        private System.Windows.Forms.MenuItem menuItemShowFavorites;
        private System.Windows.Forms.MenuItem menuItemShowReminders;
        private System.Windows.Forms.MenuItem menuItemShowBroadcasts;
        private System.Windows.Forms.MenuItem menuItemRotate;
        private System.Windows.Forms.MenuItem menuItemFilterFavorites;
        private System.Windows.Forms.MenuItem menuItemfilterReminders;
        private System.Windows.Forms.MenuItem menuItemFilterTV;
        private System.Windows.Forms.MenuItem menuItemFilterRadio;
        private System.Windows.Forms.Timer timerRefreshView;
        private System.Windows.Forms.Timer timerReminder;
        private System.Windows.Forms.ContextMenu contextMenuBroadcasts;
        private System.Windows.Forms.MenuItem menuItem1;
        private System.Windows.Forms.MenuItem menuItem2;
    }
}

