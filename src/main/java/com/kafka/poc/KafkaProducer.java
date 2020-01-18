package com.kafka.poc;

import com.kafka.poc.avro.Address;
import com.kafka.poc.avro.Person;
import io.codearte.jfairy.Fairy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.stream.IntStream;

@Component
public class KafkaProducer implements ApplicationRunner {
    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaProducer.class);
    private final KafkaTemplate<Person, Address> kafka;
    private final Fairy fairy = Fairy.create(Locale.US);

    @Value("${de.codecentric.sbkaavro.topic}")
    private String topic;
    @Value("${de.codecentric.sbkaavro.records}")
    private Integer numRecords = 1;

    public KafkaProducer(
            @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection") KafkaTemplate<Person, Address> kafka) {
        this.kafka = kafka;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        IntStream.rangeClosed(1, numRecords).boxed()
                .map(i -> fairy.person())
                .forEach(f -> {
                    Person person = Person.newBuilder()
                            .setFirstName(f.getFirstName())
                            .setLastName(f.getLastName())
                            .build();
                    Address address = Address.newBuilder()
                            .setZip(f.getAddress().getPostalCode())
                            .setCity(f.getAddress().getCity())
                            .setStreet(f.getAddress().getStreet())
                            .setStreetNumber(f.getAddress().getStreetNumber())
                            .build();

                    LOGGER.info("producing {}, {}", person, address);
                    kafka.send(topic, person, address);
                });
    }
}
