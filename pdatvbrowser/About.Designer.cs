namespace PocketTVBrowserCF2
{
    partial class About
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
            System.ComponentModel.ComponentResourceManager resources = new System.ComponentModel.ComponentResourceManager(typeof(About));
            this.mainMenu1 = new System.Windows.Forms.MainMenu();
            this.lVersion = new System.Windows.Forms.Label();
            this.pictureBoxIcon = new System.Windows.Forms.PictureBox();
            this.labelThanks = new System.Windows.Forms.Label();
            this.textBoxThanksTo = new System.Windows.Forms.TextBox();
            this.lCopyright = new System.Windows.Forms.Label();
            this.lGNU = new System.Windows.Forms.Label();
            this.SuspendLayout();
            // 
            // lVersion
            // 
            this.lVersion.Font = new System.Drawing.Font("Tahoma", 12F, System.Drawing.FontStyle.Bold);
            this.lVersion.Location = new System.Drawing.Point(0, 0);
            this.lVersion.Name = "lVersion";
            this.lVersion.Size = new System.Drawing.Size(240, 19);
            this.lVersion.Text = "Version: 0.4.9.2 BETA";
            this.lVersion.TextAlign = System.Drawing.ContentAlignment.TopCenter;
            // 
            // pictureBoxIcon
            // 
            this.pictureBoxIcon.Image = ((System.Drawing.Image)(resources.GetObject("pictureBoxIcon.Image")));
            this.pictureBoxIcon.Location = new System.Drawing.Point(3, 25);
            this.pictureBoxIcon.Name = "pictureBoxIcon";
            this.pictureBoxIcon.Size = new System.Drawing.Size(131, 131);
            this.pictureBoxIcon.SizeMode = System.Windows.Forms.PictureBoxSizeMode.StretchImage;
            // 
            // labelThanks
            // 
            this.labelThanks.Location = new System.Drawing.Point(0, 159);
            this.labelThanks.Name = "labelThanks";
            this.labelThanks.Size = new System.Drawing.Size(237, 16);
            this.labelThanks.Text = "Special thanks to:";
            // 
            // textBoxThanksTo
            // 
            this.textBoxThanksTo.AcceptsReturn = true;
            this.textBoxThanksTo.BorderStyle = System.Windows.Forms.BorderStyle.None;
            this.textBoxThanksTo.Location = new System.Drawing.Point(3, 176);
            this.textBoxThanksTo.Multiline = true;
            this.textBoxThanksTo.Name = "textBoxThanksTo";
            this.textBoxThanksTo.ScrollBars = System.Windows.Forms.ScrollBars.Vertical;
            this.textBoxThanksTo.Size = new System.Drawing.Size(234, 54);
            this.textBoxThanksTo.TabIndex = 14;
            this.textBoxThanksTo.Text = "the SQLite team for the database, the testers (especially Thilo Steinert, Mario S" +
                "iegmann, Bernd Frey, Jan Schütte,  DJ Bone) and Ihmchen for the BussyMix ;-)";
            // 
            // lCopyright
            // 
            this.lCopyright.Font = new System.Drawing.Font("Tahoma", 8F, System.Drawing.FontStyle.Regular);
            this.lCopyright.Location = new System.Drawing.Point(0, 247);
            this.lCopyright.Name = "lCopyright";
            this.lCopyright.Size = new System.Drawing.Size(240, 18);
            this.lCopyright.Text = "(c) tvbrowser.org / benedikt.grabenmeier.com";
            this.lCopyright.TextAlign = System.Drawing.ContentAlignment.TopCenter;
            // 
            // lGNU
            // 
            this.lGNU.Location = new System.Drawing.Point(0, 231);
            this.lGNU.Name = "lGNU";
            this.lGNU.Size = new System.Drawing.Size(240, 15);
            this.lGNU.Text = "created by Benedikt Grabenmeier";
            this.lGNU.TextAlign = System.Drawing.ContentAlignment.TopCenter;
            // 
            // About
            // 
            this.AutoScaleDimensions = new System.Drawing.SizeF(96F, 96F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Dpi;
            this.AutoScroll = true;
            this.ClientSize = new System.Drawing.Size(240, 268);
            this.Controls.Add(this.labelThanks);
            this.Controls.Add(this.textBoxThanksTo);
            this.Controls.Add(this.lCopyright);
            this.Controls.Add(this.lGNU);
            this.Controls.Add(this.pictureBoxIcon);
            this.Controls.Add(this.lVersion);
            this.Menu = this.mainMenu1;
            this.MinimizeBox = false;
            this.Name = "About";
            this.Text = "About";
            this.ResumeLayout(false);

        }

        #endregion

        private System.Windows.Forms.Label lVersion;
        private System.Windows.Forms.PictureBox pictureBoxIcon;
        private System.Windows.Forms.Label labelThanks;
        private System.Windows.Forms.TextBox textBoxThanksTo;
        private System.Windows.Forms.Label lCopyright;
        private System.Windows.Forms.Label lGNU;
    }
}