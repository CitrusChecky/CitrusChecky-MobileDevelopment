package com.rivaphys.citruschecky.repository

import com.rivaphys.citruschecky.data.ClassRecipes
import com.rivaphys.citruschecky.data.Recipe

object RecipeRepository {
    fun getRecipesByClassName(className: String): List<Recipe> {
        return when (className.lowercase()) {
            "matang" -> listOf(
                Recipe(
                    "Puding jeruk",
                    "Bahan:\n" +
                            "\n" +
                            "• 650 ml susu uht\n" +
                            "• 3 buah jeruk ukuran sedang\n" +
                            "• 1/2 sdt garam\n" +
                            "• 1 bungkus agar-agar Swalow plain\n" +
                            "• 1 sdm maizena larutkan dengan 50 ml air\n" +
                            "\n" +
                            "Cara membuat:\n" +
                            "• Siapkan semua bahan kupas jeruk bersihkan kulit arinya\n" +
                            "• Siapkan panci tuang susu, gula dan agar agar nyalakan api kecil biar panas merata, setelah meletup letup masukkan larutan tepung maizena aduk rata sampai mendidih, angkat dinginkan sampai uap hilang\n" +
                            "• Tuang ke dalam cetakan gelas yang sudah di beri jeruk perlahan\n" +
                            "• Biarkan dingin, setelah itu masukkan kulkas\n" +
                            "• Potong potong puding jeruk gunakan pisau yang tajam biar hasil potongan halus\n" +
                            "• Siap disajikan bisa buat camilan"
                            ),
                Recipe(
                    "Es Jeruk Biji Selasih",
                    "Bahan:\n" +
                            "\n" +
                            "• 3 bh jeruk medan\n" +
                            "• 1 sdt selasih\n" +
                            "• Air hangat (untuk merendam biji selasih)\n" +
                            "• 1 sachet tropicana slim stevia\n" +
                            "• 200 ml air\n" +
                            "• Secukupnya es batu\n" +
                            "\n" +
                            "Cara membuat:\n" +
                            "• Siapkan bahan,rendam selasih dengan air hangat\n" +
                            "• Siapkan gelas saji, tuang gula singkong tambahkan es batu tuang air perasan jeruk\n" +
                            "• Tambahkan air dan biji selasih. Es jeruk siap dinikmati\n"
                )
            )

            "sedikit-busuk" -> listOf(
                Recipe(
                    "Pembersih Minyak & Kotoran Alami (Tanpa Sabun Cuci)",
                    "Bahan:\n" +
                            "\n" +
                            "• Jeruk sedikit busuk (terutama bagian kulit dan dagingnya)\n" +
                            "\n" +
                            "Cara membuat:\n" +
                            "\n" +
                            "• Ambil jeruk busuk, potong jadi dua atau remas agar bagian dalam terbuka.\n" +
                            "• Gunakan langsung untuk menggosok permukaan berminyak seperti panci, piring, atau kompor.\n" +
                            "• Tidak perlu menambahkan air atau sabun.\n" +
                            "• Setelah digosok, bersihkan sisa kulit jeruk dan lap permukaan dengan tisu kering.\n" +
                            "• Minyak dan kotoran akan hilang, dan akan meninggalkan aroma jeruk alami.\n" +
                            "\n" +
                            "Catatan: Efektif karena minyak atsiri dan asam alami dalam kulit jeruk dapat mengangkat lemak dan noda."

                ),
                Recipe(
                    "Penghapus Noda Tinta di Tangan",
                    "Bahan:\n" +
                            "\n" +
                            "• Kulit jeruk sedikit busuk (yang masih utuh)\n" +
                            "\n" +
                            "Cara membuat:\n" +
                            "\n" +
                            "• Kupas kulit jeruk sedikit busuk, pastikan bagian luar masih baik.\n" +
                            "• Tekan kulit jeruk ke bagian tangan yang terkena noda tinta (pena, spidol, dll).\n" +
                            "• Gosok beberapa kali hingga tinta hilang.\n" +
                            "• Jika digosok terlalu kuat, mungkin kulit menjadi sedikit merah—ini normal dan akan hilang.\n" +
                            "\n" +
                            "Catatan: Minyak atsiri dari kulit jeruk membantu meluruhkan tinta. Bisa juga dipakai di dinding yang terkena coretan tinta."
                )
            )

            "sangat-busuk" -> listOf(
                Recipe(
                    "Pupuk Organik Cair dari Jeruk Busuk",
                    "Bahan:\n" +
                            "\n" +
                            "• Beberapa jeruk busuk\n" +
                            "• Air bersih\n" +
                            "• Botol plastik bekas (hindari botol kaca)\n" +
                            "• Plastik wrap (untuk menutup)\n" +
                            "\n" +
                            "Cara membuat:\n" +
                            "\n" +
                            "• Potong bagian atas botol plastik (sekitar sepertiga bagian).\n" +
                            "• Masukkan jeruk busuk, lalu hancurkan dengan ulekan atau rolling pin.\n" +
                            "• Tambahkan air hingga hampir penuh (sisakan 2 cm dari atas).\n" +
                            "• Tutup dengan plastik wrap, diamkan di tempat berventilasi selama 5–7 hari untuk fermentasi.\n" +
                            "• Buka tutup botol setiap 1–2 hari untuk mengeluarkan gas.\n" +
                            "• Setelah 7 hari, saring cairan dan gunakan untuk menyiram tanaman (encerkan jika perlu).\n" +
                            "\n" +
                            "Manfaat: Cairan ini kaya vitamin dan nutrisi, sangat baik untuk menyuburkan tanaman dan bisa mengusir hama karena aroma jeruk yang kuat."
                ),
                Recipe(
                    "Menumbuhkan Buah Jeruk dari Jeruk Busuk",
                    "Bahan:\n" +
                            "\n" +
                            "• Jeruk busuk (masih memiliki biji)\n" +
                            "• Pot kosong dan tanah\n" +
                            "• Air semprot\n" +
                            "• Plastik wrap\n" +
                            "\n" +
                            "Cara membuat:\n" +
                            "\n" +
                            "• Belah jeruk dan pisahkan jadi beberapa bagian agar bijinya mudah keluar.\n" +
                            "• Tanam seluruh bagian jeruk ke dalam pot tanah.\n" +
                            "• Semprot dengan air secukupnya.\n" +
                            "• Tutup pot dengan plastik wrap agar kelembapan terjaga\n" +
                            "• Diamkan selama ±2 minggu hingga biji mulai tumbuh.\n" +
                            "\n" +
                            "Catatan: Kulit jeruk akan membusuk dan menambah keasaman tanah, membantu biji tumbuh lebih cepat. Bisa tambahkan pupuk organik tambahan jika ingin hasil maksimal."
                )
            )

            else -> emptyList()
        }
    }

    fun getAllRecipesByDetections(detections: List<String>): List<ClassRecipes> {
        return detections.distinct().map { className ->
            ClassRecipes(className, getRecipesByClassName(className))
        }
    }
}