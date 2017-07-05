//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package fr.theshark34.openauth;

import com.google.gson.Gson;
import fr.theshark34.openauth.model.AuthAgent;
import fr.theshark34.openauth.model.AuthError;
import fr.theshark34.openauth.model.request.*;
import fr.theshark34.openauth.model.response.AuthResponse;
import fr.theshark34.openauth.model.response.RefreshResponse;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class Authenticator {
	public static final String MOJANG_AUTH_URL = "https://authserver.mojang.com/";
	private String authURL;
	private AuthPoints authPoints;

	public Authenticator(String authURL, AuthPoints authPoints) {
		this.authURL = authURL;
		this.authPoints = authPoints;
	}

	public AuthResponse authenticate(AuthAgent agent, String username, String password, String clientToken) throws AuthenticationException {
		AuthRequest request = new AuthRequest(agent, username, password, clientToken);
		return (AuthResponse)this.sendRequest(request, AuthResponse.class, this.authPoints.getAuthenticatePoint());
	}

	public RefreshResponse refresh(String accessToken, String clientToken) throws AuthenticationException {
		RefreshRequest request = new RefreshRequest(accessToken, clientToken);
		return (RefreshResponse)this.sendRequest(request, RefreshResponse.class, this.authPoints.getRefreshPoint());
	}

	public void validate(String accessToken) throws AuthenticationException {
		ValidateRequest request = new ValidateRequest(accessToken);
		this.sendRequest(request, (Class)null, this.authPoints.getValidatePoint());
	}

	public void signout(String username, String password) throws AuthenticationException {
		SignoutRequest request = new SignoutRequest(username, password);
		this.sendRequest(request, (Class)null, this.authPoints.getSignoutPoint());
	}

	public void invalidate(String accessToken, String clientToken) throws AuthenticationException {
		InvalidateRequest request = new InvalidateRequest(accessToken, clientToken);
		this.sendRequest(request, (Class)null, this.authPoints.getInvalidatePoint());
	}

	private Object sendRequest(Object request, Class<?> model, String authPoint) throws AuthenticationException {
		Gson gson = new Gson();
		String response = null;

		try {
			response = this.sendPostRequest(this.authURL + authPoint, gson.toJson(request));
		} catch (IOException var8) {
			AuthError errorModel = (AuthError)gson.fromJson(var8.getMessage(), AuthError.class);
			if(errorModel == null) {
				errorModel = new AuthError("", var8.getMessage(), "");
			}

			throw new AuthenticationException(errorModel);
		}

		return model != null?gson.fromJson(response, model):null;
	}

	private String sendPostRequest(String url, String json) throws IOException {
		URL serverURL = new URL(url);
		HttpURLConnection connection = (HttpURLConnection)serverURL.openConnection();
		connection.setRequestMethod("POST");
		connection.setDoOutput(true);
		connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/57.0.2987.98 Safari/537.36");
		connection.addRequestProperty("Content-Type", "application/json");
		DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
		wr.writeBytes(json);
		wr.flush();
		wr.close();
		connection.connect();
		int responseCode = connection.getResponseCode();
		if(responseCode == 204) {
			connection.disconnect();
			return null;
		} else {
			InputStream is = null;
			if(responseCode == 200) {
				is = connection.getInputStream();
			} else {
				is = connection.getErrorStream();
			}

			String response = null;
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			response = br.readLine();

			try {
				br.close();
			} catch (IOException var11) {
				var11.printStackTrace();
			}

			connection.disconnect();
			if(responseCode == 200) {
				return response;
			} else {
				throw new IOException(response);
			}
		}
	}
}
