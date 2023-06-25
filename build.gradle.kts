// Top-level build file where you can add configuration options common to all sub-projects/modules.
@Suppress("DSL_SCOPE_VIOLATION") // TODO: Remove once KTIJ-19369 is fixed
plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.kotlinAndroid) apply false
    //kotlinx serialization plugin
    kotlin("plugin.serialization") version "1.8.10"
}
true // Needed to make the Suppress annotation work for the plugins block