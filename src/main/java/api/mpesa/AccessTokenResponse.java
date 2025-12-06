package api.mpesa;

public class AccessTokenResponse {

    private String access_token;
    private String expires_in;

    public String getAccess_token() {
        return access_token;
    }

    public String getExpires_in() {
        return expires_in;
    }
}
