interface T {
    <!CONFLICTING_JVM_DECLARATIONS!>val x: <!UNRESOLVED_REFERENCE!>ErrorType<!><!>
    <!CONFLICTING_JVM_DECLARATIONS!>fun getX(): <!UNRESOLVED_REFERENCE!>ErrorType1<!><!>
}