# Marga Satya - Aplikasi Ujian Aman

Aplikasi ujian aman berbasis Android yang mendukung Exam Lock Mode untuk mencegah siswa keluar dari aplikasi saat ujian berlangsung.

## Fitur Utama

### 1. Role-based System
- **Siswa**: Mengerjakan ujian dengan mode terkunci
- **Guru**: Mengelola siswa, membuat dan mengelola ujian
- **Admin**: Melihat statistik dan konfigurasi aplikasi

### 2. Exam Lock Mode
- Menggunakan LockTask Mode untuk mencegah siswa:
  - Pindah ke aplikasi lain
  - Menggunakan split screen / multi-window
  - Keluar dari aplikasi saat ujian

### 3. Tipe Ujian
#### Google Form
- Menampilkan Google Form di WebView yang terkunci
- Siswa mengerjakan ujian di Google Form
- Setelah selesai, siswa menekan tombol "Selesai" di aplikasi

#### In-App Exam
- Soal ditampilkan langsung di aplikasi
- Mendukung soal pilihan ganda dan esai
- Jawaban dienkripsi sebelum dikirim ke Firestore
- Navigasi antar soal dengan indikator progress
- Auto-save jawaban saat pindah soal

### 4. Penanganan Koneksi Jelek
- Jika gagal submit karena koneksi internet:
  - Exam Lock Mode dilepas
  - Jawaban tersimpan di perangkat (terenkripsi)
  - Siswa dapat retry submit setelah koneksi stabil
  - Soal tidak ditampilkan selama pending

## Teknologi

### Core Technologies
- **Language**: Kotlin
- **UI Framework**: Jetpack Compose (Material 3)
- **Architecture**: MVVM + Repository Pattern
- **Navigation**: Jetpack Navigation (Single Activity)
- **Backend**: Firebase Firestore + Firebase Authentication
- **Dependency Injection**: Hilt

### Security
- **Encryption**: AES-256-GCM untuk enkripsi jawaban
- **Storage**: EncryptedSharedPreferences untuk kunci enkripsi
- **Lock Mode**: Android LockTask Mode

### Libraries
- Firebase BOM 32.6.0
- Compose BOM 2023.10.01
- Hilt 2.48
- AndroidX Security Crypto
- Kotlin Coroutines

## Setup Project

### Prerequisites
- Android Studio Hedgehog atau lebih baru
- JDK 17
- Android SDK minimal API 26 (Android 8.0)

### Langkah-langkah Setup

1. **Clone Repository**
   ```bash
   git clone <repository-url>
   cd MargaSatya-Android
   ```

2. **Setup Firebase**
   - Buat project baru di [Firebase Console](https://console.firebase.google.com)
   - Tambahkan aplikasi Android dengan package name: `com.margasatya`
   - Download file `google-services.json`
   - Replace file `app/google-services.json` dengan file yang baru didownload

3. **Enable Firestore**
   - Di Firebase Console, aktifkan Cloud Firestore
   - Mulai dengan mode test atau production sesuai kebutuhan
   - Set Firestore rules:

   ```
   rules_version = '2';
   service cloud.firestore {
     match /databases/{database}/documents {
       // Users collection
       match /users/{userId} {
         allow read, write: if request.auth != null && request.auth.uid == userId;
       }

       // Students collection
       match /students/{studentId} {
         allow read: if request.auth != null;
         allow write: if request.auth != null;
       }

       // Exams collection
       match /exams/{examId} {
         allow read: if request.auth != null;
         allow write: if request.auth != null;

         match /questions/{questionId} {
           allow read: if request.auth != null;
           allow write: if request.auth != null;
         }

         match /participants/{participantId} {
           allow read: if request.auth != null;
           allow write: if request.auth != null;
         }
       }

       // Exam sessions
       match /examSessions/{sessionId} {
         allow read: if request.auth != null;
         allow write: if true; // Students need to write without auth

         match /answers/{answerId} {
           allow read: if request.auth != null;
           allow write: if true; // Students need to write without auth
         }
       }

       // App config
       match /appConfigs/{configId} {
         allow read: if true;
         allow write: if request.auth != null;
       }
     }
   }
   ```

4. **Enable Firebase Authentication**
   - Di Firebase Console, aktifkan Authentication
   - Enable Email/Password provider

5. **Build Project**
   ```bash
   ./gradlew build
   ```

6. **Run on Device/Emulator**
   ```bash
   ./gradlew installDebug
   ```

## Struktur Project

```
app/src/main/java/com/margasatya/
├── core/                          # Core modules
│   ├── encryption/                # EncryptionService
│   ├── lock/                      # ExamLockManager
│   └── network/                   # NetworkMonitor
├── data/                          # Data layer
│   ├── repository/                # Firestore implementations
│   └── service/                   # Service interfaces
├── di/                            # Dependency Injection
│   ├── AppModule.kt
│   └── DataModule.kt
├── domain/                        # Domain models
│   ├── enums/                     # Enums
│   └── model/                     # Data models
├── ui/                            # UI layer
│   ├── admin/                     # Admin screens
│   ├── components/                # Reusable components
│   ├── navigation/                # Navigation setup
│   ├── role/                      # Role selection
│   ├── student/                   # Student screens
│   ├── teacher/                   # Teacher screens
│   ├── theme/                     # Material 3 theme
│   └── MainActivity.kt
└── MargaSatyaApplication.kt
```

## Data Model (Firestore Collections)

### Collections
- `users` - Data user (Guru, Admin)
- `students` - Data siswa
- `exams` - Data ujian
  - Subcollection: `questions` - Soal ujian
  - Subcollection: `participants` - Peserta ujian
- `examSessions` - Sesi ujian siswa
  - Subcollection: `answers` - Jawaban siswa (terenkripsi)
- `appConfigs` - Konfigurasi aplikasi

## User Guide

### Untuk Guru

1. **Registrasi**
   - Pilih "Masuk sebagai Guru"
   - Klik "Belum punya akun? Daftar di sini"
   - Isi nama, email, dan password
   - Klik "Daftar"

2. **Menambah Siswa**
   - Login sebagai Guru
   - Pilih "Kelola Siswa"
   - Klik tombol "+" (Tambah Siswa)
   - Isi NIS, nama, dan kelas
   - Klik "Simpan"

3. **Membuat Ujian**
   - Login sebagai Guru
   - Pilih "Kelola Ujian"
   - Klik tombol "+" (Buat Ujian)
   - Isi nama ujian, deskripsi, dan kode ujian
   - Pilih tipe ujian:
     - **In-App**: Untuk ujian yang dibuat di aplikasi
     - **Google Form**: Masukkan URL Google Form
   - Klik "Simpan"

### Untuk Siswa

1. **Mengerjakan Ujian**
   - Pilih "Masuk sebagai Siswa"
   - Masukkan NIS dan Kode Ujian
   - Klik "Mulai Ujian"
   - Aplikasi akan masuk Exam Lock Mode
   - Kerjakan soal-soal ujian
   - Klik "Kumpulkan Ujian" setelah selesai

2. **Jika Koneksi Bermasalah**
   - Aplikasi akan otomatis masuk mode pending
   - Lock mode akan dilepas
   - Setelah koneksi stabil, klik "Cek Koneksi & Kirim Ulang"

### Untuk Admin

1. **Login Admin**
   - Pilih "Masuk sebagai Admin"
   - Masukkan email dan password admin
   - Login

2. **Melihat Statistik**
   - Dashboard menampilkan:
     - Total Guru
     - Total Siswa
     - Total Ujian
     - Ujian Berjalan
     - Ujian Selesai
     - Sesi Hari Ini

## Security Considerations

### Exam Lock Mode
- Hanya berfungsi di device yang support LockTask Mode
- Perlu testing di device fisik (tidak optimal di emulator)
- Untuk production, pertimbangkan untuk menggunakan Device Owner mode

### Encryption
- Jawaban dienkripsi menggunakan AES-256-GCM
- Kunci enkripsi tersimpan aman di EncryptedSharedPreferences
- Associated data menggunakan questionId dan sessionId untuk additional security

### Firestore Rules
- Pastikan Firestore rules sudah di-setup dengan benar
- Hindari menggunakan mode test di production
- Review dan update rules sesuai kebutuhan

## Known Issues & Limitations

1. **Lock Task Mode**
   - Membutuhkan device owner atau permission khusus
   - Tidak optimal di emulator
   - Beberapa device manufacturer mungkin memiliki implementasi yang berbeda

2. **WebView**
   - Google Form mungkin memerlukan JavaScript
   - Pastikan WebView ter-update di device

3. **Offline Support**
   - Saat ini hanya mendukung retry manual untuk submission pending
   - Belum ada auto-retry di background

## Future Enhancements

- [ ] Auto-retry submission di background
- [ ] Timer countdown untuk ujian
- [ ] Foto profil siswa
- [ ] Laporan hasil ujian untuk guru
- [ ] Export hasil ujian ke Excel/PDF
- [ ] Notifikasi push untuk ujian baru
- [ ] Support tablet dengan layout yang lebih baik
- [ ] Dark mode support
- [ ] Multi-language support

## Testing

Untuk menjalankan unit tests:
```bash
./gradlew test
```

Untuk menjalankan instrumentation tests:
```bash
./gradlew connectedAndroidTest
```

## License

[Specify your license here]

## Contact

Untuk pertanyaan atau dukungan, silakan hubungi:
- Email: [your-email@example.com]
- GitHub Issues: [repository-url]/issues

## Acknowledgments

- PRD Template berdasarkan requirement dari project specification
- Material Design 3 Guidelines
- Firebase Documentation
- Android Security Best Practices
