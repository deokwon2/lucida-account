package com.nkia.lucida.account.kafka;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import com.nkia.lucida.account.constants.TopicConstants;
import com.nkia.lucida.avro.AccountEventAvro;
import com.nkia.lucida.avro.ActionType;
import com.nkia.lucida.avro.TargetType;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class AccountKafkaProducer {

  private final KafkaTemplate<String, AccountEventAvro> kafkaTemplateAccount;

  public AccountKafkaProducer(KafkaTemplate<String, AccountEventAvro> kafkaTemplateAccount) {
    this.kafkaTemplateAccount = kafkaTemplateAccount;
  }

  public void send(String lUserId, String lOrganizationId, TargetType targetType,
      ActionType actionType, String... ids) {
    if (ids == null || ids.length == 0) {
      return;
    }
    this.send(lUserId, lOrganizationId, targetType, actionType, Arrays.asList(ids));
  }

  public void send(String lUserId, String lOrganizationId, TargetType targetType,
      ActionType actionType, List<String> ids) {
    if (ids == null || ids.isEmpty()) {
      return;
    }
    AccountEventAvro event = AccountEventAvro.newBuilder().setActionType(actionType)
        .setTargetType(targetType).setIds(ids).build();
    this.send(lUserId, lOrganizationId, event);
  }

  @Async
  public void send(String lUserId, String lOrganizationId, AccountEventAvro message) {
    if (message == null) {
      return;
    }

    List<Header> messageHeaders =
        Arrays.asList(new RecordHeader("organizationId", lOrganizationId.getBytes()));

    ProducerRecord<String, AccountEventAvro> producerRecord = new ProducerRecord<>(
        TopicConstants.COMMON_ACCOUNT_EVENT, null, null, null, message, messageHeaders);

    kafkaTemplateAccount.send(producerRecord);

    log.trace("kafkaTemplateAccount.send: [{}][{}][{}][{}]", TopicConstants.COMMON_ACCOUNT_EVENT,
        message.getTargetType(), message.getActionType(),
        message.getIds().stream().collect(Collectors.joining(" ")));
  }

}
