package com.rivaphys.citruschecky.utils

object ClassNameFormatter {

    /**
     * Mengkonversi nama kelas dari model menjadi format yang user-friendly
     * @param className nama kelas dari model (contoh: "matang", "sedikit-busuk", "sangat-busuk")
     * @return nama kelas yang sudah diformat (contoh: "Matang", "Sedikit Busuk", "Sangat Busuk")
     */
    fun formatClassName(className: String): String {
        return when (className.lowercase()) {
            "matang" -> "Matang"
            "sedikit-busuk" -> "Sedikit Busuk"
            "sangat-busuk" -> "Sangat Busuk"
            else -> {
                // Fallback: capitalize first letter dan replace dash dengan spasi
                className.split("-")
                    .joinToString(" ") { word ->
                        word.lowercase().replaceFirstChar {
                            if (it.isLowerCase()) it.titlecase() else it.toString()
                        }
                    }
            }
        }
    }

    /**
     * Mendapatkan warna yang sesuai dengan kondisi jeruk
     * @param className nama kelas dari model
     * @return kode warna dalam format Color
     */
    fun getClassColor(className: String): Int {
        return when (className.lowercase()) {
            "matang" -> android.graphics.Color.GREEN
            "sedikit-busuk" -> android.graphics.Color.parseColor("#FFA500") // Orange
            "sangat-busuk" -> android.graphics.Color.RED
            else -> android.graphics.Color.BLUE // Default color
        }
    }

    /**
     * Mendapatkan deskripsi kondisi jeruk
     * @param className nama kelas dari model
     * @return deskripsi kondisi jeruk
     */
    fun getClassDescription(className: String): String {
        return when (className.lowercase()) {
            "matang" -> "Jeruk dalam kondisi baik dan siap dikonsumsi"
            "sedikit-busuk" -> "Jeruk mulai menunjukkan tanda-tanda kerusakan ringan"
            "sangat-busuk" -> "Jeruk dalam kondisi rusak dan tidak layak dikonsumsi"
            else -> "Kondisi jeruk tidak diketahui"
        }
    }
}