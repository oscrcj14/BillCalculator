package com.rabbidgames.groupon;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.os.AsyncTask;

public class URLCall extends AsyncTask<URL, Void, String>
{
	public URLCaller m_Caller;

	public URLCall(URLCaller caller)
	{
		m_Caller = caller;
	}

	protected String doInBackground(URL... urls) {
		String result = "";
		for(int i = 0; i < urls.length; i++)
		{
			result = ProcessURL(urls[i]);
		}
		
		return result;
	}
	
	protected void onProgressUpdate(Integer... progress) {
	//nothing
	}
	
	public URL FormatURL(String plainTextURL)
	{	
		URL url = null;
		try
		{
			url = new URL(plainTextURL);
		}
		catch(MalformedURLException e)
		{
			m_Caller.ErrorCallback("Malformed URL");
		}
		
		return url;
	}
    
	public String ProcessURL(URL url)
	{
		String result = "";
		try
		{
			HttpClient client = new DefaultHttpClient();
			HttpGet request = new HttpGet();
			request.setURI(url.toURI());
			HttpResponse response = client.execute(request);
			BufferedReader in = new BufferedReader
			(new InputStreamReader(response.getEntity().getContent()));
			
			StringBuffer sb = new StringBuffer("");
			String line = "";
			boolean cont = true;
	
			while ((line = in.readLine()) != null && cont) {
				sb.append(line);
			}
			in.close();
			
			result = sb.toString();
		}
		catch (Exception e)
		{
			m_Caller.ErrorCallback(e.getMessage());
		}
		
		return result;
	}

	protected void onPostExecute(String result) {
		result = RemoveHTMLComments(result);
		result = RemoveLeadingWhiteSpace(result);
		if(m_Caller != null)
		{
			if(HasErrors(result))
			{
				m_Caller.ErrorCallback(result);
			}
			else
			{
				m_Caller.SuccessCallback(result);
			}
		}
	}
	
	public boolean HasErrors(String result)
	{
		return result.startsWith("Error");
	}
	
	public static String RemoveHTMLComments(String str)
	{
		String[] strArr = str.split("<!--");
		return strArr[0];
	}

	public static String RemoveLeadingWhiteSpace(String str)
	{
		while(str.startsWith(" "))
		{
			str = str.substring(1, str.length());
		}
		
		return str;
	}
}
