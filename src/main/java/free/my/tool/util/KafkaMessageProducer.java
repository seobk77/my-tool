package free.my.tool.util;

import java.util.Properties;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

public class KafkaMessageProducer {
	public KafkaMessageProducer() {
	}
	
	public void sendMessage(String kafkaBlokers, String topic, String message) {
		Properties props = new Properties();
		props.put("bootstrap.servers", kafkaBlokers);
//		props.put("acks", "all");
		props.put("acks", "1");
		props.put("retries", 1);
		props.put("batch.size", 16384);
//		props.put("linger.ms", 1);
		props.put("linger.ms", 0);
		props.put("buffer.memory", 33554432);
		props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
		props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
		
		Producer<String, String> producer = new KafkaProducer<>(props);
		producer.send(new ProducerRecord<>(topic, null, message));
		producer.close();
	}
}
