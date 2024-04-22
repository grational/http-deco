package support

import groovy.transform.EqualsAndHashCode

@EqualsAndHashCode
class JsonClass {
	String firstKey
	String secondKey
	List<Integer> arrayKey
	SubJsonClass subObjectKey
}
