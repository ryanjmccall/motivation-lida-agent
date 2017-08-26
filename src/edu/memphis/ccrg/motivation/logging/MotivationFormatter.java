/*******************************************************************************
 * Copyright (c) 2009, 2011 The University of Memphis.  All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the LIDA Software Framework Non-Commercial License v1.0 
 * which accompanies this distribution, and is available at
 * http://ccrg.cs.memphis.edu/assets/papers/2010/LIDA-framework-non-commercial-v1.0.pdf
 *******************************************************************************/
package edu.memphis.ccrg.motivation.logging;

import java.text.MessageFormat;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;

/**
 * Logger formatter for the framework that can be used with any {@link Handler}.
 * @author Javier Snaider
 * @author Ryan J. McCall
 * @see LogManager
 */
public class MotivationFormatter extends Formatter {

	@Override
	public String format(LogRecord record) {
		String formatted = new String("");
		String message = record.getMessage();
		if (message != null) {
			MessageFormat mf = new MessageFormat(message);
			Object[] params = record.getParameters();
			formatted = String.format("%s %n",mf.format(params));
		}
		return formatted;
	}
}
