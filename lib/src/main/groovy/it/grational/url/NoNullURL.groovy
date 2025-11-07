package it.grational.url

final class NoNullURL implements URLConvertible {
	private final URL input

	NoNullURL(URL inpt) {
		this.input = inpt
	}

	@Override
	URL toURL() throws IllegalArgumentException {
		if (input == null) {
			throw new IllegalArgumentException("[${this.class.simpleName}] Null URL")
		}
		this.input
	}

	@Override
	URI toURI() throws IllegalArgumentException {
		this.toURL().toURI()
	}

	@Override
	String toString() {
		this.input.toString()
	}
}
