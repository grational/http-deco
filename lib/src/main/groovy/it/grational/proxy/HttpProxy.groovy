package it.grational.proxy

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
