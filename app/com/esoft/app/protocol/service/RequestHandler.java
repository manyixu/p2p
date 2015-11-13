package com.esoft.app.protocol.service;

import com.esoft.app.protocol.model.In;
import com.esoft.app.protocol.model.Out;

public interface RequestHandler {
	public Out handle(In in,Out out);
}
