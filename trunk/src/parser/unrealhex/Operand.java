package parser.unrealhex;

/**
 * Model class representing an UnrealScript byte code operand
 * @author XMS
 */
public class Operand implements Comparable<Operand> {
	
	/**
	 * The opcode identifier.
	 */
	private String opcode;

	/**
	 * The operand name.
	 */
	private String name;

	/**
	 * The operand description.
	 */
	private String description;
	
	/**
	 * The operand tokens. 
	 */
	private OperandToken[] tokens;
	
	/**
	 * Constructs an operand from the specified opcode and operand tokens.
	 * @param opcode the opcode
	 * @param tokens the operand tokens
	 */
	public Operand(String opcode, String name, String description, OperandToken... tokens) {
		this.opcode = opcode;
		this.name = name;
		this.description = description;
		this.tokens = tokens;
	}

	/**
	 * Returns the opcode.
	 * @return the opcode
	 */
	public String getOpcode() {
		return opcode;
	}

	/**
	 * Returns the operand name.
	 * @return the operand name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns the operand description.
	 * @return the operand description
	 */
	public String getDescription() {
		return description;
	}
	
	/**
	 * Returns the operand tokens.
	 * @return the operand tokens
	 */
	public OperandToken[] getTokens() {
		return tokens;
	}
	
	@Override
	public int compareTo(Operand that) {
		return this.opcode.compareTo(that.opcode);
	}

	/**
	 * Basic model class representing an operand token.
	 * @author XMS
	 */
	public static class OperandToken {
		
		/**
		 * Enumeration holding operand token types.
		 * @author XMS
		 */
		public enum OperandTokenType {
			REFERENCE,
			GENERIC,
			INDETERMINATE_00,
			INDETERMINATE_16,
			CASE,
			JUMP,
			ABSOLUTE_SKIP,
			CONTEXT_SKIP,
			OBJECT_SKIP,
			PARAMETER_SKIP;
		}
		
		/**
		 * The token type.
		 */
		private OperandTokenType type;
		
		/**
		 * The token byte size.
		 */
		private int size;
		
		/**
		 * Constructs an operand token of the specified type with the specified
		 * byte size.
		 * 
		 * @param type the token type
		 * @param size the token byte size
		 */
		public OperandToken(OperandTokenType type, int size) {
			this.type = type;
			this.size = size;
		}

		/**
		 * Returns the token type.
		 * @return the token type
		 */
		public OperandTokenType getType() {
			return type;
		}
		
		/**
		 * Returns the token byte size.
		 * @return the token byte size
		 */
		public int getSize() {
			return this.size;
		}
		
	}

}
