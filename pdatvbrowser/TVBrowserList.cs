using System;
using System.Drawing;
using System.Collections;
using System.Windows.Forms;
using System.Threading;

namespace PocketTVBrowserCF2
{
	public delegate void DrawItemEventHandler(object sender, DrawItemEventArgs e);
	#region DrawItemState enum
	public enum DrawItemState
	{
		None = 0,
		Selected = 1,
		Disabled = 4,
		Focus = 16
	}
	#endregion
	#region DrawItemEventArgs

	public class DrawItemEventArgs : System.EventArgs
	{
		private Color backColor;
		private Color foreColor;
		private Font font;
		private int index;
		private Graphics graphics;
		private Rectangle rect;
		private DrawItemState state;
		
		public DrawItemEventArgs(System.Drawing.Graphics graphics, System.Drawing.Font font , System.Drawing.Rectangle rect , System.Int32 index , DrawItemState state , System.Drawing.Color foreColor , System.Drawing.Color backColor )
		{
			this.graphics = graphics;
			this.font = font;
			this.rect = rect;
			this.index = index;
			this.state = state;
			this.foreColor = foreColor;
			this.backColor = backColor;
		}
		
		public DrawItemEventArgs(System.Drawing.Graphics graphics, System.Drawing.Font font , System.Drawing.Rectangle rect , System.Int32 index , DrawItemState state )
		{
			this.graphics = graphics;
			this.font = font;
			this.rect = rect;
			this.index = index;
			this.state = state;
			this.foreColor = SystemColors.ControlText;
			this.backColor = SystemColors.Window;
		}
		
		public Rectangle Bounds 
		{
			get 
			{
				return rect;
			}
		}

		public virtual void DrawBackground()
		{
			Brush brush;
			if (state == DrawItemState.Selected)
			{
				brush = new SolidBrush(SystemColors.Highlight);
			}
			else
			{
				brush = new SolidBrush(backColor);
			}		
			Rectangle rc = new Rectangle(rect.X + 1, rect.Y, rect.Width, rect.Height - 1);
			rc.Y+=1;
			graphics.FillRectangle(brush, rc);
		}

		public virtual void DrawBackground(Color color, int pixel)
		{
			Brush brush;
			brush = new SolidBrush(color);
            Rectangle rc = new Rectangle(rect.X + 1, rect.Y, pixel, rect.Height - 1);
            rc.Y+=1;
			graphics.FillRectangle(brush, rc);
		}

        public virtual void DrawBackground(Color color1, Color color2, int pixel)
        {
            Brush brush1, brush2;
            brush1 = new SolidBrush(color1);
            brush2 = new SolidBrush(color2);
            Rectangle rc1 = new Rectangle(rect.X + 1, rect.Y, rect.Width, rect.Height - 1);
            Rectangle rc2 = new Rectangle(rect.X + 1, rect.Y, pixel, rect.Height - 1);
            rc1.Y += 1;
            rc2.Y += 1;
            graphics.FillRectangle(brush1, rc1);
            graphics.FillRectangle(brush2, rc2);
        }


		public virtual void DrawFocusRectangle()
		{
			Rectangle focusRect = rect;
			focusRect.Width--;
			focusRect.Inflate(-1, 0);
			graphics.DrawRectangle(new Pen(SystemColors.Highlight), focusRect);
		}

		public DrawItemState State
		{
			get{return state;}
		}

		public Graphics Graphics
		{
			get{return graphics;}
		}

		public int Index
		{
			get{return index;}
		}

		public System.Drawing.Font Font
		{
			get{return font;}
		}

		public Color BackColor
		{
			get{return backColor;}
		}

		public Color ForeColor
		{
			get{return foreColor;}
            
		}

	}

	#endregion

	#region OwnerDrawnList

	public abstract class TVBrowserList : Control
	{
		public event DrawItemEventHandler DrawItem;
		public event EventHandler SelectedIndexChanged;
		private ArrayList listItems;
		private int itemHeight = 14;
		private int selectedIndex = -1;
		protected int SCROLL_WIDTH = 20;
		private Bitmap m_bmpOffscreen;
		private int topIndex;
		private int itemWidth;
		private bool showScrollbar;
        private bool clicked;
		private ScrollBar vScroll;
        private ContextMenu contextmenu;

		public TVBrowserList()
		{
			listItems = new ArrayList();
			vScroll = new VScrollBar();
			vScroll.Hide();
			vScroll.ValueChanged+=new EventHandler(vScrollcroll_ValueChanged);
			this.Controls.Add(vScroll);
			SCROLL_WIDTH = vScroll.Width;
			itemWidth = this.Width;
		}

		public bool ShowScrollbar
		{
			get
			{
				return showScrollbar;
			}
			set
			{
				showScrollbar = value;
				vScroll.Visible = showScrollbar;
			}
		}

		public int TopIndex
		{
			get
			{
				return topIndex;
			}
			set
			{
				topIndex = value;
				this.Invalidate();
			}
		}

        public ScrollBar VScroll
        {
            get
            {
                return vScroll;
            }
        }

		public ArrayList Items
		{
			get
			{
				return listItems;
			}
		}

		protected virtual int ItemHeight
		{
			get
			{
				return this.itemHeight;
			}
			set
			{
				this.itemHeight = value;
			}
		}

		protected virtual void OnSelectedIndexChanged(EventArgs e)
		{
			if(this.SelectedIndexChanged != null)
				this.SelectedIndexChanged(this, e);
		}

        

		protected virtual void OnDrawItem(object sender, DrawItemEventArgs e)
		{
			if (this.DrawItem != null)
				this.DrawItem(sender, e);
		}

		protected override void OnResize(EventArgs e)
		{
            try
            {
                if (!this.showScrollbar)
                {
                    this.m_bmpOffscreen = new Bitmap(this.ClientSize.Width, this.ClientSize.Height);
                }
                else
                {
                    this.Height = ((int)this.Height / itemHeight) * itemHeight;
                    int viewableItemCount = this.ClientSize.Height / this.ItemHeight;
                    this.vScroll.Bounds = new Rectangle(this.ClientSize.Width - SCROLL_WIDTH, 0, SCROLL_WIDTH, this.ClientSize.Height);
                    // scrollbars needed?
                    if (this.listItems.Count > viewableItemCount)
                    {
                        this.vScroll.Visible = true;
                        this.vScroll.LargeChange = viewableItemCount;
                        this.m_bmpOffscreen = new Bitmap(this.ClientSize.Width - SCROLL_WIDTH, this.ClientSize.Height);
                    }
                    else
                    {
                        this.vScroll.Visible = false;
                        this.vScroll.LargeChange = this.listItems.Count;
                        this.m_bmpOffscreen = new Bitmap(this.ClientSize.Width, this.ClientSize.Height);
                    }
                    this.vScroll.Maximum = this.listItems.Count - 1;
                }
            }
            catch
            {
                Thread.Sleep(3000);
                this.OnResize(e);
            }
		}

		protected override void OnPaintBackground(System.Windows.Forms.PaintEventArgs e )
		{
			//Do nothing
		}

        
        public override ContextMenu ContextMenu
        {
            get
            {
                return this.contextmenu;
            }
            set
            {
                this.contextmenu = value;
            }
        }

		protected override void OnMouseDown(MouseEventArgs e)
		{
            if (clicked)
            {
                clicked = false;
            }
            else
            {
                clicked = true;
            }

            if (listItems.Count == 0)
                return;
            int prevSelection = selectedIndex;
            selectedIndex = this.vScroll.Value + (e.Y / this.ItemHeight);
            Graphics gxTemp = this.CreateGraphics();
            if (prevSelection != -1)
                PaintItem(gxTemp, prevSelection);
            PaintItem(gxTemp, selectedIndex);
            DrawBorder(gxTemp);
            //this.Focus();
		}
        

        protected override void OnDoubleClick(EventArgs e)
        {
            this.contextmenu.Show(this, new Point(10,10));
            base.OnDoubleClick(e);
        }

		/// <summary>
		/// Gets or sets the zero-based index of the currently selected item in a OwnerDrawnList.  
		/// </summary>
		public int SelectedIndex
		{
			get
			{
				return this.selectedIndex;
			}
			set
			{
				this.selectedIndex = value;
				if (this.SelectedIndexChanged != null)
					this.SelectedIndexChanged(this, EventArgs.Empty);
				this.Invalidate();
			}
		}

		public void EnsureVisible(int index)
		{
			if(index < this.vScroll.Value)
			{
				this.vScroll.Value = index;
				this.Refresh();
			}
			else if(index >= this.vScroll.Value + this.DrawCount)
			{
				this.vScroll.Value = index - this.DrawCount + 1;
				this.Refresh();
			}
		}

		protected override void OnKeyDown(KeyEventArgs e)
		{
			switch(e.KeyCode)
			{
				case Keys.Down:
					if(this.SelectedIndex < this.vScroll.Maximum)
					{
						EnsureVisible(++this.SelectedIndex);
						this.Refresh();
					}
					break;
				case Keys.Up:
					if(this.SelectedIndex > this.vScroll.Minimum)
					{
						EnsureVisible(--this.SelectedIndex);
						this.Refresh();
					}
					break;
				case Keys.PageDown:
					this.SelectedIndex = Math.Min(this.vScroll.Maximum, this.SelectedIndex + this.DrawCount);
					EnsureVisible(this.SelectedIndex);
					this.Refresh();
					break;
				case Keys.PageUp:
					this.SelectedIndex = Math.Max(this.vScroll.Minimum, this.SelectedIndex - this.DrawCount);
					EnsureVisible(this.SelectedIndex);
					this.Refresh();
					break;
				case Keys.Home:
					this.SelectedIndex = 0;
					EnsureVisible(this.SelectedIndex);
					this.Refresh();
					break;
				case Keys.End:
					this.SelectedIndex = this.listItems.Count - 1;
					EnsureVisible(this.SelectedIndex);
					this.Refresh();
					break;
			}
			base.OnKeyDown(e);
		}

		protected override void OnPaint(System.Windows.Forms.PaintEventArgs e )
		{	
			Graphics gxOff = Graphics.FromImage(m_bmpOffscreen);
			gxOff.Clear(this.BackColor);
			int drawCount = 0;
			if (showScrollbar)
				topIndex = vScroll.Value;
			else
				topIndex = 0;
			if (showScrollbar)
				drawCount = DrawCount;
			else
				drawCount = listItems.Count;
			if (vScroll.Visible)
				itemWidth = this.Width - vScroll.Width;
			else
				itemWidth = this.Width;
			for(int index=topIndex;index<drawCount+topIndex;index++)
			{
				PaintItem(gxOff, index);
			}	
			DrawBorder(gxOff);
			e.Graphics.DrawImage(m_bmpOffscreen, 0, 0);
			gxOff.Dispose();
		}
		
		private void DrawBorder(Graphics gr)
		{
			Rectangle rc = this.ClientRectangle;
			rc.Height--;
			rc.Width--;
			gr.DrawRectangle(new Pen(Color.Black), rc);
		}

		private void PaintItem(Graphics graphics, int Index)
		{
			Rectangle itemRect = new Rectangle(0, (Index-topIndex)*this.itemHeight, itemWidth, this.itemHeight);	
			DrawItemState state;
			if (Index ==  selectedIndex)
				state = DrawItemState.Selected;
			else
				state = DrawItemState.None;
			DrawItemEventArgs drawArgs = new DrawItemEventArgs(graphics, this.Font, itemRect, Index, state);
			OnDrawItem(this, drawArgs);
		}

		protected int DrawCount
		{
			get
			{
				if(this.vScroll.Value + this.vScroll.LargeChange > this.vScroll.Maximum)
					return this.vScroll.Maximum - this.vScroll.Value + 1;
				else
					return this.vScroll.LargeChange;
			}
		}

		private void vScrollcroll_ValueChanged(object sender, EventArgs e)
		{
			this.Refresh();
		}

		protected override void OnParentChanged ( System.EventArgs e )
		{
			if (!showScrollbar)
				this.Height =  listItems.Count * itemHeight;
			else //Adjust the height
			{
				this.Height = ((int)this.Height / itemHeight) * itemHeight; 
			}
		}
	}

	#endregion
}
