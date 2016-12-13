package com.smartcar.sdk;

import okhttp3.Request;
import okhttp3.Response;
import okhttp3.OkHttpClient;

import java.io.IOException;

public final class Util {

  private static final OkHttpClient client = new OkHttpClient();

  /**
   * Calls the request, and returns the response on success. 
   * If the call fails, the code from the error is used to throw 
   * an appropriate exception from Exceptions. 
   * 
   * @param  request        
   *     
   * @return The response body from the API, which is a JSON string.
   * 
   *         If the request is a POST to /oauth/token, the response 
   *         is an access object that looks something like this:
   *         
   *         {
   *           "access_token": "...",
   *           "token_type": "Bearer",
   *           "expires_in": 7200,
   *           "refresh_token": "...",
   *         }
   *         
   *         If the request is a GET from /vehicles/:id/endpoint, 
   *         the response is a vehicle data object that looks 
   *         something like this:
   *         
   *         { "isTriggered": false }
   *
   *         If the request is a POST to /vehicles/:id/endpoint 
   *         with a payload specific to the endpoint, the response 
   *         is a simple status message that always looks like this:
   *         
   *         { "status": "success" }
   *                                
   * @throws <Exceptions.SmartcarException>
   */
  static String call(Request request) 
    throws Exceptions.SmartcarException {
    Response response;
    String body;
    try {
      response = client.newCall(request).execute();
      body = response.body().string();
    } catch (IOException e) {
      throw new Exceptions.SmartcarException(e.getMessage());
    }

    if (!response.isSuccessful()) {
      response.close();
      switch(response.code()) {
        case 400: 
          throw new Exceptions.ValidationException(body);
        case 401: 
          throw new Exceptions.AuthenticationException(body);
        case 403: 
          throw new Exceptions.PermissionException(body);
        case 404: 
          throw new Exceptions.ResourceNotFoundException(body);
        case 409: 
          throw new Exceptions.StateException(body);
        case 429: 
          throw new Exceptions.RateLimitingException(body);
        case 430: 
          throw new Exceptions.MonthlyLimitExceeded(body);
        case 500: 
          throw new Exceptions.ServerException(body);
        case 501: 
          throw new Exceptions.NotCapableException(body);
        case 504: 
          throw new Exceptions.GatewayTimeoutException(body);
      }
    } else {
      response.close();
      return body;
    }
    return null;
  }

  /**
   * Join an array of strings with spaces.
   *
   * @param  strings
   * @return Joined array
   */
  static String join(String[] strings) {
    if (strings.length == 0) 
      return "";
    if (strings.length == 1)
      return strings[0];

    String joinedString = "";
    for (int i=0; i<strings.length - 1; i++) {
      if (strings[i] != null)
        joinedString += strings[i] + " ";
    }
    joinedString += strings[strings.length - 1];
    return joinedString;
  }
}