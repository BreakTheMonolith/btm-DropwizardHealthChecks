package guru.breakthemonolith.docker;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.exception.ContextedRuntimeException;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

/**
 * Utility class to execute Docker-related commands locally. This assumes that
 * command 'docker ps' works on the machine that this is executed on.
 * 
 * @author D. Ashmore
 *
 */
public class DockerCommandUtils {

	/**
	 * Issues 'docker pull' command.
	 * 
	 * @param imageName including tag reference
	 */
	public static void dockerPull(String imageName) {
		Validate.notBlank(imageName, "Null or blank imageName not allowed.");
		issueCommand(new String[] { "docker", "pull", imageName }, "Error with 'docker pull'",
				new Pair[] { new ImmutablePair("imageName", imageName) });
	}

	/**
	 * Kills and deletes a running container ('docker rm -f').
	 * 
	 * @param containerName
	 */
	public static void dockerKillContainer(String containerName) {
		Validate.notBlank(containerName, "Null or blank containerName not allowed.");
		issueCommand(new String[] { "docker", "rm", "-f", containerName }, "Error with 'docker rm'",
				new Pair[] { new ImmutablePair("containerName", containerName) });
	}

	/**
	 * Lists all running Docker containers (issues 'docker ps').
	 * 
	 * @param containerName
	 */
	public static void dockerContainerListing() {
		issueCommand(new String[] { "docker", "ps" }, "Error with 'docker ps'");
	}

	/**
	 * Issues a 'docker run' command.
	 * 
	 * @param dockerRunSpecification
	 */
	public static String dockerRun(DockerRunSpecification dockerRunSpecification) {
		Validate.notNull(dockerRunSpecification, "Null dockerRunSpecification not allowed.");
		Validate.notBlank(dockerRunSpecification.getImageName(), "Null or blank imageName not allowed.");

		String containerName = UUID.randomUUID().toString();

		List<String> commandItemList = new ArrayList<String>();
		commandItemList.add("docker");
		commandItemList.add("run");
		commandItemList.add("--name");
		commandItemList.add(containerName);

		if (dockerRunSpecification.isDetachInd()) {
			commandItemList.add("-d");
		}

		if (dockerRunSpecification.getVolumeMap().size() > 0) {
			for (Map.Entry<String, String> volume : dockerRunSpecification.getVolumeMap().entrySet()) {
				commandItemList.add("-v");
				commandItemList.add("'" + volume.getKey() + "':'" + volume.getValue() + "'");
			}
		}

		if (dockerRunSpecification.getPortMap().size() > 0) {
			for (Map.Entry<String, String> port : dockerRunSpecification.getPortMap().entrySet()) {
				commandItemList.add("-p");
				commandItemList.add(port.getKey() + ":" + port.getValue());
			}
		}

		if (dockerRunSpecification.getEnvironmentMap().size() > 0) {
			for (Map.Entry<String, String> envVariable : dockerRunSpecification.getEnvironmentMap().entrySet()) {
				commandItemList.add("-e");
				if (StringUtils.isEmpty(envVariable.getValue())) {
					commandItemList.add(envVariable.getKey());
				} else {
					commandItemList.add(envVariable.getKey() + ":" + envVariable.getValue());
				}
			}
		}

		commandItemList.add(dockerRunSpecification.getImageName());

		if (StringUtils.isNotBlank(dockerRunSpecification.getCommand())) {
			for (String commandPart : StringUtils.split(dockerRunSpecification.getCommand(), ' ')) {
				commandItemList.add(commandPart);
			}
		}

		String[] commandArray = commandItemList.toArray(new String[0]);
		String commandStr = StringUtils.join(commandArray, ' ');

		System.out.println("Docker command: " + commandStr);
		try {
			Process docker = new ProcessBuilder(commandArray).inheritIO().start();
			if (dockerRunSpecification.isDetachInd()) {
				Thread.sleep(dockerRunSpecification.getDetachedWaitTimeMillis());
			} else {
				waitForThrowingException(docker);
			}
		} catch (Exception e) {
			throw new ContextedRuntimeException("Error with 'docker run'", e)
					.addContextValue("containerName", containerName)
					.addContextValue("dockerRunSpecification", dockerRunSpecification)
					.addContextValue("commandStr", commandStr);
		}

		return containerName;
	}

	private static void issueCommand(String[] commandArray, String errorMessage) {
		issueCommand(commandArray, errorMessage, null);
	}

	private static void issueCommand(String[] commandArray, String errorMessage,
			Pair<String, Object>[] errorContextValues) {
		String commandStr = StringUtils.join(commandArray, ' ');

		System.out.println("Docker command: " + commandStr);
		try {
			Process docker = new ProcessBuilder(commandArray).inheritIO().start();
			waitForThrowingException(docker);
		} catch (Exception e) {
			ContextedRuntimeException cEx = new ContextedRuntimeException("Error with 'docker run'", e)
					.addContextValue("commandStr", commandStr);
			if (errorContextValues != null) {
				for (Pair<String, Object> pair : errorContextValues) {
					cEx.addContextValue(pair.getKey(), pair.getValue());
				}
			}
			throw cEx;
		}
	}

	private static void waitForThrowingException(Process process) throws InterruptedException {
		int rc = process.waitFor();
		if (rc != 0) {
			throw new RuntimeException("Command Error reported");
		}
	}
}
