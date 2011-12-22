package com.gnowsis.refinder.client;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URLEncoder;

import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.commonshttp.CommonsHttpOAuthProvider;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.junit.Test;

public class OAuthTest
{
	// see <http://term.ie/oauth/example/index.php>
	String requestTokenEndpoint = "http://term.ie/oauth/example/request_token.php";
	String accessTokenEndpoint = "http://term.ie/oauth/example/access_token.php";
	
	@Test
	public void OAuthTest()
	throws Exception
	{
		CommonsHttpOAuthConsumer consumer = new CommonsHttpOAuthConsumer("key", "secret");
		CommonsHttpOAuthProvider provider = new CommonsHttpOAuthProvider(requestTokenEndpoint, accessTokenEndpoint, "http://example.com/foobar");
		
		String authorizationLink = provider.retrieveRequestToken(consumer, "");
		assertEquals("http://example.com/foobar?oauth_token=requestkey&oauth_callback=", authorizationLink);
		
		consumer.setTokenWithSecret("requestkey", "requestsecret");
		provider.retrieveAccessToken(consumer, "foobar");
		assertEquals("accesskey", consumer.getToken());
		assertEquals("accesssecret", consumer.getTokenSecret());
		
		String parameter = "q=" + URLEncoder.encode("some_physical_identifier foo bar something /?XYZ blubb %abc", "UTF-8");
		
		String testRequest = "http://term.ie/oauth/example/echo_api.php?" + parameter; 
		
		HttpGet httpGet = new HttpGet(testRequest);
		httpGet.addHeader("Accept", "application/rdf+xml");

		consumer.sign(httpGet);
		System.out.println("Signature: " + httpGet.getFirstHeader("Authorization").getValue());
		
		HttpResponse response = new DefaultHttpClient().execute(httpGet);
		
		String responseString = new BufferedReader(new InputStreamReader(response.getEntity().getContent())).readLine();
		assertEquals(parameter, responseString);
	}
}
