package api.mpesa;

public class MpesaConfig {

    public static final String CONSUMER_KEY = "SYHoWBg6DFSzEnv7jiTZe9VmAzCMG8tXt6Q20A4PHz525yfc";
    public static final String CONSUMER_SECRET = "6o7GRsl1eaZhdt5SDNNA2MDTBzK98YONcX7vrJqgBsoHveIbVsctlrGCFfEiBJrC";

    public static final String SHORT_CODE = "174379";   
    public static final String PASSKEY = "bfb279f9aa9bdbcf158e97dd71a467cd2e0c893059b10f78e6b72ada1ed2c919";

    public static final String TOKEN_URL =
            "https://sandbox.safaricom.co.ke/oauth/v1/generate?grant_type=client_credentials";

    public static final String STK_PUSH_URL =
            "https://sandbox.safaricom.co.ke/mpesa/stkpush/v1/processrequest";

    public static final String CALLBACK_URL =
            "https://mydomain.com/mpesa-express-simulate/";  // MUST be https
}
