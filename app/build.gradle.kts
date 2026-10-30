import com.android.build.api.variant.FilterConfiguration
import java.net.URI
import java.util.Properties
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

/**
 * Stages the document-detection model into the assets of the application.
 *
 * The model is committed under `models/`, so a normal build needs no network
 * access at all. Downloading is only a fallback for checkouts that do not ship
 * the binary, and it fails loudly instead of producing an APK without a model.
 */
abstract class PrepareTFLiteModelTask : DefaultTask() {

    // Read lazily and tolerate a missing directory, so that a checkout without
    // the binary still builds (it then falls back to the download below).
    @get:Internal
    abstract val localModelsDirectory: DirectoryProperty

    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    @TaskAction
    fun prepare() {
        val modelVersion = "v1.2.0"
        // Name of the asset as published by the upstream project (part of the download URL).
        val upstreamModelFileName = "fairscan-segmentation-model.tflite"
        // Name used inside this application's assets.
        val modelFileName = "document-segmentation.tflite"

        val outputFile = outputDirectory.file(modelFileName).get().asFile
        val localFile = localModelsDirectory.orNull?.asFile?.resolve(modelFileName)

        if (localFile != null && localFile.isFile && localFile.length() > 0L) {
            if (!outputFile.exists() || outputFile.length() != localFile.length()) {
                outputFile.parentFile.mkdirs()
                localFile.copyTo(outputFile, overwrite = true)
            }
            println("Model taken from ${localFile.absolutePath}")
            return
        }

        if (outputFile.exists() && outputFile.length() > 0L) {
            println("Model already present: ${outputFile.absolutePath}")
            return
        }

        val modelUrl =
            "https://github.com/pynicolas/fairscan-segmentation-model/releases/download/$modelVersion/$upstreamModelFileName"
        println("Model missing locally, downloading it from $modelUrl")
        outputFile.parentFile.mkdirs()
        try {
            URI(modelUrl).toURL().openStream().use { input ->
                outputFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
        } catch (e: Exception) {
            outputFile.delete()
            throw GradleException(
                "Could not obtain $modelFileName and no copy was found in " +
                    "app/models/. Build offline by placing the file there " +
                    "(upstream: $modelUrl). Cause: ${e.message}",
                e,
            )
        }
    }
}

val prepareTFLiteModel = tasks.register<PrepareTFLiteModelTask>("prepareTFLiteModel") {
    localModelsDirectory.set(layout.projectDirectory.dir("models"))
    outputDirectory.set(layout.buildDirectory.dir("downloads/tflite"))
}

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.aboutLibrariesAndroid)
    alias(libs.plugins.kotlin.serialization)
}

val abiCodes = mapOf(
    "arm64-v8a" to 0,
    "armeabi-v7a" to -1,
    "x86_64" to -2,
)

android {
    namespace = "com.paperin.scan"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.paperin.scan"
        // Based on tests against virtual devices, the app works with Android 8.0 (API level 26).
        // It crashes because of LiteRT on earlier versions.
        // LiteRT documentation only states that version 1.2.0 requires Android 12:
        // https://ai.google.dev/edge/litert/android/index
        minSdk = 26
        targetSdk = 37
        versionCode = 7 // increment by 3 because of ABI-specific APKs
        versionName = "1.0.2"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    androidResources {
        // Keep the bundled binaries stored instead of deflated:
        //  - .tflite must be mmap-able (LiteRT maps the asset file directly),
        //  - .traineddata is copied to private storage on first launch and is
        //    already compressed, so inflating it would only add work and peak
        //    memory on the first run.
        noCompress += listOf("tflite", "traineddata")
    }

    // Release signing material is read from keystore/keystore.properties, which
    // is git-ignored, so a fresh clone still builds (unsigned) without any
    // manual setup. CI can override it with -PRELEASE_STORE_FILE=... and so on.
    val keystorePropertiesFile = rootProject.file("keystore/keystore.properties")
    val keystoreProperties = Properties().apply {
        if (keystorePropertiesFile.isFile) {
            keystorePropertiesFile.inputStream().use { load(it) }
        }
    }

    fun signingValue(key: String): String? =
        (project.findProperty(key) as? String)?.takeIf { it.isNotBlank() }
            ?: keystoreProperties.getProperty(key)?.takeIf { it.isNotBlank() }

    val releaseStoreFile = signingValue("RELEASE_STORE_FILE")
    val releaseStorePassword = signingValue("RELEASE_STORE_PASSWORD")
    val releaseKeyAlias = signingValue("RELEASE_KEY_ALIAS")
    val releaseKeyPassword = signingValue("RELEASE_KEY_PASSWORD")

    val hasSigning = listOf(
        releaseStoreFile,
        releaseStorePassword,
        releaseKeyAlias,
        releaseKeyPassword
    ).all { it != null }

    signingConfigs {
        if (hasSigning) {
            create("release") {
                storeFile = rootProject.file(releaseStoreFile!!)
                storePassword = releaseStorePassword
                keyAlias = releaseKeyAlias
                keyPassword = releaseKeyPassword
                // APK signature scheme v2+ is required for minSdk 26 anyway;
                // v3/v4 add key rotation and faster installs.
                enableV3Signing = true
                enableV4Signing = true
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            if (hasSigning) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }

    // See https://developer.android.com/build/configure-apk-splits
    val isBuildingBundle = gradle.startParameter.taskNames.any { it.lowercase().contains("bundle") }
    splits {
        abi {
            // Disable split ABIs when building appBundle: https://issuetracker.google.com/issues/402800800
            isEnable = !isBuildingBundle
            reset()
            include(*abiCodes.keys.toTypedArray())
            isUniversalApk = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
        }
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

androidComponents {
    onVariants { variant ->
        variant.sources.assets?.addGeneratedSourceDirectory(
            prepareTFLiteModel,
            PrepareTFLiteModelTask::outputDirectory,
        )
        variant.outputs.forEach { output ->
            val abi = output.filters
                .find { it.filterType == FilterConfiguration.FilterType.ABI }
                ?.identifier
            // The build type is part of the name so that debug and release APKs
            // of the same ABI never collide (they differ in signing key, so they
            // must stay distinguishable when attached to one release).
            output.outputFileName =
                "PaperIn-${output.versionName.get()}-${variant.buildType}-${abi}.apk"
        }
    }
}

dependencies {

    implementation(project(":imageprocessing")) {
        exclude(group = "org.openpnp", module = "opencv")
    }

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.documentfile)
    implementation(libs.litert)
    implementation(libs.litert.support)
    implementation(libs.litert.metadata)
    implementation(libs.opencv)
    implementation(libs.pdfbox) {
        // To reduce APK size
        exclude("org.bouncycastle")
    }
    implementation(libs.icons.extended)
    implementation(libs.zoomable)
    implementation(libs.reorderable)
    implementation(libs.aboutlibraries.compose.m3)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.collections.immutable)
    implementation(libs.tesseract4android)

    testImplementation(libs.junit)
    testImplementation(libs.assertj)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}

aboutLibraries {
    library {
        // Enable the duplication mode, allows to merge, or link dependencies which relate
        duplicationMode = com.mikepenz.aboutlibraries.plugin.DuplicateMode.MERGE
        // Configure the duplication rule, to match "duplicates" with
        duplicationRule = com.mikepenz.aboutlibraries.plugin.DuplicateRule.SIMPLE
    }
}

// See https://developer.android.com/build/configure-apk-splits
androidComponents {
    onVariants { variant ->
        variant.outputs.forEach { output ->
            val name = output.filters.find { it.filterType == com.android.build.api.variant.FilterConfiguration.FilterType.ABI }?.identifier
            val baseAbiCode = abiCodes[name]
            if (baseAbiCode != null) {
                output.versionCode.set(output.versionCode.get() + baseAbiCode)
            }
        }
    }
}
