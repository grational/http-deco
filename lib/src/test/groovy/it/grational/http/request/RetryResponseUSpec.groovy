package it.grational.http.request

import it.grational.http.response.HttpResponse
import spock.lang.Specification

import java.util.concurrent.Callable
import java.util.concurrent.Executors

class RetryResponseUSpec extends Specification {

	def 'status and body share attempts in either access order'() {
		given:
			def request = Mock(HttpRequest)
			def waits = []
			def unavailable = reply(503, 'try later')
			def missing = reply(404, 'archived elsewhere')

		when:
			def response = new Retry(request, 3, { n, total -> waits << [n, total] }).connect()
			def first = bodyFirst ? response.text(false) : response.code()

		then:
			3 * request.connect() >>> [unavailable, interrupted(), missing]
			first == (bodyFirst ? 'archived elsewhere' : 404)
			response.code() == 404
			response.text(false) == 'archived elsewhere'
			waits == [[1, 3], [2, 3]]

		where:
			bodyFirst << [true, false]
	}

	def 'an exhausted body retry stays exhausted across accessors'() {
		given:
			def request = Mock(HttpRequest)
			2 * request.connect() >> { interrupted() }
			def response = new Retry(request, 2, { n, total -> }).connect()

		when:
			response.code()

		then:
			def first = thrown(RuntimeException)
			first.cause instanceof IOException

		when:
			response.text()

		then:
			def second = thrown(RuntimeException)
			first.is(second)
	}

	def 'concurrent readers agree on one body and caller-owned bytes'() {
		given:
			def request = Mock(HttpRequest)
			2 * request.connect() >>> [interrupted(), reply(200, 'done')]
			def executor = Executors.newFixedThreadPool(2)
			def response = new Retry(request, 3, { n, total -> }).connect()
		when:
			def results = executor.invokeAll([
				{ response.code() } as Callable,
				{ response.text() } as Callable
			])*.get()
			def copy = response.bytes()
			copy[0] = 0
		then:
			results == [200, 'done']
			response.text() == 'done'
		cleanup:
			executor.shutdownNow()
	}

	def 'reads a real truncated response again and retains the final headers and body'() {
		given:
			def listener = new ServerSocket(0, 4, InetAddress.getByName('127.0.0.1'))
			def executor = Executors.newSingleThreadExecutor()
			def server = executor.submit({
				['HTTP/1.1 200 OK\r\nContent-Length: 8\r\nConnection: close\r\n\r\nabc',
				 'HTTP/1.1 404 Not Found\r\nX-Attempt: two\r\nContent-Length: 0\r\nConnection: close\r\n\r\n'].each { message ->
					listener.accept().withCloseable { socket ->
						def reader = socket.inputStream.newReader('US-ASCII')
						while (reader.readLine()) {}
						socket.outputStream.write(message.getBytes('US-ASCII'))
						socket.outputStream.flush()
					}
				}
				return true
			} as Callable)
			def request = new Get(url: new URL("http://127.0.0.1:${listener.localPort}/"),
				proxy: Proxy.NO_PROXY, connectTimeout: 3000, readTimeout: 3000)
		when:
			def response = new Retry(request, 2, { n, total -> }).connect()
		then:
			response.code() == 404
			response.header('X-Attempt') == 'two'
			response.text(false) == ''
			server.get(5, java.util.concurrent.TimeUnit.SECONDS)
		cleanup:
			listener.close()
			executor.shutdownNow()
	}

	def 'a mutation can retain connection retries without replaying a partial response'() {
		given:
			def request = Mock(HttpRequest)
			1 * request.connect() >> interrupted()
			def response = new Retry(request, 3, { n, total -> }, false).connect()
		when:
			response.text()
		then:
			thrown(IOException)
	}

	private static HttpResponse reply(int status, String text) {
		new HttpResponse.CustomResponse(status, new ByteArrayInputStream(text.bytes),
			status >= 400, status >= 400 ? new IOException("status ${status}") : null)
	}

	private static HttpResponse interrupted() {
		def stream = new InputStream() {
			@Override int read() { throw new java.net.SocketTimeoutException('body timed out') }
		}
		new HttpResponse.CustomResponse(200, stream)
	}
}
