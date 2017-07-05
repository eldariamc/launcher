//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package fr.theshark34.openlauncherlib.external;

import fr.theshark34.openlauncherlib.JavaUtil;
import fr.theshark34.openlauncherlib.LaunchException;
import fr.theshark34.openlauncherlib.util.LogUtil;
import fr.theshark34.openlauncherlib.util.ProcessLogManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

public class ExternalLauncher {
	private BeforeLaunchingEvent launchingEvent;
	private ExternalLaunchProfile profile;
	private boolean logsEnabled;

	public ExternalLauncher(ExternalLaunchProfile profile) {
		this(profile, (BeforeLaunchingEvent)null);
	}

	public ExternalLauncher(ExternalLaunchProfile profile, BeforeLaunchingEvent launchingEvent) {
		this.logsEnabled = true;
		this.profile = profile;
		this.launchingEvent = launchingEvent;
	}

	public boolean isLogsEnabled() {
		return this.logsEnabled;
	}

	public void setLogsEnabled(boolean logsEnabled) {
		this.logsEnabled = logsEnabled;
	}

	public Process launch() throws LaunchException {
		LogUtil.info(new String[]{"hi-ext"});
		ProcessBuilder builder = new ProcessBuilder(new String[0]);
		ArrayList<String> commands = new ArrayList();
		commands.add(JavaUtil.getJavaCommand());
		commands.addAll(Arrays.asList(JavaUtil.getSpecialArgs()));
		if(this.profile.getMacDockName() != null && System.getProperty("os.name").toLowerCase().contains("mac")) {
			commands.add(JavaUtil.macDockName(this.profile.getMacDockName()));
		}

		if(this.profile.getVmArgs() != null) {
			commands.addAll(this.profile.getVmArgs());
		}

		commands.add("-cp");
		commands.add(this.profile.getClassPath());
		commands.add(this.profile.getMainClass());
		if(this.profile.getArgs() != null) {
			commands.addAll(this.profile.getArgs());
		}

		if(this.profile.getDirectory() != null) {
			builder.directory(this.profile.getDirectory());
		}

		if(this.profile.isRedirectErrorStream()) {
			builder.redirectErrorStream(true);
		}

		builder.command(commands);

		if(this.launchingEvent != null) {
			this.launchingEvent.onLaunching(builder);
		}

		String entireCommand = "";

		String command;
		for(Iterator var4 = commands.iterator(); var4.hasNext(); entireCommand = entireCommand + command + " ") {
			command = (String)var4.next();
		}

		LogUtil.info(new String[]{"ent", ":", entireCommand});
		LogUtil.info(new String[]{"start", this.profile.getMainClass()});

		try {
			Process p = builder.start();
			if(this.logsEnabled) {
				ProcessLogManager manager = new ProcessLogManager(p.getInputStream());
				manager.start();
			}

			return p;
		} catch (IOException var6) {
			throw new LaunchException("Cannot launch !", var6);
		}
	}

	public BeforeLaunchingEvent getLaunchingEvent() {
		return this.launchingEvent;
	}

	public void setLaunchingEvent(BeforeLaunchingEvent launchingEvent) {
		this.launchingEvent = launchingEvent;
	}

	public ExternalLaunchProfile getProfile() {
		return this.profile;
	}

	public void setProfile(ExternalLaunchProfile profile) {
		this.profile = profile;
	}
}
