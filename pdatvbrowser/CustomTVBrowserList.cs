using System;
using System.Drawing;
using System.Windows.Forms;
using System.Drawing.Imaging;


namespace TVBrowserMini
{
    /// <summary>
    /// List for the broadcasts
    /// </summary>
    public class CustomTVBrowserList : TVBrowserList
    {
        const int DRAW_OFFSET = 4;
        private ImageList imageList = null; //for future icons...
        private bool wrapText = false;
        private SizeF currentScreen;
        ImageAttributes imageAttr = new ImageAttributes();

        public bool WrapText
        {
            get
            {
                return wrapText;
            }
            set
            {
                wrapText = value;
            }
        }

        public CustomTVBrowserList(SizeF currentScreen)
        {
            this.currentScreen = currentScreen;
            this.ShowScrollbar = true;
            this.ForeColor = Color.Black;
            Graphics g = this.CreateGraphics();
            SizeF size = g.MeasureString("G", this.Font);
            this.ItemHeight = (int)size.Height;
            this.SCROLL_WIDTH = (int)size.Height;
            g.Dispose();
        }


        public ImageList ImageList
        {
            get
            {
                return imageList;
            }
            set
            {
                imageList = value;
            }
        }

        protected override void OnDrawItem(object sender, DrawItemEventArgs e)
        {
            //this.Font = new Font(this.Font.Name.ToString(), this.Font.Size - 1, this.Font.Style);
            Brush textBrush; //Brush for the text
            Rectangle rc = e.Bounds;
            rc.X += DRAW_OFFSET;
            try
            {
                if (e.Index < this.Items.Count)
                {
                    ListItem item = (ListItem)this.Items[e.Index];
                    //Check if the item has a image
                    if (item.ImageIndex > -1)
                    {
                        Image img = imageList.Images[item.ImageIndex];
                        if (img != null)
                        {
                            imageAttr = new ImageAttributes();
                            //Set the transparency key
                            imageAttr.SetColorKey(BackgroundImageColor(img), BackgroundImageColor(img));
                            //imageAttr.SetColorKey(Color.White, Color.White);
                            //Image's rectangle
                            Rectangle imgRect = new Rectangle(2, rc.Y + 1, img.Width, img.Height);
                            //Draw the image
                            e.Graphics.DrawImage(img, imgRect, 0, 0, img.Width, img.Height, GraphicsUnit.Pixel, imageAttr);
                            //Shift the text to the right
                            rc.X += img.Width + 2;
                        }
                    }
                    if (e.State == DrawItemState.Selected)
                    {
                        //Highlighted
                        e.DrawBackground();
                        textBrush = new SolidBrush(SystemColors.HighlightText);
                    }
                    else
                    {
                        if (item.BackColor2 != Color.Empty)
                        {
                            e.DrawBackground(item.BackColor, item.BackColor2, item.BackgroundPercent);
                        }
                        else if (item.BackColor != Color.Empty)
                        {
                            e.DrawBackground(item.BackColor, item.BackgroundPercent);
                        }
                        else
                        {
                            e.DrawBackground(this.BackColor, item.BackgroundPercent);
                        }
                        if (item.ForeColor != Color.Empty)
                            textBrush = new SolidBrush(item.ForeColor);
                        else
                            textBrush = new SolidBrush(e.ForeColor);
                        if (item.ItemFont != null)
                        {
                            //TODO
                        }
                    }
                    e.Graphics.DrawString(item.Text, e.Font, textBrush, rc);
                    e.Graphics.DrawLine(new Pen(Color.Navy), 0, e.Bounds.Bottom, e.Bounds.Width, e.Bounds.Bottom);
                    base.OnDrawItem(sender, e);
                }
            }
            catch
            {
                //too many results?
                Application.Exit();
            }
        }

        private Color BackgroundImageColor(Image image)
        {
            Bitmap bmp = new Bitmap(image);
            Color ret = bmp.GetPixel(0, 0);
            return ret;
        }
    }


    //---------------------------------------------------------------------------------------------------


    public class ListItem
    {
        private Color forecolor = Color.Empty;
        private Color backcolor = Color.Empty;
        private Color backcolor2 = Color.Empty;
        private int backgroundPercent = 100;
        private Font itemFont = null;    //new System.Drawing.Font("Tahoma", 11F, System.Drawing.FontStyle.Bold);
        private string text = "";
        private int imageIndex = -1;

        public ListItem(string text, Color forecolor, Color backcolor, Color backcolor2, int percent)
        {
            this.text = text;
            this.imageIndex = -1;
            this.forecolor = forecolor;
            this.backcolor = backcolor;
            this.backcolor2 = backcolor2;
            this.backgroundPercent = percent;
        }

        public ListItem()
        {
            this.text = "";
            this.imageIndex = -1;
            this.forecolor = Color.Empty;
            this.backcolor = Color.Empty;
        }

        public string Text
        {
            get
            {
                return text;
            }
            set
            {
                text = value;
            }
        }

        public int ImageIndex
        {
            get
            {
                return imageIndex;
            }
            set
            {
                imageIndex = value;
            }
        }

        public Color ForeColor
        {
            get
            {
                return forecolor;
            }
            set
            {
                forecolor = value;
            }
        }

        public Color BackColor
        {
            get
            {
                return backcolor;
            }
            set
            {
                backcolor = value;
            }
        }

        public Color BackColor2
        {
            get
            {
                return backcolor2;
            }
            set
            {
                backcolor2 = value;
            }
        }

        public Font ItemFont
        {
            get
            {
                return itemFont;
            }
            set
            {
                itemFont = value;
            }
        }

        public int BackgroundPercent
        {
            get
            {
                return backgroundPercent;
            }
            set
            {
                backgroundPercent = value;
            }
        }
    }
}
