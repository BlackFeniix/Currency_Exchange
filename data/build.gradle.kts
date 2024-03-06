plugins {
    alias(libs.plugins.jetbrainsKotlinJvm)
}

dependencies {
    implementation(project(":domain"))
    implementation(group = "javax.inject", name = "javax.inject", version = "1")
    api(libs.retrofit)
    api(libs.converter.gson)
}