plugins {
    `java-library`
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

dependencies {
    implementation(platform("org.springframework.boot:spring-boot-dependencies:3.4.4"))
    implementation(project(":rebac-core"))
    implementation("org.springframework.boot:spring-boot-autoconfigure")
    implementation("dev.openfga:openfga-sdk:0.9.7")
}
