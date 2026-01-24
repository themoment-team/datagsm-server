package team.themoment.datagsm.common.domain.student.entity

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
