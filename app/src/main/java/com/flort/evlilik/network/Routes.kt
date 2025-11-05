package com.flort.evlilik.network

class Routes {
    companion object {
        const val BASE_URL = "https://admin54.askologapp.com:3000/api/"
        const val SOCKET_URL = "https://admin54.askologapp.com:3001"
        
        // Auth endpoints
        const val LOGIN = "auth/login"
        const val REGISTER = "auth/register"
        const val LOGOUT = "auth/logout"
        const val LOGOUT_ALL = "auth/logout-all"
        const val PROFILE = "auth/profile"
        const val VALIDATE_TOKEN = "auth/validate-token"
        const val FORGOT_PASSWORD = "auth/forgot-password"
        const val CHECK_EMAIL = "auth/check-email"
        const val GOOGLE_LOGIN = "auth/google-login"

        //user endpoints
        const val UPDATE_INFO = "users/update-info"
        const val UPDATE_PASSWORD = "users/update-password"
        const val UPDATE_SECURITY_SETTINGS = "users/update-security-settings"
        const val GET_GALLERY = "users/gallery"
        const val UPDATE_GALLERY = "users/update-gallery"
        const val BLOCK_USER = "users/block"
        const val UNBLOCK_USER = "users/unblock"
        const val CHECK_BLOCK_STATUS = "users/check-block-status/{targetUserId}"
        const val BLOCKED_USERS = "users/blocked-users"
        const val TOGGLE_PREMIUM = "users/toggle-premium"

        // Legal endpoints    
        const val TERMS = "legal/terms"
        const val PRIVACY = "legal/privacy"

        // Settings endpoints
        const val SETTINGS = "options/get"

        // Contact us endpoints
        const val SEND_TICKET = "tickets/"
        const val REMOVE_MESSAGES = "tickets/remove-messages"
        const val START_MESSAGES = "tickets/start-messages"
        const val REPORT_REASONS = "tickets/report-reasons"
        const val SEND_REPORT = "tickets/report"

        //files
        const val UPLOAD_FILE = "files/upload"

        // Package endpoints
        const val MAIN_PACKAGES = "packages/main"
        const val DISCOUNT_PACKAGES = "packages/discount"
        const val VIP_PACKAGES = "packages/vip"
        const val APPLY_COUPON = "packages/apply-coupon"
        const val USE_COUPON = "packages/use-coupon"
        
        // Purchase endpoints
        const val PURCHASE_TOKEN = "purchase/token"
        const val PURCHASE_VIP = "purchase/vip"

        // Profile endpoints
        const val HOME_PROFILES = "profiles/home"
        const val LIKE_PROFILE = "profiles/like"
        const val UNLIKE_PROFILE = "profiles/unlike"
        const val CHECK_LIKE_STATUS = "profiles/like-status/{targetUserId}"
        const val MY_LIKES = "profiles/my-likes"
        
        // Message endpoints
        const val GET_CONVERSATIONS = "messages/conversations"
        const val GET_MESSAGES = "messages/conversations/{conversationId}/messages"
        const val START_CONVERSATION = "messages/conversations/start"
        const val DELETE_CONVERSATION = "messages/conversations/{conversationId}"
        const val DELETE_MESSAGE = "messages/{messageId}"
        const val GET_UNREAD_COUNT = "messages/unread"
        const val GET_MESSAGE_COSTS = "options/message-costs"
        const val GET_WALLET = "users/wallet"
        const val CHECK_MESSAGE_PERMISSION = "users/check-message-permission"
        
        // Gift endpoints
        const val GET_GIFTS = "gifts"
    }
}