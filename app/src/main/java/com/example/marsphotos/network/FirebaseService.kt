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
        marsPhotosRef.child(marsPhoto.id).setValue(marsPhoto)
        picsumPhotosRef.child(picsumPhoto.id).setValue(picsumPhoto)

    }




}