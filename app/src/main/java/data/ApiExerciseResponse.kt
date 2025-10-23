package data
import kotlinx.serialization.Serializable

@Serializable
data class ApiExerciseResponse(
    val muscles: List<Muscle>,
    val category: Category,
    val difficulty: Difficulty,
    val target_url: TargetUrl,
    val name: String
)

@Serializable
data class Muscle(
    val id: Int,
    val name: String,
    val name_en_us: String
)

@Serializable
data class Category(
    val id: Int,
    val name: String,
    val name_en_us: String
)

@Serializable
data class Difficulty(
    val id: Int,
    val name: String,
    val name_en_us: String
)

@Serializable
data class TargetUrl(
    val male: String,
    val female: String
)

