package team.themoment.datagsm.domain.student.entity.constant

enum class Major(val value: String) {
    SW_DEVELOPMENT("SW개발과"),
    SMART_IOT("스마트IoT과"),
    AI("인공지능과");

    companion object {
        fun fromClassNum(classNum: Int): Major? =
            when (classNum) {
                1, 2 -> SW_DEVELOPMENT
                3 -> SMART_IOT
                4 -> AI
                else -> null
            }

        fun fromMajor(major: String): Major? =
            entries.find { it.value == major }
    }
}
