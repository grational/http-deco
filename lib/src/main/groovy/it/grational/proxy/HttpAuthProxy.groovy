package it.grational.proxy

class HttpAuthProxy extends HttpProxy {
	
	HttpAuthProxy(Map params) {
		super (
			host: params.host,
			port: params.port
		)
		ProxyAuthentication.enable(params.username,params.password)
	}

}
