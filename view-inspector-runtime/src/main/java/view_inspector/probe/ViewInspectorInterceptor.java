/*
 * Copyright (C) 2015 Fumihiro Xue
 * Copyright (C) 2014 Lucas Rocha
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package view_inspector.probe;

import android.content.Context;
import android.graphics.Canvas;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import com.f2prateek.rx.preferences.Preference;
import java.util.List;
import java.util.Set;
import java.util.WeakHashMap;
import javax.inject.Inject;
import rx.functions.Action1;
import view_inspector.ViewInspector;
import view_inspector.dagger.qualifier.BypassInterceptor;
import view_inspector.dagger.qualifier.LogViewEvents;
import view_inspector.dagger.qualifier.ProbeMeasures;
import view_inspector.dagger.qualifier.Profiling;
import view_inspector.dagger.qualifier.Scalpel3D;
import view_inspector.dagger.qualifier.ScalpelShowId;
import view_inspector.dagger.qualifier.ScalpelWireframe;
import view_inspector.dagger.qualifier.ShowMargin;
import view_inspector.dagger.qualifier.ShowMeasureCount;
import view_inspector.dagger.qualifier.ShowOutline;
import view_inspector.dagger.qualifier.ShowPadding;
import view_inspector.dagger.qualifier.ViewFilter;
import view_inspector.dagger.qualifier.ViewSuspects;
import view_inspector.dagger.qualifier.ViewTag;
import view_inspector.dagger.scope.PerActivity;
import view_inspector.probe.util.DrawUtil;
import view_inspector.probe.util.LogUtil;
import view_inspector.profile.ProfileUtil;
import view_inspector.ui.ViewInspectorScalpelLayout;
import view_inspector.util.ViewUtil;

@PerActivity @SuppressWarnings("ConstantConditions") public class ViewInspectorInterceptor
    extends Interceptor {

  private final WeakHashMap<View, Integer> mMeasureByView;
  private final Context mContext;

  @Inject @ShowOutline Preference<Boolean> showOutline;
  @Inject @ShowMargin Preference<Boolean> showMargin;
  @Inject @ShowPadding Preference<Boolean> showPadding;
  @Inject @BypassInterceptor Preference<Boolean> bypassInterceptor;
  @Inject @ProbeMeasures Preference<Boolean> probeMeasures;
  @Inject @ShowMeasureCount Preference<Boolean> showMeasureCount;
  @Inject @Profiling Preference<Boolean> profiling;
  @Inject @LogViewEvents Preference<Boolean> logViewEvents;
  @Inject @Scalpel3D Preference<Boolean> scalpel3D;
  @Inject @ScalpelWireframe Preference<Boolean> scalpelWireframe;
  @Inject @ScalpelShowId Preference<Boolean> scalpelID;
  @Inject @ViewSuspects List<View> viewSuspects;
  @Inject ProfileUtil profileUtil;
  @Inject @ViewTag String viewTag;
  @Inject ViewInspectorScalpelLayout scalpelLayout;
  @Inject @ViewFilter Preference<Set<String>> viewFilterSet;

  @Inject public ViewInspectorInterceptor(final Context context) {
    ViewInspector.runtimeComponentMap.get(context).inject(this);

    this.mContext = context;
    mMeasureByView = new WeakHashMap<>();

    showOutline.asObservable().subscribe(new Action1<Boolean>() {
      @Override public void call(Boolean aBoolean) {
        invalidateScalpelAndSuspects();
      }
    });

    showMargin.asObservable().subscribe(new Action1<Boolean>() {
      @Override public void call(Boolean aBoolean) {
        invalidateScalpelAndSuspects();
      }
    });

    showPadding.asObservable().subscribe(new Action1<Boolean>() {
      @Override public void call(Boolean aBoolean) {
        invalidateScalpelAndSuspects();
      }
    });

    probeMeasures.asObservable().subscribe(new Action1<Boolean>() {
      @Override public void call(Boolean aBoolean) {
        if (ViewInspector.viewRoot != null) ViewInspector.viewRoot.requestLayout();
        invalidateScalpelAndSuspects();
      }
    });

    showMeasureCount.asObservable().subscribe(new Action1<Boolean>() {
      @Override public void call(Boolean aBoolean) {
        invalidateScalpelAndSuspects();
      }
    });

    profiling.asObservable().subscribe(new Action1<Boolean>() {
      @Override public void call(Boolean aBoolean) {
        // reset add-ons before profiling
        showOutline.set(false);
        showMargin.set(false);
        showPadding.set(false);
        scalpel3D.set(false);
        probeMeasures.set(false);

        bypassInterceptor.set(aBoolean);
        if (!aBoolean) {
          if (ViewInspector.viewRoot != null) ViewInspector.viewRoot.requestLayout();
          invalidateScalpelAndSuspects();
        }
      }
    });

    scalpel3D.asObservable().subscribe(new Action1<Boolean>() {
      @Override public void call(Boolean aBoolean) {
        scalpelLayout.setLayerInteractionEnabled(aBoolean);
      }
    });

    scalpelWireframe.asObservable().subscribe(new Action1<Boolean>() {
      @Override public void call(Boolean aBoolean) {
        scalpelLayout.setDrawViews(!aBoolean);
      }
    });

    scalpelID.asObservable().subscribe(new Action1<Boolean>() {
      @Override public void call(Boolean aBoolean) {
        scalpelLayout.setDrawIds(aBoolean);
      }
    });
  }

  @Override public void draw(View view, Canvas canvas) {
    super.draw(view, canvas);
    if (isNotBypassed(view) && isNotFiltered(view)) {
      if (showOutline.get()) DrawUtil.drawOutline(mContext, view, canvas);
      if (showMargin.get()) DrawUtil.drawMargin(view, canvas);
      if (showPadding.get()) DrawUtil.drawPadding(view, canvas);
    }
  }

  @Override public void onMeasure(View view, int widthMeasureSpec, int heightMeasureSpec) {
    if (isNotBypassed(view) && isNotFiltered(view)) {
      if (ViewInspector.viewRoot == null && ViewUtil.isLevelTwoView(view)) {
        Log.d(ViewInspector.TAG, "insert ScalpelLayout as a parent of view: " + view);
        insertScalpelLayout(view);
      }

      if (logViewEvents.get()) LogUtil.logOnMeasure(view, widthMeasureSpec, heightMeasureSpec);

      if (probeMeasures.get()) {
        if (ViewUtil.isViewRoot(view)) mMeasureByView.clear();
      }
    }

    super.onMeasure(view, widthMeasureSpec, heightMeasureSpec);

    if (probeMeasures.get() && isNotBypassed(view) && isNotFiltered(view)) {
      if (!(view instanceof ViewGroup)) {
        final Integer measureCount = mMeasureByView.get(view);
        mMeasureByView.put(view, (measureCount != null ? measureCount : 0) + 1);
      }
    }
  }

  @Override public void onLayout(View view, boolean changed, int l, int t, int r, int b) {
    if (isNotBypassed(view)) {
      if (!viewSuspects.contains(view)) viewSuspects.add(view);
      if (logViewEvents.get() && isNotFiltered(view)) {
        LogUtil.logOnLayout(view, changed, l, t, r, b);
      }
    }

    super.onLayout(view, changed, l, t, r, b);
  }

  @Override public void onDraw(View view, Canvas canvas) {
    if (isNotBypassed(view) && isNotFiltered(view)) {
      if (logViewEvents.get()) LogUtil.logOnDraw(view);
    }

    super.onDraw(view, canvas);

    if (probeMeasures.get() && isNotBypassed(view) && isNotFiltered(view)) {
      if (view instanceof ViewGroup || isExcluded(view)) {
        return;
      }

      if (!ViewUtil.isViewRoot(view)) {
        Integer measureCount = mMeasureByView.get(view);
        if (measureCount == null) measureCount = 0;
        DrawUtil.drawMeasures(view, canvas, measureCount);
        if (showMeasureCount.get()) DrawUtil.drawMeasureCount(mContext, view, canvas, measureCount);
      }
    }
  }

  @Override public void requestLayout(View view) {
    if (isNotBypassed(view) && isNotFiltered(view)) {
      if (logViewEvents.get()) LogUtil.logRequestLayout(view);
    }

    super.requestLayout(view);

    if (probeMeasures.get() && isNotBypassed(view)) {
      if (ViewUtil.isViewRoot(view)) {
        // Clear all measure spec caches and make sure all the
        // views will be redrawn.
        forceLayoutRecursive(view);
        invalidate(view);
      }
    }
  }

  @Override public void forceLayout(View view) {
    if (isNotBypassed(view) && isNotFiltered(view)) {
      if (logViewEvents.get()) LogUtil.logForceLayout(view);
    }

    super.forceLayout(view);
  }

  private void forceLayoutRecursive(View view) {
    if (!ViewUtil.isNotSupportedViewClass(view)) {
      invokeForceLayout(view);

      if (view instanceof ViewGroup) {
        final ViewGroup viewGroup = (ViewGroup) view;

        final int count = viewGroup.getChildCount();
        for (int i = 0; i < count; i++) {
          forceLayoutRecursive(viewGroup.getChildAt(i));
        }
      }
    }
  }

  private void invalidate(View view) {
    view.invalidate();

    if (view instanceof ViewGroup) {
      final ViewGroup viewGroup = (ViewGroup) view;

      final int count = viewGroup.getChildCount();
      for (int i = 0; i < count; i++) {
        invalidate(viewGroup.getChildAt(i));
      }
    }
  }

  private void insertScalpelLayout(View view) {
    ViewGroup viewParent = (ViewGroup) view.getParent();
    ViewGroup.LayoutParams layoutParams =
        new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT);
    if (ViewUtil.isActionBarOverlayLayout(viewParent)) {
      View viewChild = ((ViewGroup) view).getChildAt(0);
      ((ViewGroup) view).removeView(viewChild);
      ((ViewGroup) view).addView(scalpelLayout, layoutParams);
      scalpelLayout.addView(viewChild);
    } else {
      viewParent.removeView(view);
      viewParent.addView(scalpelLayout, layoutParams);
      scalpelLayout.addView(view);
    }
    ViewInspector.viewRoot = view;
  }

  public void setViewRoot(View view) {
    bypassInterceptor.set(false);
    probeMeasures.set(false);
    ViewInspector.viewRoot = view;
    probeMeasures.set(true);
  }

  private boolean isExcluded(View view) {
    return viewTag.equals(view.getTag());
  }

  private boolean isNotBypassed(View view) {
    return !isExcluded(view) && !bypassInterceptor.get();
  }

  private void invalidateViewSuspects() {
    for (View view : viewSuspects) {
      view.invalidate();
    }
  }

  private boolean isNotFiltered(View view) {
    return !viewFilterSet.get().contains(ViewUtil.getClassName(view));
  }

  public void invalidateScalpelAndSuspects() {
    scalpelLayout.invalidate();
    invalidateViewSuspects();
  }
}
