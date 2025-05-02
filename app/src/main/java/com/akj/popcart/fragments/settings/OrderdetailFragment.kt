package com.akj.popcart.fragments.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.akj.popcart.adapters.BillingAdapter
import com.akj.popcart.data.order.OrderStatus
import com.akj.popcart.data.order.getOrderStatus
import com.akj.popcart.databinding.FragmentOrderDetailBinding
import com.akj.popcart.util.VerticalItemDecoration

class OrderdetailFragment :Fragment() {

    private lateinit var binding: FragmentOrderDetailBinding
    private  val billingAdapter by lazy { BillingAdapter() }
    private val args by navArgs<OrderdetailFragmentArgs>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentOrderDetailBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val order=args.order

        setupOrder()

        binding.apply {

            tvOrderId.text="Order#${order.orderId}"
            stepView.setSteps(
                mutableListOf(
                    OrderStatus.Ordered.status,
                    OrderStatus.Confirmed.status,
                    OrderStatus.Shipped.status,
                    OrderStatus.Delivered.status,

                )
            )

            val currentorderState=when(getOrderStatus(order.orderStatus)){
                is OrderStatus.Ordered -> 0
                is OrderStatus.Confirmed -> 1
                is OrderStatus.Shipped -> 2
                is OrderStatus.Delivered -> 3
                else -> 0
            }

            stepView.go(currentorderState,false)
            if(currentorderState==3) {
                stepView.done(true)
            }
            tvFullName.text=order.address.fName
            tvAddress.text="${order.address.street} ${order.address.city}"
            tvPhoneNumber.text=order.address.phone
            tvTotalPrice.text="₹ ${order.totalPrice}"
        }

        billingAdapter.differ.submitList(order.products)

    }

    private fun setupOrder() {
        binding.rvProducts.apply {
            adapter=billingAdapter
            layoutManager=LinearLayoutManager(requireContext(),RecyclerView.VERTICAL,false)
            addItemDecoration((VerticalItemDecoration()))
        }
    }

}