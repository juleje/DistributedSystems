package be.kuleuven.distributedsystems.cloud.pubsub;

import be.kuleuven.distributedsystems.cloud.Application;
import com.google.api.gax.core.NoCredentialsProvider;
import com.google.api.gax.grpc.GrpcTransportChannel;
import com.google.api.gax.rpc.FixedTransportChannelProvider;
import com.google.api.gax.rpc.TransportChannelProvider;
import com.google.cloud.pubsub.v1.*;
import com.google.pubsub.v1.*;
import io.grpc.ManagedChannelBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.io.IOException;

import static be.kuleuven.distributedsystems.cloud.Application.*;

@Service
public class MessagePublisher {


    @Bean
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

    public Topic topic() throws IOException {
        System.out.println("Creating topic");
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
        return topicAdminClient.createTopic(topicName);
    }

    public Subscription subscribe() throws IOException {
        System.out.println("creating subscription");
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

        PushConfig pushConfig = PushConfig.newBuilder().setPushEndpoint("http://localhost:8080/subscription/confirmQuotes").build();
        System.out.println("pushconfig: " + pushConfig);
        System.out.println("Creating push subscription: " + subscriptionName);
        return subscriptionAdminClient.createSubscription(subscriptionName, topicName, pushConfig, 60);
    }
}
