package it.grational.http.request

import spock.lang.*
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
		and: 'the miss operation'
			int missCounter = 0
			def missOperation = { missCounter++ }
		and: 'a real cached connection'
			Duration leaseTime = Duration.ofMillis(1000)
			Cache cachedRequest = new Cache(stdGet, cacheContainer, leaseTime, missOperation)

		when: 'the first request to obtain the text is done'
			def actualResult = cachedRequest.text()
		then: '1 call is done to obtain the text the first time'
			1 * cacheContainer.valid(_) >> false
			1 * stdGet.text() >> content
			1 * cacheContainer.write(content) >> null
		and:
			actualResult == content
		and: 'the miss operation is executed once'
			missCounter == 1

		when: 'another request to obtain the text is done'
			def cacheResult = cachedRequest.text()
		then: 'the second time the cache is used'
			1 * cacheContainer.valid(_) >> true
			1 * cacheContainer.content() >> content
		and:
			cacheResult == content
		and: 'the miss operation has not been executed'
			missCounter == 1
	}

}
