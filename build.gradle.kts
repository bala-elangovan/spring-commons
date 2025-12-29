plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.spring) apply false
    alias(libs.plugins.java.conventions) apply false
    alias(libs.plugins.spring.test.conventions) apply false
    `maven-publish`
}

allprojects {
    group = "io.github.balaelangovan"
    version = "1.0.0"

    repositories {
        mavenCentral()
        mavenLocal()
    }
}

subprojects {
    apply(plugin = "maven-publish")

    afterEvaluate {
        tasks.withType<JavaCompile> {
            options.encoding = "UTF-8"
        }

        tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
            compilerOptions {
                freeCompilerArgs.set(listOf("-Xjsr305=strict"))
                jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
            }
        }

        // Configure publishing only for modules with Java component
        if (components.findByName("java") != null) {
            configure<PublishingExtension> {
                publications {
                    create<MavenPublication>("mavenJava") {
                        from(components["java"])
                        // Use full module path as artifactId to avoid conflicts
                        artifactId = project.path.substring(1).replace(":", "-")
                    }
                }
            }
        }
    }
}
