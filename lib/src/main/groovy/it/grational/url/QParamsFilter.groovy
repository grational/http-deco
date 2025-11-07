package it.grational.url

class QParamsFilter {

	private final URL  url
	private final List taboo

	QParamsFilter(
		URL  url,
		List taboo
	) {
		this.url  = url
		this.taboo = taboo
	}

	String filtered() {
		def output = this.url.toString()
		this.taboo.each { qparam ->
			output = output.replaceAll(/${qparam}=[^&]*/,'')
			               .replaceAll(/&&/,'&')
			               .replaceAll(/[?]&/,'?')
			               .replaceAll(/[?]$/,'')
			               .replaceAll(/&$/,'')
		}
		return output
	}
}
