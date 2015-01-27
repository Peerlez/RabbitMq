# RabbitMq Java AMQP library
Java library to help rabbitMq usage on java applications.

Usage:

		private static RabbitMqHandler _sendMsg = new RabbitMq();
		
		private static final String VHOST = "myVhost";
		private static final String USERNAME = "username";
		private static final String PASSWORD = "password";
		private static final String SERVER_ADDRESS = "someServer";
		private static byte[] _body = "someMessageBody".getBytes();
		
		RabbitMqConnection connection = new RabbitMqConnection(VHOST, USERNAME, 
				PASSWORD, SERVER_ADDRESS);
		
		private static BasicProperties _prop = new BasicProperties()
			.builder()
			.contentEncoding("UTF-8")
			.contentType(MediaType.APPLICATION_JSON)
			.deliveryMode(2)
			.messageId("someId")
			.build();
		
		Message msg = new Message(_prop, _body, "exchange", "routingKey", 1);

		_sendMsg.publish(msg, connection);
		_sendMsg.subscribe("exchange", "routingKey", connection);
