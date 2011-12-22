package com.gnowsis.refinder.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.UUID;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
import org.junit.Test;

import com.gnowsis.refinder.client.ns.DSTYPE;
import com.gnowsis.refinder.client.ns.NAO;
import com.gnowsis.refinder.client.ns.NCO;
import com.gnowsis.refinder.client.ns.PIMO;
import com.gnowsis.refinder.client.ns.RDFS;

public class RefinderTest
{
	private Log log = LogFactory.getLog(getClass());
	
	private String serviceUrl;
	
	private Refinder refinder;
	
	@Before
	public void setUp()
	{
		HttpClient httpClient = null;
		
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
			
			Scheme httpsScheme = new Scheme("https", sf, 443);
			SchemeRegistry schemeRegistry = new SchemeRegistry();
			schemeRegistry.register(httpsScheme);

			HttpParams params = new BasicHttpParams();
			ClientConnectionManager cm = new SingleClientConnManager(params, schemeRegistry);
			httpClient = new DefaultHttpClient(cm, params);
		}
		catch(Exception e)
		{
			fail(e.getMessage());
		}
	
		this.serviceUrl = "https://apitest.i.gnowsis.com";
		//this.serviceUrl = "http://localhost:8000";

		this.refinder = new Refinder(this.serviceUrl, "g2htwJHdgHDYYrRLrk", "qhfNdrHFCbPQ6pWnFEFWgxtnJhrBm4kF", httpClient);
		this.refinder.setAccessToken("KW3YJV9qzJCpfHM3N7", "hMBSVdyDuzDtwExmWLYddDPhjKW7X8Aa");
	}
	
	@Test
	public void testGetAllThings()
	{
		Collection<Thing> things = this.refinder.getAllThings();
		assertNotNull(things);
		assertTrue(things.size() > 0);
		for(Thing t: things)
		{
			assertNotNull(t.getType());
			assertNotNull(t.getUri());
		}
	}
	
	@Test
	public void testGetOneThing()
	{
		Thing thing = this.refinder.getThing(UUID.fromString("ccfea118-b845-11df-ba44-a4badbcc28b4"));
		assertNotNull(thing);
		assertEquals(UUID.fromString("ccfea118-b845-11df-ba44-a4badbcc28b4"), UUID.fromString(Util.findUuid(thing.getUri().toString())));
		assertEquals(PIMO.Project, thing.getType());
		assertEquals("VICEF", thing.getAttribute(RDFS.label));
		assertEquals(URI.create(this.serviceUrl + "/users/claudia/#u"), thing.getAttribute(PIMO.creator));
		assertTrue(thing.getAttribute(NAO.created) instanceof Calendar);

	}

	@Test
	public void testCreateThing()
	{
		Thing thing = new Thing(null, PIMO.Person);
		assertNull(thing.getUri());
		thing.setAttribute(RDFS.label, "A nice person");
		thing.setAttribute(NCO.birthDate, new GregorianCalendar());
		assertNull(thing.getAttribute(PIMO.creator));
		assertNull(thing.getAttribute(NAO.created));
		assertNull(thing.getAttribute(NAO.lastModified));

		thing = this.refinder.createThing(thing);
		assertNotNull(thing);
		assertNotNull(thing.getUri());
		assertEquals(PIMO.Person, thing.getType());
		assertEquals("A nice person", thing.getAttribute(RDFS.label));
		assertEquals(URI.create(this.serviceUrl + "/users/claudia/#u"), thing.getAttribute(PIMO.creator));
		assertTrue(thing.getAttribute(NAO.created) instanceof Calendar);
		assertTrue(thing.getAttribute(NAO.lastModified) instanceof Calendar);
	}

	@Test
	public void testUpdateThing()
	{
		Thing thing = new Thing(null, PIMO.Person);
		assertNull(thing.getUri());
		thing.setAttribute(RDFS.label, "A nice person");
		thing.setAttribute(NCO.birthDate, new GregorianCalendar());
		thing = this.refinder.createThing(thing);
		
		URI uri = thing.getUri();
		assertNotNull(uri);
		
		UUID uuid = UUID.fromString(Util.findUuid(uri.toString()));
		
		Thing thing2 = refinder.getThing(uuid);
		assertNotNull(thing2);
		assertEquals(uri, thing2.getUri());
		assertEquals("A nice person", thing.getAttribute(RDFS.label));
		
		thing2.setAttribute(RDFS.label, "An even nicer person");
		assertTrue(this.refinder.storeThing(thing2));
		assertEquals(uri, thing2.getUri());
		
		Thing thing3 = refinder.getThing(uuid);
		assertNotNull(thing3);
		assertEquals(uri, thing3.getUri());
		assertEquals("An even nicer person", thing3.getAttribute(RDFS.label));
	}

	@Test
	public void testSearch()
	{
		List<Thing> things = this.refinder.searchThings("cid");
		assertNotNull(things);
		assertTrue(things.size() > 0);
		for(Thing t: things)
		{
			assertNotNull(t);
		}
	}
	
	@Test
	public void testDataObjects()
	{
		Thing thing = new Thing(null, PIMO.Person);
		assertNull(thing.getUri());
		thing.setAttribute(RDFS.label, "Testthing for data objects");
		thing.setAttribute(NCO.birthDate, new GregorianCalendar());
		thing = this.refinder.createThing(thing);
	
		// use a Refinder URI as data object
		String dataObjectUri1 = "refinder::foobar" + Math.random();
		this.refinder.setDataObject(thing, dataObjectUri1);
		
		List<Thing> things = this.refinder.findThingsByDataObject(dataObjectUri1);
		assertNotNull(things);
		assertTrue(things.size() == 1);
		assertEquals(thing.getUri(), things.get(0).getUri());

		// use a HTTP URI as data object
		String dataObjectUri2 = "http://some-site.com/example/clickme" + Math.random();
		this.refinder.setDataObject(thing, dataObjectUri2);
		
		things = this.refinder.findThingsByDataObject(dataObjectUri1);
		assertNotNull(things);
		assertTrue(things.size() == 0);

		things = this.refinder.findThingsByDataObject(dataObjectUri2);
		assertNotNull(things);
		assertTrue(things.size() == 1);
		assertEquals(thing.getUri(), things.get(0).getUri());
	}
	
	
	@Test
	public void testDataSources()
	{
		UUID dsUUID = this.refinder.createDataSource(DSTYPE.OutlookStore, "test store", "foo bar");
		assertNotNull(dsUUID);
	}
	
	
	@Test
	public void testRelated()
	{
		Thing thing1 = new Thing(null, PIMO.Person);
		thing1.setAttribute(RDFS.label, "Testthing 1 for data objects");
		thing1.setAttribute(NCO.birthDate, new GregorianCalendar());
		thing1 = this.refinder.createThing(thing1);
		assertNotNull(thing1.getUri());
		
		Thing thing2 = new Thing(null, PIMO.Person);
		thing2.setAttribute(RDFS.label, "Testthing 2 for data objects");
		thing2.setAttribute(NCO.birthDate, new GregorianCalendar());
		thing2 = this.refinder.createThing(thing2);
		assertNotNull(thing2.getUri());

		Thing thing3 = new Thing(null, PIMO.Person);
		thing3.setAttribute(RDFS.label, "Testthing 3 for data objects");
		thing3.setAttribute(NCO.birthDate, new GregorianCalendar());
		thing3 = this.refinder.createThing(thing3);
		assertNotNull(thing3.getUri());

		// create relation between thing1 and thing2
		Annotation annotation1 = new Annotation(null, thing1.getUri(), PIMO.isRelated);
		annotation1.setValue(thing2.getUri());
		annotation1 = this.refinder.createAnnotation(annotation1);
		assertNotNull(annotation1);
		assertNotNull(annotation1.getUri());
		assertEquals(thing2.getUri(), annotation1.getValue());
		
		// check if the relation is in the annotations
		
		UUID thing1UUID = UUID.fromString(Util.findUuid(thing1.getUri().toString()));
		
		thing1 = this.refinder.getThing(thing1UUID);
		assertEquals(1, thing1.getAnnotations().size());
		assertEquals(annotation1.getUri(), thing1.getAnnotations().iterator().next().target);
		
		// check if the thing is in the thing's related things
		Collection<Thing> relatedThings = this.refinder.getRelatedThings(thing1);
		assertEquals(1, relatedThings.size());
		for(Thing t: relatedThings)
			System.out.println("Related thing: " + t);
		assertTrue(relatedThings.contains(thing2));
		
		// change annotation to point to thing3
		annotation1.setValue(thing3.getUri());
		assertTrue(this.refinder.storeAnnotation(annotation1));
		assertEquals(thing3.getUri(), annotation1.getValue());

		// check if the relation was updated in the annotations
		thing1 = this.refinder.getThing(thing1UUID);
		assertEquals(1, thing1.getAnnotations().size());
		assertEquals(annotation1.getUri(), thing1.getAnnotations().iterator().next().target);
	}
	
	
	@Test
	public void testAnalyzerText()
	{
		Thing thing = new Thing(null, PIMO.Topic);
		thing.setAttribute(RDFS.label, "testthing for analyzer (Java client test -- DELETE ME)");
		thing = this.refinder.createThing(thing);
		assertNotNull(thing.getUri());
		
		String plainText = "<div id=\"node-62\" class=\"node node-teaser node-type-blog\"><div class=\"node-inner\">\n" + 
				"\n" + 
				"  \n" + 
				"      <h2 class=\"title\">\n" + 
				"      <a href=\"/about/blog/2010/10/20/gnowsis-naming-competition-thank-you\" title=\"Gnowsis Naming Competition - thank you!\">Gnowsis Naming Competition - thank you!</a>\n" + 
				"    </h2>\n" + 
				"  \n" + 
				"  \n" + 
				"      <div class=\"meta\">\n" + 
				"\n" + 
				"              <div class=\"submitted\">\n" + 
				"          Submitted by Leo Sauermann on Wed, 10/20/2010 - 11:21                      <div class=\"terms terms-inline\"> in <ul class=\"links inline\"><li class=\"taxonomy_term_19 first last\"><a href=\"/about/category/tags/marketing\" rel=\"tag\" title=\"\">marketing</a></li>\n" + 
				"</ul></div>\n" + 
				"                  </div>\n" + 
				"      \n" + 
				"    </div>\n" + 
				"  \n" + 
				"  <div class=\"content\">\n" + 
				"    <p>Dear Gnowsis Fan,</p>\n" + 
				"\n" + 
				"<p>The naming competition for Refinder is over, polls closed on Sunday evening 8pm Vienna time.<br />\n" + 
				"<a href=\"http://cluug.uservoice.com/forums/77753-naming-competition\">http://cluug.uservoice.com/forums/77753-naming-competition</a></p>\n" + 
				"<p>Liwi.me is the winner with 48 votes, <br />\n" + 
				"Gratulations to Melvin Carvalho for submitting this idea!<br />\n" + 
				"&nbsp;</p>\n" + 
				"  </div>\n" + 
				"\n"; 

		this.refinder.analyzeContent(thing, plainText);
		
		// give the extractor some time
		this.log.debug("Waiting for 5 seconds to give indexer time ...");
		try { Thread.sleep(5000); } catch (Exception e) {}
		
		List<Thing> results = this.refinder.searchThings("competition");
		
		boolean resultFound = false;
		for(Thing t: results)
			if(t.getUri().equals(thing.getUri()))
				resultFound = true;
		assertTrue(resultFound);
	}
	
	@Test
	public void testAnalyzerBinary()
	throws IOException
	{
		InputStream data = RefinderTest.class.getResourceAsStream("/edam-sync.pdf");
		assertNotNull(data);

		Thing thing = new Thing(null, PIMO.Topic);
		thing.setAttribute(RDFS.label, "testthing for analyzer (Java client test -- DELETE ME)");
		thing = this.refinder.createThing(thing);
		assertNotNull(thing.getUri());
		
		this.refinder.analyzeContent(thing, data);
		
		// give the extractor some time
		this.log.debug("Waiting for 5 seconds to give indexer time ...");
		try { Thread.sleep(10000); } catch (Exception e) {}
		
		List<Thing> results = this.refinder.searchThings("edam");
		
		boolean resultFound = false;
		for(Thing t: results)
			if(t.getUri().equals(thing.getUri()))
				resultFound = true;
		assertTrue(resultFound);
	}
	
	@Test
	public void testFindDatasourceByPhysical()
	{
		String physical="some_physical_identifier foo bar something /?XYZ blubb %abc def \\ öölsdkjf";
//		String physical="some_physical_identifier foo bar something /?XYZ blubb %bc";

		UUID dsUUID = this.refinder.createDataSource(URI.create("http://some/uri"), "test datasource with backslash (DELETE ME)", physical);
		assertNotNull(dsUUID);
		
		assertTrue(this.refinder.findDataSourceByPhysical(physical).contains(dsUUID));
		assertFalse(this.refinder.findDataSourceByPhysical("krik krak").contains(dsUUID));
	}
	
	@Test
	public void _tempTest()
	{
		String physical="some_physical_identifier foo bar something /?XYZ blubb %bc";
		this.refinder.findDataSourceByPhysical(physical);
	}
	
	@Test
	public void testTransientThing()
	{
		Thing thing1 = new Thing(null, PIMO.Person);
		thing1.setAttribute(RDFS.label, "This is a transient thing");
		thing1.setAttribute(NCO.birthDate, new GregorianCalendar());
		Thing thing2 = this.refinder.createTransientThing(thing1);
		assertNotNull(thing2);
		assertNotNull(thing2.getUri());
		
		try
		{
			// we cannot GET the thing => throws NotFound
			this.refinder.getThing(UUID.fromString(Util.findUuid(thing2.getUri().toString())));
			fail();
		}
		catch(RefinderException e)
		{
			assertEquals(RefinderException.Status.NotFound, e.status);
		}
		
		// but we can retrieve recommendations
		System.out.println(this.refinder.getRecommendations(thing2));
		

	}
}
