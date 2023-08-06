package com.nkia.lucida.account.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.kafka.ConcurrentKafkaListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.DefaultErrorHandler;
import com.nkia.lucida.account.constants.TopicConstants;
import com.nkia.lucida.avro.AccountEventAvro;



@Configuration
public class KafkaConfig {

  public static final String CONCURRENT_KAFKALISTENER_CONTAINERFACTORY =
      "com.nkia.lucida.account.config.KafkaConfig.concurrentKafkaListenerContainerFactory";

  @Value("${edt.topic.num-partitions:3}")
  private int numPartitions;

  @Value("${edt.topic.replication-factor:3}")
  private int replicationFactor;

  @Bean
  public KafkaTemplate<String, AccountEventAvro> kafkaTemplateForAccountEvent(
      ProducerFactory<String, AccountEventAvro> producerFactory) {
    KafkaTemplate<String, AccountEventAvro> kafkaTemplate = new KafkaTemplate<>(producerFactory);
    kafkaTemplate.setObservationEnabled(true);
    return kafkaTemplate;
  }


  @Bean
  public NewTopic newAccountEventTopic() {
    return TopicBuilder.name(TopicConstants.COMMON_ACCOUNT_EVENT).partitions(numPartitions)
        .replicas(replicationFactor).build();
  }


  @Bean(name = CONCURRENT_KAFKALISTENER_CONTAINERFACTORY)
  public ConcurrentKafkaListenerContainerFactory<Object, Object> concurrentKafkaListenerContainerFactory(
      ConcurrentKafkaListenerContainerFactoryConfigurer configurer,
      ConsumerFactory<Object, Object> consumerFactory) {

    ConcurrentKafkaListenerContainerFactory<Object, Object> factory =
        new ConcurrentKafkaListenerContainerFactory<>();
    factory.setConcurrency(3);
    factory.setCommonErrorHandler(new DefaultErrorHandler());
    factory.getContainerProperties().setObservationEnabled(true);
    factory.getContainerProperties().setGroupId(TopicConstants.GROUP_ID);
    configurer.configure(factory, consumerFactory);
    return factory;
  }
}
