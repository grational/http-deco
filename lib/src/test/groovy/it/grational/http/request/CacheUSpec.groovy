package it.grational.http.request

import spock.lang.*
import it.grational.cache.CacheContainer
import java.time.Duration
import it.grational.http.response.HttpResponse

class CacheUSpec extends Specification {

	final String url     = 'https://www.google.it'
	final String content = 'Google homepage content'

	def "Should query the inner HttpRequest class just the first time"() {
    given: 'a mocked http response'
			HttpResponse response = Mock()
			response.code()  >> { 200 }
			response.text()  >> { content }
			response.bytes() >> { content.bytes }
		and: 'a mocked standard get'
			Get get = Mock()
			get.connect() >> response
		and: 'a mocked file cache container'
			CacheContainer cacheContainer = Mock()
		and: 'the miss operation'
			int missCounter = 0
			def missOperation = { missCounter++ }
		and: 'a real cached connection'
			Duration leaseTime = Duration.ofMillis(1000)
			Cache cachedRequest = new Cache (
				get,
				cacheContainer,
				leaseTime,
				missOperation
			)

		when: 'the first request to obtain the text is done'
			def actualResult = cachedRequest.connect()
		then: '1 call is done to obtain the text the first time'
			1 * cacheContainer.valid(_) >> false
			1 * get.connect() >> response
			1 * cacheContainer.write(response.text()) >> null
		and:
			actualResult.text() == content
		and: 'the miss operation is executed once'
			missCounter == 1

		when: 'another request to obtain the text is done'
			def cacheResult = cachedRequest.connect()
		then: 'the second time the cache is used'
			1 * cacheContainer.valid(_) >> true
			1 * cacheContainer.content() >> content
		and:
			cacheResult.text() == content
		and: 'the miss operation has not been executed'
			missCounter == 1
	}

	def "Should execute the miss operation before the actual content is retrieved and stored"() {
    given: 'a mocked standard get'
			Get get = Mock()
		and: 'a mocked file cache container'
			CacheContainer cacheContainer = Mock()
		and: 'the miss operation'
			def missOperation = { throw new RuntimeException("Cache operations interrupted") }
		and:
			Boolean mosBefore = true
		and: 'a real cached connection'
			Duration leaseTime = Duration.ofMillis(1000)
			Cache cachedRequest = new Cache (
				get,
				cacheContainer,
				leaseTime,
				missOperation,
				mosBefore
			)

		when: 'the first request to obtain the text is done'
			def actualResult = cachedRequest.connect()
		then: '1 call is done to obtain the text the first time'
			1 * cacheContainer.valid(_) >> false
			0 * get.connect() >> content
			0 * cacheContainer.write(content) >> null
		and:
			def exception = thrown(RuntimeException)
			exception.message == "Cache operations interrupted"
	}

}
