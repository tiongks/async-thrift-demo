package org.nuhara.demos;

import java.util.logging.Logger;

import org.apache.thrift.TProcessor;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.server.TNonblockingServer;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TSimpleServer;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TNonblockingServerSocket;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TServerTransport;
import org.apache.thrift.transport.TTransportException;
import org.nuhara.demos.thrift.ISOService;
import org.nuhara.demos.thrift.ISOService.AsyncIface;
import org.nuhara.demos.thrift.ISOService.AsyncProcessor;
import org.nuhara.demos.thrift.ISOService.Iface;
import org.nuhara.demos.thrift.ISOService.Processor;

import io.opentracing.Tracer;
import io.opentracing.thrift.SpanProcessor;

public class ThriftServer {
	
	private static final Logger logger = Logger.getLogger(ThriftServer.class.getCanonicalName());
	
	public static ISOService.Processor<ISOService.Iface> processor;
	public static ISOService.AsyncProcessor<ISOService.AsyncIface> asyncProcessor;
	static Tracer tracer;
	
	public static void main(String[] args) {
		processor = new ISOService.Processor<ISOService.Iface>(new ISOProcessorImpl());
		
		asyncProcessor = new ISOService.AsyncProcessor<ISOService.AsyncIface>(new ISOAsyncProcessorImpl());
		
//		Runnable runnable = new Runnable() {
//			@Override
//			public void run() {
//				simpleServer(processor);
//				nonBlockingServer(processor);
//			}
//		};
//		new Thread(runnable).start();
		
		tracer = Tracing.initTracer(Tracing.APP_NAME);
		ThriftServer thriftServer = new ThriftServer();
		thriftServer.nonBlockingServer(processor);
	}
	
	public void simpleServer(ISOService.Processor<ISOService.Iface> processor) {
		try {
			TServerTransport transport = new TServerSocket(9090);
			TServer server = new TSimpleServer(new TServer.Args(transport).processor(processor));
			logger.info("Starting Thrift Simple Server.");
			server.serve();
		} catch (TTransportException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void nonBlockingServer(Processor<Iface> processor2) {
		try {
			TProcessor spanProcessor = new SpanProcessor(processor2, tracer);
			
			TNonblockingServerSocket socket = new TNonblockingServerSocket(9090);
			TNonblockingServer.Args args = new TNonblockingServer.Args(socket);
			args.protocolFactory(new TBinaryProtocol.Factory());
			args.transportFactory(new TFramedTransport.Factory());
//			args.processorFactory(new TProcessorFactory(processor));
			args.processor(spanProcessor);
			TServer server = new TNonblockingServer(args);
			logger.info("Starting Thrift Non-Blocking Server.");
			server.serve();
		} catch (TTransportException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
