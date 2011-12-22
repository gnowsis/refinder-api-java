package com.gnowsis.refinder.client;

import java.net.URI;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Annotation
{
	private final Log					log	= LogFactory.getLog(getClass());

	private final URI             thingUri;
	private final URI					uri;
	private final URI					predicate;
	private Object						value;

	private Map<String, Object>	attributes;

//	public Annotation(UUID thingUuid, URI predicate)
//	{
//		this(thingUuid, null, predicate);
//	}

	public Annotation(URI annotationUri, URI thingUri, URI predicate)
	{
		this.uri = annotationUri;
		this.thingUri = thingUri;
		this.predicate = predicate;
		this.attributes = new HashMap<String, Object>();
	}

	public URI getUri()
	{
		return this.uri;
	}

	public URI getThingUri()
	{
		return this.thingUri;
	}
	

	public URI getPredicate()
	{
		return this.predicate;
	}
	
	public Object getValue()
	{
		return this.value;
	}
	
	public void setValue(Object value)
	{
		this.value = value;
	}
	
   public Object getAttribute(URI uri)
   {
       return this.attributes.get(uri.toString());
   }

   public void setAttribute(URI uri, Object value)
   {
       this.log.debug("Setting attribute <" + uri + "> to " + value);
       if (value != null)
           this.attributes.put(uri.toString(), value);
   }

   public Iterable<URI> getAttributeNames()
   {
       List<URI> names = new LinkedList<URI>();
       for (String attrName: this.attributes.keySet())
           names.add(URI.create(attrName));
       return names;
   }
}
