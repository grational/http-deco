package it.grational.proxy

import spock.lang.*
import it.grational.specification.Environment

class NoProxyUSpec extends Specification {

	@Shared environment = new Environment (
		no_proxy: '10.0.0.2, 10.0.0.3:21,  .pgol.net , pgol.com:443 '
	)

	def "Should be capable of saying if a certain URL needs to pass through proxy or not"() {
		setup:
			environment.insert()

		expect:
			new NoProxy().exclude(url.toURL()) == expectedValue

		where:
			url                                          || expectedValue
			// 10.0.0.2 - all ports excluded (also the implicit 21, 80, 443)
			'ftp://10.0.0.2'                             || true
			'http://10.0.0.2'                            || true
			'https://10.0.0.2'                           || true
			'ftp://10.0.0.2:21'                          || true
			'http://10.0.0.2:80'                         || true
			'https://10.0.0.2:443'                       || true
			'ftp://10.0.0.2:2121'                        || true
			'http://10.0.0.2:8080'                       || true
			'https://10.0.0.2:4433'                      || true
			// 10.0.0.3:21 - proxy excluded only towards port 21
			'ftp://10.0.0.3'                             || true
			'http://10.0.0.3'                            || false
			'https://10.0.0.3'                           || false
			'ftp://10.0.0.3:21'                          || true
			'http://10.0.0.3:80'                         || false
			'https://10.0.0.3:443'                       || false
			'ftp://10.0.0.3:2121'                        || false
			'http://10.0.0.3:8080'                       || false
			'https://10.0.0.3:4433'                      || false
			// pgol.net - all ports excluded (also the implicit 21, 80, 443)
			'ftp://ftp.pgol.net'                         || true
			'http://gitlab.pgol.net'                     || true
			'https://gitlab.pgol.net'                    || true
			'ftp://ftp.pgol.net:21'                      || true
			'http://gitlab.pgol.net:80'                  || true
			'https://gitlab.pgol.net:443'                || true
			'ftp://gitlab.pgol.net:2121'                 || true
			'http://gitlab.pgol.net:8080'                || true
			'https://gitlab.pgol.net:4433'               || true
			// pgol.com:443
			'ftp://ftp.pgol.com'                         || false
			'http://gitlab.pgol.com'                     || false
			'https://gitlab.pgol.com'                    || true
			'ftp://ftp.pgol.com:21'                      || false
			'http://gitlab.pgol.com:80'                  || false
			'https://gitlab.pgol.com:443'                || true
			'ftp://gitlab.pgol.com:2121'                 || false
			'http://gitlab.pgol.com:8080'                || false
			'https://gitlab.pgol.com:4433'               || false
			// normal external domain
			'https://gitlab.com'                         || false
			// query parameters
			'https://www.pgol.net/query?param=1&param=2' || true
			'http://10.0.0.2:8080/query?param=1&param=2' || true
	}

	def "Should work also the the no_proxy environment variable has not been set"() {
		given:
			environment.clean()

		expect:
			new NoProxy().exclude(url.toURL()) == false

		where:
			url << [
			'http://gitlab.pgol.net',
			'https://gitlab.com',
			'ftp://10.0.0.2',
			'http://10.0.0.1',
			'https://some.domain.it/query?param=1&param=2',
			'http://gitlab.pgol.net/query?param=1&param=2'
		]
	}

}
