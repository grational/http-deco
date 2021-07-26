package it.grational.http.header

class ProxyAuthorization extends BasicAuthentication {
	ProxyAuthorization(Map params) {
		this.username = params.username ?: { throw new IllegalArgumentException("[${this.class.simpleName}] Invalid username parameter") }()
		this.password = params.password ?: { throw new IllegalArgumentException("[${this.class.simpleName}] Invalid password parameter")}()
	}

	@Override
	String name() {
		return 'Proxy-Authorization'
	}

}
