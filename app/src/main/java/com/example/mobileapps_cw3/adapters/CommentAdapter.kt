package com.example.mobileapps_cw3.adapters

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.mobileapps_cw3.R
import com.example.mobileapps_cw3.fragments.ViewingProfileFragment
import com.example.mobileapps_cw3.structures.Comment
import com.example.mobileapps_cw3.structures.Profile
import com.example.mobileapps_cw3.structures.SystemData
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.File

class CommentAdapter(var comments: List<Comment>) :
    RecyclerView.Adapter<CommentAdapter.CommentViewHolder>() {

    class CommentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.ui_comment, parent, false)
        return CommentViewHolder(view)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val comment = comments[position]

        comment.getUserProfile {
            holder.itemView.findViewById<TextView>(R.id.commentName).text = it?.getUsername()

            val image = it?.getImage()
            if (image != "" && image != null) {
                val storageRef = FirebaseStorage.getInstance().getReference(image)
                val localFile = File.createTempFile("images", "jpg")

                storageRef.getFile(localFile).addOnSuccessListener {
                    Glide.with(holder.itemView.context).load(localFile)
                        .into(holder.itemView.findViewById(R.id.commentPicture))
                }
            }

            if (it != null) {
                holder.itemView.findViewById<ImageView>(R.id.manageComment)
                    .setOnClickListener { doc ->
                        if (it.getId() == SystemData.getLoggedIn()?.getId()) {
                            showDelete(holder.itemView, comment)
                        } else {
                            showProfile(holder.itemView, it, comment)
                        }
                    }
            }
        }


        holder.itemView.findViewById<TextView>(R.id.commentDate).text = comment.getCommentDate()
        holder.itemView.findViewById<TextView>(R.id.commentDescription).text = comment.getComment()
    }

    private fun showDelete(itemView: View, comment: Comment) {
        val popupMenu = PopupMenu(itemView.context, itemView)
        popupMenu.inflate(R.menu.pop_up_delete)

        popupMenu.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.menu_delete -> {
                    val commentCollection = FirebaseFirestore.getInstance().collection("comments")
                    commentCollection.document(comment.getId().toString()).delete()
                        .addOnSuccessListener {
                            Toast.makeText(itemView.context, "Comment deleted successfully!",
                                Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener {
                            Toast.makeText(itemView.context, "Error deleting comment!",
                                Toast.LENGTH_SHORT).show()
                        }
                    true
                }
                else -> false
            }
        }

        popupMenu.show()
    }

    private fun showProfile(itemView: View, profile: Profile, comment: Comment) {
        val popupMenu = PopupMenu(itemView.context, itemView)
        popupMenu.inflate(R.menu.pop_up_menu_noauth)

        popupMenu.setOnMenuItemClickListener { item: MenuItem ->
            when (item.itemId) {
                R.id.menu_navigate -> {
                    comment.getRestaurantProfile {
                        val fragmentManager =
                            (itemView.context as AppCompatActivity).supportFragmentManager
                        val fragment = ViewingProfileFragment().apply {
                            arguments = Bundle().apply {
                                putSerializable("lookedAtProfile", profile)
                                putSerializable("lookedAtRestaurant", it)
                            }
                        }
                        fragmentManager.beginTransaction()
                            .replace(R.id.frame_layout, fragment)
                            .addToBackStack(null) // Optional: Add the transaction to the back stack
                            .commit()
                    }
                    true
                }

                else -> false
            }
        }

        popupMenu.show()
    }

    override fun getItemCount(): Int {
        return comments.size
    }

}
