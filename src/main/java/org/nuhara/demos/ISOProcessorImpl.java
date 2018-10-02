package org.nuhara.demos;

import java.util.Random;
import java.util.concurrent.Executor;
import java.util.logging.Logger;

import org.apache.thrift.TException;
import org.apache.thrift.async.AsyncMethodCallback;
import org.nuhara.demos.thrift.ISOService;
import org.nuhara.demos.thrift.Message;
import org.nuhara.demos.thrift.Response;

public class ISOProcessorImpl implements ISOService.Iface {

	final static Logger logger = Logger.getLogger(ISOProcessorImpl.class.getCanonicalName());
	final static Random random = new Random();
	Executor executor;

	@Override
	public Response process(Message message) throws TException {
		
		logger.info("Message Received: " + message.getMti() + "-" + message.getMessage());
		
		Response response = new Response();
		
//		executor = Executors.newSingleThreadExecutor();
		
//		ISOService.AsyncIface asyncService = new ISOAsyncProcessorImpl();
//		ResponseHandler<Message> responseHandler = new ResponseHandler<>();
//		asyncService.process(message, responseHandler);
		
		response.setResponseCode("00");
//		 introduce some randomness in processing time so that response is not returned in order
//		try {
//			Thread.sleep(random.nextInt(20)*10);
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
		return response;
	}

	class ResponseHandler<Messasge> implements AsyncMethodCallback<Message> {

		@Override
		public void onComplete(Message message) {
			message.setMessage("From the Server.");
		}

		@Override
		public void onError(Exception exception) {
			// TODO Auto-generated method stub

		}

	}

}
