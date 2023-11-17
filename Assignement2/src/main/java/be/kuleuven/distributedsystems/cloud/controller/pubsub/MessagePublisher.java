package be.kuleuven.distributedsystems.cloud.controller.pubsub;

import com.google.api.gax.core.NoCredentialsProvider;
import com.google.api.gax.grpc.GrpcTransportChannel;
import com.google.api.gax.rpc.FixedTransportChannelProvider;
import com.google.api.gax.rpc.TransportChannelProvider;
import com.google.cloud.pubsub.v1.*;
import com.google.pubsub.v1.*;
import io.grpc.ManagedChannelBuilder;
import org.springframework.stereotype.Component;

import java.io.IOException;

import static be.kuleuven.distributedsystems.cloud.Application.*;

@Component
public class MessagePublisher {

    public Publisher publisher() throws IOException {
        TransportChannelProvider channel = FixedTransportChannelProvider.create(
                GrpcTransportChannel.create(
                        ManagedChannelBuilder.forTarget("localhost:8083").usePlaintext().build()
                )
        );
        var credentialsProvider = NoCredentialsProvider.create();
        TopicName topicName = TopicName.of(projectId, topicId);

        return Publisher
                .newBuilder(topicName)
                .setChannelProvider(channel)
                .setCredentialsProvider(credentialsProvider)
                .build();
    }

    public void topic() throws IOException {
        TransportChannelProvider channel = FixedTransportChannelProvider.create(
                GrpcTransportChannel.create(
                        ManagedChannelBuilder.forTarget("localhost:8083").usePlaintext().build()
                )
        );
        var credentialsProvider = NoCredentialsProvider.create();
        TopicName topicName = TopicName.of(projectId, topicId);

        TopicAdminSettings topicAdminSettings =
                TopicAdminSettings.newBuilder()
                        .setTransportChannelProvider(channel)
                        .setCredentialsProvider(credentialsProvider)
                        .build();
        TopicAdminClient topicAdminClient = TopicAdminClient.create(topicAdminSettings);
        try{
            topicAdminClient.createTopic(topicName);
        }catch (Exception ex){
            System.out.println("Something went wrong with creating the topic: "+ex.getMessage());
        }

    }

    public void subscribe() throws IOException {
        TransportChannelProvider channel = FixedTransportChannelProvider.create(
                GrpcTransportChannel.create(
                        ManagedChannelBuilder.forTarget("localhost:8083").usePlaintext().build()
                )
        );
        var credentialsProvider = NoCredentialsProvider.create();
        TopicName topicName = TopicName.of(projectId, topicId);
        SubscriptionName subscriptionName = SubscriptionName.of(projectId, subscriptionId);

        SubscriptionAdminSettings subscriptionAdminSettings =
                SubscriptionAdminSettings.newBuilder()
                        .setTransportChannelProvider(channel)
                        .setCredentialsProvider(credentialsProvider)
                        .build();
        SubscriptionAdminClient subscriptionAdminClient =
                SubscriptionAdminClient.create(subscriptionAdminSettings);

        PushConfig pushConfig =
                PushConfig.newBuilder().setPushEndpoint("http://localhost:8080/subscription/confirmQuotes").build();
        try{
            subscriptionAdminClient.createSubscription(subscriptionName, topicName, pushConfig, 60);
        }catch (Exception ex){
            System.out.println("Something went wrong with creating the subscriptoin: "+ex.getMessage());
        }

    }
}
