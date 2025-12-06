package api.mpesa;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import okhttp3.*;

import jakarta.enterprise.context.ApplicationScoped;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@ApplicationScoped
public class MpesaService {

    private final OkHttpClient client = new OkHttpClient();
    private final Gson gson = new Gson();

    // ====================================================
    //  GET ACCESS TOKEN (SAFER)
    // ====================================================
    private String getAccessToken() throws Exception {

        String auth = MpesaConfig.CONSUMER_KEY + ":" + MpesaConfig.CONSUMER_SECRET;
        String basicAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));

        Request request = new Request.Builder()
                .url(MpesaConfig.TOKEN_URL)
                .get()
                .addHeader("Authorization", "Basic " + basicAuth)
                .build();

        Response response = client.newCall(request).execute();

        if (!response.isSuccessful()) {
            String errorBody = response.body() != null ? response.body().string() : "EMPTY";
            throw new Exception("Token Error (" + response.code() + "): " + errorBody);
        }

        String raw = response.body().string();
        System.out.println("🔑 TOKEN RESPONSE: " + raw);

        AccessTokenResponse token = gson.fromJson(raw, AccessTokenResponse.class);

        if (token == null || token.getAccess_token() == null) {
            throw new Exception("Invalid token response: " + raw);
        }

        return token.getAccess_token();
    }

    // ====================================================
    //  INITIATE STK PUSH (FULLY FIXED)
    // ====================================================
    public StkPushResponse initiateStkPush(
            String phone,
            String amount,
            String description,
            String reference
    ) {

        StkPushResponse result = new StkPushResponse();

        try {

            // --- Generate security credentials ---
            String timestamp = generateTimestamp();
            String password = Base64.getEncoder().encodeToString(
                    (MpesaConfig.SHORT_CODE + MpesaConfig.PASSKEY + timestamp)
                            .getBytes(StandardCharsets.UTF_8)
            );

            // --- Build JSON payload ---
            JsonObject payload = new JsonObject();
            payload.addProperty("BusinessShortCode", MpesaConfig.SHORT_CODE);
            payload.addProperty("Password", password);
            payload.addProperty("Timestamp", timestamp);
            payload.addProperty("TransactionType", "CustomerPayBillOnline");
            payload.addProperty("Amount", amount);
            payload.addProperty("PartyA", phone);
            payload.addProperty("PartyB", MpesaConfig.SHORT_CODE);
            payload.addProperty("PhoneNumber", phone);
            payload.addProperty("CallBackURL", MpesaConfig.CALLBACK_URL);
            payload.addProperty("AccountReference", "Telecom Customer Service Portal");
            payload.addProperty("TransactionDesc", description);

            RequestBody body = RequestBody.create(
                    payload.toString(),
                    MediaType.parse("application/json")
            );

            Request request = new Request.Builder()
                    .url(MpesaConfig.STK_PUSH_URL)
                    .post(body)
                    .addHeader("Authorization", "Bearer " + getAccessToken())
                    .addHeader("Content-Type", "application/json")
                    .build();

            Response response = client.newCall(request).execute();
            String raw = response.body() != null ? response.body().string() : "{}";

            System.out.println("📌 RAW STK PUSH RESPONSE: " + raw);

            // Try normal STK response
            StkPushResponse success = gson.fromJson(raw, StkPushResponse.class);

            if (success != null && success.getResponseCode() != null) {
                return success;
            }

            // Handle error JSON
            JsonObject json = gson.fromJson(raw, JsonObject.class);

            if (json.has("errorMessage")) {
                result.setResponseDescription(json.get("errorMessage").getAsString());
            }
            if (json.has("errorCode")) {
                result.setResponseCode(json.get("errorCode").getAsString());
            }

            if (result.getResponseDescription() == null) {
                result.setResponseDescription(raw);
            }

            return result;

        } catch (Exception e) {

            System.out.println("❌ STK Push Exception: " + e.getMessage());
            e.printStackTrace();

            result.setResponseCode("500");
            result.setResponseDescription("Request failed: " + e.getMessage());
            return result;
        }
    }

    // ====================================================
    //  TIMESTAMP
    // ====================================================
    private String generateTimestamp() {
        return java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    }
}
