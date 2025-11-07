package it.grational.url

import spock.lang.*

/**
 * This class check the OpenSslHTTPCheck class against 3 types of fake
 * webservers.
 */
class QParamsFilterUSpec extends Specification {

  /*
   * Test against a fake HTTP2 website
   */
  def "Should filter out mutable parameters (taboo)"() {
    setup:
			def url = new URL('https://graph.facebook.com/v2.8/search?type=adgeolocation&access_token=super_secrec_access_token&country_code=IT&region_id=friuli-venezia%20giulia&location_types=city&q=fiume+veneto')
    expect:
			output == new QParamsFilter(url,taboo).filtered()
    where:
			taboo                                                                   | output
			[]                                                                      | 'https://graph.facebook.com/v2.8/search?type=adgeolocation&access_token=super_secrec_access_token&country_code=IT&region_id=friuli-venezia%20giulia&location_types=city&q=fiume+veneto'
			['type','access_token','country_code','region_id','location_types','q'] | 'https://graph.facebook.com/v2.8/search'
			['type','access_token','location_types']                                | 'https://graph.facebook.com/v2.8/search?country_code=IT&region_id=friuli-venezia%20giulia&q=fiume+veneto'
  }
}
