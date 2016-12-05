package it.italiaonline.rnd.net

final class NoNullConnection implements NetConnection {
	private final URL input

	NoNullConnection(URL inpt) {
		this.input = inpt
	}

	@Override
	String text() throws IllegalArgumentException {
		if (input == null) {
			throw new IllegalArgumentException("Input is NULL: can't go ahead.")
		}
		this.input.text
	}
}
