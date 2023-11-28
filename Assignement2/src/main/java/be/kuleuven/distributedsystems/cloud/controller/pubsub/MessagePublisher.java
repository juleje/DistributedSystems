package be.kuleuven.distributedsystems.cloud.controller.pubsub;

import com.google.api.gax.core.GoogleCredentialsProvider;
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
    private final TransportChannelProvider channel = FixedTransportChannelProvider.create(
            GrpcTransportChannel.create(
                    ManagedChannelBuilder.forTarget("localhost:8083").usePlaintext().build()
            )
    );
    private final NoCredentialsProvider credentialsProvider = NoCredentialsProvider.create();


    public Publisher publisher() throws IOException {
        //PUB env
        TopicName topicName = TopicName.of(projectIdPub, topicId);

        return Publisher
                .newBuilder(topicName)
                .build();

        /*
        //DEV env
        TopicName topicName = TopicName.of(projectIdDev, topicId);

        return Publisher
                .newBuilder(topicName)
                .setChannelProvider(channel)
                .setCredentialsProvider(credentialsProvider)
                .build();
         */
    }

    public void topic() throws IOException {
        //DEV env

    TopicName topicName = TopicName.of(projectIdDev,topicId);
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
        //PUB env
        TopicName topicName = TopicName.of(projectIdDev, topicId);
        SubscriptionName subscriptionName = SubscriptionName.of(projectIdDev, subscriptionDev);

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
