package it.italiaonline.rnd.net

class SimpleConnection implements NetConnection {

	private final URL url

	SimpleConnection(URL url) {
		this.url = url
	}

	@Override
	String text() {
		this.url.text
	}

	@Override
	String toString() {
		this.url.toString()
	}
}
