package org.nuhara.demos.kafka;

import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;
import org.nuhara.demos.Tracing;

public class TxnProducer {
	
	public static final String TOPIC = "thrift-demo";
//	public static final String TOPIC = "foo";
	private KafkaProducer<String, String> producer;
	
	public TxnProducer() {
		if (null == producer) {
			Properties properties = new Properties();
			properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
			properties.put(ProducerConfig.CLIENT_ID_CONFIG, Tracing.APP_NAME);
			properties.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, 2000);
			properties.put(ProducerConfig.LINGER_MS_CONFIG, 100);
			properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
					StringSerializer.class.getName());
			properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
					StringSerializer.class.getName());
//			properties.put(ProducerConfig.TRANSACTIONAL_ID_CONFIG, UUID.randomUUID());
			producer = new KafkaProducer<String, String>(properties);	
		}
	}
	
	public void sendMessage(String key, String value) {
		final ProducerRecord<String, String> producerRecord = new ProducerRecord<String, String>(TOPIC, key, value);
//		producer.initTransactions();
//		producer.beginTransaction();
		Future<RecordMetadata> future = producer.send(producerRecord);
//		producer.commitTransaction();
//		try {
//			RecordMetadata recordMetadata = future.get();
//			System.out.println(recordMetadata);
//		} catch (InterruptedException | ExecutionException e) {
//			producer.abortTransaction();
//			e.printStackTrace();
//		}
	}

}
