package com.example.bluff.data.remote

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest

object SupabaseProvider {
    fun createClient(url: String, key: String): SupabaseClient {
        return createSupabaseClient(url, key) {
            install(Postgrest)
        }
    }
}
