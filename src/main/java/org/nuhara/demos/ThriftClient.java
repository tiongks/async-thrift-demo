package org.nuhara.demos;

import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;
import java.util.logging.Logger;

import org.apache.thrift.TException;
import org.apache.thrift.async.AsyncMethodCallback;
import org.apache.thrift.async.TAsyncClientManager;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.protocol.TProtocolFactory;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TNonblockingSocket;
import org.apache.thrift.transport.TNonblockingTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransportException;
import org.nuhara.demos.thrift.ISOService;
import org.nuhara.demos.thrift.Message;
import org.nuhara.demos.thrift.Response;

import io.opentracing.Span;
import io.opentracing.Tracer;

public class ThriftClient {
	
	final static Logger logger = Logger.getLogger(ThriftClient.class.getCanonicalName());
	final static int NUM_MESSAGES = 10;
	final TSocket socket = new TSocket("localhost", 9090);
	final ArrayList<Response> responseList = new ArrayList<>();
	Tracer tracer;
	Span span;
	
	public static void main(String[] args) {
		ThriftClient client = new ThriftClient();
		client.run();
	}
	
	private void run() {
		TNonblockingTransport nonBlockingTransport = null;
		TFramedTransport framedTransport  = null;
		try {
			tracer = Tracing.initTracer(Tracing.APP_NAME);
			
			nonBlockingTransport = new TNonblockingSocket("localhost", 9090);
			framedTransport = new TFramedTransport(socket);
			TProtocol protocol = new TCompactProtocol(framedTransport);
//			TProtocol protocol = new TBinaryProtocol(framedTransport);
			
			framedTransport.open();
			
			TAsyncClientManager clientManager = new TAsyncClientManager();
			TProtocolFactory protocolFactory = new TCompactProtocol.Factory();
//			TBinaryProtocol binProt = new TBinaryProtocol(framedTransport);
			
			for (int i = 1; i <= NUM_MESSAGES; i++) {
				
				ISOService.AsyncClient asyncClient = new ISOService.AsyncClient(protocolFactory, clientManager, nonBlockingTransport);
				
//				ISOService.Client client = new ISOService.Client(protocol);
				
				Message message = new Message(UUID.randomUUID().toString(), "mti" + i, "message: " + i, 1L);
				logger.info("Sending: " + message);
				
				span = tracer.buildSpan(message.getMti()).start();
//				client.process(message);
				ProcessorCallback resultHandler = new ProcessorCallback();
				asyncClient.process(message, resultHandler);
				Thread.sleep(100);
			}
			
			while (responseList.size() < NUM_MESSAGES) { 
				Thread.sleep(10);
			}
		} catch (TTransportException e) {
			e.printStackTrace();
		} catch (TException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (framedTransport != null) {
				framedTransport.close();
			}
		}
	}
	
	class ProcessorCallback implements AsyncMethodCallback<Response> {

		@Override
		public void onComplete(Response response) {
//			responseList.add(response);
			logger.info("Response: " +  response.getMessageId() + " : " + response.getResponseCode());	
		}

		@Override
		public void onError(Exception exception) {
			logger.severe("Error Processing Message. " + exception.getMessage());
			exception.printStackTrace();
		}
		
	}

}
