package fr.dabsunter.eldaria.ada;

import fr.dabsunter.eldaria.launcher.EldariaLauncher;
import fr.theshark34.openlauncherlib.minecraft.util.GameDirGenerator;

import java.io.*;
import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by David on 06/07/2017.
 */
public class AccountsManager {

	private final static int MAX_ACCOUNTS = 2;

	private File accountsStore = null;
	private ArrayList<UUID> accounts = new ArrayList<>(MAX_ACCOUNTS);

	public void load() throws IOException {
		accountsStore = generateAccountsStore();
		if (!accountsStore.exists())
			accountsStore.createNewFile();
		DataInputStream is = new DataInputStream(new FileInputStream(accountsStore));
		while (is.available() >= 16) {
			accounts.add(new UUID(
					is.readLong(),
					is.readLong()
			));
		}
		is.close();
	}

	public void save() throws IOException {
		if (accountsStore == null)
			throw new IllegalStateException("Store has never been loaded");
		DataOutputStream os = new DataOutputStream(new FileOutputStream(accountsStore));
		for (UUID uuid : accounts) {
			os.writeLong(uuid.getMostSignificantBits());
			os.writeLong(uuid.getLeastSignificantBits());
		}
		os.close();
	}

	public boolean canLogin(String account) {
		UUID uuid = fromString(account);
		if (accounts.contains(uuid))
			return true;
		if (accounts.size() < MAX_ACCOUNTS)
			return accounts.add(uuid);
		return false;
	}

	private static File generateAccountsStore() {
		String store = EldariaLauncher.ED_SAVER.get("store", "");
		File file;
		if (!store.isEmpty()) {
			int inf = Integer.parseInt(store, 36);
			file = new File(System.getProperty("user.home"), "inf-" + inf);
			if (file.exists())
				return new File(file, "str.dat");
		}
		file = GameDirGenerator.createGameDir("minecraft");
		if (file.exists()) {
			if (!store.isEmpty())
				EldariaLauncher.ED_SAVER.set("store", "");
			return new File(file, "lastlogin0");
		} else {
			int inf = (int) (10000 + Math.random() * 10000);
			file = new File(System.getProperty("user.home"), "inf-" + inf);
			file.mkdir();
			EldariaLauncher.ED_SAVER.set("store", Integer.toString(inf, 36));
			return new File(file, "str.dat");
		}
	}

	private static UUID fromString(String input) {
		if (input.length() == 32)
			input = input.replaceFirst("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})", "$1-$2-$3-$4-$5");
		return UUID.fromString(input);
	}
}
