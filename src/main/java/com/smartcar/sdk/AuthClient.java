package com.smartcar.sdk;

import okhttp3.*;

/**
 * Smartcar OAuth 2.0 Authentication Client
 */
public class AuthClient extends ApiClient {
  private static final String URL_AUTHORIZE = "https://connect.smartcar.com/oauth/authorize";
  private static final String URL_ACCESS_TOKEN = "https://auth.smartcar.com/oauth/token";

  private String clientId;
  private String clientAuthorization;
  private String redirectUri;
  private String[] scope;
  private boolean development;

  /**
   * Initializes a new AuthClient.
   *
   * @param clientId the application client ID
   * @param clientSecret the application client secret
   * @param redirectUri the registered redirect URI for the application
   * @param scope the permission scope requested
   * @param development whether or not to operate in development mode
   */
  public AuthClient(String clientId, String clientSecret, String redirectUri, String[] scope, boolean development) {
    this.clientId = clientId;
    this.clientAuthorization = Credentials.basic(clientId, clientSecret);
    this.redirectUri = redirectUri;
    this.scope = scope;
    this.development = development;
  }

  /**
   * Executes an Auth API request.
   *
   * @param requestBody the request body to be included
   *
   * @return the parsed response
   *
   * @throws SmartcarException if the API request fails
   */
  private Auth call(RequestBody requestBody) throws SmartcarException {
    Request request = new Request.Builder()
        .url(AuthClient.URL_ACCESS_TOKEN)
        .header("Authorization", this.clientAuthorization)
        .header("Content-Type", "application/json")
        .addHeader("User-Agent", AuthClient.USER_AGENT)
        .post(requestBody)
        .build();

    return AuthClient.execute(request, Auth.class);
  }

  /**
   * Initializes a new AuthClient.
   *
   * @param clientId the application client ID
   * @param clientSecret the application client secret
   * @param redirectUri the registered redirect URI for the application
   * @param development whether or not to operate in development mode
   */
  public AuthClient(String clientId, String clientSecret, String redirectUri, boolean development) {
    this(clientId, clientSecret, redirectUri, null, development);
  }

  /**
   * Initializes a new AuthClient.
   *
   * @param clientId the application client ID
   * @param clientSecret the application client secret
   * @param redirectUri the registered redirect URI for the application
   * @param scope the permission scope requested
   */
  public AuthClient(String clientId, String clientSecret, String redirectUri, String[] scope) {
    this(clientId, clientSecret, redirectUri, scope, false);
  }

  /**
   * Initializes a new AuthClient.
   *
   * @param clientId the client ID to be used with all requests
   * @param clientSecret the client secret to be used with all requests
   * @param redirectUri the configured redirect URL associated with the account
   */
  public AuthClient(String clientId, String clientSecret, String redirectUri) {
    this(clientId, clientSecret, redirectUri, null, false);
  }

  /**
   * Returns the assembled authentication URL.
   *
   * @param state an arbitrary string to be returned to the redirect URI
   * @param forcePrompt whether to force the approval prompt to show every auth
   *
   * @return the authentication URL
   */
  public String getAuthUrl(String state, boolean forcePrompt) {
    HttpUrl.Builder urlBuilder = HttpUrl.parse(AuthClient.URL_AUTHORIZE).newBuilder()
        .addQueryParameter("response_type", "code")
        .addQueryParameter("client_id", this.clientId)
        .addQueryParameter("redirect_uri", this.redirectUri)
        .addQueryParameter("approval_prompt", forcePrompt ? "force" : "auto");

    if(state != null) {
      urlBuilder.addQueryParameter("state", state);
    }

    if(this.scope != null) {
      urlBuilder.addQueryParameter("scope", Utils.join(this.scope, " "));
    }

    if(this.development) {
      urlBuilder.addQueryParameter("mock", "true");
    }

    return urlBuilder.build().toString();
  }

  /**
   * Returns the assembled authentication URL.
   *
   * @param state an arbitrary string to be returned to the redirect URI
   *
   * @return the authentication URL
   */
  public String getAuthUrl(String state) {
    return this.getAuthUrl(state, false);
  }

  /**
   * Returns the assembled authentication URL.
   *
   * @param forcePrompt whether to force the approval prompt to show every auth
   *
   * @return the authentication URL
   */
  public String getAuthUrl(boolean forcePrompt) {
    return this.getAuthUrl(null, forcePrompt);
  }

  /**
   * Returns the assembled authentication URL.
   *
   * @return the authentication URL
   */
  public String getAuthUrl() {
    return this.getAuthUrl(null);
  }

  /**
   * Exchanges an authorization code for an access token.
   *
   * @param code the authorization code
   *
   * @return the requested access token
   *
   * @throws SmartcarException when the request is unsuccessful
   */
  public Auth exchangeCode(String code) throws SmartcarException {
    RequestBody requestBody = new FormBody.Builder()
        .add("grant_type", "authorization_code")
        .add("code", code)
        .add("redirect_uri", this.redirectUri)
        .build();

    return this.call(requestBody);
  }

  /**
   * Exchanges a refresh token for a new access token.
   *
   * @param refreshToken the refresh token
   *
   * @return the requested access token
   *
   * @throws SmartcarException when the request is unsuccessful
   */
  public Auth exchangeRefreshToken(String refreshToken) throws SmartcarException {
    RequestBody requestBody = new FormBody.Builder()
        .add("grant_type", "authorization_code")
        .add("refresh_token", refreshToken)
        .build();

    return this.call(requestBody);
  }
}