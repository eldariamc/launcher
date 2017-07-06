package fr.dabsunter.eldaria.launcher;

import fr.dabsunter.eldaria.ada.AccountsManager;
import fr.dabsunter.scam.Scam;
import fr.theshark34.openauth.AuthPoints;
import fr.theshark34.openauth.AuthenticationException;
import fr.theshark34.openauth.Authenticator;
import fr.theshark34.openauth.model.AuthAgent;
import fr.theshark34.openauth.model.AuthError;
import fr.theshark34.openauth.model.response.AuthResponse;
import fr.theshark34.openauth.model.response.RefreshResponse;
import fr.theshark34.openlauncherlib.LaunchException;
import fr.theshark34.openlauncherlib.external.BeforeLaunchingEvent;
import fr.theshark34.openlauncherlib.external.ExternalLaunchProfile;
import fr.theshark34.openlauncherlib.external.ExternalLauncher;
import fr.theshark34.openlauncherlib.minecraft.*;
import fr.theshark34.openlauncherlib.util.CrashReporter;
import fr.theshark34.openlauncherlib.util.Saver;
import fr.theshark34.supdate.BarAPI;
import fr.theshark34.supdate.SUpdate;
import fr.theshark34.supdate.application.integrated.FileDeleter;
import fr.theshark34.swinger.Swinger;
import fr.theshark34.swinger.colored.SColoredBar;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Created by David on 04/06/2016.
 */
public class EldariaLauncher
{
	public static final GameVersion ED_VERSION = new GameVersion("1.7.10", GameType.V1_7_10);
	public static final GameInfos ED_INFOS = new GameInfos("Eldaria", ED_VERSION, null);
	public static final File ED_DIR = ED_INFOS.getGameDir();
	public static final Saver ED_SAVER = new Saver(new File(ED_DIR, "eldaria_dev.properties"));
	public static final CrashReporter ED_CRASH = new CrashReporter(ED_INFOS.getServerName(), new File(ED_DIR, "crashs"));
	public static final String ED_URL = "https://api.eldaria.fr";
	public static final Authenticator ED_AUTH = new Authenticator(ED_URL.concat("/auth/") /*Authenticator.MOJANG_AUTH_URL*/, AuthPoints.NORMAL_AUTH_POINTS);
	public static final SUpdate ED_UPDATER = new SUpdate(ED_URL.concat("/updater"), ED_DIR);
	public static final AccountsManager ED_ACCOUNTS = new AccountsManager();
	public static Font ED_FONT_BOLD;
	public static Font ED_FONT_LOW;
	private static AuthInfos authInfos;
	private static Thread updateThread;

	public static void main(String[] args)
	{
		Scam.check("EldariaLauncher");

		Swinger.setSystemLookNFeel();
		Swinger.setResourcePath("/");

		ED_UPDATER.addApplication(new FileDeleter());

		try
		{
			ED_FONT_BOLD = Font.createFont(Font.TRUETYPE_FONT, getResourceAsStream("edosz.ttf"));
			GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(ED_FONT_BOLD);
			ED_FONT_LOW = Font.createFont(Font.TRUETYPE_FONT, getResourceAsStream("abeezee.ttf"));
			GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(ED_FONT_LOW);
		}
		catch (FontFormatException | IOException e)
		{
			e.printStackTrace();
		}

		try {
			ED_ACCOUNTS.load();
		} catch (IOException e) {
			System.err.println("Failed to load the store");
			e.printStackTrace();
		}

		new LauncherFrame().setVisible(true);
	}

	public static void auth(String user, String pass) throws AuthenticationException
	{
		AuthResponse response = ED_AUTH.authenticate(
				AuthAgent.MINECRAFT, user, pass, ED_SAVER.get("client-token", null));

		ED_SAVER.set("client-token", response.getClientToken());

		setAuthInfos(
				response.getSelectedProfile().getName(),
				response.getAccessToken(),
				response.getSelectedProfile().getId()
		);
	}

	public static void refresh() throws AuthenticationException
	{
		RefreshResponse response = ED_AUTH.refresh(
				ED_SAVER.get("access-token"), ED_SAVER.get("client-token")
		);

		setAuthInfos(
				response.getSelectedProfile().getName(),
				response.getAccessToken(),
				response.getSelectedProfile().getId()
		);
	}

	public static void setAuthInfos(String username, String accessToken, String uuid) throws AuthenticationException {
		if (uuid != null && !uuid.isEmpty())
			if (!ED_ACCOUNTS.canLogin(uuid))
				throw new AuthenticationException(new AuthError("TooManyAccounts", "La limite de comptes sur ce PC a été atteinte !", ""));
		authInfos = new AuthInfos(
				username,
				accessToken,
				uuid
		);
		LauncherFrame.getInstance().getPanel().setAuthText(
				"Bienvenue " + username
		);
	}

	public static void saveInfos(boolean keepLogin)
	{
		ED_SAVER.set("username", authInfos.getUsername());
		String accessToken = "";
		if (keepLogin)
			accessToken = authInfos.getAccessToken();
		ED_SAVER.set("access-token", accessToken);
		try {
			ED_ACCOUNTS.save();
		} catch (IOException e) {
			System.err.println("Failed to load the store");
			e.printStackTrace();
		}
	}

	public static void update() throws Exception
	{
		updateThread = new Thread()
		{
			private int val;
			private int max;

			public void run()
			{
				SColoredBar progressBar = LauncherFrame.getInstance().getPanel().getProgressBar();
				progressBar.setVisible(true);
				while (!isInterrupted())
				{
					if (BarAPI.getNumberOfFileToDownload() == 0)
					{
						LauncherFrame.getInstance().getPanel().setInfoText("Vérification des fichiers...");
						continue;
					}
					this.val = (int) (BarAPI.getNumberOfTotalDownloadedBytes()/1000);
					this.max = (int) (BarAPI.getNumberOfTotalBytesToDownload()/1000);

					progressBar.setValue(this.val);
					progressBar.setMaximum(this.max);

					LauncherFrame.getInstance().getPanel().setInfoText("Téléchargement des fichiers " +
							BarAPI.getNumberOfDownloadedFiles() + "/" + BarAPI.getNumberOfFileToDownload() +
							" " + Swinger.percentage(this.val, this.max) + "%");
				}
				progressBar.setVisible(false);
			}
		};
		updateThread.start();
		ED_UPDATER.start();
		updateThread.interrupt();
	}

	public static void interruptUpdateThread()
	{
		updateThread.interrupt();
	}

	public static void launch() throws LaunchException, InterruptedException {
		ExternalLaunchProfile profile = MinecraftLauncher.createExternalProfile(ED_INFOS,
				new GameFolder("assets", "libraries", "bin/natives", "bin/eldaria.jar"), authInfos);

		AllowedMemory am = AllowedMemory.XMX1G;
		try {
			am = AllowedMemory.valueOf(ED_SAVER.get("allowed-memory", "XMX1G"));
		} catch (IllegalArgumentException ex) {}
		profile.getVmArgs().addAll(0, am.getVmArgs());

		profile.getArgs().add("--demo");

		ExternalLauncher launcher = new ExternalLauncher(profile, new BeforeLaunchingEvent() {
			@Override
			public void onLaunching(ProcessBuilder processBuilder) {
				String javaPath = ED_SAVER.get("java-path", "");
				if (javaPath != null && !javaPath.equals(""))
					processBuilder.command().set(0, javaPath);
			}
		});

		LauncherFrame.getInstance().setVisible(false);
		int exitCode = launcher.launch().waitFor();
		System.out.println("\nMinecraft finished with exit code " + exitCode);
		System.exit(0);
	}

	public static String reportException(Exception e)
	{
		try {
			File file = ED_CRASH.writeError(e);
			return file.getCanonicalPath();
		} catch (IOException ex) {
			ex.printStackTrace();
			return "les logs du launcher.";
		}
	}

	public static void browseOnDesktop(String uri)
	{
		Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
		if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
			try {
				desktop.browse(new URI(uri));
			} catch (URISyntaxException | IOException ex) {
				ex.printStackTrace();
			}
		} else {
			System.err.println("Browse on Desktop is not supported !");
			System.err.println("But here is the link : " + uri);
		}
	}

	public static void tryToExit() {
		if (BarAPI.getNumberOfDownloadedFiles() != BarAPI.getNumberOfFileToDownload() &&
				JOptionPane.showConfirmDialog(LauncherFrame.getInstance(),
						"Voulez-vous vraiment interrompre le téléchargement en cours ?", "Téléchargement en cours",
						JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE
				) != JOptionPane.YES_OPTION)
			return;

		System.exit(0);
	}

	public static AuthInfos getAuthInfos() {
		return authInfos;
	}

	private static InputStream getResourceAsStream(String name)
	{
		return Swinger.class.getResourceAsStream("/" + name);
	}
}
