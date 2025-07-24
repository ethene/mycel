package com.quantumresearch.mycel.app.privategroup.invitation;

import com.quantumresearch.mycel.spore.api.sync.Group.Visibility;

interface State {

	int getValue();

	Visibility getVisibility();

	boolean isAwaitingResponse();
}
