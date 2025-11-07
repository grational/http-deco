package it.grational.proxy

enum EnvVar {
	ALL   { @Override String value() { System.getenv().get('all_proxy')   } },
	FTP   { @Override String value() { System.getenv().get('ftp_proxy')   } },
	HTTP  { @Override String value() { System.getenv().get('http_proxy')  } },
	HTTPS { @Override String value() { System.getenv().get('https_proxy') } },
	RSYNC { @Override String value() { System.getenv().get('rsync_proxy') } };
	abstract String value()

	static EnvVar byURL(URL url) {
		String protocol
		try {
			protocol = protocolByURL(url)
			protocol?.toUpperCase() as EnvVar
		} catch (e) {
			throw new UnsupportedOperationException("[EnvVar] Protocol '${protocol}' is not supported!",e)
		}
	}

	private static String protocolByURL(URL url) {
		url.toString().tokenize(':').first()
	}
}
