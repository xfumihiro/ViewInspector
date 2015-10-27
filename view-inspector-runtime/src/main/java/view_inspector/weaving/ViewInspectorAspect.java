package view_inspector.weaving;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import com.f2prateek.rx.preferences.Preference;
import javax.inject.Inject;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import view_inspector.ViewInspector;
import view_inspector.dagger.ActivityComponent;
import view_inspector.dagger.ActivityModule;
import view_inspector.dagger.ApplicationModule;
import view_inspector.dagger.DaggerActivityComponent;
import view_inspector.dagger.DaggerApplicationComponent;
import view_inspector.dagger.qualifier.BypassInterceptor;
import view_inspector.dagger.qualifier.LogViewEvents;
import view_inspector.dagger.qualifier.ProbeMeasures;
import view_inspector.dagger.qualifier.Scalpel3D;
import view_inspector.dagger.qualifier.ShowOutline;
import view_inspector.probe.Probe;
import view_inspector.probe.ViewInspectorInterceptor;
import view_inspector.ui.ViewInspectorToolbar;

@Aspect public class ViewInspectorAspect {
  @Inject WindowManager windowManager;
  @Inject @ShowOutline Preference<Boolean> showOutline;
  @Inject @ProbeMeasures Preference<Boolean> probeMeasures;
  @Inject @BypassInterceptor Preference<Boolean> bypassInterceptor;
  @Inject @LogViewEvents Preference<Boolean> logViewEvents;
  @Inject @Scalpel3D Preference<Boolean> scalpelEnabled;
  @Inject ViewInspectorInterceptor interceptor;
  @Inject ViewInspectorToolbar toolbar;

  @Pointcut("within(android.app.Activity+)") public void withinActivityClass() {
  }

  @Pointcut("execution(void onCreate(..)) && withinActivityClass()")
  public void activityOnCreatedCall() {
  }

  @Around("activityOnCreatedCall()")
  public Object injectViewInspector(ProceedingJoinPoint joinPoint) throws Throwable {
    Log.d(ViewInspector.TAG, "injectViewInspector");
    Context context = (Context) joinPoint.getThis();

    if (!ViewInspector.contextSet.contains(context)) {
      ViewInspector.contextSet.add(context);

      if (ViewInspector.applicationComponent == null) {
        // create dagger component for the application
        Application application = (Application) context.getApplicationContext();
        ViewInspector.applicationComponent = DaggerApplicationComponent.builder()
            .applicationModule(new ApplicationModule(application))
            .build();
      }

      if (!ViewInspector.runtimeComponentMap.containsKey(context)) {
        // create dagger components per activity
        ActivityComponent activityComponent = DaggerActivityComponent.builder()
            .applicationComponent(ViewInspector.applicationComponent)
            .activityModule(new ActivityModule((Activity) context))
            .build();
        ViewInspector.runtimeComponentMap.put(context, activityComponent);
        activityComponent.inject(this);
      }

      // Reset preferences
      showOutline.set(false);
      probeMeasures.set(false);
      bypassInterceptor.set(false);
      scalpelEnabled.set(false);

      Probe.deploy(context, interceptor);

      windowManager.addView(toolbar, ViewInspectorToolbar.createLayoutParams(context));
      ViewInspector.toolbarMap.put(context, toolbar);
    }

    return joinPoint.proceed();
  }

  @Pointcut("execution(void onResume()) && withinActivityClass()")
  public void activityOnResumeCall() {
  }

  @Around("activityOnResumeCall()") public Object showViewInspector(ProceedingJoinPoint joinPoint)
      throws Throwable {
    toolbar.setVisibility(View.VISIBLE);
    return joinPoint.proceed();
  }

  @Pointcut("execution(void onPause()) && withinActivityClass()")
  public void activityOnPauseCall() {
  }

  @Around("activityOnPauseCall()") public Object hideViewInspector(ProceedingJoinPoint joinPoint)
      throws Throwable {
    toolbar.closeMenu();
    toolbar.setVisibility(View.GONE);
    return joinPoint.proceed();
  }

  @Pointcut("execution(void onDestroy()) && withinActivityClass()")
  public void activityOnDestroyCall() {
  }

  @Around("activityOnDestroyCall()")
  public Object destroyViewInspector(ProceedingJoinPoint joinPoint) throws Throwable {
    Log.d(ViewInspector.TAG, "destroyViewInspector");
    Context context = (Context) joinPoint.getThis();

    // remove dagger component map
    ViewInspector.runtimeComponentMap.remove(context);

    windowManager.removeViewImmediate(ViewInspector.toolbarMap.get(context));

    return joinPoint.proceed();
  }
}
