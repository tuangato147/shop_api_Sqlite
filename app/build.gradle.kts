plugins {
        alias(libs.plugins.android.application)
    }

    android {
        namespace = "com.example.shop"
        compileSdk = 35

        defaultConfig {
            applicationId = "com.example.shop"
            minSdk = 24
            targetSdk = 35
            versionCode = 1
            versionName = "1.0"
        }

        buildTypes {
            debug {
                isDebuggable = true
            }
            release {
                isMinifyEnabled = false
                proguardFiles(
                    getDefaultProguardFile("proguard-android-optimize.txt"),
                    "proguard-rules.pro"
                )
            }
        }

        compileOptions {
            sourceCompatibility = JavaVersion.VERSION_11
            targetCompatibility = JavaVersion.VERSION_11
        }
    }

    dependencies {
        implementation(libs.appcompat)
        implementation(libs.material)
        testImplementation(libs.junit)
        androidTestImplementation(libs.ext.junit)
        androidTestImplementation(libs.espresso.core)

        implementation("androidx.recyclerview:recyclerview:1.3.0")
        implementation("androidx.cardview:cardview:1.0.0")
        implementation("com.google.android.material:material:1.9.0")
        implementation("com.squareup.picasso:picasso:2.71828")
        implementation("com.squareup.retrofit2:retrofit:2.9.0")
        implementation("com.squareup.retrofit2:converter-gson:2.9.0")
        implementation("com.squareup.okhttp3:logging-interceptor:4.9.1")
        implementation("com.google.code.gson:gson:2.8.9")

        implementation("com.github.bumptech.glide:glide:4.12.0")
        annotationProcessor("com.github.bumptech.glide:compiler:4.12.0")
    }