/*
 * Copyright (C) 2014 Lucas Rocha
 *
 * This code is based on bits and pieces of DexMaker's ProxyBuilder.
 *
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
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
import android.view.View;
import com.google.dexmaker.Code;
import com.google.dexmaker.Comparison;
import com.google.dexmaker.DexMaker;
import com.google.dexmaker.FieldId;
import com.google.dexmaker.Label;
import com.google.dexmaker.Local;
import com.google.dexmaker.MethodId;
import com.google.dexmaker.TypeId;
import java.io.IOException;

import static java.lang.reflect.Modifier.PRIVATE;
import static java.lang.reflect.Modifier.PUBLIC;
import static view_inspector.probe.ViewProxyBuilder.CONSTRUCTOR_ARG_TYPES;

/**
 * {@link view_inspector.probe.DexProxyBuilder} is used by {@link view_inspector.probe.ProbeViewFactory}
 * to dynamically wrap the inflated {@link View} instances for a given
 * {@link view_inspector.probe.Probe}.
 *
 * @see view_inspector.probe.Probe
 * @see view_inspector.probe.ViewProxyBuilder
 * @see view_inspector.probe.ProbeViewFactory
 */
final class DexProxyBuilder {
  private enum ViewMethod {
    ON_MEASURE("onMeasure"),
    ON_LAYOUT("onLayout"),
    DRAW("draw"),
    ON_DRAW("onDraw"),
    REQUEST_LAYOUT("requestLayout"),
    FORCE_LAYOUT("forceLayout"),
    SET_MEASURED_DIMENSION("setMeasuredDimension"),
    SET_INTERCEPTOR("setInterceptor");

    private final String mMethodName;
    private final String mInvokeMethodName;

    private ViewMethod(String methodName) {
      mMethodName = methodName;
      mInvokeMethodName = "invoke" + Character.toUpperCase(methodName.charAt(0)) +
          methodName.substring(1);
    }

    String getName() {
      return mMethodName;
    }

    String getInvokeName() {
      return mInvokeMethodName;
    }
  }

  private static final String DEX_CACHE_DIRECTORY = "probe";

  private static final String FIELD_NAME_INTERCEPTOR = "mInterceptor";

  private static final TypeId<Canvas> CANVAS_TYPE = TypeId.get(Canvas.class);
  private static final TypeId<Interceptor> INTERCEPTOR_TYPE = TypeId.get(Interceptor.class);
  private static final TypeId<View> VIEW_TYPE = TypeId.get(View.class);
  private static final TypeId<ViewProxy> INTERCEPTABLE_VIEW_TYPE = TypeId.get(ViewProxy.class);
  private static final TypeId<Void> VOID_TYPE = TypeId.get(void.class);

  private DexProxyBuilder() {
  }

  /**
   * Generates class field that holds a reference to the associated
   * {@link view_inspector.probe.Interceptor} instance and the {@link View} constructor.
   */
  private static <T, G extends T> void generateConstructorAndFields(DexMaker dexMaker,
      TypeId<G> generatedType, TypeId<T> baseType) {
    final FieldId<G, Interceptor> interceptorField =
        generatedType.getField(INTERCEPTOR_TYPE, FIELD_NAME_INTERCEPTOR);
    dexMaker.declare(interceptorField, PRIVATE, null);

    final TypeId<?>[] types = classArrayToTypeArray(CONSTRUCTOR_ARG_TYPES);
    final MethodId<?, ?> constructor = generatedType.getConstructor(types);
    final Code constructorCode = dexMaker.declare(constructor, PUBLIC);

    final Local<?>[] params = new Local[types.length];
    for (int i = 0; i < params.length; ++i) {
      params[i] = constructorCode.getParameter(i, types[i]);
    }

    final MethodId<T, ?> superConstructor = baseType.getConstructor(types);
    final Local<G> thisRef = constructorCode.getThis(generatedType);
    constructorCode.invokeDirect(superConstructor, null, thisRef, params);
    constructorCode.returnVoid();
  }

  /**
   * Generates the {@link View#onMeasure(int, int)} method for the proxy class.
   */
  private static <T, G extends T> void generateOnMeasureMethod(DexMaker dexMaker,
      TypeId<G> generatedType, TypeId<T> baseType) {
    final FieldId<G, Interceptor> interceptorField =
        generatedType.getField(INTERCEPTOR_TYPE, FIELD_NAME_INTERCEPTOR);

    final String methodName = ViewMethod.ON_MEASURE.getName();

    final MethodId<T, Void> superMethod =
        baseType.getMethod(VOID_TYPE, methodName, TypeId.INT, TypeId.INT);
    final MethodId<Interceptor, Void> onMeasureMethod =
        INTERCEPTOR_TYPE.getMethod(VOID_TYPE, methodName, VIEW_TYPE, TypeId.INT, TypeId.INT);

    final MethodId<G, Void> methodId =
        generatedType.getMethod(VOID_TYPE, methodName, TypeId.INT, TypeId.INT);
    final Code code = dexMaker.declare(methodId, PUBLIC);

    final Local<G> localThis = code.getThis(generatedType);
    final Local<Interceptor> nullInterceptor = code.newLocal(INTERCEPTOR_TYPE);
    final Local<Interceptor> localInterceptor = code.newLocal(INTERCEPTOR_TYPE);
    final Local<Integer> localWidth = code.getParameter(0, TypeId.INT);
    final Local<Integer> localHeight = code.getParameter(1, TypeId.INT);

    code.iget(interceptorField, localInterceptor, localThis);
    code.loadConstant(nullInterceptor, null);

    // Interceptor is not null, call it.
    final Label interceptorNullCase = new Label();
    code.compare(Comparison.EQ, interceptorNullCase, nullInterceptor, localInterceptor);
    code.invokeVirtual(onMeasureMethod, null, localInterceptor, localThis, localWidth, localHeight);
    code.returnVoid();

    // Interceptor is null, call super method.
    code.mark(interceptorNullCase);
    code.invokeSuper(superMethod, null, localThis, localWidth, localHeight);
    code.returnVoid();

    final MethodId<G, Void> callsSuperMethod =
        generatedType.getMethod(VOID_TYPE, ViewMethod.ON_MEASURE.getInvokeName(), TypeId.INT,
            TypeId.INT);

    final Code superCode = dexMaker.declare(callsSuperMethod, PUBLIC);

    final Local<G> superThis = superCode.getThis(generatedType);
    final Local<Integer> superLocalWidth = superCode.getParameter(0, TypeId.INT);
    final Local<Integer> superLocalHeight = superCode.getParameter(1, TypeId.INT);
    superCode.invokeSuper(superMethod, null, superThis, superLocalWidth, superLocalHeight);
    superCode.returnVoid();
  }

  /**
   * Generates the {@link View#onLayout(boolean, int, int, int, int)} method
   * for the proxy class.
   */
  private static <T, G extends T> void generateOnLayoutMethod(DexMaker dexMaker,
      TypeId<G> generatedType, TypeId<T> baseType) {
    final FieldId<G, Interceptor> interceptorField =
        generatedType.getField(INTERCEPTOR_TYPE, FIELD_NAME_INTERCEPTOR);

    final String methodName = ViewMethod.ON_LAYOUT.getName();

    final MethodId<T, Void> superMethod =
        baseType.getMethod(VOID_TYPE, methodName, TypeId.BOOLEAN, TypeId.INT, TypeId.INT,
            TypeId.INT, TypeId.INT);
    final MethodId<Interceptor, Void> onLayoutMethod =
        INTERCEPTOR_TYPE.getMethod(VOID_TYPE, methodName, VIEW_TYPE, TypeId.BOOLEAN, TypeId.INT,
            TypeId.INT, TypeId.INT, TypeId.INT);

    final MethodId<G, Void> methodId =
        generatedType.getMethod(VOID_TYPE, methodName, TypeId.BOOLEAN, TypeId.INT, TypeId.INT,
            TypeId.INT, TypeId.INT);
    final Code code = dexMaker.declare(methodId, PUBLIC);

    final Local<G> localThis = code.getThis(generatedType);
    final Local<Interceptor> nullInterceptor = code.newLocal(INTERCEPTOR_TYPE);
    final Local<Interceptor> localInterceptor = code.newLocal(INTERCEPTOR_TYPE);
    final Local<Boolean> localChanged = code.getParameter(0, TypeId.BOOLEAN);
    final Local<Integer> localLeft = code.getParameter(1, TypeId.INT);
    final Local<Integer> localTop = code.getParameter(2, TypeId.INT);
    final Local<Integer> localRight = code.getParameter(3, TypeId.INT);
    final Local<Integer> localBottom = code.getParameter(4, TypeId.INT);

    code.iget(interceptorField, localInterceptor, localThis);
    code.loadConstant(nullInterceptor, null);

    // Interceptor is not null, call it.
    final Label interceptorNullCase = new Label();
    code.compare(Comparison.EQ, interceptorNullCase, nullInterceptor, localInterceptor);
    code.invokeVirtual(onLayoutMethod, null, localInterceptor, localThis, localChanged, localLeft,
        localTop, localRight, localBottom);
    code.returnVoid();

    // Interceptor is null, call super method.
    code.mark(interceptorNullCase);
    code.invokeSuper(superMethod, null, localThis, localChanged, localLeft, localTop, localRight,
        localBottom);
    code.returnVoid();

    final MethodId<G, Void> callsSuperMethod =
        generatedType.getMethod(VOID_TYPE, ViewMethod.ON_LAYOUT.getInvokeName(), TypeId.BOOLEAN,
            TypeId.INT, TypeId.INT, TypeId.INT, TypeId.INT);

    final Code superCode = dexMaker.declare(callsSuperMethod, PUBLIC);

    final Local<G> superThis = superCode.getThis(generatedType);
    final Local<Boolean> superLocalChanged = superCode.getParameter(0, TypeId.BOOLEAN);
    final Local<Integer> superLocalLeft = superCode.getParameter(1, TypeId.INT);
    final Local<Integer> superLocalTop = superCode.getParameter(2, TypeId.INT);
    final Local<Integer> superLocalRight = superCode.getParameter(3, TypeId.INT);
    final Local<Integer> superLocalBottom = superCode.getParameter(4, TypeId.INT);
    superCode.invokeSuper(superMethod, null, superThis, superLocalChanged, superLocalLeft,
        superLocalTop, superLocalRight, superLocalBottom);
    superCode.returnVoid();
  }

  /**
   * Generates the {@link View#draw(Canvas)} method for the proxy class.
   */
  private static <T, G extends T> void generateDrawMethod(DexMaker dexMaker,
      TypeId<G> generatedType, TypeId<T> baseType, ViewMethod viewMethod, int modifier) {
    final FieldId<G, Interceptor> interceptorField =
        generatedType.getField(INTERCEPTOR_TYPE, FIELD_NAME_INTERCEPTOR);

    final String methodName = viewMethod.getName();

    final MethodId<T, Void> superMethod = baseType.getMethod(VOID_TYPE, methodName, CANVAS_TYPE);
    final MethodId<Interceptor, Void> drawMethod =
        INTERCEPTOR_TYPE.getMethod(VOID_TYPE, methodName, VIEW_TYPE, CANVAS_TYPE);

    final MethodId<G, Void> methodId = generatedType.getMethod(VOID_TYPE, methodName, CANVAS_TYPE);
    final Code code = dexMaker.declare(methodId, modifier);

    final Local<G> localThis = code.getThis(generatedType);
    final Local<Interceptor> nullInterceptor = code.newLocal(INTERCEPTOR_TYPE);
    final Local<Interceptor> localInterceptor = code.newLocal(INTERCEPTOR_TYPE);
    final Local<Canvas> localCanvas = code.getParameter(0, CANVAS_TYPE);

    code.iget(interceptorField, localInterceptor, localThis);
    code.loadConstant(nullInterceptor, null);

    // Interceptor is not null, call it.
    final Label interceptorNullCase = new Label();
    code.compare(Comparison.EQ, interceptorNullCase, nullInterceptor, localInterceptor);
    code.invokeVirtual(drawMethod, null, localInterceptor, localThis, localCanvas);
    code.returnVoid();

    // Interceptor is null, call super method.
    code.mark(interceptorNullCase);
    code.invokeSuper(superMethod, null, localThis, localCanvas);
    code.returnVoid();

    final MethodId<G, Void> callsSuperMethod =
        generatedType.getMethod(VOID_TYPE, viewMethod.getInvokeName(), CANVAS_TYPE);

    final Code superCode = dexMaker.declare(callsSuperMethod, PUBLIC);

    final Local<G> superThis = superCode.getThis(generatedType);
    final Local<Canvas> superLocalCanvas = superCode.getParameter(0, CANVAS_TYPE);
    superCode.invokeSuper(superMethod, null, superThis, superLocalCanvas);
    superCode.returnVoid();
  }

  /**
   * Generates the {@link View#onDraw(Canvas)} method for the proxy class.
   */
  private static <T, G extends T> void generateDrawMethods(DexMaker dexMaker,
      TypeId<G> generatedType, TypeId<T> baseType) {
    generateDrawMethod(dexMaker, generatedType, baseType, ViewMethod.DRAW, PUBLIC);
    generateDrawMethod(dexMaker, generatedType, baseType, ViewMethod.ON_DRAW, PUBLIC);
  }

  /**
   * Generates the {@link View#requestLayout()} method for the proxy class.
   */
  private static <T, G extends T> void generateRequestLayoutMethod(DexMaker dexMaker,
      TypeId<G> generatedType, TypeId<T> baseType) {
    final FieldId<G, Interceptor> interceptorField =
        generatedType.getField(INTERCEPTOR_TYPE, FIELD_NAME_INTERCEPTOR);

    final String methodName = ViewMethod.REQUEST_LAYOUT.getName();

    final MethodId<T, Void> superMethod = baseType.getMethod(VOID_TYPE, methodName);
    final MethodId<Interceptor, Void> requestLayoutMethod =
        INTERCEPTOR_TYPE.getMethod(VOID_TYPE, methodName, VIEW_TYPE);

    final MethodId<?, ?> methodId = generatedType.getMethod(VOID_TYPE, methodName);
    final Code code = dexMaker.declare(methodId, PUBLIC);

    final Local<G> localThis = code.getThis(generatedType);
    final Local<Interceptor> nullInterceptor = code.newLocal(INTERCEPTOR_TYPE);
    final Local<Interceptor> localInterceptor = code.newLocal(INTERCEPTOR_TYPE);

    code.iget(interceptorField, localInterceptor, localThis);
    code.loadConstant(nullInterceptor, null);

    // Interceptor is not null, call it.
    final Label interceptorNullCase = new Label();
    code.compare(Comparison.EQ, interceptorNullCase, nullInterceptor, localInterceptor);
    code.invokeVirtual(requestLayoutMethod, null, localInterceptor, localThis);
    code.returnVoid();

    // Interceptor is null, call super method.
    code.mark(interceptorNullCase);
    code.invokeSuper(superMethod, null, localThis);
    code.returnVoid();

    final MethodId<G, Void> callsSuperMethod =
        generatedType.getMethod(VOID_TYPE, ViewMethod.REQUEST_LAYOUT.getInvokeName());

    final Code superCode = dexMaker.declare(callsSuperMethod, PUBLIC);

    final Local<G> superThis = superCode.getThis(generatedType);
    superCode.invokeSuper(superMethod, null, superThis);
    superCode.returnVoid();
  }

  /**
   * Generates the {@link View#forceLayout()} method for the proxy class.
   */
  private static <T, G extends T> void generateForceLayoutMethod(DexMaker dexMaker,
      TypeId<G> generatedType, TypeId<T> baseType) {
    final FieldId<G, Interceptor> interceptorField =
        generatedType.getField(INTERCEPTOR_TYPE, FIELD_NAME_INTERCEPTOR);

    final String methodName = ViewMethod.FORCE_LAYOUT.getName();

    final MethodId<T, Void> superMethod = baseType.getMethod(VOID_TYPE, methodName);
    final MethodId<Interceptor, Void> forceLayoutMethod =
        INTERCEPTOR_TYPE.getMethod(VOID_TYPE, methodName, VIEW_TYPE);

    final MethodId<?, ?> methodId = generatedType.getMethod(VOID_TYPE, methodName);
    final Code code = dexMaker.declare(methodId, PUBLIC);

    final Local<G> localThis = code.getThis(generatedType);
    final Local<Interceptor> nullInterceptor = code.newLocal(INTERCEPTOR_TYPE);
    final Local<Interceptor> localInterceptor = code.newLocal(INTERCEPTOR_TYPE);

    code.iget(interceptorField, localInterceptor, localThis);
    code.loadConstant(nullInterceptor, null);

    // Interceptor is not null, call it.
    final Label interceptorNullCase = new Label();
    code.compare(Comparison.EQ, interceptorNullCase, nullInterceptor, localInterceptor);
    code.invokeVirtual(forceLayoutMethod, null, localInterceptor, localThis);
    code.returnVoid();

    // Interceptor is null, call super method.
    code.mark(interceptorNullCase);
    code.invokeSuper(superMethod, null, localThis);
    code.returnVoid();

    final MethodId<G, Void> callsSuperMethod =
        generatedType.getMethod(VOID_TYPE, ViewMethod.FORCE_LAYOUT.getInvokeName());

    final Code superCode = dexMaker.declare(callsSuperMethod, PUBLIC);

    final Local<G> superThis = superCode.getThis(generatedType);
    superCode.invokeSuper(superMethod, null, superThis);
    superCode.returnVoid();
  }

  /**
   * Generates the {@link View#setMeasuredDimension(int, int)} method for
   * the proxy class.
   */
  private static <T, G extends T> void generateSetMeasuredDimension(DexMaker dexMaker,
      TypeId<G> generatedType, TypeId<T> baseType) {
    final String methodName = ViewMethod.SET_MEASURED_DIMENSION.getName();

    final MethodId<T, Void> superMethod =
        baseType.getMethod(VOID_TYPE, methodName, TypeId.INT, TypeId.INT);

    final MethodId<G, Void> callsSuperMethod =
        generatedType.getMethod(VOID_TYPE, ViewMethod.SET_MEASURED_DIMENSION.getInvokeName(),
            TypeId.INT, TypeId.INT);

    final Code code = dexMaker.declare(callsSuperMethod, PUBLIC);

    final Local<G> localThis = code.getThis(generatedType);
    final Local<Integer> localWidth = code.getParameter(0, TypeId.INT);
    final Local<Integer> localHeight = code.getParameter(1, TypeId.INT);
    code.invokeSuper(superMethod, null, localThis, localWidth, localHeight);
    code.returnVoid();
  }

  /**
   * Generates the {@link View#setMeasuredDimension(int, int)} method for
   * the proxy class.
   */
  private static <T, G extends T> void generateSetInterceptor(DexMaker dexMaker,
      TypeId<G> generatedType, TypeId<T> baseType) {
    final FieldId<G, Interceptor> interceptorField =
        generatedType.getField(INTERCEPTOR_TYPE, FIELD_NAME_INTERCEPTOR);

    final String methodName = ViewMethod.SET_INTERCEPTOR.getName();

    final MethodId<G, Void> methodId =
        generatedType.getMethod(VOID_TYPE, methodName, INTERCEPTOR_TYPE);
    final Code code = dexMaker.declare(methodId, PUBLIC);

    final Local<G> localThis = code.getThis(generatedType);
    final Local<Interceptor> localInterceptor = code.getParameter(0, INTERCEPTOR_TYPE);
    code.iput(interceptorField, localThis, localInterceptor);
    code.returnVoid();
  }

  private static <T> String getClassNameForProxyOf(Class<? extends T> clazz) {
    return clazz.getSimpleName() + "_Proxy";
  }

  private static TypeId<?>[] classArrayToTypeArray(Class<?>[] input) {
    final TypeId<?>[] result = new TypeId[input.length];
    for (int i = 0; i < input.length; ++i) {
      result[i] = TypeId.get(input[i]);
    }

    return result;
  }

  /**
   * Generates dynamic {@link View} proxy class.
   */
  @SuppressWarnings("unchecked") static <T, G extends T> Class<G> generateProxyClass(
      Context context, Class<T> baseClass) throws IOException {
    // Cache missed; generate the proxy class.
    final DexMaker dexMaker = new DexMaker();

    final String proxyClassName = getClassNameForProxyOf(baseClass);
    final TypeId<G> generatedType = TypeId.get("L" + proxyClassName + ";");
    final TypeId<T> baseType = TypeId.get(baseClass);

    generateConstructorAndFields(dexMaker, generatedType, baseType);
    generateOnMeasureMethod(dexMaker, generatedType, baseType);
    generateOnLayoutMethod(dexMaker, generatedType, baseType);
    generateDrawMethods(dexMaker, generatedType, baseType);
    generateRequestLayoutMethod(dexMaker, generatedType, baseType);
    generateForceLayoutMethod(dexMaker, generatedType, baseType);
    generateSetMeasuredDimension(dexMaker, generatedType, baseType);
    generateSetInterceptor(dexMaker, generatedType, baseType);

    dexMaker.declare(generatedType, proxyClassName + ".generated", PUBLIC, baseType,
        INTERCEPTABLE_VIEW_TYPE);

    final ClassLoader classLoader = dexMaker.generateAndLoad(context.getClassLoader(),
        context.getDir(DEX_CACHE_DIRECTORY, Context.MODE_PRIVATE));
    try {
      return (Class<G>) classLoader.loadClass(proxyClassName);
    } catch (IllegalAccessError e) {
      // Thrown when the base class is not accessible.
      throw new UnsupportedOperationException("cannot proxy inaccessible class " + baseClass, e);
    } catch (ClassNotFoundException e) {
      // Should not be thrown, we're sure to have generated this class.
      throw new AssertionError(e);
    }
  }
}
