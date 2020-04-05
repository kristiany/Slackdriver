import java.time.Duration
import java.time.temporal.ChronoUnit
import kotlin.concurrent.timer

fun main() {
    val checker = ErrorChecker(Config())
    val period = Duration.of(5L, ChronoUnit.MINUTES).toMillis()
    timer("Error checker",false, 0, period) {
        checker.run()
    }
}
