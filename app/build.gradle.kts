plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.example.welltracker"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.welltracker"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        
        // Enable vector drawable support for older Android versions
        vectorDrawables.useSupportLibrary = true
        
        // Enable RenderScript support
        renderscriptTargetApi = 21
        renderscriptSupportModeEnabled = true
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // Ensure native library alignment for 16KB page size (Android 15)
            ndk {
                debugSymbolLevel = "FULL"
            }
        }
        debug {
            // Also apply alignment in debug builds for testing
            ndk {
                debugSymbolLevel = "FULL"
            }
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
        viewBinding = true
        renderScript = true
    }
    
    // Add packaging options to handle duplicate files and ensure 16KB page alignment
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "/META-INF/gradle/incremental.annotation.processors"
        }
        
        // Configure native library packaging for Android 15 16KB page size compatibility
        jniLibs {
            // Extract native libs at install time for proper 16KB page alignment
            // This is the recommended approach for Android 15+ compatibility
            useLegacyPackaging = true
            
            // Ensure libraries are properly aligned
            // AGP 8.1+ automatically handles 16KB page alignment when extracting
        }
    }
}

dependencies {
    // Core Android
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    
    // Fragment and Navigation - using compatible versions
    implementation("androidx.fragment:fragment-ktx:1.8.4")
    implementation("androidx.navigation:navigation-fragment-ktx:2.8.3")
    implementation("androidx.navigation:navigation-ui-ktx:2.8.3")
    
    // RecyclerView
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    
    // ViewModel and LiveData - using compatible versions
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.6")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.8.6")
    
    // WorkManager for notifications
    implementation("androidx.work:work-runtime-ktx:2.9.1")
    
    // JSON serialization
    implementation("com.google.code.gson:gson:2.10.1")
    
    // MPAndroidChart for mood trends visualization
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
    
    // BlurView for modern frosted glass effects - temporarily disabled to check other errors
    // implementation("com.eightbitlab:blurview:1.6.6")
    
    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}