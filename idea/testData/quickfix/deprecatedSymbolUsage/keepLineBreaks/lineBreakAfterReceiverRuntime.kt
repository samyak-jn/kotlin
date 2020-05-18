// "Replace with 'newFun()'" "true"
// WITH_RUNTIME

@Deprecated("", ReplaceWith("newFun()"))
fun Int.oldFun(): Int = this

fun Int.newFun(): Int = this

fun foo(list: List<String>): Int {
    return list
            .filter { it.isNotEmpty() }
            .map { it.length }
            .first()
            .<caret>oldFun()
}
