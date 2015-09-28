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

/**
 * Defines contract for a {@link android.view.View} that can be intercepted.
 * <p>
 * Under the hood, {@link Probe} will use either static or dynamic
 * {@link android.view.View} proxies that complies with this interface
 * when inflating views. If you make your own views comply with interface,
 * Probe will not need to use proxy classes to intercept calls.
 */
public interface ViewProxy {
  /**
   * Sets the {@link Interceptor} on this proxy.
   */
  void setInterceptor(Interceptor interceptor);

  /**
   * Calls {@code super.onMeasure(int, int)}.
   */
  void invokeOnMeasure(int widthMeasureSpec, int heightMeasureSpec);

  /**
   * Calls {@code super.onLayout(boolean, int, int, int, int)}.
   */
  void invokeOnLayout(boolean changed, int l, int t, int r, int b);

  /**
   * Calls {@code super.draw(Canvas)}.
   */
  void invokeDraw(Canvas canvas);

  /**
   * Calls {@code super.onDraw(Canvas)}.
   */
  void invokeOnDraw(Canvas canvas);

  /**
   * Calls {@code super.requestLayout()}.
   */
  void invokeRequestLayout();

  /**
   * Calls {@code super.forceLayout()}.
   */
  void invokeForceLayout();

  /**
   * Calls {@code super.onSetMeasuredDimension(int, int)}.
   */
  void invokeSetMeasuredDimension(int width, int height);
}
