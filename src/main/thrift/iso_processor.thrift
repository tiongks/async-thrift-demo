namespace java org.nuhara.demos.thrift

struct Message {
	1: string mti
	2: string message
	3: string responseCode
}

service ISOService {
	Message process(1:Message message)
}