package team.themoment.datagsm.domain.student.entity.constant

import jakarta.persistence.Column
import jakarta.persistence.Embeddable

@Embeddable
class DormitoryRoomNumber {
    @field:Column(name = "room_number", nullable = false)
    var roomNumber: Int = 0

    constructor()

    constructor(roomNumber: Int) {
        this.roomNumber = roomNumber
    }

    val floor: Int
        get() = roomNumber / 100
}
