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
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import java.io.IOException;
import java.lang.reflect.Modifier;
import view_inspector.R;
import view_inspector.ViewInspector;

import static view_inspector.probe.ViewClassUtil.findViewClass;

/**
 * {@link LayoutInflater.Factory2} used by a {@link Probe} instance to
 * inflate layout resources. It will wrap target {@link View}s with dynamic
 * proxy classes that redirect their method calls to an {@link Interceptor}.
 *
 * @see Interceptor
 * @see ViewProxyBuilder
 */
class ProbeViewFactory implements LayoutInflater.Factory2 {
  private static final String TAG_FRAGMENT = "fragment";
  private static final String TAG_INTERNAL_CLASS = "com.android.internal";
  private static final String TAG_INTERNAL_CLASS_SUPPORT_V7 = "android.support.v7.internal";
  private static final String TAG_VIEW_STUB = "ViewStub";
  private static final String TAG_VIEW_INSPECTOR = "view_inspector";

  private final Context mContext;
  private final Probe mProbe;

  ProbeViewFactory(Context context, Probe probe) {
    mContext = context;
    mProbe = probe;
  }

  private View createProxyView(Context context, String name, AttributeSet attrs)
      throws ClassNotFoundException {
    try {
      final Class<?> viewClass = findViewClass(mContext, name);

      // Probe can't wrap final or abstract View classes, just bail.
      final int modifiers = viewClass.getModifiers();
      if (Modifier.isFinal(modifiers) || Modifier.isAbstract(modifiers)) {
        return null;
      }

      if (isExcluded(viewClass.getName())) {
        return null;
      }

      return ViewProxyBuilder.forClass(context, viewClass)
          .constructorArgValues(mContext, attrs)
          .interceptor(mProbe.getInterceptor())
          .build();
    } catch (ClassCastException e) {
      // Not a View subclass, just bail.
      return null;
    } catch (IOException e) {
      throw new IllegalStateException("Failed to create View proxy", e);
    }
  }

  @Override
  public View onCreateView(View parent, String name, Context context, AttributeSet attrs) {
    Log.d(ViewInspector.TAG, "ProbeViewFactory > onCreateView > name = " + name);
    if (name.equals(TAG_FRAGMENT) ||
        name.startsWith(TAG_INTERNAL_CLASS) ||
        name.startsWith(TAG_INTERNAL_CLASS_SUPPORT_V7) ||
        name.contains(TAG_VIEW_STUB) ||
        name.startsWith(TAG_VIEW_INSPECTOR)) {
      Log.d(ViewInspector.TAG, "ProbeViewFactory > view skipped.");
      return null;
    }

    try {
      return createProxyView(context, name, attrs);
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    }

    return null;
  }

  private boolean isExcluded(String viewName) {
    String[] excludePackages = mContext.getResources().getStringArray(R.array.excludePackages);
    for (String excludePackage : excludePackages) {
      if (viewName.startsWith(excludePackage)) return true;
    }
    return false;
  }

  @Override public View onCreateView(String name, Context context, AttributeSet attrs) {
    return onCreateView(null, name, context, attrs);
  }
}

