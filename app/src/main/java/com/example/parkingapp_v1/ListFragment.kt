package com.example.parkingapp_v1

import android.app.ActionBar
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.parkingapp_v1.databinding.FragmentListBinding


class ListFragment(application: LocationApplication) : Fragment(), ListAdapter.ClickListener {

    private lateinit var actionBar: ActionBar
    private lateinit var binding: FragmentListBinding
    private val locationViewModel: LocationViewModel by viewModels {
        LocationItemModelFactory((application).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentListBinding.inflate(inflater, container, false)

        return binding.root

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.action_bar_menu,menu)
        super.onCreateOptionsMenu(menu,inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.delete_list -> locationViewModel.removeAllLocationItem()

        }
        return true
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        locationViewModel.locationItems.observe(viewLifecycleOwner) {
            binding.recyclerView.apply {
                layoutManager = LinearLayoutManager(requireContext())
                adapter = ListAdapter(it, this@ListFragment)

                val itemSwap = object :
                    ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.ANIMATION_TYPE_SWIPE_CANCEL) {
                    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                        showDialog(viewHolder,adapter as ListAdapter)
                    }

                    override fun onMove(
                        recyclerView: RecyclerView,
                        viewHolder: RecyclerView.ViewHolder,
                        target: RecyclerView.ViewHolder
                    ): Boolean {
                        return false
                    }
                }
                val swap = ItemTouchHelper(itemSwap)
                swap.attachToRecyclerView(binding.recyclerView)
            }
        }
    }
    fun showDialog(viewHolder: RecyclerView.ViewHolder,adapter: ListAdapter){
        val builder = AlertDialog.Builder(activity)
        builder.setTitle("Delete Location")
        builder.setMessage("Are you sure want to delete item?")
        builder.setPositiveButton("Confirm"){dialog,which ->
            val position = viewHolder.adapterPosition
            locationViewModel.locationItems.apply {
                locationViewModel.removeLocationItem(this.value!![position])
            //   locationViewModel.removeAllLocationItem()
            }

            adapter.notifyItemChanged(position)

        }
        builder.setNegativeButton("Cancel"){dialog,which ->
            val position = viewHolder.adapterPosition
            adapter.notifyItemChanged(position)

        }
        builder.show()
    }

    override fun onItemClick(locationItem: Location) {
        Log.d("LocationID","${locationItem.id}")
        val uri = Uri.parse("geo:0, 0?q=${locationItem.coordinates}")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        intent.setPackage("com.google.android.apps.maps")
        startActivity(intent)
    }

}