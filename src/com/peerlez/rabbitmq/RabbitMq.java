package com.peerlez.rabbitmq;

import java.io.IOException;

import com.rabbitmq.client.Channel;

/**
 * Wrapper to ease of use to publish messages to message queues and to subscribe
 * consumers to Brokers(exchange).
 *
 * @author A.Sillanpaa
 *
 */
public final class RabbitMq implements RabbitMqMessage {

	private static RabbitMq _instance;
	private RabbitMqConnectionFactory _rabbitMqConnection;

	/**
	 * Non-argument constructor.
	 */
	private RabbitMq() {
	}

	/**
	 * Gets the singleton instance of this class.
	 *
	 * @return singleton instance of this class
	 */
	public static synchronized RabbitMq instance() {

		if (_instance == null) {
			_instance = new RabbitMq();
		}
		return _instance;
	}

	/**
	 * Closes all Channels and Connections.
	 */
	public void closeConnection() {
		_rabbitMqConnection.close();
	}

	/**
	 * Publish messages to queues.
	 *
	 * @param message message entity to publish.
	 * 
	 * @throws RabbitMqClientException
	 */
	@Override
	public void publish(Message message, RabbitMqConnection connection) 
			throws RabbitMqClientException {

		RabbitMqConnectionFactory rabbitMqConnection = new 
				RabbitMqConnectionFactory(connection.getConnectionUri());
		rabbitMqConnection.newConnection();

		try {
			RabbitMqPublish.instance().send(message, rabbitMqConnection);
		} catch (IOException e) {
			throw new RabbitMqClientException("cant publish messages", e);
		} finally {
			rabbitMqConnection.close();
		}
	}

	/**
	 * Registers and initializes consumer to consume given Broker(exchange).
	 *
	 * @param exchangeName the exhange name to use on message subscribe.
	 * @param routingKey the routing key to use for the binding to queue.
	 *
	 * @return new {@link Channel} which is bind to an exchange by the given
	 *         parameters
	 *
	 * @throws RabbitMqClientException
	 */
	@Override
	public Channel subscribe(String exchangeName, String routingKey, 
			RabbitMqConnection connection) throws RabbitMqClientException {
		
		_rabbitMqConnection = new RabbitMqConnectionFactory(connection
				.getConnectionUri());
		_rabbitMqConnection.newConnection();
		Channel channel = _rabbitMqConnection.newChannel();

		try {
			channel.queueBind(channel.queueDeclare().getQueue(), exchangeName,
				routingKey);
		} catch (IOException e) {
			throw new RabbitMqClientException(
				"Cant bind the queue to exchange", e);
		}
		return channel;
	}
}