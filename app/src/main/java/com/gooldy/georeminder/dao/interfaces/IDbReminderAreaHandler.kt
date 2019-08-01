package com.gooldy.georeminder.dao.interfaces

import java.util.*

interface IDbReminderAreaHandler {

    fun getAllAreaIds(): Set<String>
    fun addRelation(aId: UUID, rId: UUID)
    fun getAreaIds(rId: UUID): Set<String>
    fun getRemindersByAreaId(aId: UUID): Set<String>
    fun deleteRelations(rId: UUID)
    fun deleteRelation(rId: UUID, aId: UUID)
}