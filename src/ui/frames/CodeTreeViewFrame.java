package ui.frames;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;

import model.modtree.ModGenericLeaf;
import model.modtree.ModOffsetLeaf;
import model.modtree.ModOperandNode;
import model.modtree.ModReferenceLeaf;
import model.modtree.ModTree;
import model.modtree.ModTreeNode;

/**
 * Displays the current code in the editor as a tree view in a new frame.
 * @author Amineri
 */

@SuppressWarnings("serial")
public class CodeTreeViewFrame extends JFrame{
	
	/**
	 * The reference to the current modfile tree structure.
	 */
	private ModTree modTree;
	
	/**
	 * Constructs the application's main frame.
	 * @param modTree
	 */
	public CodeTreeViewFrame(ModTree modTree) {
		// instantiate frame
		super();
		
		// store passed ModTree reference
		this.modTree = modTree;
		
		// set title
		this.setTitle("Tree View: " + this.modTree.getFunctionName());
		
		// create and lay out the frame's components
		try {
			this.initComponents();
		} catch (Exception e) {
//			Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, e);
			e.printStackTrace();
		}
		
		// re-route default closing behavior to close() method
		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent evt) {
				close();
			}
		});
		
		// adjust frame size
		this.pack();
		// center frame in screen
		this.setLocationRelativeTo(null);
		// show frame
		this.setVisible(true);
	}

	/**
	 * Creates and lays out the frame's components.
	 * @throws Exception if an I/O error occurs
	 */
	private void initComponents() throws Exception {
		
		// configure content pane layout
		Container contentPane = this.getContentPane();
		
		final JTree modElemTree = new JTree(this.modTree.getRoot()); 
		JScrollPane modElemTreePane = new JScrollPane(modElemTree,
				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		modElemTreePane.setPreferredSize(new Dimension(640, 480));
		// configure look and feel of tree viewer
		modElemTree.setRootVisible(false);
		modElemTree.putClientProperty("JTree.lineStyle", "Angled");
		modElemTree.setShowsRootHandles(false);
		// display alternate operand text info for opened ModOperandNodes
		DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer() {
			@Override
			public Component getTreeCellRendererComponent(JTree tree,
					Object value, boolean sel, boolean expanded, boolean leaf,
					int row, boolean hasFocus) {
				if (((value instanceof ModOperandNode) && expanded )) {
					value = ((ModTreeNode) value).toString(expanded);
				} else if((value instanceof ModReferenceLeaf) 
						|| (value instanceof ModGenericLeaf) 
						|| (value instanceof ModOffsetLeaf)) {
					value = ((ModTreeNode) value).toString(true);
				}
				Component comp = super.getTreeCellRendererComponent(tree, value, sel, expanded,
						leaf, row, hasFocus);
				comp.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
				return comp;
			}
		};
		renderer.setLeafIcon(null);
		renderer.setClosedIcon(null);
		renderer.setOpenIcon(null);
		modElemTree.setCellRenderer(renderer);	
		
		// install document listener to refresh tree on changes to the document
//		modTree.getDocument().addDocumentListener(new DocumentListener() {
		modTree.addTreeModelListener(new TreeModelListener() {
			@Override
			public void treeNodesInserted(TreeModelEvent evt) {
				this.updateTree(evt);
			}
			@Override
			public void treeNodesChanged(TreeModelEvent evt) {
				this.updateTree(evt);
			}
			@Override
			public void treeNodesRemoved(TreeModelEvent evt) {
				this.updateTree(evt);
			}
			@Override
			public void treeStructureChanged(TreeModelEvent evt) {
				this.updateTree(evt);
			}
			/** Updates the tree views on document changes */
			private void updateTree(TreeModelEvent evt) {
				// reset mod tree
				((DefaultTreeModel) modElemTree.getModel()).setRoot(
						modTree.getRoot());
			}
		});
		
		
		contentPane.add(modElemTreePane);

	}
	
	/**
	 * Disposes the frame.
	 */
	private void close() {
		// TODO: do some clean-up if necessary
		this.dispose();
	}

}
