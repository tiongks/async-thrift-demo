package org.nuhara.demos.kafka;

import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Logger;

import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;
import org.nuhara.demos.Tracing;

import kafka.javaapi.producer.Producer;

public class TxnProducer {

	private static final Logger logger = Logger.getAnonymousLogger();
//	public static final String TOPIC = "thrift-demo";
	public static final String TOPIC = "foo";
	private KafkaProducer<String, String> kafkaProdcuer;

	public static void main(String[] args) {
		TxnProducer producer = new TxnProducer();
		for (int i = 1; i <= 20; i++) {
			producer.sendMessage(Integer.toString(i*10), "4561abc87321");
		}
		producer.shutdown();
	}

	public TxnProducer() {
		if (null == kafkaProdcuer) {
			Properties properties = new Properties();
//			properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "ec2-34-217-97-121.us-west-2.compute.amazonaws.com:9092");
			properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
			properties.put(ProducerConfig.CLIENT_ID_CONFIG, Tracing.APP_NAME);
			properties.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, 2000);
			properties.put(ProducerConfig.TRANSACTION_TIMEOUT_CONFIG, 1000);
			properties.put(ProducerConfig.LINGER_MS_CONFIG, 100);
			properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
			properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
			properties.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
			properties.put(ProducerConfig.TRANSACTIONAL_ID_CONFIG, UUID.randomUUID().toString());
			properties.put(ProducerConfig.ACKS_CONFIG, "all");
			kafkaProdcuer = new KafkaProducer<String, String>(properties);
			kafkaProdcuer.initTransactions();
		}
	}

	public void sendMessage(String key, String value) {
		final ProducerRecord<String, String> producerRecord = new ProducerRecord<String, String>(TOPIC, key, value);
		kafkaProdcuer.beginTransaction();
		logger.info("sending message: " + key);
		Future<RecordMetadata> future = kafkaProdcuer.send(producerRecord, new Callback() {

			@Override
			public void onCompletion(RecordMetadata metadata, Exception exception) {
				if (null != exception) {
					logger.warning(exception.getMessage());
					exception.printStackTrace();
				} else {
					logger.info("Completed: " + metadata.toString() + ". Offset: " + metadata.offset());
				}
			}
		});
		kafkaProdcuer.commitTransaction();
	}
	
	public void shutdown() {
		kafkaProdcuer.close();
	}

}
