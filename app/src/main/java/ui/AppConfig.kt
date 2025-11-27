package ui

import com.google.firebase.BuildConfig

object AppConfig {
    // Configuración para desarrollo
    private const val DEBUG_IP = "192.168.100.22"
    private const val DEBUG_PORT = 9020
    private const val PRODUCTION_URL = "http://192.168.100.22"

    fun getBaseUrl(): String {
        return if (BuildConfig.DEBUG) {
            "http://$DEBUG_IP"
        } else {
            PRODUCTION_URL
        }
    }

    fun getDebugIp(): String {
        return DEBUG_IP
    }
}