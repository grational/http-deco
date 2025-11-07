package it.grational.proxy

class ProxyAuthentication {

	static void enable(String username, String password) {
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
