namespace TVBrowserMini
{
    partial class Search
    {
        /// <summary>
        /// Erforderliche Designervariable.
        /// </summary>
        private System.ComponentModel.IContainer components = null;
        private System.Windows.Forms.MainMenu mainMenu1;
        private CustomTVBrowserList listViewBroadcasts;

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
            this.mainMenu1 = new System.Windows.Forms.MainMenu();
            this.tbSearch = new System.Windows.Forms.TextBox();
            this.lSearch = new System.Windows.Forms.Label();
            this.bSearch = new System.Windows.Forms.Button();
            this.inputPanel = new Microsoft.WindowsCE.Forms.InputPanel();
            this.comboBoxDates = new System.Windows.Forms.ComboBox();
            this.radioButtonFavorites = new System.Windows.Forms.RadioButton();
            this.radioButtonReminder = new System.Windows.Forms.RadioButton();
            this.checkBoxExact = new System.Windows.Forms.CheckBox();
            this.comboBoxElement = new System.Windows.Forms.ComboBox();
            this.comboBoxChannel = new System.Windows.Forms.ComboBox();
            this.listViewBroadcasts = new CustomTVBrowserList(new System.Drawing.SizeF(this.Width, this.Height));
            this.SuspendLayout();
            // 
            // tbSearch
            // 
            this.tbSearch.Location = new System.Drawing.Point(2, 19);
            this.tbSearch.Name = "tbSearch";
            this.tbSearch.Size = new System.Drawing.Size(145, 21);
            this.tbSearch.TabIndex = 0;
            this.tbSearch.LostFocus += new System.EventHandler(this.tbSearch_TextLostFocus);
            this.tbSearch.GotFocus += new System.EventHandler(this.tbSearch_TextGotFocus);
            // 
            // lSearch
            // 
            this.lSearch.Font = new System.Drawing.Font("Tahoma", 10F, System.Drawing.FontStyle.Bold);
            this.lSearch.Location = new System.Drawing.Point(0, 1);
            this.lSearch.Name = "lSearch";
            this.lSearch.Size = new System.Drawing.Size(240, 16);
            this.lSearch.Text = "Search for any broadcasts";
            this.lSearch.TextAlign = System.Drawing.ContentAlignment.TopCenter;
            // 
            // bSearch
            // 
            this.bSearch.Location = new System.Drawing.Point(149, 19);
            this.bSearch.Name = "bSearch";
            this.bSearch.Size = new System.Drawing.Size(88, 21);
            this.bSearch.TabIndex = 3;
            this.bSearch.Text = "search";
            this.bSearch.Click += new System.EventHandler(this.bSearch_Click);
            // 
            // comboBoxDates
            // 
            this.comboBoxDates.Location = new System.Drawing.Point(149, 42);
            this.comboBoxDates.Name = "comboBoxDates";
            this.comboBoxDates.Size = new System.Drawing.Size(88, 22);
            this.comboBoxDates.TabIndex = 11;
            // 
            // radioButtonFavorites
            // 
            this.radioButtonFavorites.Location = new System.Drawing.Point(2, 91);
            this.radioButtonFavorites.Name = "radioButtonFavorites";
            this.radioButtonFavorites.Size = new System.Drawing.Size(100, 20);
            this.radioButtonFavorites.TabIndex = 13;
            this.radioButtonFavorites.Text = "favorites";
            this.radioButtonFavorites.Click += new System.EventHandler(this.radioButtonFavorites_Click);
            // 
            // radioButtonReminder
            // 
            this.radioButtonReminder.Location = new System.Drawing.Point(145, 91);
            this.radioButtonReminder.Name = "radioButtonReminder";
            this.radioButtonReminder.Size = new System.Drawing.Size(92, 20);
            this.radioButtonReminder.TabIndex = 14;
            this.radioButtonReminder.Text = "reminders";
            this.radioButtonReminder.Click += new System.EventHandler(this.radioButtonReminder_Click);
            // 
            // checkBoxExact
            // 
            this.checkBoxExact.Location = new System.Drawing.Point(145, 66);
            this.checkBoxExact.Name = "checkBoxExact";
            this.checkBoxExact.Size = new System.Drawing.Size(64, 20);
            this.checkBoxExact.TabIndex = 15;
            this.checkBoxExact.Text = "exact?";
            // 
            // comboBoxElement
            // 
            this.comboBoxElement.Items.Add("title");
            this.comboBoxElement.Location = new System.Drawing.Point(2, 42);
            this.comboBoxElement.Name = "comboBoxElement";
            this.comboBoxElement.Size = new System.Drawing.Size(145, 22);
            this.comboBoxElement.TabIndex = 17;
            this.comboBoxElement.SelectedIndexChanged += new System.EventHandler(this.comboBoxElement_SelectedIndexChanged);
            // 
            // comboBoxChannel
            // 
            this.comboBoxChannel.Items.Add("title");
            this.comboBoxChannel.Location = new System.Drawing.Point(2, 66);
            this.comboBoxChannel.Name = "comboBoxChannel";
            this.comboBoxChannel.Size = new System.Drawing.Size(145, 22);
            this.comboBoxChannel.TabIndex = 19;
            // 
            // listViewBroadcasts
            // 
            this.listViewBroadcasts.ImageList = null;
            this.listViewBroadcasts.Location = new System.Drawing.Point(0, 117);
            this.listViewBroadcasts.Name = "listViewBroadcasts";
            this.listViewBroadcasts.ShowScrollbar = true;
            this.listViewBroadcasts.Size = new System.Drawing.Size(240, 151);
            this.listViewBroadcasts.TabIndex = 0;
            this.listViewBroadcasts.TopIndex = 0;
            this.listViewBroadcasts.WrapText = false;
            // 
            // Search
            // 
            this.AutoScaleDimensions = new System.Drawing.SizeF(96F, 96F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Dpi;
            this.AutoScroll = true;
            this.ClientSize = new System.Drawing.Size(240, 268);
            this.Controls.Add(this.comboBoxChannel);
            this.Controls.Add(this.comboBoxElement);
            this.Controls.Add(this.checkBoxExact);
            this.Controls.Add(this.radioButtonReminder);
            this.Controls.Add(this.radioButtonFavorites);
            this.Controls.Add(this.comboBoxDates);
            this.Controls.Add(this.bSearch);
            this.Controls.Add(this.lSearch);
            this.Controls.Add(this.tbSearch);
            this.Controls.Add(this.listViewBroadcasts);
            this.Menu = this.mainMenu1;
            this.MinimizeBox = false;
            this.Name = "Search";
            this.Text = "Search";
            this.ResumeLayout(false);

        }
        #endregion

        private System.Windows.Forms.TextBox tbSearch;
        private System.Windows.Forms.Label lSearch;
        private System.Windows.Forms.Button bSearch;
        private Microsoft.WindowsCE.Forms.InputPanel inputPanel;
        private System.Windows.Forms.ComboBox comboBoxDates;
        private System.Windows.Forms.RadioButton radioButtonFavorites;
        private System.Windows.Forms.RadioButton radioButtonReminder;
        private System.Windows.Forms.CheckBox checkBoxExact;
        private System.Windows.Forms.ComboBox comboBoxElement;
        private System.Windows.Forms.ComboBox comboBoxChannel;
    }
}