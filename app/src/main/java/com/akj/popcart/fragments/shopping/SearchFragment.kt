package com.akj.popcart.fragments.shopping

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.akj.popcart.R
import com.akj.popcart.adapters.ProductAdapter
import com.akj.popcart.data.Products
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SearchFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var productAdapter: ProductAdapter

    private var productList: MutableList<Products> = mutableListOf()
    private lateinit var progressBar: ProgressBar
    private lateinit var emptyView: TextView
    private lateinit var db: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view: View = inflater.inflate(R.layout.search_fragment, container, false)

        recyclerView = view.findViewById(R.id.recycler_view)
        progressBar = view.findViewById(R.id.progress_bar)
        emptyView = view.findViewById(R.id.empty_view)
        val searchView: SearchView = view.findViewById(R.id.search_view)
        searchView.isIconified = false

        productAdapter = ProductAdapter(productList, requireContext()) { selectedProduct ->
            openProductDetail(selectedProduct)
        }
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = productAdapter

        db = FirebaseFirestore.getInstance()

        // Handle search query
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                searchProducts(query.trim())
                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                if (newText.trim().isEmpty()) {
                    productList.clear()
                    productAdapter.notifyDataSetChanged()
                    emptyView.visibility = View.VISIBLE
                    recyclerView.visibility = View.GONE
                } else {
                    recyclerView.visibility = View.VISIBLE
                    emptyView.visibility = View.GONE
                }
                return false
            }
        })

        return view
    }

    private fun searchProducts(query: String) {
        if (query.isEmpty()) return

        progressBar.visibility = View.VISIBLE
        db.collection("Products")
            .get()
            .addOnCompleteListener { task ->
                progressBar.visibility = View.GONE
                if (task.isSuccessful && task.result != null) {
                    productList.clear()
                    val searchQuery = query.lowercase() // Convert query to lowercase for case-insensitive matching
                    for (document in task.result!!) {
                        val product = document.toObject(Products::class.java)
                        if (product.name!!.lowercase().contains(searchQuery)) {
                            productList.add(product)
                        }
                    }
                    productAdapter.notifyDataSetChanged()
                    emptyView.visibility = if (productList.isEmpty()) View.VISIBLE else View.GONE
                } else {
                    Toast.makeText(requireContext(), "Error fetching products", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun openProductDetail(product: Products) {

        val action = SearchFragmentDirections.actionSearchFragmentToProductDetailFragment(product)
        findNavController().navigate(action)
    }

    // Example conversion function

}
