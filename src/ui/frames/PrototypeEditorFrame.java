package ui.frames;

import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.EventObject;

import javax.swing.DefaultCellEditor;
import javax.swing.Icon;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.CellEditorListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.basic.BasicTreeUI;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Document;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellEditor;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import ui.editor.ModEditorKit;
import ui.frames.PrototypeEditorFrame.CodeBlockNode.CodeHexNode;
import ui.frames.PrototypeEditorFrame.HeaderBlockNode.HeaderHexNode;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

/**
 * TODO: API
 * @author XMS
 */
@SuppressWarnings("serial")
public class PrototypeEditorFrame extends JFrame {

	/**
	 * TODO: API
	 */
	public PrototypeEditorFrame() {
		super("Prototype Modfile Editor");
		
		this.initComponents();

		this.pack();
		this.setLocationRelativeTo(null);
	}

	private void initComponents() {
		Container contentPane = this.getContentPane();
		
		MutableTreeNode editorRoot = new DefaultMutableTreeNode("name of modfile");
		
		ModFileTreeModel editorMdl = new ModFileTreeModel(editorRoot);

		editorMdl.insertNodeInto(new AttributeNode("MODFILEVERSION", 3), editorRoot, editorRoot.getChildCount());
		editorMdl.insertNodeInto(new AttributeNode("UPKFILE", "XComStrategyGame.upk"), editorRoot, editorRoot.getChildCount());
		editorMdl.insertNodeInto(new AttributeNode("GUID", "F8 43 46 A3 80 60 96 49 A4 27 0B 1C 3B 66 7E 33 // XComStrategyGame_EU_patch6.upk"), editorRoot, editorRoot.getChildCount());
		
		Document beforeHeaderDoc = new DefaultStyledDocument();
		Document beforeCodeDoc = new DefaultStyledDocument();
		Document afterHeaderDoc = new DefaultStyledDocument();
		Document afterCodeDoc = new DefaultStyledDocument();
		try {
			beforeHeaderDoc.insertString(0, "57 00 00 00 4B 00 00 00", null);
			beforeCodeDoc.insertString(0, "04 82 19 1B 8B 16 00 00 00 00 00 00 16 0C 00 77 2C 00 00 00 1B BF 14 00 00 00 00 00 00 2C 03 16 18 25 00 81 19 1B 8B 16 00 00 00 00 00 00 16 0C 00 6B 2C 00 00 00 1B E0 14 00 00 00 00 00 00 24 07 16 16 16 04 3A A1 2E 00 00 53", null);
			afterHeaderDoc.insertString(0, "53 00 00 00 4B 00 00 00", null);
			afterCodeDoc.insertString(0,
					"// PRES().UINarrative(xcomnarrativemoment'AlienBase', none, NP) // removing callback enables cinematic to finish without crashing\n" +
					"19 1B 12 22 00 00 00 00 00 00 16 29 00 66 FF FF FF 00 1B 27 2B 00 00 00 00 00 00 20 29 48 00 00 2A 4A 4A 4A 4A 4A 16 \n" +
					"// null-ops\n" +
					"0B 0B 0B 0B 0B 0B 0B 0B 0B 0B 0B 0B 0B 0B 0B 0B 0B 0B 0B 0B 0B 0B 0B 0B 0B 0B 0B 0B 0B 0B 0B 0B 0B \n" +
					"// return false;\n" +
					"04 28 \n" +
					"// EOS\n" +
					"53 ", null);
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
		
		FunctionNode function = editorMdl.addFunction("IsCodeActive@XGFacility_SituationRoom", beforeCodeDoc, afterCodeDoc);
		editorMdl.addHeader(function.getBeforeBlock(), beforeHeaderDoc);
		editorMdl.addHeader(function.getAfterBlock(), afterHeaderDoc);
		
		final JTree editorTree = new JTree(editorMdl);
		
		for (int row = 0; row < editorTree.getRowCount(); row++) {
			editorTree.expandRow(row);
		}
		
		ModFileTreeCellEditor renderer = new ModFileTreeCellEditor(editorTree);
		editorTree.setCellRenderer(renderer);
		editorTree.setEditable(true);
		editorTree.setCellEditor(renderer);
		editorTree.setInvokesStopCellEditing(true);
		editorTree.setRowHeight(0);
		
		editorTree.addMouseMotionListener(new MouseAdapter() {
			@Override
			public void mouseMoved(MouseEvent evt) {
				TreePath path = editorTree.getPathForLocation(evt.getX(), evt.getY());
				if (path != null) {
					Object node = path.getLastPathComponent();
					if (node instanceof ModFileTreeNode) {
						if (((ModFileTreeNode) node).isEditable()) {
							editorTree.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
							return;
						}
					}
				}
				editorTree.setCursor(Cursor.getDefaultCursor());
			}
		});
		
		JScrollPane editorScpn = new JScrollPane(editorTree,
				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		editorScpn.setPreferredSize(new Dimension(800, 600));
		
		contentPane.add(editorScpn);
	}

	/**
	 * TODO: API
	 * @author XMS
	 */
	public class ModFileTreeCellEditor extends DefaultTreeCellEditor implements TreeCellRenderer {

		private DefaultCellEditor defaultEditor;
		
		public ModFileTreeCellEditor(JTree tree) {
			super(tree, (DefaultTreeCellRenderer) tree.getCellRenderer());
			this.defaultEditor = (DefaultCellEditor) realEditor;
		}
		
		@Override
		public boolean isCellEditable(EventObject event) {
			if (event instanceof MouseEvent) {
				MouseEvent me = (MouseEvent) event;
				TreePath path = tree.getPathForLocation(me.getX(), me.getY());
				Object node = path.getLastPathComponent();
				if (node instanceof ModFileTreeNode) {
					return ((ModFileTreeNode) node).isEditable();
				}
			}
			return false;
		}
		
		@Override
		public Component getTreeCellRendererComponent(final JTree tree, Object value,
				boolean sel, boolean expanded, boolean leaf, final int row, boolean hasFocus) {
			
			if (value instanceof AttributeNode) {
				return this.createAttributeTextComponent((AttributeNode) value, expanded, leaf);
			} else if (value instanceof HeaderHexNode) {
				return this.createHeaderTextComponent((HeaderHexNode) value);
			} else if (value instanceof CodeHexNode) {
				return this.createCodeTextComponent((CodeHexNode) value);
			}
			
			return renderer.getTreeCellRendererComponent(
					tree, value, sel, expanded, leaf, row, hasFocus);
		}

		@Override
		public Component getTreeCellEditorComponent(JTree tree, Object value,
				boolean sel, boolean expanded, boolean leaf, int row) {
			
			Component rendererComp = this.getTreeCellRendererComponent(
					tree, value, sel, expanded, leaf, row, true);
			
			if ((value instanceof AttributeNode)) {
				realEditor = new DefaultCellEditor(
						(JTextField) ((Container) rendererComp).getComponent(1));
				for (CellEditorListener l : defaultEditor.getCellEditorListeners()) {
					realEditor.addCellEditorListener(l);
				}
			} else if (value instanceof HeaderHexNode) {
				realEditor = new DefaultCellEditorExt(
						(JTextField) ((Container) rendererComp).getComponent(1));
				for (CellEditorListener l : defaultEditor.getCellEditorListeners()) {
					realEditor.addCellEditorListener(l);
				}
			} else if (value instanceof CodeHexNode) {
//				realEditor = new DefaultCellEditorExt(
//						(JEditorPane) ((Container) rendererComp).getComponent(1));
				JEditorPane editor = (JEditorPane) ((JScrollPane) ((Container) rendererComp).getComponent(1)).getViewport().getView();
				realEditor = new DefaultCellEditorExt(editor);
				for (CellEditorListener l : defaultEditor.getCellEditorListeners()) {
					realEditor.addCellEditorListener(l);
				}
			}
			
			return rendererComp;
		}

		/**
		 * TODO: API
		 * @author XMS
		 */
		private JPanel createAttributeTextComponent(AttributeNode attrNode, boolean expanded, boolean leaf) {
		
			final String attrName = attrNode.getName() + "=";
			Object attrValue = attrNode.getUserObject();
			if (attrValue == null) {
				attrValue = "";
			}
			
			final JPanel attrPnl = new JPanel(new FormLayout("p, p:g", "p"));
			attrPnl.setOpaque(false);
		
			Icon icon = (leaf) ? renderer.getLeafIcon() :
				(expanded) ? renderer.getOpenIcon() : renderer.getClosedIcon();
			JLabel iconLbl = new JLabel(icon);
			
			final JTextField attrTtf = new JTextField(attrValue.toString()) {
				@Override
				protected void paintComponent(Graphics g) {
					super.paintComponent(g);
					Graphics2D g2d = (Graphics2D) g;
					g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
					g2d.drawString(attrName, 6, this.getBaseline(this.getWidth(), this.getHeight()));
				}
				@Override
				public Dimension getPreferredSize() {
					Dimension size = super.getPreferredSize();
					size.width += 1;
					return size;
				}
			};
		
			FontMetrics fm = attrTtf.getFontMetrics(attrTtf.getFont());
			attrTtf.setMargin(new Insets(0, fm.stringWidth(attrName), 0, 0));
			
			attrTtf.getDocument().addDocumentListener(new DocumentListener() {
				public void removeUpdate(DocumentEvent evt) { this.updateRow(evt); }
				public void insertUpdate(DocumentEvent evt) { this.updateRow(evt); }
				public void changedUpdate(DocumentEvent evt) { this.updateRow(evt); }
				
				private void updateRow(DocumentEvent evt) {
					attrPnl.revalidate();
					attrPnl.setSize(attrPnl.getPreferredSize());
				}
			});
			attrTtf.addCaretListener(new CaretListener() {
				@Override
				public void caretUpdate(CaretEvent evt) {
					try {
						Rectangle caretRect = SwingUtilities.convertRectangle(
								attrTtf, attrTtf.modelToView(evt.getDot()), tree);
						
						// check whether caret trailed off the right edge of the tree
						if ((caretRect.x + 5) > tree.getSize().width) {
							// hack to force tree to recalculate its size
							((BasicTreeUI) tree.getUI()).setLeftChildIndent(12);
						}
						
						// scroll with caret
						tree.scrollRectToVisible(caretRect);
					} catch (BadLocationException e) {
						e.printStackTrace();
					}
				}
			});
			
			attrPnl.add(iconLbl, CC.xy(1, 1));
			attrPnl.add(attrTtf, CC.xy(2, 1));
			
			return attrPnl;
		}
		
		/**
		 * TODO: API
		 * @param hexNode
		 * @return
		 */
		private JPanel createHeaderTextComponent(HeaderHexNode hexNode) {
			
			final JPanel headerPnl = new JPanel(new FormLayout("p, p:g", "p"));
			headerPnl.setOpaque(false);
		
			Icon icon = renderer.getLeafIcon();
			JLabel iconLbl = new JLabel(icon);
			
			Document headerDoc = (Document) hexNode.getUserObject();
			
			final JTextField headerTtf = new JTextField() {
				@Override
				public Dimension getPreferredSize() {
					Dimension size = super.getPreferredSize();
					size.width += 1;
					return size;
				}
			};
			headerTtf.setDocument(headerDoc);
			
			headerDoc.addDocumentListener(new DocumentListener() {
				public void removeUpdate(DocumentEvent evt) { this.updateRow(evt); }
				public void insertUpdate(DocumentEvent evt) { this.updateRow(evt); }
				public void changedUpdate(DocumentEvent evt) { this.updateRow(evt); }
				
				private void updateRow(DocumentEvent evt) {
					headerPnl.revalidate();
					headerPnl.setSize(headerPnl.getPreferredSize());
				}
			});
			headerTtf.addCaretListener(new CaretListener() {
				@Override
				public void caretUpdate(CaretEvent evt) {
					try {
						Rectangle caretRect = SwingUtilities.convertRectangle(
								headerTtf, headerTtf.modelToView(evt.getDot()), tree);
						
						// check whether caret trailed off the right edge of the tree
						if ((caretRect.x + 5) > tree.getSize().width) {
							// hack to force tree to recalculate its size
							((BasicTreeUI) tree.getUI()).setLeftChildIndent(12);
						}
						
						// scroll with caret
						tree.scrollRectToVisible(caretRect);
					} catch (BadLocationException e) {
						e.printStackTrace();
					}
				}
			});
			
			headerPnl.add(iconLbl, CC.xy(1, 1));
			headerPnl.add(headerTtf, CC.xy(2, 1));
			
			return headerPnl;
		}
		
		private JPanel createCodeTextComponent(CodeHexNode hexNode) {
			
			final JPanel codePnl = new JPanel(new FormLayout("p, p:g", "p"));
			codePnl.setOpaque(false);
		
			Icon icon = renderer.getLeafIcon();
			JLabel iconLbl = new JLabel(icon);
			
			Document codeDoc = (Document) hexNode.getUserObject();
			
			final JEditorPane codePane = new JEditorPane();
			codePane.setEditorKit(new ModEditorKit());
			codePane.setDocument(codeDoc);
			
			codeDoc.addDocumentListener(new DocumentListener() {
				public void removeUpdate(DocumentEvent evt) { this.updateRow(evt); }
				public void insertUpdate(DocumentEvent evt) { this.updateRow(evt); }
				public void changedUpdate(DocumentEvent evt) { this.updateRow(evt); }
				
				private void updateRow(DocumentEvent evt) {
					codePnl.revalidate();
					codePnl.setSize(codePnl.getPreferredSize());
				}
			});
			codePane.addCaretListener(new CaretListener() {
				@Override
				public void caretUpdate(CaretEvent evt) {
					try {
						Rectangle caretRect = SwingUtilities.convertRectangle(
								codePane, codePane.modelToView(evt.getDot()), tree);
						
						// check whether caret trailed off the right edge of the tree
						if ((caretRect.x + 5) > tree.getSize().width) {
							// hack to force tree to recalculate its size
							((BasicTreeUI) tree.getUI()).setLeftChildIndent(12);
						}
						
						// scroll with caret
						tree.scrollRectToVisible(caretRect);
					} catch (BadLocationException e) {
						e.printStackTrace();
					}
				}
			});
			
			codePnl.add(iconLbl, CC.xy(1, 1));
//			codePnl.add(codePane, CC.xy(2, 1));
			codePnl.add(new JScrollPane(codePane), CC.xy(2, 1));
			
			return codePnl;
		}
		
		/**
		 * TODO: API
		 * @author XMS
		 */
		private class DefaultCellEditorExt extends DefaultCellEditor {

			/**
			 * TODO: API
			 * @param textField
			 */
			public DefaultCellEditorExt(final JTextField textField) {
				super(textField);
				textField.removeActionListener(delegate);
				delegate = new EditorDelegate() {
					@Override
					public void setValue(Object value) {
						textField.setDocument((Document) value);
					}
					@Override
					public Object getCellEditorValue() {
						return textField.getDocument();
					}
				};
				textField.addActionListener(delegate);
			}

			/**
			 * TODO: API
			 * @param editorPane
			 */
			public DefaultCellEditorExt(final JEditorPane editorPane) {
				// invoke super constructor using dummy textfield
				super(new JTextField());
				
				editorComponent = editorPane;
		        delegate = new EditorDelegate() {
		        	@Override
					public void setValue(Object value) {
		        		editorPane.setDocument((Document) value);
		            }
					@Override
		            public Object getCellEditorValue() {
		                return editorPane.getDocument();
		            }
		        };
			}
			
		}
		
	}
	
	/**
	 * TODO: API
	 * @author XMS
	 */
	public class ModFileTreeModel extends DefaultTreeModel {

		public ModFileTreeModel(TreeNode root) {
			super(root);
		}
		
		public void addFunction() {
			this.addFunction("", new DefaultStyledDocument(), new DefaultStyledDocument());
		}
		
		public FunctionNode addFunction(String name, Document docBefore, Document docAfter) {
			FunctionNode functionNode = new FunctionNode(name, docBefore, docAfter);
			this.insertNodeInto(functionNode,
					(MutableTreeNode) root,
					this.getChildCount(root));
			return functionNode;
		}
		
		public void addHeader(BlockNode block, Document doc) {
			if (!block.hasHeader()) {
				this.insertNodeInto(new HeaderBlockNode(doc), block, 0);
			}
		}
		
		public void removeHeader(BlockNode block) {
			if (block.hasHeader()) {
				this.removeNodeFromParent((MutableTreeNode) block.getFirstChild());
			}
		}
		
	}
	
	/**
	 * TODO: API
	 * @author XMS
	 *
	 */
	public abstract class ModFileTreeNode extends DefaultMutableTreeNode {
		
		public ModFileTreeNode(Object userObject) {
			super(userObject);
		}

		public abstract boolean isEditable();
		
	}
	
	/**
	 * TODO: API
	 * @author XMS
	 */
	public class AttributeNode extends ModFileTreeNode {
		
		private String name;
	
		public AttributeNode(String name, Object value) {
			super(value);
			this.name = name;
		}
		
		public String getName() {
			return name;
		}

		@Override
		public boolean isEditable() {
			return true;
		}
		
		@Override
		public String toString() {
			return name + "=" + userObject;
		}
		
	}

	/**
	 * TODO: API
	 * @author XMS
	 */
	public class FunctionNode extends AttributeNode {
		
		private BlockNode beforeBlock;
		private BlockNode afterBlock;
		
		public FunctionNode(String functionName, Document docBefore, Document docAfter) {
			super("FUNCTION", functionName);
			
			beforeBlock = new BlockNode("BEFORE");
			beforeBlock.add(new CodeBlockNode(docBefore));

			afterBlock = new BlockNode("AFTER");
			afterBlock.add(new CodeBlockNode(docAfter));
			
			this.add(beforeBlock);
			this.add(afterBlock);
		}

		public BlockNode getBeforeBlock() {
			return beforeBlock;
		}
		
		public BlockNode getAfterBlock() {
			return afterBlock;
		}

		@Override
		public boolean isEditable() {
			return true;
		}
		
		@Override
		public String toString() {
			return "FUNCTION=" + this.userObject;
		}
		
	}
	
	/**
	 * TODO: API
	 * @author XMS
	 */
	public class BlockNode extends ModFileTreeNode {
		
		public BlockNode(String name) {
			super(name);
		}
		
		public boolean hasHeader() {
			return (this.getFirstChild() instanceof HeaderBlockNode);
		}

		@Override
		public boolean isEditable() {
			return false;
		}
		
	}

	/**
	 * TODO: API
	 * @author XMS
	 */
	public class HeaderBlockNode extends BlockNode {
		
		public HeaderBlockNode(Document doc) {
			super("HEADER");
			this.add(new HeaderHexNode(doc));
		}

		/**
		 * TODO: API
		 * @author XMS
		 */
		public class HeaderHexNode extends ModFileTreeNode {
			
			public HeaderHexNode(Document hex) {
				super(hex);
			}

			@Override
			public boolean isEditable() {
				return true;
			}
			
		}
		
	}

	/**
	 * TODO: API
	 * @author XMS
	 */
	public class CodeBlockNode extends BlockNode {
		
		public CodeBlockNode(Document doc) {
			super("CODE");
			this.add(new CodeHexNode(doc));
		}

		/**
		 * TODO: API
		 * @author XMS
		 */
		public class CodeHexNode extends ModFileTreeNode {
			
			public CodeHexNode(Document hex) {
				super(hex);
			}

			@Override
			public boolean isEditable() {
				return true;
			}
			
		}
		
	}
	
}
