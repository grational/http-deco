package it.italiaonline.rnd.net.http

class AuthorizationHeader extends BasicAuthenticationHeader {
	AuthorizationHeader(Map params) {
		this.username = params.username ?: { throw new IllegalArgumentException("[${this.class.simpleName}] Invalid username parameter") }()
		this.password = params.password ?: { throw new IllegalArgumentException("[${this.class.simpleName}] Invalid password parameter")}()
	}

	@Override
	String name() {
		return 'Authorization'
	}

}
