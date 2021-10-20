package it.grational.http.proxy

class HttpAuthProxy extends HttpProxy {
	
	HttpAuthProxy(Map params) {
		super (
			host: params.host,
			port: params.port
		)
		this.enableAuthentication(params.username,params.password)
	}

	private enableAuthentication(String username, String password) {
		// enable proxy basic auth for https - needed after j8
		// otherwise you will obtain a 417 http error
		System.properties << [
			'jdk.http.auth.tunneling.disabledSchemes': ''
		]
		// set the jdk net class that handles proxy basic authentication
		Authenticator.default = {
			new PasswordAuthentication (
				username,
				password as char[]
			)
		} as Authenticator
	}

}
