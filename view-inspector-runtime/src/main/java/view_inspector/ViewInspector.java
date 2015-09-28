package view_inspector;

import android.content.Context;
import android.view.View;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import view_inspector.dagger.ActivityComponent;
import view_inspector.dagger.ApplicationComponent;
import view_inspector.ui.ViewInspectorToolbar;

public final class ViewInspector {
  public static final String TAG = "ViewInspector";
  public static ApplicationComponent applicationComponent;
  public static Set<Context> contextSet = new HashSet<>();
  public static Map<Context, ActivityComponent> runtimeComponentMap = new HashMap<>();
  public static Map<Context, ViewInspectorToolbar> toolbarMap = new HashMap<>();
  public static View viewRoot;

  private ViewInspector() {
    throw new AssertionError("No instances.");
  }
}
