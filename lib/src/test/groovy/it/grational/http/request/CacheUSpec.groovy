package it.grational.http.request

import spock.lang.*

import java.time.Duration
import static java.net.HttpURLConnection.*

import it.grational.cache.CacheContainer
import it.grational.http.response.HttpResponse

class CacheUSpec extends Specification {

	final String lineSeparator = '\n'
	final String url           = 'https://www.google.it'
	final List   okResponse    = [ "${HTTP_OK}", 'Google homepage content' ]
	final List   koResponse    = [ "${HTTP_BAD_REQUEST}", 'Client request error' ]

	def setupSpec() {
		List.metaClass.code { delegate.first() as Integer }
		List.metaClass.content { delegate.last() }
	}

	def "Should query the inner HttpRequest class just the first time"() {
		given: 'a mocked http response'
			HttpResponse response = Mock()
			response.code() >> { okResponse.code() }
			response.error() >> false
			response.text() >> { okResponse.content() }
			response.bytes() >> { okResponse.content().bytes }
		and:
			def joinedResponse = joinedResponse(response)
		and: 'a mocked standard get'
			HttpRequest get = Mock()
			get.connect() >> response
		and: 'a mocked file cache container'
			CacheContainer cacheContainer = Mock()
		and: 'the miss operation'
			int missCounter = 0
			def missOperation = { missCounter++ }
		and: 'a real cached connection'
			Duration leaseTime = Duration.ofSeconds(1)
			Cache cachedRequest = new Cache (
				get,
				cacheContainer,
				leaseTime,
				missOperation
			)

		when: 'the first request to obtain the text is done'
			def actualResult = cachedRequest.connect()
		then: '1 call is done to obtain the actual response the first time'
			1 * cacheContainer.valid(_) >> false
			1 * get.connect() >> response
			1 * cacheContainer.write(joinedResponse) >> null
			1 * cacheContainer.content() >> joinedResponse
		and:
			actualResult.code() == HTTP_OK
			actualResult.text() == okResponse.content()
		and: 'the miss operation is executed once'
			missCounter == 1

		when: 'another request to obtain the text is done'
			def cacheResult = cachedRequest.connect()
		then: 'the second time the cache is used'
			1 * cacheContainer.valid(_) >> true
			1 * cacheContainer.content() >> joinedResponse
		and:
			cacheResult.code() == HTTP_OK
			cacheResult.text() == okResponse.content()
		and: 'the miss operation has not been executed'
			missCounter == 1
	}

	def "Should execute the miss operation before the actual content is retrieved and stored"() {
		given: 'a mocked standard get'
			HttpRequest get = Mock()
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
			0 * get.connect()
			0 * cacheContainer.write(okResponse.content()) >> null
		and:
			def exception = thrown(RuntimeException)
			exception.message == "Cache operations interrupted"
	}

	def "Should not cache an error by default"() {
		given: 'a mocked http response'
			HttpResponse response = Mock()
			response.code() >> { koResponse.code() }
			response.error() >> { true }
			response.text() >> { koResponse.content() }
			response.bytes() >> { koResponse.content().bytes }
		and:
			def joinedResponse = joinedResponse(response)
		and: 'a mocked standard get'
			HttpRequest get = Mock()
			get.connect() >> response
		and: 'a mocked file cache container'
			CacheContainer cacheContainer = Mock()
		and: 'the miss operation'
			int missCounter = 0
			def missOperation = { missCounter++ }
		and: 'a real cached connection'
			Duration leaseTime = Duration.ofSeconds(1)
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
			0 * cacheContainer.write(joinedResponse) >> null
		and:
			actualResult.is(response)
			actualResult.code() == HTTP_BAD_REQUEST
			actualResult.text() == koResponse.content()
		and: 'the miss operation is executed once'
			missCounter == 1

		when: 'another request to obtain the text is done'
			def secondResponse = cachedRequest.connect()
		then: 'the second time the cache is used'
			1 * cacheContainer.valid(_) >> false
			1 * get.connect() >> response
		and:
			secondResponse.code() == HTTP_BAD_REQUEST
			secondResponse.text() == koResponse.content()
		and: 'the miss operation has not been executed'
			missCounter == 2
	}

	def "Should cache an error if the cacheError parameter is passed"() {
		given: 'a mocked http response'
			HttpResponse response = Mock()
			response.code() >> { koResponse.code() }
			response.error() >> { true }
			response.text() >> { koResponse.content() }
			response.bytes() >> { koResponse.content().bytes }
		and:
			def joinedResponse = joinedResponse(response)
		and: 'a mocked standard get'
			HttpRequest get = Mock()
			get.connect() >> response
		and: 'a mocked file cache container'
			CacheContainer cacheContainer = Mock()
		and: 'the miss operation'
			int missCounter = 0
			def missOperation = { missCounter++ }
		and:
			Boolean missOpBefore = false
			Boolean cacheErrors  = true
		and: 'a real cached connection'
			Duration leaseTime = Duration.ofSeconds(1)
			Cache cachedRequest = new Cache (
				get,
				cacheContainer,
				leaseTime,
				missOperation,
				missOpBefore,
				cacheErrors
			)

		when: 'the first request to obtain the text is done'
			def actualResult = cachedRequest.connect()
		then: '1 call is done to obtain the text the first time'
			1 * cacheContainer.valid(_) >> false
			1 * get.connect() >> response
			1 * cacheContainer.write(joinedResponse) >> null
			1 * cacheContainer.content() >> { joinedResponse }
		and:
			actualResult.code() == HTTP_BAD_REQUEST
			actualResult.text() == koResponse.content()
		and: 'the miss operation is executed once'
			missCounter == 1

		when: 'another request to obtain the text is done'
			def cachedResult = cachedRequest.connect()
		then: 'the second time the cache is used'
			1 * cacheContainer.valid(_) >> true
			1 * cacheContainer.content() >> joinedResponse
		and:
			cachedResult.code() == HTTP_BAD_REQUEST
			cachedResult.text() == koResponse.content()
		and: 'the miss operation has not been executed'
			missCounter == 1
	}

	private String joinedResponse(HttpResponse response) {
		String.join (
			lineSeparator,
			response.code() as String,
			response.text()
		)
	}

}
