## 🍊 CitrusChecky — Deteksi Kebusukan Jeruk Secara Real-Time Berbasis Android

Aplikasi ini sepenuhnya dikembangkan sebagai bagian dari **Tugas Akhir** pada jenjang **Sarjana Informatika**.  
Menggunakan model **YOLOv8n** dengan akurasi **93%**, CitrusChecky dapat **mendeteksi dan mengklasifikasikan tingkat kebusukan buah jeruk, yaitu Matang, Sedikit Busuk, dan Sangat Busuk** secara **real-time** langsung dari kamera atau galeri.

> 💡 **Tidak hanya mendeteksi** — aplikasi ini juga terhubung dengan **Gemini AI** untuk memberikan rekomendasi seperti resep kreatif berdasarkan kondisi buah yang terdeteksi.

---

### 🏛 Arsitektur
- **MVVM (Model–View–ViewModel)** 🧩

---

### 🛠 Teknologi yang Digunakan
- 💻 **Kotlin**
- 🤖 **TensorFlow Lite**
- 📷 **CameraX**
- ✨ **Google Generative AI (Gemini)**
- 🔄 **Coroutines**
- 🎨 **Material Design**
- 🖼 **Glide**
- 🧭 **Navigation Component**
- 🔵 **Dots Indicator**

---

### 🚀 Fitur Utama
1. **✨ Splash Screen & Onboarding**  
   Menampilkan logo aplikasi dan 3 slide informasi utama mengenai fitur dan manfaat aplikasi.

2. **📋 Halaman Utama**  
   - Deskripsi singkat aplikasi  
   - Kondisi optimal penggunaan  
   - Informasi pengembang  
   - Pilihan untuk **ambil gambar dari kamera** 📸 atau **pilih dari galeri** 🖼

3. **📊 Halaman Hasil Deteksi**  
   - Menampilkan **bounding box**, label kelas, dan skor akurasi  
   - Rekomendasi 2 resep kegunaan pada hasil deteksi 
   - Tombol menuju obrolan **CheckyAI** 🤖

4. **💬 CheckyAI**  
   - Menampilkan 4-5 rekomendasi resep kegunaan dari buah jeruk yang sudah dideteksi

---

📌 **Catatan:**  
Aplikasi ini dirancang untuk membantu pengguna — mulai dari petani hingga konsumen — agar lebih mudah mengenali kualitas buah jeruk tanpa perlu analisis manual yang memakan waktu.

---
