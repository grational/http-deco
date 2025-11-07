package it.grational.proxy

class NoProxy implements ExclusionList {
	private final List<SocketAddress> list

	NoProxy() {
		this.list = parseEnv()
	}

	private List<SocketAddress> parseEnv() {
		System.getenv().get('no_proxy')
		?.tokenize(',')
		?.collect { elem ->
			def splitted = elem.trim().replaceFirst(/^\./,'').tokenize(':')
			String host = splitted.first()
			Integer port = ( splitted.size() == 1 ) ? Default.ANY.port : (splitted.last() as Integer)
			return new InetSocketAddress(host, port)
		} ?: []
	}

	@Override
	Boolean exclude(URL url) {
		String host = url.host
		Integer port = (url.port != -1) ? url.port : defaultPortBy(url.protocol)
		return (
			this.list.find { addr ->
				if ( addr.port != Default.ANY.port )
					host.endsWith(addr.hostName) && ( port == addr.port )
				else
					host.endsWith(addr.hostName)
			} != null
		)
	}

	private Integer defaultPortBy(String protocol) {
		Integer result = Default.HTTP.port
		if ( protocol )
			result = (protocol.toUpperCase() as Default).getPort()
		return result
	}

	private enum Default {
		ANY   { @Override Integer getPort() { 0 } },
		HTTP  { @Override Integer getPort() { 80 } },
		HTTPS { @Override Integer getPort() { 443 } },
		FTP   { @Override Integer getPort() { 21 } }
		abstract Integer getPort()
	}

}
