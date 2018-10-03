package org.nuhara.demos;

import java.util.Random;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Logger;

import org.apache.thrift.TException;
import org.apache.thrift.async.AsyncMethodCallback;
import org.nuhara.demos.thrift.ISOService;
import org.nuhara.demos.thrift.Message;
import org.nuhara.demos.thrift.Response;

public class ISOProcessorImpl implements ISOService.Iface {

	final static Logger logger = Logger.getLogger(ISOProcessorImpl.class.getCanonicalName());
	final static Random random = new Random();
	final static int MIN_SLEEP = 100;
	final static int MAX_SLEEP = 2000;
	Executor executor;

	@Override
	public Response process(Message message) throws TException {
		
		logger.info("Message Received: " + message.getMti() + "-" + message.getMessage());

//		 introduce some randomness in processing time so that response is not returned in order
		try {
			int sleepTime = ThreadLocalRandom.current().nextInt(MIN_SLEEP, MAX_SLEEP);
			logger.info(message.getMti() + ": Sleeping for " + sleepTime);
			Thread.sleep(sleepTime);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		Response response = new Response();
		
//		executor = Executors.newSingleThreadExecutor();
		
//		ISOService.AsyncIface asyncService = new ISOAsyncProcessorImpl();
//		ResponseHandler<Message> responseHandler = new ResponseHandler<>();
//		asyncService.process(message, responseHandler);
		response.setMessageId(message.getMessageId());
		response.setResponseCode(message.getMti() + ":00");
		logger.info("Done: " + message.getMti());
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
