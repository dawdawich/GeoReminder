package com.gooldy.georeminder.data

import java.time.LocalDateTime
import java.util.*

data class Reminder(val id: UUID, var reminderName: String, var reminderText: String, var reminderAreas: Set<Area>,
                    var createDate: LocalDateTime?, var modifyTime: LocalDateTime?)