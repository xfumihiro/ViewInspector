Release Process
===============

 1. Update the `CHANGELOG.md` file with relevant info and date.
 2. Update version number in `gradle.properties` file.
 3. Update version numbers in `ViewInspectorPlugin.groovy` file.
 4. Update version number in `README.md` file.
 5. Commit: `git commit -am "Prepare version X.Y.Z."`
 6. Tag: `git tag -a X.Y.Z -m "Version X.Y.Z"`
 7. Release: `./gradlew clean assemble bintrayUpload`
 8. Visit bintray site and sync to MavenCentral
 9. Update version number in `gradle.properties` file to next "SNAPSHOT" version.
 10. Update version number in `sample/build.gradle` file to next "SNAPSHOT" version.
 11. Update version numbers in `ViewInspectorPlugin.groovy` file to next "SNAPSHOT" version.
 12. Commit: `git commit -am "Prepare next development version."`
 13. Push: `git push && git push --tags`