package com.rio.rostry.config

/**
 * App Configuration - Controls demo mode and payment settings
 * 
 * Set DEMO_MODE = false when you have real payment SDKs and keys
 * Set DEMO_MODE = true to run app without external payment dependencies
 */
object AppConfig {
    
    /**
     * Demo Mode Configuration
     * 
     * When true:
     * - Uses mock payments instead of real payment gateways
     * - Simulates Google Play Billing without SDK
     * - Simulates Stripe payments without API keys
     * - Shows "Demo Mode" labels in UI
     * - All monetization features work without external dependencies
     * 
     * When false:
     * - Requires real Google Play Billing SDK
     * - Requires real Stripe API keys
     * - Uses production payment gateways
     */
    const val DEMO_MODE = true
    
    /**
     * Payment Gateway Configuration
     */
    object Payments {
        // Google Play Billing
        const val GOOGLE_PLAY_BILLING_ENABLED = !DEMO_MODE
        const val GOOGLE_PLAY_LICENSE_KEY = "YOUR_GOOGLE_PLAY_LICENSE_KEY_HERE"
        
        // Stripe Configuration
        const val STRIPE_ENABLED = !DEMO_MODE
        const val STRIPE_PUBLISHABLE_KEY = "pk_test_YOUR_STRIPE_PUBLISHABLE_KEY_HERE"
        const val STRIPE_SECRET_KEY = "sk_test_YOUR_STRIPE_SECRET_KEY_HERE"
        
        // PayPal Configuration
        const val PAYPAL_ENABLED = !DEMO_MODE
        const val PAYPAL_CLIENT_ID = "YOUR_PAYPAL_CLIENT_ID_HERE"
        
        // Mock Payment Settings (for demo mode)
        const val MOCK_PAYMENT_SUCCESS_RATE = 95 // 95% success rate
        const val MOCK_PAYMENT_DELAY_MS = 2000L // 2 second delay
    }
    
    /**
     * Feature Flags
     */
    object Features {
        const val VERIFICATION_ENABLED = true
        const val SHOWCASE_ENABLED = true
        const val WALLET_ENABLED = true
        const val CHAT_ENABLED = true
        const val MARKETPLACE_ENABLED = true
        const val TRANSFER_ENABLED = true
        
        // Premium features (require coins)
        const val PREMIUM_FEATURES_ENABLED = true
        const val COIN_SYSTEM_ENABLED = true
    }
    
    /**
     * Coin Pricing Configuration
     */
    object CoinPricing {
        const val VERIFICATION_FEE = 50
        const val LISTING_FEE = 10
        const val FEATURED_LISTING_FEE = 25
        const val SHOWCASE_FEE_DAY = 5
        const val SHOWCASE_FEE_WEEK = 15
        const val SHOWCASE_FEE_MONTH = 40
        const val BOOST_LISTING_FEE = 20
    }
    
    /**
     * Order Fee Configuration
     */
    object OrderFees {
        const val PLATFORM_FEE_PERCENTAGE = 0.05 // 5%
        const val HANDLING_CHARGE = 2.50
        const val PACKAGING_CHARGE = 1.50
        const val PROCESSING_CHARGE = 3.00
        const val DEFAULT_DELIVERY_CHARGE = 10.00
    }
    
    /**
     * Demo Mode Helper Functions
     */
    fun isDemoMode(): Boolean = DEMO_MODE
    
    fun getDemoModeLabel(): String = if (DEMO_MODE) " (Demo Mode)" else ""
    
    fun getPaymentMethodLabel(method: String): String {
        return if (DEMO_MODE) "$method (Simulated)" else method
    }
    
    /**
     * Development Settings
     */
    object Development {
        const val DEBUG_LOGGING = true
        const val SHOW_DEBUG_INFO = DEMO_MODE
        const val MOCK_USER_DATA = DEMO_MODE
        const val SKIP_ONBOARDING = false
    }
    
    /**
     * API Configuration
     */
    object API {
        val BASE_URL = if (DEMO_MODE) "https://demo-api.rostry.com" else "https://api.rostry.com"
        const val TIMEOUT_SECONDS = 30L
        const val RETRY_ATTEMPTS = 3
    }
}