package org.nuhara.demos.kafka;

import java.util.logging.Logger;

import org.apache.kafka.streams.processor.Processor;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.processor.StateStore;

public class MessageProcessor implements Processor<String, String> {
	
	private final static Logger logger = Logger.getAnonymousLogger();
	private StateStore state;

	@Override
	public void init(ProcessorContext context) {
		this.state = context.getStateStore(Streamer.STATE_STORE_NAME);
		logger.info("MessageProcessor Started.");
	}

	@Override
	public void process(String key, String value) {
		logger.info("Now Processing: " + key + "->" + value);
		for (int i = 1; i <= 10; i++) {
			System.out.println("Pretending to do something for: " + key);
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		logger.info("Exiting process stage for: " + key + "->" + value);
	}

	@Override
	public void punctuate(long timestamp) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void close() {
		logger.info("MessageProcessor Closing.");
	}

}
