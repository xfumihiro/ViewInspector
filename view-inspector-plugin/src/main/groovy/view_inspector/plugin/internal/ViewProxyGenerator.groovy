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

package view_inspector.plugin.internal

import com.android.annotations.NonNull
import com.squareup.javawriter.JavaWriter

import javax.lang.model.element.Modifier

import static javax.lang.model.element.Modifier.*

class ViewProxyGenerator {
  private static final String[] PROXY_IMPORTS = ["android.content.Context",
                                                 "android.graphics.Canvas",
                                                 "android.util.AttributeSet",
                                                 "view_inspector.probe.Interceptor",
                                                 "view_inspector.probe.ViewProxy"]

  private static final String FIELD_INTERCEPTOR = "mInterceptor"

  private static final String METHOD_SET_INTERCEPTOR = "setInterceptor"
  private static final String METHOD_ON_MEASURE = "onMeasure"
  private static final String METHOD_ON_LAYOUT = "onLayout"
  private static final String METHOD_DRAW = "draw"
  private static final String METHOD_ON_DRAW = "onDraw"
  private static final String METHOD_REQUEST_LAYOUT = "requestLayout"
  private static final String METHOD_FORCE_LAYOUT = "forceLayout"

  // setInterceptor(Interceptor)
  private static final String PARAM_INTERCEPTOR = "interceptor"

  // onMeasure(int, int)
  private static final String PARAM_WIDTH_SPEC = "widthMeasureSpec"
  private static final String PARAM_HEIGHT_SPEC = "heightMeasureSpec"

  // onLayout(boolean, int, int, int, int)
  private static final String PARAM_CHANGED = "changed"
  private static final String PARAM_LEFT = "left"
  private static final String PARAM_TOP = "top"
  private static final String PARAM_RIGHT = "right"
  private static final String PARAM_BOTTOM = "bottom"

  // draw(Canvas) and onDraw(Canvas)
  private static final String PARAM_CANVAS = "canvas"

  // setMeasuredDimension(Canvas)
  private static final String PARAM_WIDTH = "width"
  private static final String PARAM_HEIGHT = "height"

  private ViewProxyGenerator() {}

  static String filenameForClassName(String className) {
    return "${generateProxyName(className)}.java"
  }

  static void generate(@NonNull Writer writer, @NonNull String className,
      @NonNull String packageName) {
    def proxyName = generateProxyName(className)

    JavaWriter javaWriter = new JavaWriter(writer)
    generatePackage(javaWriter, packageName)
    generateImports(javaWriter, className)
    generateClass(javaWriter, className, proxyName)
  }

  private static String generateProxyName(String className) {
    return "ProbeProxy\$${className.replace('.', '_')}"
  }

  private static void generatePackage(JavaWriter javaWriter, String packageName) {
    javaWriter.emitPackage(packageName)
  }

  private static void generateImports(JavaWriter javaWriter, String className) {
    javaWriter.emitImports(PROXY_IMPORTS)
    javaWriter.emitImports(className)
  }

  private static void generateClass(JavaWriter javaWriter, String className,
      String proxyName) {
    javaWriter.beginType(proxyName, "class", EnumSet.of(PUBLIC, FINAL),
        className, "ViewProxy")

    generateFields(javaWriter)
    generateConstructor(javaWriter)
    generateInterceptorSetter(javaWriter)
    generateOnMeasureMethod(javaWriter)
    generateOnLayoutMethod(javaWriter)
    generateDrawMethods(javaWriter)
    generateRequestLayoutMethod(javaWriter)
    generateForceLayoutMethod(javaWriter)
    generateSetMeasuredDimensionMethod(javaWriter)

    javaWriter.endType()
  }

  private static void generateFields(JavaWriter javaWriter) {
    javaWriter.emitField("Interceptor", FIELD_INTERCEPTOR, EnumSet.of(PRIVATE))
  }

  private static void generateConstructor(JavaWriter javaWriter) {
    javaWriter.beginConstructor(EnumSet.of(PUBLIC),
        "Context", "context", "AttributeSet", "attrs")
    javaWriter.emitStatement("super(context, attrs)")
    javaWriter.endConstructor()
  }

  private static void generateInterceptorSetter(JavaWriter javaWriter) {
    javaWriter.beginMethod("void", METHOD_SET_INTERCEPTOR, EnumSet.of(PUBLIC),
        "Interceptor", PARAM_INTERCEPTOR)
    javaWriter.emitStatement("%s = %s", FIELD_INTERCEPTOR, PARAM_INTERCEPTOR)
    javaWriter.endMethod()
  }

  private static String generateCommaSeparatedArgs(String... args) {
    def sb = new StringBuilder()

    for (int i = 0; i < args.length; i++) {
      if (i > 0) {
        sb.append(", ")
      }

      sb.append(args[i])
    }

    return sb.toString()
  }

  private static void generateInterceptorConditional(JavaWriter javaWriter,
      String methodName,
      String... args) {
    def methodArgs = generateCommaSeparatedArgs(args)

    javaWriter.beginControlFlow("if (%s != null)", FIELD_INTERCEPTOR)
    if (methodArgs.length() != 0) {
      javaWriter.emitStatement("%s.%s(this, %s)", FIELD_INTERCEPTOR, methodName, methodArgs)
    } else {
      javaWriter.emitStatement("%s.%s(this)", FIELD_INTERCEPTOR, methodName)
    }
    javaWriter.endControlFlow()
    javaWriter.beginControlFlow("else")
    javaWriter.emitStatement("super.%s(%s)", methodName, methodArgs)
    javaWriter.endControlFlow()
  }

  private static void beginOnMeasureMethod(JavaWriter javaWriter, String methodName,
      Modifier modifier) {
    javaWriter.beginMethod("void", methodName, EnumSet.of(modifier),
        "int", PARAM_WIDTH_SPEC, "int", PARAM_HEIGHT_SPEC)
  }

  private static void generateOnMeasureMethod(JavaWriter javaWriter) {
    beginOnMeasureMethod(javaWriter, METHOD_ON_MEASURE, PUBLIC)
    generateInterceptorConditional(javaWriter, METHOD_ON_MEASURE, PARAM_WIDTH_SPEC,
        PARAM_HEIGHT_SPEC)
    javaWriter.endMethod()

    beginOnMeasureMethod(javaWriter, "invokeOnMeasure", PUBLIC)
    javaWriter.emitStatement("super.%s(%s, %s)", METHOD_ON_MEASURE, PARAM_WIDTH_SPEC,
        PARAM_HEIGHT_SPEC)
    javaWriter.endMethod()
  }

  private static void beginOnLayoutMethod(JavaWriter javaWriter, String methodName,
      Modifier modifier) {
    javaWriter.beginMethod("void", methodName, EnumSet.of(modifier),
        "boolean", PARAM_CHANGED, "int", PARAM_LEFT, "int", PARAM_TOP,
        "int", PARAM_RIGHT, "int", PARAM_BOTTOM)
  }

  private static void generateOnLayoutMethod(JavaWriter javaWriter) {
    beginOnLayoutMethod(javaWriter, METHOD_ON_LAYOUT, PUBLIC)
    generateInterceptorConditional(javaWriter, METHOD_ON_LAYOUT, PARAM_CHANGED,
        PARAM_LEFT, PARAM_TOP, PARAM_RIGHT, PARAM_BOTTOM)
    javaWriter.endMethod()

    beginOnLayoutMethod(javaWriter, "invokeOnLayout", PUBLIC)
    javaWriter.emitStatement("super.%s(%s, %s, %s, %s, %s)", METHOD_ON_LAYOUT,
        PARAM_CHANGED, PARAM_LEFT, PARAM_TOP, PARAM_RIGHT, PARAM_BOTTOM)
    javaWriter.endMethod()
  }

  private static void beginDrawMethod(JavaWriter javaWriter, String methodName,
      Modifier modifier) {
    javaWriter.beginMethod("void", methodName, EnumSet.of(modifier),
        "Canvas", PARAM_CANVAS)
  }

  private static void generateDrawMethods(JavaWriter javaWriter) {
    beginDrawMethod(javaWriter, METHOD_DRAW, PUBLIC)
    generateInterceptorConditional(javaWriter, METHOD_DRAW, PARAM_CANVAS)
    javaWriter.endMethod()

    beginDrawMethod(javaWriter, "invokeDraw", PUBLIC)
    javaWriter.emitStatement("super.%s(%s)", METHOD_DRAW, PARAM_CANVAS)
    javaWriter.endMethod()

    beginDrawMethod(javaWriter, METHOD_ON_DRAW, PUBLIC)
    generateInterceptorConditional(javaWriter, METHOD_ON_DRAW, PARAM_CANVAS)
    javaWriter.endMethod()

    beginDrawMethod(javaWriter, "invokeOnDraw", PUBLIC)
    javaWriter.emitStatement("super.%s(%s)", METHOD_ON_DRAW, PARAM_CANVAS)
    javaWriter.endMethod()
  }

  private static void beginRequestLayoutMethod(JavaWriter javaWriter, String methodName) {
    javaWriter.beginMethod("void", methodName, EnumSet.of(PUBLIC))
  }

  private static void generateRequestLayoutMethod(JavaWriter javaWriter) {
    beginForceLayoutMethod(javaWriter, METHOD_REQUEST_LAYOUT)
    generateInterceptorConditional(javaWriter, METHOD_REQUEST_LAYOUT)
    javaWriter.endMethod()

    beginForceLayoutMethod(javaWriter, "invokeRequestLayout")
    javaWriter.emitStatement("super.%s()", METHOD_REQUEST_LAYOUT)
    javaWriter.endMethod()
  }

  private static void beginForceLayoutMethod(JavaWriter javaWriter, String methodName) {
    javaWriter.beginMethod("void", methodName, EnumSet.of(PUBLIC))
  }

  private static void generateForceLayoutMethod(JavaWriter javaWriter) {
    beginForceLayoutMethod(javaWriter, METHOD_FORCE_LAYOUT)
    generateInterceptorConditional(javaWriter, METHOD_FORCE_LAYOUT)
    javaWriter.endMethod()

    beginForceLayoutMethod(javaWriter, "invokeForceLayout")
    javaWriter.emitStatement("super.%s()", METHOD_FORCE_LAYOUT)
    javaWriter.endMethod()
  }

  private static void generateSetMeasuredDimensionMethod(JavaWriter javaWriter) {
    javaWriter.beginMethod("void", "invokeSetMeasuredDimension", EnumSet.of(PUBLIC),
        "int", PARAM_WIDTH, "int", PARAM_HEIGHT)
    javaWriter.emitStatement("super.setMeasuredDimension(%s, %s)", PARAM_WIDTH,
        PARAM_HEIGHT)
    javaWriter.endMethod()
  }
}
