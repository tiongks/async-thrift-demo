package org.nuhara.demos;

import org.apache.thrift.TException;
import org.apache.thrift.async.AsyncMethodCallback;
import org.nuhara.demos.thrift.ISOService;
import org.nuhara.demos.thrift.Message;

public class ISOAsyncProcessorImpl implements ISOService.AsyncIface {

	@Override
	public void process(Message message, AsyncMethodCallback<Message> resultHandler) throws TException {
		resultHandler.onComplete(message);
	}

}
