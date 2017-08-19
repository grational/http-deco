package it.italiaonline.rnd.net

import spock.lang.Specification
import it.italiaonline.rnd.cache.CacheContainer
import java.time.Duration

class CachedConnectionSpec extends Specification {

	final String url     = 'https://www.google.it'
	final String content = 'Google homepage content'

	def "Should query the inner NetConnection class just the first time"() {

    given: 'a mocked simple connection'
			SimpleConnection sconn = Mock()
		and: 'a mocked file cache container'
			CacheContainer cacheContainer = Mock()
		and: 'a real cached connection'
			Boolean alreadyCalled = false
			Duration leaseTime = Duration.ofMillis(1000)
			CachedConnection cconn = new CachedConnection (
			                           sconn,       // NetConnection
			                           cacheContainer,   // CacheContainer
			                           leaseTime    // BigInteger
			                         )

    when: 'the first request to obtain the text is done'
			def actualResult = cconn.text()
		then: '1 call is done to obtain the text the first time'
			1 * cacheContainer.valid(_) >> false
			1 * sconn.text() >> content
			1 * cacheContainer.write(content) >> null
			actualResult == content
		
		when: 'another request to obtain the text is done'
			def cacheResult = cconn.text()
    then: 'the second time the cache is used'
			1 * cacheContainer.valid(_) >> true
			1 * cacheContainer.content() >> content
			cacheResult == content
	}
}
