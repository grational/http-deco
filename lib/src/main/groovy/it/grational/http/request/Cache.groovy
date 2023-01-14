package it.grational.http.request

import java.time.Duration
import static java.nio.charset.StandardCharsets.*
import static java.net.HttpURLConnection.*

import it.grational.cache.CacheContainer
import it.grational.http.response.Response
import it.grational.http.response.HttpResponse
import it.grational.http.response.Stream

class Cache implements HttpRequest {

	private final HttpRequest    origin
	private final CacheContainer cacheContainer
	private final Duration       leaseTime
	private final Closure        missOperation
	private final Boolean        missOpBefore
	private final String         separator = '\n'

	Cache (
		HttpRequest org,
		CacheContainer cc,
		Duration lt,
		Closure mos = {},
		Boolean mosBefore = false
	) {
		this.origin         = org
		this.cacheContainer = cc
		this.leaseTime      = lt
		this.missOperation  = mos
		this.missOpBefore   = mosBefore
	}

	@Override
	HttpResponse connect() {
		HttpResponse response
		if ( this.cacheContainer.valid(this.leaseTime) ) {
			List cacheLines = this.cacheLines()
			response = new HttpResponse.CustomResponse (
				this.cachedCode(cacheLines),
				this.cachedContent(cacheLines)
			)
		} else {
			if ( this.missOpBefore )
				this.missOperation()

			response = this.origin.connect()
			Stream source = (response.code() == HTTP_OK)
			              ? Stream.INPUT
			              : Stream.ERROR

				this.cacheContainer.write (
					this.joinedResponse (
						source,
						response
					)
				)

			if ( ! this.missOpBefore )
				this.missOperation()
		}
		return response
	}

	@Override
	String toString() {
		this.origin.toString()
	}

	private List<String> cacheLines() {
		this.cacheContainer
			.content()
			.readLines()
	}

	private Integer cachedCode(List lines) {
		lines.first() as Integer
	}

	private ByteArrayInputStream cachedContent(List lines) {
		new ByteArrayInputStream (
			lines.drop(1).join(this.separator).getBytes()
		)
	}

	private String joinedResponse (
		Stream source,
		HttpResponse response
	) {
		String.join (
			this.separator,
			response.code() as String,
			response.text(source, UTF_8.name())
		)
	}

}
