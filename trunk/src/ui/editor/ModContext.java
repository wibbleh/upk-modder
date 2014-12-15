package ui.editor;

import static model.modtree.ModContext.ModContextType.HEX_CODE;
import static model.modtree.ModContext.ModContextType.VALID_CODE;

import java.awt.Color;
import java.util.Enumeration;

import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import static model.modtree.ModContext.ModContextType.AFTER_HEX;
import static model.modtree.ModContext.ModContextType.BEFORE_HEX;

import model.modtree.ModOffsetLeaf;
import model.modtree.ModReferenceLeaf;
import model.modtree.ModTreeNode;

/**
 * Style cache for mod file editor contents.
 * 
 * @author XMS
 */
@SuppressWarnings("serial")
public class ModContext extends StyleContext {
	
	/* style name constants */
	private static final String COMMENT_STYLE = "comment";
	private static final String REFERENCE_STYLE = "reference";
	private static final String VIRTUAL_FUNCTION_REFERENCE_STYLE = "vfreference";
	private static final String INVALID_HEX_STYLE = "invalid";
	private static final String OPERAND_STYLE = "operand";
	private static final String NOTHING_OPERAND_STYLE = "nothingoperand";
	private static final String VALID_PRECEDING_OFFSET_STYLE = "precedingoffset";
	private static final String VALID_SUCCEEDING_OFFSET_STYLE = "succedingoffset";
	private static final String INVALID_OFFSET_STYLE = "invalidoffset";
	private static final String RELATIVE_OFFSET_STYLE = "relativeoffset";

	/**
	 * Creates a default mod file editor style cache.
	 */
	public ModContext() {
		super();
		
		this.initDefaults();
	}

	/**
	 * Populates the pool of styles with default values.
	 */
	private void initDefaults() {
		// default style
		Style defaultStyle = this.getStyle(DEFAULT_STYLE);
		
		// comment style
		Style commentStyle = this.addStyle(COMMENT_STYLE, defaultStyle);
		commentStyle.addAttribute(StyleConstants.Foreground, new Color(128, 128, 128));
		commentStyle.addAttribute(StyleConstants.Italic, true);
		
		// reference style
		Style referenceStyle = this.addStyle(REFERENCE_STYLE, defaultStyle);
		referenceStyle.addAttribute(StyleConstants.Foreground, new Color(160, 140, 100));
		referenceStyle.addAttribute(StyleConstants.Underline, true);
		// virtual function reference style
		Style vfReferenceStyle = this.addStyle(VIRTUAL_FUNCTION_REFERENCE_STYLE, (Style) referenceStyle);
		vfReferenceStyle.addAttribute(StyleConstants.Foreground, new Color(220, 180, 50));
		
		// invalid hex style
		Style invalidHexStyle = this.addStyle(INVALID_HEX_STYLE, defaultStyle);
		invalidHexStyle.addAttribute(StyleConstants.Foreground, new Color(255, 128, 128));
		invalidHexStyle.addAttribute(StyleConstants.StrikeThrough, true);
		
		// operand style
		Style operandStyle = this.addStyle(OPERAND_STYLE, defaultStyle);
		operandStyle.addAttribute(StyleConstants.Foreground, Color.BLUE);
		operandStyle.addAttribute(StyleConstants.Bold, true);
		// 0B operand style
		Style nothingOperandStyle = this.addStyle(NOTHING_OPERAND_STYLE, (Style) operandStyle);
		nothingOperandStyle.addAttribute(StyleConstants.Bold, false);
		
		// preceding offset style
		Style precedingOffsetStyle = this.addStyle(VALID_PRECEDING_OFFSET_STYLE, defaultStyle);
		precedingOffsetStyle.addAttribute(StyleConstants.Background, new Color( 240, 180, 255));  // lilac
		// succeeding offset style
		Style succeedingOffsetStyle = this.addStyle(VALID_SUCCEEDING_OFFSET_STYLE, defaultStyle);
		succeedingOffsetStyle.addAttribute(StyleConstants.Background, new Color( 115, 255, 150));  // green
		// invalid offset style
		Style invalidOffsetStyle = this.addStyle(INVALID_OFFSET_STYLE, (Style) succeedingOffsetStyle);
		invalidOffsetStyle.addAttribute(StyleConstants.Background, new Color( 255, 200, 100)); // orange
		// relative offset style
		Style relativeOffsetStyle = this.addStyle(RELATIVE_OFFSET_STYLE, (Style) succeedingOffsetStyle);
		relativeOffsetStyle.addAttribute(StyleConstants.Background, new Color(255, 255, 180)); // yellow
		
	}
	
	public String getStyleNameByNode(ModTreeNode node) {
		
		// plain text, may be comment
		if (node.isPlainText()) {
			// find comment marker
			String s = node.getFullText();
			if (s.contains("//")) {
				return COMMENT_STYLE;
			} else {
				return DEFAULT_STYLE;
			}
		}
		
		// references
		if (node instanceof ModReferenceLeaf) {
			if (node.isVirtualFunctionRef()) {
				return VIRTUAL_FUNCTION_REFERENCE_STYLE;
			} else {
				return REFERENCE_STYLE;
			}
		}
		
		// invalid hex
		if ((node.getContextFlag(HEX_CODE) && !node.getContextFlag(VALID_CODE))) {
			return INVALID_HEX_STYLE;
		}

		// operands
		if ("OperandToken".equals(node.getName())) {
			if (node.getFullText().toUpperCase().startsWith("0B")) {
				return NOTHING_OPERAND_STYLE;
			} else {
				return OPERAND_STYLE;
			}
		}

		// jump offsets
		if (node instanceof ModOffsetLeaf) {
			ModOffsetLeaf offsetLeaf = (ModOffsetLeaf) node;
			if (offsetLeaf.getOperand() == null) {
				// absolute jump offset
				boolean isValid = false;
				boolean before = false;
				boolean after = false;

				// FIXME: the logic for determining offset validity should be located inside the node itself and ideally would need to be run once per tree change
				// can't do this while parsing the tree, because forward lines haven't been parsed -- this also will only run once per tree change anyhow
				ModTreeNode root = node;
				while (root.getParentNode() != null) {
					root = root.getParentNode();
				}
				Enumeration<ModTreeNode> children = root.children();
				while (children.hasMoreElements()) {
					ModTreeNode child = children.nextElement();
					if ((offsetLeaf.getOffset() == child.getMemoryPosition()) 
							&& ((offsetLeaf.getLineParent().getContextFlag(BEFORE_HEX) && child.getLineParent().getContextFlag(BEFORE_HEX))
							|| (offsetLeaf.getLineParent().getContextFlag(AFTER_HEX) && child.getLineParent().getContextFlag(AFTER_HEX)))) {
						isValid = true;
						if(offsetLeaf.getLineParent().getMemoryPosition() < child.getMemoryPosition()) {
							after = true;
						} else {
							if(offsetLeaf.getLineParent().getMemoryPosition() > child.getMemoryPosition()) {
								before = true;
							} else {
								isValid = false;
							}
						}
						break;
					}
					
				}
				
				if (isValid) {
					if (after) {
						return VALID_SUCCEEDING_OFFSET_STYLE;
					} else {
						if (before) {
							return VALID_PRECEDING_OFFSET_STYLE;
						} else {
							return INVALID_OFFSET_STYLE;
						}
					}
					
				} else {
					return INVALID_OFFSET_STYLE;
				}
			} else {
				// relative jump offset
				return RELATIVE_OFFSET_STYLE;
			}
		}
		
		// fall-back value
		return DEFAULT_STYLE;
	}

}
