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

import android.graphics.Canvas;
import android.view.View;

/**
 * Observe and override method calls on views inflated by a {@link Probe}. You
 * must provide an {@link Interceptor} instance when creating a new {@link Probe}.
 *
 * <p>By default, all methods in an {@link Interceptor} simply call their respective
 * view method implementations i.e. {@link #onDraw(View, Canvas)} will simply call
 * {@code view.onDraw(Canvas)} by default.</p>
 *
 * <p></>You should override one or more methods in your {@link Interceptor} subclass to
 * either observer the method calls or completely override the method for the given
 * view on-the-fly. For example:</p>
 *
 * <pre>
 * public void draw(View view, Canvas canvas) {
 *     // Not calling super.draw(view, canvas) here will completely
 *     // override the given view's draw call.
 *     canvas.drawRect(0, 0, view.getWidth(). view.Height(), paint);
 * }
 * </pre>
 *
 * An {@link Interceptor} can also be used to track and benchmark the behaviour of
 * specific views in your Android UI.
 */
public class Interceptor {
  /**
   * Intercepts an {@link View#onMeasure(int, int)} call on the given {@link View}.
   * By default, it simply calls the view's original method.
   */
  public void onMeasure(View view, int widthMeasureSpec, int heightMeasureSpec) {
    invokeOnMeasure(view, widthMeasureSpec, heightMeasureSpec);
  }

  /**
   * Performs a {@link View#onMeasure(int, int)} call on the given {@link View}.
   */
  protected final void invokeOnMeasure(View view, int widthMeasureSpec, int heightMeasureSpec) {
    final ViewProxy proxy = (ViewProxy) view;
    proxy.invokeOnMeasure(widthMeasureSpec, heightMeasureSpec);
  }

  /**
   * Intercepts an {@link View#onLayout(boolean, int, int, int, int)} call on the
   * given {@link View}. By default, it simply calls the view's original method.
   */
  public void onLayout(View view, boolean changed, int l, int t, int r, int b) {
    invokeOnLayout(view, changed, l, t, r, b);
  }

  /**
   * Performs an {@link View#onLayout(boolean, int, int, int, int)} call on the
   * given {@link View}.
   */
  protected final void invokeOnLayout(View view, boolean changed, int l, int t, int r, int b) {
    final ViewProxy proxy = (ViewProxy) view;
    proxy.invokeOnLayout(changed, l, t, r, b);
  }

  /**
   * Intercepts a {@link View#draw(Canvas)} call on the given {@link View}. By default,
   * it simply calls the view's original method.
   */
  public void draw(View view, Canvas canvas) {
    invokeDraw(view, canvas);
  }

  /**
   * Performs a {@link View#draw(Canvas)} call on the given {@link View}.
   */
  protected final void invokeDraw(View view, Canvas canvas) {
    final ViewProxy proxy = (ViewProxy) view;
    proxy.invokeDraw(canvas);
  }

  /**
   * Intercepts an {@link View#onDraw(Canvas)} call on the given {@link View}. By default,
   * it simply calls the view's original method.
   */
  public void onDraw(View view, Canvas canvas) {
    invokeOnDraw(view, canvas);
  }

  /**
   * Performs an {@link View#onDraw(Canvas)} call on the given {@link View}.
   */
  protected final void invokeOnDraw(View view, Canvas canvas) {
    final ViewProxy proxy = (ViewProxy) view;
    proxy.invokeOnDraw(canvas);
  }

  /**
   * Intercepts a {@link View#requestLayout()} call on the given {@link View}. By default,
   * it simply calls the view's original method.
   */
  public void requestLayout(View view) {
    invokeRequestLayout(view);
  }

  /**
   * Performs a {@link View#requestLayout()} call on the given {@link View}.
   */
  protected final void invokeRequestLayout(View view) {
    final ViewProxy proxy = (ViewProxy) view;
    proxy.invokeRequestLayout();
  }

  /**
   * Intercepts a {@link View#forceLayout()} call on the given {@link View}. By default,
   * it simply calls the view's original method.
   */
  public void forceLayout(View view) {
    invokeForceLayout(view);
  }

  /**
   * Performs a {@link View#forceLayout()} call on the given {@link View}.
   */
  protected final void invokeForceLayout(View view) {
    final ViewProxy proxy = (ViewProxy) view;
    proxy.invokeForceLayout();
  }

  /**
   * Calls {@link View#setMeasuredDimension(int, int)} on the given {@link View}.
   * This can be used to override {@link View#onMeasure(int, int)} calls on-the-fly
   * in interceptors.
   */
  protected final void setMeasuredDimension(View view, int width, int height) {
    final ViewProxy proxy = (ViewProxy) view;
    proxy.invokeSetMeasuredDimension(width, height);
  }
}
