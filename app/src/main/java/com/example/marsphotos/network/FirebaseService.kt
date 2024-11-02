package com.example.marsphotos.network

import android.util.Log
import com.example.marsphotos.model.MarsPhoto
import com.example.marsphotos.model.PicsumPhoto
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

object FirebaseService {

    private val database: FirebaseDatabase = Firebase.database
    private val marsPhotosRef: DatabaseReference = database.getReference("marsPhotos")
    private val picsumPhotosRef: DatabaseReference = database.getReference("picsumPhotos")

    fun savePhotos(marsPhoto: MarsPhoto, picsumPhoto: PicsumPhoto) {
        addTimestamps(marsPhoto, picsumPhoto)
        marsPhotosRef.child(marsPhoto.id).setValue(marsPhoto)
        picsumPhotosRef.child(picsumPhoto.id).setValue(picsumPhoto)
    }

    fun addTimestamps(marsPhoto: MarsPhoto, picsumPhoto: PicsumPhoto){
        val savedAt = System.currentTimeMillis()
        marsPhoto.savedAt = savedAt
        picsumPhoto.savedAt = savedAt
    }

    fun getLastSavedPhotos(callback: (Pair<MarsPhoto?, PicsumPhoto?>) -> Unit) {
        var marsPhoto: MarsPhoto? = null
        var picsumPhoto: PicsumPhoto? = null

        marsPhotosRef.orderByChild("savedAt").limitToLast(1).addListenerForSingleValueEvent(
            object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (postSnapshot in snapshot.children) {
                        marsPhoto = postSnapshot.getValue(MarsPhoto::class.java)
                    }
                    if (marsPhoto != null && picsumPhoto != null) {
                        callback(Pair(marsPhoto, picsumPhoto))
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("FirebaseService", "Failed to read MarsPhoto", error.toException())
                    callback(Pair(null, null))
                }
            })

        picsumPhotosRef.orderByChild("savedAt").limitToLast(1)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (postSnapshot in snapshot.children) {
                        picsumPhoto = postSnapshot.getValue(PicsumPhoto::class.java)
                    }
                    if (marsPhoto != null && picsumPhoto != null) {
                        callback(Pair(marsPhoto, picsumPhoto))
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("FirebaseService", "Failed to read PicsumPhoto", error.toException())
                    callback(Pair(null, null))
                }
            })
    }
}