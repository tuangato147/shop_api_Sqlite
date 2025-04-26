package com.example.shop;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

public class ProductDetailActivity extends AppCompatActivity {
    private ImageView imgProduct;
    private TextView tvName, tvPrice, tvDescription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        // Ánh xạ view
        imgProduct = findViewById(R.id.imgProduct);
        tvName = findViewById(R.id.tvName);
        tvPrice = findViewById(R.id.tvPrice);
        tvDescription = findViewById(R.id.tvDescription);

        // Lấy dữ liệu từ intent
        Intent intent = getIntent();
        String name = intent.getStringExtra("product_name");
        double price = intent.getDoubleExtra("product_price", 0);
        String image = intent.getStringExtra("product_image");
        String description = intent.getStringExtra("product_description");

        // Hiển thị dữ liệu
        tvName.setText(name);
        tvPrice.setText(String.format("%,d đ", (int)price));
        tvDescription.setText(description);

        // Load ảnh
        if (image != null && !image.isEmpty()) {
            Glide.with(this)
                    .load(image)
                    .placeholder(R.drawable.placeholder)
                    .error(R.drawable.placeholder)
                    .into(imgProduct);
        }
    }
}
