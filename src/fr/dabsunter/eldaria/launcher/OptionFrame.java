package fr.dabsunter.eldaria.launcher;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import static fr.dabsunter.eldaria.launcher.EldariaLauncher.ED_SAVER;
import static fr.dabsunter.eldaria.launcher.EldariaLauncher.browseOnDesktop;

/**
 * Created by David on 30/10/2016.
 */
public class OptionFrame extends JDialog implements MouseListener {

	private static OptionFrame instance;

	private JLabel memoryLabel = new JLabel("RAM allouée");
	private JComboBox<AllowedMemory> memoryComboBox = new JComboBox<>(
			new AllowedMemory[]{AllowedMemory.XMX512M, AllowedMemory.XMX1G, AllowedMemory.XMX2G, AllowedMemory.XMX4G, AllowedMemory.XMX6G}
	);
	private JButton saveButton = new JButton("Valider");
	private JLabel dabsLabel = new JLabel("<html><u>Développé par Dabsunter</u></html>");

	public static OptionFrame getInstance() {
		if (instance == null)
			instance = new OptionFrame();
		return instance;
	}

	private OptionFrame() {
		super(LauncherFrame.getInstance(), "Options", true);

		setSize(250, 150);
		setResizable(false);
		setDefaultCloseOperation(HIDE_ON_CLOSE);
		setLocationRelativeTo(LauncherFrame.getInstance());
		setLayout(null);

		memoryLabel.setBounds(30, 0, 200, 70);
		add(memoryLabel);

		memoryComboBox.setBounds(150, 23, 70, 25);
		add(memoryComboBox);

		saveButton.setBounds(75, 70, 100, 30);
		saveButton.addMouseListener(this);
		add(saveButton);

		dabsLabel.setForeground(Color.BLUE);
		dabsLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		dabsLabel.setBounds(115, 100, 200, 25);
		dabsLabel.addMouseListener(this);
		add(dabsLabel);
	}

	@Override
	public void setVisible(boolean b) {
		if (b) {
			try {
				AllowedMemory am = AllowedMemory.valueOf(ED_SAVER.get("allowed-memory", "XMX1G"));
				memoryComboBox.setSelectedItem(am);
			} catch (IllegalArgumentException ex) {
				memoryComboBox.setSelectedIndex(1);
			}
		}

		super.setVisible(b);
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if (e.getSource() == saveButton)
		{
			ED_SAVER.set("allowed-memory", ((AllowedMemory) memoryComboBox.getSelectedItem()).name());
			setVisible(false);
		}
		else if (e.getSource() == dabsLabel)
		{
			browseOnDesktop("https://github.com/Dabsunter");
		}
	}

	@Override
	public void mousePressed(MouseEvent e) {}

	@Override
	public void mouseReleased(MouseEvent e) {}

	@Override
	public void mouseEntered(MouseEvent e) {}

	@Override
	public void mouseExited(MouseEvent e) {}
}
