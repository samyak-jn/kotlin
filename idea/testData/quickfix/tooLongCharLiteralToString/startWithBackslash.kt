// "Convert too long character literal to string" "true"
// ERROR: Illegal escape: '\ '

fun foo() {
    '\ bar<caret>'
}