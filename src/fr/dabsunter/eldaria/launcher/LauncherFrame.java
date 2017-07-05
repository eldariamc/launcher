package fr.dabsunter.eldaria.launcher;

import fr.theshark34.swinger.util.WindowMover;

import javax.swing.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import static fr.dabsunter.eldaria.launcher.EldariaLauncher.tryToExit;
import static fr.theshark34.swinger.Swinger.getResource;

/**
 * Created by David on 04/06/2016.
 */
public class LauncherFrame extends JFrame implements WindowListener
{
	private static LauncherFrame instance;
	private LauncherPanel panel;

	public LauncherFrame()
	{
		instance = this;

		setTitle(EldariaLauncher.ED_INFOS.getServerName());
		setIconImage(getResource("icon.png"));
		setSize(853, 609);
		setUndecorated(true);
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		setLocationRelativeTo(null);
		setLayout(null);

		WindowMover mover = new WindowMover(this);
		addMouseListener(mover);
		addMouseMotionListener(mover);

		addWindowListener(this);

		this.panel = new LauncherPanel();
		add(panel);
	}

	public static LauncherFrame getInstance()
	{
		return instance;
	}

	public LauncherPanel getPanel()
	{
		return panel;
	}

	@Override
	public void windowOpened(WindowEvent e) {}

	@Override
	public void windowClosing(WindowEvent e) {
		tryToExit();
	}

	@Override
	public void windowClosed(WindowEvent e) {}

	@Override
	public void windowIconified(WindowEvent e) {}

	@Override
	public void windowDeiconified(WindowEvent e) {}

	@Override
	public void windowActivated(WindowEvent e) {}

	@Override
	public void windowDeactivated(WindowEvent e) {}
}
