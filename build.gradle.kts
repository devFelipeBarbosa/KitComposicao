plugins {
    id("java")
}

group = "br.com.evolution.kitcomposicao"
version = "1.0"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(8)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    compileOnly(files("libs/SankhyaW-extensions.jar"))
    compileOnly(files("libs/mge-modelcore-4.35b491.jar"))
    compileOnly(files("libs/mgecom-model-4.35b491.jar"))
    // Stubs de classes do servidor ausentes localmente (ex.: ServiceContext do ws).
    // Usados apenas na compilação; em runtime valem as classes reais do Sankhya.
    compileOnly(files("libs/sankhya-compile-stubs.jar"))
    // JDOM 1.x — mesma major usada pelo servidor Sankhya (org.jdom.Element).
    compileOnly("org.jdom:jdom:1.1.3")

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.test {
    useJUnitPlatform()
}

tasks.jar {
    archiveBaseName.set("KitComposicaoPA")
}
