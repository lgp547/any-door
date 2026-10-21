package io.github.lgp547.anydoor.attach.fixture

data class IsolatedPayload(val value: String, val suffix: String = "!")

object IsolatedKotlinFixture {
    fun echo(value: String): String = value

    fun payload(payload: IsolatedPayload): String = payload.value + payload.suffix
}
