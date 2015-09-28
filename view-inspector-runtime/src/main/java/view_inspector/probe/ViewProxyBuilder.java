/*
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
import android.view.View;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import view_inspector.ViewInspector;

import static view_inspector.probe.ViewClassUtil.findProxyViewClass;

/**
 * Builds a proxy class that redirects {@link View} layout-related method
 * calls to an {@link Interceptor}.
 *
 * {@link view_inspector.probe.ViewProxyBuilder} is used by {@link view_inspector.probe.ProbeViewFactory}
 * to
 * wrap the inflated {@link View} instances for a given {@link Probe}.
 *
 * @see Probe
 * @see view_inspector.probe.ProbeViewFactory
 */
final class ViewProxyBuilder<T extends View> {
  private static final Map<Class<?>, Class<?>> sGeneratedProxyClasses =
      Collections.synchronizedMap(new HashMap<Class<?>, Class<?>>());

  static final Class<?>[] CONSTRUCTOR_ARG_TYPES = new Class<?>[] {
      Context.class, AttributeSet.class
  };
  private static final Object[] CONSTRUCTOR_ARG_VALUES = new Object[2];

  private final Context mContext;
  private final Class<T> mBaseClass;
  private final ClassLoader mParentClassLoader;
  private Interceptor mInterceptor;

  private ViewProxyBuilder(Context context, Class<T> clazz) {
    mContext = context;
    mBaseClass = clazz;
    mParentClassLoader = context.getClassLoader();
  }

  private static <T> Constructor<? extends T> getProxyClassConstructor(
      Class<? extends T> proxyClass) {
    final Constructor<? extends T> constructor;
    try {
      constructor = proxyClass.getConstructor(CONSTRUCTOR_ARG_TYPES);
    } catch (NoSuchMethodException e) {
      throw new IllegalStateException(
          "No constructor for " + proxyClass.getName() + " with parameter types " + Arrays.toString(
              CONSTRUCTOR_ARG_TYPES));
    }

    return constructor;
  }

  /**
   * Generates dynamic {@link View} proxy class.
   */
  @SuppressWarnings("unchecked") private Class<? extends T> generateProxyClass()
      throws IOException {
    if (ViewProxy.class.isAssignableFrom(mBaseClass)) {
      return mBaseClass;
    }

    Class<? extends T> proxyClass = (Class) sGeneratedProxyClasses.get(mBaseClass);
    if (proxyClass != null && (proxyClass.getClassLoader() == mParentClassLoader
        || proxyClass.getClassLoader().getParent() == mParentClassLoader)) {
      // Cache hit; return immediately.
      return proxyClass;
    }

    proxyClass = (Class<? extends T>) findProxyViewClass(mContext, mBaseClass.getName());
    if (proxyClass != null) {
      Log.d(ViewInspector.TAG, "build-time proxy found. proxyClass = " + proxyClass);
      // This app ships with the build-time proxy.
      sGeneratedProxyClasses.put(mBaseClass, proxyClass);
      return proxyClass;
    }

    Log.d(ViewInspector.TAG, "Build proxy with DexMaker > mBaseClass = " + mBaseClass);

    try {
      Class.forName("com.google.dexmaker.DexMaker");
      return DexProxyBuilder.generateProxyClass(mContext, mBaseClass);
    } catch (ClassNotFoundException e) {
      return null;
    }
  }

  private static RuntimeException launderCause(InvocationTargetException e) {
    final Throwable cause = e.getCause();

    // Errors should be thrown as they are.
    if (cause instanceof Error) {
      throw (Error) cause;
    }

    // RuntimeException can be thrown as-is.
    if (cause instanceof RuntimeException) {
      throw (RuntimeException) cause;
    }

    // Declared exceptions will have to be wrapped.
    throw new UndeclaredThrowableException(cause);
  }

  @SuppressWarnings("unchecked") static <T> ViewProxyBuilder forClass(Context context, Class<T> clazz) {
    return new ViewProxyBuilder(context, clazz);
  }

  ViewProxyBuilder interceptor(Interceptor interceptor) {
    mInterceptor = interceptor;
    return this;
  }

  ViewProxyBuilder constructorArgValues(Context context, AttributeSet attrs) {
    CONSTRUCTOR_ARG_VALUES[0] = context;
    CONSTRUCTOR_ARG_VALUES[1] = attrs;
    return this;
  }

  /**
   * Builds instance of the built {@link View} proxy class..
   */
  View build() throws IOException {
    final Class<? extends T> proxyClass = generateProxyClass();
    if (proxyClass == null) {
      return null;
    }

    final Constructor<? extends T> constructor = getProxyClassConstructor(proxyClass);

    final View result;
    try {
      result = constructor.newInstance(CONSTRUCTOR_ARG_VALUES);
    } catch (InstantiationException e) {
      // Should not be thrown, generated class is not abstract.
      throw new AssertionError(e);
    } catch (IllegalAccessException e) {
      // Should not be thrown, the generated constructor is accessible.
      throw new AssertionError(e);
    } catch (InvocationTargetException e) {
      // Thrown when the base class constructor throws an exception.
      throw launderCause(e);
    }

    ((ViewProxy) result).setInterceptor(mInterceptor);
    return result;
  }
}

