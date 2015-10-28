package view_inspector.ui.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
import com.f2prateek.rx.preferences.Preference;
import javax.inject.Inject;
import view_inspector.R;
import view_inspector.ViewInspector;
import view_inspector.dagger.qualifier.BypassInterceptor;
import view_inspector.ui.ViewInspectorToolbar;
import view_inspector.ui.menu.BaseMenu;

public class BaseDialog extends AlertDialog {
  @Inject @BypassInterceptor Preference<Boolean> bypassInterceptor;
  @Inject ViewInspectorToolbar toolbar;
  BaseMenu mMenu;

  public BaseDialog(Context context) {
    super(context);
    ViewInspector.runtimeComponentMap.get(((ContextThemeWrapper) context).getBaseContext())
        .inject(this);
    setCancelable(true);
    setCanceledOnTouchOutside(true);
    setOnCancelListener(new OnCancelListener() {
      @Override public void onCancel(DialogInterface dialog) {
        restoreOpenedMenu();
      }
    });
  }

  @Override public void onAttachedToWindow() {
    bypassInterceptor.set(true);
    mMenu = toolbar.getMenu();
    toolbar.closeToolbar();
    super.onAttachedToWindow();
  }

  @Override public void onDetachedFromWindow() {
    bypassInterceptor.set(false);
    super.onDetachedFromWindow();
  }

  protected void restoreOpenedMenu() {
    toolbar.toggleToolbar();
    if (mMenu != null) toolbar.openMenu(mMenu);
  }

  public static int getDialogTheme(Context context) {
    TypedValue outValue = new TypedValue();
    context.getTheme().resolveAttribute(R.attr.isLightTheme, outValue, true);
    return outValue.data != 0 ? R.style.DialogThemeLight : R.style.DialogTheme;
  }
}
