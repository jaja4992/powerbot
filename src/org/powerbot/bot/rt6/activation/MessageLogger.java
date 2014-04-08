package org.powerbot.bot.rt6.activation;

import java.util.logging.Logger;

import org.powerbot.script.MessageEvent;
import org.powerbot.script.MessageListener;
import org.powerbot.script.rt6.ClientAccessor;
import org.powerbot.script.rt6.ClientContext;

public class MessageLogger extends ClientAccessor implements MessageListener {
	private static final Logger log = Logger.getLogger("Messages");

	public MessageLogger(final ClientContext ctx) {
		super(ctx);
	}

	public void messaged(final MessageEvent e) {
		if (e.getSender().equals("")) {
			log.info("[" + e.getId() + "] " + e.getMessage());
		} else {
			log.info("[" + e.getId() + "] " + e.getSender() + ": " + e.getMessage());
		}
	}
}
