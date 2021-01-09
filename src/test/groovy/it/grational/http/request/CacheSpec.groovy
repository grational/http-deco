package it.grational.http.request

import spock.lang.Specification
import it.italiaonline.rnd.cache.CacheContainer
import java.time.Duration

class CacheSpec extends Specification {

	final String url     = 'https://www.google.it'
	final String content = 'Google homepage content'

	def "Should query the inner HttpRequest class just the first time"() {
    given: 'a mocked standard get'
			StandardGet stdGet = Mock()
		and: 'a mocked file cache container'
			CacheContainer cacheContainer = Mock()
		and: 'a real cached connection'
			Boolean alreadyCalled = false
			Duration leaseTime = Duration.ofMillis(1000)
			Cache cache = new Cache (stdGet, cacheContainer, leaseTime)

		when: 'the first request to obtain the text is done'
			def actualResult = cache.text()
		then: '1 call is done to obtain the text the first time'
			1 * cacheContainer.valid(_) >> false
			1 * stdGet.text() >> content
			1 * cacheContainer.write(content) >> null
		and:
			actualResult == content

		when: 'another request to obtain the text is done'
			def cacheResult = cache.text()
		then: 'the second time the cache is used'
			1 * cacheContainer.valid(_) >> true
			1 * cacheContainer.content() >> content
		and:
			cacheResult == content
	}
}
