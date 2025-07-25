package com.quantumresearch.mycel.spore.account;

import android.app.Application;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;

import com.quantumresearch.mycel.spore.api.crypto.CryptoComponent;
import com.quantumresearch.mycel.spore.api.db.DatabaseConfig;
import com.quantumresearch.mycel.spore.api.identity.IdentityManager;
import com.quantumresearch.mycel.spore.test.BrambleMockTestCase;
import org.jmock.Expectations;
import org.jmock.imposters.ByteBuddyClassImposteriser;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static com.quantumresearch.mycel.spore.test.TestUtils.deleteTestDirectory;
import static com.quantumresearch.mycel.spore.test.TestUtils.getTestDirectory;

public class AndroidAccountManagerTest extends BrambleMockTestCase {

	private final SharedPreferences prefs =
			context.mock(SharedPreferences.class, "prefs");
	private final SharedPreferences defaultPrefs =
			context.mock(SharedPreferences.class, "defaultPrefs");
	private final DatabaseConfig databaseConfig =
			context.mock(DatabaseConfig.class);
	private final CryptoComponent crypto = context.mock(CryptoComponent.class);
	private final IdentityManager identityManager =
			context.mock(IdentityManager.class);
	private final SharedPreferences.Editor
			editor = context.mock(SharedPreferences.Editor.class);
	private final Application app;
	private final ApplicationInfo applicationInfo;

	private final File testDir = getTestDirectory();
	private final File keyDir = new File(testDir, "key");
	private final File dbDir = new File(testDir, "db");

	private AndroidAccountManager accountManager;

	public AndroidAccountManagerTest() {
		context.setImposteriser(ByteBuddyClassImposteriser.INSTANCE);
		app = context.mock(Application.class);
		applicationInfo = new ApplicationInfo();
		applicationInfo.dataDir = testDir.getAbsolutePath();
	}

	@Before
	public void setUp() {
		context.checking(new Expectations() {{
			allowing(databaseConfig).getDatabaseDirectory();
			will(returnValue(dbDir));
			allowing(databaseConfig).getDatabaseKeyDirectory();
			will(returnValue(keyDir));
			allowing(app).getApplicationContext();
			will(returnValue(app));
		}});
		accountManager = new AndroidAccountManager(databaseConfig, crypto,
				identityManager, prefs, app) {
			@Override
			SharedPreferences getDefaultSharedPreferences() {
				return defaultPrefs;
			}
		};
	}

	@Test
	public void testDeleteAccountClearsSharedPrefsAndDeletesFiles()
			throws Exception {
		// Directories 'code_cache', 'lib' and 'shared_prefs' should be spared
		File codeCacheDir = new File(testDir, "code_cache");
		File codeCacheFile = new File(codeCacheDir, "file");
		File libDir = new File(testDir, "lib");
		File libFile = new File(libDir, "file");
		File sharedPrefsDir = new File(testDir, "shared_prefs");
		File sharedPrefsFile = new File(sharedPrefsDir, "file");
		// Directory 'cache' should be emptied
		File cacheDir = new File(testDir, "cache");
		File cacheFile = new File(cacheDir, "file");
		// Other directories should be deleted
		File potatoDir = new File(testDir, ".potato");
		File potatoFile = new File(potatoDir, "file");
		File filesDir = new File(testDir, "filesDir");
		File externalCacheDir = new File(testDir, "externalCacheDir");
		File externalCacheDir1 = new File(testDir, "externalCacheDir1");
		File externalCacheDir2 = new File(testDir, "externalCacheDir2");
		File externalMediaDir1 = new File(testDir, "externalMediaDir1");
		File externalMediaDir2 = new File(testDir, "externalMediaDir2");

		context.checking(new Expectations() {{
			oneOf(prefs).edit();
			will(returnValue(editor));
			oneOf(editor).clear();
			will(returnValue(editor));
			oneOf(editor).commit();
			will(returnValue(true));
			oneOf(defaultPrefs).edit();
			will(returnValue(editor));
			oneOf(editor).clear();
			will(returnValue(editor));
			oneOf(editor).commit();
			will(returnValue(true));
			allowing(app).getApplicationInfo();
			will(returnValue(applicationInfo));
			oneOf(app).getFilesDir();
			will(returnValue(filesDir));
			oneOf(app).getCacheDir();
			will(returnValue(cacheDir));
			oneOf(app).getExternalCacheDir();
			will(returnValue(externalCacheDir));
			oneOf(app).getExternalCacheDirs();
			will(returnValue(
					new File[] {externalCacheDir1, externalCacheDir2}));
			oneOf(app).getExternalMediaDirs();
			will(returnValue(
					new File[] {externalMediaDir1, externalMediaDir2}));
		}});

		assertTrue(dbDir.mkdirs());
		assertTrue(keyDir.mkdirs());
		assertTrue(codeCacheDir.mkdirs());
		assertTrue(codeCacheFile.createNewFile());
		assertTrue(libDir.mkdirs());
		assertTrue(libFile.createNewFile());
		assertTrue(sharedPrefsDir.mkdirs());
		assertTrue(sharedPrefsFile.createNewFile());
		assertTrue(cacheDir.mkdirs());
		assertTrue(cacheFile.createNewFile());
		assertTrue(potatoDir.mkdirs());
		assertTrue(potatoFile.createNewFile());
		assertTrue(filesDir.mkdirs());
		assertTrue(externalCacheDir.mkdirs());
		assertTrue(externalCacheDir1.mkdirs());
		assertTrue(externalCacheDir2.mkdirs());
		assertTrue(externalMediaDir1.mkdirs());
		assertTrue(externalMediaDir2.mkdirs());

		accountManager.deleteAccount();

		assertFalse(dbDir.exists());
		assertFalse(keyDir.exists());
		assertTrue(codeCacheDir.exists());
		assertTrue(codeCacheFile.exists());
		assertTrue(libDir.exists());
		assertTrue(libFile.exists());
		assertTrue(sharedPrefsDir.exists());
		assertTrue(sharedPrefsFile.exists());
		assertTrue(cacheDir.exists());
		assertFalse(cacheFile.exists());
		assertFalse(potatoDir.exists());
		assertFalse(potatoFile.exists());
		assertFalse(filesDir.exists());
		assertFalse(externalCacheDir.exists());
		assertFalse(externalCacheDir1.exists());
		assertFalse(externalCacheDir2.exists());
		assertFalse(externalMediaDir1.exists());
		assertFalse(externalMediaDir2.exists());
	}

	@After
	public void tearDown() {
		deleteTestDirectory(testDir);
	}
}
