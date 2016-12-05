package it.italiaonline.rnd.net

import spock.lang.Specification

class SimpleConnectionSpec extends Specification {

	String url     = 'https://www.google.it'
	String content = 'Google homepage content'

  def "Given an URL should return the string version of url and its content"() {
    setup:
			URL mockUrl = GroovyMock()
			mockUrl.getText()  >> content
			mockUrl.toString() >> url
    when:
			SimpleConnection sconn = new SimpleConnection(mockUrl)
    then:
			sconn.text()      == content
			sconn.toString()  == url
  }
}
