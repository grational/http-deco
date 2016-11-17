package it.italiaonline.rnd.net

class SimpleConnection implements NetConnection {

	private final String url

	SimpleConnection(String url) {
		this.url = url
	}

	@Override
	String text() {
		new URL(this.url).text
	}

	@Override
	String toString() {
		this.url
	}
}
