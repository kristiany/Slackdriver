import java.time.Duration
import java.time.Instant

class Seen(val cacheEvictionPeriod: Duration) : (Any) -> Boolean {
    private var seen = HashMap<Int, Instant>()

    override fun invoke(any: Any): Boolean {
        return !seen.keys.contains(any.hashCode())
    }

    fun update(anys: List<Any>) {
        removeExpired()

        val expiresAt = Instant.now().plus(cacheEvictionPeriod)
        anys.forEach { seen.put(it.hashCode(), expiresAt) }
    }

    private fun removeExpired() {
        val now = Instant.now()
        val expiredKeys = seen.entries.filter { it.value < now }.map { it.key }
        expiredKeys.forEach { seen.remove(it) }
    }
}