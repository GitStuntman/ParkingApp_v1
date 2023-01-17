package com.example.parkingapp_v1

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.parkingapp_v1.databinding.CardviewLayoutBinding
import java.io.IOException
import java.io.InputStream
import java.net.URL

class ListAdapter(private val listItems: List<Location>, val clickListener: ClickListener): RecyclerView.Adapter<ListAdapter.ListViewHolder>() {


    inner class ListViewHolder(private val context: Context, private val binding: CardviewLayoutBinding)
        :RecyclerView.ViewHolder(binding.root){

            fun bindLocationItem(locationItem: Location){
                binding.cardLocation.text = locationItem.location
                binding.cardDate.text = locationItem.date
                SetMap(locationItem.mapImage,binding.cardMap).execute()
                binding.cardTime.text = locationItem.time
            }
    }

    inner class SetMap(private val url: String, private val cardMap: ImageView) :
        AsyncTask<Void, Void, Bitmap>(){
        override fun doInBackground(vararg params: Void?): Bitmap? {
            var bitMap :Bitmap? = null
            try {
                val mapUrl = URL(url)
                val stream = mapUrl.openConnection().content as InputStream
                bitMap = BitmapFactory.decodeStream(stream)
            }catch (e: IOException){
                e.printStackTrace()
            }
            return bitMap
        }
        override fun onPostExecute(result: Bitmap?) {
            super.onPostExecute(result)
            cardMap.setImageBitmap(result)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder {
        val from = LayoutInflater.from(parent.context)
        val binding = CardviewLayoutBinding.inflate(from,parent,false)
        return ListViewHolder(parent.context, binding)
    }

    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        holder.bindLocationItem(listItems[position])
        holder.itemView.setOnClickListener {
            clickListener.onItemClick(listItems[position])
        }
    }

    override fun getItemCount(): Int = listItems.size

    interface ClickListener{
        fun onItemClick(locationItem: Location)
    }
}