
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false

    // Google Services plugin MUST have a version here!
    id("com.google.gms.google-services") version "4.4.2" apply false
    id("com.google.devtools.ksp") version "2.0.0-1.0.21" apply false


}
buildscript {
    dependencies {
        classpath("com.google.gms:google-services:4.4.0")
    }
}