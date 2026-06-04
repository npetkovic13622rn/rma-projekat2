package rs.raf.showtime.quiz.data.generator

import rs.raf.showtime.movies.data.local.entity.CastMemberEntity
import rs.raf.showtime.movies.data.local.entity.MovieEntity
import rs.raf.showtime.movies.data.local.entity.MovieImageEntity
import rs.raf.showtime.quiz.domain.QuizAnswer
import rs.raf.showtime.quiz.domain.QuizException
import rs.raf.showtime.quiz.domain.QuizQuestion
import rs.raf.showtime.quiz.domain.QuizQuestionType
import rs.raf.showtime.quiz.domain.QuizRules
import rs.raf.showtime.quiz.domain.QuizSession
import kotlin.random.Random

class QuizGenerator {

    fun generate(
        movies: List<MovieEntity>,
        imagesByMovie: Map<String, List<MovieImageEntity>>,
        castByMovie: Map<String, List<CastMemberEntity>>,
        allCast: List<CastMemberEntity>,
    ): QuizSession {
        val candidates = movies
            .filter { movie -> movie.imageOptions(imagesByMovie).isNotEmpty() }
            .shuffled()

        if (candidates.size < QuizRules.QuestionCount) {
            throw QuizException("Browse the catalog first to populate your quiz pool.")
        }

        val questions = mutableListOf<QuizQuestion>()
        val typeCounts = mutableMapOf<QuizQuestionType, Int>()
        val usedImages = mutableSetOf<String>()
        val moviePool = candidates.toMutableList()
        var attempts = 0
        val maxAttempts = candidates.size * QuizQuestionType.entries.size * 6

        while (questions.size < QuizRules.QuestionCount && attempts < maxAttempts) {
            attempts += 1
            if (moviePool.isEmpty()) moviePool += candidates.shuffled()
            val movie = moviePool.removeAt(0)

            var selectedType: QuizQuestionType? = null
            var question: QuizQuestion? = null
            for (type in typeOrder(typeCounts)) {
                question = buildQuestion(
                    type = type,
                    movie = movie,
                    movies = candidates,
                    imagesByMovie = imagesByMovie,
                    castByMovie = castByMovie,
                    allCast = allCast,
                    usedImages = usedImages,
                    index = questions.size,
                )
                if (question != null) {
                    selectedType = type
                    break
                }
            }

            val nextQuestion = question ?: continue
            val nextType = selectedType ?: continue
            questions += nextQuestion
            typeCounts[nextType] = (typeCounts[nextType] ?: 0) + 1
            nextQuestion.imagePath?.let { usedImages += it }
        }

        if (questions.size != QuizRules.QuestionCount) {
            throw QuizException("Browse the catalog first to populate your quiz pool.")
        }

        return QuizSession(
            id = "quiz-${Random.nextInt()}",
            questions = questions,
        )
    }

    private fun typeOrder(typeCounts: Map<QuizQuestionType, Int>): List<QuizQuestionType> {
        val preferredTypes = QuizQuestionType.entries
            .filter { type -> (typeCounts[type] ?: 0) < QuizRules.MaxPerType }
            .shuffled()
        val fallbackTypes = QuizQuestionType.entries
            .filterNot { type -> type in preferredTypes }
            .shuffled()
        return preferredTypes + fallbackTypes
    }

    private fun buildQuestion(
        type: QuizQuestionType,
        movie: MovieEntity,
        movies: List<MovieEntity>,
        imagesByMovie: Map<String, List<MovieImageEntity>>,
        castByMovie: Map<String, List<CastMemberEntity>>,
        allCast: List<CastMemberEntity>,
        usedImages: Set<String>,
        index: Int,
    ): QuizQuestion? =
        when (type) {
            QuizQuestionType.GuessMovie -> buildGuessMovieQuestion(
                movie = movie,
                movies = movies,
                imagesByMovie = imagesByMovie,
                usedImages = usedImages,
                index = index,
            )
            QuizQuestionType.GuessYear -> buildGuessYearQuestion(
                movie = movie,
                imagesByMovie = imagesByMovie,
                usedImages = usedImages,
                index = index,
            )
            QuizQuestionType.GuessLeadActor -> buildGuessLeadActorQuestion(
                movie = movie,
                imagesByMovie = imagesByMovie,
                castByMovie = castByMovie,
                allCast = allCast,
                usedImages = usedImages,
                index = index,
            )
        }

    private fun buildGuessMovieQuestion(
        movie: MovieEntity,
        movies: List<MovieEntity>,
        imagesByMovie: Map<String, List<MovieImageEntity>>,
        usedImages: Set<String>,
        index: Int,
    ): QuizQuestion? {
        val wrongMovies = movies
            .filter { it.movieId != movie.movieId }
            .distinctBy { it.title }
            .shuffled()
            .take(3)
        if (wrongMovies.size < 3) return null

        val image = movie.imageOptions(imagesByMovie)
            .filterNot { it in usedImages }
            .randomOrNull() ?: return null

        return createQuestion(
            id = "q-$index",
            type = QuizQuestionType.GuessMovie,
            movie = movie,
            prompt = "Which movie is this?",
            image = image,
            correctText = movie.title,
            wrongTexts = wrongMovies.map { it.title },
        )
    }

    private fun buildGuessYearQuestion(
        movie: MovieEntity,
        imagesByMovie: Map<String, List<MovieImageEntity>>,
        usedImages: Set<String>,
        index: Int,
    ): QuizQuestion? {
        val year = movie.year ?: return null
        val image = movie.imageOptions(imagesByMovie)
            .filterNot { it in usedImages }
            .randomOrNull() ?: return null

        val wrongYears = (-10..10)
            .filter { it != 0 }
            .shuffled()
            .map { year + it }
            .filter { it > 1880 }
            .distinct()
            .take(3)
        if (wrongYears.size < 3) return null

        return createQuestion(
            id = "q-$index",
            type = QuizQuestionType.GuessYear,
            movie = movie,
            prompt = "What year was ${movie.title} released?",
            image = image,
            correctText = year.toString(),
            wrongTexts = wrongYears.map { it.toString() },
        )
    }

    private fun buildGuessLeadActorQuestion(
        movie: MovieEntity,
        imagesByMovie: Map<String, List<MovieImageEntity>>,
        castByMovie: Map<String, List<CastMemberEntity>>,
        allCast: List<CastMemberEntity>,
        usedImages: Set<String>,
        index: Int,
    ): QuizQuestion? {
        val movieCast = castByMovie[movie.movieId].orEmpty()
        val leadActors = movieCast
            .sortedWith(compareBy<CastMemberEntity> { it.orderIndex ?: Int.MAX_VALUE }.thenBy { it.name })
            .take(3)
            .filter { it.name.isNotBlank() }
        val correct = leadActors.randomOrNull() ?: return null

        val movieActorNames = movieCast.map { it.name }.toSet()
        val wrongNames = allCast
            .map { it.name }
            .filter { it.isNotBlank() && it !in movieActorNames }
            .distinct()
            .shuffled()
            .take(3)
        if (wrongNames.size < 3) return null

        val image = movie.imageOptions(imagesByMovie)
            .filterNot { it in usedImages }
            .randomOrNull() ?: return null

        return createQuestion(
            id = "q-$index",
            type = QuizQuestionType.GuessLeadActor,
            movie = movie,
            prompt = "Who stars in ${movie.title}?",
            image = image,
            correctText = correct.name,
            wrongTexts = wrongNames,
        )
    }

    private fun createQuestion(
        id: String,
        type: QuizQuestionType,
        movie: MovieEntity,
        prompt: String,
        image: String,
        correctText: String,
        wrongTexts: List<String>,
    ): QuizQuestion {
        val answerTexts = (wrongTexts + correctText)
            .distinct()
            .shuffled()
        val answers = answerTexts.mapIndexed { index, text ->
            QuizAnswer(
                id = "$id-a-$index",
                text = text,
                isCorrect = text == correctText,
            )
        }
        val correctAnswer = answers.first { it.isCorrect }

        return QuizQuestion(
            id = id,
            type = type,
            movieId = movie.movieId,
            prompt = prompt,
            imagePath = image,
            answers = answers,
            correctAnswerId = correctAnswer.id,
        )
    }

    private fun MovieEntity.imageOptions(imagesByMovie: Map<String, List<MovieImageEntity>>): List<String> =
        buildList {
            backdropPath?.takeIf { it.isNotBlank() }?.let(::add)
            posterPath?.takeIf { it.isNotBlank() }?.let(::add)
            imagesByMovie[movieId]
                .orEmpty()
                .filter { it.type != "logo" }
                .map { it.filePath }
                .filter { it.isNotBlank() }
                .forEach(::add)
        }.distinct()
}
