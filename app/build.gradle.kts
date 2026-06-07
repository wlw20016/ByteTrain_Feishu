import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.Exec
import java.util.Locale

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

val androidMinSdk = 23
val androidRustTargets = listOf(
    AndroidRustTarget("arm64-v8a", "aarch64-linux-android", "aarch64-linux-android"),
    AndroidRustTarget("armeabi-v7a", "armv7-linux-androideabi", "armv7a-linux-androideabi"),
    AndroidRustTarget("x86", "i686-linux-android", "i686-linux-android"),
    AndroidRustTarget("x86_64", "x86_64-linux-android", "x86_64-linux-android"),
)
val rustSdkDir = rootProject.layout.projectDirectory.dir("sdk/rust")
val generatedRustJniLibsDir = layout.buildDirectory.dir("rustJniLibs")
val ndkHome = providers.environmentVariable("ANDROID_NDK_HOME")
    .orElse(providers.environmentVariable("ANDROID_NDK_ROOT"))

android {
    namespace = "com.bytetrain.feishuclone"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.bytetrain.feishuclone"
        minSdk = androidMinSdk
        targetSdk = 36
        versionCode = 1
        versionName = "0.1.0"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    sourceSets {
        getByName("main") {
            manifest.srcFile("src/main/AndroidManifest.xml")
            jniLibs.srcDirs("src/main/jniLibs", generatedRustJniLibsDir)
            java.srcDirs(
                "src/main/kotlin",
                "../shared",
                "../features",
            )
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

dependencies {
    testImplementation(kotlin("test"))
}

val cargoBuildRustAndroidTargets = androidRustTargets.map { target ->
    tasks.register<Exec>("cargoBuild${target.taskName}Debug") {
        val linkerEnvName = "CARGO_TARGET_${target.rustTriple.uppercase(Locale.US).replace('-', '_')}_LINKER"
        val linker = ndkHome.map { home ->
            file("$home/toolchains/llvm/prebuilt/windows-x86_64/bin/${target.clangPrefix}$androidMinSdk-clang.cmd")
        }.get()
        val outputFile = rustSdkDir.file("target/${target.rustTriple}/debug/libbytetrain_feed_sdk.so")

        require(linker.exists()) { "Android NDK clang not found: ${linker.absolutePath}" }
        inputs.dir(rustSdkDir.dir("src"))
        inputs.file(rustSdkDir.file("Cargo.toml"))
        inputs.file(rustSdkDir.file("Cargo.lock"))
        outputs.file(outputFile)
        environment(linkerEnvName, linker.absolutePath)
        commandLine(
            "cargo",
            "build",
            "--manifest-path",
            rustSdkDir.file("Cargo.toml").asFile.absolutePath,
            "--target",
            target.rustTriple,
        )
    }
}

val buildRustAndroidJniLibs = tasks.register<Copy>("buildRustAndroidJniLibs") {
    dependsOn(cargoBuildRustAndroidTargets)
    into(generatedRustJniLibsDir)

    androidRustTargets.forEach { target ->
        from(rustSdkDir.file("target/${target.rustTriple}/debug/libbytetrain_feed_sdk.so")) {
            into(target.abi)
        }
    }
}

tasks.matching { task ->
    task.name.startsWith("merge") && task.name.endsWith("JniLibFolders")
}.configureEach {
    dependsOn(buildRustAndroidJniLibs)
}

data class AndroidRustTarget(
    val abi: String,
    val rustTriple: String,
    val clangPrefix: String,
) {
    val taskName: String =
        abi.split('-', '_').joinToString("") { part ->
            part.replaceFirstChar { char ->
                if (char.isLowerCase()) char.titlecase(Locale.US) else char.toString()
            }
        }
}
