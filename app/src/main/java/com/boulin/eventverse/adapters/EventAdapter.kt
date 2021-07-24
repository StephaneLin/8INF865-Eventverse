package com.boulin.eventverse.adapters

import android.annotation.SuppressLint
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.boulin.eventverse.R
import com.boulin.eventverse.data.model.Event
import com.boulin.eventverse.ui.activities.EventDetailsActivity
import com.squareup.picasso.Picasso

class EventAdapter (private val showActions: Boolean = false, private var dataset: List<Event>? = null) : RecyclerView.Adapter<EventAdapter.EventViewHolder>() {

    class EventViewHolder (view: View) : RecyclerView.ViewHolder(view) {
        val coverImage: ImageView = view.findViewById(R.id.event_item_cover)
        val titleTv: TextView = view.findViewById(R.id.event_item_title)
        val datesTv: TextView = view.findViewById(R.id.event_item_dates)
        val locationTv: TextView = view.findViewById(R.id.event_item_location)
        val overviewLayout: LinearLayout = view.findViewById(R.id.event_item_overview)
        val editButton: ImageView = view.findViewById(R.id.event_item_edit_button)
        val deleteButton: ImageView = view.findViewById(R.id.event_item_delete_button)
        val actionsLayout: LinearLayout = view.findViewById(R.id.event_item_actions_layout)
    }

    var onEditButtonClick: ((Event) -> Unit)? = null
    var onDeleteButtonClick: ((Event) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.event_item_layout, parent, false)

        return EventViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        if(dataset == null) return
        val event = dataset!![position]

        if(event.cover != "") {
            Picasso.get().load(event.cover).into(holder.coverImage)
            holder.coverImage.scaleType = ImageView.ScaleType.CENTER_CROP
        }
        holder.titleTv.text = event.title
        holder.datesTv.text = event.getFormattedDates()

        if(event.distance != null) {
            holder.locationTv.text = "${event.location.name} - ${"%.2f".format(event.distance!!)} km"
        }
        else {
            holder.locationTv.text = event.location.name
        }

        if(!showActions) {
            holder.actionsLayout.visibility = View.GONE
        }

        // add listeners for buttons

        holder.coverImage.setOnClickListener { view ->
            redirectToEventDetails(view, event.id)
        }

        holder.overviewLayout.setOnClickListener { view ->
            redirectToEventDetails(view, event.id)
        }

        holder.editButton.setOnClickListener {
            onEditButtonClick?.let { listener -> listener(event) }
        }

        holder.deleteButton.setOnClickListener {
            onDeleteButtonClick?.let { listener -> listener(event) }
        }
    }

    override fun getItemCount(): Int {
        if(dataset == null) return 0
        return dataset!!.size
    }

    fun updateData(data: List<Event>) {
        dataset = data
        notifyDataSetChanged()
    }

    private fun redirectToEventDetails(view: View, eventId: String) {
        val context = view.context
        val intent = Intent(context, EventDetailsActivity::class.java).apply {
            putExtra(EventDetailsActivity.EXTRA_EVENT_ID, eventId)
        }
        context.startActivity(intent)
    }
}