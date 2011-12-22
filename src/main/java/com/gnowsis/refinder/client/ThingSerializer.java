package com.gnowsis.refinder.client;

import java.net.URI;
import java.util.UUID;

import com.gnowsis.refinder.client.ns.RDF;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;

class ThingSerializer
{
	@SuppressWarnings("unchecked")
	public static String getObjectUri(String serviceUrl, UUID uuid, Class type)
	{
		if (type == Thing.class)
			return serviceUrl + "/things/" + uuid.toString() + "/#t";
		else if (type == Annotation.class)
			return serviceUrl + "/annotations/" + uuid.toString() + "/#a";
		else
			return null;	
	}
	
//	public static String getObjectUri(String serviceUrl, Object o)
//	{
//		if (o instanceof Thing)
//			return serviceUrl + "/things/" + ((Thing) o).getUUID().toString() + "/#t";
//		else if (o instanceof Annotation)
//			return serviceUrl + "/annotations/" + ((Annotation) o).getUUID().toString() + "/#a";
//		else
//			return null;
//	}

	public static void toRDF(Model g, Thing t)
	{
		Resource thingNode = null;

		if (t.getUri() == null)
			thingNode = g.createResource();
		else
			thingNode = g.getResource(t.getUri().toString());

		g.add(thingNode, g.getProperty(RDF.type.toString()), g.getResource(t.getType().toString()));

		for (URI key : t.getAttributeNames())
		{
			Object value = t.getAttribute(key);
			g.add(thingNode, g.getProperty(key.toString()), g.createTypedLiteral(value));
		}
	}
	
	public static void toRDF(Model g, Annotation a)
	{
		Resource annotationNode = null;

		if (a.getUri() == null)
			annotationNode = g.createResource();
		else
			annotationNode = g.getResource(a.getUri().toString());

		// add thing -> predicate -> annotation statement
		Resource thingNode = g.getResource(a.getThingUri().toString());
		g.add(thingNode, g.getProperty(a.getPredicate().toString()), annotationNode);

		// add "value" statement
		g.add(annotationNode, g.getProperty(RDF.value.toString()), g.createTypedLiteral(a.getValue()));
		
		for (URI key : a.getAttributeNames())
		{
			Object value = a.getAttribute(key);
			g.add(annotationNode, g.getProperty(key.toString()), g.createTypedLiteral(value));
		}
	}
}
