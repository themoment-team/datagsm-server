package team.themoment.datagsm.domain.student.entity.constant

import jakarta.persistence.Column
import jakarta.persistence.Embeddable

@Embeddable
class DormitoryRoomNumber {
    @field:Column(name = "room_number", nullable = true)
    var dormitoryRoomNumber: Int? = null

    constructor()

    constructor(roomNumber: Int?) {
        this.dormitoryRoomNumber = roomNumber
    }

    val dormitoryRoomFloor: Int?
        get() = dormitoryRoomNumber?.div(100)
}
