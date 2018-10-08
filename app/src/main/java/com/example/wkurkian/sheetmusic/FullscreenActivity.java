package com.example.wkurkian.sheetmusic;

import android.annotation.SuppressLint;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class FullscreenActivity extends AppCompatActivity {
  /**
   * Whether or not the system UI should be auto-hidden after
   * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
   */
  private static final boolean AUTO_HIDE = true;

  /**
   * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
   * user interaction before hiding the system UI.
   */
  private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

  private int forwardVelocity = 0;

  /**
   * Some older devices needs a small delay between UI widget updates
   * and a change of the status and navigation bar.
   */
  private static final int UI_ANIMATION_DELAY = 300;
  private final Handler mHideHandler = new Handler();
  private View mContentView;
  private final Runnable mHidePart2Runnable = new Runnable() {
    @SuppressLint("InlinedApi")
    @Override
    public void run() {
      // Delayed removal of status and navigation bar

      // Note that some of these constants are new as of API 16 (Jelly Bean)
      // and API 19 (KitKat). It is safe to use them, as they are inlined
      // at compile-time and do nothing on earlier devices.
      mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
        | View.SYSTEM_UI_FLAG_FULLSCREEN
        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
    }
  };
  private View mControlsView;
  private final Runnable mShowPart2Runnable = new Runnable() {
    @Override
    public void run() {
      // Delayed display of UI elements
      ActionBar actionBar = getSupportActionBar();
      if (actionBar != null) {
        actionBar.show();
      }
      mControlsView.setVisibility(View.VISIBLE);
    }
  };
  private boolean mVisible;

  private View.OnClickListener zoomInListener = new View.OnClickListener() {
    @Override
    public void onClick(View v) {
      zoomIn();
    }
  };

  private View.OnClickListener zoomOutListener = new View.OnClickListener() {
    @Override
    public void onClick(View v) {
      zoomOut();
    }
  };

  private View.OnClickListener forwardListener = new View.OnClickListener() {
    @Override
    public void onClick(View v) {
      forward();
    }
  };

  private View.OnClickListener backwardListener = new View.OnClickListener() {
    @Override
    public void onClick(View v) {
      backward();
    }
  };

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_fullscreen);

    mVisible = true;
    mControlsView = findViewById(R.id.fullscreen_content_controls);
    //mContentView = findViewById(R.id.fullscreen_content);


    // Set up the user interaction to manually show or hide the system UI.
    /*mContentView.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        toggle();
      }
    });*/

    // set maximum scroll amount (based on center of image)
    int maxX = 2000;

    // set scroll limits
    final int maxLeft = (maxX * -1);
    final int maxRight = maxX;

    ImageView music = findViewById(R.id.imageView);

    music.setScaleX(7.7f);
    music.setScaleY(7.7f);
    music.scrollTo(-200,0);

    // set touchlistener
    music.setOnTouchListener(new View.OnTouchListener()
    {
      float downX, downY;
      int totalX, totalY;
      int scrollByX, scrollByY;
      public boolean onTouch(View view, MotionEvent event)
      {
        float currentX, currentY;
        switch (event.getAction())
        {
          case MotionEvent.ACTION_DOWN:
            downX = event.getX();
            break;

          case MotionEvent.ACTION_MOVE:
            currentX = event.getX();
            scrollByX = (int)(downX - currentX);

            // scrolling to left side of image (pic moving to the right)
            if (currentX > downX)
            {
              if (totalX == maxLeft)
              {
                scrollByX = 0;
              }
              if (totalX > maxLeft)
              {
                totalX = totalX + scrollByX;
              }
              if (totalX < maxLeft)
              {
                scrollByX = maxLeft - (totalX - scrollByX);
                totalX = maxLeft;
              }
            }

            // scrolling to right side of image (pic moving to the left)
            if (currentX < downX)
            {
              if (totalX == maxRight)
              {
                scrollByX = 0;
              }
              if (totalX < maxRight)
              {
                totalX = totalX + scrollByX;
              }
              if (totalX > maxRight)
              {
                scrollByX = maxRight - (totalX - scrollByX);
                totalX = maxRight;
              }
            }

            findViewById(R.id.imageView).scrollBy(scrollByX, 0);
            downX = currentX;
            break;

        }

        return true;
      }
    });

    // Upon interacting with UI controls, delay any scheduled hide()
    // operations to prevent the jarring behavior of controls going away
    // while interacting with the UI.
    findViewById(R.id.zoomIn).setOnClickListener(zoomInListener);
    findViewById(R.id.zoomOut).setOnClickListener(zoomOutListener);
    findViewById(R.id.forward).setOnClickListener(forwardListener);
    findViewById(R.id.backward).setOnClickListener(backwardListener);
  }

  @Override
  protected void onPostCreate(Bundle savedInstanceState) {
    super.onPostCreate(savedInstanceState);

    Mover mover = new Mover();
    mover.start();

  }

  private void zoomIn() {
    ImageView music = findViewById(R.id.imageView);
    music.setScaleX((float)(music.getScaleX()+0.2));
    music.setScaleY((float)(music.getScaleY()+0.2));
    music.getAdjustViewBounds();
  }

  private void zoomOut() {
    ImageView music = findViewById(R.id.imageView);
    music.setScaleX((float)(music.getScaleX()-0.2));
    music.setScaleY((float)(music.getScaleY()-0.2));
    music.getAdjustViewBounds();
  }

  private void forward() {
    forwardVelocity += 1;
  }

  private void backward() {
    forwardVelocity -= 1;
  }

  private class Mover extends Thread {

    public void run() {
      while(true) {
        findViewById(R.id.imageView).scrollBy((int)Math.signum(forwardVelocity), 0);
        try {
          int time = 1000;
          if (forwardVelocity != 0 ) {
            time = (int)(1000/Math.abs(forwardVelocity));
          }
          Thread.sleep(time);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
    }

  }

}
