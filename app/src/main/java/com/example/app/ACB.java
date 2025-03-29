package com.example.app;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ACB {
    private static final String TAG = "ACB";
    private static final String LOGIN_URL = "https://apiapp.acb.com.vn/mb/v2/auth/tokens";
    private static final String GET_TRANS_URL = "https://apiapp.acb.com.vn/mb/legacy/ss/cs/bankservice/saving/tx-history?maxRows=%s&account=%s";

    private String clientId = "";
    private String username;
    private String password;
    private OkHttpClient client;

    public ACB(String username, String password) {
        this.username = username;
        this.password = password;

        // Bỏ proxy để kiểm tra
        Log.d(TAG, "Khởi tạo ACB không sử dụng proxy");
        this.client = new OkHttpClient.Builder()
                .build();
    }

    public interface LoginCallback {
        void onSuccess(String token);
        void onFailure(String error);
    }

    public void login(LoginCallback callback) {
        Log.d(TAG, "Bắt đầu đăng nhập vào ACB với username: " + username);
        Map<String, String> data = new HashMap<>();
        data.put("clientId", clientId);
        data.put("username", username);
        data.put("password", password);

        RequestBody body = RequestBody.create(
                MediaType.parse("application/json; charset=utf-8"),
                new JSONObject(data).toString()
        );

        Request request = new Request.Builder()
                .url(LOGIN_URL)
                .post(body)
                .addHeader("Content-Type", "application/json; charset=utf-8")
                .addHeader("Host", "apiapp.acb.com.vn")
                .addHeader("accept", "application/json")
                .build();

        Log.d(TAG, "Gửi yêu cầu đăng nhập đến: " + LOGIN_URL);
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "Lỗi khi đăng nhập vào ACB: " + e.getMessage());
                callback.onFailure("Lỗi khi đăng nhập: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    String errorBody = response.body() != null ? response.body().string() : "Không có phản hồi";
                    Log.e(TAG, "Đăng nhập thất bại, mã lỗi: " + response.code() + ", phản hồi: " + errorBody);
                    callback.onFailure("Lỗi từ API ACB: " + response.code() + ", phản hồi: " + errorBody);
                    return;
                }

                try {
                    String responseBody = response.body().string();
                    Log.d(TAG, "Phản hồi đăng nhập: " + responseBody);
                    JSONObject json = new JSONObject(responseBody);
                    String token = json.optString("access_token");
                    if (token != null && !token.isEmpty()) {
                        Log.d(TAG, "Đăng nhập thành công, token: " + token);
                        callback.onSuccess(token);
                    } else {
                        Log.e(TAG, "Không tìm thấy token trong phản hồi: " + responseBody);
                        callback.onFailure("Không tìm thấy token trong phản hồi: " + responseBody);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Lỗi khi xử lý phản hồi đăng nhập: " + e.getMessage());
                    callback.onFailure("Lỗi khi xử lý phản hồi: " + e.getMessage());
                }
            }
        });
    }

    public interface TransactionCallback {
        void onSuccess(JSONArray transactions);
        void onFailure(String error);
    }

    public void getTransactions(String accountNo, String rows, String token, TransactionCallback callback) {
        String url = String.format(GET_TRANS_URL, rows, accountNo);
        Log.d(TAG, "Bắt đầu lấy lịch sử giao dịch từ ACB, URL: " + url);

        Request request = new Request.Builder()
                .url(url)
                .get()
                .addHeader("Content-Type", "application/json")
                .addHeader("Host", "apiapp.acb.com.vn")
                .addHeader("Authorization", "bearer " + token)
                .addHeader("User-Agent", "ACB-MBA/5 CFNetwork/1333.0.4 Darwin/21.5.0")
                .addHeader("Accept-Language", "vi")
                .addHeader("x-app-version", "3.7.0")
                .build();

        Log.d(TAG, "Gửi yêu cầu lấy lịch sử giao dịch đến: " + url);
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "Lỗi khi lấy lịch sử giao dịch: " + e.getMessage());
                callback.onFailure("Lỗi khi lấy lịch sử giao dịch: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    String errorBody = response.body() != null ? response.body().string() : "Không có phản hồi";
                    Log.e(TAG, "Lấy lịch sử giao dịch thất bại, mã lỗi: " + response.code() + ", phản hồi: " + errorBody);
                    callback.onFailure("Lỗi từ API ACB: " + response.code() + ", phản hồi: " + errorBody);
                    return;
                }

                try {
                    String responseBody = response.body().string();
                    Log.d(TAG, "Phản hồi lịch sử giao dịch: " + responseBody);
                    JSONObject json = new JSONObject(responseBody);
                    String status = json.optString("status");

                    if (!"success".equals(status)) {
                        Log.e(TAG, "Lỗi từ API ACB: " + json.optString("message"));
                        callback.onFailure("Lỗi từ API ACB: " + json.optString("message"));
                        return;
                    }

                    JSONArray transactions = json.optJSONArray("transactions");
                    if (transactions != null) {
                        Log.d(TAG, "Lấy lịch sử giao dịch thành công, số lượng giao dịch: " + transactions.length());
                        callback.onSuccess(transactions);
                    } else {
                        Log.e(TAG, "Không tìm thấy giao dịch trong phản hồi: " + responseBody);
                        callback.onFailure("Không tìm thấy giao dịch trong phản hồi: " + responseBody);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Lỗi khi xử lý phản hồi lịch sử giao dịch: " + e.getMessage());
                    callback.onFailure("Lỗi khi xử lý phản hồi: " + e.getMessage());
                }
            }
        });
    }

    public String generateImei() {
        String imei = generateRandomString(8) + "-" + generateRandomString(4) + "-" +
                generateRandomString(4) + "-" + generateRandomString(4) + "-" +
                generateRandomString(12);
        Log.d(TAG, "Tạo IMEI: " + imei);
        return imei;
    }

    public String generateRandomString(int length) {
        String characters = "0123456789zxcvbnmlkjhgfdsaqwertyuiopZXCVBNMLKJHGFDSAQWERTYUIOP";
        Random random = new Random();
        StringBuilder randomString = new StringBuilder();
        for (int i = 0; i < length; i++) {
            randomString.append(characters.charAt(random.nextInt(characters.length())));
        }
        return randomString.toString();
    }

    public String getToken() {
        String token = generateRandomString(39);
        Log.d(TAG, "Tạo token ngẫu nhiên: " + token);
        return token;
    }
}