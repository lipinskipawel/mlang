plugins {
    `java-library`
    id("com.github.johnrengelman.shadow") version ("7.1.2")
}

repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.0")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.10.0")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.10.0")
    testImplementation("org.assertj:assertj-core:3.24.2")
}

tasks {
    jar {
        manifest {
            attributes["Main-Class"] = "com.github.lipinskipawel.mlang.repl.Main"
        }
    }

    shadowJar {
        archiveFileName = project.name + ".jar"
    }

    test {
        useJUnitPlatform()
    }

    named<Wrapper>("wrapper") {
        gradleVersion = "8.4"
        distributionType = Wrapper.DistributionType.ALL
    }
}
