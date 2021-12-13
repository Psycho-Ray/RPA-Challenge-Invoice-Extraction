package dev.botcity.modules;

import dev.botcity.framework.bot.DesktopBot;
import dev.botcity.framework.bot.UIElement;
import dev.botcity.main.NotFoundException;
import org.marvinproject.framework.io.MarvinImageIO;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@SuppressWarnings({"unused", "SameParameterValue"})
public class BaseModule extends DesktopBot {
	//Init
	protected int miniDelay = 5000;
	protected int maxDelay = 10000;
	protected int threshold = 230;
	protected double matching = 0.95;
	protected String rootError = "Prints/";
	protected String extraPath = "";
	protected String debugPath = "";

	public BaseModule() {
		setResourceClassLoader(this.getClass().getClassLoader());
		preInit();
	}

	protected void preInit() {

	}

	public static String timeStamp() {
		return new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss").format(new Date());
	}

	public void doubleClickAt(int x, int y) {
		doubleClickAt(x, y, 200);
	}

	public void doubleClickAt(int x, int y, int sleepBetweenClicks) {
		clickAt(x, y);
		wait(sleepBetweenClicks);
		clickAt(x, y);
	}

	public void tripleClickAt(int x, int y) {
		doubleClickAt(x, y, 200);
	}

	public void tripleClickAt(int x, int y, int sleepBetweenClicks) {
		doubleClickAt(x, y, sleepBetweenClicks);
		wait(sleepBetweenClicks);
		clickAt(x, y);
	}

	public void clickRelative(int x, int y, int waitAfter) {
		moveRelative(x, y);
		getRobot().mousePress(16);
		getRobot().mouseRelease(16);
	}

	public void dropDown(String option) {
		type(option.substring(0, 1));
	}

	public void dropDown(String option, int down) {
		dropDown(option);
		for (int i=0; i<down; i++) keyDown();
	}

	public String getPath() {
		return extraPath;
	}

	public String getDebugPath() {
		return rootError + debugPath;
	}

	public void notFound(String label) {
		System.out.println("Not found: " + label + ". Taking a Screen Shoot");
		try { printError("NotFound_" + label); }
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	public List<UIElement> quickFindAll(String label) throws NotFoundException, IOException {
		return quickFindAll(label, matching);
	}

	public List<UIElement> quickFindAll(String label, double customMatching) throws NotFoundException {
		List<UIElement> elementList = findAllUntil(extraPath + label, customMatching, maxDelay);
		if (elementList == null || elementList.isEmpty()) {
			notFound(label);
			throw new NotFoundException(label + " not found");
		}

		return elementList;
	}

	public void quickFind(String label) throws NotFoundException {
		quickFind(label, matching, maxDelay);
	}

	public void quickFind(String label, double customMatching) throws NotFoundException {
		quickFind(label, customMatching, maxDelay);
	}

	public void quickFind(String label, int customMaxDelay) throws NotFoundException {
		quickFind(label, matching, customMaxDelay);
	}

	public void quickFind(String label, double customMatching, int customMaxDelay) throws NotFoundException {
		quickFind(label, customMatching, customMaxDelay, label + " not found");
	}

	public void quickFind(String label, double customMatching, int customMaxDelay, String notFoundMessage) throws NotFoundException {
		if (!find(extraPath + label, customMatching, customMaxDelay, false)) {
			if (notFoundMessage != null) notFound(label);
			throw new NotFoundException(notFoundMessage);
		}
	}

	public void quickFind(String label, String notFoundMessage) throws NotFoundException {
		quickFind(label, matching, maxDelay, notFoundMessage);
	}

	public void quickFind(String label, int customDelay, String notFoundMessage) throws NotFoundException {
		quickFind(label, matching, customDelay, notFoundMessage);
	}

	public List<UIElement> quickCheckAll(String label) {
		return quickCheckAll(label, matching);
	}

	public List<UIElement> quickCheckAll(String label, double customMatching) {
		List<UIElement> elementList = findAllUntil(extraPath + label, customMatching, miniDelay);
		if (elementList == null) elementList = new ArrayList<>();
		return elementList;
	}

	public boolean quickCheck(String label) {
		return find(label, matching, miniDelay);
	}

	public boolean quickCheck(String label, int customMaxDelay) {
		return find(label, matching, customMaxDelay);
	}

	public void quickFindText(String label) throws NotFoundException, IOException {
		quickFindText(label, threshold);
	}

	public void quickFindText(String label, int customThreshold) throws NotFoundException {
		quickFindText(label, customThreshold, maxDelay);
	}

	public void quickFindText(String label, double customMatching) throws NotFoundException {
		quickFindText(label, threshold, customMatching);
	}

	public void quickFindText(String label, int customThreshold, double customMatching) throws NotFoundException {
		quickFindText(label, customThreshold, customMatching, maxDelay);
	}

	public void quickFindText(String label, int customThreshold, int customMaxDelay) throws NotFoundException {
		quickFindText(label, customThreshold, 0.9, customMaxDelay, label + " not found");
	}

	public void quickFindText(String label, int customThreshold, double customMatching, int customMaxDelay) throws NotFoundException {
		quickFindText(label, customThreshold, customMatching, customMaxDelay, label + " not found");
	}

	public void quickFindText(String label, int customThreshold, double customMatching, int customMaxDelay, String notFoundMessage) throws NotFoundException {
		if (!findText(extraPath + label, customThreshold, customMatching, customMaxDelay)) {
			if (notFoundMessage != null) notFound(notFoundMessage);
			throw new NotFoundException(notFoundMessage);
		}
	}

	public void quickFindText(String label, String notFoundMessage) throws NotFoundException, IOException {
		quickFindText(label, 180, 0.9, maxDelay, notFoundMessage);
	}

	@SuppressWarnings("UnusedReturnValue")
	public String printError(String fileName) throws IOException {
		return printScreen(timeStamp() + " - " + fileName);
	}

	public String printScreen(String fileName) throws IOException {
		return printScreen(fileName, getDebugPath());
	}

	public String printScreen(String fileName, String customFilePath) throws IOException {
		//Creates the folder, if it doesn't exist
		Files.createDirectories(Paths.get(customFilePath));

		//Saves a screenshot
		String filePath = customFilePath + fileName + ".png";
		MarvinImageIO.saveImage(getScreenShot(), filePath);

		//Returns the path to it
		return filePath;
	}

	public static void moveFile(File file, String folderPath) throws IOException {
		moveFile(file, folderPath, file.getName());
	}

	public static void moveFile(File file, String folderPath, String newName) throws IOException {
		//Moves the file, creating the folder if it doesn't exist
		Files.createDirectories(Paths.get(folderPath));
		Files.move(file.toPath(), Paths.get(folderPath + newName), StandardCopyOption.REPLACE_EXISTING);
	}
}
