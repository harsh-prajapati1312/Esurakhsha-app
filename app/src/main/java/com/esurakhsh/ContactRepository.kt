package com.esurakhsh

import android.content.Context
import android.provider.ContactsContract
import android.util.Log

class ContactRepository(private val context: Context) {

    fun getAllContacts(): List<ContactData> {
        val contactList = mutableListOf<ContactData>()
        try {
            val uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI
            val projection = arrayOf(
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER
            )
            val sortOrder = "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} ASC"

            context.contentResolver.query(
                uri,
                projection,
                null,
                null,
                sortOrder
            )?.use { cursor ->
                val indexId = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
                val indexName = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                val indexNumber = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)

                while (cursor.moveToNext()) {
                    val id = if (indexId != -1) cursor.getString(indexId) else ""
                    val name = if (indexName != -1) cursor.getString(indexName) ?: "Unknown" else "Unknown"
                    val number = if (indexNumber != -1) cursor.getString(indexNumber) ?: "" else ""
                    
                    contactList.add(ContactData(id, name, number))
                }
            }
        } catch (e: SecurityException) {
            Log.e("ContactRepository", "Permission denied", e)
        } catch (e: Exception) {
            Log.e("ContactRepository", "Error reading contacts", e)
        }
        return contactList
    }
}
