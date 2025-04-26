package com.example.shop;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.*;

public interface ApiService {
    @GET("api/products")
    Call<List<Product>> getProducts();

    @POST("api/customers")
    Call<Customer> createCustomer(@Body Customer customer);

    @POST("api/orders")
    Call<Order> createOrder(@Body Order order);
}