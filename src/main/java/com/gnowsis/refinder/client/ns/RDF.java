package com.gnowsis.refinder.client.ns;

import java.net.URI;

public class RDF
{
	public static final String _base = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";

	public static final URI type = URI.create(_base + "type");
	public static final URI value = URI.create(_base + "value");
}
