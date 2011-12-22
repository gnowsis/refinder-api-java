package com.gnowsis.refinder.client;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Before;
import org.junit.Test;

public class OAuthCallbackTest
{
	private Log log = LogFactory.getLog(getClass());
	
	private String serviceUrl;
	private Refinder refinder;

	@Before
	public void setUp()
	{
		this.serviceUrl = "http://apitest.gnowsis.com";
		
//		this.refinder = new Refinder(this.serviceUrl, "g2htwJHdgHDYYrRLrk", "qhfNdrHFCbPQ6pWnFEFWgxtnJhrBm4kF");
		this.refinder = new Refinder(this.serviceUrl, "Zhz14UF54BfYzpBbHX", "yhG3IMhpma5T706nuVwn7X8I0bSARzLv");
	}
	
	@Test
	public void testCallbackUrl()
	{
//		this.refinder.initAuthentication("http://www.gnowsis.com/playground/refinder-oauth-callback.php");
		this.refinder.initAuthentication("http://bcc.ajado.net/auth/connect");
	}

}
