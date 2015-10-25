package view_inspector.plugin

import com.android.build.gradle.AppPlugin
import org.aspectj.bridge.IMessage
import org.aspectj.bridge.MessageHandler
import org.aspectj.tools.ajc.Main
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.compile.JavaCompile

class ViewInspectorPlugin implements Plugin<Project> {

  @Override
  void apply(Project project) {
    if (!project.plugins.withType(AppPlugin)) {
      throw new IllegalStateException("'android' plugin required.")
    }

    project.dependencies {
      debugCompile 'com.github.xfumihiro.view-inspector:view-inspector-runtime:0.1.1-SNAPSHOT'
      debugCompile 'org.aspectj:aspectjrt:1.8.6'
      debugCompile('com.google.dexmaker:dexmaker:1.1') {
        transitive = true
      }
      debugCompile 'com.google.dagger:dagger:2.0.1'
      debugCompile 'com.f2prateek.rx.preferences:rx-preferences:1.0.0'
      debugCompile 'com.github.frankiesardo:auto-parcel:0.3'
      debugCompile 'com.jakewharton.scalpel:scalpel:1.1.2'
      debugCompile 'com.android.support:appcompat-v7:21.0.3'
    }

    final def log = project.logger
    final def variants = project.android.applicationVariants

    variants.all { variant ->
      if (!variant.buildType.isDebuggable()) {
        log.debug("Skipping non-debuggable build type '${variant.buildType.name}'.")
        return;
      }

      JavaCompile javaCompile = variant.javaCompile
      javaCompile.doLast {
        String[] args = ["-showWeaveInfo",
                         "-1.5",
                         "-inpath", javaCompile.destinationDir.toString(),
                         "-aspectpath", javaCompile.classpath.asPath,
                         "-d", javaCompile.destinationDir.toString(),
                         "-classpath", javaCompile.classpath.asPath,
                         "-bootclasspath", project.android.bootClasspath.join(File.pathSeparator)]
        log.debug "ajc args: " + Arrays.toString(args)

        MessageHandler handler = new MessageHandler(true);
        new Main().run(args, handler);
        for (IMessage message : handler.getMessages(null, true)) {
          switch (message.getKind()) {
            case IMessage.ABORT:
            case IMessage.ERROR:
            case IMessage.FAIL:
              log.error message.message, message.thrown
              break;
            case IMessage.WARNING:
              log.warn message.message, message.thrown
              break;
            case IMessage.INFO:
              log.info message.message, message.thrown
              break;
            case IMessage.DEBUG:
              log.debug message.message, message.thrown
              break;
          }
        }
      }

      def sourcePath = "${project.buildDir}/generated/source/view-inspector/${variant.dirName}"
      def packageName = "${variant.mergedFlavor.applicationId}.view_inspector"
      if (variant.mergedFlavor.applicationId == null) {
        File androidManifest = project.file('src/main/AndroidManifest.xml')
        def manifest = new XmlSlurper().parse(androidManifest)
        packageName = "${manifest.@package.text()}.view_inspector"
      }
      def task = project.tasks.create("view_inspector${variant.name.capitalize()}Views",
          ViewInspectorTask)

      // Set task properties
      task.packageName = packageName
      task.outputDir = new File("${sourcePath}/${packageName.replace('.', '/')}")
      task.inputFiles = project.fileTree(dir: variant.mergeResources.outputDir)
          .matching { include 'layout*/*.xml' }

      // Set task dependencies
      task.dependsOn variant.mergeResources
      variant.javaCompile.source sourcePath
      variant.javaCompile.dependsOn task
    }
  }
}
