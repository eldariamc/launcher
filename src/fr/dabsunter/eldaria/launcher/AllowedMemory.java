package fr.dabsunter.eldaria.launcher;

import java.util.Arrays;
import java.util.List;

/**
 * Created by David on 30/10/2016.
 */
public enum AllowedMemory {

	XMX512M("512 Mo", "-Xmx512M"),
	XMX1G("1 Go", "-Xmx1G"),
	XMX2G("2 Go", "-Xmx2G", "-Xms1G"),
	XMX4G("4 Go", "-Xmx4G", "-Xms2G"),
	XMX6G("6 Go", "-Xmx6G", "-Xms3G");

	private String name;
	private List<String> vmArgs;

	AllowedMemory(String name, String... vmArgs) {
		this.name = name;
		this.vmArgs = Arrays.asList(vmArgs);
	}

	@Override
	public String toString() {
		return name;
	}

	public List<String> getVmArgs() {
		return vmArgs;
	}
}
