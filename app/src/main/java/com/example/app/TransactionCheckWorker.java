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
        long totalAmount = getInputData().getLong("totalAmount", 0);
        String accountNumber = getInputData().getString("accountNumber");

        Log.d(TAG, "Bắt đầu kiểm tra giao dịch: orderId=" + orderId + ", transactionContent=" + transactionContent +
                ", totalAmount=" + totalAmount + ", accountNumber=" + accountNumber);

        if (orderId == null || transactionContent == null || accountNumber == null) {
            Log.e(TAG, "Thiếu thông tin cần thiết để kiểm tra giao dịch");
            return Result.failure();
        }

        // Sử dụng CountDownLatch để chờ API hoàn tất
        CountDownLatch latch = new CountDownLatch(1);
        final Result[] workerResult = {Result.success()}; // Mặc định là success, sẽ thay đổi nếu có lỗi

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
                            checkTransaction(orderId, transactionContent, latch, workerResult);
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

            // Chờ API hoàn tất, tối đa 30 giây
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

    private void checkTransaction(String orderId, String transactionContent,
                                  CountDownLatch latch, Result[] workerResult) {
        Log.d(TAG, "Bắt đầu kiểm tra giao dịch với API: " + API_URL);
        Log.d(TAG, "Tìm kiếm giao dịch với transactionContent: " + transactionContent);

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
                                // Kiểm tra không phân biệt hoa thường
                                if (description.toLowerCase().contains(transactionContent.toLowerCase())) {
                                    isConfirmed = true;
                                    transactionId = String.valueOf(transaction.optInt("transactionID"));
                                    Log.d(TAG, "Tìm thấy giao dịch khớp: transactionId=" + transactionId);
                                    break;
                                } else {
                                    Log.d(TAG, "Không khớp: description.toLowerCase()=" + description.toLowerCase() +
                                            ", transactionContent.toLowerCase()=" + transactionContent.toLowerCase());
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Lỗi khi xử lý giao dịch: " + e.getMessage());
                            }
                        }

                        if (isConfirmed) {
                            Log.d(TAG, "Giao dịch được xác nhận, cập nhật trạng thái đơn hàng: " + orderId);
                            updateOrderStatus(orderId, transactionId);
                            sendNotification("Giao dịch đã được xác nhận", "Đơn hàng " + orderId + " đã được xác nhận thành công!");
                            WorkManager.getInstance(getApplicationContext()).cancelAllWorkByTag("transaction_check_" + orderId);
                        } else {
                            Log.d(TAG, "Không tìm thấy giao dịch khớp với transactionContent=" + transactionContent);
                            // Nếu không tìm thấy giao dịch khớp, trả về Result.retry() để kiểm tra lại sau
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

    private void updateOrderStatus(String orderId, String transactionId) {
        Log.d(TAG, "Cập nhật trạng thái đơn hàng trên Firestore: orderId=" + orderId + ", transactionId=" + transactionId);
        db.collection("Orders").document(orderId).update(
                "status", "Confirmed",
                "transactionId", transactionId,
                "updatedAt", System.currentTimeMillis()
        ).addOnSuccessListener(aVoid -> {
            Log.d(TAG, "Cập nhật trạng thái thành công: Đơn hàng " + orderId + " đã được xác nhận");
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Lỗi khi cập nhật trạng thái: " + e.getMessage());
        });
    }

    private void sendNotification(String title, String message) {
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

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        notificationManager.notify((int) System.currentTimeMillis(), builder.build());
        Log.d(TAG, "Thông báo đã được gửi");
    }

    // Phương thức để lập lịch kiểm tra giao dịch
    public static void scheduleTransactionCheck(Context context, String orderId, String transactionContent, long totalAmount, String accountNumber) {
        Data inputData = new Data.Builder()
                .putString("orderId", orderId)
                .putString("transactionContent", transactionContent)
                .putLong("totalAmount", totalAmount)
                .putString("accountNumber", accountNumber)
                .build();

        OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(TransactionCheckWorker.class)
                .setInputData(inputData)
                .addTag("transaction_check_" + orderId)
                .setBackoffCriteria(
                        BackoffPolicy.LINEAR, // Chính sách retry
                        5, TimeUnit.SECONDS // Retry sau 5 giây
                )
                .build();

        WorkManager.getInstance(context).enqueue(workRequest);
        Log.d(TAG, "Đã lập lịch kiểm tra giao dịch cho orderId: " + orderId);
    }
}