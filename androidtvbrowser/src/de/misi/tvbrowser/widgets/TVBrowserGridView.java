package de.misi.tvbrowser.widgets;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.*;
import android.util.AttributeSet;
import android.view.*;
import de.misi.tvbrowser.R;
import de.misi.tvbrowser.TVBrowser;
import de.misi.tvbrowser.Utility;
import de.misi.tvbrowser.data.Broadcast;
import de.misi.tvbrowser.data.Channel;

import java.util.ArrayList;
import java.util.Calendar;

public class TVBrowserGridView extends View implements View.OnCreateContextMenuListener, View.OnClickListener {

   public static final int CHANNELNAME_SIZE = 60;
   public static final int MINUTE_WIDTH = 2;
   public static final int TEXTHEIGHT = 14;
   public static final int HORIZONTAL_GAP = 1;
   public static final int VERTICAL_GAP = 1;

   /**
    * The initial state of the touch mode when we enter this view.
    */
   private static final int TOUCH_MODE_INITIAL_STATE = 0;

   /**
    * Indicates we just received the touch event and we are waiting to see if
    * it is a tap or a scroll gesture.
    */
   private static final int TOUCH_MODE_DOWN = 1;

   /**
    * Indicates the touch gesture is a vertical scroll
    */
   private static final int TOUCH_MODE_VSCROLL = 0x20;

   /**
    * Indicates the touch gesture is a horizontal scroll
    */
   private static final int TOUCH_MODE_HSCROLL = 0x40;

   private static DrawConfiguration drawConfiguration = null;

   private TVBrowser parentActivity;
   private ArrayList<Channel> channels = null;
   private Bitmap mChannelNameBitmap = null;
   private Canvas mChannelNameCanvas = null;
   private Bitmap mBitmap = null;
   private Canvas mCanvas = null;
   private Rect mSrcRect = new Rect();
   private Rect mDestRect = new Rect();
   private boolean needRepaint = false;
   private int visibleStartX = 0;
   private int visibleStartY = 0;
   private int scrollStartX = 0;
   private int scrollStartY = 0;
   private ContinueScroll continueScroll = new ContinueScroll();
   private int mTouchMode = TOUCH_MODE_INITIAL_STATE;
   public Channel selectedChannel = null;
   public Broadcast selectedBroadcast = null;

   public TVBrowserGridView(TVBrowser activity) {
      super(activity);
      parentActivity = activity;
      initialize(activity.getResources());
   }

   public TVBrowserGridView(Context context, AttributeSet attributeSet) {
      super(context, attributeSet);
      initialize(context.getResources());
   }

   public TVBrowserGridView(Context context, AttributeSet attributeSet, int i) {
      super(context, attributeSet, i);
      initialize(context.getResources());
   }

   private void initialize(Resources resources) {
      if (drawConfiguration == null) {
         drawConfiguration = new DrawConfiguration();
         drawConfiguration.initialize(resources);
      }
      Calendar now = Calendar.getInstance();
      visibleStartX = Utility.getX(now);
      needRepaint = true;
      mTouchMode = TOUCH_MODE_INITIAL_STATE;
      selectedBroadcast = null;
      setOnCreateContextMenuListener(this);
      setOnClickListener(this);
   }

   public ArrayList<Channel> getChannels() {
      return channels;
   }

   public void setChannels(ArrayList<Channel> channels) {
      this.channels = channels;
      needRepaint = true;
      selectedBroadcast = null;
      invalidate();
   }

   @Override
   protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
      super.onSizeChanged(width, height, oldWidth, oldHeight);
      drawConfiguration.screenWidth = width;
      drawConfiguration.screenHeight = height;
      remeasure(width, height);
   }

   private void remeasure(int width, int height) {
      drawConfiguration.bitmapWidth = width;
      int bitmapHeight = (channels != null ? channels.size() * TEXTHEIGHT + 2 * channels.size() * HORIZONTAL_GAP : height) + TEXTHEIGHT;
      if (bitmapHeight < height)
         bitmapHeight = height;
      drawConfiguration.bitmapHeight = bitmapHeight;
      if (mBitmap != null)
         mBitmap.recycle();
      mBitmap = Bitmap.createBitmap(drawConfiguration.bitmapWidth,
              drawConfiguration.bitmapHeight,
              Bitmap.Config.RGB_565);
      mCanvas = new Canvas(mBitmap);
      if (mChannelNameBitmap != null)
         mChannelNameBitmap.recycle();
      mChannelNameBitmap = Bitmap.createBitmap(CHANNELNAME_SIZE, drawConfiguration.bitmapHeight, Bitmap.Config.RGB_565);
      mChannelNameCanvas = new Canvas(mChannelNameBitmap);
      needRepaint = true;
   }

   @Override
   protected void onDraw(Canvas canvas) {
      boolean doCopyBitmap = true;
      if (needRepaint && mCanvas != null) {
         mSrcRect.left = 0;
         mSrcRect.top = 0;
         mSrcRect.right = drawConfiguration.bitmapWidth;
         mSrcRect.bottom = TEXTHEIGHT + 2 * HORIZONTAL_GAP;
         mCanvas.drawRect(mSrcRect, drawConfiguration.headerBackground);
         mSrcRect.top = mSrcRect.bottom;
         mSrcRect.bottom = drawConfiguration.bitmapHeight;
         mCanvas.drawRect(mSrcRect, drawConfiguration.backgroundPaint);
         mChannelNameCanvas.drawRect(mSrcRect, drawConfiguration.headerBackground);
         Paint textPaint = drawConfiguration.headerTextColor;
         if (channels != null) {
            for (int i = 0; i < 25; i++) {
               int x = i * 60 * MINUTE_WIDTH;
               if (x >= visibleStartX && x < visibleStartX + drawConfiguration.bitmapWidth) {
                  String value = Integer.toString(i);
                  float measure = textPaint.measureText(value);
                  mCanvas.drawText(value, x - measure / 2 - visibleStartX, TEXTHEIGHT + HORIZONTAL_GAP, textPaint);
               }
            }
            int y = TEXTHEIGHT + 2 * HORIZONTAL_GAP;
            int visibleEndX = (visibleStartX + drawConfiguration.bitmapWidth) / MINUTE_WIDTH;
            Calendar now = Calendar.getInstance();
            long nowInMillis = now.getTimeInMillis();
            int nowx = Utility.getX(now);
            for (Channel channel : channels) {
               channel.drawChannelNames(mChannelNameCanvas, drawConfiguration, y);
               channel.drawBroadcasts(mCanvas, drawConfiguration, y, visibleStartX / MINUTE_WIDTH, visibleEndX, nowInMillis, nowx, visibleStartX, selectedBroadcast);
               y += TEXTHEIGHT + 2 * HORIZONTAL_GAP;
            }
         } else {
            doCopyBitmap = false;
            canvas.drawText(getResources().getString(R.string.no_data_available), 0, 0, textPaint);
         }
         needRepaint = false;
      }
      if (doCopyBitmap)
         copyBitmap(canvas);
   }

   private void copyBitmap(Canvas canvas) {
      if (mBitmap != null) {
         Rect src = mSrcRect;
         Rect dest = mDestRect;

         src.top = visibleStartY;
         src.bottom = visibleStartY + drawConfiguration.screenHeight;
         src.left = 0;
         src.right = drawConfiguration.screenWidth - CHANNELNAME_SIZE;

         dest.top = 0;
         dest.bottom = drawConfiguration.screenHeight;
         dest.left = 0;
         dest.right = drawConfiguration.screenWidth;

         canvas.save();
         canvas.clipRect(dest);
         canvas.drawColor(0, PorterDuff.Mode.CLEAR);
         dest.left = CHANNELNAME_SIZE;
         canvas.drawBitmap(mBitmap, src, dest, null);
         dest.left = 0;
         dest.right = CHANNELNAME_SIZE;
         src.left = 0;
         src.right = CHANNELNAME_SIZE;
         canvas.drawBitmap(mChannelNameBitmap, src, dest, null);
         canvas.restore();
      }
   }

   public void doScroll(MotionEvent motionEvent1, MotionEvent motionEvent2, float deltaX, float deltaY) {
      int distanceX = (int) motionEvent1.getX() - (int) motionEvent2.getX();
      int distanceY = (int) motionEvent1.getY() - (int) motionEvent2.getY();

      if (mTouchMode == TOUCH_MODE_DOWN) {
         int absDistanceX = Math.abs(distanceX);
         int absDistanceY = Math.abs(distanceY);
         scrollStartX = visibleStartX;
         scrollStartY = visibleStartY;
         if (absDistanceY >= 2 * absDistanceX) {
            mTouchMode = TOUCH_MODE_VSCROLL;
         } else {
            mTouchMode = TOUCH_MODE_HSCROLL;
         }
      }
      if ((mTouchMode & TOUCH_MODE_VSCROLL) != 0) {
         visibleStartY = scrollStartY + distanceY;
         if (visibleStartY < 0)
            visibleStartY = 0;
         if (visibleStartY > drawConfiguration.bitmapHeight - drawConfiguration.screenHeight)
            visibleStartY = drawConfiguration.bitmapHeight - drawConfiguration.screenHeight;
      }
      if ((mTouchMode & TOUCH_MODE_HSCROLL) != 0) {
         visibleStartX = scrollStartX + distanceX;
         checkForValidXPosition();
         needRepaint = true;
      }
      invalidate();
   }

   private void checkForValidXPosition() {
      if (visibleStartX < 0) {
         visibleStartX = 0;
      } else if (visibleStartX > 24 * 60 * MINUTE_WIDTH) {
         visibleStartX = 24 * 60 * MINUTE_WIDTH - drawConfiguration.bitmapWidth;
      }
   }

   public void doFling(MotionEvent motionEvent1, MotionEvent motionEvent2, float velocityX, float velocityY) {
      mTouchMode = TOUCH_MODE_INITIAL_STATE;
      continueScroll.init((int) velocityX / 20);
      post(continueScroll);
   }

   public void doDown(MotionEvent motionEvent) {
      mTouchMode = TOUCH_MODE_DOWN;
      getHandler().removeCallbacks(continueScroll);
   }

   public void doShowPress(MotionEvent motionEvent) {
      selectedChannel = getChannelByAbsolutePosition((int) motionEvent.getY());
      selectedBroadcast = getBroadcastByAbsolutePosition(selectedChannel, (int) motionEvent.getX());
      needRepaint = true;
      invalidate();
   }

   public void doLongPress(MotionEvent motionEvent) {
      performLongClick();
   }

   private Channel getChannelByAbsolutePosition(int y) {
      y = y - TEXTHEIGHT - 2 * HORIZONTAL_GAP;
      if (y > 0) {
         y += visibleStartY;
         int channelId = (y / (TEXTHEIGHT + 2 * HORIZONTAL_GAP));
         if (channelId >= 0 && channels.size() > channelId)
            return channels.get(channelId);
      }
      return null;
   }

   private Broadcast getBroadcastByAbsolutePosition(Channel channel, int x) {
      if (channel != null) {
         x = x - CHANNELNAME_SIZE - 2 * VERTICAL_GAP;
         if (x > 0) {
            x += visibleStartX;
            x /= MINUTE_WIDTH;
            return channel.getBroadcastByPosition(x);
         }
      }
      return null;
   }

   @Override
   public boolean onTouchEvent(MotionEvent motionEvent) {
      switch (motionEvent.getAction()) {
         case MotionEvent.ACTION_DOWN:
         case MotionEvent.ACTION_MOVE:
         case MotionEvent.ACTION_CANCEL:
            parentActivity.gestureDetector.onTouchEvent(motionEvent);
            return true;
         case MotionEvent.ACTION_UP:
            parentActivity.gestureDetector.onTouchEvent(motionEvent);
            mTouchMode = TOUCH_MODE_INITIAL_STATE;
            return true;
         default:
            return parentActivity.gestureDetector.onTouchEvent(motionEvent) || super.onTouchEvent(motionEvent);
      }
   }

   public void setVisiblePosition(int hour, int minute) {
      visibleStartX = Utility.getX(hour, minute) - drawConfiguration.bitmapWidth / 2;
      checkForValidXPosition();
      needRepaint = true;
      invalidate();
   }

   @Override
   public boolean onKeyDown(int keyCode, KeyEvent keyEvent) {
      switch (keyCode) {
         case KeyEvent.KEYCODE_DPAD_CENTER:
            break;
         case KeyEvent.KEYCODE_DPAD_LEFT:
//            if(selectedBroadcast==null)
//               selectedBroadcast = findUpperLeftBroadcast();
//            needRepaint = true;
//            invalidate();
            break;
      }
      return super.onKeyDown(keyCode, keyEvent);
   }

   public void onCreateContextMenu(ContextMenu contextMenu, View view, ContextMenu.ContextMenuInfo contextMenuInfo) {
      if (selectedBroadcast != null) {
         contextMenu.setHeaderTitle(selectedBroadcast.title);
         contextMenu.add(Menu.NONE, TVBrowser.MENU_SHOWDETAIL, Menu.NONE, R.string.menu_showdetail);
         contextMenu.add(Menu.NONE, TVBrowser.MENU_SEARCHFORREPEAT, Menu.NONE, R.string.menu_searchforrepeat);
         contextMenu.add(Menu.NONE, TVBrowser.MENU_ADDREMINDER, Menu.NONE, R.string.menu_addreminder);
         contextMenu.add(Menu.NONE, TVBrowser.MENU_EDITREMINDER, Menu.NONE, R.string.menu_editreminder);
         contextMenu.add(Menu.NONE, TVBrowser.MENU_DELETEREMINDER, Menu.NONE, R.string.menu_deletereminder);
      }
   }

   public void onClick(View view) {
      //To change body of implemented methods use File | Settings | File Templates.
   }

   public class DrawConfiguration {

      public int screenWidth;
      private int screenHeight;
      private int bitmapWidth;
      public int bitmapHeight;

      public Paint backgroundPaint = new Paint();
      public Paint borderPaint = new Paint();
      public Paint selectedBorderPaint = new Paint();
      public Paint textPaint = new Paint();
      public Paint oldTextPaint = new Paint();
      public Paint oldBackgroundPaint = new Paint();
      public Paint currentEventOldTime = new Paint();
      public Paint currentEventNewTime = new Paint();
      public Paint headerBackground = new Paint();
      public Paint headerTextColor = new Paint();

      private void initialize(Resources resources) {
         backgroundPaint.setColor(resources.getColor(R.color.event_background));
         borderPaint.setColor(resources.getColor(R.color.event_bordercolor));
         borderPaint.setStyle(Paint.Style.STROKE);
         selectedBorderPaint.setColor(resources.getColor(R.color.event_selectedbordercolor));
         selectedBorderPaint.setStyle(Paint.Style.STROKE);
         textPaint.setColor(resources.getColor(R.color.event_text_color));
         textPaint.setTypeface(Typeface.DEFAULT);
         textPaint.setTextSize(TEXTHEIGHT);
         textPaint.setAntiAlias(true);
         oldTextPaint.setColor(resources.getColor(R.color.old_event_text_color));
         oldTextPaint.setTypeface(Typeface.DEFAULT);
         oldTextPaint.setTextSize(TEXTHEIGHT);
         oldTextPaint.setAntiAlias(true);
         oldBackgroundPaint.setColor(resources.getColor(R.color.old_event_background));
         currentEventOldTime.setColor(resources.getColor(R.color.currentevent_oldtimebackground));
         currentEventNewTime.setColor(resources.getColor(R.color.currentevent_newtimebackground));
         headerBackground.setColor(resources.getColor(R.color.header_backgroud));
         headerTextColor.setColor(resources.getColor(R.color.header_text_color));
         headerTextColor.setTypeface(Typeface.DEFAULT_BOLD);
         headerTextColor.setTextSize(TEXTHEIGHT);
         headerTextColor.setAntiAlias(true);
      }
   }

   private class ContinueScroll implements Runnable {

      private static final long FREE_SPIN_MILLIS = 180;
      private static final int MAX_DELTA = 60;
      private static final float FRICTION_COEF = 0.7F;
      private static final int SCROLL_REPEAT_INTERVAL = 30;

      private int mSignDeltaX;
      private int mAbsDeltaX;
      private float mFloatDeltaX;
      private long mFreeSpinTime;

      public void init(int deltaX) {
         mSignDeltaX = 0;
         if (deltaX > 0) {
            mSignDeltaX = 1;
         } else if (deltaX < 0) {
            mSignDeltaX = -1;
         }
         // Limit the maximum speed
         if (mAbsDeltaX > MAX_DELTA) {
            mAbsDeltaX = MAX_DELTA;
         }
         mFloatDeltaX = mAbsDeltaX;
         mFreeSpinTime = System.currentTimeMillis() + FREE_SPIN_MILLIS;
      }

      public void run() {
         long time = System.currentTimeMillis();

         // Start out with a frictionless "free spin"
         if (time > mFreeSpinTime) {
            // If the delta is small, then apply a fixed deceleration.
            // Otherwise
            if (mAbsDeltaX <= 10) {
               mAbsDeltaX -= 2;
            } else {
               mFloatDeltaX *= FRICTION_COEF;
               mAbsDeltaX = (int) mFloatDeltaX;
            }

            if (mAbsDeltaX < 0) {
               mAbsDeltaX = 0;
            }
         }
         if (mSignDeltaX == 1) {
            visibleStartX -= mAbsDeltaX;
         } else {
            visibleStartX += mAbsDeltaX;
         }
         if (visibleStartX < 0) {
            visibleStartX = 0;
            mAbsDeltaX = 0;
         } else if (visibleStartX > 24 * 60 * MINUTE_WIDTH) {
            visibleStartX = 24 * 60 * MINUTE_WIDTH - drawConfiguration.bitmapWidth;
            mAbsDeltaX = 0;
         }
         needRepaint = true;
         if (mAbsDeltaX > 0) {
            postDelayed(this, SCROLL_REPEAT_INTERVAL);
         } else {
            needRepaint = true;
         }
         invalidate();
      }
   }
}
