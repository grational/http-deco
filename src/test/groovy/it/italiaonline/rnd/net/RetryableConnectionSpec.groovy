package it.italiaonline.rnd.net

import spock.lang.Specification

class RetryableConnectionSpec extends Specification {

	// 1. fields
	String url      = 'https://www.google.it'
	String content  = 'Google homepage content'
	Integer counter = 1
	Integer retries = 3

	SimpleConnection    sconn
	RetryableConnection rconn

  // 2. fixture methods
	// 3. feature methods
	def "Should obtain the result after x retries"() {
		setup:
			sconn = Mock()
			Number.metaClass.getSeconds { delegate * 1000 }
			RetryableConnection rconn = new RetryableConnection (
			                              sconn,     // NetConnection
			                              2.seconds, // baseTimeout
			                              retries    // retries
			                            )

    when: 'the request to obtain the text is done'
			def actualResult = rconn.text()

		then: '2 calls to SimpleConnection are done and the actual content is retrieved'
			2 * sconn.text() >> {
				if ( counter < (retries-1) ) {
					def exMessage = "Attempt ${counter}"
					counter++
					throw new IOException(exMessage)
				}
				return content
			}
			notThrown(RuntimeException)
			actualResult == content
	}

	def "Should exceed the retry connection limit and raise a RuntimeException"() {
		setup:
			sconn = Mock()
			Number.metaClass.getSeconds { delegate * 1000 }
			RetryableConnection rconn = new RetryableConnection (
			                              sconn,     // NetConnection
			                              2.seconds, // baseTimeout
			                              retries    // retries
			                            )

    when: 'the request to obtain the text is done'
			def actualResult = rconn.text()

		then: '3 calls to SimpleConnection are done and 1 RuntimeException is thrown'
			3 * sconn.text() >> {
				if ( counter < (retries+1) ) {
					def exMessage = "Attempt ${counter}"
					counter++
					throw new IOException(exMessage)
				}
				return content
			}
			def exception = thrown(RuntimeException)
			exception.message == "Connection retry limit exceeded"
			actualResult != content
	}
  // 4. helper methods
}
