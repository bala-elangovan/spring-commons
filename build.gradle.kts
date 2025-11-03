plugins {
    kotlin("jvm") version "2.2.20" apply false
    kotlin("plugin.spring") version "2.2.20" apply false
    id("io.github.platform.java-conventions") version "1.0.0" apply false
    id("io.github.platform.spring-conventions") version "1.0.0" apply false
    id("io.github.platform.spring-test-conventions") version "1.0.0" apply false
    `maven-publish`
}

allprojects {
    group = "io.github.platform"
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

                        pom {
                            name.set(project.name)
                            description.set(project.description ?: "Platform Commons - ${project.name}")
                            url.set("https://github.com/bala-lab-projects/platform-commons")

                            licenses {
                                license {
                                    name.set("MIT License")
                                    url.set("https://opensource.org/licenses/MIT")
                                }
                            }

                            developers {
                                developer {
                                    id.set("bala-lab-projects")
                                    name.set("Balamurugan Elangovan")
                                    email.set("mail.bala0224@gmail.com")
                                }
                            }

                            scm {
                                connection.set("scm:git:git://github.com/bala-lab-projects/platform-commons.git")
                                developerConnection.set("scm:git:ssh://github.com/bala-lab-projects/platform-commons.git")
                                url.set("https://github.com/bala-lab-projects/platform-commons")
                            }
                        }
                    }
                }
            }
        }
    }
}
