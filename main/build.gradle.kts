import com.android.build.gradle.api.ApplicationVariant

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("checkstyle")
}

android {
    buildFeatures {
        aidl = true
        buildConfig = true
    }
    namespace = "de.embreis.openvpn"
    compileSdk = 35
    //compileSdkPreview = "UpsideDownCake"

    // Also update runcoverity.sh
    ndkVersion = "27.0.12077973"


    defaultConfig {
        minSdk = 21
        targetSdk = 35
        //targetSdkPreview = "UpsideDownCake"
        versionCode = 209
        versionName = "1.0.8"
        externalNativeBuild {
            cmake {
                //arguments+= "-DCMAKE_VERBOSE_MAKEFILE=1"
            }
        }
    }


    //testOptions.unitTests.isIncludeAndroidResources = true

    externalNativeBuild {
        cmake {
            path = File("${projectDir}/src/main/cpp/CMakeLists.txt")
        }
    }

    sourceSets {
        getByName("main") {
            assets.srcDirs("src/main/assets", "build/ovpnassets")

        }

        create("ui") {
        }

        create("skeleton") {
        }

        getByName("debug") {
        }

        getByName("release") {
        }
    }

            // ~/.gradle/gradle.properties
            // ~/.gradle/gradle.properties

    lint {
        enable += setOf("BackButton", "EasterEgg", "StopShip", "IconExpectedSize", "GradleDynamicVersion", "NewerVersionAvailable")
        checkOnly += setOf("ImpliedQuantity", "MissingQuantity")
        disable += setOf("MissingTranslation", "UnsafeNativeCodeLocation")
    }


    flavorDimensions += listOf("implementation", "ovpnimpl")

    productFlavors {
        create("ui") {
            dimension = "implementation"
        }

        create("skeleton") {
            dimension = "implementation"
            applicationId = "de.embreis.openvpn"
        }

        create("ovpn23")
        {
            dimension = "ovpnimpl"
            buildConfigField("boolean", "openvpn3", "true")
        }

    }

    buildTypes {
        getByName("release") {
            if (project.hasProperty("icsopenvpnDebugSign")) {
                logger.warn("property icsopenvpnDebugSign set, using debug signing for release")
            } else {
            }
        }
    }

    compileOptions {
        targetCompatibility = JavaVersion.VERSION_17
        sourceCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    splits {
        abi {
            isEnable = false
            reset()
            include("x86", "x86_64", "armeabi-v7a", "arm64-v8a")
            isUniversalApk = true
        }
    }

    packaging {
        jniLibs {
            useLegacyPackaging = true
        }
    }

    bundle {
        codeTransparency {
            signing {
                val keystoreTPFile: String? by project
                storeFile = keystoreTPFile?.let { file(it) }
                val keystoreTPPassword: String? by project
                storePassword = keystoreTPPassword
                val keystoreTPAliasPassword: String? by project
                keyPassword = keystoreTPAliasPassword
                val keystoreTPAlias: String? by project
                keyAlias = keystoreTPAlias

                if (keystoreTPFile?.isEmpty() ?: true)
                    println("keystoreTPFile not set, disabling transparency signing")
                if (keystoreTPPassword?.isEmpty() ?: true)
                    println("keystoreTPPassword not set, disabling transparency signing")
                if (keystoreTPAliasPassword?.isEmpty() ?: true)
                    println("keystoreTPAliasPassword not set, disabling transparency signing")
                if (keystoreTPAlias?.isEmpty() ?: true)
                    println("keyAlias not set, disabling transparency signing")

            }
        }
    }
}

var swigcmd = "swig"
// Workaround for macOS(arm64) and macOS(intel) since it otherwise does not find swig and
// I cannot get the Exec task to respect the PATH environment :(
if (file("/opt/homebrew/bin/swig").exists())
    swigcmd = "/opt/homebrew/bin/swig"
else if (file("/usr/local/bin/swig").exists())
    swigcmd = "/usr/local/bin/swig"


fun registerGenTask(variantName: String, variantDirName: String): File {
    val baseDir = File(buildDir, "generated/source/ovpn3swig/${variantDirName}")
    val genDir = File(baseDir, "net/openvpn/ovpn3")

    tasks.register<Exec>("generateOpenVPN3Swig${variantName}")
    {

        doFirst {
            mkdir(genDir)
        }
        commandLine(listOf(swigcmd, "-outdir", genDir, "-outcurrentdir", "-c++", "-java", "-package", "net.openvpn.ovpn3",
                "-Isrc/main/cpp/openvpn3/client", "-Isrc/main/cpp/openvpn3/",
                "-DOPENVPN_PLATFORM_ANDROID",
                "-o", "${genDir}/ovpncli_wrap.cxx", "-oh", "${genDir}/ovpncli_wrap.h",
                "src/main/cpp/openvpn3/client/ovpncli.i"))
        inputs.files( "src/main/cpp/openvpn3/client/ovpncli.i")
        outputs.dir( genDir)

    }
    return baseDir
}

android.applicationVariants.all(object : Action<ApplicationVariant> {
    override fun execute(variant: ApplicationVariant) {
        val sourceDir = registerGenTask(variant.name, variant.baseName.replace("-", "/"))
        val task = tasks.named("generateOpenVPN3Swig${variant.name}").get()

        variant.registerJavaGeneratingTask(task, sourceDir)
    }
})


dependencies {
    // https://maven.google.com/web/index.html
    implementation(libs.androidx.annotation)
    implementation(libs.androidx.core.ktx)

    uiImplementation(libs.android.view.material)
    uiImplementation(libs.androidx.activity)
    uiImplementation(libs.androidx.activity.ktx)
    uiImplementation(libs.androidx.appcompat)
    uiImplementation(libs.androidx.cardview)
    uiImplementation(libs.androidx.viewpager2)
    uiImplementation(libs.androidx.constraintlayout)
    uiImplementation(libs.androidx.core.ktx)
    uiImplementation(libs.androidx.fragment.ktx)
    uiImplementation(libs.androidx.lifecycle.runtime.ktx)
    uiImplementation(libs.androidx.lifecycle.viewmodel.ktx)
    uiImplementation(libs.androidx.preference.ktx)
    uiImplementation(libs.androidx.recyclerview)
    uiImplementation(libs.androidx.security.crypto)
    uiImplementation(libs.androidx.webkit)
    uiImplementation(libs.kotlin)
    uiImplementation(libs.mpandroidchart)
    uiImplementation(libs.square.okhttp)
    implementation("androidx.appcompat:appcompat:1.1.0")
    implementation("androidx.preference:preference:1.1.0")
    implementation("androidx.legacy:legacy-support-v4:1.0.0")
    implementation("com.google.android.material:material:1.0.0")
    implementation("androidx.constraintlayout:constraintlayout:1.1.3")
    testImplementation("junit:junit:4.12")
    implementation ("commons-codec:commons-codec:1.15")
    implementation("com.airbnb.android:lottie:5.2.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("mysql:mysql-connector-java:8.0.33")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("pl.droidsonroids.gif:android-gif-drawable:1.2.27")

   // implementation("com.google.android.gms:play-services-basement:18.4.0")
    testImplementation(libs.androidx.test.core)
    testImplementation(libs.junit)
    testImplementation(libs.kotlin)
    testImplementation(libs.mockito.core)
    testImplementation(libs.robolectric)
}

fun DependencyHandler.uiImplementation(dependencyNotation: Any): Dependency? =
    add("uiImplementation", dependencyNotation)