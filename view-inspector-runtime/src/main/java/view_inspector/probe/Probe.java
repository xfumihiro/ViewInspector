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
import android.view.LayoutInflater;

public class Probe {
  private final Interceptor mInterceptor;

  private Probe(Context context, Interceptor interceptor) {
    if (context == null) {
      throw new IllegalArgumentException("Context should not be null.");
    }

    if (interceptor == null) {
      throw new IllegalArgumentException("Interceptor should not be null.");
    }

    mInterceptor = interceptor;
  }

  Interceptor getInterceptor() {
    return mInterceptor;
  }

  /**
   * Deploy an {@link Interceptor} in the given {@link Context}.
   */
  public static void deploy(Context context, Interceptor interceptor) {
    final Probe probe = new Probe(context, interceptor);
    LayoutInflater.from(context).setFactory2(new ProbeViewFactory(context, probe));
  }
}
