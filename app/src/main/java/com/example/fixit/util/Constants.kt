// app/src/main/java/com/example/fixit/util/Constants.kt
package com.example.fixit.util // Ini adalah deklarasi paket untuk file ini

// Object untuk menampung semua konstanta yang akan digunakan di aplikasi
object Constants {
    // ID unik untuk Notification Channel. Ini harus string unik di dalam aplikasi Anda.
    // Android menggunakan ini secara internal untuk mengidentifikasi channel.
    const val NOTIFICATION_CHANNEL_ID = "fixit_order_channel"

    // Nama yang akan ditampilkan kepada pengguna di pengaturan notifikasi perangkat mereka.
    // Contoh: "FixIT Order Updates"
    const val NOTIFICATION_CHANNEL_NAME = "FixIT Order Updates"

    // ID unik untuk setiap notifikasi yang akan ditampilkan.
    // Jika Anda mengirim beberapa notifikasi dengan ID yang sama, notifikasi yang lebih baru akan menggantikan yang lama.
    // Jika Anda ingin setiap notifikasi tampil secara terpisah, gunakan ID unik setiap kali.
    const val NOTIFICATION_ID = 1 // ID unik untuk notifikasi order
}