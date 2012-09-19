package com.android.livesc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.content.Context;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.view.Menu;

public class SCPreferencesActivity extends PreferenceActivity 
{
	@Override
	  protected void onCreate(Bundle savedInstanceState) 
	{
	    super.onCreate(savedInstanceState);
	    setPreferenceScreen(createPreferenceScreen());
	  }
	 
	private PreferenceScreen createPreferenceScreen() 
	 {
		PreferenceScreen root = getPreferenceManager().createPreferenceScreen(this);
		PreferenceCategory dialogBasedPrefCat = new PreferenceCategory(this);
        root.addPreference(dialogBasedPrefCat);
		// List preference
        ListPreference listPref = new ListPreference(this);
		HttpClient client = new DefaultHttpClient();
		HttpGet get = new HttpGet("http://live-scorecard.appspot.com/getAllMatches");
		HttpResponse response;
		String[] entryList = null;
		String[] entryValueList = null;
		try 
		{
			response = client.execute(get);
			if (response.getStatusLine().getStatusCode() == 200) 
			{
				HttpEntity entity = response.getEntity();
				 if (entity != null) 
				 {
					 InputStream instream = entity.getContent();
					 String result = convertStreamToString(instream);
					 if(null!=result && !result.equals(""))
					 {
						 String[] temp = result.split(",");
						 int resultCount = temp.length;
						 entryList = new String[resultCount];
						 entryValueList =  new String[resultCount];
						 for (int i = 0; i < resultCount; i++)
						 {
					        entryList[i] = entryValueList[i] = temp[i];
						 }
					 }
					 
				 }
				
			}
		}
		catch (ClientProtocolException e) 
		{
			listPref.setEnabled(false);
		}
		catch (IOException e) 
		{
			listPref.setEnabled(false);
		} 
		catch(Exception e)
		{
			listPref.setEnabled(false);
		}
		
        listPref.setEntries(entryList);
        listPref.setEntryValues(entryValueList);
        listPref.setDialogTitle(R.string.choose_match);
        listPref.setTitle(R.string.choose_match);
        listPref.setKey("list_preference");
        listPref.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				 return true;
			}
		});
        
        listPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			
			@Override
			public boolean onPreferenceClick(Preference preference) {
				return true;
			}
		});
        
        ListPreference refreshTime = new ListPreference(this);
        refreshTime.setEntries(R.array.refresh_interval);
        refreshTime.setEntryValues(R.array.refresh_interval_values);
        refreshTime.setDialogTitle(R.string.refresh_interval);
        refreshTime.setKey("refresh_interval");
        refreshTime.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				 return true;
			}
		});
        
        refreshTime.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			
			@Override
			public boolean onPreferenceClick(Preference preference) {
				return true;
			}
		});
        
        refreshTime.setTitle(R.string.refresh_interval);
        
        dialogBasedPrefCat.addPreference(refreshTime);
        dialogBasedPrefCat.addPreference(listPref);
        return root;
	 }
	
	
	public boolean isOnline() 
	{
	    ConnectivityManager cm =
	        (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

	    return cm.getActiveNetworkInfo() != null && 
	       cm.getActiveNetworkInfo().isConnectedOrConnecting();
	}
	

	public static String convertStreamToString(InputStream is) 
	{
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();
		String line = null;
		try
		{
			while ((line = reader.readLine()) != null) 
			{
				sb.append(line + "\n");
			}
		}
		catch(IOException e)
		{
			
		}
		finally 
		{
			try 
			{
				is.close();
			}
			catch (IOException e) 
			{
				e.printStackTrace();
			}
		}
		Log.i("JSON Data", sb.toString());
		return sb.toString();
	}
}
