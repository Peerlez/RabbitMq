package com.peerlez.rabbitmq;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

/**
 * Connection factory provides ONE SINGLE connection to a RabbitMQ message
 * broker.
 *
 * @author A.Sillanpaa
 *
 */
public class RabbitMqConnectionFactory {

	private static final Logger LOG = LoggerFactory
		.getLogger(RabbitMqConnectionFactory.class);

	private Connection _connection;
	private Channel _channel;
	private ConnectionFactory _factory;
	private String _connectionUri;

	/**
	 * Constructor to get the AMQP URI to establish new connections.
	 *
	 * @param ConnectionUri the AMQP URI. Valid Connection properties fields in
	 *            an AMQP URI are: host, port, username, password and virtual
	 *            host.
	 */
	public RabbitMqConnectionFactory(String connectionUri) {
		_connectionUri = connectionUri;
	}

	/**
	 * Gets a new connection from the {@link ConnectionFactory}. As this factory
	 * only provides one connection. Every subsequent call will return the same
	 * instance of the connection.
	 *
	 * @return The Connection
	 *
	 * @throws RabbitMqClientException if establishing a new connection fail
	 */
	public Connection newConnection() throws RabbitMqClientException {
		if (_connection == null || !_connection.isOpen()) {
			establishConnection();
		}
		return _connection;
	}

	/**
	 * Gets a new channel. Only one channel can be provided.
	 *
	 * @return The Channel
	 *
	 * @throws RabbitMqClientException if establishing a new channel fails
	 */
	public Channel newChannel() throws RabbitMqClientException {
		if (_channel == null || !_channel.isOpen()) {
			try {
				establishChannel();
			} catch (IOException e) {
				// if no established channel could not be retrieved
				throw new RabbitMqClientException("Unable to retrieve channel",
					e);
			}
		}
		return _channel;
	}

	/**
	 * Close the channel {@link Channel} and the underlying connection
	 * {@link Connection}.
	 */
	protected void close() {
		channelClose();
		connectionClose();
	}

	/**
	 * Establishes a new {@link Connection} and Sets the Connection properties
	 * from the AMQP URI. Valid Connection properties fields in an AMQP URI are:
	 * host, port, username, password and virtual host.
	 *
	 * @throws RabbitMqClientException if setting the AMQP URI
	 *             {@link #setConnectionURI} fails; or if establishing a new
	 *             connection fails
	 */
	private void establishConnection() throws RabbitMqClientException {
		try {
			setConnectionURI();
			_connection = _factory.newConnection();

		} catch (IOException e) {
			throw new RabbitMqClientException("Failed to establish "
				+ "connection to: " + _connection.getAddress(), e);
		}
	}

	/**
	 * Establishes a new {@link Channel}.
	 *
	 * @throws IOException if establishing a new channel fails
	 */
	private void establishChannel() throws IOException {
		_channel = _connection.createChannel();
	}

	/**
	 * Sets the Connection properties from the AMQP URI. Valid Connection
	 * properties fields in an AMQP URI are: host, port, username, password and
	 * virtual host.
	 *
	 * @throws RabbitMqClientException if the connection URI is malformed or it
	 *             can't be decoded or URI decode algorithm isn't found.
	 */
	private void setConnectionURI() throws RabbitMqClientException {
		try {
			_factory = new ConnectionFactory();
			_factory.setUri(_connectionUri);
		} catch (KeyManagementException | NoSuchAlgorithmException e) {
			throw new RabbitMqClientException(e);
		} catch (URISyntaxException e) {
			throw new RabbitMqClientException("Connection URI malformed", e);
		}

		String vHost = _factory.getVirtualHost();

		// do like this because of the need for "/" on /virtualhost
		StringBuilder builder = new StringBuilder("/");
		builder.append(vHost);

		_factory.setVirtualHost(builder.toString());
	}

	/**
	 * Close the channel {@link Channel} and the underlying connection
	 * {@link Connection}. Not necessary as it happens implicitly anyway.
	 */
	private void channelClose() {
		if (_channel != null) {
			try {
				_channel.close();
				_channel = null;
			} catch (IOException e) {
				if (!_channel.isOpen()) {
					LOG.warn("Attempt to close an already closed channel");
				} else {
					LOG.error("Unable to close current channel", e);
				}
			}
		}
	}

	/**
	 * Close the connection {@link Connection}. If I/O exception is throwed then
	 * abort and close connection and it's channels.
	 */
	private void connectionClose() {
		if (_connection != null) {
			try {
				_connection.close();
				_connection = null;
			} catch (IOException e) {
				if (!_connection.isOpen()) {
					LOG.warn("Attempt to close an already closed connection");
				} else {
					LOG.error("Unable to close current connection", e);
					_connection.abort();
				}
			}
		}
	}
}