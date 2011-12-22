package com.gnowsis.refinder.client;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Before;
import org.junit.Test;

import com.gnowsis.refinder.client.ns.PIMO;
import com.gnowsis.refinder.client.ns.RDFS;

public class Issue929Test
{
	private Log log = LogFactory.getLog(getClass());

	private String serviceUrl;
	private Refinder refinder;

	@Before
	public void setUp()
	{
		this.serviceUrl = "http://localhost:8000";
		
		this.refinder = new Refinder(this.serviceUrl, "g2htwJHdgHDYYrRLrk", "qhfNdrHFCbPQ6pWnFEFWgxtnJhrBm4kF");
		this.refinder.setAccessToken("KW3YJV9qzJCpfHM3N7", "hMBSVdyDuzDtwExmWLYddDPhjKW7X8Aa");
	}
	
	@Test
	public void testIssue929()
	{
		Thing t1 = new Thing(null, PIMO.Topic);
		t1.setAttribute(RDFS.label, "Thing t1");
		t1 = this.refinder.createThing(t1);
		
		Thing t2 = new Thing(null, PIMO.Topic);
		t2.setAttribute(RDFS.label, "Thing t2");
		t2 = this.refinder.createThing(t2);
		
		this.refinder.deleteThing(t1);
		this.refinder.deleteThing(t2);
	}
}
