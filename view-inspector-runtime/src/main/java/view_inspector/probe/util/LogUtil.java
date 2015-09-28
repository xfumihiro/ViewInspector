package view_inspector.probe.util;

import android.util.Log;
import android.view.View;
import view_inspector.ViewInspector;
import view_inspector.util.ViewUtil;

public final class LogUtil {
  private static final int TAB_SPACE = 70;
  private static final int LEVEL_SPACE = 4;
  private static final int EVENT_SPACE = 15;
  private static final int MEASURE_WH_SPACE = 6;
  private static final int WIDTH_SPEC_SPACE = 15;
  private static final int HEIGHT_SPEC_SPACE = 11;
  private static final int LAYOUT_CHANGED_SPACE = 10;
  private static final int LAYOUT_LTRB_SPACE = 7;

  public static void logOnMeasure(View view, int widthMeasureSpec, int heightMeasureSpec) {
    int level = ViewUtil.getViewLevel(view);
    String eventName = "onMeasure";
    StringBuilder stringBuilder = new StringBuilder(eventName);
    int eventPostfix = EVENT_SPACE - eventName.length();
    for (int i = 0; i < eventPostfix; i++) stringBuilder.append(" ");
    String className = ViewUtil.getClassName(view);
    String viewId = ViewUtil.getSimpleViewId(view);
    for (int i = 0; i < level; i++) stringBuilder.append("\t\t");
    stringBuilder.append("> ").append(className).append(" [").append(viewId).append("] ");
    for (int i = className.length() + viewId.length(); i < TAB_SPACE - level * LEVEL_SPACE; i++)
      stringBuilder.append(" ");

    int width = View.MeasureSpec.getSize(widthMeasureSpec);
    int widthPostfix = MEASURE_WH_SPACE - String.valueOf(width).length();
    int height = View.MeasureSpec.getSize(heightMeasureSpec);
    int heightPostfix = MEASURE_WH_SPACE - String.valueOf(height).length();
    String widthSpec =
        View.MeasureSpec.toString(widthMeasureSpec).split(":")[1].split(" ")[1].toLowerCase();
    int widthSpecPostfix = WIDTH_SPEC_SPACE - widthSpec.length();
    String heightSpec =
        View.MeasureSpec.toString(heightMeasureSpec).split(":")[1].split(" ")[1].toLowerCase();
    int heightSpecPostfix = HEIGHT_SPEC_SPACE - heightSpec.length();

    stringBuilder.append(" [w: ").append(width);
    for (int i = 0; i < widthPostfix; i++) stringBuilder.append(" ");
    stringBuilder.append(widthSpec);
    for (int i = 0; i < widthSpecPostfix; i++) stringBuilder.append(" ");
    stringBuilder.append(" ,h: ").append(height);
    for (int i = 0; i < heightPostfix; i++) stringBuilder.append(" ");
    stringBuilder.append(heightSpec);
    for (int i = 0; i < heightSpecPostfix; i++) stringBuilder.append(" ");
    stringBuilder.append("]");

    Log.d(ViewInspector.TAG, stringBuilder.toString());
  }

  public static void logOnLayout(View view, boolean changed, int l, int t, int r, int b) {
    int level = ViewUtil.getViewLevel(view);
    String eventName = "onLayout";
    StringBuilder stringBuilder = new StringBuilder(eventName);
    int eventPostfix = EVENT_SPACE - eventName.length();
    for (int i = 0; i < eventPostfix; i++) stringBuilder.append(" ");
    String className = ViewUtil.getClassName(view);
    String viewId = ViewUtil.getSimpleViewId(view);
    for (int i = 0; i < level; i++) stringBuilder.append("\t\t");
    stringBuilder.append("> ").append(className).append(" [").append(viewId).append("] ");
    for (int i = className.length() + viewId.length();
        i < TAB_SPACE - level * LEVEL_SPACE - LAYOUT_CHANGED_SPACE; i++)
      stringBuilder.append(" ");

    String changedFlag = changed ? " (changed)" : " ";
    int changedPostfix = LAYOUT_CHANGED_SPACE - changedFlag.length();
    stringBuilder.append(changedFlag);
    for (int i = 0; i < changedPostfix; i++) stringBuilder.append(" ");
    stringBuilder.append(" [");
    int leftPostfix = LAYOUT_LTRB_SPACE - String.valueOf(l).length();
    stringBuilder.append("l: ").append(l);
    for (int i = 0; i < leftPostfix; i++) stringBuilder.append(" ");
    stringBuilder.append(" ,");
    int topPostfix = LAYOUT_LTRB_SPACE - String.valueOf(t).length();
    stringBuilder.append("t: ").append(t);
    for (int i = 0; i < topPostfix; i++) stringBuilder.append(" ");
    stringBuilder.append(" ,");
    int rightPostfix = LAYOUT_LTRB_SPACE - String.valueOf(r).length();
    stringBuilder.append("r: ").append(r);
    for (int i = 0; i < rightPostfix; i++) stringBuilder.append(" ");
    stringBuilder.append(" ,");
    int bottomPostfix = LAYOUT_LTRB_SPACE - String.valueOf(b).length();
    stringBuilder.append("b: ").append(b);
    for (int i = 0; i < bottomPostfix; i++) stringBuilder.append(" ");
    stringBuilder.append("]");

    Log.d(ViewInspector.TAG, stringBuilder.toString());
  }

  public static void logOnDraw(View view) {
    int level = ViewUtil.getViewLevel(view);
    String eventName = "onDraw";
    StringBuilder stringBuilder = new StringBuilder(eventName);
    int eventPostfix = EVENT_SPACE - eventName.length();
    for (int i = 0; i < eventPostfix; i++) stringBuilder.append(" ");
    String className = ViewUtil.getClassName(view);
    String viewId = ViewUtil.getSimpleViewId(view);
    for (int i = 0; i < level; i++) stringBuilder.append("\t\t");
    stringBuilder.append("> ").append(className).append(" [").append(viewId).append("] ");
    for (int i = className.length() + viewId.length(); i < TAB_SPACE - level * LEVEL_SPACE; i++)
      stringBuilder.append(" ");

    Log.d(ViewInspector.TAG, stringBuilder.toString());
  }

  public static void logRequestLayout(View view) {
    String eventName = "requestLayout";
    StringBuilder stringBuilder = new StringBuilder(eventName);
    int eventPostfix = EVENT_SPACE - eventName.length();
    for (int i = 0; i < eventPostfix; i++) stringBuilder.append(" ");
    String className = ViewUtil.getClassName(view);
    String viewId = ViewUtil.getSimpleViewId(view);
    stringBuilder.append("\t\t> ").append(className).append(" [").append(viewId).append("] ");
    Log.d(ViewInspector.TAG, stringBuilder.toString());
  }

  public static void logForceLayout(View view) {
    int level = ViewUtil.getViewLevel(view);
    String eventName = "forceLayout";
    StringBuilder stringBuilder = new StringBuilder(eventName);
    int eventPostfix = EVENT_SPACE - eventName.length();
    for (int i = 0; i < eventPostfix; i++) stringBuilder.append(" ");
    String className = ViewUtil.getClassName(view);
    String viewId = ViewUtil.getSimpleViewId(view);
    for (int i = 0; i < level; i++) stringBuilder.append("\t\t");
    stringBuilder.append("> ").append(className).append(" [").append(viewId).append("] ");
    Log.d(ViewInspector.TAG, stringBuilder.toString());
  }

  private LogUtil() {
    throw new AssertionError("No instances.");
  }
}
