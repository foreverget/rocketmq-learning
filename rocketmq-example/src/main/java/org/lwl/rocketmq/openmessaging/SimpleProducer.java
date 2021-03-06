package org.lwl.rocketmq.openmessaging;

import io.openmessaging.Message;
import io.openmessaging.MessagingAccessPoint;
import io.openmessaging.MessagingAccessPointFactory;
import io.openmessaging.Producer;
import io.openmessaging.Promise;
import io.openmessaging.PromiseListener;
import io.openmessaging.SendResult;
import org.lwl.rocketmq.common.TopicName;

import java.nio.charset.Charset;

/**
 * @author thinking_fioa
 * @createTime 2018/4/23
 * @description 使用Openmessaging协议，
 */


public class SimpleProducer {
    public static void main(String [] args) {
        final MessagingAccessPoint messagingAccessPoint = MessagingAccessPointFactory
                .getMessagingAccessPoint("openmessaging:rocketmq://127.0.0.1:9876/namespace");

        final Producer producer = messagingAccessPoint.createProducer();

        messagingAccessPoint.startup();
        System.out.printf("MessagingAccessPoint start Ok%n");

        producer.startup();
        System.out.printf("Producer startup OK%n");

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                producer.shutdown();
                messagingAccessPoint.shutdown();
            }
        }));

        {
            Message message = producer.createBytesMessageToTopic(TopicName.OPEN_MESSAGING.getTopicName(), "OMS_HELLO_BODY_1".getBytes(Charset.forName("UTF-8")));
            SendResult sendResult = producer.send(message);
            System.out.printf("Send async message OK, msgId: %s%n", sendResult.messageId());
        }

        {
            Message asynMessage = producer.createBytesMessageToTopic(TopicName.OPEN_MESSAGING.getTopicName(), "OMS_HELLO_BODY_2".getBytes(Charset.forName("UTF-8")));
            final Promise<SendResult> result = producer.sendAsync(asynMessage);
            result.addListener(new PromiseListener<SendResult>() {
                @Override
                public void operationCompleted(Promise<SendResult> promise) {
                    System.out.printf("Send async message OK, msgId: %s%n", promise.get().messageId());
                }

                @Override
                public void operationFailed(Promise<SendResult> promise) {
                    System.out.printf("Send async message Failed, error: %s%n", promise.getThrowable().getMessage());
                }
            });
        }

        {
            producer.sendOneway(producer.createBytesMessageToTopic(TopicName.OPEN_MESSAGING.getTopicName(), "OMS_HELLO_BODY_3".getBytes(Charset.forName("UTF-8"))));
            System.out.printf("Send oneway message OK%n");

        }
    }
}
