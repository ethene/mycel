package com.quantumresearch.mycel.infrastructure;

import com.quantumresearch.mycel.infrastructure.io.DnsModule;
import com.quantumresearch.mycel.infrastructure.mailbox.ModularMailboxModule;
import com.quantumresearch.mycel.infrastructure.network.JavaNetworkModule;
import com.quantumresearch.mycel.infrastructure.plugin.tor.CircumventionModule;
import com.quantumresearch.mycel.infrastructure.socks.SocksModule;
import com.quantumresearch.mycel.infrastructure.system.JavaSystemModule;

import dagger.Module;

@Module(includes = {
		CircumventionModule.class,
		DnsModule.class,
		JavaNetworkModule.class,
		JavaSystemModule.class,
		ModularMailboxModule.class,
		SocksModule.class
})
public class BrambleJavaModule {

}
