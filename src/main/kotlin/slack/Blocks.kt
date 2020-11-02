package slack

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
class Blocks(vararg val blocks: SectionBlock) {
    private companion object {
        val json = Json { }
    }

    fun asJson(): String {
        return json.encodeToString(serializer(), this)
    }
}