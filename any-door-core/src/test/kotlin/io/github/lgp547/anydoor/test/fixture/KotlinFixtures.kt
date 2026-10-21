package io.github.lgp547.anydoor.test.fixture

data class KotlinPayload(
    val name: String,
    val age: Int,
    val tags: List<String>,
    val nickname: String?
)

object KotlinObjectFixture {
    fun identity(): Int = System.identityHashCode(this)

    fun nullableValue(value: String?): String = value ?: "null"

    fun requiredValue(value: String): String = value

    fun collidingNames(args1: String, args0: String): String = "$args1|$args0"

    fun payloadValue(payload: KotlinPayload): String = "${payload.name}:${payload.age}:${payload.tags.size}:${payload.nickname}"
}

class KotlinStaticOnly private constructor(private val value: String) {
    companion object {
        @JvmStatic
        fun staticEcho(value: String): String = value

        fun companionEcho(value: String): String = value
    }
}

class KotlinTypeFixture {
    class NestedType(val value: String)

    fun overloaded(value: IntArray): String = "int-array:${value.size}"

    fun overloaded(value: Array<String>): String = "string-array:${value.size}"

    fun nested(value: NestedType): String = value.value
}

class KotlinPrivateMethodFixture {
    private fun privateEcho(value: String): String = "private:$value"
}

open class KotlinSpringFixture {
    private val invocationCount = java.util.concurrent.atomic.AtomicInteger()

    private fun privateEcho(value: String): String = "spring:$value:${invocationCount.incrementAndGet()}"

    open fun invocationCount(): Int = invocationCount.get()
}

object KotlinScheduleFixture {
    private val invocationCount = java.util.concurrent.atomic.AtomicInteger()
    @Volatile
    private var completionLatch = java.util.concurrent.CountDownLatch(0)

    fun reset(expectedInvocations: Int) {
        invocationCount.set(0)
        completionLatch = java.util.concurrent.CountDownLatch(expectedInvocations)
    }

    fun record(value: String): String = "$value:${invocationCount.incrementAndGet()}".also {
        completionLatch.countDown()
    }

    fun invocationCount(): Int = invocationCount.get()

    fun awaitInvocations(timeout: Long, unit: java.util.concurrent.TimeUnit): Boolean = completionLatch.await(timeout, unit)
}
