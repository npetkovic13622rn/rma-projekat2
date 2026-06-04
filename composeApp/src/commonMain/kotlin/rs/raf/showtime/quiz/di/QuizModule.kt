package rs.raf.showtime.quiz.di

import de.jensklingenberg.ktorfit.Ktorfit
import io.ktor.client.HttpClient
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module
import rs.raf.showtime.core.network.ApiConstants
import rs.raf.showtime.networking.di.Qualifiers
import rs.raf.showtime.quiz.data.QuizApi
import rs.raf.showtime.quiz.data.QuizRepositoryImpl
import rs.raf.showtime.quiz.data.createQuizApi
import rs.raf.showtime.quiz.data.generator.QuizGenerator
import rs.raf.showtime.quiz.domain.QuizRepository
import rs.raf.showtime.quiz.presentation.intro.QuizIntroViewModel
import rs.raf.showtime.quiz.presentation.question.QuizQuestionViewModel

val quizModule = module {
    single<QuizApi> {
        Ktorfit.Builder()
            .httpClient(get<HttpClient>(Qualifiers.Authenticated))
            .baseUrl(ApiConstants.BASE_URL)
            .build()
            .createQuizApi()
    }

    single { QuizGenerator() }

    single {
        QuizRepositoryImpl(
            appDatabase = get(),
            quizApi = get(),
            quizGenerator = get(),
        )
    } bind QuizRepository::class

    viewModelOf(::QuizIntroViewModel)
    viewModelOf(::QuizQuestionViewModel)
}
