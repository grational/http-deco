package it.italiaonline.rnd.net

class QParamsFilter implements NetConnection {

	private final NetConnection origin
	private final List tabu

	QParamsFilter(
		NetConnection orign,
		List tabu
	) {
		this.origin = orign
		this.tabu = tabu
	}

	@Override
	String text() {
		def input = this.origin.text()
	}

	@Override
	String toString() {
		def output = this.origin.toString()
		this.tabu.each { qparam ->
			output = output.replaceAll(/${qparam}=[^&]*/,'')
			               .replaceAll(/&&/,'&')
			               .replaceAll(/[?]&/,'?')
			               .replaceAll(/[?]$/,'')
		}
		return output
	}
}
