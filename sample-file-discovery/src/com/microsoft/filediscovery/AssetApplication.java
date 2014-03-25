/*******************************************************************************
 * Copyright (c) Microsoft Open Technologies, Inc.
 * All Rights Reserved
 * See License.txt in the project root for license information. 
 ******************************************************************************/
package com.microsoft.filediscovery;

import java.util.HashMap;
import java.util.Map;
import android.app.Activity;
import android.app.Application;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.widget.Toast;
import com.microsoft.adal.AuthenticationCallback;
import com.microsoft.adal.AuthenticationContext;
import com.microsoft.adal.AuthenticationResult;
import com.microsoft.office365.Credentials;
import com.microsoft.office365.LogLevel;
import com.microsoft.office365.Logger;
import com.microsoft.office365.OfficeClient;
import com.microsoft.office365.OfficeFuture;
import com.microsoft.office365.files.FileClient;
import com.microsoft.office365.http.OAuthCredentials;

// TODO: Auto-generated Javadoc
/**
 * The Class AssetApplication.
 */
public class AssetApplication extends Application {

	/** The m credentials. */
	private Map<String, Credentials> mCredentials = new HashMap<String, Credentials>();

	/* (non-Javadoc)
	 * @see android.app.Application#onCreate()
	 */
	@Override
	public void onCreate() {

		Log.d("Asset Management", "onCreate");
		super.onCreate();
	}

	/**
	 * Gets the credentials.
	 *
	 * @return the credentials
	 */
	public Credentials getCredentials(String resourceId) {
		return mCredentials.get(resourceId);
	}

	/**
	 * Sets the credentials.
	 *
	 * @param credentials the new credentials
	 */
	public void setCredentials(Map<String,Credentials> credentials) {
		mCredentials = credentials;
	}

	/**
	 * Handle error.
	 *
	 * @param throwable the throwable
	 */
	public void handleError(Throwable throwable) {
		Toast.makeText(this, throwable.getMessage(), Toast.LENGTH_LONG).show();
		Log.e("Asset", throwable.toString());
	}

	/**
	 * Authenticate.
	 *
	 * @param activity the activity
	 * @return the office future
	 */
	public OfficeFuture<Map<String,Credentials>> authenticate(Activity activity,final String resourceId) {
		final OfficeFuture<Map<String,Credentials>> result = new OfficeFuture<Map<String,Credentials>>();

		getAuthenticationContext(activity).acquireToken(
				activity, resourceId,Constants.CLIENT_ID,Constants.REDIRECT_URL,"",
				new AuthenticationCallback<AuthenticationResult>() {

					@Override
					public void onSuccess(AuthenticationResult authenticationResult) {
						//once succeeded we create a credentials instance using the token from ADAL
						mCredentials.put(resourceId, new OAuthCredentials(authenticationResult.getAccessToken())); 

						result.setResult(mCredentials);
					}

					@Override
					public void onError(Exception exc) {
						result.triggerError(exc);
					}
				});

		return result;
	}

	public AuthenticationContext context = null;

	/**
	 * Gets AuthenticationContext for AAD.
	 *
	 * @return authenticationContext, if successful
	 */
	public AuthenticationContext getAuthenticationContext(Activity activity) {

		try {
			context = new AuthenticationContext(activity, Constants.AUTHORITY_URL, false);
		} catch (Throwable t) {
			Log.e("Asset", t.getMessage());
		}
		return context;
	}

	/**
	 * Clear preferences.
	 */
	public void clearPreferences() {
		// mPreferences.clear();
		CookieSyncManager syncManager = CookieSyncManager.createInstance(this);
		if (syncManager != null) {
			CookieManager cookieManager = CookieManager.getInstance();
			cookieManager.removeAllCookie();
		}
	}

	/**
	 * Gets the current list client.
	 *
	 * @return the current list client
	 */
	public FileClient getCurrentFileClient(String resourceId, String endpoint) {
		Credentials credentials = getCredentials(resourceId);

		return new FileClient(endpoint, "/",
				credentials, new Logger() {

			@Override
			public void log(String message, LogLevel level) {
				Log.d("Asset", message);
			}
		});
	}

	public OfficeClient getOfficeClient(String resourceId) {
		return new OfficeClient(getCredentials(resourceId));
	}
}