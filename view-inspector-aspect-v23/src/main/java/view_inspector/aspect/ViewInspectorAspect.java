package view_inspector.aspect;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import view_inspector.ViewInspector;

@Aspect public class ViewInspectorAspect {
  private static final int OVERLAY_PERMISSION_CALL = 1;

  private ViewInspector mViewInspector;
  private boolean isRequestingOverlayPermission = false;
  private boolean isRestarting = false;

  public ViewInspectorAspect() {
    mViewInspector = ViewInspector.create();
  }

  @Pointcut("within(android.app.Activity+)") public void withinActivityClass() {
  }

  @Pointcut("execution(void onCreate(..)) && withinActivityClass()")
  public void activityOnCreatedCall() {
  }

  @Around("activityOnCreatedCall()")
  public Object injectViewInspector(ProceedingJoinPoint joinPoint) throws Throwable {
    Log.d(ViewInspector.TAG, "injectViewInspector");
    Context context = (Context) joinPoint.getThis();

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      if (Settings.canDrawOverlays(context)) {
        mViewInspector.onCreate(context);
        isRestarting = false;
      } else {
        isRequestingOverlayPermission = true;
        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
        ((Activity) context).startActivityForResult(intent, OVERLAY_PERMISSION_CALL);
      }
    } else {
      mViewInspector.onCreate(context);
    }

    return joinPoint.proceed();
  }

  @Pointcut("execution(void onResume()) && withinActivityClass()")
  public void activityOnResumeCall() {
  }

  @Around("activityOnResumeCall()") public Object showViewInspector(ProceedingJoinPoint joinPoint)
      throws Throwable {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      Context context = (Context) joinPoint.getThis();
      if (isRequestingOverlayPermission) {
        if (Settings.canDrawOverlays(context)) {
          // relaunching the app for deploying Probe features
          Intent intent =
              context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
          intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
          ((Activity) context).finish();
          isRequestingOverlayPermission = false;
          isRestarting = true;
          context.startActivity(intent);
        }
      } else {
        mViewInspector.onResume();
      }
    } else {
      mViewInspector.onResume();
    }

    return joinPoint.proceed();
  }

  @Pointcut("execution(void onPause()) && withinActivityClass()")
  public void activityOnPauseCall() {
  }

  @Around("activityOnPauseCall()") public Object hideViewInspector(ProceedingJoinPoint joinPoint)
      throws Throwable {
    if (!isRequestingOverlayPermission && !isRestarting) {
      mViewInspector.onPause();
    }
    return joinPoint.proceed();
  }

  @Pointcut("execution(void onDestroy()) && withinActivityClass()")
  public void activityOnDestroyCall() {
  }

  @Around("activityOnDestroyCall()")
  public Object destroyViewInspector(ProceedingJoinPoint joinPoint) throws Throwable {
    if (!isRequestingOverlayPermission && !isRestarting) {
      Context context = (Context) joinPoint.getThis();
      mViewInspector.onDestroy(context);
    }
    return joinPoint.proceed();
  }
}
