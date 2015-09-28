package view_inspector.ui.dialog;

import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.support.v7.internal.view.ContextThemeWrapper;
import android.util.TypedValue;
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
  }

  @Override protected void onStart() {
    super.onStart();
    bypassInterceptor.set(true);
    mMenu = toolbar.getMenu();
    toolbar.closeToolbar();
  }

  @Override protected void onStop() {
    bypassInterceptor.set(false);
    toolbar.toggleToolbar();
    if (mMenu != null) toolbar.openMenu(mMenu);
    super.onStop();
  }

  @Override public void onAttachedToWindow() {
    super.onAttachedToWindow();
  }

  @Override public void onDetachedFromWindow() {
    super.onDetachedFromWindow();
  }

  public static int getDialogTheme(Context context) {
    TypedValue outValue = new TypedValue();
    context.getTheme().resolveAttribute(R.attr.isLightTheme, outValue, true);
    return outValue.data != 0 ? R.style.DialogThemeLight : R.style.DialogTheme;
  }
}
