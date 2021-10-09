package it.grational.http.proxy

import groovy.lang.Delegate

class HttpProxy extends Proxy {
	
	HttpProxy(Map params) {
		super (
			Proxy.Type.HTTP,
			new InetSocketAddress (
				params.host,
				params.port
			)
		)
	}

}
