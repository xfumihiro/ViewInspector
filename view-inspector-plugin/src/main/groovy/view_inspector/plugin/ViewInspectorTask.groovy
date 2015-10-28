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

package view_inspector.plugin

import com.android.annotations.NonNull
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.incremental.IncrementalTaskInputs
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import view_inspector.plugin.internal.LayoutResourceParser
import view_inspector.plugin.internal.ViewProxyGenerator

class ViewInspectorTask extends DefaultTask {
  @NonNull
  String packageName

  @NonNull @OutputDirectory
  File outputDir

  @NonNull @InputFiles
  Iterable<File> inputFiles

  @Input
  @Optional
  String[] excludePackages

  @TaskAction
  void taskAction(IncrementalTaskInputs inputs) {
    if (!outputDir.isDirectory()) {
      outputDir.mkdirs()
    }

    if (!inputs.isIncremental()) {
      parseLayoutFiles(inputFiles)
      return;
    }

    Set<String> newFiles = new HashSet<String>()
    boolean isIncremental = true

    inputs.outOfDate { change ->
      if (change.isAdded()) {
        newFiles.add(change.file)
      } else {
        isIncremental = false
      }
    }

    inputs.removed { change -> isIncremental = false
    }

    // There's no trivial way to figure out what view proxies need to
    // be added or removed. The plugin only does incremental builds when
    // new layout files are added for now.
    if (isIncremental && newFiles.size() > 0) {
      parseLayoutFiles(newFiles)
    } else {
      parseLayoutFiles(inputFiles)
    }
  }

  private void parseLayoutFiles(Iterable<File> layoutFiles) {
    layoutFiles.each { layoutFile ->
      Set<String> viewClassNames = LayoutResourceParser.parse(layoutFile, excludePackages)
      for (String viewClassName : viewClassNames) {
        String filename = ViewProxyGenerator.filenameForClassName(viewClassName)
        File proxyFile = new File(outputDir, filename)

        // Proxy class already exists, skip it.
        if (proxyFile.exists()) {
          continue
        }

        project.logger.debug "Generating ${filename}"
        FileWriter writer = new FileWriter(proxyFile)
        ViewProxyGenerator.generate(writer, viewClassName, packageName)
        writer.close()
      }
    }
  }
}
