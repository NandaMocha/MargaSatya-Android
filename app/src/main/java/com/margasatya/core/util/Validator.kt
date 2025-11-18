package com.margasatya.core.util

/**
 * Validator utility following Single Responsibility Principle
 */
object Validator {

    data class ValidationResult(
        val isValid: Boolean,
        val errorMessage: String? = null
    )

    fun validateNis(nis: String): ValidationResult {
        return when {
            nis.isBlank() -> ValidationResult(false, "Nomor Induk Siswa wajib diisi.")
            nis.length < 3 -> ValidationResult(false, "NIS minimal 3 karakter.")
            else -> ValidationResult(true)
        }
    }

    fun validateExamCode(code: String): ValidationResult {
        return when {
            code.isBlank() -> ValidationResult(false, "Kode ujian wajib diisi.")
            code.length < 4 -> ValidationResult(false, "Kode ujian minimal 4 karakter.")
            else -> ValidationResult(true)
        }
    }

    fun validateEmail(email: String): ValidationResult {
        return when {
            email.isBlank() -> ValidationResult(false, "Email wajib diisi.")
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() ->
                ValidationResult(false, "Format email tidak valid.")
            else -> ValidationResult(true)
        }
    }

    fun validatePassword(password: String): ValidationResult {
        return when {
            password.isBlank() -> ValidationResult(false, "Password wajib diisi.")
            password.length < 6 -> ValidationResult(false, "Password minimal 6 karakter.")
            else -> ValidationResult(true)
        }
    }

    fun validatePasswordMatch(password: String, confirmPassword: String): ValidationResult {
        return when {
            password != confirmPassword -> ValidationResult(false, "Password dan konfirmasi password tidak sama.")
            else -> ValidationResult(true)
        }
    }

    fun validateName(name: String): ValidationResult {
        return when {
            name.isBlank() -> ValidationResult(false, "Nama wajib diisi.")
            name.length < 3 -> ValidationResult(false, "Nama minimal 3 karakter.")
            else -> ValidationResult(true)
        }
    }

    fun validateExamTitle(title: String): ValidationResult {
        return when {
            title.isBlank() -> ValidationResult(false, "Nama ujian wajib diisi.")
            title.length < 3 -> ValidationResult(false, "Nama ujian minimal 3 karakter.")
            else -> ValidationResult(true)
        }
    }

    fun validateUrl(url: String): ValidationResult {
        return when {
            url.isBlank() -> ValidationResult(false, "URL wajib diisi.")
            !android.util.Patterns.WEB_URL.matcher(url).matches() ->
                ValidationResult(false, "Format URL tidak valid.")
            else -> ValidationResult(true)
        }
    }
}
