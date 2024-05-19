package com.example.azalea.activities

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.azalea.R
import com.example.azalea.adapters.NotificationAdapter
import com.example.azalea.data.User
import com.example.azalea.models.Notification
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class NotificationActivity : AppCompatActivity() {
    private lateinit var notificationsRecyclerView: RecyclerView
    private lateinit var notifications: MutableList<Notification>
    private lateinit var adapter: NotificationAdapter

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notification)

        notificationsRecyclerView = findViewById(R.id.notificationsRecyclerView)
        notificationsRecyclerView.layoutManager = LinearLayoutManager(this)
        notifications = mutableListOf()
        adapter = NotificationAdapter(notifications)
        notificationsRecyclerView.adapter = adapter

        val uid = FirebaseAuth.getInstance().currentUser?.uid!!
        val userRef = FirebaseDatabase.getInstance().getReference("Users/$uid")
        val handler= Handler(Looper.getMainLooper())
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val user = dataSnapshot.getValue(User::class.java)
                if (user != null) {
                    for (senderId in user.emergencyContactFor) {
                        Log.d("NotificationActivity", "SenderId: $senderId")

                        val databaseRef =
                            FirebaseDatabase.getInstance().getReference("Users/$senderId")
                        addNotificationListener(databaseRef, senderId)
                    }
                }

                //val runnable=Runnable {
                //    clearNotifications()
                //}

                //handler.postDelayed(runnable, 600000)
            }



            override fun onCancelled(databaseError: DatabaseError) {
                // Manejo de errores
            }
        })
    }

    private fun addNotificationListener(databaseRef: DatabaseReference, senderId: String) {
        val userRef = FirebaseDatabase.getInstance().getReference("Users/$senderId")
        databaseRef.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(dataSnapshot: DataSnapshot, prevChildKey: String?) {
                userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        val user = dataSnapshot.getValue(User::class.java)
                        if (user != null) {
                            if (user.emergency && !user.emergencySent) {
                                val newNotification = Notification(
                                    senderId = senderId,
                                    title = "Alerta del usuario: ${user.name}",
                                    content = "${user.emergencyMessage}"
                                )

                                // Verificar si la notificación ya existe
                                val notificationExists = notifications.any { it.senderId == newNotification.senderId }

                                if (!notificationExists) {
                                    // Si la notificación no existe, agregarla a la lista y enviarla
                                    val userRef = FirebaseDatabase.getInstance().getReference("Users").child(senderId)
                                    userRef.child("notifications").push().setValue(newNotification)
                                    notifications.add(newNotification)
                                    adapter.notifyDataSetChanged()
                                }

                                // Marcar la notificación de emergencia como enviada
                               // user.emergencySent = true
                                userRef.setValue(user)
                            } else if (!user.emergency) {
                                val cancelNotification = Notification(
                                    senderId = senderId,
                                    title = "Cancelación de alerta del usuario: ${user.name}",
                                    content = "${user.cancelMessage}"
                                )

                                // Verificar si la notificación de cancelación ya existe
                                val notificationExists = notifications.any { it.senderId == cancelNotification.senderId }

                                if (!notificationExists) {
                                    // Si la notificación de cancelación no existe, agregarla a la lista y enviarla
                                    val userRef = FirebaseDatabase.getInstance().getReference("Users").child(senderId)
                                    userRef.child("notifications").push().setValue(cancelNotification)
                                    notifications.add(cancelNotification)
                                    adapter.notifyDataSetChanged()
                                }
                            }
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        // Manejo de errores
                    }
                })
            }


            override fun onChildChanged(dataSnapshot: DataSnapshot, prevChildKey: String?) {}
            override fun onChildRemoved(dataSnapshot: DataSnapshot) {}
            override fun onChildMoved(dataSnapshot: DataSnapshot, prevChildKey: String?) {}
            override fun onCancelled(databaseError: DatabaseError) {}
        })

    }

        private fun clearNotifications() {
            val uid = FirebaseAuth.getInstance().currentUser?.uid!!
            val userRef =
                FirebaseDatabase.getInstance().getReference("Users/$uid/notifications")

            // Borrar todas las notificaciones en Firebase
            userRef.removeValue().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Borrar todas las notificaciones de la lista local
                    notifications.clear()

                    // Notificar al adaptador que los datos han cambiado
                    adapter.notifyDataSetChanged()
                } else {
                    // Manejo de errores
                    Log.e(
                        "NotificationActivity",
                        "Error al borrar las notificaciones",
                        task.exception
                    )
                }
            }
    }
}