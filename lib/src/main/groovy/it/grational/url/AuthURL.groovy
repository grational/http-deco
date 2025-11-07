package it.grational.url

interface AuthURL extends URLConvertible {
	Boolean auth()
	String  username()
	String  password()
	String  header()
}
