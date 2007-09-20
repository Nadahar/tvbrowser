namespace PocketTVBrowserCF2
{
    partial class Details
    {
        /// <summary>
        /// Erforderliche Designervariable.
        /// </summary>
        private System.ComponentModel.IContainer components = null;
        private System.Windows.Forms.MainMenu mainMenu1;

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
            System.ComponentModel.ComponentResourceManager resources = new System.ComponentModel.ComponentResourceManager(typeof(Details));
            this.mainMenu1 = new System.Windows.Forms.MainMenu();
            this.menuItemMenu = new System.Windows.Forms.MenuItem();
            this.menuItemSearch = new System.Windows.Forms.MenuItem();
            this.menuItemOutlook = new System.Windows.Forms.MenuItem();
            this.menuItemReminder = new System.Windows.Forms.MenuItem();
            this.menuItemRotate = new System.Windows.Forms.MenuItem();
            this.menuItemOK = new System.Windows.Forms.MenuItem();
            this.pictureBoxReminder = new System.Windows.Forms.PictureBox();
            this.pictureBoxBookmark = new System.Windows.Forms.PictureBox();
            this.menuItemSMS = new System.Windows.Forms.MenuItem();
            this.SuspendLayout();
            // 
            // mainMenu1
            // 
            this.mainMenu1.MenuItems.Add(this.menuItemMenu);
            this.mainMenu1.MenuItems.Add(this.menuItemOK);
            // 
            // menuItemMenu
            // 
            this.menuItemMenu.MenuItems.Add(this.menuItemSearch);
            this.menuItemMenu.MenuItems.Add(this.menuItemOutlook);
            this.menuItemMenu.MenuItems.Add(this.menuItemReminder);
            this.menuItemMenu.MenuItems.Add(this.menuItemSMS);
            this.menuItemMenu.MenuItems.Add(this.menuItemRotate);
            this.menuItemMenu.Text = "Menu";
            // 
            // menuItemSearch
            // 
            this.menuItemSearch.Text = "Search similar";
            this.menuItemSearch.Click += new System.EventHandler(this.menuItemSearch_Click);
            // 
            // menuItemOutlook
            // 
            this.menuItemOutlook.Text = "Add to Calendar (WM5)";
            this.menuItemOutlook.Click += new System.EventHandler(this.menuItemOutlook_Click);
            // 
            // menuItemReminder
            // 
            this.menuItemReminder.Text = "Add to reminders";
            this.menuItemReminder.Click += new System.EventHandler(this.menuItemReminder_Click);
            // 
            // menuItemRotate
            // 
            this.menuItemRotate.Text = "Rotate";
            this.menuItemRotate.Click += new System.EventHandler(this.menuItemRotate_Click);
            // 
            // menuItemOK
            // 
            this.menuItemOK.Text = "OK";
            this.menuItemOK.Click += new System.EventHandler(this.menuItemOK_Click);
            // 
            // pictureBoxReminder
            // 
            this.pictureBoxReminder.Image = ((System.Drawing.Image)(resources.GetObject("pictureBoxReminder.Image")));
            this.pictureBoxReminder.Location = new System.Drawing.Point(31, 243);
            this.pictureBoxReminder.Name = "pictureBoxReminder";
            this.pictureBoxReminder.Size = new System.Drawing.Size(22, 22);
            this.pictureBoxReminder.SizeMode = System.Windows.Forms.PictureBoxSizeMode.StretchImage;
            this.pictureBoxReminder.Visible = false;
            // 
            // pictureBoxBookmark
            // 
            this.pictureBoxBookmark.Image = ((System.Drawing.Image)(resources.GetObject("pictureBoxBookmark.Image")));
            this.pictureBoxBookmark.Location = new System.Drawing.Point(3, 243);
            this.pictureBoxBookmark.Name = "pictureBoxBookmark";
            this.pictureBoxBookmark.Size = new System.Drawing.Size(22, 22);
            this.pictureBoxBookmark.SizeMode = System.Windows.Forms.PictureBoxSizeMode.StretchImage;
            this.pictureBoxBookmark.Visible = false;
            // 
            // menuItemSMS
            // 
            this.menuItemSMS.Text = "send as sms";
            this.menuItemSMS.Click += new System.EventHandler(this.menuItemSMS_Click);
            // 
            // Details
            // 
            this.AutoScaleDimensions = new System.Drawing.SizeF(96F, 96F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Dpi;
            this.AutoScroll = true;
            this.ClientSize = new System.Drawing.Size(240, 268);
            this.Controls.Add(this.pictureBoxReminder);
            this.Controls.Add(this.pictureBoxBookmark);
            this.Menu = this.mainMenu1;
            this.Name = "Details";
            this.Text = "PocketTVBrowser";
            this.ResumeLayout(false);

        }

        #endregion

        private System.Windows.Forms.MenuItem menuItemMenu;
        private System.Windows.Forms.MenuItem menuItemSearch;
        private System.Windows.Forms.MenuItem menuItemOutlook;
        private System.Windows.Forms.MenuItem menuItemReminder;
        private System.Windows.Forms.MenuItem menuItemOK;
        private System.Windows.Forms.PictureBox pictureBoxReminder;
        private System.Windows.Forms.PictureBox pictureBoxBookmark;
        private System.Windows.Forms.MenuItem menuItemRotate;
        private System.Windows.Forms.MenuItem menuItemSMS;
    }
}