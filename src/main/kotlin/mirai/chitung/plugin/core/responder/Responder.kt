package mirai.chitung.plugin.core.responder

interface Responder {
    fun receive(event: PreprocessedMessageEvent): Boolean
}

@Target(AnnotationTarget.CLASS)
annotation class ResponderAutoRegistry(
    val name: String,
    val from: RespondFrom = RespondFrom.Group,
    val priority: Priority = Priority.Higher
)

enum class RespondFrom(val typeBit: Byte) {
    Group(1),
    Friend(2),
    GroupAndFriend(3)
}

enum class Priority(val i: Int) {
    Lowest(0),
    Low(1),
    Normal(2),
    Common(3),
    Higher(4),
    Highest(5)
}