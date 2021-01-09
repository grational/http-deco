package it.grational.http.header

class Authorization extends BasicAuthentication {
	Authorization(Map params) {
		this.username = params.username ?: { throw new IllegalArgumentException("[${this.class.simpleName}] Invalid username parameter") }()
		this.password = params.password ?: { throw new IllegalArgumentException("[${this.class.simpleName}] Invalid password parameter")}()
	}

	@Override
	String name() {
		return 'Authorization'
	}

}
