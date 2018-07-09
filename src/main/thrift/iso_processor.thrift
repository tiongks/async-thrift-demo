namespace java org.nuhara.demos.thrift

struct Message {
	1: string mti
	2: string rrn
	3: string message
	4: string responseCode
}

service ISOService {
	Message process(1:Message message)
}