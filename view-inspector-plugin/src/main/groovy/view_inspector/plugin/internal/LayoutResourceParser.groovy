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

package view_inspector.plugin.internal

import com.android.annotations.NonNull

class LayoutResourceParser {
  private LayoutResourceParser() {}

  public static Set<String> parse(@NonNull File layoutFile, String[] excludePackages) {
    Set<String> viewClassNames = new HashSet<String>()

    def rootNode = new XmlParser().parse(layoutFile)
    traverseLayoutXml(rootNode, viewClassNames)

    excludePackages.each { excludePackage ->
      viewClassNames.removeAll {
        it.startsWith(excludePackage)
      }
    }
    return viewClassNames
  }

  private static void traverseLayoutXml(Node node, Set<String> viewClassNames) {
    def name = node.name()
    if (name == "view") {
      name = node.@class
    }

    def className = resolveClassName(name)
    if (className != null) {
      viewClassNames.add(className)
    }

    node.children().each {
      // children() might contain string elements
      if (it instanceof Node) {
        traverseLayoutXml(it, viewClassNames)
      }
    }
  }

  private static String resolveClassName(String name) {
    def className
    if (name == 'View') {
      className = "android.view.View"
    } else if (name == 'SurfaceView') {
      className = "android.view.SurfaceView"
    } else if (name == 'WebView') {
      className = "android.webkit.WebView"
    } else if (
        name == "merge" ||
        name == "include" ||
        name == "ViewStub" ||
        name == "fragment" ||
        name == "requestLayout" ||
        name == "DateTimeView" ||
        name == "Space" ||
        name == "requestFocus") {
      className = null
    } else if (name.indexOf('.') == -1) {
      className = "android.widget.${name}"
    } else if (name.startsWith("android.") && name.indexOf('internal')) {
      className = null
    } else if (name.startsWith("view_inspector")) {
      className = null
    } else {
      className = name
    }

    return className
  }
}
