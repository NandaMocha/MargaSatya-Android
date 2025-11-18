package com.margasatya.domain.usecase.auth

import com.margasatya.core.util.Resource
import com.margasatya.core.util.Validator
import com.margasatya.domain.model.User
import com.margasatya.domain.repository.AuthRepository
import com.margasatya.domain.usecase.BaseUseCase
import javax.inject.Inject

class RegisterUseCase @Inject constructor(
    private val authRepository: AuthRepository
) : BaseUseCase<RegisterUseCase.Params, User>() {

    data class Params(
        val name: String,
        val email: String,
        val password: String,
        val confirmPassword: String
    )

    override suspend fun execute(params: Params): Resource<User> {
        // Validate name
        val nameValidation = Validator.validateName(params.name)
        if (!nameValidation.isValid) {
            return Resource.Error(
                IllegalArgumentException(nameValidation.errorMessage)
            )
        }

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

        // Validate password match
        val matchValidation = Validator.validatePasswordMatch(params.password, params.confirmPassword)
        if (!matchValidation.isValid) {
            return Resource.Error(
                IllegalArgumentException(matchValidation.errorMessage)
            )
        }

        return authRepository.register(params.name, params.email, params.password)
    }
}
