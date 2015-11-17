package view_inspector.ui;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import com.f2prateek.rx.preferences.Preference;
import javax.inject.Inject;
import view_inspector.R;
import view_inspector.ViewInspector;
import view_inspector.dagger.qualifier.BypassInterceptor;
import view_inspector.dagger.scope.PerActivity;
import view_inspector.profile.ProfileUtil;
import view_inspector.ui.dialog.BaseDialog;
import view_inspector.ui.dialog.ProfileResultDialog;
import view_inspector.ui.menu.BaseMenu;
import view_inspector.ui.menu.BoundaryMenu;
import view_inspector.ui.menu.EventMenu;
import view_inspector.ui.menu.LayerMenu;
import view_inspector.ui.menu.SettingsMenu;

import static android.graphics.PixelFormat.TRANSLUCENT;
import static android.view.WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR;
import static android.view.WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
import static android.view.WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;
import static android.view.WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
import static android.view.WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
import static android.view.WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;

@PerActivity public class ViewInspectorToolbar extends FrameLayout
    implements ProfileUtil.ProfileResultListener {
  private static final int TOOLBAR_MENU_ITEMS = 5;
  private final Context mContext;
  private final int mToolbarWidth;
  private final int mToolbarClosedWidth;

  @Inject @BypassInterceptor Preference<Boolean> bypassInterceptor;
  @Inject ProfileUtil profileUtil;
  @Inject WindowManager windowManager;
  @Inject ProfileProgressbar profileProgressbar;

  private View mToolbar;
  private ImageButton mToggleButton;
  private BaseMenu mMenu;

  @Inject public ViewInspectorToolbar(Context context) {
    super(context);
    ViewInspector.runtimeComponentMap.get(context).inject(this);
    mContext = context;
    inflate(context, R.layout.view_inspector_toolbar, this);

    Resources resources = mContext.getResources();
    mToolbarWidth = resources.getDimensionPixelSize(R.dimen.view_inspector_toolbar_header_width)
        + resources.getDimensionPixelSize(R.dimen.view_inspector_toolbar_icon_width)
        * TOOLBAR_MENU_ITEMS;
    mToolbarClosedWidth =
        resources.getDimensionPixelSize(R.dimen.view_inspector_toolbar_closed_width);
  }

  public static WindowManager.LayoutParams createLayoutParams(Context context) {
    Resources res = context.getResources();
    int width = res.getDimensionPixelSize(R.dimen.view_inspector_toolbar_header_width)
        + res.getDimensionPixelSize(R.dimen.view_inspector_toolbar_icon_width) * TOOLBAR_MENU_ITEMS;
    int height = res.getDimensionPixelSize(R.dimen.view_inspector_toolbar_height);
    if (Build.VERSION.SDK_INT == 23) { // MARSHMALLOW
      height = res.getDimensionPixelSize(R.dimen.view_inspector_toolbar_height_m);
    }

    final WindowManager.LayoutParams params =
        new WindowManager.LayoutParams(width, height, TYPE_SYSTEM_ERROR,
            FLAG_NOT_FOCUSABLE | FLAG_NOT_TOUCH_MODAL | FLAG_LAYOUT_NO_LIMITS
                | FLAG_LAYOUT_INSET_DECOR | FLAG_LAYOUT_IN_SCREEN, TRANSLUCENT);
    params.gravity = Gravity.TOP | Gravity.RIGHT;

    return params;
  }

  @Override protected void onAttachedToWindow() {
    super.onAttachedToWindow();

    mToolbar = findViewById(R.id.toolbar);
    mToggleButton = (ImageButton) findViewById(R.id.toggle_menu);
    ImageButton buttonBoundaryMenu = (ImageButton) findViewById(R.id.outline_menu);
    ImageButton buttonLayerMenu = (ImageButton) findViewById(R.id.layer_menu);
    ImageButton buttonEventMenu = (ImageButton) findViewById(R.id.event_menu);
    ImageButton buttonSettingsMenu = (ImageButton) findViewById(R.id.settings_menu);

    mToolbar.setTranslationX(mToolbarWidth);

    ObjectAnimator animator = ObjectAnimator.ofFloat(mToolbar, "translationX", mToolbarWidth,
        mToolbarWidth - mToolbarClosedWidth);
    animator.setInterpolator(new DecelerateInterpolator());
    animator.start();

    mToggleButton.setOnClickListener(new OnClickListener() {
      @Override public void onClick(View v) {
        closeMenu();
        toggleToolbar();
      }
    });

    buttonBoundaryMenu.setOnClickListener(new OnClickListener() {
      @Override public void onClick(View v) {
        if (!(mMenu instanceof BoundaryMenu)) {
          closeMenu();
          mMenu = new BoundaryMenu(mContext);
          windowManager.addView(mMenu, BaseMenu.createLayoutParams(mContext));
        } else {
          closeMenu();
        }
      }
    });

    buttonLayerMenu.setOnClickListener(new OnClickListener() {
      @Override public void onClick(View v) {
        if (!(mMenu instanceof LayerMenu)) {
          closeMenu();
          mMenu = new LayerMenu(mContext);
          windowManager.addView(mMenu, BaseMenu.createLayoutParams(mContext));
        } else {
          closeMenu();
        }
      }
    });

    buttonEventMenu.setOnClickListener(new OnClickListener() {
      @Override public void onClick(View v) {
        if (!(mMenu instanceof EventMenu)) {
          closeMenu();
          mMenu = new EventMenu(mContext);
          windowManager.addView(mMenu, BaseMenu.createLayoutParams(mContext));
        } else {
          closeMenu();
        }
      }
    });

    buttonSettingsMenu.setOnClickListener(new OnClickListener() {
      @Override public void onClick(View v) {
        if (!(mMenu instanceof SettingsMenu)) {
          closeMenu();
          mMenu = new SettingsMenu(mContext);
          windowManager.addView(mMenu, BaseMenu.createLayoutParams(mContext));
        } else {
          closeMenu();
        }
      }
    });
  }

  @Override protected void onDetachedFromWindow() {
    closeMenu();
    super.onDetachedFromWindow();
  }

  public void closeMenu() {
    if (mMenu != null) {
      windowManager.removeViewImmediate(mMenu);
      mMenu = null;
    }
  }

  public BaseMenu getMenu() {
    return mMenu;
  }

  public void openMenu(BaseMenu baseMenu) {
    mMenu = baseMenu;
    windowManager.addView(mMenu, BaseMenu.createLayoutParams(mContext));
  }

  @SuppressWarnings("deprecation") public void toggleToolbar() {
    ObjectAnimator animator =
        ObjectAnimator.ofFloat(mToolbar, "translationX", mToolbar.getTranslationX(),
            mToolbar.getTranslationX() < mToolbarClosedWidth ? mToolbarWidth - mToolbarClosedWidth
                : 0);
    animator.setInterpolator(new DecelerateInterpolator());
    animator.start();
    if (mToolbar.getTranslationX() < mToolbarClosedWidth) {
      mToggleButton.setImageDrawable(
          getResources().getDrawable(R.drawable.ic_chevron_left_white_24dp));
    } else {
      mToggleButton.setImageDrawable(
          getResources().getDrawable(R.drawable.ic_chevron_right_white_24dp));
    }
  }

  @SuppressWarnings("deprecation") public void closeToolbar() {
    closeMenu();
    ObjectAnimator animator =
        ObjectAnimator.ofFloat(mToolbar, "translationX", mToolbar.getTranslationX(), mToolbarWidth);
    animator.setInterpolator(new DecelerateInterpolator());
    animator.start();
    mToggleButton.setImageDrawable(
        getResources().getDrawable(R.drawable.ic_chevron_left_white_24dp));
  }

  @Override public void onProfileDone() {
    ((Activity) mContext).runOnUiThread(new Runnable() {
      @Override public void run() {
        new ProfileResultDialog(
            new ContextThemeWrapper(mContext, BaseDialog.getDialogTheme(mContext)),
            profileProgressbar.getSamples()).show();
        closeProgressbar();
      }
    });
  }

  @Override public void onProgress(final int step) {
    ((Activity) mContext).runOnUiThread(new Runnable() {
      @Override public void run() {
        profileProgressbar.setProgress(step);
      }
    });
  }

  public void showProgressbar() {
    windowManager.addView(profileProgressbar, ProfileProgressbar.createLayoutParams(mContext));

    // lock screen orientation
    int currentOrientation = mContext.getResources().getConfiguration().orientation;
    if (currentOrientation == Configuration.ORIENTATION_LANDSCAPE) {
      ((Activity) mContext).setRequestedOrientation(
          ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
    } else {
      ((Activity) mContext).setRequestedOrientation(
          ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
    }
  }

  private void closeProgressbar() {
    windowManager.removeViewImmediate(profileProgressbar);

    // unlock screen orientation
    ((Activity) mContext).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
  }
}
