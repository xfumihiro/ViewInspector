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
import android.view.View;

/**
 * Utility methods to find and load {@link View} classes. Used
 * by {@link ProbeViewFactory}.
 *
 * @see ProbeViewFactory
 */
class ViewClassUtil {
  private ViewClassUtil() {
  }

  static final String[] VIEW_CLASS_PREFIX_LIST = {
      "android.widget.", "android.view.", "android.webkit."
  };

  /**
   * Loads class for the given class name.
   */
  static Class<?> loadViewClass(Context context, String name) throws ClassNotFoundException {
    return context.getClassLoader().loadClass(name).asSubclass(View.class);
  }

  /*
   * Tries to load the view proxy class generated at build time for the
   * given class name.
   */
  static Class<?> findProxyViewClass(Context context, String name) {
    try {
      return loadViewClass(context,
          String.format("%s.view_inspector.ProbeProxy$%s", context.getPackageName(),
              name.replace('.', '_')));
    } catch (ClassNotFoundException e) {
      return null;
    }
  }

  /**
   * Tries to load class using a predefined list of class prefixes for
   * Android views.
   */
  static Class<?> findViewClass(Context context, String name) throws ClassNotFoundException {
    if (name.indexOf('.') >= 0) {
      return loadViewClass(context, name);
    }

    for (String prefix : VIEW_CLASS_PREFIX_LIST) {
      try {
        return loadViewClass(context, prefix + name);
      } catch (ClassNotFoundException e) {
        continue;
      }
    }

    throw new ClassNotFoundException("Couldn't load View class for " + name);
  }
}