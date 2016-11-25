import java.util.*;

/**
 * Abstract base class for abstract syntax tree (AST) nodes. Provides some
 * basic definitions used across many nodes, such as {@link DataType} definitions
 * and {@link attributes} management. AST nodes are designed to be
 * semi-mutable by means of the {@link attributes} map that is stored in every
 * node. Several of the more important attributes (e.g., parent links and tree
 * depth) are widely used and thus have special handlers implemented in this
 * class. However, this class is not intended to be instantiated directly,
 * and thus is declared {@code abstract}.
 *
 * List of potential attributes (not exhaustive):
 *
 * <table border="1">
 * <tr><th>Key</th><th>Description</th></tr>
 * <tr><td>{@code parent}</td><td>Uptree parent {@link ASTNode} reference</td></tr>
 * <tr><td>{@code depth}</td><td>Tree depth ({@code int})</td></tr>
 * <tr><td>{@code source}</td><td>{@link SourceInfo} reference</td></tr>
 * <tr><td>{@code symbolTable}</td><td>{@link SymbolTable} reference (only in {@link ASTProgram}, {@link ASTFunction}, and {@link ASTBlock})</td></tr>
 * <tr><td>{@code type}</td><td>{@link ASTNode.DataType} of node (only in {@link ASTExpression} subclasses)</td></tr>
 * <tr><td>{@code staticSize}</td><td>Size (in bytes) of global variables (only in {@link ASTProgram})</td></tr>
 * <tr><td>{@code localSize}</td><td>Size (in bytes) of local variables (only in {@link ASTFunction})</td></tr>
 * <caption>Potential attributes</caption>
 * </table>
 */
public abstract class ASTNode
{
    /**
     * List of Decaf AST data types.
     */
    public enum DataType { INT, BOOL, VOID, STR, UNKNOWN }

    public static String typeToString(DataType t)
    {
        switch (t) {
            case INT:       return "int";
            case BOOL:      return "bool";
            case VOID:      return "void";
            case STR:       return "str";
            case UNKNOWN:   return "unknown";
            default:        return "???";
        }
    }

    public Map<String, Object> attributes;

    public ASTNode()
    {
        attributes = new HashMap<String, Object>();
    }

    /**
     * Returns a string with the AST class type (mainly used for debugging)
     * @return
     */
    public String getASTTypeStr()
    {
        return getClass().getName().replace("edu.jmu.decaf.AST", "");
    }

    /**
     * Initialize the uptree parent pointer
     * @param parent
     */
    public void setParent(ASTNode parent)
    {
        attributes.put("parent", parent);
    }

    /**
     * Retrieve the uptree parent pointer
     * @return Parent {@link ASTNode} reference or {@code null} if the
     * corresponding attribute is not present
     */
    public ASTNode getParent()
    {
        ASTNode parent = null;
        if (attributes.containsKey("parent")) {
            parent = (ASTNode)attributes.get("parent");
        }
        return parent;
    }

    /**
     * Initialize the tree depth marker
     * @param depth
     */
    public void setDepth(int depth)
    {
        attributes.put("depth", Integer.valueOf(depth));
    }

    /**
     * Retrieve the tree depth marker
     * @return Depth (as an integer) or -1 if the corresponding attribute is
     * not present.
     */
    public int getDepth()
    {
        int depth = -1;
        if (attributes.containsKey("depth")) {
            depth = ((Integer)attributes.get("depth")).intValue();
        }
        return depth;
    }

    public void traverse(ASTVisitor visitor) { }
}

