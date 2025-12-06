package api.mpesa;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.*;

import jakarta.enterprise.context.ApplicationScoped;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@ApplicationScoped
public class MpesaService {

    private final OkHttpClient client = new OkHttpClient();
    private final Gson gson = new Gson();

    // ====================================================
    //  GET ACCESS TOKEN
    // ====================================================
    private String getAccessToken() throws Exception {

        String consumerPair = MpesaConfig.CONSUMER_KEY + ":" + MpesaConfig.CONSUMER_SECRET;

        String basicAuth = Base64.getEncoder()
                .encodeToString(consumerPair.getBytes(StandardCharsets.UTF_8));

        Request request = new Request.Builder()
                .url(MpesaConfig.TOKEN_URL)
                .get()
                .addHeader("Authorization", "Basic " + basicAuth)
                .build();

        Response response = client.newCall(request).execute();

        if (!response.isSuccessful()) {
            throw new Exception("Unable to generate access token: " + response.code());
        }

        String raw = response.body().string();
        System.out.println("🔑 RAW TOKEN RESPONSE: " + raw);

        AccessTokenResponse tokenResponse = gson.fromJson(raw, AccessTokenResponse.class);

        return tokenResponse.getAccess_token();
    }

    // ====================================================
    //  INITIATE STK PUSH (IMPROVED)
    // ====================================================
    public StkPushResponse initiateStkPush(
            String phone,
            String amount,
            String description,
            String reference
    ) throws Exception {

        String timestamp = generateTimestamp();

        String password = Base64.getEncoder().encodeToString(
                (MpesaConfig.SHORT_CODE + MpesaConfig.PASSKEY + timestamp)
                        .getBytes(StandardCharsets.UTF_8)
        );

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
        payload.addProperty("AccountReference", reference);
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
        String rawResponse = response.body().string();

        // Log FULL response for debugging
        System.out.println("📌 RAW M-PESA RESPONSE: " + rawResponse);

        // Parse to response object
        StkPushResponse stk = gson.fromJson(rawResponse, StkPushResponse.class);

        // If ResponseCode exists → normal STK response
        if (stk != null && stk.getResponseCode() != null) {
            return stk;
        }

        // If M-Pesa returned an error JSON
        JsonObject json = JsonParser.parseString(rawResponse).getAsJsonObject();

        StkPushResponse error = new StkPushResponse();

        if (json.has("errorMessage")) {
            error.setResponseDescription(json.get("errorMessage").getAsString());
        }
        if (json.has("errorCode")) {
            error.setResponseCode(json.get("errorCode").getAsString());
        }

        if (error.getResponseDescription() == null) {
            error.setResponseDescription("Unknown STK Push error. RAW: " + rawResponse);
        }

        return error;
    }

    // ====================================================
    //  TIMESTAMP
    // ====================================================
    private String generateTimestamp() {
        java.time.format.DateTimeFormatter dtf =
                java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        return java.time.LocalDateTime.now().format(dtf);
    }
}
