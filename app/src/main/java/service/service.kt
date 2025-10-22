package service

import data.Exercise
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

class ExerciseService(private val client: HttpClient) {

    companion object {
        private const val BASE_URL = "https://api.api-ninjas.com/v1"
        private const val API_KEY = "vXO3ReejVqbz3uprfPrG2w==2eIo65wejTCi1UuD"
    }

    suspend fun getAllExercises(): List<Exercise> {
        return try {
            client.get("$BASE_URL/exercises") {
                header("X-Api-Key", API_KEY)
                header(HttpHeaders.Accept, "application/json")
            }.body()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getExercisesByMuscle(muscle: String): List<Exercise> {
        return try {
            client.get("$BASE_URL/exercises") {
                header("X-Api-Key", API_KEY)
                header(HttpHeaders.Accept, "application/json")
                parameter("muscle", muscle)
            }.body()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getExercisesByType(type: String): List<Exercise> {
        return try {
            client.get("$BASE_URL/exercises") {
                header("X-Api-Key", API_KEY)
                header(HttpHeaders.Accept, "application/json")
                parameter("type", type)
            }.body()
        } catch (e: Exception) {
            emptyList()
        }
    }
}