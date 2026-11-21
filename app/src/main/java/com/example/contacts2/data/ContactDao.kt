package com.example.contacts2.data

import android.content.ContentValues
import android.database.Cursor
import com.example.contacts2.Contact

class ContactDao(private val helper: ContactDbHelper) {

    private val order = "${ContactContract.COL_NOM} COLLATE NOCASE ASC, " +
            "${ContactContract.COL_PRENOM} COLLATE NOCASE ASC"

    private fun toValues(c: Contact) = ContentValues().apply {
        put(ContactContract.COL_NOM, c.nom)
        put(ContactContract.COL_PRENOM, c.prenom)
        put(ContactContract.COL_TEL, c.telephone)
        put(ContactContract.COL_EMAIL, c.email)
        put(ContactContract.COL_PHOTO, c.photoUri)
        put(ContactContract.COL_LAT, c.latitude)
        put(ContactContract.COL_LNG, c.longitude)
    }

    private fun Cursor.toContact(): Contact {
        val iLat = getColumnIndexOrThrow(ContactContract.COL_LAT)
        val iLng = getColumnIndexOrThrow(ContactContract.COL_LNG)
        val iMail = getColumnIndexOrThrow(ContactContract.COL_EMAIL)
        val iPhoto = getColumnIndexOrThrow(ContactContract.COL_PHOTO)
        return Contact(
            id = getLong(getColumnIndexOrThrow(ContactContract.COL_ID)),
            nom = getString(getColumnIndexOrThrow(ContactContract.COL_NOM)),
            prenom = getString(getColumnIndexOrThrow(ContactContract.COL_PRENOM)),
            telephone = getString(getColumnIndexOrThrow(ContactContract.COL_TEL)),
            email = if (isNull(iMail)) null else getString(iMail),
            photoUri = if (isNull(iPhoto)) null else getString(iPhoto),
            latitude = if (isNull(iLat)) null else getDouble(iLat),
            longitude = if (isNull(iLng)) null else getDouble(iLng)
        )
    }

    private fun Cursor.toList(): List<Contact> {
        val list = mutableListOf<Contact>()
        while (moveToNext()) list.add(toContact())
        return list
    }

    fun insert(c: Contact): Long =
        helper.writableDatabase.insert(ContactContract.TABLE, null, toValues(c))

    fun update(c: Contact): Int =
        helper.writableDatabase.update(
            ContactContract.TABLE, toValues(c),
            "${ContactContract.COL_ID} = ?", arrayOf(c.id.toString())
        )

    fun delete(id: Long): Int =
        helper.writableDatabase.delete(
            ContactContract.TABLE,
            "${ContactContract.COL_ID} = ?", arrayOf(id.toString())
        )

    fun getAll(): List<Contact> =
        helper.readableDatabase
            .query(ContactContract.TABLE, null, null, null, null, null, order)
            .use { it.toList() }

    fun getById(id: Long): Contact? =
        helper.readableDatabase
            .query(
                ContactContract.TABLE, null,
                "${ContactContract.COL_ID} = ?", arrayOf(id.toString()),
                null, null, null
            )
            .use { if (it.moveToFirst()) it.toContact() else null }

    fun search(texte: String): List<Contact> {
        val like = "%$texte%"
        val sel = "${ContactContract.COL_NOM} LIKE ? OR " +
                "${ContactContract.COL_PRENOM} LIKE ? OR " +
                "${ContactContract.COL_TEL} LIKE ?"
        return helper.readableDatabase
            .query(ContactContract.TABLE, null, sel, arrayOf(like, like, like), null, null, order)
            .use { it.toList() }
    }
}