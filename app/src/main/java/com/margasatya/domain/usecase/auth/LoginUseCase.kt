package com.margasatya.domain.usecase.auth

import com.margasatya.core.util.Resource
import com.margasatya.core.util.Validator
import com.margasatya.domain.model.User
import com.margasatya.domain.repository.AuthRepository
import com.margasatya.domain.usecase.BaseUseCase
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val authRepository: AuthRepository
) : BaseUseCase<LoginUseCase.Params, User>() {

    data class Params(
        val email: String,
        val password: String
    )

    override suspend fun execute(params: Params): Resource<User> {
        // Validate email
        val emailValidation = Validator.validateEmail(params.email)
        if (!emailValidation.isValid) {
            return Resource.Error(
                IllegalArgumentException(emailValidation.errorMessage)
            )
        }

        // Validate password
        val passwordValidation = Validator.validatePassword(params.password)
        if (!passwordValidation.isValid) {
            return Resource.Error(
                IllegalArgumentException(passwordValidation.errorMessage)
            )
        }

        return authRepository.login(params.email, params.password)
    }
}
