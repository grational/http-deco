package it.grational.http.request

/**
 * StandardRequest
 * This class is not instantiable since it requires some members to be defined.
 * The subclasses StandardGet/Head/Delete/Post/Put define what is needed and they
 * are, therefore, instantiable.
 */
abstract class StandardRequest implements HttpRequest {

	protected String    verb
	protected URL       url
	protected String    body
	protected Map       connectionParameters
	protected Proxy     proxy

	@Override
	String text() {
		String result

		this.url.openConnection(this.proxy).with {
			requestMethod = this.verb

			if (this.connectionParameters.connectTimeout)
				setConnectTimeout ( // milliseconds
					this.connectionParameters.connectTimeout
				)
			if (this.connectionParameters.readTimeout)
				setReadTimeout ( // milliseconds
					this.connectionParameters.readTimeout
				)
			if (this.connectionParameters.allowUserInteraction)
				setAllowUserInteraction ( // boolean
					this.connectionParameters.allowUserInteraction
				)
			if (this.connectionParameters.useCaches)
				setUseCaches ( // boolean
					this.connectionParameters.useCaches
				)

			this.connectionParameters.requestProperties.each { k, v ->
				setRequestProperty(k,v)
			}

			if (this.body) {
				doOutput = true
				outputStream.withWriter { writer ->
					writer.write (this.body)
				}
			}

			result = inputStream.text
		}
		return result
	}

	@Override
	String toString() {
		String r
		r  = "verb: ${this.verb}"
		r += "\nurl: ${this.url}"
		if (this.body)
			r += "\nbody: ${this.body}"
		if (this.connectionParameters)
			r += "\nparameters: ${this.connectionParameters}"
		r += "\nproxy: ${this.proxy}"
		return r
	}
}
