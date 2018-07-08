package org.nuhara.demos;

import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Logger;

import org.apache.thrift.TException;
import org.apache.thrift.async.AsyncMethodCallback;
import org.apache.thrift.async.TAsyncClientManager;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocolFactory;
import org.apache.thrift.transport.TNonblockingSocket;
import org.apache.thrift.transport.TNonblockingTransport;
import org.apache.thrift.transport.TTransportException;
import org.nuhara.demos.thrift.ISOService;
import org.nuhara.demos.thrift.Message;

import io.opentracing.Tracer;
import io.opentracing.Span;

public class ThriftClient {
	
	final static Logger logger = Logger.getLogger(ThriftClient.class.getCanonicalName());
	final static int NUM_MESSAGES = 10; 
	final ArrayList<Message> responseList = new ArrayList<>();
	Span span;
	
	public static void main(String[] args) {
		ThriftClient client = new ThriftClient();
		client.run();
	}
	
	private void run() {
		TNonblockingTransport transport = null;
		try {
			Tracer tracer = Tracing.initTracer("async-thrift-demo");
			transport = new TNonblockingSocket("localhost", 9090);
			TAsyncClientManager clientManager = new TAsyncClientManager();
			TProtocolFactory protocolFactory = new TBinaryProtocol.Factory();
			ISOService.AsyncClient client = new ISOService.AsyncClient(protocolFactory, clientManager, transport);
			
			for (int i = 1; i <= NUM_MESSAGES; i++) {
				
				Message message = new Message(Integer.toString(i*100), "From the Client", "");
				
//				logger.info("Sending: " + message);
				System.out.println("Sending: " + message);

				span = tracer.buildSpan(message.getMti()).start();
				client.process(message, new ProcessorCallback());
				Thread.sleep(200);
			}
			
			while (responseList.size() < NUM_MESSAGES) {
				Thread.sleep(10);
			}
		} catch (TTransportException e) {
			e.printStackTrace();
		} catch (TException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			transport.close();
		}
	}
	
	class ProcessorCallback implements AsyncMethodCallback<Message> {

		@Override
		public void onComplete(Message response) {
			responseList.add(response);
			span.finish();
			logger.info("Response: " + response.getMti() + "-" + response.getMessage() + " " + response.getResponseCode());	
		}

		@Override
		public void onError(Exception exception) {
			logger.severe("Error Processing Message. " + exception.getMessage());
			exception.printStackTrace();
		}
		
	}

}
