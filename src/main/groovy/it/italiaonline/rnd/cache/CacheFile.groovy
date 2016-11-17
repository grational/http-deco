package it.italiaonline.rnd.cache

class CacheFile { // {{{

	private final File path

	CacheFile(
		String basepath,
		String basename
	) {
		this.path = new File("${basepath}/${basename}")
	}

	Boolean valid(BigInteger leaseTime) {
		(this.path.isFile() && this.newer(leaseTime)) ? true : false
	}

	String content() {
		new ZipEngine(this.path.text).unzipped()
	}

	void write(String input) {
		this.path.getParentFile()?.mkdirs()
		this.path.write(
			new ZipEngine(input).zipped()
		)
	}

	private Boolean newer(BigInteger leaseTime) {
		Long currentEpoch = new Date().getTime()
		Long lastModified = this.path.lastModified()
		( (currentEpoch - lastModified) < leaseTime ) ? true : false
	}

} // }}}
