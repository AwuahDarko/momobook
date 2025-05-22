package com.presetschool.momobookapp.service

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.presetschool.momobookapp.model.LocalItem

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "momobookapp_db"
        private const val DATABASE_VERSION = 1

        // Table Name
        private const val TABLE_ITEMS = "sent_messages"

        // Table Columns
        private const val COLUMN_ID = "id"
        private const val COLUMN_MESSAGE_ID = "_id"
        private const val COLUMN_BODY = "message"
        private const val COLUMN_MREF = "mref"
    }

    override fun onCreate(db: SQLiteDatabase) {
        // Create Table
        val createTableQuery = "CREATE TABLE $TABLE_ITEMS (" +
                "$COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$COLUMN_MESSAGE_ID TEXT, " +
                "$COLUMN_MREF TEXT, " +
                "$COLUMN_BODY TEXT)"
        db.execSQL(createTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Drop older table if exists and create fresh
        db.execSQL("DROP TABLE IF EXISTS $TABLE_ITEMS")
        onCreate(db)
    }

    // CRUD Operations

    // Create - Add a new item
    fun addItem(item: LocalItem): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            // Don't need to put ID as it's auto-increment
            put(COLUMN_MESSAGE_ID, item._id)
            put(COLUMN_BODY, item.body)
            put(COLUMN_MREF, item.mref)
        }

        // Insert row and get inserted ID
        val id = db.insert(TABLE_ITEMS, null, values)
        db.close()
        return id
    }

    // Read - Get all items
    fun getAllItems(): ArrayList<LocalItem> {
        val itemsList = ArrayList<LocalItem>()
        val selectQuery = "SELECT * FROM $TABLE_ITEMS"
        val db = this.readableDatabase
        val cursor = db.rawQuery(selectQuery, null)

        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MESSAGE_ID))
                val mref = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MREF))
                val body = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BODY))

                val item = LocalItem(id, mref, body)
                itemsList.add(item)
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()
        return itemsList
    }

    // Read - Get a single item
    fun getItem(id: Int): LocalItem? {
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_ITEMS,
            arrayOf(COLUMN_MREF, COLUMN_MESSAGE_ID, COLUMN_BODY),
            "$COLUMN_MESSAGE_ID = ?",
            arrayOf(id.toString()),
            null, null, null
        )

        return if (cursor.moveToFirst()) {
            val _id = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MESSAGE_ID))
            val mref = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MREF))
            val body = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BODY))

            cursor.close()
            db.close()
            LocalItem(_id, mref, body)
        } else {
            cursor.close()
            db.close()
            null
        }
    }

    // Update - Update an item
    fun updateItem(item: LocalItem): Int {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_MREF, item.mref)
            put(COLUMN_MESSAGE_ID, item._id)
            put(COLUMN_BODY, item.body)
        }

        // Update row and return number of rows affected
        val result = db.update(
            TABLE_ITEMS,
            values,
            "$COLUMN_MESSAGE_ID = ?",
            arrayOf(item._id)
        )
        db.close()
        return result
    }

    // Delete - Delete an item
    fun deleteItem(id: Int): Int {
        val db = this.writableDatabase
        val result = db.delete(
            TABLE_ITEMS,
            "$COLUMN_ID = ?",
            arrayOf(id.toString())
        )
        db.close()
        return result
    }

    // Delete - Delete all items
    fun deleteAllItems(): Int {
        val db = this.writableDatabase
        val result = db.delete(TABLE_ITEMS, null, null)
        db.close()
        return result
    }

    // Get count of items
    fun getItemsCount(): Int {
        val countQuery = "SELECT * FROM $TABLE_ITEMS"
        val db = this.readableDatabase
        val cursor = db.rawQuery(countQuery, null)
        val count = cursor.count
        cursor.close()
        db.close()
        return count
    }
}