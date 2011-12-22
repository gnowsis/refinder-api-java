package com.gnowsis.refinder.client;

import java.io.InputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import oauth.signpost.OAuthConsumer;
import oauth.signpost.OAuthProvider;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.commonshttp.CommonsHttpOAuthProvider;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import oauth.signpost.exception.OAuthNotAuthorizedException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import com.gnowsis.refinder.client.ns.DATASOURCE;
import com.gnowsis.refinder.client.ns.PIMO;
import com.gnowsis.refinder.client.ns.RDFS;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.RDF;

/**
 * Copyright 2010 Gnowsis.com
 * 
 * Refinder client API
 * 
 * @author bernhard.schandl
 * @author leo.sauermann
 *
 */
public class Refinder
{
	private final Log			log						= LogFactory.getLog(getClass());

	// some constants ...
	public final static String		REQUEST_TOKEN			= "request_token";
	public final static String		REQUEST_TOKEN_SECRET	= "request_token_secret";
	public final static String		ACCESS_TOKEN			= "access_token";
	public final static String		ACCESS_TOKEN_SECRET	= "access_token_secret";
	public final static String		AUTHORIZE_URL			= "authorize_url";

	// serviceUrl will be given by the client application
	private String				serviceUrl;

	// these URLs will be built by the constructor by appending to the serviceUrl
	private String				requestUrl;
	private String				accessUrl;
	private String				authorizeUrl;

	// consumerKey and consumerSecret are provided by the application
	private String				consumerKey;
	private String				consumerSecret;

	// refinder status
	private boolean			online					= false;
	private boolean			authenticated			= false;

	// OAuth
	private OAuthConsumer	consumer;
	private OAuthProvider	provider;

	/**
	 * Instantiates a Refinder client.
	 * 
	 * @param serviceUrl
	 *           The full URL of the Refinder web service, including "http://" and
	 *           without (!) trailing slash. Usually this will be
	 *           "http://www.refinder.info" or (for development)
	 *           "http://apitest.gnowsis.com".
	 * @param consumerKey
	 *           The OAuth consumer key of your application, provided by Gnowsis.
	 * @param consumerSecret
	 *           The OAuth consumer secret of your application, provided by
	 *           Gnowsis.
	 */
	public Refinder(String serviceUrl, String consumerKey, String consumerSecret)
	{
		this.serviceUrl = serviceUrl;
		this.consumerKey = consumerKey;
		this.consumerSecret = consumerSecret;

		this.requestUrl = this.serviceUrl + "/oauth/request_token/";
		this.accessUrl = this.serviceUrl + "/oauth/access_token/";
//		this.authorizeUrl = this.serviceUrl + "/oauth/authorize/?oauth_token={0}";
		this.authorizeUrl = this.serviceUrl + "/oauth/authorize/";

		this.consumer = new CommonsHttpOAuthConsumer(this.consumerKey, this.consumerSecret);
		this.provider = new CommonsHttpOAuthProvider(this.requestUrl, this.accessUrl, this.authorizeUrl);
//		{
//			@Override
//			protected HttpRequest createRequest(String endpointUrl) 
//			throws MalformedURLException, IOException
//			{
//		      HttpGet request = new HttpGet(endpointUrl);
////		      request.addHeader("Content-Type", "application/x-www-form-urlencoded");
//		      return new HttpRequestAdapter(request);			
//		   }
//		};
	}

	/**
	 * Returns the URL of a HTML-based view of a thing's sidebar. Can be used to
	 * embed Refinder functionality in desktop applications. NOTE: This URL works
	 * only if the Webbrowser used to display it is logged in at the Refinder web
	 * service. To log in via OAuth, call first the URL returned by
	 * <see>getOauthLoginUrl()</see>
	 * 
	 * @param thingUUID
	 *           The UUID of the thing to be displayed.
	 * @return URL that can be opened by a WebBrowser widget.
	 */
	public String getEmbedDisplayUrl(String thingUUID)
	{
		return this.serviceUrl + "/client/view/things/" + thingUUID + "/";
	}

	/**
	 * Returns the service URL.
	 * 
	 * @return the service URL -- usually this will be "http://www.refinder.info".
	 */
	public String getServiceUrl()
	{
		return this.serviceUrl;
	}

	/**
	 * Returns the full URI of an object. Objects can be Things, Annotations, DataSources, etc.
	 * @param o
	 * @return
	 */
//	public URI getObjectURI(Object o)
//	{
//		return URI.create(ThingSerializer.getObjectUri(this.serviceUrl, o));
//	}
	
	/**
	 * Returns a URL that can be used to login a web browser without requiring
	 * user credentials. Use this to authenticate e.g., a WebBrowser widget that
	 * can be embedded in any desktop application.
	 * 
	 * @return A URL that can be accessed by a WebBrowser widget.
	 */
	public String getOauthLoginUrl()
	{
		try
		{
			return this.consumer.sign(this.serviceUrl + "/oauth/login/");
		}
		catch (Exception e)
		{
			this.log.warn("Problem when returning login URL: " + e.getMessage());
			return null;
		}
	}

	/**
	 * Initiates OAuth authentication with the Refinder web service.
	 * 
	 * @return a dictionary containing a request token, a request token secret,
	 *         and an authorization URL to which the user must be directed in
	 *         order to authenticate the client application. Use
	 *         <code>Refinder.REQUEST_TOKEN</code>,
	 *         <code>Refinder.REQUEST_TOKEN_SECRET</code>, and
	 *         <code>Refinder.AUTHORIZE_URL</code> to retrieve the data from the
	 *         returned Map.
	 */
	public Map<String, String> initAuthentication()
	{
		try
		{
			HashMap<String, String> results = new HashMap<String, String>();

			// FIXME replace null with OAuth.OUT_OF_BAND if OAuth 1.0a is supported
			// using empty string instead of null (which would be correct) because of bug in signpost
			// see <http://code.google.com/p/oauth-signpost/issues/detail?id=56>
			String authorizationLink = this.provider.retrieveRequestToken(this.consumer, ""); 

			results.put(REQUEST_TOKEN, this.consumer.getToken());
			results.put(REQUEST_TOKEN_SECRET, this.consumer.getTokenSecret());
			results.put(AUTHORIZE_URL, authorizationLink);

			this.online = true;

			return results;
		}
		catch (Exception e)
		{
			throw new RefinderException(RefinderException.Status.Offline, e);
		}
	}

	/**
	 * Continues authentication after the user has authorized the consumer. This
	 * method requires the request token and request token secret that has been
	 * obtained from <code>initAuthentication()</code> as well as the
	 * verification code entered by the user.
	 * 
	 * @param token
	 *           the request token (obtained from
	 *           <code>initAuthentication()</code>)
	 * @param tokenSecret
	 *           the request token secret (obtained from
	 *           <code>initAuthentication()</code>
	 * @param verificationCode
	 *           An authorized and valid access token and access token secret.
	 * @return a Map containing the authentification data.
	 *           Use <code>Refinder.ACCESS_TOKEN</code> and
	 *           <code>Refinder.ACCESS_TOKEN_SECRET</code> to retrieve the data from
	 *           the returned Map.
	 */
	public Map<String, String> continueAuthentication(String token, String tokenSecret, String verificationCode)
	{
		try
		{
			HashMap<String, String> results = new HashMap<String, String>();

			// exchange the request token for an access token
			this.consumer.setTokenWithSecret(token, tokenSecret);

			this.provider.retrieveAccessToken(this.consumer, verificationCode);

			results.put(ACCESS_TOKEN, this.consumer.getToken());
			results.put(ACCESS_TOKEN_SECRET, this.consumer.getTokenSecret());

			this.authenticated = true;
			this.online = true;

			return results;
		}
		catch (Exception e)
		{
			throw new RefinderException(RefinderException.Status.Offline, e);
		}
	}
	
	public Map<String, String> initAuthentication(String callbackUrl)
	{
		try
		{
			String authorizationLink = this.provider.retrieveRequestToken(this.consumer, callbackUrl);
			this.log.debug("Authorization link: " + authorizationLink);
		}
		catch (OAuthMessageSignerException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (OAuthNotAuthorizedException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (OAuthExpectationFailedException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (OAuthCommunicationException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		return null;
	}

	/**
	 * Sets an access token if it is already known. The access token must be
	 * authenticated and valid, and it must correspond to the application's
	 * consumer key and secret.
	 * 
	 * @param accessToken
	 *           the access token (retrieved from
	 *           <code>continueAuthentication()</code>)
	 * @param accessTokenSecret
	 *           the access token secret (retrieved from
	 *           <code>continueAuthentication()</code>)
	 */
	public void setAccessToken(String accessToken, String accessTokenSecret)
	{
		this.consumer.setTokenWithSecret(accessToken, accessTokenSecret);
		this.authenticated = true;
	}

	/**
	 * Returns whether the client is authenticated or not.
	 * 
	 * @return
	 */
	public Boolean isAuthenticated()
	{
		return this.authenticated;
	}

	/**
	 * Returns whether the last operation was successful. This method performs no
	 * check and is therefore not reliable.
	 * 
	 * @return
	 */
	public Boolean isOnline()
	{
		return this.online;
	}

   public String getAuthenticatedUsername()
   {
       this.log.debug("getAuthenticatedUsername()");
       try
       {
           Model g = this.getGraph(this.serviceUrl + "/data/admin/userinfo/");

           String username = null;
           StmtIterator i = g.listStatements(null, g.getProperty("http://www.cluug.com/admin#username"), (Literal) null);
           while(i.hasNext())
           {
               username = i.nextStatement().getLiteral().getString();
           }

           return username;
       }
       catch (Exception e)
       {
           this.log.debug("Exception: " + e.getMessage(), e);
           return null;
       }
   }

	
	/**
	 * Returns all things of the user. Usually the number of retrieved results is
	 * limited by the server.
	 * 
	 * @return A list of <code>Thing</code>s.
	 */
	public Collection<Thing> getAllThings()
	{
		this.log.debug("getAllThings()");
		Model g = this.getGraph(this.serviceUrl + "/things/");

		ArrayList<Thing> results = new ArrayList<Thing>();

		ResIterator i = g.listSubjectsWithProperty(RDF.type);
		while (i.hasNext())
		{
			Resource r = i.nextResource();
			this.log.debug("Thing: " + r.getURI());
			Thing t = ThingDeserializer.thingFromRDF(g, r);
			if (t != null)
				results.add(t);
		}
		return results;
	}

	/**
	 * Returns a thing with a given UUID. Returns a <code>RefinderException</code>
	 * if this thing does not exist.
	 * 
	 * @param uuid
	 *           the UUID of the thing to be retrieved.
	 * @return the thing with UUID uuid
	 */
	public Thing getThing(UUID uuid)
	{
		Model g = this.getGraph(this.serviceUrl + "/things/" + uuid.toString() + "/");
		Thing t = ThingDeserializer.thingFromRDF(g, g.getResource(ThingSerializer.getObjectUri(this.serviceUrl, uuid, Thing.class)));
		return t;
	}

	/**
	 * Creates a new Thing. The thing UUID is automatically assigned by the Refinder
	 * web service.
	 * 
	 * @param thing
	 *           The thing to be created.
	 * @return A new thing instance that represents the created thing. Use this
	 *         instance if you need to further process the created thing.
	 */
	public Thing createThing(Thing thing)
	{
		Model g = ModelFactory.createDefaultModel();

		ThingSerializer.toRDF(g, thing);
		HttpResponse response = this.postGraph(this.serviceUrl + "/data/things/", g);

		if (response.getStatusLine().getStatusCode() == HttpStatus.SC_CREATED)
		{
			this.log.debug("*** Created transient thing!, Response Location: " + response.getHeaders("Location")[0]);

			UUID thingUuid = UUID.fromString(Util.findUuid(response.getHeaders("Location")[0].getValue()));
			Thing createdThing = this.getThing(thingUuid);
			return createdThing;
		}
		else
		{
			this.log.debug("Problem: status = " + response.getStatusLine());
			return null;
		}
	}

	public Thing createTransientThing(Thing thing)
	{
		Model g = ModelFactory.createDefaultModel();

		ThingSerializer.toRDF(g, thing);
		HttpResponse response = this.postGraph(this.serviceUrl + "/data/things/transient/", g);

		if (response.getStatusLine().getStatusCode() == HttpStatus.SC_CREATED)
		{
			this.log.debug("*** Created!, Response Location: " + response.getHeaders("Location")[0]);

			Thing newThing = new Thing(URI.create(response.getHeaders("Location")[0].getValue()), thing.getType());
			for(URI attributeName: thing.getAttributeNames())
				newThing.setAttribute(attributeName, thing.getAttribute(attributeName));
			
			return newThing;
		}
		else
		{
			this.log.debug("Problem: status = " + response.getStatusLine());
			return null;
		}
	}
	
	/**
	 * Writes an already existing thing to the server (e.g., after modifications
	 * have been applied).
	 * 
	 * @param thing
	 *           The thing to be stored
	 * @return <code>true</code> if the thing was successfully stored.
	 */
	public boolean storeThing(Thing thing)
	{
		Model g = ModelFactory.createDefaultModel();
		ThingSerializer.toRDF(g, thing);
		
		HttpResponse response = this.putGraph(thing.getUri().toString(), g);
		this.log.debug("Response: " + response.getStatusLine().getStatusCode());
		return response.getStatusLine().getStatusCode() == HttpStatus.SC_OK;
	}

   public boolean deleteThing(Thing thing)
	{
		HttpResponse response = this.deleteGraph(thing.getUri().toString());
		this.log.debug("Response: " + response.getStatusLine().getStatusCode());
		return response.getStatusLine().getStatusCode() == HttpStatus.SC_NO_CONTENT;
	} 
   
	/**
	 * Returns an annotation with a given UUID. Returns a <code>RefinderException</code>
	 * if this annotation does not exist.
	 * 
	 * @param uuid the UUID of the annotation to be retrieved.
	 * @return the annotation with UUID uuid
	 */
	public Annotation getAnnotation(UUID uuid)
	{
		Model g = this.getGraph(this.serviceUrl + "/annotations/" + uuid.toString() + "/");
		Annotation a = ThingDeserializer.annotationFromRDF(g, g.getResource(ThingSerializer.getObjectUri(this.serviceUrl, uuid, Annotation.class)));
		return a;
	}
	
	/**
	 * Creates a new Annotation. The thing UUID is automatically assigned by the Refinder
	 * web service.
	 * 
	 * @param annotation The annotation to be created.
	 * @return A new annotation instance that represents the created thing. Use this
	 *         instance if you need to further process the created thing.
	 */
	public Annotation createAnnotation(Annotation annotation)
	{
		Model g = ModelFactory.createDefaultModel();

		ThingSerializer.toRDF(g, annotation);
		g.write(System.out, "N3");
		
		HttpResponse response = this.postGraph(this.serviceUrl + "/data/annotations/", g);

		if (response.getStatusLine().getStatusCode() == HttpStatus.SC_CREATED)
		{
			this.log.debug("*** Created!, Response Location: " + response.getHeaders("Location")[0]);

			UUID annotationUuid = UUID.fromString(Util.findUuid(response.getHeaders("Location")[0].getValue()));
			Annotation createdAnnotation = this.getAnnotation(annotationUuid);
			return createdAnnotation;
		}
		else
		{
			this.log.debug("Problem: status = " + response.getStatusLine());
			return null;
		}
	}
	
	/**
	 * Writes an already existing annotation to the server (e.g., after modifications have been applied).
	 * @param annotation The anontation to be stored
	 * @return <code>true</code> if the annotation was successfully stored.
	 */
	public Boolean storeAnnotation(Annotation annotation)
	{
		Model g = ModelFactory.createDefaultModel();
		ThingSerializer.toRDF(g, annotation);

		HttpResponse response = this.putGraph(annotation.getUri().toString(), g);
		return response.getStatusLine().getStatusCode() == HttpStatus.SC_OK;
	}
	
	/**
	 * Returns things based on a fulltext search query.
	 * 
	 * @param q
	 *           The query, as it would be entered in the "Search" field in the
	 *           Refinder web application.
	 * @return A list of things that match the search criteria.
	 */
	public List<Thing> searchThings(String q)
	{
		Model g = this.getGraph(this.serviceUrl + "/things/search/?q=" + this.encode(q));

		List<Thing> results = new LinkedList<Thing>();
		ResIterator i = g.listResourcesWithProperty(RDF.type);
		while (i.hasNext())
		{
			Resource r = i.nextResource();
			this.log.debug("Thing: " + r.getURI());
			Thing t = ThingDeserializer.thingFromRDF(g, r);
			if (t != null)
				results.add(t);
		}
		return results;
	}

	public Collection<Thing> getRelatedThings(Thing thing)
	{
		Model g = this.getGraph(thing.getUri().toString().replace("#t", "related/"));
		
		System.out.println("RELATED THINGS GRAPH for " + thing.getUri() + ": ");
		g.write(System.out, "N3");
		
		List<Thing> results = new LinkedList<Thing>();
		ResIterator i = g.listResourcesWithProperty(RDF.type);
		while (i.hasNext())
		{
			Resource r = i.nextResource();
			this.log.debug("Thing: " + r.getURI());
			Thing t = ThingDeserializer.thingFromRDF(g, r);
			if (t != null)
				results.add(t);
		}
		return results;
	}
	
	
	public Collection<Thing> getRecommendations(Thing thing)
	{
		Model g = this.getGraph(thing.getUri().toString().replace("#t", "recommendations/"));
		List<Thing> results = new LinkedList<Thing>();
		ResIterator i = g.listResourcesWithProperty(RDF.type);
		while (i.hasNext())
		{
			Resource r = i.nextResource();
			this.log.debug("Thing: " + r.getURI());
			Thing t = ThingDeserializer.thingFromRDF(g, r);
			if (t != null)
				results.add(t);
		}
		return results;
	}
	
	/**
	 * Returns a list of things that are associated with a data object.
	 * 
	 * @param dataObjectUri
	 *           The URI of the data object; usually this is a URL of the form
	 *           "refinder:[datasourceid]:[localid]".
	 * @return A list of things associated with the data object.
	 */
	public List<Thing> findThingsByDataObject(String dataObjectUri)
	{
		String request = this.serviceUrl + "/things/search/?dataobject=" + this.encode(dataObjectUri);

		Model g = this.getGraph(request);

		List<Thing> results = new LinkedList<Thing>();
		ResIterator i = g.listResourcesWithProperty(RDF.type);
		while (i.hasNext())
		{
			Resource r = i.nextResource();
			this.log.debug("Thing: " + r.getURI());
			Thing t = ThingDeserializer.thingFromRDF(g, r);
			if (t != null)
				results.add(t);
		}
		return results;
	}

	/**
	 * Returns data object URLs that are associated with a given thing. This are
	 * usually "refinder:" URLs.
	 * 
	 * @param t
	 *           The thing
	 * @return a list of data object URLs.
	 */
	public List<String> getDataObjects(Thing thing)
	{
		Model g = this.getGraph(thing.getUri().toString().replace("#t", "dataobjects/"));

		List<String> results = new LinkedList<String>();

		StmtIterator i = g.listStatements(null, g.getProperty(PIMO.groundingOccurrence.toString()), (RDFNode) null);
		while (i.hasNext())
		{
			Statement s = i.nextStatement();
			this.log.debug("Occurrence: " + s.getObject());
			results.add(((Resource) s.getObject()).getURI());
		}

		return results;
	}

	/**
	 * Associates a data object with the given thing. Note that the associations
	 * to other data objects will be deleted. If you want to associate multiple
	 * data objects with a thing, use <code>setDataObjects()</code> instead.
	 * 
	 * @param thing
	 *           the thing
	 * @param dataObjectUri
	 *           the data object URL ("refinder:[datasourceid]:[localid]")
	 */
	public void setDataObject(Thing thing, String dataObjectUri)
	{
		List<String> dataObjectUris = new LinkedList<String>();
		dataObjectUris.add(dataObjectUri);
		this.setDataObjects(thing, dataObjectUris);
	}
	
	/**
	 * Associates a data object with the given thing. Note that the associations
	 * to other data objects will be deleted. If you want to associate multiple
	 * data objects with a thing, use <code>setDataObjects()</code> instead.
	 * 
	 * @param thing
	 *           the thing
	 * @param dataObjectUri
	 *           the data object URL ("refinder:[datasourceid]:[localid]")
	 * @param clickableUrl
	 * 			 a clickable URL that will be used in the Refinder user interface
	 */
	public void setDataObject(Thing thing, String dataObjectUri, String clickableUrl)
	{
		Model g = ModelFactory.createDefaultModel();

		Resource thingEntity = g.getResource(thing.getUri().toString());
		g.add(thingEntity, g.getProperty(PIMO.groundingOccurrence.toString()), g.getResource(dataObjectUri));
		if(clickableUrl != null)
			g.add(g.getResource(dataObjectUri), g.getProperty("http://www.cluug.com/ns/2010/06/dataobject#clickable-url"), g.getResource(clickableUrl));

		this.putGraph(thing.getUri().toString().replace("#t", "dataobjects/"), g);
	}

	/**
	 * Associates a set of data objects with the given thing. Already existing
	 * associations will be removed.
	 * 
	 * @param thing
	 *           the thing
	 * @param dataObjectUris
	 *           a list of data object URLs.
	 */
	public void setDataObjects(Thing thing, List<String> dataObjectUris)
	{
		Model g = ModelFactory.createDefaultModel();

		Resource thingEntity = g.getResource(thing.getUri().toString());
		Property groundingOccurrence = g.getProperty(PIMO.groundingOccurrence.toString());

		for (String dataObjectUri : dataObjectUris)
		{
			g.add(thingEntity, groundingOccurrence, g.getResource(dataObjectUri));
		}

		this.putGraph(thing.getUri().toString().replace("#t", "dataobjects/"), g);

	}

	/**
	 * Returns the URI of an already existing data source with UUID, or
	 * <code>null</code> if no data source with this UUID exists.
	 * 
	 * @param uuid
	 *           the data source's UUID
	 * @return the data source's full URI
	 */
	public URI getDataSource(UUID uuid)
	{
		try
		{
			// check if datasource exists
			this.getGraph(this.serviceUrl + "/datasources/" + uuid + "/#d");
			return URI.create(this.serviceUrl + "/datasources/" + uuid + "/#d");
		}
		catch (RefinderException e)
		{
			return null;
		}
	}

	/**
	 * Registers a data source at the Refinder web service.
	 * 
	 * @param type
	 *           The type of the data source. Can be any URI; it is recommended
	 *           to use URIs from the <code>DSTYPE</code> class.
	 * @param name
	 *           The human-readable name of the data source.
	 * @param physical
	 *           The physical identifier of the data source. The format and
	 *           interpretation of the physical identifier depends on the data
	 *           source type.
	 * @return A newly created UUID for this data source.
	 */
	public UUID createDataSource(URI type, String name, String physical)
	{
		Model g = ModelFactory.createDefaultModel();

		Resource dsEntity = g.createResource();

		g.add(dsEntity, g.getProperty(RDF.type.toString()), g.getResource(DATASOURCE.DataSource.toString()));
		g.add(dsEntity, g.getProperty(DATASOURCE.type.toString()), g.getResource(type.toString()));
		g.add(dsEntity, g.getProperty(RDFS.label.toString()), g.createLiteral(name));
		g.add(dsEntity, g.getProperty(DATASOURCE.physical.toString()), g.createLiteral(physical));

		HttpResponse res = this.postGraph(this.serviceUrl + "/datasources/", g);
		return UUID.fromString(Util.findUuid(res.getHeaders("Location")[0].getValue()));
	}

	
	public Collection<UUID> findDataSourceByPhysical(String physical)
	{
		Model g = this.getGraph(this.serviceUrl + "/datasources/?physical=" + this.encode(physical));
		
		ArrayList<UUID> results = new ArrayList<UUID>();
		StmtIterator i = g.listStatements(null, RDF.type, g.getResource(DATASOURCE.DataSource.toString()));
		while(i.hasNext())
		{
			Statement s = i.nextStatement();
			results.add(UUID.fromString(Util.findUuid(s.getSubject().toString())));
		}
		
		return results;
	}
	
	
	
	public void analyzeContent(Thing thing, String content)
	{
		try
		{
			byte[] byteData = content.getBytes("UTF-8");
			this.analyzeContent(thing, byteData, null);
		}
		catch (UnsupportedEncodingException e)
		{
			this.handleException(e);
		}
	}
	
	
	public void analyzeContent(Thing thing, InputStream data)
	{
		try
		{
			byte[] byteData = Util.obtainByteData(data);
			this.analyzeContent(thing, byteData, null);
		}
		catch (Exception e)
		{
			throw this.handleException(e);
		}
	}
	
	public void analyzeContent(Thing thing, byte[] byteData, String contentType)
	{
		if(contentType == null)
			contentType = "application/octet-stream";
		
		try
		{
			String url = thing.getUri().toString().replace("#t", "analyzer/");
		
			this.log.debug("POST content to <" + url + "> ...");

			HttpPost httpPost = new HttpPost(url);
			httpPost.addHeader("Content-Type", contentType);

			httpPost.setEntity(new ByteArrayEntity(byteData));   // -1 means "length is unknown"

			this.consumer.sign(httpPost);

			HttpResponse response = new DefaultHttpClient().execute(httpPost);
		}
		catch (Exception e)
		{
			throw this.handleException(e);
		}
	}
	
	
	// ******************************************************************************
	// private helpers
	// ******************************************************************************

	private final String	CONTENT_TYPE	= "application/rdf+xml";
	private final String	JENA_TYPE		= "RDF/XML";

	private String cookie;

	private Model getGraph(String url)
	{
		try
		{
			this.log.debug("GET graph from <" + url + "> ...");

			HttpGet httpGet = new HttpGet(url);
			httpGet.addHeader("Accept", CONTENT_TYPE);
			if(this.cookie != null){
				httpGet.addHeader("Cookie", this.cookie);
			}
			this.consumer.sign(httpGet);
			this.log.debug("Signature: " + httpGet.getFirstHeader("Authorization").getValue());
			
			HttpResponse response = new DefaultHttpClient().execute(httpGet);
			if(response.getStatusLine().getStatusCode() == 200)
			{
				Model g = ModelFactory.createDefaultModel();
				g.read(response.getEntity().getContent(), "", JENA_TYPE);
	
				this.log.debug("Retrieved " + g.size() + " triples.");
				
				this.online = true;
				this.authenticated = true;
	
				return g;
			}
			else if(response.getStatusLine().getStatusCode() == 201)
				throw new RefinderException(RefinderException.Status.NotFound);
			else if(response.getStatusLine().getStatusCode() == 401)
				throw new RefinderException(RefinderException.Status.NotAuthenticated);
			else if(response.getStatusLine().getStatusCode() == 404)
				throw new RefinderException(RefinderException.Status.NotFound);
			else
				throw new RefinderException(RefinderException.Status.Unknown);
		}
		catch (Exception e)
		{
			if(e instanceof RefinderException)
			{
				throw (RefinderException) e;
			}
			else
			{
				throw this.handleException(e);
			}
		}
	}

	private HttpResponse postGraph(String url, Model graph)
	{
		try
		{
			url = Util.cutFragment(url);

			this.log.debug("POST + " + graph.size() + " triples to <" + url + "> ...");

			HttpPost httpPost = new HttpPost(url);
			httpPost.addHeader("Content-type", CONTENT_TYPE);
			if(this.cookie != null){
				httpPost.addHeader("Cokoie", this.cookie);
			}
			httpPost.setEntity(new StringEntity(this.serializeGraph(graph), "UTF-8"));

			this.consumer.sign(httpPost);

			HttpResponse response = new DefaultHttpClient().execute(httpPost);
			if(this.cookie==null && response.getHeaders("Set-Cookie").length>0){
				this.cookie=  response.getHeaders("Set-Cookie")[0].getValue();
			}
			return response;
		}
		catch (Exception e)
		{
			throw this.handleException(e);
		}
	}

	private HttpResponse putGraph(String url, Model graph)
	{
		try
		{
			url = Util.cutFragment(url);
			
			this.log.debug("PUT + " + graph.size() + " triples to <" + url + "> ...");
			
			HttpPut httpPut = new HttpPut(url);
			if(this.cookie != null){
				httpPut.addHeader("Cokoie", this.cookie);
			}
			httpPut.addHeader("Content-type", CONTENT_TYPE);
			httpPut.setEntity(new StringEntity(this.serializeGraph(graph), "UTF-8"));

			this.consumer.sign(httpPut);

			HttpResponse response = new DefaultHttpClient().execute(httpPut);

			return response;
		}
		catch (Exception e)
		{
			throw this.handleException(e);
		}
	}

   private HttpResponse deleteGraph(String url)
   {
   	try
   	{
   		url = Util.cutFragment(url);

   		this.log.debug("DELETE URL <" + url + ">");

   		HttpDelete httpDelete = new HttpDelete(url);
   		httpDelete.addHeader("Accept", CONTENT_TYPE);
   		if(this.cookie != null){
			httpDelete.addHeader("Cokoie", this.cookie);
		}
			this.consumer.sign(httpDelete);
			this.log.debug("Signature: " + httpDelete.getFirstHeader("Authorization").getValue());

			HttpResponse response = new DefaultHttpClient().execute(httpDelete);

			return response;
   	}
   	catch(Exception e)
   	{
   		throw this.handleException(e);
   	}
   }

	
	private RefinderException handleException(Exception e)
	{
		// TODO implement

		e.printStackTrace();
		this.log.error("Error: " + e.getMessage());
		return new RefinderException(RefinderException.Status.Unknown, e);
	}

	private String serializeGraph(Model graph)
	{
		StringWriter sw = new StringWriter();
		graph.write(sw, JENA_TYPE);
		return sw.getBuffer().toString();
	}

	private String encode(String string)
	{
		try
		{
			return URLEncoder.encode(string, "UTF-8");
		}
		catch (UnsupportedEncodingException e)
		{
			throw new RefinderException(RefinderException.Status.EnvironmentProblem, e);
		}
	}

}
