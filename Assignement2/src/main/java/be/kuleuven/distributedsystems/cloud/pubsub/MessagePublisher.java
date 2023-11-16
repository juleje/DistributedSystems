package be.kuleuven.distributedsystems.cloud.pubsub;

import be.kuleuven.distributedsystems.cloud.Application;
import com.google.api.gax.core.NoCredentialsProvider;
import com.google.api.gax.grpc.GrpcTransportChannel;
import com.google.api.gax.rpc.FixedTransportChannelProvider;
import com.google.api.gax.rpc.TransportChannelProvider;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.pubsub.v1.TopicName;
import io.grpc.ManagedChannelBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.io.IOException;

import static be.kuleuven.distributedsystems.cloud.Application.projectId;
import static be.kuleuven.distributedsystems.cloud.Application.topicId;

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
}
