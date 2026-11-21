package com.example.contacts2.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class ContactDbHelper private constructor(ctx: Context)
    : SQLiteOpenHelper(ctx, DB_NAME, null, DB_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE ${ContactContract.TABLE} (
                ${ContactContract.COL_ID} INTEGER PRIMARY KEY AUTOINCREMENT,
                ${ContactContract.COL_NOM} TEXT NOT NULL,
                ${ContactContract.COL_PRENOM} TEXT NOT NULL,
                ${ContactContract.COL_TEL} TEXT NOT NULL,
                ${ContactContract.COL_EMAIL} TEXT,
                ${ContactContract.COL_PHOTO} TEXT,
                ${ContactContract.COL_LAT} REAL,
                ${ContactContract.COL_LNG} REAL
            )
        """.trimIndent())
        seed(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS ${ContactContract.TABLE}")
        onCreate(db)
    }

    private fun seed(db: SQLiteDatabase) {
        fun add(nom: String, prenom: String, tel: String, mail: String?, lat: Double?, lng: Double?) {
            val v = ContentValues().apply {
                put(ContactContract.COL_NOM, nom)
                put(ContactContract.COL_PRENOM, prenom)
                put(ContactContract.COL_TEL, tel)
                put(ContactContract.COL_EMAIL, mail)
                put(ContactContract.COL_LAT, lat)
                put(ContactContract.COL_LNG, lng)
            }
            db.insert(ContactContract.TABLE, null, v)
        }
        add("Ben Ali", "Ahmed", "20111222", "ahmed@mail.com", 33.8815, 10.0982)
        add("Trabelsi", "Sana", "50333444", "sana@mail.com", 36.8065, 10.1815)
        add("Gharbi", "Omar", "98555666", "omar@mail.com", null, null)     // sans GPS
        add("Jlassi", "Mariem", "22777888", "mariem@mail.com", 35.8256, 10.6084)
        add("Hammami", "Yassine", "55123456", null, null, null)            // sans GPS
    }

    companion object {
        private const val DB_NAME = "contacts.db"
        private const val DB_VERSION = 1

        @Volatile private var instance: ContactDbHelper? = null

        fun getInstance(ctx: Context): ContactDbHelper =
            instance ?: synchronized(this) {
                instance ?: ContactDbHelper(ctx.applicationContext).also { instance = it }
            }
    }
}