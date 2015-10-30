package view_inspector.aspect;

import android.content.Context;
import android.util.Log;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import view_inspector.ViewInspector;

@Aspect public class ViewInspectorAspect {
  private static final int OVERLAY_PERMISSION_CALL = 1;

  private ViewInspector mViewInspector;

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
    mViewInspector.onCreate((Context) joinPoint.getThis());
    return joinPoint.proceed();
  }

  @Pointcut("execution(void onResume()) && withinActivityClass()")
  public void activityOnResumeCall() {
  }

  @Around("activityOnResumeCall()") public Object showViewInspector(ProceedingJoinPoint joinPoint)
      throws Throwable {
    mViewInspector.onResume();
    return joinPoint.proceed();
  }

  @Pointcut("execution(void onPause()) && withinActivityClass()")
  public void activityOnPauseCall() {
  }

  @Around("activityOnPauseCall()") public Object hideViewInspector(ProceedingJoinPoint joinPoint)
      throws Throwable {
    mViewInspector.onPause();
    return joinPoint.proceed();
  }

  @Pointcut("execution(void onDestroy()) && withinActivityClass()")
  public void activityOnDestroyCall() {
  }

  @Around("activityOnDestroyCall()")
  public Object destroyViewInspector(ProceedingJoinPoint joinPoint) throws Throwable {
    mViewInspector.onDestroy((Context) joinPoint.getThis());
    return joinPoint.proceed();
  }
}
