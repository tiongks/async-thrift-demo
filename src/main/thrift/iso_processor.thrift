namespace java org.nuhara.demos.thrift

struct Message {
	1: string messageId
	2: string mti
	3: string message
	4: i64 amount
}

struct Response {
	1: string messageId
	2: string responseCode
}

service ISOService {
	Response process(1:Message message)
}