package de.misi.tvbrowser.widgets;

import android.content.res.Resources;
import android.graphics.*;
import android.os.Handler;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
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

   private static DrawConfiguration drawConfiguration;

   private TVBrowser mParentActivity;
   private ArrayList<Channel> mChannels;
   private Bitmap mChannelNameBitmap;
   private Canvas mChannelNameCanvas;
   private Bitmap mBitmap;
   private Canvas mCanvas;
   private final Rect mSrcRect = new Rect();
   private final Rect mDestRect = new Rect();
   private boolean mNeedRepaint;
   private int mVisibleStartX;
   private int mVisibleStartY;
   private int mScrollStartX;
   private int mScrollStartY;
   private final ContinueScroll mContinueScroll = new ContinueScroll();
   private int mTouchMode = TOUCH_MODE_INITIAL_STATE;
   private Channel mSelectedChannel;
   public Broadcast mSelectedBroadcast;
   private Handler mHandler = new Handler();
   private Ticker mTicker = new Ticker();

   public TVBrowserGridView(TVBrowser activity) {
      super(activity);
      mParentActivity = activity;
      initialize(activity.getResources());
   }

   private void initialize(Resources resources) {
      if (drawConfiguration == null) {
         drawConfiguration = new DrawConfiguration();
         drawConfiguration.initialize(resources);
      }
      mVisibleStartX = Utility.getX(Calendar.getInstance());
      mNeedRepaint = true;
      mTouchMode = TOUCH_MODE_INITIAL_STATE;
      mSelectedBroadcast = null;
      setOnCreateContextMenuListener(this);
      setOnClickListener(this);
      mHandler.removeCallbacks(mTicker);
      mHandler.postDelayed(mTicker, 20000);
   }

   public void setChannels(ArrayList<Channel> channels) {
      mChannels = channels;
      mNeedRepaint = true;
      mSelectedBroadcast = null;
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
      int bitmapHeight = (mChannels != null ? mChannels.size() * TEXTHEIGHT + 2 * mChannels.size() * HORIZONTAL_GAP : height) + TEXTHEIGHT;
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
      mNeedRepaint = true;
   }

   @Override
   protected void onDraw(Canvas canvas) {
      boolean doCopyBitmap = true;
      if (mNeedRepaint && mCanvas != null) {
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
         if (mChannels != null) {
            for (int i = 0; i < 25; i++) {
               int x = i * 60 * MINUTE_WIDTH;
               if (x >= mVisibleStartX && x < mVisibleStartX + drawConfiguration.bitmapWidth) {
                  String value = Integer.toString(i);
                  float measure = textPaint.measureText(value);
                  mCanvas.drawText(value, x - measure / 2 - mVisibleStartX, TEXTHEIGHT + HORIZONTAL_GAP, textPaint);
               }
            }
            int y = TEXTHEIGHT + 2 * HORIZONTAL_GAP;
            int visibleEndX = (mVisibleStartX + drawConfiguration.bitmapWidth) / MINUTE_WIDTH;
            Calendar now = Calendar.getInstance();
            long nowInMillis = now.getTimeInMillis();
            int nowx = Utility.getX(now);
            for (Channel channel : mChannels) {
               channel.drawChannelNames(mChannelNameCanvas, drawConfiguration, y);
               channel.drawBroadcasts(mCanvas, drawConfiguration, y, mVisibleStartX / MINUTE_WIDTH, visibleEndX, nowInMillis, nowx, mVisibleStartX, mSelectedBroadcast);
               y += TEXTHEIGHT + 2 * HORIZONTAL_GAP;
            }
         } else {
            doCopyBitmap = false;
            canvas.drawText(getResources().getString(R.string.no_data_available), 0, 0, textPaint);
         }
         mNeedRepaint = false;
      }
      if (doCopyBitmap)
         copyBitmap(canvas);
   }

   private void copyBitmap(Canvas canvas) {
      if (mBitmap != null) {
         Rect src = mSrcRect;
         Rect dest = mDestRect;

         src.top = mVisibleStartY;
         src.bottom = mVisibleStartY + drawConfiguration.screenHeight;
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
         mScrollStartX = mVisibleStartX;
         mScrollStartY = mVisibleStartY;
         if (absDistanceY >= 2 * absDistanceX) {
            mTouchMode = TOUCH_MODE_VSCROLL;
         } else {
            mTouchMode = TOUCH_MODE_HSCROLL;
         }
      }
      if ((mTouchMode & TOUCH_MODE_VSCROLL) != 0) {
         mVisibleStartY = mScrollStartY + distanceY;
         if (mVisibleStartY < 0)
            mVisibleStartY = 0;
         if (mVisibleStartY > drawConfiguration.bitmapHeight - drawConfiguration.screenHeight)
            mVisibleStartY = drawConfiguration.bitmapHeight - drawConfiguration.screenHeight;
      }
      if ((mTouchMode & TOUCH_MODE_HSCROLL) != 0) {
         mVisibleStartX = mScrollStartX + distanceX;
         checkForValidXPosition();
         mNeedRepaint = true;
      }
      invalidate();
   }

   private void checkForValidXPosition() {
      if (mVisibleStartX < 0) {
         mVisibleStartX = 0;
      } else if (mVisibleStartX > 24 * 60 * MINUTE_WIDTH) {
         mVisibleStartX = 24 * 60 * MINUTE_WIDTH - drawConfiguration.bitmapWidth;
      }
   }

   public void doFling(MotionEvent motionEvent1, MotionEvent motionEvent2, float velocityX, float velocityY) {
      mTouchMode = TOUCH_MODE_INITIAL_STATE;
      mContinueScroll.init((int) velocityX / 20);
      post(mContinueScroll);
   }

   public void doDown(MotionEvent motionEvent) {
      mTouchMode = TOUCH_MODE_DOWN;
      getHandler().removeCallbacks(mContinueScroll);
   }

   public void doShowPress(MotionEvent motionEvent) {
      mSelectedChannel = getChannelByAbsolutePosition((int) motionEvent.getY());
      mSelectedBroadcast = getBroadcastByAbsolutePosition(mSelectedChannel, (int) motionEvent.getX());
      if (mSelectedChannel != null && mSelectedBroadcast != null)
         Log.d(TVBrowser.LOGTAG, "doShowPress: " + mSelectedChannel.mName + ", " + mSelectedBroadcast.mTitle);
      mNeedRepaint = true;
      invalidate();
   }

   public void doLongPress(MotionEvent motionEvent) {
      performLongClick();
   }

   private Channel getChannelByAbsolutePosition(int y) {
      y = y - TEXTHEIGHT - 2 * HORIZONTAL_GAP;
      if (y > 0) {
         y += mVisibleStartY;
         int channelId = (y / (TEXTHEIGHT + 2 * HORIZONTAL_GAP));
         if (channelId >= 0 && mChannels.size() > channelId)
            return mChannels.get(channelId);
      }
      return null;
   }

   private Broadcast getBroadcastByAbsolutePosition(Channel channel, int x) {
      if (channel != null) {
         x = x - CHANNELNAME_SIZE - 2 * VERTICAL_GAP;
         if (x > 0) {
            x += mVisibleStartX;
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
            mParentActivity.mGestureDetector.onTouchEvent(motionEvent);
            return true;
         case MotionEvent.ACTION_UP:
            mParentActivity.mGestureDetector.onTouchEvent(motionEvent);
            mTouchMode = TOUCH_MODE_INITIAL_STATE;
            return true;
         default:
            return mParentActivity.mGestureDetector.onTouchEvent(motionEvent) || super.onTouchEvent(motionEvent);
      }
   }

   public void setVisiblePosition(int hour, int minute) {
      mVisibleStartX = Utility.getX(hour, minute) - drawConfiguration.bitmapWidth / 2;
      checkForValidXPosition();
      mNeedRepaint = true;
      invalidate();
   }

   @Override
   public boolean onKeyDown(int keyCode, KeyEvent keyEvent) {
      switch (keyCode) {
         case KeyEvent.KEYCODE_DPAD_CENTER:
            break;
         case KeyEvent.KEYCODE_DPAD_LEFT:
//            if(mSelectedBroadcast==null)
//               mSelectedBroadcast = findUpperLeftBroadcast();
//            mNeedRepaint = true;
//            invalidate();
            break;
      }
      return super.onKeyDown(keyCode, keyEvent);
   }

   public void onCreateContextMenu(ContextMenu contextMenu, View view, ContextMenu.ContextMenuInfo contextMenuInfo) {
      Utility.createBroadcastContextMenu(mSelectedBroadcast, contextMenu);
   }

   public void onClick(View view) {
      //Todo change body of implemented methods use File | Settings | File Templates.
   }

   public class DrawConfiguration {

      public int screenWidth;
      private int screenHeight;
      private int bitmapWidth;
      public int bitmapHeight;

      public final Paint backgroundPaint = new Paint();
      public final Paint borderPaint = new Paint();
      public final Paint selectedBorderPaint = new Paint();
      public final Paint textPaint = new Paint();
      public final Paint oldTextPaint = new Paint();
      public final Paint oldBackgroundPaint = new Paint();
      public final Paint currentEventOldTime = new Paint();
      public final Paint currentEventNewTime = new Paint();
      public final Paint headerBackground = new Paint();
      public final Paint headerTextColor = new Paint();

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
            mVisibleStartX -= mAbsDeltaX;
         } else {
            mVisibleStartX += mAbsDeltaX;
         }
         if (mVisibleStartX < 0) {
            mVisibleStartX = 0;
            mAbsDeltaX = 0;
         } else if (mVisibleStartX > 24 * 60 * MINUTE_WIDTH) {
            mVisibleStartX = 24 * 60 * MINUTE_WIDTH - drawConfiguration.bitmapWidth;
            mAbsDeltaX = 0;
         }
         mNeedRepaint = true;
         if (mAbsDeltaX > 0) {
            postDelayed(this, SCROLL_REPEAT_INTERVAL);
         } else {
            mNeedRepaint = true;
         }
         invalidate();
      }
   }

   private class Ticker implements Runnable {

      public void run() {
         Log.d(TVBrowser.LOGTAG, "tick");
         invalidate();
         mHandler.postAtTime(this, System.currentTimeMillis() + 10000);
      }
   }
}
