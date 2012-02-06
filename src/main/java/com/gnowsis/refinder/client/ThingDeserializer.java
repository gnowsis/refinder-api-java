package com.gnowsis.refinder.client;

import java.net.URI;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.gnowsis.refinder.client.ns.RDF;
import com.hp.hpl.jena.datatypes.xsd.XSDDateTime;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.rdf.model.impl.ResourceImpl;

class ThingDeserializer
{
	private static final Log	log	= LogFactory.getLog(ThingDeserializer.class);


	public static Thing thingFromRDF(Model g, Resource n)
	{
		try
		{
			// find out uuid
			String thingUri = n.getURI();
			
			if (thingUri == null)
			{
				log.debug("Found no ThingURI in <" + n.getURI() + ">, returning null.");
				return null;
			}

			// generic thing URIs always use http://
			if(thingUri.startsWith("https://"))
			{
				thingUri = "http://" + thingUri.substring("https://".length());
				n = ResourceFactory.createResource(thingUri);
			}
			
			log.debug("Thing URI: <" + thingUri + ">");
			
			// find out type
			String type = null;
			Property RDF_TYPE = ResourceFactory.createProperty(RDF.type.toString());

			StmtIterator i = g.listStatements(n, RDF_TYPE, (RDFNode) null);
			while (i.hasNext())
			{
				Statement s = i.nextStatement();
				type = ((Resource) s.getObject()).getURI();
			}
			if (type == null)
			{
				log.debug("Found no type information, returning null.");
				return null;
			}

			log.debug("Found type: " + type);

			Thing thing = new Thing(URI.create(thingUri), URI.create(type));

			i = g.listStatements(n, null, (RDFNode) null);
			while (i.hasNext())
			{
				Statement triple = i.nextStatement();

				log.debug("Converting statement to Attribute: " + triple.getPredicate() + " => " + triple.getObject());
				if (triple.getPredicate().equals(RDF_TYPE))
					log.debug("Skipping");
				else if (triple.getObject().isResource()
					      && ((Resource) triple.getObject()).getURI().endsWith("/#a"))
				{
					URI annotationType = URI.create(triple.getPredicate().toString());
					URI annotationUri = URI.create(triple.getObject().toString());

					thing.addAnnotation(annotationType, annotationUri);
				}
				else if (triple.getObject() instanceof Literal || triple.getObject() instanceof Resource)
				{
					thing.setAttribute(URI.create(triple.getPredicate().getURI()), toPOJO(triple.getObject()));
				}
				else
				{
					log.warn("Failed to interpret statement: " + triple);
				}
			}

			return thing;
		}
		catch (Exception e)
		{
			log.debug("Error: " + e);
			e.printStackTrace();
			return null;
		}
	}
	
	public static Annotation annotationFromRDF(Model g, Resource n)
	{
		try
		{
			// find out URI
			String uri = n.getURI();
			if (uri == null)
			{
				log.debug("Found no UUID, returning null.");
				return null;
			}

			// find out thing and predicate
			String thingUri = null;
			String type = null;

			StmtIterator i = g.listStatements(null, null, n);
			while (i.hasNext())
			{
				Statement s = i.nextStatement();
				thingUri = s.getSubject().getURI();
				type = ((Resource) s.getPredicate()).getURI();
			}
			if (type == null)
			{
				log.debug("Found no type information, returning null.");
				return null;
			}
			if(thingUri == null)
			{
				log.debug("Found no thing URI, returning null.");
				return null;
			}

			log.debug("Found thing URI = " + thingUri + ", type: " + type);

			Annotation annotation = new Annotation(URI.create(uri), URI.create(thingUri), URI.create(type));

			Property RDF_VALUE = ResourceFactory.createProperty(RDF.value.toString());
			i = g.listStatements(n, null, (RDFNode) null);
			
			while (i.hasNext())
			{
				Statement triple = i.nextStatement();

				log.debug("Converting statement to Attribute: " + triple.getPredicate() + " => " + triple.getObject());
				
				if (triple.getPredicate().equals(RDF_VALUE))
				{
					annotation.setValue(toPOJO(triple.getObject()));
				}
				else if (triple.getObject() instanceof Literal || triple.getObject() instanceof Resource)
				{
					annotation.setAttribute(URI.create(triple.getPredicate().getURI()), toPOJO(triple.getObject()));
				}
				else
				{
					log.warn("Failed to interpret statement: " + triple);
				}
			}

			return annotation;
		}
		catch (Exception e)
		{
			log.debug("Error: " + e);
			e.printStackTrace();
			return null;
		}
	}
	
	private static Object toPOJO(RDFNode node)
	{
		if (node instanceof Resource)
		{
			Resource r = (Resource) node;
			if(r.isAnon())
				return null;
			else
				return URI.create(r.getURI());
		}
		else if(node instanceof Literal)
		{
			Literal lit = (Literal) node;

			Object o = lit.getValue();
			if(o instanceof XSDDateTime)
			{
				XSDDateTime _o = (XSDDateTime) o;
				o = _o.asCalendar();
			}
			
			return o;
		}
		else
			return null;
	}
}