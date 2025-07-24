package com.quantumresearch.mycel.spore.plugin.tcp;

import javax.annotation.Nullable;

interface PortMapper {

	@Nullable
	MappingResult map(int port);
}
