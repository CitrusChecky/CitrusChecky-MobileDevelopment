package com.rivaphys.citruschecky.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.rivaphys.citruschecky.data.InformationItem

class InfoViewModel : ViewModel() {

    private val _navigateToGallery = MutableLiveData<Boolean>()
    val navigateToGallery: LiveData<Boolean> = _navigateToGallery

    private val _navigateToHome = MutableLiveData<Boolean>()
    val navigateToHome: LiveData<Boolean> = _navigateToHome

    private val _informationItems = MutableLiveData<List<InformationItem>>()
    val informationItems: LiveData<List<InformationItem>> = _informationItems

    init {
        loadInformationData()
    }

    private fun loadInformationData() {
        val items = listOf(
            InformationItem(
                title = "Deskripsi",
                description = "CitrusChecky adalah aplikasi yang menggunakan teknologi  Artificial Intelligence (AI) untuk mendeteksi tingkat kebusukan buah jeruk secara real time. Aplikasi ini membantu untuk menilai kualitas jeruk dari mulai matang, sedikit busuk, dan sangat busuk" +
                        "\n\nPilih menu kamera untuk mengambil gambar secara langsung atau pilih dari galeri dengan memilih menu galeri"
            ),
            InformationItem(
                title = "Kondisi Optimal",
                description = "• Pencahayaan cukup dan merata (tidak terlalu gelap atau terang) \n" +
                        "• Jeruk tidak tertutup plastik atau kemasan \n" +
                        "• Multiple jeruk tidak saling menimpa \n" +
                        "• Gunakan tripod untuk menjaga stabilitas kamera\n" +
                        "• Pastikan gambar tajam, tidak blur, dan fokus pada objek"
            ),
            InformationItem(
                title = "Tentang Developer",
                description = "Dikembangkan oleh Rivanda Mahdiyansyah sebagai bagian dari penelitian tugas akhir dalam bidang Computer Vision dan Deep Learning."
            )
        )
        _informationItems.value = items
    }

    fun navigateToGallery() {
        _navigateToGallery.value = true
    }

    fun onGalleryNavigationComplete() {
        _navigateToGallery.value = false
    }

    fun navigateToHome() {
        _navigateToHome.value = true
    }

    fun onHomeNavigationComplete() {
        _navigateToHome.value = false
    }
}