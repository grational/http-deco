package it.grational.http.proxy

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
