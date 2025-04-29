plugins {
    id("com.android.application")
    id("com.google.gms.google-services") // Firebase 연동용
}

android {
    namespace = "com.example.silmedy"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.silmedy"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // 기존 buildConfigField 은 그대로 두세요
        val kakaoKey: String? = project.findProperty("KAKAO_NATIVE_APP_KEY") as String?
        buildConfigField("String", "KAKAO_NATIVE_APP_KEY", "\"$kakaoKey\"")

        val hfKey = (project.findProperty("HUGGINGFACE_API_KEY") as? String).orEmpty()
        val hfUrl = (project.findProperty("HUGGINGFACE_API_URL") as? String).orEmpty()
        buildConfigField("String", "HUGGINGFACE_API_KEY", "\"$hfKey\"")
        buildConfigField("String", "HUGGINGFACE_API_URL", "\"$hfUrl\"")
    }

    buildTypes {
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
    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

    // —— debug 변형에서 src/debug/assets 완전 제외 ——
    sourceSets {
        getByName("debug").apply {
            assets.setSrcDirs(emptyList<Any>())
        }
    }


    packaging {
        resources {
            // 중복된 모델 파일을 첫 번째만 포함하도록
            pickFirsts.add("assets/model_unquant.tflite")
            // 기존 excludes 는 그대로 두세요
            excludes.add("META-INF/native-image/**")
            excludes.add("META-INF/licenses/**")
            excludes.add("META-INF/DEPENDENCIES")
            excludes.add("META-INF/NOTICE")
            excludes.add("META-INF/LICENSE")
            excludes.add("META-INF/ASL2.0")
            excludes.add("META-INF/*.kotlin_module")
            excludes.add("google/protobuf/field_mask.proto")
            excludes.add("google/protobuf/descriptor.proto")
        }
    }
}

dependencies {
    // Volley networking library
    implementation("com.android.volley:volley:1.2.1")

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.annotation)
    implementation(libs.lifecycle.livedata.ktx)
    implementation(libs.lifecycle.viewmodel.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // Firebase BoM (Bill of Materials)
    implementation(platform("com.google.firebase:firebase-bom:33.12.0"))
    implementation("com.google.android.material:material:1.11.0") // 버전은 네 프로젝트에 맞춰서
    // Glide
    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")

    // transform 기능 쓰려면 이거도 추가
    implementation("jp.wasabeef:glide-transformations:4.3.0")

    // Firebase Storage
    implementation("com.google.firebase:firebase-storage:20.2.1")

    // 사용 Firebase SDK (예: Analytics + Auth + Firestore 등)
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-auth:22.1.2") // SNS 인증위해 최신 버전 사용
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-messaging:23.4.1")
    implementation("com.google.android.gms:play-services-location:21.0.1")
    implementation("com.squareup.okhttp3:okhttp:4.10.0")
    implementation("androidx.room:room-runtime:2.5.1")

    // TensorFlow Lite for Android
    implementation("org.tensorflow:tensorflow-lite:2.14.0")
    implementation("org.tensorflow:tensorflow-lite-support:0.4.4")
    implementation("org.tensorflow:tensorflow-lite-select-tf-ops:2.14.0")
}

// Exclude Google's protobuf that conflicts with nd4j's protobuf and nd4j's bundled guava
configurations.all {
}