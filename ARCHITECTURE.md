# Architecture Documentation

## Clean Architecture Implementation

Aplikasi ini mengikuti **Clean Architecture** dengan layer separation yang jelas:

```
┌─────────────────────────────────────────┐
│         Presentation Layer (UI)         │
│    - ViewModels (State Management)      │
│    - Compose Screens                    │
│    - Navigation                         │
└───────────────┬─────────────────────────┘
                │ depends on
┌───────────────▼─────────────────────────┐
│          Domain Layer (Core)            │
│    - Use Cases (Business Logic)         │
│    - Repository Interfaces              │
│    - Domain Models                      │
│    - Enums                              │
└───────────────┬─────────────────────────┘
                │ implemented by
┌───────────────▼─────────────────────────┐
│           Data Layer                    │
│    - Repository Implementations         │
│    - Data Sources (Firestore)          │
│    - Data Models Mappers                │
└─────────────────────────────────────────┘
```

## MVVM Pattern

### ViewModel Responsibilities (SLIM!)
✅ **DO:**
- Manage UI State
- Handle user interactions
- Call Use Cases
- Map domain results to UI state

❌ **DON'T:**
- Business logic validation
- Direct service/repository calls
- Data transformation
- Complex calculations

### Example: Refactored ViewModel

**Before (❌ Fat ViewModel):**
```kotlin
@HiltViewModel
class StudentEntryViewModel @Inject constructor(
    private val examService: ExamService,
    private val studentAccessService: StudentAccessService,
    private val examSessionService: ExamSessionService
) : ViewModel() {
    fun startExam() {
        // 80+ lines of business logic
        // Validation logic
        // Service orchestration
        // Error handling
    }
}
```

**After (✅ Slim ViewModel):**
```kotlin
@HiltViewModel
class StudentEntryViewModel @Inject constructor(
    private val startExamUseCase: StartExamUseCase  // Single dependency!
) : ViewModel() {
    fun startExam() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            when (val result = startExamUseCase(params)) {
                is Resource.Success -> handleSuccess(result.data)
                is Resource.Error -> handleError(result.message)
                is Resource.Loading -> {}
            }
        }
    }
}
```

## SOLID Principles

### 1. Single Responsibility Principle (SRP) ✅

**Each class has ONE reason to change:**

- `Validator`: Hanya untuk validasi input
- `StartExamUseCase`: Hanya untuk logic memulai ujian
- `ExamRepository`: Hanya untuk data access ujian
- `StudentEntryViewModel`: Hanya untuk UI state management

### 2. Open/Closed Principle (OCP) ✅

**Open for extension, closed for modification:**

```kotlin
// Base Use Case - extensible
abstract class BaseUseCase<in Params, out Type> {
    suspend operator fun invoke(params: Params): Resource<Type> {
        return try {
            withContext(dispatcher) {
                execute(params)
            }
        } catch (e: Exception) {
            Resource.Error(e)
        }
    }

    protected abstract suspend fun execute(params: Params): Resource<Type>
}

// Extend without modifying base
class StartExamUseCase : BaseUseCase<Params, ExamSession>() {
    override suspend fun execute(params: Params): Resource<ExamSession> {
        // Implementation
    }
}
```

### 3. Liskov Substitution Principle (LSP) ✅

**Derived classes must be substitutable for base classes:**

```kotlin
// Interface defines contract
interface ExamRepository {
    suspend fun getExamByCode(code: String): Resource<Exam>
}

// Implementation honors contract
class ExamRepositoryImpl @Inject constructor(
    private val examService: ExamService
) : ExamRepository {
    override suspend fun getExamByCode(code: String): Resource<Exam> {
        // Always returns Resource<Exam> as promised
    }
}
```

### 4. Interface Segregation Principle (ISP) ✅

**Clients should not depend on interfaces they don't use:**

**Before (❌ Fat Interface):**
```kotlin
interface ExamService {
    suspend fun getExamByCode(code: String): Exam?
    suspend fun createExam(...): Exam
    suspend fun listQuestions(...): List<ExamQuestion>
    suspend fun saveQuestions(...): Unit
    suspend fun listParticipants(...): List<ExamParticipant>
    suspend fun saveParticipants(...): Unit
}
```

**After (✅ Segregated Interfaces):**
```kotlin
interface ExamRepository {
    suspend fun getExamByCode(code: String): Resource<Exam>
    suspend fun createExam(draft: ExamDraft): Resource<Exam>
}

interface QuestionRepository {
    suspend fun getQuestions(examId: String): Resource<List<ExamQuestion>>
    suspend fun saveQuestions(...): Resource<Unit>
}

interface ParticipantRepository {
    suspend fun getParticipants(...): Resource<List<ExamParticipant>>
    suspend fun saveParticipants(...): Resource<Unit>
}
```

### 5. Dependency Inversion Principle (DIP) ✅

**Depend on abstractions, not concretions:**

```kotlin
// ❌ BAD: ViewModel depends on concrete implementation
class ViewModel(
    private val firestoreExamService: FirestoreExamService  // Concrete!
)

// ✅ GOOD: ViewModel depends on abstraction
class ViewModel(
    private val startExamUseCase: StartExamUseCase  // Abstraction
)

// Use Case depends on abstraction
class StartExamUseCase(
    private val examRepository: ExamRepository,  // Interface!
    private val studentRepository: StudentRepository  // Interface!
)
```

## Resource Pattern

Standardized result handling:

```kotlin
sealed class Resource<out T> {
    data class Success<T>(val data: T) : Resource<T>()
    data class Error(val exception: Throwable, val message: String?) : Resource<Nothing>()
    object Loading : Resource<Nothing>()
}

// Usage
when (val result = useCase(params)) {
    is Resource.Success -> handleSuccess(result.data)
    is Resource.Error -> handleError(result.message)
    is Resource.Loading -> showLoading()
}
```

## Use Cases

Business logic encapsulation:

### Structure:
```
domain/usecase/
├── BaseUseCase.kt
├── student/
│   ├── StartExamUseCase.kt
│   └── SubmitExamUseCase.kt
├── auth/
│   ├── LoginUseCase.kt
│   └── RegisterUseCase.kt
└── teacher/
    ├── CreateExamUseCase.kt
    └── ManageStudentsUseCase.kt
```

### Benefits:
1. **Testability**: Easy to unit test business logic
2. **Reusability**: Use cases can be reused across ViewModels
3. **Single Responsibility**: Each use case does one thing
4. **Independence**: No Android dependencies

## Repository Pattern

### Interface (Domain Layer):
```kotlin
interface ExamRepository {
    suspend fun getExamByCode(code: String): Resource<Exam>
    suspend fun createExam(draft: ExamDraft): Resource<Exam>
    suspend fun getExamsByTeacher(teacherId: String): Flow<Resource<List<Exam>>>
}
```

### Implementation (Data Layer):
```kotlin
class ExamRepositoryImpl @Inject constructor(
    private val examService: ExamService  // Data source
) : ExamRepository {
    override suspend fun getExamByCode(code: String): Resource<Exam> {
        return try {
            val exam = examService.getExamByCode(code)
            if (exam != null) Resource.Success(exam)
            else Resource.Error(...)
        } catch (e: Exception) {
            Resource.Error(e)
        }
    }
}
```

## Dependency Injection

### Module Organization:

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object DataModule {
    // Internal services (not exposed)
    @Provides @Singleton
    fun provideExamService(...): ExamService

    // Repositories (exposed to domain)
    @Provides @Singleton
    fun provideExamRepository(
        examService: ExamService
    ): ExamRepository {
        return ExamRepositoryImpl(examService)
    }
}
```

### Benefits:
- **Testability**: Easy to swap implementations
- **Flexibility**: Can change data sources without affecting domain
- **Clear boundaries**: Services are internal to data layer

## Validation

Centralized validation following SRP:

```kotlin
object Validator {
    data class ValidationResult(
        val isValid: Boolean,
        val errorMessage: String? = null
    )

    fun validateNis(nis: String): ValidationResult
    fun validateEmail(email: String): ValidationResult
    fun validatePassword(password: String): ValidationResult
}
```

## Key Improvements Summary

### ✅ What Was Fixed:

1. **Fat ViewModels → Slim ViewModels**
   - Business logic moved to Use Cases
   - ViewModels only manage UI state

2. **Direct Service Calls → Repository Pattern**
   - Added abstraction layer
   - Proper dependency inversion

3. **Scattered Validation → Centralized Validator**
   - Single source of truth
   - Reusable across app

4. **Inconsistent Error Handling → Resource Pattern**
   - Standardized result wrapper
   - Type-safe error handling

5. **Tight Coupling → Loose Coupling**
   - Depend on interfaces, not implementations
   - Easy to test and maintain

### 📊 Code Quality Metrics:

| Metric | Before | After |
|--------|--------|-------|
| ViewModel LOC | 145 | ~50 |
| Business Logic in ViewModel | ❌ Yes | ✅ No |
| Direct Service Dependencies | ❌ 3+ | ✅ 1 Use Case |
| Testability | Low | High |
| SOLID Compliance | ❌ | ✅ |

## Testing Strategy

### Unit Tests:

```kotlin
class StartExamUseCaseTest {
    @Test
    fun `should return error when NIS is empty`() = runTest {
        // Given
        val params = StartExamUseCase.Params(nis = "", examCode = "ABC")

        // When
        val result = useCase(params)

        // Then
        assertTrue(result is Resource.Error)
        assertEquals("Nomor Induk Siswa wajib diisi.", result.message)
    }
}
```

### ViewModel Tests:

```kotlin
class StudentEntryViewModelTest {
    @Test
    fun `should update UI state on successful exam start`() = runTest {
        // Given
        val mockUseCase = mockk<StartExamUseCase>()
        coEvery { mockUseCase(any()) } returns Resource.Success(mockSession)

        // When
        viewModel.startExam()

        // Then
        assertEquals(false, viewModel.uiState.value.isLoading)
        assertEquals(true, viewModel.uiState.value.navigateToExam)
    }
}
```

## Best Practices

1. ✅ **Always use interfaces** for repositories
2. ✅ **Encapsulate business logic** in Use Cases
3. ✅ **Keep ViewModels slim** - only UI state
4. ✅ **Use Resource wrapper** for all async operations
5. ✅ **Centralize validation** in Validator
6. ✅ **Follow naming conventions**:
   - Use Cases: `VerbNounUseCase` (e.g., `StartExamUseCase`)
   - Repositories: `NounRepository` (e.g., `ExamRepository`)
   - ViewModels: `ScreenViewModel` (e.g., `StudentEntryViewModel`)

## References

- [Android Architecture Guide](https://developer.android.com/topic/architecture)
- [Clean Architecture by Uncle Bob](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html)
- [SOLID Principles](https://en.wikipedia.org/wiki/SOLID)
- [Repository Pattern](https://developer.android.com/codelabs/basic-android-kotlin-training-repository-pattern)
