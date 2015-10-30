package view_inspector;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import com.f2prateek.rx.preferences.Preference;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.inject.Inject;
import view_inspector.dagger.ActivityComponent;
import view_inspector.dagger.ActivityModule;
import view_inspector.dagger.ApplicationComponent;
import view_inspector.dagger.ApplicationModule;
import view_inspector.dagger.DaggerActivityComponent;
import view_inspector.dagger.DaggerApplicationComponent;
import view_inspector.dagger.qualifier.BypassInterceptor;
import view_inspector.dagger.qualifier.ProbeMeasures;
import view_inspector.dagger.qualifier.Scalpel3D;
import view_inspector.dagger.qualifier.ShowOutline;
import view_inspector.probe.Probe;
import view_inspector.probe.ViewInspectorInterceptor;
import view_inspector.ui.ViewInspectorToolbar;

public final class ViewInspector {
  public static final String TAG = "ViewInspector";
  public static ApplicationComponent applicationComponent;
  public static Set<Context> contextSet = new HashSet<>();
  public static Map<Context, ActivityComponent> runtimeComponentMap = new HashMap<>();
  public static Map<Context, ViewInspectorToolbar> toolbarMap = new HashMap<>();
  public static View viewRoot;

  @Inject WindowManager windowManager;
  @Inject @ShowOutline Preference<Boolean> showOutline;
  @Inject @ProbeMeasures Preference<Boolean> probeMeasures;
  @Inject @BypassInterceptor Preference<Boolean> bypassInterceptor;
  @Inject @Scalpel3D Preference<Boolean> scalpelEnabled;
  @Inject ViewInspectorInterceptor interceptor;
  @Inject ViewInspectorToolbar toolbar;

  public static ViewInspector create() {
    return new ViewInspector();
  }

  public void onCreate(Context context) {
    if (!contextSet.contains(context)) {
      contextSet.add(context);

      if (applicationComponent == null) {
        // create dagger component for the application
        Application application = (Application) context.getApplicationContext();
        applicationComponent = DaggerApplicationComponent.builder()
            .applicationModule(new ApplicationModule(application))
            .build();
      }

      if (!runtimeComponentMap.containsKey(context)) {
        // create dagger components per activity
        ActivityComponent activityComponent = DaggerActivityComponent.builder()
            .applicationComponent(applicationComponent)
            .activityModule(new ActivityModule((Activity) context))
            .build();
        runtimeComponentMap.put(context, activityComponent);
        activityComponent.inject(this);
      }

      // Reset preferences
      showOutline.set(false);
      probeMeasures.set(false);
      bypassInterceptor.set(false);
      scalpelEnabled.set(false);

      Probe.deploy(context, interceptor);

      windowManager.addView(toolbar, ViewInspectorToolbar.createLayoutParams(context));
      toolbarMap.put(context, toolbar);
    }
  }

  public void onResume() {
    toolbar.setVisibility(View.VISIBLE);
  }

  public void onPause() {
    toolbar.closeMenu();
    toolbar.setVisibility(View.GONE);
  }

  public void onDestroy(Context context) {
    // remove dagger component map
    runtimeComponentMap.remove(context);

    ViewInspectorToolbar toolbarInstance = toolbarMap.get(context);
    if (toolbarInstance != null) windowManager.removeViewImmediate(toolbarInstance);
  }
}
