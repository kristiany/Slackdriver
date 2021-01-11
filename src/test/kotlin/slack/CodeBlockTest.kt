package slack

import config.DisplayConfig
import org.assertj.core.api.Assertions.assertThat

import org.junit.jupiter.api.Test
import java.time.ZoneId

class CodeBlockTest {

    val testConfig = DisplayConfig(ZoneId.of("CET"), listOf("org.routing.", "com.somecode.", "org.bestcode."))
    val stackTrace = "java.lang.IllegalArgumentException: No sir!\n" +
            "   at com.mycode.MyClass.doStuff(MyClass.java:42)\n" +
            "   at org.routing.server.Router.route(Router.java:333)\n" +
            "   at org.routing.server.Router.route(Router.java:222)\n" +
            "   at org.routing.entry.EntryPoint.handle(EntryPoint.java:10)\n" +
            "Caused by: java.lang.NullPointerException:\n" +
            "   at com.mycode.MyOtherClass.wat(MyOtherClass.java:90)"

    @Test
    fun `No filter - Stack is not collapsed but marked`() {
        val stack = CodeBlock(stackTrace, DisplayConfig(ZoneId.of("CET"), listOf()))
        assertThat(stack.code["text"]).isEqualTo(
                "_java.lang.IllegalArgumentException: No sir!_\n" +
                        "_   at com.mycode.MyClass.doStuff(MyClass.java:42)_\n" +
                        "_   at org.routing.server.Router.route(Router.java:333)_\n" +
                        "_   at org.routing.server.Router.route(Router.java:222)_\n" +
                        "_   at org.routing.entry.EntryPoint.handle(EntryPoint.java:10)_\n" +
                        "_*Caused by:* java.lang.NullPointerException:_\n" +
                        "_   at com.mycode.MyOtherClass.wat(MyOtherClass.java:90)_")
    }

    @Test
    fun `Stack is collapsed and marked`() {
        val stack = CodeBlock(stackTrace, testConfig)
        assertThat(stack.code["text"]).isEqualTo(
                "_java.lang.IllegalArgumentException: No sir!_\n" +
                        "_   at com.mycode.MyClass.doStuff(MyClass.java:42)_\n" +
                        "_   at org.routing.* ..._\n" +
                        "_*Caused by:* java.lang.NullPointerException:_\n" +
                        "_   at com.mycode.MyOtherClass.wat(MyOtherClass.java:90)_")
    }

    @Test
    fun `Stack is collapsed and marked, without Caused by`() {
        val stackTrace = "java.lang.IllegalArgumentException: No sir!\n" +
                "   at com.mycode.MyClass.doStuff(MyClass.java:42)\n" +
                "   at org.routing.entry.EntryPoint.handle(EntryPoint.java:10)\n" +
                "   at org.routing.entry.EntryPoint.handle(EntryPoint.java:15)\n" +
                "   at com.mycode.MyOtherClass.wat(MyOtherClass.java:90)"
        val stack = CodeBlock(stackTrace, testConfig)
        assertThat(stack.code["text"]).isEqualTo(
                "_java.lang.IllegalArgumentException: No sir!_\n" +
                        "_   at com.mycode.MyClass.doStuff(MyClass.java:42)_\n" +
                        "_   at org.routing.* ..._\n" +
                        "_   at com.mycode.MyOtherClass.wat(MyOtherClass.java:90)_")
    }

    @Test
    fun `Don't collapse Caused by`() {
        val stackTrace = "java.lang.IllegalArgumentException: No sir!\n" +
                "   at com.mycode.MyClass.doStuff(MyClass.java:42)\n" +
                "   at org.routing.entry.EntryPoint.handle(EntryPoint.java:10)\n" +
                "Caused by: org.routing.errors.ShitHitTheFanException: nope\n" +
                "   at com.mycode.MyOtherClass.wat(MyOtherClass.java:90)"
        val stack = CodeBlock(stackTrace, testConfig)
        assertThat(stack.code["text"]).isEqualTo(
                "_java.lang.IllegalArgumentException: No sir!_\n" +
                        "_   at com.mycode.MyClass.doStuff(MyClass.java:42)_\n" +
                        "_   at org.routing.* ..._\n" +
                        "_*Caused by:* org.routing.errors.ShitHitTheFanException: nope_\n" +
                        "_   at com.mycode.MyOtherClass.wat(MyOtherClass.java:90)_")
    }

    @Test
    fun `Don't collapse multiple Caused bys`() {
        val stackTrace = "java.lang.IllegalArgumentException: No sir!\n" +
                "   at com.mycode.MyClass.doStuff(MyClass.java:42)\n" +
                "   at org.routing.entry.EntryPoint.handle(EntryPoint.java:10)\n" +
                "Caused by: org.routing.errors.ShitHitTheFanException: nope\n" +
                "   at com.mycode.MyOtherClass.wat(MyOtherClass.java:90)\n" +
                "   at com.mycode.MyOtherClass.maybe(MyOtherClass.java:78)\n" +
                "   at org.bestcode.StarClass.soso(StarClass.java:1)\n" +
                "   at org.bestcode.StarClass.nono(StarClass.java:5)\n" +
                "Caused by: com.somecode.pkg.BonkersException: nope\n" +
                "   at com.somecode.SomeOtherClass.wat(SomeOtherClass.java:33)\n" +
                "Caused by: org.bestcode.doh.DohException: nah\n" +
                "   at org.bestcode.WeirdClass.wat(WeirdClass.java:666)\n" +
                "   at org.bestcode.WeirdClass.trythis(WeirdClass.java:1)"
        val stack = CodeBlock(stackTrace, testConfig)
        assertThat(stack.code["text"]).isEqualTo(
                "_java.lang.IllegalArgumentException: No sir!_\n" +
                        "_   at com.mycode.MyClass.doStuff(MyClass.java:42)_\n" +
                        "_   at org.routing.* ..._\n" +
                        "_*Caused by:* org.routing.errors.ShitHitTheFanException: nope_\n" +
                        "_   at com.mycode.MyOtherClass.wat(MyOtherClass.java:90)_\n" +
                        "_   at com.mycode.MyOtherClass.maybe(MyOtherClass.java:78)_\n" +
                        "_   at org.bestcode.* ..._\n" +
                        "_*Caused by:* com.somecode.pkg.BonkersException: nope_\n" +
                        "_   at com.somecode.* ..._\n" +
                        "_*Caused by:* org.bestcode.doh.DohException: nah_\n" +
                        "_   at org.bestcode.* ..._")
    }
}