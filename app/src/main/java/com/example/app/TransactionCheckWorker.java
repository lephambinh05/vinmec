package com.example.app;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.BackoffPolicy;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class TransactionCheckWorker extends Worker {
    private static final String TAG = "TransactionCheckWorker";
    private static final String NOTIFICATION_CHANNEL_ID = "transaction_channel";

    private final FirebaseFirestore db;
    private final OkHttpClient client;

    // Thay thế bằng token thật của bạn
    private static final String TOKEN_ACB = "0c8c99792fae17b2f5555fb1ee94c6f9"; // Thay bằng token thật
    private static final String API_URL = "https://thueapi.pro/historyapiacbv2/" + TOKEN_ACB;

    public TransactionCheckWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
        this.db = FirebaseFirestore.getInstance();
        this.client = new OkHttpClient.Builder().build();
        Log.d(TAG, "Khởi tạo TransactionCheckWorker");
    }

    @NonNull
    @Override
    public Result doWork() {
        String orderId = getInputData().getString("orderId");
        String transactionContent = getInputData().getString("transactionContent");
        String accountNumber = getInputData().getString("accountNumber");
        String orderItemsJson = getInputData().getString("orderItems");
        String address = getInputData().getString("address");
        String fullName = getInputData().getString("fullName");
        String paymentMethod = getInputData().getString("paymentMethod");

        Log.d(TAG, "Bắt đầu kiểm tra giao dịch: orderId=" + orderId + ", transactionContent=" + transactionContent +
                ", accountNumber=" + accountNumber + ", orderItems=" + orderItemsJson +
                ", address=" + address + ", fullName=" + fullName + ", paymentMethod=" + paymentMethod);

        if (orderId == null || transactionContent == null || accountNumber == null || orderItemsJson == null ||
                address == null || fullName == null || paymentMethod == null) {
            Log.e(TAG, "Thiếu thông tin cần thiết để kiểm tra giao dịch");
            return Result.failure();
        }

        // Parse danh sách thuốc từ JSON
        List<OrderItem> orderItems = parseOrderItems(orderItemsJson);
        if (orderItems.isEmpty()) {
            Log.e(TAG, "Danh sách thuốc rỗng");
            return Result.failure();
        }

        // Tính tổng tiền
        long totalAmount = calculateTotalAmount(orderItems);
        Log.d(TAG, "Tổng tiền đơn hàng: " + totalAmount);

        // Sử dụng CountDownLatch để chờ API hoàn tất
        CountDownLatch latch = new CountDownLatch(1);
        final Result[] workerResult = {Result.success()};

        try {
            Log.d(TAG, "Kiểm tra trạng thái đơn hàng trên Firestore: " + orderId);
            db.collection("Orders").document(orderId).get()
                    .addOnSuccessListener(document -> {
                        if (document.exists()) {
                            String status = document.getString("status");
                            Log.d(TAG, "Trạng thái đơn hàng: " + status);
                            if ("Confirmed".equals(status)) {
                                Log.d(TAG, "Đơn hàng đã được xác nhận, hủy kiểm tra định kỳ");
                                WorkManager.getInstance(getApplicationContext()).cancelAllWorkByTag("transaction_check_" + orderId);
                                latch.countDown();
                                return;
                            }
                            checkTransaction(orderId, transactionContent, orderItems, totalAmount, address, fullName, paymentMethod, latch, workerResult);
                        } else {
                            Log.e(TAG, "Không tìm thấy đơn hàng: " + orderId);
                            workerResult[0] = Result.failure();
                            latch.countDown();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Lỗi khi kiểm tra trạng thái đơn hàng: " + e.getMessage());
                        workerResult[0] = Result.failure();
                        latch.countDown();
                    });

            try {
                latch.await(30, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Log.e(TAG, "Lỗi khi chờ API: " + e.getMessage());
                return Result.retry();
            }

            Log.d(TAG, "Worker hoàn tất với kết quả: " + workerResult[0]);
            return workerResult[0];
        } catch (Exception e) {
            Log.e(TAG, "Lỗi trong Worker: " + e.getMessage());
            return Result.retry();
        }
    }

    private void checkTransaction(String orderId, String transactionContent, List<OrderItem> orderItems,
                                  long totalAmount, String address, String fullName, String paymentMethod,
                                  CountDownLatch latch, Result[] workerResult) {
        Log.d(TAG, "Bắt đầu kiểm tra giao dịch với API: " + API_URL);
        Log.d(TAG, "Tìm kiếm giao dịch với transactionContent: " + transactionContent);

        String normalizedTransactionContent = transactionContent.replace("_", "");
        Log.d(TAG, "transactionContent sau khi chuẩn hóa: " + normalizedTransactionContent);

        Request request = new Request.Builder()
                .url(API_URL)
                .get()
                .build();

        Log.d(TAG, "Gửi yêu cầu lấy lịch sử giao dịch đến: " + API_URL);
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "Lỗi khi lấy lịch sử giao dịch: " + e.getMessage());
                workerResult[0] = Result.retry();
                latch.countDown();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    String errorBody = response.body() != null ? response.body().string() : "Không có phản hồi";
                    Log.e(TAG, "Lấy lịch sử giao dịch thất bại, mã lỗi: " + response.code() + ", phản hồi: " + errorBody);
                    workerResult[0] = Result.retry();
                    latch.countDown();
                    return;
                }

                try {
                    String responseBody = response.body().string();
                    Log.d(TAG, "Phản hồi lịch sử giao dịch: " + responseBody);
                    JSONObject json = new JSONObject(responseBody);
                    String status = json.optString("status");

                    if (!"success".equals(status)) {
                        Log.e(TAG, "Lỗi từ API: " + json.optString("message"));
                        workerResult[0] = Result.retry();
                        latch.countDown();
                        return;
                    }

                    JSONArray transactions = json.optJSONArray("transactions");
                    if (transactions != null) {
                        Log.d(TAG, "Bắt đầu xử lý " + transactions.length() + " giao dịch từ API");
                        boolean isConfirmed = false;
                        String transactionId = null;

                        for (int i = 0; i < transactions.length(); i++) {
                            try {
                                JSONObject transaction = transactions.getJSONObject(i);
                                String description = transaction.optString("description");
                                long amount = transaction.optLong("amount");
                                Log.d(TAG, "Giao dịch " + (i + 1) + ": description=" + description + ", amount=" + amount);
                                if (description.toLowerCase().contains(normalizedTransactionContent.toLowerCase())) {
                                    isConfirmed = true;
                                    transactionId = String.valueOf(transaction.optInt("transactionID"));
                                    Log.d(TAG, "Tìm thấy giao dịch khớp: transactionId=" + transactionId);
                                    break;
                                } else {
                                    Log.d(TAG, "Không khớp: description.toLowerCase()=" + description.toLowerCase() +
                                            ", normalizedTransactionContent.toLowerCase()=" + normalizedTransactionContent.toLowerCase());
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Lỗi khi xử lý giao dịch: " + e.getMessage());
                            }
                        }

                        if (isConfirmed) {
                            Log.d(TAG, "Giao dịch được xác nhận, cập nhật trạng thái đơn hàng: " + orderId);
                            updateOrderStatus(orderId, transactionId, orderItems, totalAmount, address, fullName, paymentMethod);
                            sendNotification("Giao dịch đã được xác nhận", "Đơn hàng " + orderId + " đã được xác nhận thành công!", fullName, address, paymentMethod);
                            WorkManager.getInstance(getApplicationContext()).cancelAllWorkByTag("transaction_check_" + orderId);
                        } else {
                            Log.d(TAG, "Không tìm thấy giao dịch khớp với transactionContent=" + transactionContent);
                            workerResult[0] = Result.retry();
                        }
                    } else {
                        Log.e(TAG, "Không tìm thấy giao dịch trong phản hồi: " + responseBody);
                        workerResult[0] = Result.retry();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Lỗi khi xử lý phản hồi lịch sử giao dịch: " + e.getMessage());
                    workerResult[0] = Result.retry();
                } finally {
                    latch.countDown();
                }
            }
        });
    }

    private void updateOrderStatus(String orderId, String transactionId, List<OrderItem> orderItems,
                                   long totalAmount, String address, String fullName, String paymentMethod) {
        Log.d(TAG, "Cập nhật trạng thái đơn hàng trên Firestore: orderId=" + orderId + ", transactionId=" + transactionId);

        Map<String, Object> orderData = new HashMap<>();
        orderData.put("orderId", orderId);
        orderData.put("address", address);
        orderData.put("fullName", fullName);
        orderData.put("paymentMethod", paymentMethod);
        orderData.put("totalAmount", totalAmount);
        orderData.put("status", "Confirmed");
        orderData.put("transactionId", transactionId);
        orderData.put("updatedAt", System.currentTimeMillis());

        db.collection("Orders").document(orderId).set(orderData)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Cập nhật trạng thái đơn hàng thành công: Đơn hàng " + orderId + " đã được xác nhận");

                    // Lưu danh sách thuốc vào collection OrderItems
                    saveOrderItems(orderId, orderItems);

                    // Giảm số lượng thuốc trong kho
                    for (OrderItem item : orderItems) {
                        updateMedicineQuantity(orderId, item.medicineName, item.quantity);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Lỗi khi cập nhật trạng thái đơn hàng: " + e.getMessage());
                });
    }

    private void saveOrderItems(String orderId, List<OrderItem> orderItems) {
        for (OrderItem item : orderItems) {
            Map<String, Object> itemData = new HashMap<>();
            itemData.put("orderId", orderId);
            itemData.put("medicineName", item.medicineName);
            itemData.put("price", item.price);
            itemData.put("quantity", item.quantity);

            String documentId = orderId + "_" + item.medicineName;
            db.collection("OrderItems").document(documentId).set(itemData)
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "Lưu OrderItem thành công: orderId=" + orderId + ", medicineName=" + item.medicineName);
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Lỗi khi lưu OrderItem: " + e.getMessage());
                    });
        }
    }

    private void updateMedicineQuantity(String orderId, String medicineName, int quantityToReduce) {
        Log.d(TAG, "Kiểm tra và giảm số lượng thuốc: medicineName=" + medicineName + ", quantityToReduce=" + quantityToReduce);

        // Truy vấn bảng medicine (thay vì Medicines)
        db.collection("medicine")
                .whereEqualTo("name_Pro", medicineName)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        for (var doc : queryDocumentSnapshots) {
                            String amountStr = doc.getString("Amount");
                            if (amountStr == null) {
                                Log.e(TAG, "Số lượng thuốc không hợp lệ: medicineName=" + medicineName);
                                return;
                            }

                            // Chuyển Amount từ string sang số
                            int currentQuantity;
                            try {
                                currentQuantity = Integer.parseInt(amountStr);
                            } catch (NumberFormatException e) {
                                Log.e(TAG, "Số lượng thuốc không hợp lệ: medicineName=" + medicineName);
                                return;
                            }

                            // Kiểm tra số lượng trong kho
                            if (currentQuantity < quantityToReduce) {
                                Log.e(TAG, "Không đủ số lượng thuốc trong kho: medicineName=" + medicineName +
                                        ", currentQuantity=" + currentQuantity + ", quantityToReduce=" + quantityToReduce);
                                db.collection("Orders").document(orderId).update("status", "Failed");
                                return;
                            }

                            // Giảm số lượng thuốc
                            int newQuantity = currentQuantity - quantityToReduce;
                            db.collection("medicine").document(doc.getId())
                                    .update("Amount", String.valueOf(newQuantity))
                                    .addOnSuccessListener(aVoid -> {
                                        Log.d(TAG, "Đã giảm số lượng thuốc: medicineName=" + medicineName +
                                                ", newQuantity=" + newQuantity);
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e(TAG, "Lỗi khi giảm số lượng thuốc: " + e.getMessage());
                                    });
                            break;
                        }
                    } else {
                        Log.e(TAG, "Không tìm thấy thuốc trong kho: medicineName=" + medicineName);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Lỗi khi kiểm tra số lượng thuốc: " + e.getMessage());
                });
    }

    private void sendNotification(String title, String message, String fullName, String address, String paymentMethod) {
        Log.d(TAG, "Gửi thông báo: title=" + title + ", message=" + message);
        NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    NOTIFICATION_CHANNEL_ID,
                    "Transaction Notifications",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            notificationManager.createNotificationChannel(channel);
            Log.d(TAG, "Tạo NotificationChannel: " + NOTIFICATION_CHANNEL_ID);
        }

        String detailedMessage = message + "\n" +
                "Tên khách hàng: " + fullName + "\n" +
                "Địa chỉ: " + address + "\n" +
                "Phương thức thanh toán: " + paymentMethod + "\n" +
                "Trạng thái: Confirmed";

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(detailedMessage))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        notificationManager.notify((int) System.currentTimeMillis(), builder.build());
        Log.d(TAG, "Thông báo đã được gửi");
    }

    public static void scheduleTransactionCheck(Context context, String orderId, String transactionContent,
                                                String accountNumber, String orderItemsJson,
                                                String address, String fullName, String paymentMethod) {
        Data inputData = new Data.Builder()
                .putString("orderId", orderId)
                .putString("transactionContent", transactionContent)
                .putString("accountNumber", accountNumber)
                .putString("orderItems", orderItemsJson)
                .putString("address", address)
                .putString("fullName", fullName)
                .putString("paymentMethod", paymentMethod)
                .build();

        OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(TransactionCheckWorker.class)
                .setInputData(inputData)
                .addTag("transaction_check_" + orderId)
                .setBackoffCriteria(
                        BackoffPolicy.LINEAR,
                        5, TimeUnit.SECONDS
                )
                .build();

        WorkManager.getInstance(context).enqueue(workRequest);
        Log.d(TAG, "Đã lập lịch kiểm tra giao dịch cho orderId: " + orderId);
    }

    public static class OrderItem {
        String medicineName;
        long price;
        int quantity;

        OrderItem(String medicineName, long price, int quantity) {
            this.medicineName = medicineName;
            this.price = price;
            this.quantity = quantity;
        }
    }

    private List<OrderItem> parseOrderItems(String orderItemsJson) {
        List<OrderItem> orderItems = new ArrayList<>();
        try {
            JSONArray jsonArray = new JSONArray(orderItemsJson);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String medicineName = jsonObject.getString("medicineName");
                long price = jsonObject.getLong("price");
                int quantity = jsonObject.getInt("quantity");
                orderItems.add(new OrderItem(medicineName, price, quantity));
            }
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi parse orderItems: " + e.getMessage());
        }
        return orderItems;
    }

    private long calculateTotalAmount(List<OrderItem> orderItems) {
        long totalAmount = 0;
        for (OrderItem item : orderItems) {
            totalAmount += item.price * item.quantity;
        }
        return totalAmount;
    }
}