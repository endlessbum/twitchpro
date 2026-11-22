import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.navigation.safeargs)
    alias(libs.plugins.apollo)
}

kotlin {
    jvmToolchain(21)
}

android {
    // Release signing lives outside the repo: keystore.properties (gitignored) points to it.
    // Without it any Release task fails loudly instead of silently falling back to the debug key.
    val keystorePropsFile = rootProject.file("keystore.properties")
    val keystoreProps = Properties().apply {
        if (keystorePropsFile.exists()) keystorePropsFile.reader(Charsets.UTF_8).use { load(it) }
    }
    val wantsReleaseBuild = gradle.startParameter.taskNames.any { it.contains("Release", ignoreCase = true) }
    if (wantsReleaseBuild && !keystorePropsFile.exists()) {
        throw GradleException(
            "Release signing is not configured: create keystore.properties in the project root " +
                "(storeFile, storePassword, keyAlias, keyPassword) pointing at a non-committed keystore."
        )
    }

    // Aggregate tasks (assemble, build, bundle) can pull in the release variant without "Release"
    // in the requested task name — guard the actual release tasks themselves as well (NEW-009).
    if (!keystorePropsFile.exists()) {
        tasks.matching { it.name.contains("Release", ignoreCase = true) }.configureEach {
            doFirst {
                throw GradleException(
                    "Release signing is not configured: create keystore.properties in the project root " +
                        "(storeFile, storePassword, keyAlias, keyPassword) pointing at a non-committed keystore."
                )
            }
        }
    }

    signingConfigs {
        getByName("debug") {
            // the project debug keystore is not committed; when it is absent (fresh clone)
            // the debug build falls back to the default ~/.android debug keystore
            if (file("debug-keystore.jks").exists()) {
                keyAlias = "debug"
                keyPassword = "123456"
                storeFile = file("debug-keystore.jks")
                storePassword = "123456"
            }
        }
        if (keystorePropsFile.exists()) {
            create("release") {
                storeFile = rootProject.file(keystoreProps["storeFile"] as String)
                storePassword = keystoreProps["storePassword"] as String
                keyAlias = keystoreProps["keyAlias"] as String
                keyPassword = keystoreProps["keyPassword"] as String
            }
        }
    }
    namespace = "com.endlessbum.twitchpro"
    compileSdk = 37

    defaultConfig {
        // separate applicationId: installs next to the original Xtra / Xtra TV
        applicationId = "com.endlessbum.twitchpro"
        minSdk = 24
        targetSdk = 37
        versionCode = 166
        versionName = "1.1.0-tv"
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-DEBUG"
            if (file("debug-keystore.jks").exists()) {
                signingConfig = signingConfigs.getByName("debug")
            }
        }
        release {
            isShrinkResources = true
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            if (keystorePropsFile.exists()) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }
    buildFeatures {
        buildConfig = true
        compose = true
        viewBinding = true
    }
    androidResources {
        generateLocaleConfig = true
    }
    lint {
        disable += "ContentDescription"
    }
    packaging.jniLibs.excludes.addAll(listOf(
        "lib/x86/libtranslate_jni.so",
        "lib/x86/liblanguage_id_l2c_jni.so",
        "lib/x86_64/libtranslate_jni.so",
        "lib/x86_64/liblanguage_id_l2c_jni.so",
        "lib/armeabi-v7a/libtranslate_jni.so",
        "lib/armeabi-v7a/liblanguage_id_l2c_jni.so",
    ))
}
dependencies {
    compileOnly("com.google.j2objc:j2objc-annotations:3.0.0") // OkHttpDataSource SettableFuture
    implementation("com.google.mlkit:language-id:17.0.6")
    implementation("com.google.mlkit:translate:17.0.3")

    implementation(libs.material)
    implementation(libs.markwon.core)
    implementation(libs.markwon.linkify)

    implementation(platform(libs.compose.bom))
    implementation(libs.compose.foundation)
    implementation(libs.compose.material3)
    implementation(libs.compose.ui)
    implementation(libs.activity.compose)
    implementation(libs.tv.material)

    implementation(libs.activity)
    implementation(libs.appcompat)
    implementation(libs.constraintlayout)
    implementation(libs.coordinatorlayout)
    implementation(libs.core)
    implementation(libs.fragment)
    implementation(libs.lifecycle.service)
    implementation(libs.lifecycle.viewmodel)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    implementation(libs.paging.runtime)
    implementation(libs.preference.ktx)
    implementation(libs.recyclerview)
    implementation(libs.room.runtime)
    ksp(libs.room.compiler)
    implementation(libs.room.paging)
    implementation(libs.swiperefreshlayout)
    implementation(libs.viewpager2)
    implementation(libs.webkit)
    implementation(libs.work.runtime)

    implementation(libs.cronet.api)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation(libs.conscrypt)
    implementation(libs.serialization.json)
    implementation(libs.apollo.api)

    implementation(libs.media3.exoplayer)
    implementation(libs.media3.exoplayer.hls)
    implementation(libs.media3.ui)
    implementation(libs.media3.effect)
    implementation(libs.media3.session)

    implementation(libs.coil)
    implementation(libs.coil.compose)
    implementation(libs.coil.gif)
    implementation(libs.coil.okhttp)

    implementation(libs.glide)
    ksp(libs.glide.ksp)
    implementation(libs.glide.okhttp)
    implementation(libs.glide.webpdecoder)

    implementation(libs.coroutines)
}

apollo {
    @Suppress("ApolloEndpointNotConfigured")
    service("service") {
        packageName.set("com.endlessbum.twitchpro.graphql")
    }
}

