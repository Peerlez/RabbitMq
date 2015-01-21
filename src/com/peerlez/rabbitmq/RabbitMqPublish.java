package com.peerlez.rabbitmq;

import java.io.IOException;
import java.util.UUID;

import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;

/**
 * Provides utilities to publish messages to queues.
 *
 * @author A.Sillanpaa
 *
 */
public final class RabbitMqPublish {

	private static final Logger LOG = LoggerFactory
		.getLogger(RabbitMqPublish.class);

	private static RabbitMqPublish _instance;
	private Channel _channel;
	private static ObjectMapper _jsonObjectMapper = new ObjectMapper();

	/**
	 * Non-argument constructor.
	 */
	public RabbitMqPublish() {
	}

	/**
	 * Gets the singleton instance of this class.
	 *
	 * @return singleton instance of this class
	 */
	public static synchronized RabbitMqPublish instance() {

		if (_instance == null) {
			_instance = new RabbitMqPublish();
		}
		return _instance;
	}

	/**
	 * Constructs the message to publish.
	 *
	 * @param message Message to be published
	 * 
	 * @param rabbitMqConnection The {@link RabbitMqConnectionFactory} to provide the
	 *            {@link Channel} where to publish messages
	 *
	 * @throws IOException if an I/O problem is encountered
	 * @throws RabbitMqClientException if establishing a new channel fails
	 */
	public void send(Message message, RabbitMqConnectionFactory rabbitMqConnection)
		throws IOException, RabbitMqClientException {

		_channel = rabbitMqConnection.newChannel();
		_channel.exchangeDeclare(message.getExchange(), message.getRoutingKey(),
				true);

		_channel.basicPublish(message.getExchange(), message.getRoutingKey(),
			new AMQP.BasicProperties.Builder()
				.deliveryMode(2)
				// persistent
				.contentType(MediaType.APPLICATION_JSON)
				.contentEncoding("UTF-8")
				.correlationId(getUniqueId())
				.messageId(getUniqueId())
				.build(),
			_jsonObjectMapper.writeValueAsBytes(message));

		LOG.info("RabbitMQ message sent: {}", message);
	}

	/**
	 * Generates unique identifiers for the RabbitMq message properties
	 * {@code correlationId} and {@code messageId}.
	 *
	 * @return unique String
	 */
	private static String getUniqueId() {
		return UUID.randomUUID().toString();
	}
}