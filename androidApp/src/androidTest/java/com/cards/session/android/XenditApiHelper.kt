package com.cards.session.android

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.Base64

/**
 * Helper class for making API calls to Xendit for testing purposes
 */
class XenditApiHelper(
    private val apiKey: String,
    private val baseUrl: String
) {
    
    /**
     * Create a session for card payments
     */
    suspend fun createSession(
        referenceId: String,
        amount: Int,
        locale: String,
        country: String,
        currency: String,
        customerId: String,
        sessionType: String,
        description: String? = null,
        cardPaymentTokenId: String? = null,
        metadata: Map<String, Any>? = null
    ): Map<String, Any> = withContext(Dispatchers.IO) {
        val url = URL("$baseUrl/sessions")
        println("URL: ${url}");
        val connection = url.openConnection() as HttpURLConnection
        
        try {
            // Set up connection
            connection.requestMethod = "POST"
            connection.doOutput = true
            connection.setRequestProperty("Content-Type", "application/json")
            connection.setRequestProperty("Accept", "application/json")
            
            // Set authorization header
            val authHeader = if (apiKey.startsWith("Basic ")) {
                apiKey
            } else {
                "Basic " + Base64.getEncoder().encodeToString("$apiKey:".toByteArray())
            }
            connection.setRequestProperty("Authorization", authHeader)
            
            // Create request body
            val requestBody = JSONObject().apply {
                put("mode", "CARDS_SESSION_JS")
                put("amount", amount)
                put("locale", locale)
                put("country", country)
                put("currency", currency)
                put("customer_id", customerId)
                put("reference_id", referenceId)
                put("session_type", sessionType)
                description?.let { put("description", it) }
                
                // Channel properties
                put("channel_properties", JSONObject().apply {
                    put("cards", JSONObject().apply {
                        put("skip_three_ds", true)
                    })
                })
                
                // Cards session JS properties
                put("cards_session_js", JSONObject().apply {
                    cardPaymentTokenId?.let { put("card_payment_token_id", it) }
                    put("success_return_url", "https://yourcompany.com/success")
                    put("failure_return_url", "https://yourcompany.com/failure")
                })
                
                metadata?.let { 
                    put("metadata", JSONObject(it))
                }
            }
            
            // Send request
            connection.outputStream.use { os ->
                os.write(requestBody.toString().toByteArray())
            }
            
            // Read response
            val responseCode = connection.responseCode
            val responseText = if (responseCode in 200..299) {
                connection.inputStream.bufferedReader().use { it.readText() }
            } else {
                connection.errorStream?.bufferedReader()?.use { it.readText() } ?: ""
            }
            
            if (responseCode in 200..299) {
                // Parse JSON response to Map
                val jsonResponse = JSONObject(responseText)
                jsonResponse.toMap()
            } else {
                throw Exception("Failed to create session: $responseCode - $responseText")
            }
        } finally {
            connection.disconnect()
        }
    }
    
    /**
     * Extract session ID from session response
     */
    fun extractSessionId(sessionResponse: Map<String, Any>): String? {
        return sessionResponse["payment_session_id"] as? String
    }
    
    /**
     * Get session details
     */
    suspend fun getSession(sessionId: String): Map<String, Any> = withContext(Dispatchers.IO) {
        val url = URL("$baseUrl/sessions/$sessionId")
        val connection = url.openConnection() as HttpURLConnection
        
        try {
            // Set up connection
            connection.requestMethod = "GET"
            connection.setRequestProperty("Accept", "application/json")
            
            // Set authorization header
            val authHeader = if (apiKey.startsWith("Basic ")) {
                apiKey
            } else {
                "Basic " + Base64.getEncoder().encodeToString("$apiKey:".toByteArray())
            }
            connection.setRequestProperty("Authorization", authHeader)
            
            // Read response
            val responseCode = connection.responseCode
            val responseText = if (responseCode == 200) {
                connection.inputStream.bufferedReader().use { it.readText() }
            } else {
                connection.errorStream?.bufferedReader()?.use { it.readText() } ?: ""
            }
            
            if (responseCode == 200) {
                // Parse JSON response to Map
                val jsonResponse = JSONObject(responseText)
                jsonResponse.toMap()
            } else {
                throw Exception("Failed to get session: $responseCode - $responseText")
            }
        } finally {
            connection.disconnect()
        }
    }
    
    /**
     * Extension function to convert JSONObject to Map
     */
    private fun JSONObject.toMap(): Map<String, Any> {
        val map = mutableMapOf<String, Any>()
        keys().forEach { key ->
            val value = get(key)
            map[key] = when (value) {
                is JSONObject -> value.toMap()
                else -> value
            }
        }
        return map
    }
}