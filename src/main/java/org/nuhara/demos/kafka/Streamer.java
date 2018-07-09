package org.nuhara.demos.kafka;

import java.util.Properties;
import java.util.logging.Logger;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.Consumed;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.StoreBuilder;
import org.apache.kafka.streams.state.Stores;
import org.nuhara.demos.Tracing;

public class Streamer {
	
	private static final Logger logger = Logger.getAnonymousLogger();
	public static final String STATE_STORE_NAME = "messageState";

	public static void main(String[] args) {

		Properties consumerConfig = new Properties();
		consumerConfig.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
//		consumerConfig.put(ConsumerConfig.GROUP_ID_CONFIG, "C0");
		consumerConfig.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
		consumerConfig.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
		consumerConfig.put(StreamsConfig.APPLICATION_ID_CONFIG, Tracing.APP_NAME);
		consumerConfig.put(StreamsConfig.CLIENT_ID_CONFIG, "stream-consumer");
//		consumerConfig.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, Serdes.String().getClass().getName());
//		consumerConfig.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, Serdes.String().getClass().getName());
		
		startStreams(consumerConfig);
	}
	
	private static void startStreams(Properties consumerConfig) {
		StreamsBuilder builder = new StreamsBuilder();
		
		KStream<String, String> messages = builder.stream(
			    TxnProducer.TOPIC,
			    Consumed.with(
			      Serdes.String(), /* key serde */
			      Serdes.String()   /* value serde */
			    ));
		
		StoreBuilder<KeyValueStore<String,String>> keyValueStoreBuilder =
		         Stores.keyValueStoreBuilder(Stores.persistentKeyValueStore(STATE_STORE_NAME),
		                 Serdes.String(),
		                 Serdes.String());
		
		builder.addStateStore(keyValueStoreBuilder);
		
		messages.process(() -> new MessageProcessor(), STATE_STORE_NAME);
		
		KafkaStreams streams = new KafkaStreams(builder.build(), consumerConfig);
		
		streams.start();
		
		logger.info("Kafka StreamBuilder Consumer Started.");
		
//		Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
	}

	private static void startProcessor(Properties consumerConfig) {
		
		Topology topology = new Topology();
		topology.addSource("request-source", TxnProducer.TOPIC);
		topology.addProcessor("messageProcessor", () -> new MessageProcessor(), "request-source");
		
		KafkaStreams streams = new KafkaStreams(topology, consumerConfig);
		
		streams.start();
		
		logger.info("Kafka StreamProcessor Consumer Started.");
		
//		Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
	}

}
