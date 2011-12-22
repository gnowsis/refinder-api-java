package com.gnowsis.refinder.client;

import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Thing
{
    private final Log log = LogFactory.getLog(getClass());

//    private final UUID uuid;
    private final URI uri;
    private final URI type;

    private Map<String, Object> attributes;
    private List<URIPair> annotations;

//    public Thing(URI type)
//    {
//   	 this(null, type);
//    }

    public Thing(URI uri, URI type)
    {
        this.uri = uri;
        this.type = type;
        this.attributes = new HashMap<String, Object>();
        this.annotations = new LinkedList<URIPair>();
    }

    // ********************** Attributes ********************

    public URI getUri()
    {
   	 return this.uri;
    }
    
    public URI getType()
    {
   	 return this.type;
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

    // ********************** annotations ***********************

    public Collection<URIPair> getAnnotations()
    {
        return this.annotations;
    }

    public void addAnnotation(URI type, URI annotationUri)
    {
        this.annotations.add(new URIPair(type, annotationUri));

    }


    // ************************************************************

    public class URIPair 
    {
        public URI type;
        public URI target;
        
        public URIPair(URI type, URI target)
        {
      	  this.type = type;
      	  this.target = target;
        }
    };

    
    // ************************************************************

    @Override
    public String toString()
    {
        return "<" + this.uri + "> (" + this.attributes.size() + " attributes, " + this.annotations.size() + " annotations.)";
    }
    
    @Override
    public boolean equals(Object o)
    {
   	 if (!(o instanceof Thing))
   		 return false;
   	 
   	 return this.getUri().equals(((Thing) o).getUri());
    }
}
