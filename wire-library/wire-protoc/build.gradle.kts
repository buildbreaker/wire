
plugins {
  application

  java
  kotlin("jvm")
}

tasks {
  // binary: https://www.baeldung.com/kotlin/gradle-executable-jar
  val javaGeneratorBinary = register<Jar>("javaGeneratorBinary") {
    archiveBaseName.set("protoc-java")
    dependsOn.addAll(listOf("compileJava", "compileKotlin", "processResources")) // We need this for Gradle optimization to work
    archiveClassifier.set("standalone") // Naming the jar
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    manifest { attributes(mapOf("Main-Class" to "com.squareup.wire.protocwire.cmd.JavaGenerator")) } // Provided we set it up in the application plugin configuration
    val sourcesMain = sourceSets.main.get()
    val contents = configurations.runtimeClasspath.get()
      .map { if (it.isDirectory) it else zipTree(it) } +
      sourcesMain.output
    from(contents)
  }
  val kotlinGeneratorBinary = register<Jar>("kotlinGeneratorBinary") {
    archiveBaseName.set("protoc-kotlin")
    dependsOn.addAll(listOf("compileJava", "compileKotlin", "processResources")) // We need this for Gradle optimization to work
    archiveClassifier.set("standalone") // Naming the jar
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    manifest { attributes(mapOf("Main-Class" to "com.squareup.wire.protocwire.cmd.KotlinGenerator")) } // Provided we set it up in the application plugin configuration
    val sourcesMain = sourceSets.main.get()
    val contents = configurations.runtimeClasspath.get()
      .map { if (it.isDirectory) it else zipTree(it) } +
      sourcesMain.output
    from(contents)
  }

  val swiftGeneratorBinary = register<Jar>("swiftGeneratorBinary") {
    archiveBaseName.set("protoc-swift")
    dependsOn.addAll(listOf("compileJava", "compileKotlin", "processResources")) // We need this for Gradle optimization to work
    archiveClassifier.set("standalone") // Naming the jar
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    manifest { attributes(mapOf("Main-Class" to "com.squareup.wire.protocwire.cmd.SwiftGenerator")) } // Provided we set it up in the application plugin configuration
    val sourcesMain = sourceSets.main.get()
    val contents = configurations.runtimeClasspath.get()
      .map { if (it.isDirectory) it else zipTree(it) } +
      sourcesMain.output
    from(contents)
  }
  build {
    dependsOn(kotlinGeneratorBinary, swiftGeneratorBinary, javaGeneratorBinary)
  }
}

java {
  sourceCompatibility = JavaVersion.VERSION_1_8
  targetCompatibility = JavaVersion.VERSION_1_8
}

dependencies {
  implementation(libs.protobuf.java)
  implementation(projects.wireSchema)
  implementation(projects.wireGrpcClient)
  implementation(projects.wireCompiler)
  implementation(projects.wireKotlinGenerator) // This is kind of needed
  implementation("com.squareup.okio:okio-fakefilesystem-jvm:3.2.0")
}
