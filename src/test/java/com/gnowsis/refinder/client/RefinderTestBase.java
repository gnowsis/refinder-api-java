package com.gnowsis.refinder.client;

import static org.junit.Assert.fail;

import java.security.SecureRandom;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.client.HttpClient;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.AllowAllHostnameVerifier;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.junit.Before;

public abstract class RefinderTestBase
{
	protected String serviceUrl;
	protected Refinder refinder;
	

	@Before
	public void setUp()
	{
		//this.serviceUrl = "https://apitest.i.gnowsis.com";
		this.serviceUrl = "http://localhost:8000";

		HttpClient httpClient = new DefaultHttpClient();
		
		// check if we use https 
		if(this.serviceUrl.startsWith("https://"))
		{
			try
			{
				SSLContext sslContext = SSLContext.getInstance("SSL");
	
				// set up a TrustManager that trusts everything -- used to test against a server
				// that has invalid / self-signed certificates
				sslContext.init(null, new TrustManager[] { new X509TrustManager()
				{
					public X509Certificate[] getAcceptedIssuers() { return null; }
					public void checkClientTrusted(X509Certificate[] certs, String authType) {}
					public void checkServerTrusted(X509Certificate[] certs, String authType) {}
				} }, new SecureRandom());
	
				SSLSocketFactory sf = new SSLSocketFactory(sslContext);
				
				// we even accept certificates that do not match the hostname
				sf.setHostnameVerifier(new AllowAllHostnameVerifier());
				
				SchemeRegistry schemeRegistry = new SchemeRegistry();
	
				Scheme httpsScheme = new Scheme("https", sf, 443);
				schemeRegistry.register(httpsScheme);
				
				HttpParams params = new BasicHttpParams();
				ClientConnectionManager cm = new SingleClientConnManager(params, schemeRegistry);
				httpClient = new DefaultHttpClient(cm, params);
			}
			catch(Exception e)
			{
				fail(e.getMessage());
			}
		}

		this.refinder = new Refinder(this.serviceUrl, "g2htwJHdgHDYYrRLrk", "qhfNdrHFCbPQ6pWnFEFWgxtnJhrBm4kF", httpClient);
		this.refinder.setAccessToken("KW3YJV9qzJCpfHM3N7", "hMBSVdyDuzDtwExmWLYddDPhjKW7X8Aa");
	}
}
