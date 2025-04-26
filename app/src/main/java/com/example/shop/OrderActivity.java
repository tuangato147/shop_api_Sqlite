package com.example.shop;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrderActivity extends AppCompatActivity {
    private static final String TAG = "OrderActivity";
    private ImageView imgProduct;
    private TextView tvProductName;
    private TextView tvProductPrice;
    private EditText etName, etPhone, etAddress;
    private Button btnSubmit;
    private int productId;
    private String productName;
    private String productImage;
    private double productPrice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order);

        // Ánh xạ view
        imgProduct = findViewById(R.id.imgProduct);
        tvProductName = findViewById(R.id.tvProductName);
        tvProductPrice = findViewById(R.id.tvProductPrice);
        etName = findViewById(R.id.etName);
        etPhone = findViewById(R.id.etPhone);
        etAddress = findViewById(R.id.etAddress);
        btnSubmit = findViewById(R.id.btnSubmit);

        // Lấy thông tin sản phẩm từ intent
        Intent intent = getIntent();
        productId = intent.getIntExtra("product_id", 0);
        productName = intent.getStringExtra("product_name");
        productPrice = intent.getDoubleExtra("product_price", 0);
        productImage = intent.getStringExtra("product_image");

        // Hiển thị thông tin sản phẩm
        displayProductInfo();

        btnSubmit.setOnClickListener(v -> submitOrder());
    }

    private void displayProductInfo() {
        // Hiển thị ảnh sản phẩm
        if (productImage != null && !productImage.isEmpty()) {
            Glide.with(this)
                    .load(productImage)
                    .placeholder(R.drawable.placeholder)
                    .error(R.drawable.placeholder)
                    .into(imgProduct);
        }

        // Hiển thị tên và giá sản phẩm
        tvProductName.setText(productName);
        tvProductPrice.setText(String.format("%,d đ", (int)productPrice));
    }

    private void submitOrder() {
        // Validate input
        String name = etName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String address = etAddress.getText().toString().trim();

        if (name.isEmpty() || phone.isEmpty() || address.isEmpty()) {
            Toast.makeText(this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        // Hiển thị loading
        LoadingDialog loadingDialog = new LoadingDialog(this);
        loadingDialog.show();

        // Tạo customer trước
        Customer customer = new Customer(name, phone, address);
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<Customer> customerCall = apiService.createCustomer(customer);

        customerCall.enqueue(new Callback<Customer>() {
            @Override
            public void onResponse(Call<Customer> call, Response<Customer> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Customer newCustomer = response.body();
                    Log.d(TAG, "Created customer with ID: " + newCustomer.getId());

                    // Tạo order với customer_id
                    createOrder(newCustomer.getId(), loadingDialog);
                } else {
                    loadingDialog.dismiss();
                    String error = "Lỗi tạo khách hàng: " + response.code();
                    try {
                        if (response.errorBody() != null) {
                            error = response.errorBody().string();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error reading error body", e);
                    }
                    Toast.makeText(OrderActivity.this, error, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Customer> call, Throwable t) {
                loadingDialog.dismiss();
                Log.e(TAG, "Network error", t);
                Toast.makeText(OrderActivity.this,
                        "Lỗi kết nối: " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void createOrder(int customerId, LoadingDialog loadingDialog) {
        Log.d(TAG, "Creating order with customer_id: " + customerId);

        Order order = new Order(
                customerId,    // customer_id
                productId,     // product_id
                1,            // quantity
                productPrice   // total_price
        );

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<Order> orderCall = apiService.createOrder(order);

        orderCall.enqueue(new Callback<Order>() {
            @Override
            public void onResponse(Call<Order> call, Response<Order> response) {
                loadingDialog.dismiss();
                if (response.isSuccessful()) {
                    Toast.makeText(OrderActivity.this,
                            "Đặt hàng thành công!",
                            Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    String error = "Lỗi tạo đơn hàng: " + response.code();
                    try {
                        if (response.errorBody() != null) {
                            String errorBody = response.errorBody().string();
                            Log.e(TAG, "Error body: " + errorBody);
                            error = errorBody;
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error reading error body", e);
                    }
                    Toast.makeText(OrderActivity.this, error, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Order> call, Throwable t) {
                loadingDialog.dismiss();
                Log.e(TAG, "Network error", t);
                Toast.makeText(OrderActivity.this,
                        "Lỗi kết nối: " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Cleanup to avoid memory leaks
    }
}