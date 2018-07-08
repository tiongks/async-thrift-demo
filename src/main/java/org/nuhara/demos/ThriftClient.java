package org.nuhara.demos;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
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

import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.thrift.SpanProtocol;
import io.opentracing.thrift.TracingAsyncMethodCallback;

public class ThriftClient {
	
	final static Logger logger = Logger.getLogger(ThriftClient.class.getCanonicalName());
	final static int NUM_MESSAGES = 10; 
	final ArrayList<Message> responseList = new ArrayList<>();
	Tracer tracer;
	Span span;
	
	public static void main(String[] args) {
		ThriftClient client = new ThriftClient();
		client.run();
	}
	
	private void run() {
		TNonblockingTransport transport = null;
		try {
			tracer = Tracing.initTracer(Tracing.APP_NAME);

			TAsyncClientManager clientManager = new TAsyncClientManager();
			transport = new TNonblockingSocket("localhost", 9090);
			TBinaryProtocol binProt = new TBinaryProtocol(transport);
			TProtocolFactory protocolFactory = new TBinaryProtocol.Factory();
			SpanProtocol.Factory spanFactory = new SpanProtocol.Factory(protocolFactory, tracer, false);
			
			for (int i = 1; i <= NUM_MESSAGES; i++) {
				
				ISOService.AsyncClient client = new ISOService.AsyncClient(spanFactory, clientManager, transport);
				
				Message message = new Message(Integer.toString(i*100), "From the Client", "");
				
				logger.info("Sending: " + message);
				
				span = tracer.buildSpan(message.getMti()).start();
				TracingAsyncMethodCallback<Message> tracingCallback = 
						new TracingAsyncMethodCallback<>(new ProcessorCallback(), spanFactory);
				client.process(message, tracingCallback);
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
