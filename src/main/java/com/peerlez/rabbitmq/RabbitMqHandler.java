package com.peerlez.rabbitmq;

import com.rabbitmq.client.Channel;

/**
 * @author A.Sillanpaa
 *
 */
public interface RabbitMqHandler {

	/**
	 * Publish messages to queues.
	 *
	 * @param message message entity to publish {@link Message}
	 * 
	 */
	void publish(Message message, RabbitMqConnection connection);
	
	/**
	 * Registers and initializes consumer to consume given Broker(exchange).
	 *
	 * @param exchangeName 
	 * 				the exhange name to use on message subscribe.
	 * @param routingKey 
	 * 				the routing key to use for the binding to queue.
	 *
	 * @return new {@link Channel} which is bind to an exchange by the given
	 *         		parameters
	 *
	 */
	Channel subscribe(String exchangeName, String routingKey, 
			RabbitMqConnection connection);

}
