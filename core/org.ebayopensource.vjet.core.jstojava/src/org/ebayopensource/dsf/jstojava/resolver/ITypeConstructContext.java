/**
 * 
 */
package org.ebayopensource.dsf.jstojava.resolver;

import java.util.List;

import org.ebayopensource.dsf.jst.IJstType;
import org.ebayopensource.dsf.jst.JstSource;
import org.ebayopensource.dsf.jst.token.IExpr;
import org.ebayopensource.dsf.jstojava.translator.robust.completion.IJstCompletion;

/**
 * A type construction context that holds information about the left-hand side
 * expression, input expressions and the constructed types.
 * 
 * @author paragraval
 * 
 */
public interface ITypeConstructContext {

	/**
	 * Returns non-null input expression node that needs to be resolved.
	 * 
	 * @return
	 */
	public IExpr getInputExpr();

	/**
	 * Returns the left-hand-side of expression, if available.
	 * 
	 * @return
	 */
	public IExpr getLhsExpr();

	/**
	 * Returns list of expressions for input arguments.
	 * 
	 * @return
	 */
	public List<IExpr> getArgsExpr();

	/**
	 * Returns non-modifiable list of {@link IJstType}, if resolved.
	 * 
	 * @return
	 */
	public List<IJstType> getTypes();

	/**
	 * Adds the given type to the list of resolved types.
	 * 
	 * @param jstType
	 * @return true, if the given type is added.
	 */
	public boolean addType(IJstType jstType);

	/**
	 * Returns the {@link IJstCompletion} node
	 * 
	 * @return
	 */
	public IJstCompletion getCompletion();
	

	public Class<? extends IExpr> getExprClass();
	
	public String getGroupName();
	
	/**
	 * Get the working source info where type is defined
	 * @return
	 */
	public JstSource getWorkingSource();
	
	public String getOriginalTypeName();



}
