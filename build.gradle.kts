// Top-level build file where you can add configuration options common to all sub-projects/modules.

plugins {
    alias(libs.plugins.android.application) apply false  // Version Catalog 사용 시
    id("com.google.gms.google-services") version "4.4.2" apply false // Firebase 연동용
}