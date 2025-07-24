package com.quantumresearch.mycel.spore;

import com.quantumresearch.mycel.spore.io.DnsModule;
import com.quantumresearch.mycel.spore.mailbox.ModularMailboxModule;
import com.quantumresearch.mycel.spore.network.JavaNetworkModule;
import com.quantumresearch.mycel.spore.plugin.tor.CircumventionModule;
import com.quantumresearch.mycel.spore.socks.SocksModule;
import com.quantumresearch.mycel.spore.system.JavaSystemModule;

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
