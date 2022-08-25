
plugins {
  application

  java
  kotlin("jvm")
}

application {
  mainClass.set("com.squareup.wire.protocwire.WireGenerator")
}

tasks {
  // binary: https://www.baeldung.com/kotlin/gradle-executable-jar
  val binary = register<Jar>("binary") {
    dependsOn.addAll(listOf("compileJava", "compileKotlin", "processResources")) // We need this for Gradle optimization to work
    archiveClassifier.set("standalone") // Naming the jar
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    manifest { attributes(mapOf("Main-Class" to application.mainClass)) } // Provided we set it up in the application plugin configuration
    val sourcesMain = sourceSets.main.get()
    val contents = configurations.runtimeClasspath.get()
      .map { if (it.isDirectory) it else zipTree(it) } +
      sourcesMain.output
    from(contents)
  }
  build {
    dependsOn(binary)
  }
}

java {
  sourceCompatibility = JavaVersion.VERSION_1_8
  targetCompatibility = JavaVersion.VERSION_1_8
}

dependencies {
  implementation(libs.protobuf.java)
  implementation(projects.wireSchema)
  implementation(projects.wireCompiler)
  implementation(projects.wireKotlinGenerator) // This is kind of needed
}