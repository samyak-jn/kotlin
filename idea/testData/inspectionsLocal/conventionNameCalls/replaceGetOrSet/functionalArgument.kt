fun test() {
    class Test{
        operator fun get(fn: (i: Int) -> Int) : Int = 0
    }
    val test = Test()
    test.g<caret>et() { i ->
        i
    }
}
