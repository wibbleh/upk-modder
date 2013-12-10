package model.modtree;

import model.modtree.ModContext.ModContextType;


/**
 *
 * @author Amineri
 */
public class ModTreeLeaf extends ModTreeNode {
    
	/**
	 * The string data of this token.
	 */
    protected String data;
    
	public ModTreeLeaf(ModTreeNode parent) {
		this(parent, "");
	}
    
	public ModTreeLeaf(ModTreeNode parent, String data) {
		this(parent, data, false);
	}
    
	public ModTreeLeaf(ModTreeNode parent, String data, boolean isSimpleString) {
		super(parent);
		init(parent);
		this.data = data;
		this.isSimpleString = isSimpleString;
		name = "ModToken";
	}
    
    @Override
    public int getMemorySize()
    {
        if(isSimpleString) {
            return -1;
        } else {
            return data.length()/3;
        }
    }
    
    private void init(ModTreeNode o)
    {
        this.parent = o;
        this.isSimpleString = false;
        setContextFlag(ModContextType.HEX_CODE, this.isCode());
        setContextFlag(ModContextType.VALID_CODE, false);
        setContextFlag(ModContextType.HEX_HEADER, false);
        setContextFlag(ModContextType.BEFORE_HEX, false);
        setContextFlag(ModContextType.AFTER_HEX, false);
    }

    
    @Override
    protected String getString()
    {
        return data;
		}

    @Override
    protected void setString(String s)
    {
        data = s;
    }

    @Override
    public boolean isVFFunctionRef()
    {
        return false;
    }
    
    @Override
    public int getOffset()
    {
        return -1;
    }

    @Override
    public int getRefValue()
    {
        return -1;
    }
    
	String parseUnrealHex(String s, int num) {
		int endOffset = this.getEndOffset();
		for (int i = 0; i < num; i++) {
			endOffset += 3;
			data += s.split("\\s", 2)[0] + " ";
			s = s.split("\\s", 2)[1];
		}
		this.setRange(this.getStartOffset(), endOffset);
		return s;
	}

    /**
     * Returns true if element is leaf node (eg "token" )
     * IMPLEMENTED
     * @return
     */
    @Override
    public boolean isLeaf()
    {
        return true;
    }

}
