package com.peerlez.rabbitmq;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.ShutdownSignalException;

/**
 * Base Consumer class that used to initialize consumers. Intented to extend
 * this on Consumer implementations. Extends the DefaultConsumer class from
 * RabbitMQ library. To get the {@link Message} from the broker override the
 * {@link #handleMessage(Message)}.
 *
 * @author A.Sillanpaa
 *
 */
public abstract class RabbitMqConsumer extends DefaultConsumer {

	private static final Logger LOG = LoggerFactory
		.getLogger(RabbitMqConsumer.class);

	private Channel _channel;
	private volatile String _consumerTag;
	private boolean autoAck = false;

	/**
	 * Constructs new instance of {@link DefaultConsumer} with given
	 * {@link Channel}.
	 *
	 * @param channel Channel to use by the Consumer
	 *
	 * @throws IOException if an error is encountered
	 */
	public RabbitMqConsumer(Channel channel) throws IOException {
		super(channel);
		_channel = channel;
		basicConsume();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void handleConsumeOk(String consumerTag) {
		_consumerTag = consumerTag;
		LOG.debug("Consumer: {} Received consume OK", consumerTag);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void handleCancelOk(String consumerTag) {
		LOG.debug("Consumer: {} Received cancel OK", consumerTag);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void handleCancel(String consumerTag) throws IOException {
		LOG.debug("Consumer: {} Received cancel", consumerTag);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void handleShutdownSignal(String consumerTag,
		ShutdownSignalException sig) {
		LOG.debug("Consumer: {} Received shutdown signal: {}", consumerTag,
			sig.getMessage());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void handleRecoverOk(String consumerTag) {
		LOG.debug("Consumer: {} Received recover OK", consumerTag);
	}

	/**
	 * Handles a message delivery from the broker.
	 */
	@Override
	public void handleDelivery(String consumerTag, Envelope envelope,
		BasicProperties properties, byte[] body) throws IOException {

		LOG.debug("Consumer: {} Received handle delivery", consumerTag);

		Message message = new Message(properties, body, envelope.getExchange(),
			envelope.getRoutingKey(), envelope.getDeliveryTag());

		LOG.info("Consumer: {} Received message: {}", consumerTag,
			envelope.getDeliveryTag());

		handleMessage(message);
		_channel.basicAck(envelope.getDeliveryTag(), true);
	}

	/**
	 * Starts the Consumer
	 *
	 * @throws IOException if an error is encountered
	 */
	public void basicConsume() throws IOException {

		_channel
			.basicConsume(_channel.queueDeclare().getQueue(), autoAck, this);
	}

	/* (non-Javadoc)
	 *
	 * @see com.rabbitmq.client.DefaultConsumer#getConsumerTag() */
	@Override
	public String getConsumerTag() {
		return _consumerTag;
	}

	/* (non-Javadoc)
	 *
	 * @see com.rabbitmq.client.DefaultConsumer#getChannel() */
	@Override
	public Channel getChannel() {
		return _channel;
	}

	/**
	 * Handles a message delivered by the broker to the consumer.
	 *
	 * This method is expected to be overritten.
	 *
	 * @param message The delivered message
	 */
	public abstract void handleMessage(Message message);
}