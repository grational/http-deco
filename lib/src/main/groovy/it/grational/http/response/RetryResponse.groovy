package it.grational.http.response

import groovy.json.JsonSlurper
import java.nio.charset.Charset

/** One complete attempt, resolved on the first access to metadata or content. */
final class RetryResponse extends HttpResponse.StandardResponse {

	private HttpResponse reply
	private Closure<HttpResponse> reopen
	private byte[] content
	private Exception failure

	RetryResponse(HttpResponse first, Closure<HttpResponse> reopen) {
		this.reply = first
		this.reopen = reopen
	}

	private synchronized HttpResponse resolve() {
		if (failure != null) throw failure
		try {
			while (content == null) {
				try {
					content = Objects.requireNonNull(reply.bytes(!reply.error()), "Missing response body")
				} catch (IOException ex) {
					reply = reopen.call(ex)
				}
			}
			return reply
		} catch (Exception ex) {
			failure = ex
			throw ex
		} finally {
			reopen = null
		}
	}

	private byte[] data(Boolean exceptions) {
		HttpResponse complete = resolve()
		if (exceptions && complete.error())
			throw complete.exception()
		return content
	}

	@Override Integer code() { resolve().code() }
	@Override Boolean error() { resolve().error() }
	@Override Throwable exception() { resolve().exception() }
	@Override URL url() { resolve().url() }
	@Override String header(String name) { resolve().header(name) }
	@Override HttpCookie cookie(String name) { resolve().cookie(name) }

	@Override byte[] bytes(Boolean exceptions) {
		byte[] original = data(exceptions)
		return Arrays.copyOf(original, original.length)
	}

	@Override String text(Charset charset, Boolean exceptions) {
		new String(data(exceptions), charset)
	}

	@Override <T> T jsonObject(Class<T> type, Charset charset, Boolean exceptions) {
		new JsonSlurper().parseText(text(charset, exceptions)).asType(type)
	}
}
