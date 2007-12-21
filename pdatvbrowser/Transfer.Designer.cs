namespace TVBrowserMini
{
    partial class Transfer
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
            this.mainMenu1 = new System.Windows.Forms.MainMenu();
            this.labelHeadline = new System.Windows.Forms.Label();
            this.labelPercent = new System.Windows.Forms.Label();
            this.SuspendLayout();
            // 
            // labelHeadline
            // 
            this.labelHeadline.Font = new System.Drawing.Font("Tahoma", 9F, System.Drawing.FontStyle.Bold);
            this.labelHeadline.Location = new System.Drawing.Point(3, 5);
            this.labelHeadline.Name = "labelHeadline";
            this.labelHeadline.Size = new System.Drawing.Size(194, 17);
            this.labelHeadline.Text = "TV-Data synchronisation";
            this.labelHeadline.TextAlign = System.Drawing.ContentAlignment.TopCenter;
            // 
            // labelPercent
            // 
            this.labelPercent.Location = new System.Drawing.Point(5, 27);
            this.labelPercent.Name = "labelPercent";
            this.labelPercent.Size = new System.Drawing.Size(191, 40);
            this.labelPercent.TextAlign = System.Drawing.ContentAlignment.TopCenter;
            // 
            // Transfer
            // 
            this.AutoScaleDimensions = new System.Drawing.SizeF(96F, 96F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Dpi;
            this.AutoScroll = true;
            this.BackColor = System.Drawing.Color.FromArgb(((int)(((byte)(192)))), ((int)(((byte)(224)))), ((int)(((byte)(255)))));
            this.ClientSize = new System.Drawing.Size(200, 70);
            this.Controls.Add(this.labelPercent);
            this.Controls.Add(this.labelHeadline);
            this.FormBorderStyle = System.Windows.Forms.FormBorderStyle.None;
            this.Name = "Transfer";
            this.Text = "Transfer";
            this.ResumeLayout(false);

        }

        #endregion

        private System.Windows.Forms.Label labelHeadline;
        private System.Windows.Forms.Label labelPercent;
    }
}