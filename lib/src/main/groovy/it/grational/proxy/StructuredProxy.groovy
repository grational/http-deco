package it.grational.proxy

import java.net.Proxy

interface StructuredProxy {
	Proxy      proxy()
	Proxy.Type type()
	String     host()
	Integer    port()
	Boolean    secure()
	Boolean    auth()
	String     username()
	String     password()
}
