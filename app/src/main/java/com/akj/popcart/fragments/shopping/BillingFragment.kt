package com.akj.popcart.fragments.shopping

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.akj.popcart.R
import com.akj.popcart.activities.OnlineOrder
import com.akj.popcart.adapters.AddressAdapter
import com.akj.popcart.adapters.BillingAdapter
import com.akj.popcart.data.Address
import com.akj.popcart.data.CartProd
import com.akj.popcart.data.order.Order
import com.akj.popcart.data.order.OrderStatus
import com.akj.popcart.databinding.FragmentBillingBinding
import com.akj.popcart.util.Resource
import com.akj.popcart.viewmodel.BillingViewModel
import com.akj.popcart.viewmodel.OrderViewModel
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.google.android.material.snackbar.Snackbar
import com.paypal.android.corepayments.CoreConfig
import com.paypal.android.corepayments.Environment
import com.paypal.android.corepayments.PayPalSDKError
import com.paypal.android.paypalwebpayments.PayPalWebCheckoutClient
import com.paypal.android.paypalwebpayments.PayPalWebCheckoutFundingSource
import com.paypal.android.paypalwebpayments.PayPalWebCheckoutListener
import com.paypal.android.paypalwebpayments.PayPalWebCheckoutRequest
import com.paypal.android.paypalwebpayments.PayPalWebCheckoutResult
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID


@AndroidEntryPoint
class BillingFragment : Fragment() {

    private lateinit var binding: FragmentBillingBinding
    private val addressAdapter by lazy { AddressAdapter() }
    private val billingAdapter by lazy { BillingAdapter() }
    private val billingViewModel by viewModels<BillingViewModel>()
    private val orderViewModel by viewModels<OrderViewModel>()
    private val args by navArgs<BillingFragmentArgs>()
    private var products = emptyList<CartProd>()
    private var totalPrice = 0f
    private var selectedAddress: Address? = null
    private var selectedPaymentMethod: String? = null

//    1.
//    private val clientID= "AWKYIBhYkTUDj-NJPxmgEfNLk4UEQ5z6VZ8LPGPfnLH5z2a7pIjgeJMhwiWXSPpBsJiQzqYlGbRZAsXf"
//    private val secretID= "EMG45Ricd253pHZw9NHnJiihDwD-5x7fobcSotuRzD1XgfKUDKpKP9TusgN-1OXRcyxv8KwAx31J8WjQ"

    private val clientID= "AWKYIBhYkTUDj-NJPxmgEfNLk4UEQ5z6VZ8LPGPfnLH5z2a7pIjgeJMhwiWXSPpBsJiQzqYlGbRZAsXf"
    private val secretID= "EMG45Ricd253pHZw9NHnJiihDwD-5x7fobcSotuRzD1XgfKUDKpKP9TusgN-1OXRcyxv8KwAx31J8WjQ"
    private val returnUrl="com.akj.popcart://paypalpay"
    var accessToken = ""
    private lateinit var uniqueId: String
    private var orderid = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        products = args.products.toList()
        totalPrice = args.totalPrice

        AndroidNetworking.initialize(requireContext())

       fetchAccessToken()

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentBillingBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



        setupBillingProduct()
        setupAddress()

        if (!args.payment) {
            binding.apply {
                buttonPlaceOrder.visibility = View.INVISIBLE
                totalBoxContainer.visibility = View.INVISIBLE
                middleLine.visibility = View.INVISIBLE
                bottomLine.visibility = View.INVISIBLE
                tvPaymentMethods.visibility=View.GONE
                rgPaymentMethods.visibility=View.GONE
                rbCashOnDelivery.visibility=View.GONE
                rbOnlinePayment.visibility=View.GONE
                tvShoppingAddress.text="Recipent Address"
                title.text="Change Address"

            }
        }

        binding.rgPaymentMethods.setOnCheckedChangeListener { _, checkedId ->
            selectedPaymentMethod = when (checkedId) {
                R.id.rbOnlinePayment -> "Online Payment"
                R.id.rbCashOnDelivery -> "Cash on Delivery"
                else -> null
            }

            updateOnlineOrderButtonState()
            updateCODOrderButtonState()
        }

        addressAdapter.onClick = {
            selectedAddress = it
            if(!args.payment){
                val b=Bundle().apply { putParcelable("address",selectedAddress) }
                findNavController().navigate(R.id.action_billingFragment_to_addressFragment)
            }
            updateOnlineOrderButtonState()
            updateCODOrderButtonState()
        }

        binding.OnlinePlaceOrder.setOnClickListener {
                if (selectedAddress == null) {
                    Toast.makeText(requireContext(), "Please select an address", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

            val intent = Intent(requireContext(), OnlineOrder::class.java).apply {
                putExtra("totalPrice", totalPrice)
                putParcelableArrayListExtra("products", ArrayList(products)) // Pass list of CartProd
                putExtra("selectedAddress", selectedAddress)
                putExtra("selectedPaymentMethod", selectedPaymentMethod)
            }
            startActivity(intent)
//            startOrder()


        }


        binding.buttonPlaceOrder.setOnClickListener {
            if (selectedAddress == null) {
                Toast.makeText(requireContext(),"Please select and add address",Toast.LENGTH_SHORT).show()

                return@setOnClickListener
            }
            showOrderConfirmationDialog()
        }

        binding.imageAddAddress.setOnClickListener {
            findNavController().navigate(R.id.action_billingFragment_to_addressFragment)
        }

        billingAdapter.differ.submitList(products)
        binding.tvTotalPrice.text = "₹ $totalPrice"

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                billingViewModel.address.collectLatest {
                    when (it) {
                        is Resource.Loading -> {
                            binding.progressbarAddress.visibility = View.VISIBLE
                        }

                        is Resource.Success -> {
                            addressAdapter.differ.submitList((it.data))
                            binding.progressbarAddress.visibility = View.INVISIBLE
                        }

                        is Resource.Error -> {
                            binding.progressbarAddress.visibility = View.INVISIBLE
                            Toast.makeText(
                                requireContext(),
                                "Error ${it.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        else -> Unit
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                orderViewModel.order.collectLatest {
                    when (it) {
                        is Resource.Loading -> {
                            binding.buttonPlaceOrder.startAnimation()
                        }

                        is Resource.Success -> {
                            binding.buttonPlaceOrder.revertAnimation()
                            findNavController().navigateUp()
                            Snackbar.make(
                                requireView(),
                                "Your Order has been placed",
                                Snackbar.LENGTH_LONG
                            ).show()
                        }

                        is Resource.Error -> {
                            binding.buttonPlaceOrder.revertAnimation()
                            Toast.makeText(
                                requireContext(),
                                "Error ${it.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        else -> Unit
                    }
                }
            }
        }


    }




    private fun updateOnlineOrderButtonState() {
        binding.OnlinePlaceOrder.visibility =
            if (selectedAddress != null && selectedPaymentMethod == "Online Payment") {
                View.VISIBLE
            } else {
                View.INVISIBLE
            }
    }

    private fun updateCODOrderButtonState() {
        binding.buttonPlaceOrder.visibility =
            if (selectedAddress != null && selectedPaymentMethod == "Cash on Delivery") {
                View.VISIBLE
            } else {
                View.INVISIBLE
            }
    }

    private fun showOrderConfirmationDialog() {
        val alertDialog = AlertDialog.Builder(requireContext()).apply {
            setTitle("Order Items")
            setMessage("Do you want to Order this Products ? ")
            setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            setPositiveButton("Yes") { dialog, _ ->
                val order = Order(
                    OrderStatus.Ordered.status,
                    totalPrice,
                    products,
                    selectedAddress!!,
                    selectedPaymentMethod!!
                )
                orderViewModel.placeOrder(order)
                dialog.dismiss()
            }
        }
        alertDialog.create()
        alertDialog.show()
    }

    private fun setupAddress() {
        binding.rvAddress.apply {
            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
            adapter = addressAdapter
        }
    }

    private fun setupBillingProduct() {
        binding.rvProducts.apply {
            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
            adapter = billingAdapter
        }
    }


    private fun handlerOrderID(orderID: String) {
        val config = CoreConfig(clientID, environment = Environment.SANDBOX)
        val payPalWebCheckoutClient = PayPalWebCheckoutClient(requireActivity(), config, returnUrl)
        payPalWebCheckoutClient.listener = object : PayPalWebCheckoutListener {
            override fun onPayPalWebSuccess(result: PayPalWebCheckoutResult) {
                Log.d(TAG, "onPayPalWebSuccess: $result")
                val order = Order(
                    OrderStatus.Ordered.status,
                    totalPrice,
                    products,
                    selectedAddress!!,
                    selectedPaymentMethod!!
                )
                orderViewModel.placeOrder(order)

                // Show success message
                Snackbar.make(requireView(), "Payment Successful and Order Placed", Snackbar.LENGTH_LONG).show()
                findNavController().navigateUp()
            }

            override fun onPayPalWebFailure(error: PayPalSDKError) {
                Log.d(TAG, "onPayPalWebFailure: $error")
            }

            override fun onPayPalWebCanceled() {
                Log.d(TAG, "onPayPalWebCanceled: ")
            }
        }

        orderid = orderID
        val payPalWebCheckoutRequest =
            PayPalWebCheckoutRequest(orderID, fundingSource = PayPalWebCheckoutFundingSource.PAYPAL)
        payPalWebCheckoutClient.start(payPalWebCheckoutRequest)

    }

    private fun startOrder() {
        uniqueId = UUID.randomUUID().toString()

        val orderRequestJson = JSONObject().apply {
            put("intent", "CAPTURE")
            put("purchase_units", JSONArray().apply {
                put(JSONObject().apply {
                    put("reference_id", uniqueId)
                    put("amount", JSONObject().apply {
                        put("currency_code", "USD")
//                        val totalPrice = intent.getFloatExtra("totalPrice", 0f)
                        put("value",totalPrice )
                    })
                })
            })
            put("payment_source", JSONObject().apply {
                put("paypal", JSONObject().apply {
                    put("experience_context", JSONObject().apply {
                        put("payment_method_preference", "IMMEDIATE_PAYMENT_REQUIRED")
                        put("brand_name", "POPCART")
                        put("locale", "en-US")
                        put("landing_page", "LOGIN")
                        put("shipping_preference", "NO_SHIPPING")
                        put("user_action", "PAY_NOW")
                        put("return_url", returnUrl)
                        put("cancel_url", "https://example.com/cancelUrl")
                    })
                })
            })
        }

        AndroidNetworking.post("https://api-m.sandbox.paypal.com/v2/checkout/orders")
            .addHeaders("Authorization", "Bearer $accessToken")
            .addHeaders("Content-Type", "application/json")
            .addHeaders("PayPal-Request-Id", uniqueId)
            .addJSONObjectBody(orderRequestJson)
            .setPriority(Priority.HIGH)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject) {
                    Log.d(TAG, "Order Response : " + response.toString())
                    handlerOrderID(response.getString("id"))
                }

                override fun onError(error: ANError) {
                    Log.d(
                        TAG,
                        "Order Error : ${error.message} || ${error.errorBody} || ${error.response}"
                    )
                }
            })
    }

    private fun fetchAccessToken() {
        val authString = "$clientID:$secretID"
        val encodedAuthString = Base64.encodeToString(authString.toByteArray(), Base64.NO_WRAP)

        AndroidNetworking.post("https://api-m.sandbox.paypal.com/v1/oauth2/token")
            .addHeaders("Authorization", "Basic $encodedAuthString")
            .addHeaders("Content-Type", "application/x-www-form-urlencoded")
            .addBodyParameter("grant_type", "client_credentials")
            .setPriority(Priority.HIGH)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject) {
                    accessToken = response.getString("access_token")
                    Log.d(TAG, accessToken)




                }

                override fun onError(error: ANError) {
                    Log.d(TAG, error.errorBody)
                    Toast.makeText(requireContext(), "Error Occurred!", Toast.LENGTH_SHORT).show()
                }
            })
    }

    override fun onResume() {
        super.onResume()

        val intent = requireActivity().intent // Get the intent from the activity
        Log.d(TAG, "onResume: $intent")

        intent?.data?.let { data ->
            Log.d(TAG, "Received data: $data")
            val opType = data.getQueryParameter("opType")
            Log.d(TAG, "Operation Type: $opType")
            when (opType) {
                "payment" -> captureOrder(orderid)
                "cancel" -> {
                    Toast.makeText(requireContext(), "Payment Cancelled", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    Log.d(TAG, "Unknown operation type: $opType")
                }
            }
        } ?: Toast.makeText(requireContext(),"No intent data received",Toast.LENGTH_SHORT).show()

    }


    private fun captureOrder(orderID: String) {
        AndroidNetworking.post("https://api-m.sandbox.paypal.com/v2/checkout/orders/$orderID/capture")
            .addHeaders("Authorization", "Bearer $accessToken")
            .addHeaders("Content-Type", "application/json")
            .addJSONObjectBody(JSONObject()) // Empty body
            .setPriority(Priority.HIGH)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject) {
                    Log.d(TAG, "Capture Response : ${response.toString()}")
                    Toast.makeText(requireContext(), "Payment Successful", Toast.LENGTH_SHORT).show()

                }

                override fun onError(error: ANError) {
                    Log.e(TAG, "Capture Error : ${error.errorDetail}")
                    Toast.makeText(requireContext(), "Payment Capture Failed", Toast.LENGTH_SHORT).show()
                }
            })
    }


    companion object {
        const val TAG = "MyTag"
    }
}

