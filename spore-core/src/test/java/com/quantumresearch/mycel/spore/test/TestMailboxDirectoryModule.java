package com.quantumresearch.mycel.spore.test;

import com.quantumresearch.mycel.spore.api.mailbox.MailboxDirectory;

import java.io.File;

import dagger.Module;
import dagger.Provides;

@Module
public class TestMailboxDirectoryModule {

	@Provides
	@MailboxDirectory
	File provideMailboxDirectory() {
		return new File("mailbox");
	}
}
