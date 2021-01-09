package it.grational.http.request

import spock.lang.Specification

class StandardGetSpec extends Specification {

	String url     = 'https://www.google.it'
	String content = 'Google homepage content'

  def "Given an URL should return the string version of url and its content"() {
    setup:
			URL mockUrl = GroovyMock()
			mockUrl.getText(_)  >> content
			mockUrl.toString() >> url
    when:
			StandardGet stdGet = new StandardGet(mockUrl)
    then:
			stdGet.text()     == content
			stdGet.toString() == url
  }
}
