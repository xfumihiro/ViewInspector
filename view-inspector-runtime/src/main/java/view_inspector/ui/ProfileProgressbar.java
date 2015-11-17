package view_inspector.ui;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.view.Gravity;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import javax.inject.Inject;
import view_inspector.R;
import view_inspector.dagger.scope.PerActivity;

import static android.graphics.PixelFormat.TRANSLUCENT;
import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR;
import static android.view.WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
import static android.view.WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;
import static android.view.WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
import static android.view.WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
import static android.view.WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;

@PerActivity public class ProfileProgressbar extends LinearLayout {
  private static int mLayoutHeight;
  private final ProgressBar mProgressbar;
  private final TextView mPercentage;
  private int mSamples = 10;

  @Inject public ProfileProgressbar(Context context) {
    super(context);
    inflate(context, R.layout.view_inspector_progressbar, this);
    mProgressbar = (ProgressBar) findViewById(R.id.progressbar);
    mProgressbar.setIndeterminate(false);
    mProgressbar.setMax(100);
    mPercentage = (TextView) findViewById(R.id.percentage);
  }

  public static WindowManager.LayoutParams createLayoutParams(Context context) {
    Resources res = context.getResources();
    mLayoutHeight = res.getDimensionPixelSize(R.dimen.view_inspector_toolbar_height);
    if (Build.VERSION.SDK_INT == 23) { // MARSHMALLOW
      mLayoutHeight = res.getDimensionPixelSize(R.dimen.view_inspector_toolbar_height_m);
    }

    final WindowManager.LayoutParams params =
        new WindowManager.LayoutParams(MATCH_PARENT, mLayoutHeight, TYPE_SYSTEM_ERROR,
            FLAG_NOT_FOCUSABLE | FLAG_NOT_TOUCH_MODAL | FLAG_LAYOUT_NO_LIMITS
                | FLAG_LAYOUT_INSET_DECOR | FLAG_LAYOUT_IN_SCREEN, TRANSLUCENT);
    params.gravity = Gravity.TOP | Gravity.RIGHT;
    return params;
  }

  @Override protected void onAttachedToWindow() {
    super.onAttachedToWindow();
    LinearLayout progressbarLayout = (LinearLayout) findViewById(R.id.progressbar_layout);
    progressbarLayout.setTranslationY(-mLayoutHeight);
    progressbarLayout.setVisibility(VISIBLE);
    ObjectAnimator animator =
        ObjectAnimator.ofFloat(progressbarLayout, "translationY", -mLayoutHeight, 0);
    animator.setInterpolator(new DecelerateInterpolator());
    animator.setDuration(500);
    animator.start();
  }

  public void setProgress(int sampleIndex) {
    int progress = (int) (((float) sampleIndex / (float) mSamples) * 100);
    mProgressbar.setProgress(progress);
    mPercentage.setText(String.valueOf(progress));
  }

  public void setSamples(int max) {
    mSamples = max;
  }

  public int getSamples() {
    return mSamples;
  }
}
