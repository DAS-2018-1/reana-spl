package expressionsolver;

import jadd.ADD;
import jadd.JADD;

import java.util.HashMap;
import java.util.Map;

import org.nfunk.jep.JEP;
import org.nfunk.jep.SymbolTable;

import expressionsolver.functions.Add;
import expressionsolver.functions.Divide;
import expressionsolver.functions.LogicalAnd;
import expressionsolver.functions.LogicalNot;
import expressionsolver.functions.LogicalOr;
import expressionsolver.functions.Multiply;
import expressionsolver.functions.Subtract;
import expressionsolver.functions.UnaryMinus;

/**
 * @author thiago
 *
 */
public class ExpressionSolver {

    private JEP parser;
    private JADD jadd;

    /**
     * Solves expressions using the provided ADD manager.
     */
    public ExpressionSolver(JADD jadd) {
        this.jadd = jadd;
        parser = new JEP(false,
                         true,
                         false,
                         new ADDNumberFactory(jadd));
        parser.addFunction("\"+\"", new Add());
        parser.addFunction("\"-\":2", new Subtract());
        parser.addFunction("\"-\":1", new UnaryMinus());
        parser.addFunction("\"*\"", new Multiply());
        parser.addFunction("\"/\"", new Divide());

        parser.addFunction("\"&&\"", new LogicalAnd());
        parser.addFunction("\"||\"", new LogicalOr());
        parser.addFunction("\"!\"", new LogicalNot());
    }

    /**
     * Solves an expression with respect to the given interpretation of variables.
     * Here, variables are interpreted in the algebraic sense, not as boolean ADD-variables.
     *
     * @param expression
     * @param interpretation A map from variable names to the respective values
     *          to be considered during evaluation.
     * @return a (possibly constant) function (ADD) representing all possible results
     *      according to the ADDs involved.
     */
    public ADD solveExpression(String expression, Map<String, ADD> interpretation) {
        parser.parseExpression(expression);
        if (parser.hasError()) {
            System.err.println("Parser error: " + parser.getErrorInfo());
            return null;
        }

        SymbolTable symbolTable = parser.getSymbolTable();
        for (Object var: symbolTable.keySet()) {
            String varName = (String)var;
            if (interpretation.containsKey(varName)) {
                parser.addVariableAsObject(varName, interpretation.get(varName));
            } else {
                System.err.println("No interpretation for variable <"+varName+"> was provided");
            }
        }
        return (ADD)parser.getValueAsObject();

    }

    /**
     * Useful shortcut for expressions with no variables involved.
     * @param expression
     * @return
     */
    public ADD solveExpression(String expression) {
        return solveExpression(expression, new HashMap<String, ADD>());
    }

    /**
     * Encodes a propositional logic formula as a 0,1-ADD, which is roughly
     * equivalent to a BDD, but better suited to representing boolean
     * functions which interact with Real ADDs.
     *
     * If there are variables in the formula, they are interpreted as ADD variables.
     * These are internally indexed by name, so that multiple references in
     * (possibly multiple) expressions are taken to be the same.
     *
     * @param formula Propositional logic formula to be encoded. The valid
     *  boolean operators are && (AND), || (OR) and !(NOT).
     * @return
     */
    public ADD encodeFormula(String formula) {
        parser.parseExpression(formula);
        if (parser.hasError()) {
            System.err.println("Parser error: " + parser.getErrorInfo());
            return null;
        }

        SymbolTable symbolTable = parser.getSymbolTable();
        for (Object var: symbolTable.keySet()) {
            String varName = (String)var;
            ADD variable = jadd.getVariable(varName);
            parser.addVariableAsObject(varName, variable);
        }
        return (ADD)parser.getValueAsObject();
    }

}