plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.services)
}

android {
    namespace = "com.tuempresa.quecocino"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.tuempresa.quecocino"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            storeFile = rootProject.file(project.property("RELEASE_STORE_FILE") as String)
            storePassword = project.property("RELEASE_STORE_PASSWORD") as String
            keyAlias = project.property("RELEASE_KEY_ALIAS") as String
            keyPassword = project.property("RELEASE_KEY_PASSWORD") as String
        }
    }

    buildTypes {
        getByName("debug") {
            // Agrega este flag para activar el bypass de login en test E2E
            buildConfigField("Boolean", "E2E_TEST", "true")
        }
        getByName("release") {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // El flag en release queda en false
            buildConfigField("Boolean", "E2E_TEST", "false")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    // Firebase BoM (gestión de versiones)
    implementation(platform("com.google.firebase:firebase-bom:33.16.0"))

    // Firebase SDKs
    implementation("com.google.firebase:firebase-database-ktx")
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-appcheck-interop")           // 🔧 Agregado para evitar el crash
    implementation("com.google.firebase:firebase-appcheck-playintegrity")    // Opcional: si usas App Check
    implementation("com.google.firebase:firebase-firestore-ktx")             // Si usas Firestore

    // Android y Jetpack
    implementation(libs.androidx.core.ktx)
    implementation("androidx.core:core-splashscreen:1.0.1")
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("io.coil-kt:coil-compose:2.5.0")
    implementation ("androidx.compose.material:material-icons-extended:1.5.0")
    implementation(libs.androidx.navigation.compose)
    implementation(libs.play.services.auth)
    implementation(libs.google.material)
    implementation(libs.firebase.firestore.ktx)   // Si usas Firestore
    implementation(libs.firebase.firestore)       // Si usas Firestore
    implementation(libs.coil.compose)

    // Pruebas
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    androidTestImplementation ("androidx.compose.ui:ui-test-junit4:1.6.0")
    androidTestImplementation ("androidx.compose.ui:ui-test-manifest:1.6.0")

}

// Aplica el plugin de Google Services al final
apply(plugin = "com.google.gms.google-services")
