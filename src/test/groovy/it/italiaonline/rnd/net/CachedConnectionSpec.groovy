package it.italiaonline.rnd.net

import spock.lang.Specification
import it.italiaonline.rnd.cache.CacheFile

class CachedConnectionSpec extends Specification {

	final String url     = 'https://www.google.it'
	final String content = 'Google homepage content'

	def "Should query the inner NetConnection class just the first time"() {

    setup:
			SimpleConnection sconn = Mock()
			sconn.text()     >> content
			sconn.toString() >> url

			Boolean alreadyCalled = false
			CacheFile cache = GroovyMock()
			cache.valid() >> {
				Boolean result 
				if (alreadyCalled) {
					result = true
				} else {
					alreadyCalled = true
					result = false
				}
				return result
			}
			cache.content() >> content

			BigInteger leaseTime = 1000

			CachedConnection cconn = new CachedConnection (
			                           sconn,       // NetConnection
			                           cache,       // CacheFile
			                           leaseTime    // BigInteger
			                         )

    when: 'the first request to obtain the text is done'
			def actualResult = cconn.text()
		then: '1 call is done to obtain the text the first time'
			1 * cache.valid(_) >> false
			1 * sconn.text() >> content
			actualResult == content
		
		when: 'another request to obtain the text is done'
			def cacheResult = cconn.text()
    then: 'the second time the cache is used'
			1 * cache.valid(_) >> true
			1 * cache.content() >> content
			cacheResult == content
	}
}
