package ui;

import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.plaf.nimbus.AbstractRegionPainter;

import ui.frames.MainFrame;

/**
 * Container class for storing application-wide actions, e.g. 'New', 'Open', 'Save', etc.
 * @author XMS
 */
@SuppressWarnings("serial")
public class ActionCache {
	
	/**
	 * The actual action cache.
	 */
	private static Map<String, Action> actionCache;

	/**
	 * Private constructor to prevent instantiation of this class.
	 */
	private ActionCache() {};
	
	/**
	 * Returns the action associated with the specified name.
	 * @param name the action name
	 * @return the desired action or <code>null</code> if no action with the specified name exists
	 */
	public static Action getAction(String name) {
		return actionCache.get(name);
	}

	/**
	 * Creates the cache of shared UI actions.
	 * @param mainFrame
	 */
	public static void initActionCache(final MainFrame mainFrame) {
		Map<String, Action> cache = new HashMap<>();
		
		/* Project actions */
		// new project
		Action newProjectAction = new BrowseAbstractAction("New Project", mainFrame, null) {
			@Override
			public void execute(File file) {
				mainFrame.createNewProject(file.toPath());
			}
		};
		newProjectAction.putValue(Action.SMALL_ICON, UIManager.getIcon("FileChooser.newFolderIcon"));
		newProjectAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK + InputEvent.SHIFT_DOWN_MASK));
		newProjectAction.putValue(Action.MNEMONIC_KEY, (int) 'n');
		newProjectAction.putValue(Action.SHORT_DESCRIPTION, "New Project");

		// open project
		Action openProjectAction = new BrowseAbstractAction("Open Project...", mainFrame, Constants.XML_FILE_FILTER) {
			@Override
			public void execute(File file) {
				mainFrame.openProject(file.toPath());
			}
		};
		openProjectAction.putValue(Action.SMALL_ICON, UIManager.getIcon("FileView.directoryIcon"));
		openProjectAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK + InputEvent.SHIFT_DOWN_MASK));
		openProjectAction.putValue(Action.MNEMONIC_KEY, (int) 'o');
		openProjectAction.putValue(Action.SHORT_DESCRIPTION, "Open Project");
				
		// close project
		Action removeProjectAction = new AbstractAction("Close Project") {
			@Override
			public void actionPerformed(ActionEvent evt) {
				mainFrame.removeProject();
			}
		};
		removeProjectAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_DOWN_MASK + InputEvent.SHIFT_DOWN_MASK));
		removeProjectAction.putValue(Action.MNEMONIC_KEY, (int) 'r');
		removeProjectAction.putValue(Action.SHORT_DESCRIPTION, "Remove Project");
		removeProjectAction.setEnabled(false);
		
		// delete project
		Action deleteProjectAction = new AbstractAction("Delete Project") {
			@Override
			public void actionPerformed(ActionEvent e) {
				mainFrame.deleteProject();
			}
		};
		deleteProjectAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.CTRL_DOWN_MASK + InputEvent.SHIFT_DOWN_MASK));
		deleteProjectAction.putValue(Action.MNEMONIC_KEY, (int) 'd');
		deleteProjectAction.putValue(Action.SHORT_DESCRIPTION, "Delete Project");
		deleteProjectAction.setEnabled(false);

		/* Mod File actions */
		// new mod file
		Action newModFileAction = new AbstractAction("New Mod File") {
			@Override
			public void actionPerformed(ActionEvent evt) {
				mainFrame.createNewModFile();
			}
		};
		newModFileAction.putValue(Action.SMALL_ICON, UIManager.getIcon("FileView.fileIcon"));
		newModFileAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK));
		newModFileAction.putValue(Action.MNEMONIC_KEY, (int) 'n');
		newModFileAction.putValue(Action.SHORT_DESCRIPTION, "New Mod File");
		newModFileAction.setEnabled(false);

		// open mod file
		Action openModFileAction = new BrowseAbstractAction("Open Mod File...", mainFrame, Constants.MOD_FILE_FILTER) {
			@Override
			public void execute(File file) {
				mainFrame.openModFile(file.toPath());
			}
		};
		openModFileAction.putValue(Action.SMALL_ICON, UIManager.getIcon("FileView.directoryIcon"));
		openModFileAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK));
		openModFileAction.putValue(Action.MNEMONIC_KEY, (int) 'p');
		openModFileAction.putValue(Action.SHORT_DESCRIPTION, "Open Mod File");
				
		// close mod file
		Action closeModFileAction = new AbstractAction("Close Mod File") {
			@Override
			public void actionPerformed(ActionEvent evt) {
				mainFrame.closeModFile();
			}
		};
		closeModFileAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_W, InputEvent.CTRL_DOWN_MASK));
		closeModFileAction.putValue(Action.MNEMONIC_KEY, (int) 'c');
		closeModFileAction.putValue(Action.SHORT_DESCRIPTION, "Close Mod File");
		closeModFileAction.setEnabled(false);
				
		// close all mods
		Action closeAllModFilesAction = new AbstractAction("Close All Mod Files") {
			@Override
			public void actionPerformed(ActionEvent evt) {
				mainFrame.closeAllModFiles();
			}
		};
		closeAllModFilesAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_W, InputEvent.CTRL_DOWN_MASK + InputEvent.SHIFT_DOWN_MASK));
		closeAllModFilesAction.putValue(Action.MNEMONIC_KEY, (int) 'l');
		closeAllModFilesAction.putValue(Action.SHORT_DESCRIPTION, "Close All Mod Files");
		closeAllModFilesAction.setEnabled(false);
		
		// save
		Action saveAction = new AbstractAction("Save Mod File") {
			@Override
			public void actionPerformed(ActionEvent evt) {
				mainFrame.saveModFile();
			}
		};
		saveAction.putValue(Action.SMALL_ICON, UIManager.getIcon("FileView.floppyDriveIcon"));
		saveAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK));
		saveAction.putValue(Action.MNEMONIC_KEY, (int) 's');
		saveAction.putValue(Action.SHORT_DESCRIPTION, "Save");
		saveAction.setEnabled(false);
				
		// save as
		final Action saveAsAction = new BrowseAbstractAction("Save Mod File As...", mainFrame, Constants.MOD_FILE_FILTER, true) {
			@Override
			public File getTarget() {
				return mainFrame.getActiveModFile().toFile();
			}
			@Override
			public void execute(File file) {
				mainFrame.saveModFileAs(file.toPath());
			}
		};
		saveAsAction.putValue(Action.SMALL_ICON, UIManager.getIcon("FileView.floppyDriveIcon"));
		saveAsAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK + InputEvent.ALT_DOWN_MASK));
		saveAsAction.putValue(Action.MNEMONIC_KEY, (int) 'a');
		saveAsAction.putValue(Action.SHORT_DESCRIPTION, "Save Mod File As...");
		saveAsAction.setEnabled(false);
		
		/* General File menu actions */
		// export
		// TODO: implement export functionality, create file filters
		Action exportAction = new BrowseAbstractAction("Export...", mainFrame, null, true) {
			@Override
			public void execute(File file) {
				// TODO: do something
			}
		};
		exportAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.CTRL_DOWN_MASK));
		exportAction.putValue(Action.MNEMONIC_KEY, (int) 'e');
		exportAction.setEnabled(false);
				
		// exit
		Action exitAction = new AbstractAction("Exit") {
			@Override
			public void actionPerformed(ActionEvent evt) {
				// dispose main frame (thereby terminating the application)
				mainFrame.dispose();
			}
		};
		exitAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_F4, InputEvent.ALT_DOWN_MASK));
		exitAction.putValue(Action.MNEMONIC_KEY, (int) 'x');
		
		/* Edit menu actions */
		// update references
		Action refUpdateAction = new AbstractAction("Update References...") {
			@Override
			public void actionPerformed(ActionEvent evt) {
				mainFrame.showReferenceUpdateDialog();
			}
		};
		refUpdateAction.putValue(Action.SMALL_ICON, UIManager.getIcon("FileChooser.detailsViewIcon"));
		refUpdateAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_U, InputEvent.CTRL_DOWN_MASK));
		refUpdateAction.putValue(Action.MNEMONIC_KEY, (int) 'u');
		refUpdateAction.putValue(Action.SHORT_DESCRIPTION, "Update References...");
		refUpdateAction.setEnabled(false);
		
		// test mod file status
		Action testApplyStatusAction = new AbstractAction("Test Apply State") {
			@Override
			public void actionPerformed(ActionEvent e) {
				mainFrame.testModFileStatus();
			}
		};
		testApplyStatusAction.putValue(Action.SMALL_ICON, Constants.TEST_MOD_STATUS_ICON);
		testApplyStatusAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_T, InputEvent.CTRL_DOWN_MASK + InputEvent.ALT_DOWN_MASK));
		testApplyStatusAction.putValue(Action.MNEMONIC_KEY, (int) 't');
		testApplyStatusAction.putValue(Action.SHORT_DESCRIPTION, "Test Apply State");
		testApplyStatusAction.setEnabled(false);
				
		// apply hex
		Action hexApplyAction = new AbstractAction("Apply Hex Changes") {
			@Override
			public void actionPerformed(ActionEvent evt) {
				mainFrame.applyModFile();
			}
		}; 
		hexApplyAction.putValue(Action.SMALL_ICON, Constants.APPLY_ACTION_ICON);
		hexApplyAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.CTRL_DOWN_MASK + InputEvent.ALT_DOWN_MASK));
		hexApplyAction.putValue(Action.MNEMONIC_KEY, (int) 'a');
		hexApplyAction.putValue(Action.SHORT_DESCRIPTION, "Apply Hex Changes");
		hexApplyAction.setEnabled(false);
				
		// revert hex
		Action hexRevertAction = new AbstractAction("Revert Hex Changes") {
			@Override
			public void actionPerformed(ActionEvent e) {
				mainFrame.revertModFile();
			}
		};
		hexRevertAction.putValue(Action.SMALL_ICON, Constants.REVERT_ACTION_ICON);
		hexRevertAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_DOWN_MASK + InputEvent.ALT_DOWN_MASK));
		hexRevertAction.putValue(Action.MNEMONIC_KEY, (int) 'r');
		hexRevertAction.putValue(Action.SHORT_DESCRIPTION, "Revert Hex Changes");
		hexRevertAction.setEnabled(false);
		
		/* Help menu actions */
		// create help and about icons
		BufferedImage helpImg = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2 = helpImg.createGraphics();
		((AbstractRegionPainter) UIManager.get("OptionPane[Enabled].questionIconPainter")).paint(
				g2, null, 16, 16);
		g2.dispose();
		Icon helpIcon = new ImageIcon(helpImg);
		BufferedImage aboutImg = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
		g2 = aboutImg.createGraphics();
		((AbstractRegionPainter) UIManager.get("OptionPane[Enabled].informationIconPainter")).paint(
				g2, null, 16, 16);
		g2.dispose();
		Icon aboutIcon = new ImageIcon(aboutImg);
		
		// help dialog
		Action helpAction = new AbstractAction("Help") {
			@Override
			public void actionPerformed(ActionEvent evt) {
				mainFrame.showHelpDialog();
			}
		};
		helpAction.putValue(Action.SMALL_ICON, helpIcon);
		helpAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));
		helpAction.putValue(Action.MNEMONIC_KEY, (int) 'h');
		helpAction.setEnabled(false);
				
		// about dialog
		Action aboutAction = new AbstractAction("About") {
			@Override
			public void actionPerformed(ActionEvent evt) {
				mainFrame.showAboutDialog();
			}
		};
		aboutAction.putValue(Action.SMALL_ICON, aboutIcon);
		aboutAction.putValue(Action.MNEMONIC_KEY, (int) 'a');
		
		/* Misc actions */
		Action associateUpkAction = new BrowseAbstractAction(null, mainFrame, Constants.UPK_FILE_FILTER) {
			@Override
			public void execute(File file) {
				mainFrame.associateUpk(file.toPath());
			}
		};
		associateUpkAction.putValue(Action.SMALL_ICON, UIManager.getIcon("FileView.directoryIcon"));
		associateUpkAction.putValue(Action.SHORT_DESCRIPTION, "Associate UPK File");
		associateUpkAction.setEnabled(false);
		
		Action toggleLogAction = new AbstractAction("Message Log") {
			@Override
			public void actionPerformed(ActionEvent evt) {
				mainFrame.toggleLogDialog();
			}
		};
		toggleLogAction.putValue(Action.SMALL_ICON, UIManager.getIcon("FileChooser.listViewIcon"));
		toggleLogAction.putValue(Action.SHORT_DESCRIPTION, "Toggle Message Log");
		
		// file actions
		cache.put("newProject", newProjectAction);
		cache.put("openProject", openProjectAction);
		cache.put("removeProject", removeProjectAction);
		cache.put("deleteProject", deleteProjectAction);
		
		cache.put("newModFile", newModFileAction);
		cache.put("openModFile", openModFileAction);
		cache.put("closeModFile", closeModFileAction);
		cache.put("closeAllModFiles", closeAllModFilesAction);
		cache.put("saveModFile", saveAction);
		cache.put("saveModFileAs", saveAsAction);
		
		cache.put("export", exportAction);
		
		cache.put("exit", exitAction);
		
		// edit actions
		cache.put("refUpdate", refUpdateAction);
		
		cache.put("hexApply", hexApplyAction);
		cache.put("hexRevert", hexRevertAction);
		cache.put("testFile", testApplyStatusAction);
		
		// help actions
		cache.put("help", helpAction);
		cache.put("about", aboutAction);
		
		// misc actions
		cache.put("associateUpk", associateUpkAction);
		cache.put("toggleLog", toggleLogAction);
		
		actionCache = Collections.unmodifiableMap(cache);
	}
	
}
