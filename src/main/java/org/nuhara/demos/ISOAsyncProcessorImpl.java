package org.nuhara.demos;

import org.apache.thrift.TException;
import org.apache.thrift.async.AsyncMethodCallback;
import org.nuhara.demos.thrift.ISOService;
import org.nuhara.demos.thrift.Message;
import org.nuhara.demos.thrift.Response;

public class ISOAsyncProcessorImpl implements ISOService.AsyncIface {

	@Override
	public void process(Message message, AsyncMethodCallback<Response> resultHandler) throws TException {
		// TODO Auto-generated method stub
		System.out.println("Async Processor.");
		Response response = new Response();
		resultHandler.onComplete(response);
	}
	
}
