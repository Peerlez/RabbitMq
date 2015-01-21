package com.peerlez.rabbitmq;

/**
 *
 * Abstract class to hold RabbitMQ exchange types. Contains all avaiable
 * exchange types on RabbitMQ.
 *
 * @author A.Sillanpaa
 *
 */
public abstract class ExchangeType {

	public static final String TOPIC = "topic";
	public static final String FANOUT = "fanout";
	public static final String DIRECT = "direct";
	public static final String HEADRES = "headers";
}
